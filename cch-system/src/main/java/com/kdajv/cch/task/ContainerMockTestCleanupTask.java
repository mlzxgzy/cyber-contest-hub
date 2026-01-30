package com.kdajv.cch.task;

import com.kdajv.cch.service.IChallengeContainerMockTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 容器模拟测试清理任务
 * 每分钟检查并清理过期的测试环境
 *
 * @author system
 * @date 2026-01-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContainerMockTestCleanupTask {

    private final IChallengeContainerMockTestService containerMockTestService;

    /**
     * 每分钟执行一次，清理过期的测试环境
     * cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 * * * * ?")
    public void cleanupExpiredTests() {
        log.debug("开始执行容器模拟测试过期清理任务...");

        try {
            int count = containerMockTestService.cleanupExpiredTests();
            if (count > 0) {
                log.info("容器模拟测试过期清理任务完成，共清理 {} 个过期测试", count);
            }
        } catch (Exception e) {
            log.error("容器模拟测试过期清理任务执行失败", e);
        }
    }
}
