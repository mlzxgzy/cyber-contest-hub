package com.kdajv.cch.service;

import com.kdajv.cch.domain.bo.ChallengeVersionExportTaskBo;
import com.kdajv.cch.domain.vo.ChallengeVersionExportTaskVo;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;

import java.util.Collection;
import java.util.List;

/**
 * 题目版本导出任务Service接口
 *
 * @author system
 * @date 2026-01-30
 */
public interface IChallengeVersionExportTaskService {

    /**
     * 查询题目版本导出任务
     *
     * @param id 主键
     * @return 题目版本导出任务
     */
    ChallengeVersionExportTaskVo queryById(Long id);

    /**
     * 分页查询题目版本导出任务列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 题目版本导出任务分页列表
     */
    TableDataInfo<ChallengeVersionExportTaskVo> queryPageList(ChallengeVersionExportTaskBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的题目版本导出任务列表
     *
     * @param bo 查询条件
     * @return 题目版本导出任务列表
     */
    List<ChallengeVersionExportTaskVo> queryList(ChallengeVersionExportTaskBo bo);

    /**
     * 创建导出任务
     *
     * @param versionId     题目版本ID
     * @param includeImages 是否导出容器镜像文件（true-导出镜像tar包，false-仅保留镜像地址）
     * @return 任务ID
     */
    Long createExportTask(Long versionId, boolean includeImages);

    /**
     * 批量创建导出任务
     *
     * @param versionIds    题目版本ID列表
     * @param includeImages 是否导出容器镜像文件（true-导出镜像tar包，false-仅保留镜像地址）
     * @return 任务ID列表
     */
    List<Long> createExportTasks(List<Long> versionIds, boolean includeImages);

    /**
     * 执行导出任务（异步调用）
     *
     * @param taskId 任务ID
     */
    void executeExportTask(Long taskId);

    /**
     * 获取下载链接
     *
     * @param taskId 任务ID
     * @return 临时下载链接
     */
    String getDownloadUrl(Long taskId);

    /**
     * 清理过期文件（定时任务调用）
     *
     * @return 清理的文件数量
     */
    int cleanupExpiredFiles();

    /**
     * 校验并批量删除题目版本导出任务信息
     *
     * @param ids     待删除的主键集合
     * @param isValid 是否进行有效性校验
     * @return 是否删除成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);
}
