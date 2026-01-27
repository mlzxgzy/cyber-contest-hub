package com.kdajv.cch.domain.vo;

import lombok.Data;

/**
 * Docker容器信息VO
 *
 * @author system
 * @date 2025-01-27
 */
@Data
public class DockerContainerVo {
    /**
     * 容器ID
     */
    private String id;

    /**
     * 容器名称
     */
    private String names;

    /**
     * 镜像名称
     */
    private String image;

    /**
     * 镜像ID
     */
    private String imageId;

    /**
     * 命令
     */
    private String command;

    /**
     * 创建时间
     */
    private String created;

    /**
     * 状态
     */
    private String status;

    /**
     * 端口映射信息
     */
    private String ports;

    /**
     * 使用的内存（字节）
     */
    private Long memoryUsage;

    /**
     * CPU使用率
     */
    private Double cpuUsage;
}

