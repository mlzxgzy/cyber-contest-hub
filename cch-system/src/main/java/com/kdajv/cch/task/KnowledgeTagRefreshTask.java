package com.kdajv.cch.task;

import com.kdajv.cch.service.IChallengeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 知识点标签缓存维护任务
 * <p>
 * 扫描全部题目草稿的 config，聚合去重知识点标签并写入缓存，
 * 供题目列表搜索下拉使用。启动时刷新一次，之后每小时定时刷新。
 *
 * @author system
 * @date 2026-03-19
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeTagRefreshTask implements CommandLineRunner {

    private final IChallengeService challengeService;

    /**
     * 每小时执行一次，刷新知识点标签缓存
     * cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void refreshKnowledgeTags() {
        log.debug("开始刷新知识点标签缓存...");
        challengeService.refreshKnowledgeTags();
    }

    /**
     * 应用启动时刷新一次知识点标签缓存
     */
    @Override
    public void run(String... args) {
        log.info("启动时初始化知识点标签缓存...");
        challengeService.refreshKnowledgeTags();
    }
}
