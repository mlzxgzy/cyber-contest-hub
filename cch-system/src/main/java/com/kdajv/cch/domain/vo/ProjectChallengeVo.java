package com.kdajv.cch.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 项目题目视图对象 t_project_challenge
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Data
public class ProjectChallengeVo implements Serializable {

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
     * 题目ID
     */
    private Long challengeId;

    /**
     * 题目版本ID
     */
    private Long versionId;

    /**
     * 题目名称（用于显示）
     */
    private String challengeName;

    /**
     * 版本号（用于显示）
     */
    private String versionTag;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人
     */
    private Long createBy;
}
