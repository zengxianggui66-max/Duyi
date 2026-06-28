package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 试卷实体
 */
@Data
@TableName("exam_paper")
public class ExamPaper {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String gradeLevel;
    private String subject;
    private Integer difficulty;
    private Integer totalScore;
    private Integer duration;
    private String questionConfig;
    private String questions;
    private String answerUrl;
    private Integer downloadCount;
    private Integer status;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
