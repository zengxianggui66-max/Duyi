package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("download_record")
public class DownloadRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long resourceId;
    private String resourceType;
    private String resourceTitle;
    private String fileExt;
    private Long fileSize;
    private String subject;
    private String gradeName;
    private String teachingType;
    private String stageKey;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
