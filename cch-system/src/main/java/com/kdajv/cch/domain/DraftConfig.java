package com.kdajv.cch.domain;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 题目配置对象
 *
 * @author Zyi Guo
 * @date 2025-12-07
 */
@Data
public class DraftConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置描述
     */
    private String stem;

    /**
     * 难度
     */
    private String difficulty;

    // 其他字段你可以自己添加
}
