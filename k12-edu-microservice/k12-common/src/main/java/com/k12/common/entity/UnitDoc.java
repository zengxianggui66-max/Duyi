package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 单元知识文档OSS存储表
 * 对应数据库表：oss_unit_doc
 */
@Data
@TableName("oss_unit_doc")
public class UnitDoc {
    
    @TableId(type = IdType.AUTO)
    private Integer id;           // 主键ID
    
    private String unitName;      // 单元名称
    
    @TableField("original_filename")
    private String originalFilename;  // 原始文件名
    
    @TableField("oss_bucket")
    private String ossBucket;     // OSS Bucket名称
    
    @TableField("oss_object_key")
    private String ossObjectKey;   // OSS文件路径
    
    @TableField("oss_url")
    private String ossUrl;         // OSS访问URL
    
    @TableField("file_size_kb")
    private Integer fileSizeKb;    // 文件大小（KB）
    
    @TableField("upload_time")
    private LocalDateTime uploadTime;  // 上传时间
    
    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;     // 是否删除：0=正常，1=删除
    
    /**
     * 获取文件大小（MB）
     */
    public Double getFileSizeMb() {
        if (fileSizeKb == null || fileSizeKb == 0) {
            return 0.0;
        }
        return Math.round(fileSizeKb / 1024.0 * 100.0) / 100.0;
    }
    
    /**
     * 获取文件类型（根据文件名后缀）
     */
    public String getFileType() {
        if (originalFilename == null) {
            return "unknown";
        }
        String filename = originalFilename.toLowerCase();
        if (filename.endsWith(".doc")) return "doc";
        if (filename.endsWith(".docx")) return "docx";
        if (filename.endsWith(".pdf")) return "pdf";
        if (filename.endsWith(".ppt")) return "ppt";
        if (filename.endsWith(".pptx")) return "pptx";
        if (filename.endsWith(".xls")) return "xls";
        if (filename.endsWith(".xlsx")) return "xlsx";
        return "other";
    }
}
