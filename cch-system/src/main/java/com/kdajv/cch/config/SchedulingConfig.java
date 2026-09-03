package com.kdajv.cch.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 开启 Spring 定时任务调度
 *
 * <p>工程内唯一的 @EnableScheduling 位于 SnailJobConfig，
 * 且受 @ConditionalOnProperty(snail-job.enabled=true) 控制；
 * dev 环境下 snail-job 关闭会导致所有 @Scheduled 任务
 * （如 ContainerMockTestCleanupTask 过期清理）静默失效，
 * 故在此独立开启调度，与 snail-job 解耦。</p>
 *
 * @author system
 * @date 2026-09-03
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
