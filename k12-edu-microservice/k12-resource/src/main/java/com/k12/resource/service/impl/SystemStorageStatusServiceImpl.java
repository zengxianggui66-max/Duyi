package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.config.MinioClientWrapper;
import com.k12.common.config.MinioProperties;
import com.k12.common.dto.AdminSystemLocalFallbackVO;
import com.k12.common.dto.AdminSystemStorageStatusVO;
import com.k12.common.entity.SysConfig;
import com.k12.common.mapper.SysConfigMapper;
import com.k12.resource.service.SystemStorageStatusService;
import io.minio.BucketExistsArgs;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@SuppressWarnings("null")
public class SystemStorageStatusServiceImpl implements SystemStorageStatusService {

    private final MinioProperties minioProperties;
    private final MinioClientWrapper minioClientWrapper;
    private final SysConfigMapper sysConfigMapper;
    public SystemStorageStatusServiceImpl(MinioProperties minioProperties, MinioClientWrapper minioClientWrapper, SysConfigMapper sysConfigMapper) {
        this.minioProperties = minioProperties;
        this.minioClientWrapper = minioClientWrapper;
        this.sysConfigMapper = sysConfigMapper;
    }


    @Value("${upload.path:${user.home}/k12-uploads}")
    private String uploadPath;

    @Override
    public AdminSystemStorageStatusVO probe() {
        AdminSystemStorageStatusVO vo = new AdminSystemStorageStatusVO();
        String provider = configValue("storage.provider", "minio");
        vo.setProvider(provider);

        AdminSystemLocalFallbackVO local = new AdminSystemLocalFallbackVO();
        Path localPath = resolveLocalPath();
        local.setPath(displayPath(localPath));
        local.setWritable(isWritable(localPath));
        vo.setLocalFallback(local);

        if ("oss".equalsIgnoreCase(provider)) {
            vo.setConfigured(StringUtils.hasText(configValue("storage.oss.endpoint", "")));
            vo.setReachable(false);
            vo.setBucket(configValue("storage.oss.bucketName", ""));
            vo.setLatencyMs(0);
            return vo;
        }

        String endpoint = configValue("storage.minio.endpoint", minioProperties.getEndpoint());
        String bucket = configValue("storage.minio.bucketName", minioProperties.getBucketName());
        vo.setBucket(bucket);
        vo.setConfigured(StringUtils.hasText(endpoint) && StringUtils.hasText(bucket));

        if (!Boolean.TRUE.equals(vo.getConfigured())) {
            vo.setReachable(false);
            vo.setLatencyMs(0);
            return vo;
        }

        long start = System.currentTimeMillis();
        boolean reachable = false;
        try {
            MinioClient client = minioClientWrapper.getMinioClient();
            boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (exists) {
                client.listObjects(ListObjectsArgs.builder().bucket(bucket).maxKeys(1).build())
                        .iterator()
                        .hasNext();
            }
            reachable = exists;
        } catch (Exception ignored) {
            reachable = false;
        }
        vo.setReachable(reachable);
        vo.setLatencyMs((int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - start));
        return vo;
    }

    private String configValue(String key, String fallback) {
        SysConfig row = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key)
                .last("LIMIT 1"));
        if (row != null && StringUtils.hasText(row.getConfigValue())) {
            return row.getConfigValue().trim();
        }
        return fallback;
    }

    private Path resolveLocalPath() {
        String raw = uploadPath;
        if (!StringUtils.hasText(raw)) {
            raw = System.getProperty("user.home") + File.separator + "k12-uploads";
        }
        if (raw.contains("${user.home}")) {
            raw = raw.replace("${user.home}", System.getProperty("user.home"));
        }
        return Path.of(raw).toAbsolutePath().normalize();
    }

    private String displayPath(Path path) {
        String home = System.getProperty("user.home");
        String absolute = path.toString();
        if (home != null && absolute.startsWith(home)) {
            return "~" + absolute.substring(home.length()).replace('\\', '/');
        }
        return absolute.replace('\\', '/');
    }

    private boolean isWritable(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            return Files.isDirectory(path) && Files.isWritable(path);
        } catch (Exception ex) {
            return false;
        }
    }
}
