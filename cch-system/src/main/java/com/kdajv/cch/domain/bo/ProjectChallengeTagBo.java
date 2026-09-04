package com.kdajv.cch.domain.bo;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.util.List;

/**
 * 项目题目标签业务对象
 *
 * @author Zyi Guo
 * @date 2026-09-04
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProjectChallengeTagBo extends BaseEntity {

    /**
     * 关联记录ID列表（t_project_challenge.id）
     */
    @NotEmpty(message = "题目关联ID不能为空")
    private List<Long> ids;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 是否追加模式：true=在现有标签基础上追加（去重），false=覆盖现有标签
     */
    private Boolean append = Boolean.TRUE;
}
