package com.k12.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamPaperSummaryVO {
    private Long id;
    private String title;
    private String gradeLevel;
    private String subject;
    private Integer totalScore;
    private Integer duration;
    private Integer questionCount;
    private LocalDateTime createTime;
}
