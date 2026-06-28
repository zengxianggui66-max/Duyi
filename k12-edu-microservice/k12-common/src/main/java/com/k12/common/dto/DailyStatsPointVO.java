package com.k12.common.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * Phase 8：日统计趋势数据点
 */
@Data
public class DailyStatsPointVO {

    /** 日期 */
    private LocalDate statDate;

    /** 新增数 */
    private Integer newCount;

    /** 审核通过 */
    private Integer approvedCount;

    /** 驳回 */
    private Integer rejectedCount;

    /** 下架 */
    private Integer offlineCount;

    /** 下载 */
    private Integer downloadCount;

    /** 浏览 */
    private Integer viewCount;
}
