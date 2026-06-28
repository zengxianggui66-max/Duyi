package com.k12.common.dto;

import lombok.Data;

import java.util.List;

/**
 * Phase 8：质量治理大盘 VO
 */
@Data
public class QualityDashboardVO {

    /** 审核 SLA */
    private AuditSlaVO sla;

    /** 驳回原因统计 Top N */
    private List<RejectStatsVO> rejectStats;

    /** 审核员工作量 Top N */
    private List<AuditWorkloadVO> auditorWorkload;

    /** 资源增长趋势(最近30天) */
    private GrowthTrendVO growthTrend;

    /** 文件安全风险数 */
    private Integer fileSafetyRiskCount;

    /** 预览失败待处理数 */
    private Integer previewFailPendingCount;

    /** 低访问资源数 */
    private Integer lowAccessCount;

    /** 敏感词总数 */
    private Integer sensitiveWordCount;
}
