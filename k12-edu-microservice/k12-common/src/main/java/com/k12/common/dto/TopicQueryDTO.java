package com.k12.common.dto;

import lombok.Data;

@Data
public class TopicQueryDTO {
    private String category;
    private String region;
    private String gradeStage;
    private String subject;
    private String resourceForm;
    private String keyword;
    private Integer isFree;
    private Integer current = 1;
    private Integer size = 12;
    private String sortField = "sort";
    private String sortOrder = "desc";
}
