package com.kdajv.cch.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 容器模拟测试 - 容器暴露信息VO
 *
 * @author system
 * @date 2026-01-30
 */
@Data
public class ContainerMockTestContainerVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 靶机名称
     */
    private String name;

    /**
     * 宿主机地址
     */
    private String host;

    /**
     * 协议：tcp/udp
     */
    private String protocol;

    /**
     * 内部端口
     */
    private Integer internalPort;

    /**
     * 外部映射端口
     */
    private Integer externalPort;

}
