package com.k12.common.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LegacyApiUsageVO {
    private String apiPath;
    private LocalDate statDate;
    private Long hitCount;
    private LocalDateTime lastHitTime;
    private String sampleQuery;
}
