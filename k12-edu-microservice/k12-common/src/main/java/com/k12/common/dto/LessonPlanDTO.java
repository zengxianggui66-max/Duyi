package com.k12.common.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
public class LessonPlanDTO {
    @NotBlank(message = "课题名称不能为空")
    private String topic;
    @NotBlank(message = "学段不能为空")
    private String gradeLevel;
    @NotBlank(message = "学科不能为空")
    private String subject;
    private String grade;
    private String version;
    private List<String> types;
    /** 资料篮资源标题（智能备课引用） */
    private List<String> basketResourceTitles;
    /** 资料篮试题 ID */
    private List<Long> basketQuestionIds;
}
