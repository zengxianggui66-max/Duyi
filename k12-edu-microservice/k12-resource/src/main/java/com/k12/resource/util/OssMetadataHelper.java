package com.k12.resource.util;

import com.k12.common.entity.PrimaryChineseResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;

/**
 * 根据文件 URL 填充 OSS 元数据（本地存储与 MinIO 统一字段）
 */
@Component
public class OssMetadataHelper {

    @Value("${upload.base-url:http://localhost:8082/uploads}")
    private String uploadBaseUrl;

    @Value("${minio.bucketName:k12-resources}")
    private String minioBucket;

    public void fillOssFields(PrimaryChineseResource resource) {
        if (resource == null) {
            return;
        }
        String url = resource.getOssUrl();
        if (!StringUtils.hasText(url)) {
            return;
        }
        String normalizedBase = uploadBaseUrl.endsWith("/")
                ? uploadBaseUrl.substring(0, uploadBaseUrl.length() - 1)
                : uploadBaseUrl;

        if (url.startsWith(normalizedBase)) {
            resource.setOssBucket("local");
            String key = url.substring(normalizedBase.length());
            if (key.startsWith("/")) {
                key = key.substring(1);
            }
            resource.setOssObjectKey(key);
            return;
        }

        if (url.startsWith("http://") || url.startsWith("https://")) {
            try {
                URI uri = URI.create(url);
                String path = uri.getPath();
                if (path != null && path.startsWith("/")) {
                    path = path.substring(1);
                }
                resource.setOssBucket(minioBucket);
                resource.setOssObjectKey(path);
            } catch (Exception ignored) {
                resource.setOssBucket(minioBucket);
                resource.setOssObjectKey(url);
            }
        } else {
            resource.setOssBucket("local");
            resource.setOssObjectKey(url);
        }
    }
}
