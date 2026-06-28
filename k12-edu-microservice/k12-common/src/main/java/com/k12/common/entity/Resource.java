package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 资源实体
 */
@Data
@TableName("resource")
public class Resource {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private String coverUrl;
    private String fileUrl;
    private Long fileSize;
    private String fileFormat;
    private String gradeLevel;
    private String subject;
    private String grade;
    private String version;
    private String resourceType;
    private String examType;
    private Long categoryId;
    private String tags;
    private Integer downloadCount;
    private Integer viewCount;
    private Integer collectCount;
    private java.math.BigDecimal score;
    private java.math.BigDecimal rating;  // 资源评分（0-5分）
    private Integer ratingCount;  // 评分次数
    private Integer isFree;
    private Long authorId;
    private String authorName;
    private Integer status;
    
    // 文件上传相关字段
    private String storagePath;      // 存储路径
    private String originalName;      // 原始文件名
    private String contentType;      // MIME类型
    private Integer isPreviewable;   // 是否可预览（0-否，1-是）
    private String uploadStatus;     // 上传状态（pending/success/failed）
    private String storageType;      // 存储类型（local/oss/cos）
    private String thumbnailUrl;     // 缩略图URL
    private Integer duration;        // 音视频时长（秒）
    private Integer pageCount;       // 文档页数
    
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
