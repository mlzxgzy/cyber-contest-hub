package com.kdajv.cch.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 竞赛阶段DTO类（如初赛、决赛、选拔赛等）
 *
 * @author Zyi Guo
 * @date 2026-08-31
 */
@Data
public class ContestStage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 阶段名称（如初赛、决赛、选拔赛等）
     */
    private String stageName;

    /**
     * 阶段开始时间
     */
    private String startTime;

    /**
     * 阶段时长（分钟）
     */
    private Integer duration;

    /**
     * 本阶段赛题需求（多行字符串描述）
     */
    private String challengeRequirement;
}
