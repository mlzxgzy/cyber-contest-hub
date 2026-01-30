package com.kdajv.cch.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 容器模拟测试视图对象
 *
 * @author system
 * @date 2026-01-30
 */
@Data
public class ChallengeContainerMockTestVo implements Serializable {

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
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> containerIds;

    /**
     * 暴露信息（JSON）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ContainerMockTestContainerVo> containers;

    /**
     * 状态：running/destroying/expired
     */
    private String status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 剩余时间（秒）
     */
    private Long remainingSeconds;

    /**
     * 延长次数
     */
    private Integer extendCount;

}
