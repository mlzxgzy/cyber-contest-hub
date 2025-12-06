package com.kdajv.cch.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 题目版本对象 t_challenge_version
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_challenge_version")
public class ChallengeVersion extends BaseEntity {

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
     * 草稿ID
     */
    private Long draftId;

    /**
     * 版本号
     */
    private String versionTag;

    /**
     * 版本描述
     */
    private String versionDescription;

    /**
     * 删除标志
     */
    @TableLogic
    private Long delFlag;


}
