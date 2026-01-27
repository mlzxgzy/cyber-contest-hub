package com.kdajv.cch.domain.vo;

import lombok.Data;

/**
 * Docker镜像信息VO
 *
 * @author system
 * @date 2025-01-27
 */
@Data
public class DockerImageVo {
    /**
     * 镜像ID
     */
    private String id;

    /**
     * 镜像标签（仓库:标签格式）
     */
    private String repoTags;

    /**
     * 仓库名称（不含标签）
     */
    private String repository;

    /**
     * 标签
     */
    private String tag;

    /**
     * 镜像ID简写
     */
    private String shortId;

    /**
     * 大小（字节）
     */
    private Long size;

    /**
     * 大小（人类可读格式）
     */
    private String sizeHuman;

    /**
     * 创建时间
     */
    private String created;

    /**
     * 镜像摘要
     */
    private String digest;
}

