package com.kdajv.cch.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.kdajv.cch.domain.bo.ProjectBo;
import com.kdajv.cch.domain.bo.ProjectChallengeBo;
import com.kdajv.cch.domain.bo.ProjectInviteCodeBo;
import com.kdajv.cch.domain.bo.ProjectJoinByInviteBo;
import com.kdajv.cch.domain.bo.ProjectMemberBo;
import com.kdajv.cch.domain.vo.ContestFileVo;
import com.kdajv.cch.domain.vo.ProjectChallengeVo;
import com.kdajv.cch.domain.vo.ProjectMemberVo;
import com.kdajv.cch.domain.vo.ProjectVo;
import com.kdajv.cch.service.IProjectService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.idempotent.annotation.RepeatSubmit;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 项目Controller
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/cch/project")
public class ProjectController extends BaseController {

    private final IProjectService projectService;

    /**
     * 查询项目列表
     */
    @SaCheckPermission("cch:project:list")
    @GetMapping("/list")
    public TableDataInfo<ProjectVo> list(ProjectBo bo, PageQuery pageQuery) {
        return projectService.queryPageList(bo, pageQuery);
    }

    /**
     * 获取项目详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("cch:project:query")
    @GetMapping("/{id}")
    public R<ProjectVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(projectService.queryById(id));
    }

    /**
     * 新增项目
     */
    @SaCheckPermission("cch:project:add")
    @Log(title = "项目", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    public R<Void> add(@Validated(AddGroup.class) @RequestBody ProjectBo bo) {
        return toAjax(projectService.insertByBo(bo));
    }

    /**
     * 修改项目
     */
    @SaCheckPermission("cch:project:edit")
    @Log(title = "项目", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody ProjectBo bo) {
        return toAjax(projectService.updateByBo(bo));
    }

    /**
     * 删除项目
     *
     * @param ids 主键串
     */
    @SaCheckPermission("cch:project:remove")
    @Log(title = "项目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "主键不能为空") @PathVariable Long[] ids) {
        return toAjax(projectService.deleteWithValidByIds(List.of(ids), true));
    }

    /**
     * 查询项目成员列表
     *
     * @param projectId 项目ID
     */
    @SaCheckPermission("cch:project:query")
    @GetMapping("/{projectId}/members")
    public R<List<ProjectMemberVo>> getMembers(@NotNull(message = "项目ID不能为空") @PathVariable Long projectId) {
        ProjectVo project = projectService.queryById(projectId);
        return R.ok(project != null ? project.getMembers() : List.of());
    }

    /**
     * 添加项目成员
     *
     * @param projectId 项目ID
     * @param members   成员列表
     */
    @SaCheckPermission("cch:project:edit")
    @Log(title = "项目成员", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/{projectId}/members")
    public R<Void> addMembers(
        @NotNull(message = "项目ID不能为空") @PathVariable Long projectId,
        @Validated @RequestBody List<ProjectMemberBo> members
    ) {
        return toAjax(projectService.addMembers(projectId, members));
    }

    /**
     * 移除项目成员
     *
     * @param projectId 项目ID
     * @param userIds   用户ID列表
     */
    @SaCheckPermission("cch:project:edit")
    @Log(title = "项目成员", businessType = BusinessType.DELETE)
    @DeleteMapping("/{projectId}/members")
    public R<Void> removeMembers(
        @NotNull(message = "项目ID不能为空") @PathVariable Long projectId,
        @RequestBody List<Long> userIds
    ) {
        return toAjax(projectService.removeMembers(projectId, userIds));
    }

    /**
     * 生成项目成员邀请Code
     *
     * @param projectId 项目ID
     * @param bo        邀请Code请求
     */
    @SaCheckPermission("cch:project:edit")
    @Log(title = "项目成员邀请", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/{projectId}/members/invite-code")
    public R<String> generateInviteCode(
        @NotNull(message = "项目ID不能为空") @PathVariable Long projectId,
        @Validated @RequestBody ProjectInviteCodeBo bo
    ) {
        return R.ok(projectService.generateInviteCode(projectId, bo.getPermissionType()));
    }

    /**
     * 通过邀请Code加入项目（仅需登录）
     *
     * @param projectId 项目ID
     * @param bo        加入请求
     */
    @PostMapping("/{projectId}/members/join-by-invite")
    public R<Void> joinByInvite(
        @NotNull(message = "项目ID不能为空") @PathVariable Long projectId,
        @Validated @RequestBody ProjectJoinByInviteBo bo
    ) {
        return toAjax(projectService.joinByInvite(projectId, bo.getInviteCode()));
    }

    /**
     * 查询项目题目列表
     *
     * @param projectId 项目ID
     */
    @SaCheckPermission("cch:project:query")
    @GetMapping("/{projectId}/challenges")
    public R<List<ProjectChallengeVo>> getChallenges(@NotNull(message = "项目ID不能为空") @PathVariable Long projectId) {
        return R.ok(projectService.queryProjectChallenges(projectId));
    }

    /**
     * 导入项目题目
     *
     * @param projectId  项目ID
     * @param challenges 题目列表
     */
    @SaCheckPermission("cch:project:edit")
    @Log(title = "项目题目", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/{projectId}/challenges")
    public R<Void> importChallenges(
        @NotNull(message = "项目ID不能为空") @PathVariable Long projectId,
        @Validated @RequestBody List<ProjectChallengeBo> challenges
    ) {
        return toAjax(projectService.importChallenges(projectId, challenges));
    }

    /**
     * 移除项目题目
     *
     * @param projectId   项目ID
     * @param challengeIds 题目关联ID列表
     */
    @SaCheckPermission("cch:project:edit")
    @Log(title = "项目题目", businessType = BusinessType.DELETE)
    @DeleteMapping("/{projectId}/challenges")
    public R<Void> removeChallenges(
        @NotNull(message = "项目ID不能为空") @PathVariable Long projectId,
        @RequestBody List<Long> challengeIds
    ) {
        return toAjax(projectService.removeChallenges(projectId, challengeIds));
    }

    /**
     * 上传竞赛文件
     *
     * @param projectId 项目ID
     * @param file      文件
     * @param fileTag   文件标签
     */
    @SaCheckPermission("cch:project:edit")
    @Log(title = "竞赛文件", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping("/{projectId}/files/upload")
    public R<ContestFileVo> uploadContestFile(
        @NotNull(message = "项目ID不能为空") @PathVariable Long projectId,
        @NotNull(message = "文件不能为空") @RequestParam("file") MultipartFile file,
        @RequestParam(value = "fileTag", required = false) String fileTag
    ) {
        return R.ok(projectService.uploadContestFile(projectId, file, fileTag));
    }

    /**
     * 下载竞赛文件
     *
     * @param fileId   文件ID
     * @param response HTTP响应
     */
    @SaCheckPermission("cch:project:query")
    @GetMapping("/files/{fileId}/download")
    public void downloadContestFile(
        @NotNull(message = "文件ID不能为空") @PathVariable Long fileId,
        HttpServletResponse response
    ) throws Exception {
        projectService.downloadContestFile(fileId, response);
    }

    /**
     * 删除竞赛文件
     *
     * @param fileIds 文件ID列表
     */
    @SaCheckPermission("cch:project:edit")
    @Log(title = "竞赛文件", businessType = BusinessType.DELETE)
    @DeleteMapping("/files/{fileIds}")
    public R<Void> removeContestFile(@NotEmpty(message = "文件ID不能为空") @PathVariable Long[] fileIds) {
        return toAjax(projectService.removeContestFile(List.of(fileIds)));
    }
}
