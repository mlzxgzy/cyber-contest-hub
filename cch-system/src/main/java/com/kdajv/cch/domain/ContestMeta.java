package com.kdajv.cch.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 竞赛meta信息DTO类
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Data
public class ContestMeta implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 竞赛名称
     */
    private String contestName;

    /**
     * 赛事备注
     */
    private String contestRemark;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 题目需求（多行字符串描述）
     */
    private String challengeRequirement;

    /**
     * 竞赛阶段列表（如初赛、决赛、选拔赛等）
     */
    private List<ContestStage> stages;

    /**
     * 竞赛平台列表（线上比赛平台等）
     */
    private List<ContestPlatform> platforms;
}
