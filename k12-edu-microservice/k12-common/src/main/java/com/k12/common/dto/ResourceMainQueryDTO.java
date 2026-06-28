package com.k12.common.dto;

import lombok.Data;

/**
 * Phase 7：统一资源查询参数（支持跨学科、跨学段筛选）
 */
@Data
public class ResourceMainQueryDTO {

    /** 源类型筛选：primary_chinese | junior_math 等；不传=全部 */
    private String sourceType;

    private String stage;
    private String subject;
    private String module;
    private String type;
    private String subType;
    private String gradeName;
    private String edition;
    private String brandCode;
    private String unitName;
    private String lessonName;
    private String fileExt;
    private String keyword;
    private Long catalogNodeId;
    private Boolean includeSubtree;

    /** 按 legacy_status 筛选；null 时默认排除回收站 */
    private Integer status;

    /** 按 audit_status 精确筛选 */
    private Integer auditStatus;

    /** 按 publish_status 精确筛选 */
    private Integer publishStatus;

    private Integer isRecommend;
    private Integer isTop;
    private Integer isFree;
    private Long uploaderId;

    /** 分页 */
    private Integer current = 1;
    private Integer size = 15;

    /** 排序 */
    private String sortField = "upload_time";
    private String sortOrder = "desc";
}
