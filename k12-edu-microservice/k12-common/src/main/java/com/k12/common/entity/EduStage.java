package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学段维度（edu_stage）
 */
@Data
@TableName("edu_stage")
public class EduStage {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String code;

    private String name;

    private String icon;

    private Integer sort;

    /** 0-停用 1-启用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
