package com.kdajv.cch.container;

import com.kdajv.cch.domain.vo.DockerContainerVo;
import com.kdajv.cch.domain.vo.DockerImageVo;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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
     * 关闭客户端并释放资源
     */
    @Override
    void close() throws IOException;
}


