package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教材版本（edu_edition）
 */
@Data
@TableName("edu_edition")
public class EduEdition {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String code;

    private String name;

    @TableField("short_name")
    private String shortName;

    /** 绑定学段ID（NULL=通用） */
    @TableField("stage_id")
    private Integer stageId;

    /** 绑定学科ID（NULL=通用） */
    @TableField("subject_id")
    private Integer subjectId;

    private String publisher;

    @TableField("year_label")
    private String yearLabel;

    private Integer sort;

    /** 0-停用 1-启用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
