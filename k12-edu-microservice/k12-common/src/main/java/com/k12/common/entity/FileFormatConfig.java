package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文件格式配置实体
 */
@Data
@TableName("file_format_config")
public class FileFormatConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String extension;        // 文件扩展名
    private String formatName;        // 格式名称
    private String category;         // 分类: doc/video/audio/image
    private String mimeType;          // MIME类型
    private Integer maxSizeMb;       // 最大文件大小(MB)
    private Boolean isPreviewable;    // 是否支持预览
    private String previewType;      // 预览类型
    private String icon;              // 图标
    private Integer status;           // 状态: 0-禁用 1-启用
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
