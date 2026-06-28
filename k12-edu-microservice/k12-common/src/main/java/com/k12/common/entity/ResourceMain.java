package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Phase 7：统一资源主域映射表（resource_main）
 * 轻量级——只做各源表 → 全局 ID 的路由映射
 */
@Data
@TableName("resource_main")
public class ResourceMain {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 源表标识：primary_chinese | junior_math | junior_english */
    private String sourceType;

    /** 源表名 */
    private String sourceTable;

    /** 源表主键 */
    private Long sourceId;

    /** 冗余：标题 */
    private String title;

    /** 冗余：学段编码 */
    private String stageCode;

    /** 冗余：学科编码 */
    private String subjectCode;

    /** 审核状态：-1草稿 0待审 1通过 2驳回 3复审 */
    private Integer auditStatus;

    /** 发布状态：0未上架 1已上架 2已下架 3定时 4回收站 */
    private Integer publishStatus;

    /** 兼容 legacy status */
    private Integer legacyStatus;

    private Long uploaderId;

    private LocalDateTime uploadTime;

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
