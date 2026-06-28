package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 字典/筛选维度实体
 */
@Data
@TableName("dict")
public class Dict {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String type;      // 类型: grade_level, subject, version, resource_type, exam_type, grade
    
    private String label;     // 显示标签（前端展示用）
    
    private String value;     // 存储值（与前端 code 对应）
    
    private String name;      // 名称（兼容旧字段）
    
    private String code;      // 编码（兼容前端 key）
    
    private String icon;      // 图标emoji
    
    private String groupKey;  // 分组键（用于前端分组展示）
    
    private String gradeLevels; // 适用的学段（逗号分隔: primary,junior,senior,art,dance）
    
    private String shortName;  // 简称
    
    private Integer sort;     // 排序
    
    private Integer status;
    
    private String description; // 描述
    
    @TableLogic
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
