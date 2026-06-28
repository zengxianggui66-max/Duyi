package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("edu_resource_file")
public class EduResourceFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("resource_id")
    private Long resourceId;

    /** main | answer | seewo | video | audio | attachment */
    @TableField("file_role")
    private String fileRole;

    @TableField("original_filename")
    private String originalFilename;

    @TableField("file_ext")
    private String fileExt;

    @TableField("mime_type")
    private String mimeType;

    @TableField("file_size_bytes")
    private Long fileSizeBytes;

    @TableField("oss_bucket")
    private String ossBucket;

    @TableField("oss_object_key")
    private String ossObjectKey;

    @TableField("oss_url")
    private String ossUrl;

    @TableField("allow_preview")
    private Integer allowPreview;

    private Integer sort;

    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;
}
