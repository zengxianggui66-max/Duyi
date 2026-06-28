package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.ResourceMigrationQualityDashboardVO;
import com.k12.resource.service.ResourceMigrationQualityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 3C：数据回填质量看板
 */
@RestController
@RequestMapping("/api/admin/quality")
public class AdminResourceMigrationQualityController {

    private final ResourceMigrationQualityService migrationQualityService;

    public AdminResourceMigrationQualityController(ResourceMigrationQualityService migrationQualityService) {
        this.migrationQualityService = migrationQualityService;
    }

    @GetMapping("/analytics/migration-dashboard")
    @RequiresPermission("admin:quality:sensitive_view")
    public Result<ResourceMigrationQualityDashboardVO> dashboard(
            @RequestHeader(value = "X-User-Id", required = false) Long adminUserId) {
        return Result.success(migrationQualityService.getDashboard(adminUserId));
    }
}
