package com.kdajv.cch.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 容器模拟测试 - 来源选项VO（用于前端下拉选择）
 *
 * @author system
 * @date 2026-01-30
 */
@Data
public class ContainerMockTestSourceVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 草稿ID或版本ID（根据sourceType确定）
     */
    private Long id;

    /**
     * 最终使用的草稿ID
     */
    private Long draftId;

    /**
     * 显示名称
     * 草稿：challengeName (ID: id)
     * 版本：challengeName - versionTag (ID: id)
     */
    private String name;

    /**
     * 来源类型：draft 或 version
     */
    private String sourceType;

    /**
     * 题目名称
     */
    private String challengeName;

    /**
     * 版本标签（仅版本来源有值）
     */
    private String versionTag;

    /**
     * 创建时间（修改时间）
     */
    private java.util.Date createTime;

}
