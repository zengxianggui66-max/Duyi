package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 年级维度（edu_grade）
 */
@Data
@TableName("edu_grade")
public class EduGrade {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer stageId;

    private String code;

    private String name;

    private Integer sort;

    /** 0-停用 1-启用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
