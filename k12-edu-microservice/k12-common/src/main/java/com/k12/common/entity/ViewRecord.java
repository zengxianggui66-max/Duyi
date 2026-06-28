package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("view_record")
public class ViewRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long resourceId;
    private String resourceType;
    private String title;
    private String subject;
    private String stageKey;
    private String stage;
    private String gradeName;
    private String teachingType;
    private String fileExt;
    private String ossUrl;
    private String detailUrl;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
