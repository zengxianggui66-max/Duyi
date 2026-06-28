package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("edu_resource_dimension")
public class EduResourceDimension {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("resource_id")
    private Long resourceId;

    @TableField("stage_id")
    private Integer stageId;

    @TableField("subject_id")
    private Integer subjectId;

    @TableField("edition_id")
    private Integer editionId;

    @TableField("grade_id")
    private Integer gradeId;

    @TableField("semester_id")
    private Integer semesterId;

    @TableField("volume_id")
    private Integer volumeId;

    @TableField("module_id")
    private Integer moduleId;

    @TableField("resource_type_id")
    private Integer resourceTypeId;

    @TableField("unit_id")
    private Integer unitId;

    @TableField("lesson_id")
    private Integer lessonId;
}
