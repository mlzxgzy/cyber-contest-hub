package com.kdajv.cch.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

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
}
