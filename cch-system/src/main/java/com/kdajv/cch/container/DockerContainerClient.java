package com.kdajv.cch.container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.LoadImageCallback;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerPort;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.kdajv.cch.domain.vo.DockerContainerVo;
import com.kdajv.cch.domain.vo.DockerImageVo;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Docker 实现的容器客户端
 */
@Slf4j
public class DockerContainerClient implements ContainerClient {

    private final DockerClient dockerClient;

    public DockerContainerClient(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    @Override
    public boolean ping() throws Exception {
        dockerClient.pingCmd().exec();
        return true;
    }

    @Override
    public List<DockerContainerVo> listContainers() throws Exception {
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();

        return containers.stream().map(container -> {
            DockerContainerVo vo = new DockerContainerVo();
            vo.setId(container.getId());
            vo.setNames(container.getNames() != null ? String.join(",", container.getNames()) : "");
            vo.setImage(container.getImage());
            vo.setImageId(container.getImageId());
            vo.setCommand(container.getCommand());
            vo.setCreated(String.valueOf(container.getCreated()));
            vo.setStatus(container.getState());

            ContainerPort[] ports = container.getPorts();
            if (ports != null) {
                String portsStr = Arrays.stream(ports).map(port -> port.getIp() + ":" + port.getPublicPort() + "->" + port.getPrivatePort() + "/" + port.getType()).collect(Collectors.joining(", "));
                vo.setPorts(portsStr);
            } else {
                vo.setPorts("");
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<DockerImageVo> listImages() throws Exception {
        List<Image> images = dockerClient.listImagesCmd().exec();

        return images.stream().map(image -> {
            DockerImageVo vo = new DockerImageVo();
            vo.setId(image.getId());

            if (image.getRepoTags() != null && image.getRepoTags().length > 0) {
                String fullTag = image.getRepoTags()[0];
                vo.setRepoTags(fullTag);

                int lastColonIndex = fullTag.lastIndexOf(':');
                if (lastColonIndex > 0) {
                    vo.setRepository(fullTag.substring(0, lastColonIndex));
                    vo.setTag(fullTag.substring(lastColonIndex + 1));
                } else {
                    vo.setRepository(fullTag);
                    vo.setTag("latest");
                }
            } else {
                vo.setRepoTags("<none>:<none>");
                vo.setRepository("<none>");
                vo.setTag("<none>");
            }

            if (image.getId() != null && image.getId().length() >= 12) {
                vo.setShortId(image.getId().substring(0, 12));
            } else {
                vo.setShortId(image.getId());
            }

            if (image.getSize() != null) {
                vo.setSize(image.getSize());
                vo.setSizeHuman(humanReadableByteCount(image.getSize()));
            }

            if (image.getCreated() != null) {
                vo.setCreated(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(image.getCreated() * 1000L)));
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public String loadImage(InputStream imageStream) throws Exception {
        try {
            LoadImageCallback callback = dockerClient.loadImageAsyncCmd(imageStream).exec(new LoadImageCallback());
            log.info("开始加载镜像到Docker...");
            return callback.awaitMessage();
        } catch (Exception e) {
            throw new ServiceException("加载镜像到 Docker 失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void tagImage(String sourceImage, String targetImage, String tag) throws Exception {
        try {
            dockerClient.tagImageCmd(sourceImage, targetImage, tag).exec();
        } catch (Exception e) {
            throw new ServiceException("为镜像打标签失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void removeImage(String image) throws Exception {
        try {
            dockerClient.removeImageCmd(image).exec();
        } catch (Exception e) {
            throw new ServiceException("删除镜像失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            dockerClient.close();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            log.warn("关闭 DockerClient 时发生非 IO 异常", e);
        }
    }

    private String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}


