package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 教学场景标签（edu_teaching_scene）
 */
@Data
@TableName("edu_teaching_scene")
public class EduTeachingScene {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String code;

    private String name;

    private Integer sort;

    /** 0-停用 1-启用 */
    private Integer status;
}
