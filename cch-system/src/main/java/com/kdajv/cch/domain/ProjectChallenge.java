package com.kdajv.cch.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.kdajv.cch.domain.bo.ProjectChallengeBo;
import com.kdajv.cch.domain.vo.ProjectChallengeVo;
import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMappers;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 项目题目关联对象 t_project_challenge
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_project_challenge")
@AutoMappers({
    @AutoMapper(target = ProjectChallengeBo.class),
    @AutoMapper(target = ProjectChallengeVo.class)
})
public class ProjectChallenge extends BaseEntity {

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
     * 题目ID
     */
    private Long challengeId;

    /**
     * 题目版本ID
     */
    private Long versionId;

    /**
     * 删除标志
     */
    @TableLogic
    private Long delFlag;
}
