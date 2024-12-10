package com.kdajv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 启动程序
 *
 * @author ruoyi
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CyberContestHubApplication {
    public static void main(String[] args) {
        // System.setProperty("spring.devtools.restart.enabled", "false");
        SpringApplication.run(CyberContestHubApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  CCH启动成功   ლ(´ڡ`ლ)ﾞ  \n");
    }
}