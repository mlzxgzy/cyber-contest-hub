package com.kdajv.cch.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.io.Serial;

/**
 * 题目列表对象 t_challenge
 *
 * @author Zyi Guo
 * @date 2025-12-06
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_challenge")
public class Challenge extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 题目类型
     */
    private String category;

    /**
     * 题目名称
     */
    private String name;

    /**
     * 题目备注
     */
    private String remark;

    /**
     * 题目最新版ID
     */
    private Long latestVersionId;

    /**
     * 删除标志
     */
    @TableLogic
    private Long delFlag;


}
