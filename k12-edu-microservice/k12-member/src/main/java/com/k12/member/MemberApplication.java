package com.k12.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.k12.member", "com.k12.common"})
@MapperScan("com.k12.member.mapper")
public class MemberApplication {
    public static void main(String[] args) {
        SpringApplication.run(MemberApplication.class, args);
        System.out.println("========================================");
        System.out.println("  K12 会员服务启动成功！");
        System.out.println("  http://localhost:8086");
        System.out.println("========================================");
    }
}
