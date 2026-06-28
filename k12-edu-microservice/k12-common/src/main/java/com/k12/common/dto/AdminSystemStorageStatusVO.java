package com.k12.common.dto;

import lombok.Data;

@Data
public class AdminSystemStorageStatusVO {

    private String provider;
    private Boolean configured;
    private Boolean reachable;
    private String bucket;
    private Integer latencyMs;
    private AdminSystemLocalFallbackVO localFallback;
}
