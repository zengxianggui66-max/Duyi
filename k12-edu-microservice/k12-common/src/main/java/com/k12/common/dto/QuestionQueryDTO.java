package com.k12.common.dto;

import lombok.Data;

@Data
public class QuestionQueryDTO {
    private String keyword;
    private String gradeLevel;
    private String subject;
    private Integer difficulty;
    private String questionType;
    private String sourceType;
    private String region;
    private Integer current = 1;
    private Integer size = 10;
}
