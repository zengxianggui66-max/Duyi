package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("question_bank")
public class QuestionBank {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String stem;
    private String questionType;
    private String optionsJson;
    private String answer;
    private String analysis;
    private String gradeLevel;
    private String subject;
    private Integer difficulty;
    private BigDecimal score;
    private String knowledgePoints;
    private String sourceType;
    private String sourceName;
    private String region;
    private Integer year;
    private Integer usageCount;
    private Integer status;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
