package com.kdajv.cch.domain.bo;

import com.kdajv.cch.domain.ProjectMember;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 项目成员业务对象 t_project_member
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectMemberBo extends BaseEntity {

    /**
     * 主键
     */
    private Long id;

    /**
     * 项目ID
     */
    @NotNull(message = "项目ID不能为空", groups = {AddGroup.class})
    private Long projectId;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空", groups = {AddGroup.class})
    private Long userId;

    /**
     * 权限类型（'admin'管理员, 'view_all'仅查看所有题, 'view_own'仅查看自己导入的题目）
     */
    @NotBlank(message = "权限类型不能为空", groups = {AddGroup.class})
    private String permissionType;

    /**
     * 用户名（用于显示，不存储）
     */
    private String userName;

    /**
     * 用户昵称（用于显示，不存储）
     */
    private String nickName;
}
