package com.k12.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class SmartExamDTO {
    private String title;
    private String gradeLevel;
    private String subject;
    private Integer difficulty;
    private Integer duration;
    /** 是否优先并入资料篮试题 */
    private Boolean useBasketQuestions;
    private List<QuestionTypeCount> typeCounts;

    @Data
    public static class QuestionTypeCount {
        private String questionType;
        private Integer count;
    }
}
