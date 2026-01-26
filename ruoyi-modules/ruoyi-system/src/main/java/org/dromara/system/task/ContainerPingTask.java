package org.dromara.system.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.system.service.impl.CchContainerConfigServiceImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 容器连接Ping任务
 * 每10秒ping一次当前活跃的容器实例
 *
 * @author system
 * @date 2025-12-11
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContainerPingTask implements CommandLineRunner {

    private final CchContainerConfigServiceImpl containerConfigService;

    /**
     * 每10秒执行一次ping
     */
    @Scheduled(fixedRate = 10000)
    public void pingActiveInstance() {
        try {
            boolean success = containerConfigService.pingActiveInstance();
            if (!success) {
                log.debug("当前没有活跃的容器实例或ping失败");
            }
        } catch (Exception e) {
            log.error("Ping容器实例时发生异常", e);
        }
    }

    /**
     * 应用启动时初始化活跃实例
     */
    @Override
    public void run(String... args) {
        log.info("初始化容器配置服务...");
        containerConfigService.initActiveInstance();
    }
}

