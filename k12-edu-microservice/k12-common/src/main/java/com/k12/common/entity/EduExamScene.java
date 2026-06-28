package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 考试/备考场景（edu_exam_scene）
 */
@Data
@TableName("edu_exam_scene")
public class EduExamScene {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String code;

    private String name;

    private String examLevel;

    private Integer sort;

    /** 0-停用 1-启用 */
    private Integer status;
}
