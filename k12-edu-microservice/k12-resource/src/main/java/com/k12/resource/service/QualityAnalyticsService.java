package com.k12.resource.service;

import com.k12.common.dto.*;

import java.util.List;

/**
 * Phase 8：质量治理分析 Service
 */
public interface QualityAnalyticsService {

    /**
     * 质量大盘
     */
    QualityDashboardVO getQualityDashboard(int days);

    /**
     * 审核 SLA
     */
    AuditSlaVO getAuditSla(int days);

    /**
     * 审核员工作量（Top N）
     */
    List<AuditWorkloadVO> getAuditorWorkload(int days, int limit);

    /**
     * 驳回原因统计
     */
    List<RejectStatsVO> getRejectStats(int days);

    /**
     * 资源增长趋势（通过率/驳回率/下架率 + 日数据点）
     */
    GrowthTrendVO getGrowthTrend(int days);

    /**
     * 低访问资源分页查询
     */
    PageResult<LowAccessResourceVO> getLowAccessResources(
            int pageNum, int pageSize,
            String stage, String subject, String accessLevel);
}
