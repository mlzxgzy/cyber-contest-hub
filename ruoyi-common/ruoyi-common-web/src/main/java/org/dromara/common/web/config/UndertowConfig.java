package org.dromara.common.web.config;

import io.undertow.UndertowOptions;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.server.handlers.DisallowedMethodsHandler;
import io.undertow.util.HttpString;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.dromara.common.core.utils.SpringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.core.task.VirtualThreadTaskExecutor;

/**
 * Undertow 自定义配置
 *
 * @author Lion Li
 */
@AutoConfiguration
public class UndertowConfig implements WebServerFactoryCustomizer<UndertowServletWebServerFactory> {

    @Autowired
    private ServerProperties serverProperties;

    @Autowired
    private MultipartProperties multipartProperties;

    /**
     * 自定义 Undertow 配置
     * <p>
     * 主要配置内容包括：
     * 1. 配置 WebSocket 部署信息
     * 2. 在虚拟线程模式下使用虚拟线程池
     * 3. 禁用不安全的 HTTP 方法，如 CONNECT、TRACE、TRACK
     * </p>
     *
     * @param factory Undertow 的 Web 服务器工厂
     */
    @Override
    public void customize(UndertowServletWebServerFactory factory) {
        // Undertow 容器层的 multipart 请求体上限需与 spring.servlet.multipart.max-request-size 保持一致。
        // 不能使用 max-http-post-size（配置 -1 时为负值，会回落到 Undertow 默认 2MB），
        // 否则超过 2MB 的文件上传会被 Undertow 直接以 400 拒绝，请求体尚未传输完毕连接即被关闭。
        long multipartMaxBytes = multipartProperties.getMaxRequestSize().toBytes();
        factory.addBuilderCustomizers(builder -> {
            builder.setServerOption(UndertowOptions.MULTIPART_MAX_ENTITY_SIZE, multipartMaxBytes);
        });

        factory.addDeploymentInfoCustomizers(deploymentInfo -> {
            // 配置 WebSocket 部署信息，设置 WebSocket 使用的缓冲区池
            WebSocketDeploymentInfo webSocketDeploymentInfo = new WebSocketDeploymentInfo();
            webSocketDeploymentInfo.setBuffers(new DefaultByteBufferPool(true, 1024));
            deploymentInfo.addServletContextAttribute("io.undertow.websockets.jsr.WebSocketDeploymentInfo", webSocketDeploymentInfo);

            // 如果启用了虚拟线程，配置 Undertow 使用虚拟线程池
            if (SpringUtils.isVirtual()) {
                // 创建虚拟线程池，线程池前缀为 "undertow-"
                VirtualThreadTaskExecutor executor = new VirtualThreadTaskExecutor("undertow-");
                // 设置虚拟线程池为执行器和异步执行器
                deploymentInfo.setExecutor(executor);
                deploymentInfo.setAsyncExecutor(executor);
            }

            // 配置禁止某些不安全的 HTTP 方法（如 CONNECT、TRACE、TRACK）
            deploymentInfo.addInitialHandlerChainWrapper(handler -> {
                // 禁止三个方法 CONNECT/TRACE/TRACK 也是不安全的 避免爬虫骚扰
                HttpString[] disallowedHttpMethods = {
                    HttpString.tryFromString("CONNECT"),
                    HttpString.tryFromString("TRACE"),
                    HttpString.tryFromString("TRACK")
                };
                // 使用 DisallowedMethodsHandler 拦截并拒绝这些方法的请求
                return new DisallowedMethodsHandler(handler, disallowedHttpMethods);
            });
        });
    }

}
