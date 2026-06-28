package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 备课方案实体
 */
@Data
@TableName("lesson_plan")
public class LessonPlan {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String topic;
    private String gradeLevel;
    private String subject;
    private String grade;
    private String version;
    private String types;
    private String coursewareUrl;
    private String lessonPlanUrl;
    private String studyGuideUrl;
    private String exercisesUrl;
    private Integer status;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
