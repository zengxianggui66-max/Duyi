package com.k12.auth.admin.controller;

import com.k12.auth.admin.service.AdminAnalyticsService;
import com.k12.common.Result;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.AdminAnalyticsUserVO;
import org.springframework.web.bind.annotation.GetMapping;
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


    @GetMapping("/users")
    @RequiresPermission("admin:analytics:view")
    public Result<AdminAnalyticsUserVO> userAnalytics(@RequestParam(defaultValue = "30") int days) {
        return Result.success(adminAnalyticsService.getUserAnalytics(days));
    }
}
