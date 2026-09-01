package com.kdajv.cch.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 竞赛平台DTO类（线上比赛平台）
 *
 * @author Zyi Guo
 * @date 2026-08-31
 */
@Data
public class ContestPlatform implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 平台名称
     */
    private String platformName;

    /**
     * 平台地址
     */
    private String platformUrl;
}
