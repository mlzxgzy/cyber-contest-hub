package com.kdajv.cch.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.kdajv.cch.domain.bo.ChallengeContainerMockTestBo;
import com.kdajv.cch.domain.vo.ChallengeContainerMockTestVo;
import io.github.linpeilie.annotations.AutoMapper;
import io.github.linpeilie.annotations.AutoMappers;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;
import java.util.List;

/**
 * 容器模拟测试对象 t_challenge_container_mock_test
 *
 * @author system
 * @date 2026-01-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_challenge_container_mock_test")
public class ChallengeContainerMockTest extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 主键
     */
    @TableId(value = "id")
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
     * 容器ID列表（JSON格式）
     */
    private String containerIds;

    /**
     * 暴露信息（JSON）
     */
    private String exposeInfo;

    /**
     * 状态：starting/running/failed/destroying/expired
     */
    private String status;

    /**
     * 失败原因
     */
    private String errorMsg;

    /**
     * 过期时间
     */
    private java.util.Date expireTime;

    /**
     * 延长次数
     */
    private Integer extendCount;

    /**
     * 删除标志
     */
    @TableLogic
    private Long delFlag;

}
