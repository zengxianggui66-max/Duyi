package com.k12.common.dto;

import lombok.Data;

import java.util.List;

/**
 * Phase 8：资源增长趋势 VO
 */
@Data
public class GrowthTrendVO {

    /** 资源总数 */
    private Integer totalCount;

    /** 已上架数 */
    private Integer publishedCount;

    /** 近30天日均新增 */
    private Double avgDailyNew;

    /** 近30天日均审核通过 */
    private Double avgDailyApproved;

    /** 近30天日均驳回 */
    private Double avgDailyRejected;

    /** 通过率 (通过数/审核总数) */
    private String approvalRate;

    /** 驳回率 (驳回数/审核总数) */
    private String rejectionRate;

    /** 下架率 (下架数/上架总数) */
    private String offlineRate;

    /** 日趋势数据点 */
    private List<DailyStatsPointVO> dailyPoints;
}
