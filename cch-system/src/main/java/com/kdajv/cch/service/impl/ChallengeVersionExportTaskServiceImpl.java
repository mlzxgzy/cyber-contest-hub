package com.kdajv.cch.service.impl;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kdajv.cch.domain.ChallengeVersionExportTask;
import com.kdajv.cch.domain.bo.ChallengeVersionExportTaskBo;
import com.kdajv.cch.domain.vo.ChallengeVersionExportTaskVo;
import com.kdajv.cch.enums.ExportTaskStatus;
import com.kdajv.cch.mapper.ChallengeVersionExportTaskMapper;
import com.kdajv.cch.service.IChallengeVersionExportTaskService;
import com.kdajv.cch.service.IChallengeVersionService;
import com.kdajv.cch.service.IChallengeDraftService;
import com.kdajv.cch.domain.vo.ChallengeVersionVo;
import com.kdajv.cch.domain.vo.ChallengeDraftVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.service.ISysOssService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 题目版本导出任务Service业务层处理
 *
 * @author system
 * @date 2026-01-30
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ChallengeVersionExportTaskServiceImpl implements IChallengeVersionExportTaskService {

    private final ChallengeVersionExportTaskMapper baseMapper;
    private final IChallengeVersionService challengeVersionService;
    private final IChallengeDraftService challengeDraftService;
    private final ISysOssService sysOssService;

    /**
     * 批量操作最大数量限制，防止资源耗尽攻击
     */
    private static final int MAX_BATCH_SIZE = 100;

    /**
     * 单个任务最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 5;

    /**
     * 查询题目版本导出任务
     *
     * @param id 主键
     * @return 题目版本导出任务
     */
    @Override
    public ChallengeVersionExportTaskVo queryById(Long id) {
        ChallengeVersionExportTaskVo vo = baseMapper.selectVoById(id);
        if (vo != null) {
            enrichVo(vo);
        }
        return vo;
    }

    /**
     * 分页查询题目版本导出任务列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 题目版本导出任务分页列表
     */
    @Override
    public TableDataInfo<ChallengeVersionExportTaskVo> queryPageList(ChallengeVersionExportTaskBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ChallengeVersionExportTask> lqw = buildQueryWrapper(bo);
        Page<ChallengeVersionExportTaskVo> result = baseMapper.selectVoPage(pageQuery.build(), lqw);
        enrichVos(result.getRecords());
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的题目版本导出任务列表
     *
     * @param bo 查询条件
     * @return 题目版本导出任务列表
     */
    @Override
    public List<ChallengeVersionExportTaskVo> queryList(ChallengeVersionExportTaskBo bo) {
        LambdaQueryWrapper<ChallengeVersionExportTask> lqw = buildQueryWrapper(bo);
        List<ChallengeVersionExportTaskVo> list = baseMapper.selectVoList(lqw);
        enrichVos(list);
        return list;
    }

    /**
     * 创建导出任务
     *
     * @param versionId     题目版本ID
     * @param includeImages 是否导出容器镜像文件（true-导出镜像tar包，false-仅保留镜像地址）
     * @return 任务ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createExportTask(Long versionId, boolean includeImages) {
        // 输入验证：ID必须为正数
        if (versionId == null || versionId <= 0) {
            throw new ServiceException("版本ID无效");
        }

        // 查询版本信息
        ChallengeVersionVo version = challengeVersionService.queryById(versionId);
        if (version == null) {
            throw new ServiceException("题目版本不存在");
        }

        // 幂等校验：同版本同参数的任务已存在待处理/处理中记录时拒绝重复创建
        Long activeCount = baseMapper.selectCount(Wrappers.<ChallengeVersionExportTask>lambdaQuery()
            .eq(ChallengeVersionExportTask::getVersionId, versionId)
            .eq(ChallengeVersionExportTask::getIncludeImages, includeImages)
            .in(ChallengeVersionExportTask::getTaskStatus,
                ExportTaskStatus.PENDING.getCode(), ExportTaskStatus.PROCESSING.getCode()));
        if (activeCount != null && activeCount > 0) {
            throw new ServiceException(String.format("版本「%s」已有进行中的导出任务，请等待完成后再创建",
                version.getVersionTag()));
        }

        // 创建任务
        ChallengeVersionExportTask task = new ChallengeVersionExportTask();
        task.setVersionId(versionId);
        task.setVersionTag(version.getVersionTag());
        task.setIncludeImages(includeImages);
        task.setTaskStatus(ExportTaskStatus.PENDING.getCode()); // 待处理
        task.setRetryCount(0);
        baseMapper.insert(task);

        log.info("创建导出任务成功，任务ID: {}, 版本ID: {}, 包含镜像: {}", task.getId(), versionId, includeImages);
        return task.getId();
    }

    /**
     * 重试失败的导出任务
     * <p>仅失败状态的任务允许重试；重试次数达到上限后拒绝；
     * 重试时累加重试次数、重置为待处理状态并清空历史下载信息。</p>
     *
     * @param taskId 任务ID
     * @return 是否重试成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean retryExportTask(Long taskId) {
        if (taskId == null || taskId <= 0) {
            throw new ServiceException("任务ID无效");
        }
        ChallengeVersionExportTask task = baseMapper.selectById(taskId);
        if (task == null) {
            throw new ServiceException("导出任务不存在");
        }
        if (task.getTaskStatus() != ExportTaskStatus.FAILED.getCode()) {
            throw new ServiceException("仅失败的任务允许重试");
        }
        int retryCount = task.getRetryCount() == null ? 0 : task.getRetryCount();
        if (retryCount >= MAX_RETRY_COUNT) {
            throw new ServiceException(String.format("该任务已重试 %d 次，达到上限，请检查失败原因后删除重建", MAX_RETRY_COUNT));
        }

        task.setTaskStatus(ExportTaskStatus.PENDING.getCode());
        task.setRetryCount(retryCount + 1);
        // 清空历史产物信息，避免重试期间误导下载
        // （全局 updateStrategy=NOT_NULL，实体 null 字段不更新，需用 UpdateWrapper 显式 set null）
        LambdaUpdateWrapper<ChallengeVersionExportTask> luw = Wrappers.<ChallengeVersionExportTask>lambdaUpdate()
            .eq(ChallengeVersionExportTask::getId, taskId)
            .set(ChallengeVersionExportTask::getTaskStatus, ExportTaskStatus.PENDING.getCode())
            .set(ChallengeVersionExportTask::getRetryCount, retryCount + 1)
            .set(ChallengeVersionExportTask::getErrorMessage, null)
            .set(ChallengeVersionExportTask::getOssFileId, null)
            .set(ChallengeVersionExportTask::getOssFileName, null)
            .set(ChallengeVersionExportTask::getFileSize, null)
            .set(ChallengeVersionExportTask::getDownloadUrl, null)
            .set(ChallengeVersionExportTask::getExpireTime, null);
        baseMapper.update(null, luw);

        log.info("重试导出任务，任务ID: {}, 第 {} 次重试", taskId, retryCount + 1);
        return true;
    }

    /**
     * 批量创建导出任务
     *
     * @param versionIds    题目版本ID列表
     * @param includeImages 是否导出容器镜像文件（true-导出镜像tar包，false-仅保留镜像地址）
     * @return 任务ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> createExportTasks(List<Long> versionIds, boolean includeImages) {
        // 安全检查：限制批量操作数量，防止资源耗尽攻击
        if (versionIds == null || versionIds.isEmpty()) {
            throw new ServiceException("版本ID列表不能为空");
        }
        if (versionIds.size() > MAX_BATCH_SIZE) {
            throw new ServiceException("批量操作数量不能超过 " + MAX_BATCH_SIZE + " 个");
        }

        // 验证所有ID的有效性
        for (Long versionId : versionIds) {
            if (versionId == null || versionId <= 0) {
                throw new ServiceException("版本ID无效");
            }
        }

        return versionIds.stream()
            .map(id -> createExportTask(id, includeImages))
            .collect(Collectors.toList());
    }

    /**
     * 执行导出任务（异步调用）
     * 此方法由异步执行器调用，不在此处实现
     *
     * @param taskId 任务ID
     */
    @Override
    public void executeExportTask(Long taskId) {
        // 此方法由ChallengeVersionExportExecutor调用
        throw new UnsupportedOperationException("此方法应由ChallengeVersionExportExecutor调用");
    }

    /**
     * 获取下载链接
     *
     * @param taskId 任务ID
     * @return 临时下载链接
     */
    @Override
    public String getDownloadUrl(Long taskId) {
        // 输入验证：ID必须为正数
        if (taskId == null || taskId <= 0) {
            throw new ServiceException("任务ID无效");
        }

        ChallengeVersionExportTaskVo task = queryById(taskId);
        if (task == null) {
            throw new ServiceException("导出任务不存在");
        }
        if (task.getTaskStatus() != ExportTaskStatus.COMPLETED.getCode()) {
            throw new ServiceException("任务尚未完成，无法下载");
        }
        if (StringUtils.isBlank(task.getDownloadUrl())) {
            throw new ServiceException("下载链接不存在");
        }
        return task.getDownloadUrl();
    }

    /**
     * 清理过期文件（定时任务调用）
     *
     * @return 清理的文件数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupExpiredFiles() {
        LambdaQueryWrapper<ChallengeVersionExportTask> lqw = Wrappers.lambdaQuery();
        lqw.eq(ChallengeVersionExportTask::getTaskStatus, ExportTaskStatus.COMPLETED.getCode()) // 已完成
            .isNotNull(ChallengeVersionExportTask::getExpireTime)
            .lt(ChallengeVersionExportTask::getExpireTime, LocalDateTime.now());

        List<ChallengeVersionExportTask> expiredTasks = baseMapper.selectList(lqw);
        int count = 0;

        for (ChallengeVersionExportTask task : expiredTasks) {
            try {
                // 删除OSS文件
                if (task.getOssFileId() != null && task.getOssFileName() != null) {
                    try {
                        // 通过OSS服务删除文件
                        sysOssService.deleteWithValidByIds(List.of(task.getOssFileId()), false);
                        log.debug("删除过期OSS文件成功，文件ID: {}, 文件名: {}", task.getOssFileId(), task.getOssFileName());
                    } catch (Exception e) {
                        log.warn("删除OSS文件失败，文件ID: {}, 文件名: {}", task.getOssFileId(), task.getOssFileName(), e);
                        // 即使OSS删除失败，也继续更新任务状态
                    }
                }

                // 更新任务状态（标记为已过期，但不删除记录）
                // 注意：状态3表示失败，这里用于表示文件已过期被清理
                task.setTaskStatus(ExportTaskStatus.FAILED.getCode()); // 失败状态，表示已过期
                task.setErrorMessage("文件已过期，已被系统自动清理");
                task.setUpdateTime(DateTime.now());
                baseMapper.updateById(task);
                count++;
            } catch (Exception e) {
                log.error("清理过期文件失败，任务ID: {}", task.getId(), e);
            }
        }

        return count;
    }

    /**
     * 校验并批量删除题目版本导出任务信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        // 输入验证
        if (ids == null || ids.isEmpty()) {
            throw new ServiceException("待删除的任务ID列表不能为空");
        }

        // 限制批量删除数量，防止资源耗尽攻击
        if (ids.size() > MAX_BATCH_SIZE) {
            throw new ServiceException("批量删除数量不能超过 " + MAX_BATCH_SIZE + " 个");
        }

        // 验证所有ID的有效性
        for (Long id : ids) {
            if (id == null || id <= 0) {
                throw new ServiceException("任务ID无效");
            }
        }

        if (isValid) {
            // 验证任务是否存在
            List<Long> idList = List.copyOf(ids);
            long count = baseMapper.selectCount(
                Wrappers.lambdaQuery(ChallengeVersionExportTask.class)
                    .in(ChallengeVersionExportTask::getId, idList)
            );
            if (count != idList.size()) {
                throw new ServiceException("部分任务不存在，无法删除");
            }
        }
        return baseMapper.deleteByIds(ids) > 0;
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<ChallengeVersionExportTask> buildQueryWrapper(ChallengeVersionExportTaskBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<ChallengeVersionExportTask> lqw = Wrappers.lambdaQuery();
        lqw.orderByDesc(ChallengeVersionExportTask::getCreateTime);
        lqw.eq(bo.getVersionId() != null, ChallengeVersionExportTask::getVersionId, bo.getVersionId());
        lqw.like(StringUtils.isNotBlank(bo.getVersionTag()), ChallengeVersionExportTask::getVersionTag, bo.getVersionTag());
        lqw.eq(bo.getTaskStatus() != null, ChallengeVersionExportTask::getTaskStatus, bo.getTaskStatus());
        return lqw;
    }

    /**
     * 批量丰富Vo对象（性能优化：去重后批量查询题目名称）
     * 通过版本ID获取draftId，再通过draftId查询题目名称（避免版本号重复问题）
     */
    private void enrichVos(List<ChallengeVersionExportTaskVo> vos) {
        if (vos == null || vos.isEmpty()) {
            return;
        }

        // 收集需要查询题目名称的版本ID（去重）
        List<Long> versionIds = vos.stream()
            .filter(vo -> vo.getVersionId() != null && StringUtils.isBlank(vo.getChallengeName()))
            .map(ChallengeVersionExportTaskVo::getVersionId)
            .distinct()
            .collect(Collectors.toList());

        if (versionIds.isEmpty()) {
            return;
        }

        // 第一步：批量查询版本信息，获取draftId（版本ID -> draftId 映射）
        Map<Long, Long> versionIdToDraftIdMap = new HashMap<>();
        for (Long versionId : versionIds) {
            try {
                ChallengeVersionVo version = challengeVersionService.queryById(versionId);
                if (version != null && version.getDraftId() != null) {
                    versionIdToDraftIdMap.put(versionId, version.getDraftId());
                }
            } catch (Exception e) {
                log.warn("查询版本信息失败，版本ID: {}", versionId, e);
            }
        }

        // 第二步：收集所有draftId（去重）
        List<Long> draftIds = versionIdToDraftIdMap.values().stream()
            .distinct()
            .collect(Collectors.toList());

        // 第三步：批量查询草稿信息，获取题目名称（draftId -> challengeName 映射）
        Map<Long, String> draftIdToChallengeNameMap = new HashMap<>();
        for (Long draftId : draftIds) {
            try {
                ChallengeDraftVo draft = challengeDraftService.queryById(draftId);
                if (draft != null && StringUtils.isNotBlank(draft.getChallengeName())) {
                    draftIdToChallengeNameMap.put(draftId, draft.getChallengeName());
                }
            } catch (Exception e) {
                log.warn("查询草稿信息失败，草稿ID: {}", draftId, e);
            }
        }

        // 第四步：构建版本ID -> 题目名称的映射
        Map<Long, String> challengeNameMap = new HashMap<>();
        for (Map.Entry<Long, Long> entry : versionIdToDraftIdMap.entrySet()) {
            Long versionId = entry.getKey();
            Long draftId = entry.getValue();
            String challengeName = draftIdToChallengeNameMap.get(draftId);
            if (StringUtils.isNotBlank(challengeName)) {
                challengeNameMap.put(versionId, challengeName);
            }
        }

        // 第五步：填充Vo对象
        for (ChallengeVersionExportTaskVo vo : vos) {
            enrichVo(vo, challengeNameMap);
        }
    }

    /**
     * 丰富Vo对象
     */
    private void enrichVo(ChallengeVersionExportTaskVo vo) {
        enrichVo(vo, null);
    }

    /**
     * 丰富Vo对象
     */
    private void enrichVo(ChallengeVersionExportTaskVo vo, Map<Long, String> challengeNameMap) {
        // 任务状态文本
        if (vo.getTaskStatus() != null) {
            switch (ExportTaskStatus.of(vo.getTaskStatus())) {
                case PENDING -> vo.setTaskStatusText("待处理");
                case PROCESSING -> vo.setTaskStatusText("处理中");
                case COMPLETED -> vo.setTaskStatusText("已完成");
                case FAILED -> vo.setTaskStatusText("失败");
            }
        }

        // 文件大小文本
        if (vo.getFileSize() != null) {
            long size = vo.getFileSize();
            if (size < 1024) {
                vo.setFileSizeText(size + " B");
            } else if (size < 1024 * 1024) {
                vo.setFileSizeText(String.format("%.2f KB", size / 1024.0));
            } else if (size < 1024 * 1024 * 1024) {
                vo.setFileSizeText(String.format("%.2f MB", size / (1024.0 * 1024.0)));
            } else {
                vo.setFileSizeText(String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0)));
            }
        }

        // 题目名称（优先从缓存Map获取，否则通过draftId查询）
        if (vo.getVersionId() != null && StringUtils.isBlank(vo.getChallengeName())) {
            if (challengeNameMap != null && challengeNameMap.containsKey(vo.getVersionId())) {
                vo.setChallengeName(challengeNameMap.get(vo.getVersionId()));
            } else {
                try {
                    // 先通过版本ID获取draftId
                    ChallengeVersionVo version = challengeVersionService.queryById(vo.getVersionId());
                    if (version != null && version.getDraftId() != null) {
                        // 再通过draftId查询草稿获取题目名称
                        ChallengeDraftVo draft = challengeDraftService.queryById(version.getDraftId());
                        if (draft != null && StringUtils.isNotBlank(draft.getChallengeName())) {
                            vo.setChallengeName(draft.getChallengeName());
                        }
                    }
                } catch (Exception e) {
                    log.warn("查询题目名称失败，版本ID: {}", vo.getVersionId(), e);
                }
            }
        }
    }
}
