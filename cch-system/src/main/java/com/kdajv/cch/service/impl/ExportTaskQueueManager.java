package com.kdajv.cch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.kdajv.cch.domain.ChallengeVersionExportTask;
import com.kdajv.cch.enums.ExportTaskStatus;
import com.kdajv.cch.mapper.ChallengeVersionExportTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.system.service.ISysConfigService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 导出任务队列管理器
 * 负责控制最大并发数，自动启动待处理任务；多实例部署时通过 Redisson 分布式锁保证
 * 只有一个节点执行调度；处理中的僵尸任务（超过超时时间无进展）自动标记失败，可手动重试。
 *
 * @author system
 * @date 2026-01-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExportTaskQueueManager {

    private final ChallengeVersionExportTaskMapper taskMapper;
    private final ChallengeVersionExportExecutor executor;
    private final ISysConfigService sysConfigService;
    private final RedissonClient redissonClient;

    private static final String CONFIG_KEY_MAX_CONCURRENT = "cch.export.maxConcurrent";
    private static final int DEFAULT_MAX_CONCURRENT = 3;

    /**
     * 分布式锁键：同一时刻仅允许一个节点执行调度
     */
    private static final String SCHEDULE_LOCK_KEY = "cch:export:queue:schedule:lock";

    /**
     * 处理中任务的僵尸超时时间（分钟）：超过该时间无状态更新则判定为失败
     */
    private static final int STALE_TASK_TIMEOUT_MINUTES = 30;

    /**
     * 定时检查并启动待处理任务
     * 每10秒执行一次
     */
    @Scheduled(fixedRate = 10000)
    public void processPendingTasks() {
        RLock lock = redissonClient.getLock(SCHEDULE_LOCK_KEY);
        boolean locked = false;
        try {
            // 尝试立即获取锁，获取不到说明其他节点正在调度，直接跳过
            locked = lock.tryLock(0, 30, TimeUnit.SECONDS);
            if (!locked) {
                return;
            }
            // 恢复僵尸任务（处理中超时无进展的任务标记为失败）
            recoverStaleTasks();

            // 获取当前处理中的任务数量
            int processingCount = getProcessingTaskCount();

            // 获取最大并发数
            int maxConcurrent = getMaxConcurrent();

            // 计算可启动的任务数量
            int availableSlots = maxConcurrent - processingCount;
            if (availableSlots <= 0) {
                return; // 已达到最大并发数，不启动新任务
            }

            // 查询待处理的任务，按创建时间排序
            LambdaQueryWrapper<ChallengeVersionExportTask> lqw = Wrappers.lambdaQuery();
            lqw.eq(ChallengeVersionExportTask::getTaskStatus, ExportTaskStatus.PENDING.getCode()) // 待处理
                .orderByAsc(ChallengeVersionExportTask::getCreateTime)
                .last("LIMIT " + availableSlots);

            List<ChallengeVersionExportTask> pendingTasks = taskMapper.selectList(lqw);

            // 启动任务
            for (ChallengeVersionExportTask task : pendingTasks) {
                startTask(task);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("导出任务调度被中断");
        } catch (Exception e) {
            log.error("处理待处理任务时发生异常", e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 启动单个任务：CAS 抢占状态（0->1）成功后异步执行
     * <p>注意：此处不使用外层事务，状态更新即时提交，
     * 保证异步执行器在读取任务时一定能读到已提交的「处理中」状态。</p>
     *
     * @param task 待处理任务
     */
    private void startTask(ChallengeVersionExportTask task) {
        try {
            // 使用乐观锁更新状态，防止并发问题
            LambdaUpdateWrapper<ChallengeVersionExportTask> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(ChallengeVersionExportTask::getId, task.getId())
                .eq(ChallengeVersionExportTask::getTaskStatus, ExportTaskStatus.PENDING.getCode()) // 确保状态仍然是待处理
                .set(ChallengeVersionExportTask::getTaskStatus, ExportTaskStatus.PROCESSING.getCode()); // 设置为处理中

            int updated = taskMapper.update(null, updateWrapper);
            if (updated > 0) {
                // 状态更新成功，异步执行任务
                executor.executeExportTask(task.getId());
                log.debug("启动导出任务，任务ID: {}", task.getId());
            }
        } catch (Exception e) {
            log.error("启动导出任务失败，任务ID: {}", task.getId(), e);
        }
    }

    /**
     * 恢复僵尸任务：处理中状态但超过超时时间无进展的任务标记为失败
     * <p>典型场景：应用重启、异步线程被拒绝、节点宕机导致任务卡在处理中。</p>
     */
    private void recoverStaleTasks() {
        LocalDateTime staleBefore = LocalDateTime.now().minusMinutes(STALE_TASK_TIMEOUT_MINUTES);
        LambdaUpdateWrapper<ChallengeVersionExportTask> recoverWrapper = Wrappers.lambdaUpdate();
        recoverWrapper.eq(ChallengeVersionExportTask::getTaskStatus, ExportTaskStatus.PROCESSING.getCode())
            .lt(ChallengeVersionExportTask::getUpdateTime, staleBefore)
            .set(ChallengeVersionExportTask::getTaskStatus, ExportTaskStatus.FAILED.getCode())
            .set(ChallengeVersionExportTask::getErrorMessage, "任务处理超时（超过" + STALE_TASK_TIMEOUT_MINUTES + "分钟无进展），可手动重试");
        int recovered = taskMapper.update(null, recoverWrapper);
        if (recovered > 0) {
            log.warn("已恢复 {} 个僵尸导出任务（处理中超时，标记为失败）", recovered);
        }
    }

    /**
     * 获取当前处理中的任务数量
     */
    private int getProcessingTaskCount() {
        LambdaQueryWrapper<ChallengeVersionExportTask> lqw = Wrappers.lambdaQuery();
        lqw.eq(ChallengeVersionExportTask::getTaskStatus, ExportTaskStatus.PROCESSING.getCode()); // 处理中
        return taskMapper.selectCount(lqw).intValue();
    }

    /**
     * 获取最大并发数
     */
    private int getMaxConcurrent() {
        try {
            String value = sysConfigService.selectConfigByKey(CONFIG_KEY_MAX_CONCURRENT);
            if (StringUtils.isNotBlank(value)) {
                return Integer.parseInt(value);
            }
        } catch (Exception e) {
            log.warn("获取最大并发数配置失败，使用默认值: {}", DEFAULT_MAX_CONCURRENT, e);
        }
        return DEFAULT_MAX_CONCURRENT;
    }
}
