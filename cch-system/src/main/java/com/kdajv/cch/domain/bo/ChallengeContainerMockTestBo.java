package com.kdajv.cch.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 容器模拟测试业务对象
 *
 * @author system
 * @date 2026-01-30
 */
@Data
public class ChallengeContainerMockTestBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 草稿ID
     */
    private Long draftId;

    /**
     * 来源类型：draft/version
     */
    private String sourceType;

    /**
     * 来源ID（草稿ID或版本ID）
     */
    private Long sourceId;

    /**
     * 题目名称
     */
    private String challengeName;

    /**
     * 容器ID列表
     */
    private String containerIds;

    /**
     * 暴露信息（JSON）
     */
    private String exposeInfo;

    /**
     * 状态：running/destroying/expired
     */
    private String status;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 延长次数
     */
    private Integer extendCount;

}
