package com.k12.common.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * MinIO 客户端包装类
 */
@Component
public class MinioClientWrapper {

    @Autowired
    private MinioProperties minioProperties;

    @Autowired
    private MinioClient minioClient;

    public String getBucketName() {
        return minioProperties.getBucketName();
    }

    public MinioClient getMinioClient() {
        return minioClient;
    }
}
