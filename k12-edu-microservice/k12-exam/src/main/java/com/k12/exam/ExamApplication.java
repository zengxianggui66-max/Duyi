package com.k12.exam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.k12.exam", "com.k12.common"})
@MapperScan("com.k12.exam.mapper")
public class ExamApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExamApplication.class, args);
        System.out.println("========================================");
        System.out.println("  K12 组卷服务启动成功！");
        System.out.println("  http://localhost:8085");
        System.out.println("========================================");
    }
}
