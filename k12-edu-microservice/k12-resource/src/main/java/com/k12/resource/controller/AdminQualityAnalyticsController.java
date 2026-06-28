package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.*;
import com.k12.resource.service.QualityAnalyticsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Phase 8：质量治理分析 Controller
 */
@RestController
@RequestMapping("/api/admin/quality")
public class AdminQualityAnalyticsController {

    private final QualityAnalyticsService qualityAnalyticsService;
    public AdminQualityAnalyticsController(QualityAnalyticsService qualityAnalyticsService) {
        this.qualityAnalyticsService = qualityAnalyticsService;
    }


    /**
     * 质量大盘
     */
    @GetMapping("/analytics/dashboard")
    @RequiresPermission("admin:quality:sensitive_view")
    public Result<QualityDashboardVO> dashboard(
            @RequestParam(defaultValue = "30") int days) {
        return Result.success(qualityAnalyticsService.getQualityDashboard(days));
    }

    /**
     * 审核 SLA
     */
    @GetMapping("/analytics/sla")
    @RequiresPermission("admin:quality:sla")
    public Result<AuditSlaVO> sla(@RequestParam(defaultValue = "30") int days) {
        return Result.success(qualityAnalyticsService.getAuditSla(days));
    }

    /**
     * 审核员工作量（Top N）
     */
    @GetMapping("/analytics/workload")
    @RequiresPermission("admin:quality:workload")
    public Result<List<AuditWorkloadVO>> workload(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "10") int limit) {
        return Result.success(qualityAnalyticsService.getAuditorWorkload(days, limit));
    }

    /**
     * 驳回原因统计
     */
    @GetMapping("/analytics/reject-stats")
    @RequiresPermission("admin:quality:sensitive_view")
    public Result<List<RejectStatsVO>> rejectStats(
            @RequestParam(defaultValue = "30") int days) {
        return Result.success(qualityAnalyticsService.getRejectStats(days));
    }

    /**
     * 资源增长趋势
     */
    @GetMapping("/analytics/growth")
    @RequiresPermission("admin:quality:sensitive_view")
    public Result<GrowthTrendVO> growth(@RequestParam(defaultValue = "30") int days) {
        return Result.success(qualityAnalyticsService.getGrowthTrend(days));
    }

    /**
     * 低访问资源分页
     */
    @GetMapping("/analytics/low-access")
    @RequiresPermission("admin:quality:low_access")
    public Result<PageResult<LowAccessResourceVO>> lowAccess(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String accessLevel) {
        return Result.success(qualityAnalyticsService.getLowAccessResources(
                pageNum, pageSize, stage, subject, accessLevel));
    }
}
