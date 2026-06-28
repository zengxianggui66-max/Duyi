package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 教育资源实体类
 * 对应数据库表：xinketang.edu_resource
 * 通过 edu_resource_dimension 关联多维度（学段/学科/年级/版本/学期/册别/单元/栏目/资源类型）
 */
@Data
@TableName("xinketang.edu_resource")
public class EduResource {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 资源标题
     */
    private String title;

    /**
     * 资源描述
     */
    private String description;

    /**
     * 原始文件名
     */
    @TableField("original_filename")
    private String originalFilename;

    /**
     * 文件扩展名：ppt/pptx/doc/docx/pdf/rar/zip/mp4 等
     */
    @TableField("file_ext")
    private String fileExt;

    /**
     * OSS存储桶
     */
    @TableField("oss_bucket")
    private String ossBucket;

    /**
     * OSS文件唯一路径
     */
    @TableField("oss_object_key")
    private String ossObjectKey;

    /**
     * OSS访问URL
     */
    @TableField("oss_url")
    private String ossUrl;

    /**
     * 文件大小（KB）
     */
    @TableField("file_size_kb")
    private Integer fileSizeKb;

    /**
     * 下载次数
     */
    @TableField("download_count")
    private Integer downloadCount;

    /**
     * 浏览次数
     */
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 收藏次数
     */
    @TableField("collect_count")
    private Integer collectCount;

    /**
     * 状态：0=待审核 1=已发布 2=审核不通过 3=下架
     */
    private Integer status;

    /**
     * 上传者ID
     */
    @TableField("uploader_id")
    private Long uploaderId;

    /**
     * 审核者ID
     */
    @TableField("auditor_id")
    private Long auditorId;

    /**
     * 审核时间
     */
    @TableField("audit_time")
    private LocalDateTime auditTime;

    /**
     * 审核备注
     */
    @TableField("audit_remark")
    private String auditRemark;

    /**
     * 上传时间
     */
    @TableField("upload_time")
    private LocalDateTime uploadTime;

    /**
     * 更新时间（自动更新）
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除：0=正常 1=已删除
     */
    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;

    /**
     * 排序权重（越大越靠前）
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

    // ===== 以下为关联维度字段（非数据库字段，通过JOIN或视图查询时填充）=====

    /**
     * 学段名称（来自 edu_stage）
     */
    @TableField(exist = false)
    private String stageName;

    /**
     * 学科名称（来自 edu_subject）
     */
    @TableField(exist = false)
    private String subjectName;

    /**
     * 版本名称（来自 edu_edition）
     */
    @TableField(exist = false)
    private String editionName;

    /**
     * 年级名称（来自 edu_grade）
     */
    @TableField(exist = false)
    private String gradeName;

    /**
     * 学期名称（来自 edu_semester）
     */
    @TableField(exist = false)
    private String semesterName;

    /**
     * 册别名称（来自 edu_volume）
     */
    @TableField(exist = false)
    private String volumeName;

    /**
     * 栏目名称（来自 edu_module）
     */
    @TableField(exist = false)
    private String moduleName;

    /**
     * 资源类型名称（来自 edu_resource_type）
     */
    @TableField(exist = false)
    private String resourceTypeName;

    /**
     * 单元名称（来自 edu_unit）
     */
    @TableField(exist = false)
    private String unitName;
}
