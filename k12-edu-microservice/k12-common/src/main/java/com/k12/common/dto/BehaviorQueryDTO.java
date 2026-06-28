package com.k12.common.dto;

import lombok.Data;

@Data
public class BehaviorQueryDTO {
    private Integer current = 1;
    private Integer size = 20;
    private String keyword;
    private String stageKey;
    private String subject;
    private String teachingType;
}
