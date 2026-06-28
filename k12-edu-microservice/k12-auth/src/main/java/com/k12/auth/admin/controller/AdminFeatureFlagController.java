package com.k12.auth.admin.controller;

import com.k12.auth.admin.service.FeatureFlagService;
import com.k12.common.Result;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.AdminFeatureFlagsVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/system")
public class AdminFeatureFlagController {

    private final FeatureFlagService featureFlagService;
    public AdminFeatureFlagController(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }


    @GetMapping("/feature-flags")
    @RequiresPermission("admin:system:config_view")
    public Result<AdminFeatureFlagsVO> featureFlags() {
        return Result.success(featureFlagService.getAdminView());
    }
}
