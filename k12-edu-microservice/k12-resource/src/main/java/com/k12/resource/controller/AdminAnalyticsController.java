package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.AdminAnalyticsDashboardVO;
import com.k12.resource.service.AdminAnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;
    public AdminAnalyticsController(AdminAnalyticsService adminAnalyticsService) {
        this.adminAnalyticsService = adminAnalyticsService;
    }


    @GetMapping("/dashboard")
    @RequiresPermission("admin:analytics:view")
    public Result<AdminAnalyticsDashboardVO> dashboard(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam(defaultValue = "7") int days) {
        return Result.success(adminAnalyticsService.getDashboard(userId, days));
    }
}
