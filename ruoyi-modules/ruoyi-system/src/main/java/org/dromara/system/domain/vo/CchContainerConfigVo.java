package org.dromara.system.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 容器配置视图对象（存储在sys_config表中）
 *
 * @author system
 * @date 2025-12-11
 */
@Data
public class CchContainerConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置ID（对应sys_config.config_id）
     */
    private Long id;

    /**
     * 配置名称
     */
    private String configName;

    /**
     * 后端类型（docker/kubernetes）
     */
    private String backendType;

    /**
     * Docker URL
     */
    private String dockerUrl;

    /**
     * Docker API版本
     */
    private String dockerApiVersion;

    /**
     * Docker证书路径
     */
    private String dockerCertPath;

    /**
     * Docker TLS验证（0否 1是）
     */
    private String dockerTlsVerify;

    /**
     * Kubernetes配置（JSON格式）
     */
    private String kubernetesConfig;

    /**
     * Kubernetes命名空间
     */
    private String kubernetesNamespace;

    /**
     * 状态（0正常 1停用）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

}
