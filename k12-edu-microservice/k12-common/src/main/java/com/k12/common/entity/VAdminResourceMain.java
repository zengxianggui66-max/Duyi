package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Phase 7：统一资源视图实体（映射 v_admin_resource_main）
 * 该视图 JOIN resource_main + 各源表，提供管理端统一查询
 */
@Data
@TableName("v_admin_resource_main")
public class VAdminResourceMain {

    @TableId
    private Long globalId;

    /** 源类型：primary_chinese | junior_math 等 */
    private String sourceType;

    private String sourceTable;

    private Long sourceId;

    private String title;

    // ---- 分类维度 ----
    private String stage;
    private String subject;
    private String module;
    private String type;
    private String gradeName;
    private String edition;
    private String brandCode;
    private String subType;
    private String unitName;
    private String lessonName;

    // ---- 目录挂载 ----
    private Long catalogNodeId;
    private String catalogPath;

    // ---- 文件信息 ----
    private String originalFilename;
    private String fileExt;
    private String ossBucket;
    private String ossObjectKey;
    private String ossUrl;
    private Integer fileSizeKb;

    // ---- 状态三件套 ----
    /** legacy 状态（兼容旧前台） */
    private Integer legacyStatus;
    /** 审核状态 */
    private Integer auditStatus;
    /** 上架/发布状态 */
    private Integer publishStatus;

    // ---- 上传者 ----
    private Long uploaderId;
    private LocalDateTime uploadTime;
    private LocalDateTime updateTime;

    /** 视图已过滤 is_deleted=0，无需 @TableLogic */
    private Integer isDeleted;

    // ---- 运营 ----
    private Integer sort;
    private String remark;
    private Integer isRecommend;
    private Integer isTop;
    private Integer topSort;
    private Integer isFree;
    private Integer downloadCount;
    private Integer viewCount;

    // ---- 安全/预览 ----
    private Integer allowPreview;
    private Integer previewStatus;
    private Integer fileSafetyStatus;

    @TableField("lesson_plan_json")
    private String lessonPlanJson;
}
