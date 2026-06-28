package com.k12.common.config;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 工具类
 */
@Component
public class MinioUtil {

    @Autowired
    private MinioProperties minioProperties;

    @Autowired
    private MinioClientWrapper minioClientWrapper;

    private MinioClient getClient() {
        return minioClientWrapper.getMinioClient();
    }

    public String getBucketName() {
        return minioClientWrapper.getBucketName();
    }

    /**
     * 上传文件到 MinIO
     * @param inputStream 文件输入流
     * @param objectName  对象名称（存储路径+文件名）
     * @param contentType 文件类型
     * @return 文件访问URL
     */
    public String uploadFile(InputStream inputStream, String objectName, String contentType) {
        try {
            // 确保bucket存在
            boolean bucketExists = getClient().bucketExists(BucketExistsArgs.builder()
                    .bucket(minioProperties.getBucketName()).build());
            if (!bucketExists) {
                getClient().makeBucket(MakeBucketArgs.builder()
                        .bucket(minioProperties.getBucketName()).build());
            }

            // 上传文件
            getClient().putObject(PutObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .stream(inputStream, inputStream.available(), -1)
                    .contentType(contentType)
                    .build());

            return getPublicUrl(objectName);
        } catch (Exception e) {
            throw new RuntimeException("上传文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件访问URL（永久有效，需bucket设置为公开）
     */
    public String getPublicUrl(String objectName) {
        return minioProperties.getEndpoint() + "/" + minioProperties.getBucketName() + "/" + objectName;
    }

    /**
     * 获取文件访问URL（带过期时间）
     */
    public String getFileUrl(String objectName) {
        try {
            return getClient().getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .expiry(7, TimeUnit.DAYS)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("获取文件URL失败: " + e.getMessage(), e);
        }
    }

    /**
     * 下载文件
     */
    public InputStream getFile(String objectName) {
        try {
            return getClient().getObject(GetObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("下载文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除文件
     */
    public void deleteFile(String objectName) {
        try {
            getClient().removeObject(RemoveObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("删除文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String objectName) {
        try {
            getClient().statObject(StatObjectArgs.builder()
                    .bucket(minioProperties.getBucketName())
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
