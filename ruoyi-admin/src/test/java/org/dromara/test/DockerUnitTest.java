package org.dromara.test;

import cn.hutool.core.lang.Console;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Swarm;
import com.github.dockerjava.api.model.SwarmNode;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.kdajv.cch.container.DockerContainerClient;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Kami
 * @create 2026/1/29 14:47
 */
// @SpringBootTest // 此注解只能在 springboot 主包下使用 需包含 main 方法与 yml 配置文件
@DisplayName("单元测试案例")
public class DockerUnitTest {
    private String dockerUrl = "tcp://localhost:2375";
    private String apiVersion = "1.51";

    DockerClient getInstance() {
        DefaultDockerClientConfig.Builder configBuilder = DefaultDockerClientConfig.createDefaultConfigBuilder();
        configBuilder.withDockerHost(dockerUrl);
        configBuilder.withApiVersion(apiVersion);
        DefaultDockerClientConfig dockerConfig = configBuilder.build();
        ApacheDockerHttpClient apacheDockerHttpClient = new ApacheDockerHttpClient.Builder().dockerHost(dockerConfig.getDockerHost()).sslConfig(dockerConfig.getSSLConfig()).build();
        return DockerClientBuilder.getInstance(dockerConfig).withDockerHttpClient(apacheDockerHttpClient).build();
    }

    @Test
    public void testPing() throws Exception {
        DockerClient instance = getInstance();
        instance.pingCmd().exec();
    }

    @Test
    public void testInspectSwarmCmd() throws Exception {
        DockerClient instance = getInstance();
        Swarm exec = instance.inspectSwarmCmd().exec();
        Console.log(exec);
    }

    @Test
    public void testListSwarmNodesCmd() throws Exception {
        DockerClient instance = getInstance();
        List<SwarmNode> exec = instance.listSwarmNodesCmd().exec();
        Console.log(exec);
    }
}
