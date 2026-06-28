package com.k12.common.dto;

import lombok.Data;

/**
 * 字典查询DTO - 支持按学段筛选
 */
@Data
public class DictQueryDTO {
    private String type;        // 字典类型: grade_level, subject, version, resource_type, exam_type
    private String gradeLevel;  // 学段: primary, junior, senior, art, dance
}
