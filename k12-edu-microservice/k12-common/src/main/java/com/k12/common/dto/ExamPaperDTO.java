package com.k12.common.dto;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Data
public class ExamPaperDTO {
    @NotBlank(message = "学段不能为空")
    private String gradeLevel;
    @NotBlank(message = "学科不能为空")
    private String subject;
    @Min(value = 1, message = "难度最低为1")
    @Max(value = 5, message = "难度最高为5")
    private Integer difficulty = 3;
    private Integer totalScore = 100;
    private Integer duration = 90;
    private String questionConfig;
}
