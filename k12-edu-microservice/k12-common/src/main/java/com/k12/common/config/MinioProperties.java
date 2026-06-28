package com.k12.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 配置属性类
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {
    /**
     * MinIO 服务地址
     */
    private String endpoint = "http://localhost:9000";
    
    /**
     * 访问密钥
     */
    private String accessKey = "admin";
    
    /**
     * 秘密密钥
     */
    private String secretKey = "admin123";
    
    /**
     * 存储桶名称
     */
    private String bucketName = "k12-resources";
}
