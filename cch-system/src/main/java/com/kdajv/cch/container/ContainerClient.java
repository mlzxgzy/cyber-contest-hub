package com.kdajv.cch.container;

import com.kdajv.cch.domain.DraftConfig;
import com.kdajv.cch.domain.vo.ClusterNodeVo;
import com.kdajv.cch.domain.vo.DockerContainerVo;
import com.kdajv.cch.domain.vo.DockerImageVo;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 容器后端通用客户端接口
 * <p>
 * 该接口抽象了容器后端（如 Docker、Kubernetes）需要提供的基础能力，
 * 便于后续通过不同实现类适配不同容器平台。
 */
public interface ContainerClient extends Closeable {

    /**
     * Ping 当前后端是否可用
     *
     * @return 是否可用
     * @throws Exception 后端不可用或调用失败时抛出
     */
    boolean ping() throws Exception;

    /**
     * 获取容器列表
     *
     * @return 容器列表
     * @throws Exception 查询失败时抛出
     */
    List<DockerContainerVo> listContainers() throws Exception;

    /**
     * 获取镜像列表
     *
     * @return 镜像列表
     * @throws Exception 查询失败时抛出
     */
    List<DockerImageVo> listImages() throws Exception;

    /**
     * 从镜像流中载入镜像
     *
     * @param imageStream 镜像输入流
     * @return 后端返回的原始结果字符串（例如 Docker 的 \"Loaded image: xxx\"）
     * @throws Exception 载入失败时抛出
     */
    String loadImage(InputStream imageStream) throws Exception;

    /**
     * 为镜像打标签
     *
     * @param sourceImage 源镜像名称（含标签）
     * @param targetImage 目标镜像名称（不含标签）
     * @param tag         目标标签
     * @throws Exception 打标签失败时抛出
     */
    void tagImage(String sourceImage, String targetImage, String tag) throws Exception;

    /**
     * 删除镜像
     *
     * @param image 镜像名称（可含标签）
     * @throws Exception 删除失败时抛出
     */
    void removeImage(String image) throws Exception;

    /**
     * 推送镜像到Registry
     *
     * @param imageName 镜像名称（不含标签）
     * @param tag       镜像标签
     * @throws Exception 推送失败时抛出
     */
    void pushImage(String imageName, String tag) throws Exception;

    /**
     * 获取集群节点列表
     *
     * @return 节点列表
     * @throws Exception 查询失败时抛出
     */
    List<ClusterNodeVo> listNodes() throws Exception;

    /**
     * 更新节点外部访问地址
     *
     * @param nodeId  节点ID
     * @param address 外部访问地址（IP或域名）
     * @throws Exception 更新失败时抛出
     */
    void updateNodeExternalAddress(String nodeId, String address) throws Exception;

    /**
     * 更新节点标签（内部使用）
     *
     * @param nodeId 节点ID
     * @param labels 标签Map
     * @throws Exception 更新失败时抛出
     */
    void updateNodeLabels(String nodeId, Map<String, String> labels) throws Exception;

    // ==================== Docker Swarm Service 操作相关方法（用于模拟测试） ====================

    /**
     * 服务端口信息（Swarm模式下使用）
     */
    record ServicePortInfo(
        String serviceId,
        String serviceName,
        String imageName,
        String status,
        String host,
        List<PortMapping> portMappings
    ) {
    }

    /**
     * 端口映射信息
     */
    record PortMapping(
        String name,
        String protocol,
        Integer internalPort,
        Integer externalPort
    ) {
    }

    /**
     * 创建并启动 Docker Swarm Service
     *
     * @param imageName   镜像名称（含标签）
     * @param env         环境变量
     * @param ports       端口配置
     * @param cpuLimit    CPU限制（millicores）
     * @param memoryLimit 内存限制（MB）
     * @param serviceName 服务名称（可选）
     * @return 服务端口信息
     * @throws Exception 创建失败时抛出
     */
    ServicePortInfo createAndStartService(
        String imageName,
        Map<String, String> env,
        Map<String, DraftConfig.PortConfig> ports,
        Integer cpuLimit,
        Integer memoryLimit,
        String serviceName
    ) throws Exception;

    /**
     * 停止并删除服务
     *
     * @param serviceId 服务ID
     * @throws Exception 停止失败时抛出
     */
    void removeService(String serviceId) throws Exception;

    /**
     * 检查服务是否运行中
     *
     * @param serviceId 服务ID
     * @return 是否运行中
     * @throwsException 查询失败时抛出
     */
    boolean isServiceRunning(String serviceId) throws Exception;

    /**
     * 获取服务端口信息
     *
     * @param serviceId 服务ID
     * @return 服务端口信息
     * @throws Exception 查询失败时抛出
     */
    ServicePortInfo getServicePortInfo(String serviceId) throws Exception;

    /**
     * 关闭客户端并释放资源
     */
    @Override
    void close() throws IOException;
}


