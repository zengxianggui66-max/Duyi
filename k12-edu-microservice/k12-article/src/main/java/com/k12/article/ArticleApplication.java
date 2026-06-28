package com.k12.article;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = {"com.k12.article", "com.k12.common"})
@MapperScan("com.k12.article.mapper")
public class ArticleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArticleApplication.class, args);
        System.out.println("========================================");
        System.out.println("  K12 资讯服务启动成功！");
        System.out.println("  http://localhost:8083");
        System.out.println("========================================");
    }
}
