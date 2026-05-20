package com.kdajv.cch.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.util.Date;

/**
 * 项目成员邀请对象 t_project_member_invite
 *
 * 用于生成邀请链接后，在有效期内允许其他用户通过邀请加入项目。
 *
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_project_member_invite")
public class ProjectMemberInvite extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 权限类型（'admin'管理员, 'view_all'仅查看所有题, 'view_own'仅查看自己导入的题目）
     */
    private String permissionType;

    /**
     * 邀请Code（用于拼接成前端邀请链接）
     */
    private String inviteCode;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 删除标志
     */
    @TableLogic
    private Long delFlag;
}

