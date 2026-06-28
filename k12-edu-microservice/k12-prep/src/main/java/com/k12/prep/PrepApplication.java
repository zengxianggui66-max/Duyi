package com.k12.prep;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.k12.prep", "com.k12.common"})
@MapperScan("com.k12.prep.mapper")
public class PrepApplication {
    public static void main(String[] args) {
        SpringApplication.run(PrepApplication.class, args);
        System.out.println("========================================");
        System.out.println("  K12 备课中心服务启动成功！");
        System.out.println("  http://localhost:8087");
        System.out.println("========================================");
    }
}
