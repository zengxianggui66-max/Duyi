package com.k12.resource.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 启动时初始化本地上传目录，避免首次上传因目录不存在失败
 */
@Slf4j
@Component
public class UploadStorageInitializer {

    @Value("${upload.path:${user.home}/k12-uploads}")
    private String uploadPath;

    @PostConstruct
    public void init() throws IOException {
        Path root = Paths.get(uploadPath).toAbsolutePath().normalize();
        Files.createDirectories(root);
        Files.createDirectories(root.resolve("preview-cache"));
        log.info("本地上传目录已就绪: {}", root);
    }
}
