package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 小学语文学科资源OSS存储表
 * 对应数据库表：xinketang.oss_primary_chinese_resource
 */
@Data
@TableName("xinketang.oss_primary_chinese_resource")
public class PrimaryChineseResource {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 学段：小学/初中/高中
     */
    private String stage;

    /**
     * 学科：语文/数学/英语等
     */
    private String subject;

    /**
     * 资源栏目：同步备课/一轮复习/专题/月考/期中/期末/真题/模拟等
     */
    private String module;

    /**
     * 类型：教案/课件/练习/试卷/学案等
     */
    private String type;

    /**
     * 年级：一年级下册
     */
    @TableField("grade_name")
    private String gradeName;

    /**
     * 版本：人教版/统编版等
     */
    private String edition;

    /**
     * 品牌编码：qicai | zhuangyuan | platform
     */
    @TableField("brand_code")
    private String brandCode;

    /**
     * 目录节点 ID（M2+ 挂载）
     */
    @TableField("catalog_node_id")
    private Long catalogNodeId;

    /**
     * 目录路径（M2+ 挂载）
     */
    @TableField("catalog_path")
    private String catalogPath;

    /**
     * 子类型：教案版/精品版/希沃等
     */
    @TableField("sub_type")
    private String subType;

    /**
     * 单元/章节名称
     */
    @TableField("unit_name")
    private String unitName;

    /**
     * 课文名称（目录叶子节点，如「春夏秋冬」）
     */
    @TableField("lesson_name")
    private String lessonName;

    /**
     * 资源标题
     */
    private String title;

    /**
     * 原始文件名
     */
    @TableField("original_filename")
    private String originalFilename;

    /**
     * 文件扩展名：ppt/pptx/doc/pdf/rar/mp4等
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
     * 状态：0=待审核 1=已发布 2=审核不通过 3=下架
     */
    private Integer status;

    /** 审核状态：-1=草稿 0=待审核 1=审核通过 2=已驳回 3=复审中 */
    @TableField("audit_status")
    private Integer auditStatus;

    /** 发布状态：0=未上架 1=已上架 2=已下架 3=定时上架 4=回收站 */
    @TableField("publish_status")
    private Integer publishStatus;

    /**
     * 上传者ID
     */
    @TableField("uploader_id")
    private Long uploaderId;

    /**
     * 上传时间
     */
    @TableField("upload_time")
    private LocalDateTime uploadTime;

    /**
     * 更新时间（自动更新）
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 是否删除：0=正常 1=已删除
     */
    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;

    /**
     * 排序权重
     */
    private Integer sort;

    /**
     * 备注（简介，API 对外字段名为 description）
     */
    private String remark;

    /**
     * 是否允许预览：0=否 1=是
     */
    @TableField("allow_preview")
    private Integer allowPreview;

    /** 预览状态：0不可预览 1可预览 2待检测 3失败 */
    @TableField("preview_status")
    private Integer previewStatus;

    /** 文件安全：0未知 1待检测 2安全 3风险 */
    @TableField("file_safety_status")
    private Integer fileSafetyStatus;

    /**
     * 结构化教案 JSON（可选）
     */
    @TableField("lesson_plan_json")
    private String lessonPlanJson;

    /** 是否推荐 */
    @TableField("is_recommend")
    private Integer isRecommend;

    /** 是否置顶 */
    @TableField("is_top")
    private Integer isTop;

    /** 置顶排序 */
    @TableField("top_sort")
    private Integer topSort;

    /** 是否免费：1 免费 0 付费 */
    @TableField("is_free")
    private Integer isFree;
}

