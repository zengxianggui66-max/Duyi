package com.k12.common.dto;

import lombok.Data;

@Data
public class AdminSearchIntentRuleVO {
    private Long id;
    private String pattern;
    private String intentType;
    private String targetKey;
    private String targetValue;
    private String targetPayload;
    private Integer priority;
    private Integer status;
    private String updateTime;
}
