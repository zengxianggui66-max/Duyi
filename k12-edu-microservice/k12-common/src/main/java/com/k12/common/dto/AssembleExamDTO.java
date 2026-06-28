package com.k12.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssembleExamDTO {
    private String title;
    private String gradeLevel;
    private String subject;
    private Integer duration;
    private Integer difficulty;
    /** 按顺序排列的试题 ID；为空则从资料篮读取 */
    /** 为空时从资料篮读取试题 */
    private List<Long> questionIds;
}
