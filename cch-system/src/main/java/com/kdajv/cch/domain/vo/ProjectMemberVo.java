package com.kdajv.cch.domain.vo;

import com.kdajv.cch.domain.ProjectMember;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 项目成员视图对象 t_project_member
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Data
public class ProjectMemberVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 权限类型（'admin'管理员, 'view_all'仅查看所有题, 'view_own'仅查看自己导入的题目）
     */
    private String permissionType;

    /**
     * 用户名（用于显示）
     */
    private String userName;

    /**
     * 用户昵称（用于显示）
     */
    private String nickName;

    /**
     * 创建时间
     */
    private Date createTime;
}
