package com.kdajv.cch.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kdajv.cch.container.ContainerClient;
import com.kdajv.cch.domain.ChallengeContainerMockTest;
import com.kdajv.cch.domain.DraftConfig;
import com.kdajv.cch.domain.vo.*;
import com.kdajv.cch.mapper.ChallengeContainerMockTestMapper;
import com.kdajv.cch.mapper.ChallengeDraftMapper;
import com.kdajv.cch.mapper.ChallengeVersionMapper;
import com.kdajv.cch.service.ICchContainerConfigService;
import com.kdajv.cch.service.IChallengeContainerMockTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 容器模拟测试Service实现（使用 Docker Swarm Service）
 *
 * @author system
 * @date 2026-01-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChallengeContainerMockTestServiceImpl implements IChallengeContainerMockTestService {

    private static final int DEFAULT_EXPIRE_MINUTES = 30;
    private static final int MAX_EXTEND_COUNT = 5;

    private final ChallengeContainerMockTestMapper mockTestMapper;
    private final ChallengeDraftMapper draftMapper;
    private final ChallengeVersionMapper versionMapper;
    private final ICchContainerConfigService containerConfigService;

    @Override
    public List<ContainerMockTestSourceVo> getAvailableSources(Long challengeId) {
        List<ContainerMockTestSourceVo> sources = new ArrayList<>();

        // 获取同一题目下的草稿列表
        List<ContainerMockTestSourceVo> drafts = mockTestMapper.selectDraftList(challengeId);
        for (ContainerMockTestSourceVo draft : drafts) {
            draft.setSourceType("draft");
            // 确保 draftId 被设置（草稿的 id 就是 draftId）
            if (draft.getDraftId() == null) {
                draft.setDraftId(draft.getId());
            }
            draft.setName(draft.getChallengeName());
            sources.add(draft);
        }
        // 获取同一题目下的版本列表
        List<ContainerMockTestSourceVo> versions = mockTestMapper.selectVersionList(challengeId);
        for (ContainerMockTestSourceVo version : versions) {
            version.setSourceType("version");
            version.setName(String.format("%s - %s (ID: %d)", version.getChallengeName(), version.getVersionTag() != null ? version.getVersionTag() : "v" + version.getId(), version.getId()));
            sources.add(version);
        }
        return sources;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChallengeContainerMockTestVo startContainerMockTest(String sourceType, Long sourceId) {
        // 1. 获取草稿ID
        Long draftId = getDraftIdBySource(sourceType, sourceId);
        if (draftId == null) {
            throw new ServiceException("无法获取草稿ID");
        }

        // 2. 获取草稿数据
        ChallengeDraftVo draft = draftMapper.selectVoById(draftId);
        if (draft == null) {
            throw new ServiceException("草稿不存在");
        }

        DraftConfig config = draft.getConfig();
        if (config == null || config.getContainerTargets() == null || config.getContainerTargets().isEmpty()) {
            throw new ServiceException("草稿中没有容器靶机配置");
        }

        // 3. 获取活跃的容器客户端
        ContainerClient containerClient = containerConfigService.getActiveClient();
        if (containerClient == null) {
            throw new ServiceException("没有活跃的容器连接，请先配置并测试容器连接");
        }

        // 4. 启动 Docker Swarm Service 并收集信息
        List<String> serviceIds = new ArrayList<>();
        List<ContainerMockTestContainerVo> containers = new ArrayList<>();
        Date now = new Date();
        Date expireTime = new Date(now.getTime() + DEFAULT_EXPIRE_MINUTES * 60 * 1000L);

        for (int i = 0; i < config.getContainerTargets().size(); i++) {
            DraftConfig.ContainerTarget target = config.getContainerTargets().get(i);

            if (target.getImageName() == null || target.getImageName().isEmpty()) {
                log.warn("靶机 {} 未配置镜像，跳过", target.getName());
                continue;
            }

            // 获取镜像信息
            String imageName = target.getImageName();

            // 构建服务名称（添加mock-前缀以标识）
            // Docker Swarm 服务名称必须是有效的 DNS 名称：只包含小写字母、数字、连字符
            String serviceName = String.format("mock-%s-%s-%d",
                sanitizeServiceName(draft.getChallengeName()),
                sanitizeServiceName(target.getName()),
                System.currentTimeMillis());

            // 获取宿主机地址（从节点标签获取）
            String host = "localhost";
            try {
                List<ClusterNodeVo> nodes = containerConfigService.getClusterNodes();
                if (!nodes.isEmpty()) {
                    for (ClusterNodeVo node : nodes) {
                        if (StringUtils.isNotBlank(node.getExternalAccessAddress())) {
                            host = node.getExternalAccessAddress();
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("获取集群节点失败，使用默认地址: {}", e.getMessage());
            }

            try {
                // 创建并启动 Docker Swarm Service
                ContainerClient.ServicePortInfo portInfo = containerClient.createAndStartService(imageName, target.getEnv(), target.getPorts(), target.getResources() != null ? target.getResources().getCpuLimit() : null, target.getResources() != null ? target.getResources().getMemoryLimit() : null, serviceName);

                serviceIds.add(portInfo.serviceId());

                // 构建端口暴露信息
                List<ContainerClient.PortMapping> mappings = portInfo.portMappings();

                // 如果 getServicePortInfo 返回的端口信息为空，使用配置的端口信息
                if (mappings == null || mappings.isEmpty()) {
                    // 使用配置的端口作为后备
                    if (target.getPorts() != null) {
                        for (Map.Entry<String, DraftConfig.PortConfig> entry : target.getPorts().entrySet()) {
                            DraftConfig.PortConfig portConfig = entry.getValue();
                            ContainerMockTestContainerVo containerVo = new ContainerMockTestContainerVo();
                            containerVo.setName(target.getName() != null ? target.getName() : "target-" + i);
                            containerVo.setHost(host);
                            containerVo.setProtocol(portConfig.getProtocol() != null ? portConfig.getProtocol().toLowerCase() : "tcp");
                            containerVo.setInternalPort(portConfig.getInternalPort());
                            containerVo.setExternalPort(null); // 待分配
                            containers.add(containerVo);
                        }
                    }
                } else {
                    // 使用实际获取的端口信息
                    for (ContainerClient.PortMapping pm : mappings) {
                        ContainerMockTestContainerVo containerVo = new ContainerMockTestContainerVo();
                        containerVo.setName(target.getName() != null ? target.getName() : "target-" + i);
                        containerVo.setHost(portInfo.host() != null ? portInfo.host() : host);
                        containerVo.setProtocol(pm.protocol());
                        containerVo.setInternalPort(pm.internalPort());
                        containerVo.setExternalPort(pm.externalPort());
                        containers.add(containerVo);
                    }
                }

                log.info("Docker Swarm Service 启动成功: {}, 端口映射: {}", serviceName, mappings);

            } catch (Exception e) {
                log.error("启动 Docker Swarm Service 失败: {}", serviceName, e);
                // 清理已启动的服务
                cleanupServices(containerClient, serviceIds);
                throw new ServiceException("启动 Docker Swarm Service 失败: " + e.getMessage());
            }
        }

        if (serviceIds.isEmpty()) {
            throw new ServiceException("没有成功启动任何服务");
        }

        // 5. 保存测试记录（使用 JSON 字符串存储）
        ChallengeContainerMockTest test = new ChallengeContainerMockTest();
        test.setDraftId(draftId);
        test.setSourceType(sourceType);
        test.setSourceId(sourceId);
        test.setChallengeName(draft.getChallengeName());
        test.setContainerIds(JSONUtil.toJsonStr(serviceIds));
        test.setExposeInfo(JSONUtil.toJsonStr(containers));
        test.setStatus("running");
        test.setExpireTime(expireTime);
        test.setExtendCount(0);

        mockTestMapper.insert(test);

        // 6. 返回结果
        ChallengeContainerMockTestVo result = convertToVo(test, containers);
        result.setRemainingSeconds(calculateRemainingSeconds(expireTime));

        return result;
    }

    @Override
    public ChallengeContainerMockTestVo getContainerMockTestDetail(Long id) {
        ChallengeContainerMockTest test = mockTestMapper.selectById(id);
        if (test == null || !"running".equals(test.getStatus())) {
            return null;
        }

        List<ContainerMockTestContainerVo> containers = parseExposeInfo(test.getExposeInfo());
        ChallengeContainerMockTestVo vo = convertToVo(test, containers);
        vo.setRemainingSeconds(calculateRemainingSeconds(test.getExpireTime()));

        return vo;
    }

    @Override
    public List<ChallengeContainerMockTestVo> getMyActiveTests() {
        List<ChallengeContainerMockTest> tests = mockTestMapper.selectList(
            new LambdaQueryWrapper<ChallengeContainerMockTest>()
                .eq(ChallengeContainerMockTest::getStatus, "running")
                .orderByDesc(ChallengeContainerMockTest::getCreateTime)
        );

        return tests.stream().map(test -> {
            List<ContainerMockTestContainerVo> containers = parseExposeInfo(test.getExposeInfo());
            ChallengeContainerMockTestVo vo = convertToVo(test, containers);
            vo.setRemainingSeconds(calculateRemainingSeconds(test.getExpireTime()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean extendTime(Long id, Integer minutes) {
        ChallengeContainerMockTest test = mockTestMapper.selectById(id);
        if (test == null || !"running".equals(test.getStatus())) {
            throw new ServiceException("测试不存在或已结束");
        }

        if (test.getExtendCount() >= MAX_EXTEND_COUNT) {
            throw new ServiceException("已达到最大延长次数限制(" + MAX_EXTEND_COUNT + ")");
        }

        Date newExpireTime = new Date(test.getExpireTime().getTime() + minutes * 60 * 1000L);

        test.setExpireTime(newExpireTime);
        test.setExtendCount(test.getExtendCount() + 1);
        test.setUpdateTime(new Date());

        return mockTestMapper.updateById(test) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean destroyEnvironment(Long id) {
        ChallengeContainerMockTest test = mockTestMapper.selectById(id);
        if (test == null) {
            return false;
        }

        if ("destroying".equals(test.getStatus()) || "expired".equals(test.getStatus())) {
            return true; // 已经处理中或已过期
        }

        // 更新状态
        test.setStatus("destroying");
        test.setUpdateTime(new Date());
        mockTestMapper.updateById(test);

        // 销毁服务
        try {
            // 直接从 JSON 字符串解析
            List<String> serviceIds = parseContainerIds(test.getContainerIds());
            if (serviceIds != null && !serviceIds.isEmpty()) {
                ContainerClient containerClient = containerConfigService.getActiveClient();
                if (containerClient != null) {
                    cleanupServices(containerClient, serviceIds);
                }
            }
        } catch (Exception e) {
            log.error("销毁 Docker Swarm Service 失败: {}", id, e);
        }

        // 标记为过期
        test.setStatus("expired");
        test.setUpdateTime(new Date());
        mockTestMapper.updateById(test);

        return true;
    }

    @Override
    public int cleanupExpiredTests() {
        List<Long> expiredIds = mockTestMapper.selectExpiredTestIds();

        if (expiredIds.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (Long id : expiredIds) {
            try {
                if (destroyEnvironment(id)) {
                    count++;
                }
            } catch (Exception e) {
                log.error("清理过期测试失败: {}", id, e);
            }
        }

        log.info("清理过期测试完成，共清理 {} 个", count);
        return count;
    }

    @Override
    public Long getDraftIdBySource(String sourceType, Long sourceId) {
        if ("draft".equals(sourceType)) {
            return sourceId;
        } else if ("version".equals(sourceType)) {
            // sourceType为version时，需要查询版本表获取draftId
            ChallengeVersionVo version = versionMapper.selectVoById(sourceId);
            if (version != null) {
                return version.getDraftId();
            }
        }
        return null;
    }

    // ==================== 私有方法 ====================

    private ChallengeContainerMockTestVo convertToVo(ChallengeContainerMockTest test, List<ContainerMockTestContainerVo> containers) {
        ChallengeContainerMockTestVo vo = new ChallengeContainerMockTestVo();
        vo.setId(test.getId());
        vo.setDraftId(test.getDraftId());
        vo.setSourceType(test.getSourceType());
        vo.setSourceId(test.getSourceId());
        vo.setChallengeName(test.getChallengeName());
        // 从 JSON 字符串解析 containerIds
        vo.setContainerIds(parseContainerIds(test.getContainerIds()));
        vo.setContainers(containers);
        vo.setStatus(test.getStatus());
        vo.setCreateTime(test.getCreateTime());
        vo.setExpireTime(test.getExpireTime());
        vo.setExtendCount(test.getExtendCount());
        return vo;
    }

    /**
     * 从 JSON 字符串解析容器 ID 列表
     */
    private List<String> parseContainerIds(String json) {
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            return JSONUtil.toList(json, String.class);
        } catch (Exception e) {
            log.error("解析容器ID列表失败: {}", json, e);
            return new ArrayList<>();
        }
    }

    /**
     * 从 JSON 字符串解析暴露信息
     */
    private List<ContainerMockTestContainerVo> parseExposeInfo(String json) {
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            return JSONUtil.toList(json, ContainerMockTestContainerVo.class);
        } catch (Exception e) {
            log.error("解析暴露信息失败", e);
            return new ArrayList<>();
        }
    }

    private Long calculateRemainingSeconds(Date expireTime) {
        if (expireTime == null) {
            return 0L;
        }
        long remaining = (expireTime.getTime() - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    /**
     * 清理 Docker Swarm Service
     */
    private void cleanupServices(ContainerClient client, List<String> serviceIds) {
        for (String serviceId : serviceIds) {
            try {
                if (client.isServiceRunning(serviceId)) {
                    client.removeService(serviceId);
                }
            } catch (Exception e) {
                log.error("清理 Docker Swarm Service 失败: {}", serviceId, e);
            }
        }
    }

    /**
     * 清理服务名称，转换为有效的 DNS 名称
     * 规则：只包含小写字母(a-z)、数字(0-9)和连字符(-)
     */
    private String sanitizeServiceName(String name) {
        if (name == null || name.isEmpty()) {
            return "unknown";
        }
        // 转小写，只保留小写字母、数字和连字符，移除其他字符
        String sanitized = name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        // 移除首尾连字符
        sanitized = sanitized.replaceAll("^-+|-+$", "");
        // 限制长度（DNS 名称最大 63 字符）
        if (sanitized.length() > 63) {
            sanitized = sanitized.substring(0, 63);
        }
        // 确保不为空
        return sanitized.isEmpty() ? "unknown" : sanitized;
    }
}
