package com.kdajv.cch.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.kdajv.cch.container.ContainerClient;
import com.kdajv.cch.container.DockerContainerClient;
import com.kdajv.cch.domain.vo.ClusterNodeVo;
import com.kdajv.cch.domain.vo.CchContainerConfigVo;
import com.kdajv.cch.domain.vo.DockerContainerVo;
import com.kdajv.cch.domain.vo.DockerImageVo;
import com.kdajv.cch.service.ICchContainerConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.system.domain.SysConfig;
import org.dromara.system.domain.bo.SysConfigBo;
import org.dromara.system.mapper.SysConfigMapper;
import org.dromara.system.service.ISysConfigService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 容器配置Service业务层处理
 *
 * @author system
 * @date 2025-12-11
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CchContainerConfigServiceImpl implements ICchContainerConfigService {

    private static final String CONFIG_KEY_PREFIX = "cch.container.config.";
    private static final String ACTIVE_INSTANCE_KEY = "cch.container.active.instance";

    // 当前活跃的容器客户端实例（Docker / Kubernetes 等）
    private volatile ContainerClient activeClient = null;
    private volatile Long activeInstanceId = null;
    private final ReentrantLock lock = new ReentrantLock();

    private final ISysConfigService sysConfigService;
    private final SysConfigMapper sysConfigMapper;

    /**
     * 查询容器配置
     */
    @Override
    public CchContainerConfigVo queryById(Long id) {
        SysConfig config = sysConfigMapper.selectById(id);
        if (config == null || !isContainerConfig(config)) {
            return null;
        }
        return convertToVo(config);
    }

    /**
     * 查询容器配置列表
     */
    @Override
    public TableDataInfo<CchContainerConfigVo> queryPageList(String configName, String backendType, PageQuery pageQuery) {
        LambdaQueryWrapper<SysConfig> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(configName), SysConfig::getConfigName, configName);
        lqw.like(SysConfig::getConfigKey, CONFIG_KEY_PREFIX + "%");
        lqw.orderByDesc(SysConfig::getCreateTime);

        Page<SysConfig> page = sysConfigMapper.selectPage(pageQuery.build(), lqw);
        List<CchContainerConfigVo> voList = page.getRecords().stream().map(this::convertToVo).filter(vo -> StringUtils.isBlank(backendType) || backendType.equals(vo.getBackendType())).collect(Collectors.toList());

        Page<CchContainerConfigVo> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(voList);
        return TableDataInfo.build(voPage);
    }

    /**
     * 查询容器配置列表
     */
    @Override
    public List<CchContainerConfigVo> queryList(String configName, String backendType) {
        LambdaQueryWrapper<SysConfig> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(configName), SysConfig::getConfigName, configName);
        lqw.like(SysConfig::getConfigKey, CONFIG_KEY_PREFIX + "%");
        lqw.orderByDesc(SysConfig::getCreateTime);

        return sysConfigMapper.selectList(lqw).stream().map(this::convertToVo).filter(vo -> StringUtils.isBlank(backendType) || backendType.equals(vo.getBackendType())).collect(Collectors.toList());
    }

    /**
     * 新增容器配置
     */
    @Override
    public Boolean insertByVo(CchContainerConfigVo vo) {
        validEntityBeforeSave(vo);

        SysConfigBo bo = new SysConfigBo();
        bo.setConfigName(vo.getConfigName());
        bo.setConfigKey(CONFIG_KEY_PREFIX + vo.getConfigName());
        bo.setConfigValue(convertToJson(vo));
        bo.setConfigType("N");
        bo.setRemark(vo.getRemark());

        // 检查configKey是否唯一
        if (!sysConfigService.checkConfigKeyUnique(bo)) {
            throw new ServiceException("配置名称已存在");
        }

        sysConfigService.insertConfig(bo);
        return true;
    }

    /**
     * 修改容器配置
     */
    @Override
    public Boolean updateByVo(CchContainerConfigVo vo) {
        if (vo.getId() == null) {
            throw new ServiceException("配置ID不能为空");
        }

        SysConfig config = sysConfigMapper.selectById(vo.getId());
        if (config == null || !isContainerConfig(config)) {
            throw new ServiceException("容器配置不存在");
        }

        validEntityBeforeSave(vo);

        SysConfigBo bo = new SysConfigBo();
        bo.setConfigId(vo.getId());
        bo.setConfigName(vo.getConfigName());
        bo.setConfigKey(CONFIG_KEY_PREFIX + vo.getConfigName());
        bo.setConfigValue(convertToJson(vo));
        bo.setRemark(vo.getRemark());

        // 检查configKey是否唯一（排除自己）
        if (!sysConfigService.checkConfigKeyUnique(bo)) {
            throw new ServiceException("配置名称已存在");
        }

        sysConfigService.updateConfig(bo);
        return true;
    }

    /**
     * 批量删除容器配置
     */
    @Override
    public Boolean deleteByIds(List<Long> ids) {
        lock.lock();
        try {
            for (Long id : ids) {
                // 如果删除的是当前活跃实例，先断开连接
                if (activeInstanceId != null && activeInstanceId.equals(id)) {
                    disconnectActiveInstance();
                }

                SysConfig config = sysConfigMapper.selectById(id);
                if (config != null && isContainerConfig(config)) {
                    sysConfigService.deleteConfigByIds(List.of(id));
                }
            }
        } finally {
            lock.unlock();
        }
        return true;
    }

    /**
     * 测试连接并激活实例（单例模式）
     */
    @Override
    public Boolean testConnection(Long id) {
        lock.lock();
        try {
            CchContainerConfigVo config = queryById(id);
            if (config == null) {
                throw new ServiceException("容器配置不存在");
            }

            // 如果已有其他活跃实例，先断开
            if (activeInstanceId != null && !activeInstanceId.equals(id)) {
                log.info("检测到已有活跃实例 {}，正在断开...", activeInstanceId);
                disconnectActiveInstance();
            }

            // 测试新连接
            boolean success = false;
            try {
                if ("docker".equals(config.getBackendType())) {
                    success = connectDocker(config);
                } else if ("kubernetes".equals(config.getBackendType())) {
                    throw new ServiceException("Kubernetes连接测试暂未实现");
                } else {
                    throw new ServiceException("不支持的后端类型: " + config.getBackendType());
                }

                if (success) {
                    // 保存当前活跃实例
                    activeInstanceId = id;
                    saveActiveInstance(id);
                    log.info("容器实例 {} 连接成功并已激活", id);
                }
            } catch (Exception e) {
                log.error("连接测试失败", e);
                throw new ServiceException("连接测试失败: " + e.getMessage());
            }

            return success;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 获取当前活跃的容器实例
     * 直接从数据库读取，确保应用重启后也能正确获取
     */
    @Override
    public CchContainerConfigVo getActiveInstance() {
        try {
            // 直接从sys_config表获取活跃实例ID
            String activeInstanceValue = sysConfigService.selectConfigByKey(ACTIVE_INSTANCE_KEY);
            if (StringUtils.isBlank(activeInstanceValue)) {
                return null;
            }
            Long instanceId = Long.parseLong(activeInstanceValue);
            return queryById(instanceId);
        } catch (Exception e) {
            log.error("获取活跃实例失败", e);
            return null;
        }
    }

    /**
     * 断开当前活跃实例（公开方法）
     */
    @Override
    public void disconnectActiveInstance() {
        lock.lock();
        try {
            if (activeClient != null) {
                try {
                    activeClient.close();
                    log.info("已断开活跃实例连接: {}", activeInstanceId);
                } catch (Exception e) {
                    log.error("断开连接时出错", e);
                } finally {
                    activeClient = null;
                }
            }
            activeInstanceId = null;
            clearActiveInstance();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 连接Docker
     */
    private boolean connectDocker(CchContainerConfigVo config) {
        try {
            if (StringUtils.isBlank(config.getDockerUrl())) {
                throw new ServiceException("Docker URL不能为空");
            }

            log.info("正在连接Docker: {}", config.getDockerUrl());

            // 使用docker-java创建DockerClient
            DefaultDockerClientConfig.Builder configBuilder = DefaultDockerClientConfig.createDefaultConfigBuilder().withDockerHost(config.getDockerUrl());

            if (StringUtils.isNotBlank(config.getDockerApiVersion())) {
                configBuilder.withApiVersion(config.getDockerApiVersion());
            }

            // 处理TLS认证
            if ("1".equals(config.getDockerTlsVerify()) && StringUtils.isNotBlank(config.getDockerCertPath())) {
                configBuilder.withDockerTlsVerify(true);
                configBuilder.withDockerCertPath(config.getDockerCertPath());
            }

            DefaultDockerClientConfig dockerConfig = configBuilder.build();
            ApacheDockerHttpClient apacheDockerHttpClient = new ApacheDockerHttpClient.Builder().dockerHost(dockerConfig.getDockerHost()).sslConfig(dockerConfig.getSSLConfig()).build();
            DockerClient dockerClient = DockerClientBuilder.getInstance(dockerConfig).withDockerHttpClient(apacheDockerHttpClient).build();

            // 包装为通用容器客户端并测试连接
            DockerContainerClient containerClient = new DockerContainerClient(dockerClient);
            containerClient.ping();

            // 保存连接
            activeClient = containerClient;

            log.info("Docker连接成功: {}", config.getDockerUrl());
            return true;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Docker连接失败: {}", config.getDockerUrl(), e);
            throw new ServiceException("Docker连接失败: " + e.getMessage());
        }
    }

    /**
     * Ping当前活跃实例
     */
    public boolean pingActiveInstance() {
        if (activeInstanceId == null || activeClient == null) {
            return false;
        }

        try {
            CchContainerConfigVo config = queryById(activeInstanceId);
            if (config == null) {
                log.warn("活跃实例 {} 已不存在，断开连接", activeInstanceId);
                disconnectActiveInstance();
                return false;
            }

            if ("docker".equals(config.getBackendType())) {
                if (activeClient != null) {
                    activeClient.ping();
                    return true;
                }
            } else if ("kubernetes".equals(config.getBackendType())) {
                // TODO: Kubernetes ping
                return true;
            }
        } catch (Exception e) {
            log.error("Ping活跃实例失败: {}", activeInstanceId, e);
            disconnectActiveInstance();
            return false;
        }

        return false;
    }

    /**
     * 保存当前活跃实例ID到sys_config
     */
    private void saveActiveInstance(Long instanceId) {
        try {
            SysConfig activeConfig = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, ACTIVE_INSTANCE_KEY));

            if (activeConfig != null) {
                SysConfigBo bo = new SysConfigBo();
                bo.setConfigId(activeConfig.getConfigId());
                bo.setConfigValue(String.valueOf(instanceId));
                sysConfigService.updateConfig(bo);
            } else {
                SysConfigBo bo = new SysConfigBo();
                bo.setConfigName("当前活跃容器实例");
                bo.setConfigKey(ACTIVE_INSTANCE_KEY);
                bo.setConfigValue(String.valueOf(instanceId));
                bo.setConfigType("N");
                sysConfigService.insertConfig(bo);
            }
        } catch (Exception e) {
            log.error("保存活跃实例失败", e);
        }
    }

    /**
     * 清除活跃实例记录
     */
    private void clearActiveInstance() {
        try {
            SysConfig activeConfig = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, ACTIVE_INSTANCE_KEY));
            if (activeConfig != null) {
                sysConfigService.deleteConfigByIds(List.of(activeConfig.getConfigId()));
            }
        } catch (Exception e) {
            log.error("清除活跃实例记录失败", e);
        }
    }

    /**
     * 初始化：从sys_config恢复活跃实例
     */
    public void initActiveInstance() {
        try {
            String activeInstanceValue = sysConfigService.selectConfigByKey(ACTIVE_INSTANCE_KEY);
            if (StringUtils.isNotBlank(activeInstanceValue)) {
                Long instanceId = Long.parseLong(activeInstanceValue);
                CchContainerConfigVo config = queryById(instanceId);
                if (config != null) {
                    // 尝试恢复连接
                    if (testConnection(instanceId)) {
                        log.info("成功恢复活跃实例: {}", instanceId);
                    } else {
                        log.warn("恢复活跃实例失败: {}", instanceId);
                        clearActiveInstance();
                    }
                } else {
                    log.warn("活跃实例 {} 不存在，清除记录", instanceId);
                    clearActiveInstance();
                }
            }
        } catch (Exception e) {
            log.error("初始化活跃实例失败", e);
            clearActiveInstance();
        }
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(CchContainerConfigVo vo) {
        if (StringUtils.isBlank(vo.getConfigName())) {
            throw new ServiceException("配置名称不能为空");
        }
        if (StringUtils.isBlank(vo.getBackendType())) {
            throw new ServiceException("后端类型不能为空");
        }

        // Docker类型需要验证Docker URL
        if ("docker".equals(vo.getBackendType())) {
            if (StringUtils.isBlank(vo.getDockerUrl())) {
                throw new ServiceException("Docker类型必须填写Docker URL");
            }
        }
        // Kubernetes类型需要验证Kubernetes配置
        if ("kubernetes".equals(vo.getBackendType())) {
            if (StringUtils.isBlank(vo.getKubernetesConfig())) {
                throw new ServiceException("Kubernetes类型必须填写Kubernetes配置");
            }
        }
    }

    /**
     * 判断是否为容器配置
     */
    private boolean isContainerConfig(SysConfig config) {
        return config.getConfigKey() != null && config.getConfigKey().startsWith(CONFIG_KEY_PREFIX);
    }

    /**
     * 转换为VO对象
     */
    private CchContainerConfigVo convertToVo(SysConfig config) {
        CchContainerConfigVo vo = new CchContainerConfigVo();
        vo.setId(config.getConfigId());
        vo.setConfigName(config.getConfigName());
        vo.setRemark(config.getRemark());
        vo.setCreateTime(config.getCreateTime());

        // 从JSON中解析配置信息
        @SuppressWarnings("unchecked") Map<String, Object> configMap = JSONUtil.toBean(config.getConfigValue(), Map.class);
        if (configMap != null) {
            vo.setBackendType((String) configMap.get("backendType"));
            vo.setDockerUrl((String) configMap.get("dockerUrl"));
            vo.setDockerApiVersion((String) configMap.get("dockerApiVersion"));
            vo.setDockerCertPath((String) configMap.get("dockerCertPath"));
            vo.setDockerTlsVerify((String) configMap.get("dockerTlsVerify"));
            vo.setKubernetesConfig((String) configMap.get("kubernetesConfig"));
            vo.setKubernetesNamespace((String) configMap.get("kubernetesNamespace"));
            vo.setStatus((String) configMap.getOrDefault("status", "0"));
        }

        return vo;
    }

    /**
     * 转换为JSON字符串
     */
    private String convertToJson(CchContainerConfigVo vo) {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("backendType", vo.getBackendType());
        configMap.put("dockerUrl", vo.getDockerUrl());
        configMap.put("dockerApiVersion", vo.getDockerApiVersion());
        configMap.put("dockerCertPath", vo.getDockerCertPath());
        configMap.put("dockerTlsVerify", vo.getDockerTlsVerify());
        configMap.put("kubernetesConfig", vo.getKubernetesConfig());
        configMap.put("kubernetesNamespace", vo.getKubernetesNamespace());
        configMap.put("status", StringUtils.isNotBlank(vo.getStatus()) ? vo.getStatus() : "0");
        return JSONUtil.toJsonStr(configMap);
    }

    @Override
    public List<DockerContainerVo> getDockerContainers() {
        if (activeClient == null) {
            throw new ServiceException("没有活跃的容器连接");
        }

        try {
            return activeClient.listContainers();
        } catch (Exception e) {
            log.error("获取容器列表失败", e);
            throw new ServiceException("获取容器列表失败: " + e.getMessage());
        }
    }

    @Override
    public List<DockerImageVo> getDockerImages() {
        if (activeClient == null) {
            throw new ServiceException("没有活跃的容器连接");
        }

        try {
            return activeClient.listImages();
        } catch (Exception e) {
            log.error("获取镜像列表失败", e);
            throw new ServiceException("获取镜像列表失败: " + e.getMessage());
        }
    }

    @Override
    public ContainerClient getActiveClient() {
        return activeClient;
    }

    @Override
    public List<ClusterNodeVo> getClusterNodes() {
        if (activeClient == null) {
            throw new ServiceException("没有活跃的容器连接");
        }

        try {
            return activeClient.listNodes();
        } catch (Exception e) {
            log.error("获取节点列表失败", e);
            throw new ServiceException("获取节点列表失败: " + e.getMessage());
        }
    }

    @Override
    public void updateNodeExternalAddress(String nodeId, String address) {
        if (activeClient == null) {
            throw new ServiceException("没有活跃的容器连接");
        }

        // 校验地址格式：IP或域名
        if (address != null && !address.trim().isEmpty()) {
            String trimmedAddress = address.trim();
            // 校验是否为有效IP或域名
            if (!isValidIpAddress(trimmedAddress) && !isValidDomain(trimmedAddress)) {
                throw new ServiceException("请输入有效的IP地址或域名");
            }
            address = trimmedAddress;
        }

        try {
            activeClient.updateNodeExternalAddress(nodeId, address);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新节点外部访问地址失败", e);
            throw new ServiceException("更新节点外部访问地址失败: " + e.getMessage());
        }
    }

    /**
     * 校验是否为有效IP地址
     */
    private boolean isValidIpAddress(String address) {
        // IPv4校验
        String ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        // IPv6校验（简化版）
        String ipv6Pattern = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$" +
                "|^([0-9a-fA-F]{1,4}:){1,7}:$" +
                "|^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$" +
                "|^([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}$" +
                "|^([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}$" +
                "|^([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}$" +
                "|^([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}$" +
                "|^[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})$" +
                "|^:((:[0-9a-fA-F]{1,4}){1,7}|:)$" +
                "|^::$";

        return address.matches(ipv4Pattern) || address.matches(ipv6Pattern);
    }

    /**
     * 校验是否为有效域名
     */
    private boolean isValidDomain(String domain) {
        // 域名正则校验
        String domainPattern = "^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$";
        return domain.matches(domainPattern);
    }

    @Override
    public void updateNodeLabels(String nodeId, Map<String, String> labels) {
        if (activeClient == null) {
            throw new ServiceException("没有活跃的容器连接");
        }

        try {
            activeClient.updateNodeLabels(nodeId, labels);
        } catch (Exception e) {
            log.error("更新节点标签失败", e);
            throw new ServiceException("更新节点标签失败: " + e.getMessage());
        }
    }

}
