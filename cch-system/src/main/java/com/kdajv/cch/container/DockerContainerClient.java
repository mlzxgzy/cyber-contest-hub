package com.kdajv.cch.container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateServiceResponse;
import com.github.dockerjava.api.command.LoadImageCallback;
import com.github.dockerjava.api.model.*;
import com.kdajv.cch.domain.DraftConfig;
import com.kdajv.cch.domain.vo.ClusterNodeVo;
import com.kdajv.cch.domain.vo.DockerContainerVo;
import com.kdajv.cch.domain.vo.DockerImageVo;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Docker 实现的容器客户端（支持 Docker Swarm Service）
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
                String portsStr = Arrays.stream(ports).map(port ->
                    port.getIp() + ":" + port.getPublicPort() + "->" + port.getPrivatePort() + "/" + port.getType()
                ).collect(Collectors.joining(", "));
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
    public void pushImage(String imageName, String tag) throws Exception {
        try {
            String fullImageName = imageName + ":" + tag;
            log.info("开始推送镜像到Registry: {}", fullImageName);

            dockerClient.pushImageCmd(fullImageName).start().awaitCompletion();

            log.info("镜像推送成功: {}", fullImageName);
        } catch (Exception e) {
            throw new ServiceException("推送镜像失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ClusterNodeVo> listNodes() throws Exception {
        try {
            List<SwarmNode> swarmNodes = dockerClient.listSwarmNodesCmd().exec();
            return swarmNodes.stream().map(node -> {
                ClusterNodeVo vo = new ClusterNodeVo();
                vo.setId(node.getId());
                vo.setName(node.getDescription().getHostname());

                // 设置角色
                if (node.getSpec() != null && node.getSpec().getRole() != null) {
                    vo.setRole(node.getSpec().getRole().toString().toLowerCase());
                }

                // 设置状态
                if (node.getStatus() != null && node.getStatus().getState() != null) {
                    vo.setStatus(node.getStatus().getState().toString());
                }

                // 设置地址
                if (node.getStatus() != null && node.getStatus().getAddress() != null) {
                    vo.setAddress(node.getStatus().getAddress());
                }

                // 设置标签
                if (node.getSpec() != null && node.getSpec().getLabels() != null) {
                    vo.setLabels(node.getSpec().getLabels());
                    // 提取外部访问地址
                    String externalAddress = node.getSpec().getLabels().get("cch.external.access.address");
                    if (externalAddress == null) {
                        externalAddress = node.getSpec().getLabels().get("external.access.address");
                    }
                    vo.setExternalAccessAddress(externalAddress);
                } else {
                    vo.setLabels(new HashMap<>());
                }

                // 设置架构和操作系统
                if (node.getDescription() != null) {
                    if (node.getDescription().getPlatform() != null) {
                        vo.setArchitecture(node.getDescription().getPlatform().getArchitecture());
                        vo.setOperatingSystem(node.getDescription().getPlatform().getOs());
                    }
                    if (node.getDescription().getResources() != null) {
                        vo.setCpuCount(node.getDescription().getResources().getNanoCPUs() != null ?
                            (int) (node.getDescription().getResources().getNanoCPUs() / 1_000_000_000L) : null);
                        vo.setMemoryTotal(node.getDescription().getResources().getMemoryBytes());
                    }
                }

                return vo;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            // 如果不是Swarm模式，返回空列表
            log.debug("获取Swarm节点列表失败，可能不是Swarm模式: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public void updateNodeExternalAddress(String nodeId, String address) throws Exception {
        // 构造外部访问地址标签
        Map<String, String> labels = Map.of("cch.external.access.address", address != null ? address : "");
        updateNodeLabels(nodeId, labels);
    }

    @Override
    public void updateNodeLabels(String nodeId, Map<String, String> labels) throws Exception {
        try {
            // 获取当前节点信息
            List<SwarmNode> nodes = dockerClient.listSwarmNodesCmd().exec();
            SwarmNode targetNode = nodes.stream()
                .filter(node -> nodeId.equals(node.getId())).findFirst()
                .orElseThrow(() -> new ServiceException("节点不存在: " + nodeId));

            // 获取当前节点的Spec
            SwarmNodeSpec currentSpec = targetNode.getSpec();
            if (currentSpec == null) {
                throw new ServiceException("节点配置不存在");
            }

            // 合并标签，新标签覆盖原有标签
            Map<String, String> mergedLabels = new HashMap<>(currentSpec.getLabels());
            if (labels != null) {
                mergedLabels.putAll(labels);
            }
            currentSpec.withLabels(mergedLabels);

            // 更新节点
            dockerClient.updateSwarmNodeCmd()
                .withSwarmNodeId(nodeId)
                .withVersion(targetNode.getVersion().getIndex())
                .withSwarmNodeSpec(currentSpec).exec();
        } catch (Exception e) {
            throw new ServiceException("更新节点标签失败: " + e.getMessage(), e);
        }
    }

    // ==================== Docker Swarm Service 操作方法（用于模拟测试） ====================

    @Override
    public ServicePortInfo createAndStartService(
        String imageName,
        Map<String, String> env,
        Map<String, DraftConfig.PortConfig> ports,
        Integer cpuLimit,
        Integer memoryLimit,
        String serviceName
    ) throws Exception {
        try {
            // 构建服务名称（添加mock-前缀以标识）
            String fullServiceName = "mock-" + serviceName;

            // 构建服务规格
            ServiceSpec serviceSpec = new ServiceSpec();
            serviceSpec.withName(fullServiceName);

            // 构建容器规格
            ContainerSpec containerSpec = new ContainerSpec()
                .withImage(imageName)
                .withTty(true);

            // 设置环境变量
            if (env != null && !env.isEmpty()) {
                List<String> envList = new ArrayList<>();
                for (Map.Entry<String, String> entry : env.entrySet()) {
                    envList.add(entry.getKey() + "=" + entry.getValue());
                }
                containerSpec.withEnv(envList);
            }

            // 构建任务模板
            TaskSpec taskSpec = new TaskSpec();
            taskSpec.withContainerSpec(containerSpec);
            serviceSpec.withTaskTemplate(taskSpec);

            // 设置端口暴露
            if (ports != null && !ports.isEmpty()) {
                EndpointSpec endpointSpec = new EndpointSpec();
                List<PortConfig> portConfigs = new ArrayList<>();

                for (Map.Entry<String, DraftConfig.PortConfig> entry : ports.entrySet()) {
                    DraftConfig.PortConfig portConfig = entry.getValue();
                    if (portConfig.getInternalPort() != null) {
                        // 不设置 publishedPort，让 Docker 自动分配随机端口
                        PortConfig dockerPortConfig = new PortConfig()
                            .withName(entry.getKey())
                            .withTargetPort(portConfig.getInternalPort());

                        portConfigs.add(dockerPortConfig);
                    }
                }

                endpointSpec.withPorts(portConfigs);
                serviceSpec.withEndpointSpec(endpointSpec);
            }

            // 创建服务
            CreateServiceResponse serviceResponse = dockerClient.createServiceCmd(serviceSpec).exec();
            String serviceId = serviceResponse.getId();
            log.info("Docker Swarm Service 创建成功: {} ({})", fullServiceName, serviceId);

            // 等待服务启动
            // Thread.sleep(2000);

            // 获取服务端口信息
            return getServicePortInfo(serviceId);

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建并启动 Docker Swarm Service 失败", e);
            throw new ServiceException("创建并启动 Docker Swarm Service 失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void removeService(String serviceId) throws Exception {
        try {
            // 先检查服务是否存在
            try {
                dockerClient.inspectServiceCmd(serviceId).exec();
            } catch (Exception e) {
                log.debug("服务不存在或已删除: {}", serviceId);
                return;
            }

            // 删除服务
            dockerClient.removeServiceCmd(serviceId).exec();
            log.info("Docker Swarm Service 已删除: {}", serviceId);
        } catch (Exception e) {
            log.error("删除服务失败: {}", serviceId, e);
            throw new ServiceException("删除 Docker Swarm Service 失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isServiceRunning(String serviceId) throws Exception {
        try {
            // 尝试检查服务，如果失败则说明服务不存在
            dockerClient.inspectServiceCmd(serviceId).exec();
            return true;
        } catch (Exception e) {
            log.debug("检查服务状态失败或服务不存在: {}", serviceId, e);
            return false;
        }
    }

    @Override
    public ServicePortInfo getServicePortInfo(String serviceId) throws Exception {
        try {
            // 获取宿主机地址
            String host = "localhost";
            try {
                List<ClusterNodeVo> nodes = listNodes();
                for (ClusterNodeVo node : nodes) {
                    if (StringUtils.isNotBlank(node.getExternalAccessAddress())) {
                        host = node.getExternalAccessAddress();
                        break;
                    }
                }
            } catch (Exception e) {
                log.warn("获取集群节点失败，使用默认地址: {}", e.getMessage());
            }

            // 获取端口映射
            List<PortMapping> portMappings = new ArrayList<>();

            // 从服务定义获取端口信息
            try {
                com.github.dockerjava.api.model.Service service = dockerClient.inspectServiceCmd(serviceId).exec();
                ServiceSpec spec = service.getSpec();
                
                // 获取服务名称
                String serviceName = spec != null && spec.getName() != null ? spec.getName() : serviceId;
                if (serviceName.length() > 12) {
                    serviceName = serviceName.substring(0, 12);
                }
                
                // 获取镜像名称
                String imageName = "";
                if (spec != null && spec.getTaskTemplate() != null &&
                    spec.getTaskTemplate().getContainerSpec() != null) {
                    imageName = spec.getTaskTemplate().getContainerSpec().getImage();
                }

                // 优先从 service.getEndpoint().getPorts() 获取实际分配的端口
                // 这是运行时实际分配的真实端口映射
                if (service.getEndpoint() != null && service.getEndpoint().getPorts() != null) {
                    for (PortConfig portConfig : service.getEndpoint().getPorts()) {
                        Integer targetPort = null;
                        Integer publishedPort = null;
                        
                        // 安全地获取端口值
                        Object targetPortObj = portConfig.getTargetPort();
                        if (targetPortObj != null) {
                            if (targetPortObj instanceof Integer) {
                                targetPort = (Integer) targetPortObj;
                            } else if (targetPortObj instanceof Number) {
                                targetPort = ((Number) targetPortObj).intValue();
                            }
                        }
                        
                        Object publishedPortObj = portConfig.getPublishedPort();
                        if (publishedPortObj != null) {
                            if (publishedPortObj instanceof Integer) {
                                publishedPort = (Integer) publishedPortObj;
                            } else if (publishedPortObj instanceof Number) {
                                publishedPort = ((Number) publishedPortObj).intValue();
                            }
                        }
                        
                        portMappings.add(new PortMapping(
                            portConfig.getName() != null ? portConfig.getName() : "port",
                            portConfig.getProtocol() != null ? portConfig.getProtocol().toString().toLowerCase() : "tcp",
                            targetPort,
                            publishedPort
                        ));
                    }
                } 
                // 如果运行时没有端口信息，回退到配置中的端口
                else if (spec != null && spec.getEndpointSpec() != null &&
                    spec.getEndpointSpec().getPorts() != null) {
                    for (PortConfig portConfig : spec.getEndpointSpec().getPorts()) {
                        Integer targetPort = null;
                        Integer publishedPort = null;
                        
                        Object targetPortObj = portConfig.getTargetPort();
                        if (targetPortObj != null) {
                            if (targetPortObj instanceof Integer) {
                                targetPort = (Integer) targetPortObj;
                            } else if (targetPortObj instanceof Number) {
                                targetPort = ((Number) targetPortObj).intValue();
                            }
                        }
                        
                        Object publishedPortObj = portConfig.getPublishedPort();
                        if (publishedPortObj != null) {
                            if (publishedPortObj instanceof Integer) {
                                publishedPort = (Integer) publishedPortObj;
                            } else if (publishedPortObj instanceof Number) {
                                publishedPort = ((Number) publishedPortObj).intValue();
                            }
                        }
                        
                        portMappings.add(new PortMapping(
                            portConfig.getName() != null ? portConfig.getName() : "port",
                            portConfig.getProtocol() != null ? portConfig.getProtocol().toString().toLowerCase() : "tcp",
                            targetPort,
                            publishedPort
                        ));
                    }
                }

                return new ServicePortInfo(serviceId, serviceName, imageName, "running", host, portMappings);
            } catch (Exception e) {
                log.warn("获取服务端口配置失败，使用默认值: {}", e.getMessage());
                // 如果获取失败，返回基本信息
                return new ServicePortInfo(serviceId, serviceId.length() > 12 ? serviceId.substring(0, 12) : serviceId, "", "running", host, portMappings);
            }

        } catch (Exception e) {
            log.error("获取服务端口信息失败: {}", serviceId, e);
            throw new ServiceException("获取服务端口信息失败: " + e.getMessage(), e);
        }
    }

    private String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
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
}
