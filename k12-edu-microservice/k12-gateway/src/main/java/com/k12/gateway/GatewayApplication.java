package com.k12.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {
        "com.k12.gateway",
        // 仅引入 JWT 相关 Bean，避免扫描 Servlet/MinIO 等 Web 栈组件
        "com.k12.common.util",
        "com.k12.common.auth"
})
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
        System.out.println("========================================");
        System.out.println("  K12 API 网关启动成功！");
        System.out.println("  http://localhost:9000");
        System.out.println("========================================");
    }
}
