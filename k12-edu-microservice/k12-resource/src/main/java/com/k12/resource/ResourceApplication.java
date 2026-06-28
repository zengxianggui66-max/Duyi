package com.k12.resource;

import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@ComponentScan(basePackages = {"com.k12.resource", "com.k12.common"})
@MapperScan({"com.k12.resource.mapper", "com.k12.common.mapper"})
@EnableFeignClients(basePackages = {"com.k12.common.feign"})
@EnableAsync
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class ResourceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResourceApplication.class, args);
        System.out.println("========================================");
        System.out.println("  K12 资源服务启动成功！");
        System.out.println("  http://localhost:8082");
        System.out.println("========================================");
    }
}
