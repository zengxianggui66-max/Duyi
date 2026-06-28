package com.k12.lesson;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.k12.lesson", "com.k12.common"})
@MapperScan("com.k12.lesson.mapper")
public class LessonApplication {
    public static void main(String[] args) {
        SpringApplication.run(LessonApplication.class, args);
        System.out.println("========================================");
        System.out.println("  K12 备课服务启动成功！");
        System.out.println("  http://localhost:8084");
        System.out.println("========================================");
    }
}
