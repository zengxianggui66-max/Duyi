package com.k12.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Phase 8：低访问资源视图投影 VO
 */
@Data
public class LowAccessResourceVO {

    /** resource_main.id */
    private Long globalId;

    private String sourceType;

    private Long sourceId;

    /** 资源标题 */
    private String title;

    /** 学段 */
    private String stage;

    /** 学科 */
    private String subject;

    private Long uploaderId;

    private LocalDateTime uploadTime;

    private Integer downloadCount;

    private Integer viewCount;

    /** 发布状态 */
    private Integer publishStatus;

    /** 审核状态 */
    private Integer auditStatus;

    /** 已上架天数 */
    private Integer daysSinceUpload;

    /** 访问级别: 零访问 / 低浏览 / 低下载 / 正常 */
    private String accessLevel;
}
