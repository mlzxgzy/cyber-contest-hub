package com.kdajv.cch.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 题目草稿对象 t_challenge_draft
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "t_challenge_draft", autoResultMap = true)
public class ChallengeDraft extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 题目ID
     */
    private Long challengeId;

    /**
     * 题目名称
     */
    private String challengeName;

    /**
     * 草稿描述
     */
    private String challengeDescription;

    /**
     * 配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private DraftConfig config;

    /**
     * 删除标志
     */
    @TableLogic
    private Long delFlag;


}
