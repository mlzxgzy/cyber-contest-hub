package com.kdajv.cch.task;

import com.kdajv.cch.service.IChallengeVersionExportTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 导出任务清理任务
 * 每小时检查并清理过期的导出文件
 *
 * @author system
 * @date 2026-01-30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExportTaskCleanupTask {

    private final IChallengeVersionExportTaskService exportTaskService;

    /**
     * 每小时执行一次，清理过期的导出文件
     * cron: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupExpiredFiles() {
        log.debug("开始执行导出任务过期清理任务...");

        try {
            int count = exportTaskService.cleanupExpiredFiles();
            if (count > 0) {
                log.info("导出任务过期清理任务完成，共清理 {} 个过期文件", count);
            }
        } catch (Exception e) {
            log.error("导出任务过期清理任务执行失败", e);
        }
    }
}
