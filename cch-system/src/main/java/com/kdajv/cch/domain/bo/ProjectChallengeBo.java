package com.kdajv.cch.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 项目题目业务对象 t_project_challenge
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectChallengeBo extends BaseEntity {

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
     * 题目ID
     */
    @NotNull(message = "题目ID不能为空", groups = {AddGroup.class})
    private Long challengeId;

    /**
     * 题目版本ID
     */
    @NotNull(message = "题目版本ID不能为空", groups = {AddGroup.class})
    private Long versionId;

    /**
     * 标签（逗号分隔，可选）
     */
    private String tags;

    /**
     * 题目名称（用于显示，不存储）
     */
    private String challengeName;

    /**
     * 版本号（用于显示，不存储）
     */
    private String versionTag;
}
