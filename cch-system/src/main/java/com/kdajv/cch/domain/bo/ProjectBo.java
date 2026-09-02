package com.kdajv.cch.domain.bo;

import com.kdajv.cch.domain.ContestMeta;
import com.kdajv.cch.domain.Project;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.util.List;

/**
 * 项目业务对象 t_project
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectBo extends BaseEntity {

    /**
     * 主键
     */
    @NotNull(message = "主键不能为空", groups = {EditGroup.class})
    private Long id;

    /**
     * 项目类型（'normal'普通项目, 'contest'竞赛项目）
     */
    @NotBlank(message = "项目类型不能为空", groups = {AddGroup.class, EditGroup.class})
    private String projectType;

    /**
     * 项目名称
     */
    @NotBlank(message = "项目名称不能为空", groups = {AddGroup.class, EditGroup.class})
    @Size(max = 128, message = "项目名称长度不能超过128字符", groups = {AddGroup.class, EditGroup.class})
    private String name;

    /**
     * 备注
     */
    private String remark;

    /**
     * 项目负责人（手填人名）
     */
    @Size(max = 64, message = "项目负责人长度不能超过64字符", groups = {AddGroup.class, EditGroup.class})
    private String leader;

    /**
     * 竞赛meta信息（仅竞赛项目使用）
     */
    private ContestMeta meta;

    /**
     * 成员列表（用于新增/更新时批量添加成员）
     */
    private List<ProjectMemberBo> members;

    /**
     * 题目列表（用于新增/更新时批量导入题目）
     */
    private List<ProjectChallengeBo> challenges;
}
