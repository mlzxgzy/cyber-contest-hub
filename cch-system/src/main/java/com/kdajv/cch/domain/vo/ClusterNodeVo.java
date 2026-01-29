package com.kdajv.cch.domain.vo;

import lombok.Data;

import java.util.Map;

/**
 * 集群节点信息VO（Docker Swarm 或 Kubernetes）
 *
 * @author system
 * @date 2025-01-27
 */
@Data
public class ClusterNodeVo {
    /**
     * 节点ID
     */
    private String id;

    /**
     * 节点名称
     */
    private String name;

    /**
     * 节点角色（manager/worker 或 master/node）
     */
    private String role;

    /**
     * 节点状态
     */
    private String status;

    /**
     * 节点地址
     */
    private String address;

    /**
     * 节点标签（key-value格式）
     */
    private Map<String, String> labels;

    /**
     * 外部访问地址（从labels中提取）
     */
    private String externalAccessAddress;

    /**
     * 架构
     */
    private String architecture;

    /**
     * 操作系统
     */
    private String operatingSystem;

    /**
     * CPU数量
     */
    private Integer cpuCount;

    /**
     * 内存总量（字节）
     */
    private Long memoryTotal;
}
