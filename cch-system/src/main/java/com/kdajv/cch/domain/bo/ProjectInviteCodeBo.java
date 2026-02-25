package com.kdajv.cch.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 项目成员邀请Code请求对象
 */
@Data
public class ProjectInviteCodeBo {

    /**
     * 权限类型（'admin'管理员, 'view_all'仅查看所有题, 'view_own'仅查看自己导入的题目）
     */
    @NotBlank(message = "权限类型不能为空")
    private String permissionType;
}

