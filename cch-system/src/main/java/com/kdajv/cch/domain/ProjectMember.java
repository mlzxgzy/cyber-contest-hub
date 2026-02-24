package com.kdajv.cch.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kdajv.cch.domain.bo.ProjectMemberBo;
import com.kdajv.cch.domain.vo.ProjectMemberVo;
import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMappers;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 项目成员关联对象 t_project_member
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_project_member")
@AutoMappers({
    @AutoMapper(target = ProjectMemberVo.class),
    @AutoMapper(target = ProjectMemberBo.class),
})
public class ProjectMember extends BaseEntity {

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
     * 用户ID
     */
    private Long userId;

    /**
     * 权限类型（'admin'管理员, 'view_all'仅查看所有题, 'view_own'仅查看自己导入的题目）
     */
    private String permissionType;

    /**
     * 删除标志
     */
    @TableLogic
    private Long delFlag;
}
