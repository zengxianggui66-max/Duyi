package com.k12.auth.controller;

import com.k12.auth.admin.service.FeatureFlagService;
import com.k12.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Phase 8-D：C 端可读运行时功能开关（无 secret）
 */
@RestController
@RequestMapping("/api/public")
public class PublicFeatureFlagController {

    private final FeatureFlagService featureFlagService;
    public PublicFeatureFlagController(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }


    @GetMapping("/feature-flags")
    public Result<Map<String, Boolean>> featureFlags() {
        return Result.success(featureFlagService.getPublicFlags());
    }
}
