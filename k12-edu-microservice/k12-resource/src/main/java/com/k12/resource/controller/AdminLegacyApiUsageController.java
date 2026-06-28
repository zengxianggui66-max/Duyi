package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.LegacyApiUsageVO;
import com.k12.resource.service.LegacyApiUsageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/resource-main")
public class AdminLegacyApiUsageController {

    private final LegacyApiUsageService legacyApiUsageService;

    public AdminLegacyApiUsageController(LegacyApiUsageService legacyApiUsageService) {
        this.legacyApiUsageService = legacyApiUsageService;
    }

    @GetMapping("/legacy-api-usage")
    @RequiresPermission("admin:analytics:view")
    public Result<List<LegacyApiUsageVO>> legacyApiUsage(
            @RequestParam(required = false, defaultValue = "7") Integer days) {
        return Result.success(legacyApiUsageService.listRecent(days));
    }
}
