package com.kdajv.cch.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.kdajv.cch.domain.bo.ChallengeVersionExportTaskBo;
import com.kdajv.cch.domain.vo.ChallengeVersionExportTaskVo;
import com.kdajv.cch.service.IChallengeVersionExportTaskService;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 题目版本导出任务
 *
 * @author system
 * @date 2026-01-30
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/cch/challengeVersion/export")
public class ChallengeVersionExportTaskController extends BaseController {

    private final IChallengeVersionExportTaskService exportTaskService;

    /**
     * 创建导出任务（单个）
     *
     * @param versionId     版本ID
     * @param includeImages 是否导出容器镜像文件（true-导出镜像tar包，false-仅保留镜像地址），默认false
     */
    @SaCheckPermission("cch:challengeVersion:export")
    @Log(title = "题目版本导出任务", businessType = BusinessType.INSERT)
    @PostMapping("/create")
    public R<Long> createTask(
        @NotNull(message = "版本ID不能为空") @RequestParam Long versionId,
        @RequestParam(defaultValue = "false") boolean includeImages
    ) {
        Long taskId = exportTaskService.createExportTask(versionId, includeImages);
        return R.ok(taskId);
    }

    /**
     * 批量创建导出任务
     *
     * @param versionIds    版本ID列表（请求体）
     * @param includeImages 是否导出容器镜像文件（true-导出镜像tar包，false-仅保留镜像地址），默认false
     */
    @SaCheckPermission("cch:challengeVersion:export")
    @Log(title = "题目版本导出任务", businessType = BusinessType.INSERT)
    @PostMapping("/createBatch")
    public R<List<Long>> createTasks(
        @NotEmpty(message = "版本ID列表不能为空") @RequestBody List<Long> versionIds,
        @RequestParam(defaultValue = "false") boolean includeImages
    ) {
        List<Long> taskIds = exportTaskService.createExportTasks(versionIds, includeImages);
        return R.ok(taskIds);
    }

    /**
     * 查询导出任务列表
     */
    @SaCheckPermission("cch:challengeVersion:export")
    @GetMapping("/task/list")
    public TableDataInfo<ChallengeVersionExportTaskVo> list(ChallengeVersionExportTaskBo bo, PageQuery pageQuery) {
        return exportTaskService.queryPageList(bo, pageQuery);
    }

    /**
     * 获取下载链接
     */
    @SaCheckPermission("cch:challengeVersion:export")
    @GetMapping("/task/{id}/download")
    public R<String> getDownloadUrl(@NotNull(message = "任务ID不能为空") @PathVariable Long id) {
        String downloadUrl = exportTaskService.getDownloadUrl(id);
        return R.ok(downloadUrl);
    }

    /**
     * 删除导出任务
     */
    @SaCheckPermission("cch:challengeVersion:export")
    @Log(title = "题目版本导出任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/task/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(exportTaskService.deleteWithValidByIds(List.of(ids), true));
    }
}
