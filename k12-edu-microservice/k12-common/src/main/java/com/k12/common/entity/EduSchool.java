package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学校信息实体
 * 对应数据库表：xinketang.edu_school
 */
@Data
@TableName("xinketang.edu_school")
public class EduSchool {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 学校全称 */
    private String name;

    /** 简称 */
    @TableField("short_name")
    private String shortName;

    /** 关联 edu_region.id */
    @TableField("region_id")
    private Integer regionId;

    /** 地区路径：四川省/成都市/武侯区 */
    @TableField("region_path")
    private String regionPath;

    /** 学校类型：public/private/key/foreign_lang */
    @TableField("school_type")
    private String schoolType;

    /** 学段：primary/junior/senior/k12 */
    @TableField("school_level")
    private String schoolLevel;

    /** 标签 */
    private String tags;

    /** 地址 */
    private String address;

    /** 联系方式 */
    private String contact;

    private Integer status;

    private Integer sort;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ===== 非数据库字段 =====

    /** 地区名称（JOIN填充） */
    @TableField(exist = false)
    private String regionName;
}
