package com.k12.common.dto;

import lombok.Data;

/**
 * Phase 8：驳回原因统计 VO
 */
@Data
public class RejectStatsVO {

    /** 驳回原因分类: 0-通用 1-内容质量 2-格式规范 3-版权合规 4-分类挂载 5-其他 */
    private Integer category;

    /** 分类名称 */
    private String categoryName;

    /** 驳回原因原文 */
    private String reason;

    /** 驳回次数 */
    private Integer rejectCount;

    /** 占比(百分比，如 "35.2") */
    private String percentage;
}
