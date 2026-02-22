package com.kdajv.cch.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.kdajv.cch.domain.ChallengeVersionExportTask;
import com.kdajv.cch.mapper.ChallengeVersionExportTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.system.service.ISysConfigService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 导出任务队列管理器
 * 负责控制最大并发数，自动启动待处理任务
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

    private static final String CONFIG_KEY_MAX_CONCURRENT = "cch.export.maxConcurrent";
    private static final int DEFAULT_MAX_CONCURRENT = 3;

    /**
     * 定时检查并启动待处理任务
     * 每10秒执行一次
     */
    @Scheduled(fixedRate = 10000)
    @Transactional(rollbackFor = Exception.class)
    public void processPendingTasks() {
        try {
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
            lqw.eq(ChallengeVersionExportTask::getTaskStatus, 0) // 待处理
                .orderByAsc(ChallengeVersionExportTask::getCreateTime)
                .last("LIMIT " + availableSlots);

            List<ChallengeVersionExportTask> pendingTasks = taskMapper.selectList(lqw);

            // 启动任务
            for (ChallengeVersionExportTask task : pendingTasks) {
                try {
                    // 使用乐观锁更新状态，防止并发问题
                    LambdaUpdateWrapper<ChallengeVersionExportTask> updateWrapper = Wrappers.lambdaUpdate();
                    updateWrapper.eq(ChallengeVersionExportTask::getId, task.getId())
                        .eq(ChallengeVersionExportTask::getTaskStatus, 0) // 确保状态仍然是待处理
                        .set(ChallengeVersionExportTask::getTaskStatus, 1); // 设置为处理中

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
        } catch (Exception e) {
            log.error("处理待处理任务时发生异常", e);
        }
    }

    /**
     * 获取当前处理中的任务数量
     */
    private int getProcessingTaskCount() {
        LambdaQueryWrapper<ChallengeVersionExportTask> lqw = Wrappers.lambdaQuery();
        lqw.eq(ChallengeVersionExportTask::getTaskStatus, 1); // 处理中
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
