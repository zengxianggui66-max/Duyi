package com.k12.resource.service.impl;

import com.k12.common.dto.*;
import com.k12.resource.mapper.QualityAnalyticsMapper;
import com.k12.resource.service.QualityAnalyticsService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Phase 8：质量治理分析 Service 实现
 */
@Service
@SuppressWarnings("null")
public class QualityAnalyticsServiceImpl implements QualityAnalyticsService {

    private static final int DEFAULT_DAYS = 30;

    private final QualityAnalyticsMapper qualityAnalyticsMapper;
    public QualityAnalyticsServiceImpl(QualityAnalyticsMapper qualityAnalyticsMapper) {
        this.qualityAnalyticsMapper = qualityAnalyticsMapper;
    }


    @Override
    public QualityDashboardVO getQualityDashboard(int days) {
        int safeDays = clampDays(days);
        LocalDate since = LocalDate.now().minusDays(safeDays);

        // 超时边界
        LocalDate now = LocalDate.now();
        LocalDate overtime24h = now.minusDays(1);
        LocalDate overtime48h = now.minusDays(2);
        LocalDate overtime72h = now.minusDays(3);

        QualityDashboardVO vo = new QualityDashboardVO();

        // SLA
        AuditSlaVO sla = qualityAnalyticsMapper.selectAuditSla(
                since, overtime24h, overtime48h, overtime72h);
        if (sla != null && sla.getAvgAuditDurationSec() != null) {
            long secs = sla.getAvgAuditDurationSec();
            sla.setAvgAuditDurationFormatted(formatSeconds(secs));
        }
        vo.setSla(sla);

        // 驳回原因 Top 10
        vo.setRejectStats(qualityAnalyticsMapper.selectRejectStats(since));

        // 审核员工作量 Top 10
        vo.setAuditorWorkload(qualityAnalyticsMapper.selectAuditorWorkload(since, 10));

        // 增长趋势
        vo.setGrowthTrend(getGrowthTrend(safeDays));

        // 风险计数
        vo.setFileSafetyRiskCount(qualityAnalyticsMapper.countFileSafetyRisk());
        vo.setPreviewFailPendingCount(qualityAnalyticsMapper.countPreviewFailPending());
        vo.setLowAccessCount(
                qualityAnalyticsMapper.countLowAccessResources(null, null, null));
        vo.setSensitiveWordCount(qualityAnalyticsMapper.countSensitiveWords());

        return vo;
    }

    @Override
    public AuditSlaVO getAuditSla(int days) {
        int safeDays = clampDays(days);
        LocalDate since = LocalDate.now().minusDays(safeDays);
        LocalDate now = LocalDate.now();
        AuditSlaVO sla = qualityAnalyticsMapper.selectAuditSla(
                since, now.minusDays(1), now.minusDays(2), now.minusDays(3));
        if (sla != null && sla.getAvgAuditDurationSec() != null) {
            sla.setAvgAuditDurationFormatted(formatSeconds(sla.getAvgAuditDurationSec()));
        }
        return sla;
    }

    @Override
    public List<AuditWorkloadVO> getAuditorWorkload(int days, int limit) {
        int safeDays = clampDays(days);
        LocalDate since = LocalDate.now().minusDays(safeDays);
        return qualityAnalyticsMapper.selectAuditorWorkload(since, limit);
    }

    @Override
    public List<RejectStatsVO> getRejectStats(int days) {
        int safeDays = clampDays(days);
        LocalDate since = LocalDate.now().minusDays(safeDays);
        List<RejectStatsVO> stats = qualityAnalyticsMapper.selectRejectStats(since);

        // 计算占比百分比
        int total = stats.stream().mapToInt(RejectStatsVO::getRejectCount).sum();
        for (RejectStatsVO item : stats) {
            if (total > 0) {
                item.setPercentage(String.format("%.1f",
                        item.getRejectCount() * 100.0 / total));
            } else {
                item.setPercentage("0.0");
            }
        }
        return stats;
    }

    @Override
    public GrowthTrendVO getGrowthTrend(int days) {
        int safeDays = clampDays(days);
        LocalDate since = LocalDate.now().minusDays(safeDays);

        GrowthTrendVO vo = new GrowthTrendVO();

        // 日数据点
        vo.setDailyPoints(
                qualityAnalyticsMapper.selectGrowthDailyPoints(since, "ALL"));

        // 汇总统计
        Map<String, Object> approvalSummary =
                qualityAnalyticsMapper.selectApprovalRateSummary(since);
        long totalApproved = toLong(approvalSummary.get("totalApproved"));
        long totalRejected = toLong(approvalSummary.get("totalRejected"));
        long totalAudit = totalApproved + totalRejected;

        int publishedCount = qualityAnalyticsMapper.countPublished();
        int offlineSince = qualityAnalyticsMapper.countOfflineSince(since);

        // 日平均
        vo.setAvgDailyNew(Math.round(
                vo.getDailyPoints().stream()
                        .mapToInt(DailyStatsPointVO::getNewCount)
                        .average().orElse(0) * 100.0) / 100.0);
        vo.setAvgDailyApproved(Math.round(
                vo.getDailyPoints().stream()
                        .mapToInt(DailyStatsPointVO::getApprovedCount)
                        .average().orElse(0) * 100.0) / 100.0);
        vo.setAvgDailyRejected(Math.round(
                vo.getDailyPoints().stream()
                        .mapToInt(DailyStatsPointVO::getRejectedCount)
                        .average().orElse(0) * 100.0) / 100.0);

        // 计算的计数
        int totalResources = vo.getDailyPoints().stream()
                .mapToInt(DailyStatsPointVO::getNewCount).sum();
        vo.setTotalCount(totalResources);
        vo.setPublishedCount(publishedCount);

        // 通过率 / 驳回率 / 下架率
        vo.setApprovalRate(totalAudit > 0
                ? String.format("%.1f%%", totalApproved * 100.0 / totalAudit)
                : "N/A");
        vo.setRejectionRate(totalAudit > 0
                ? String.format("%.1f%%", totalRejected * 100.0 / totalAudit)
                : "N/A");
        vo.setOfflineRate(publishedCount > 0
                ? String.format("%.1f%%", offlineSince * 100.0 / publishedCount)
                : "0.0%");

        return vo;
    }

    @Override
    public PageResult<LowAccessResourceVO> getLowAccessResources(
            int pageNum, int pageSize,
            String stage, String subject, String accessLevel) {
        int total = qualityAnalyticsMapper.countLowAccessResources(stage, subject, accessLevel);
        int offset = (pageNum - 1) * pageSize;
        List<LowAccessResourceVO> records = qualityAnalyticsMapper.selectLowAccessResources(
                stage, subject, accessLevel, offset, pageSize);
        return PageResult.of(records, total, pageNum, pageSize);
    }

    private int clampDays(int days) {
        if (days < 7) return 7;
        return Math.min(days, 90);
    }

    private String formatSeconds(long totalSecs) {
        if (totalSecs <= 0) return "0m";
        long hours = totalSecs / 3600;
        long minutes = (totalSecs % 3600) / 60;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }

    private long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(String.valueOf(value));
    }
}
