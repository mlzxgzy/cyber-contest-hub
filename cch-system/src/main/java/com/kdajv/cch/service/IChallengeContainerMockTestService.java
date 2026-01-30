package com.kdajv.cch.service;

import com.kdajv.cch.domain.vo.ChallengeContainerMockTestVo;
import com.kdajv.cch.domain.vo.ContainerMockTestSourceVo;

import java.util.Date;
import java.util.List;

/**
 * 容器模拟测试Service接口
 *
 * @author system
 * @date 2026-01-30
 */
public interface IChallengeContainerMockTestService {

    /**
     * 获取可选来源列表（同一题目下的草稿+版本）
     *
     * @param challengeId 题目ID
     * @return 来源选项列表
     */
    List<ContainerMockTestSourceVo> getAvailableSources(Long challengeId);

    /**
     * 启动容器模拟测试
     *
     * @param sourceType 来源类型：draft 或 version
     * @param sourceId   来源ID（草稿ID或版本ID）
     * @return 测试详情
     */
    ChallengeContainerMockTestVo startContainerMockTest(String sourceType, Long sourceId);

    /**
     * 获取测试详情
     *
     * @param id 测试ID
     * @return 测试详情
     */
    ChallengeContainerMockTestVo getContainerMockTestDetail(Long id);

    /**
     * 获取我的活跃测试列表
     *
     * @return 活跃测试列表
     */
    List<ChallengeContainerMockTestVo> getMyActiveTests();

    /**
     * 延长测试时间
     *
     * @param id      测试ID
     * @param minutes 延长时间（分钟）
     * @return 是否成功
     */
    boolean extendTime(Long id, Integer minutes);

    /**
     * 销毁测试环境
     *
     * @param id 测试ID
     * @return 是否成功
     */
    boolean destroyEnvironment(Long id);

    /**
     * 清理过期测试（由定时任务调用）
     *
     * @return 清理的测试数量
     */
    int cleanupExpiredTests();

    /**
     * 根据来源获取草稿ID
     *
     * @param sourceType 来源类型
     * @param sourceId   来源ID
     * @return 草稿ID
     */
    Long getDraftIdBySource(String sourceType, Long sourceId);
}
