package com.k12.auth;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@ComponentScan(basePackages = {"com.k12.auth", "com.k12.common"})
@MapperScan({"com.k12.auth.mapper", "com.k12.auth.admin.mapper", "com.k12.common.mapper"})
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);

        System.out.println("========================================");
        System.out.println("  K12 认证服务启动成功！");
        System.out.println("  http://localhost:8081");
        System.out.println("========================================");
    }
}
