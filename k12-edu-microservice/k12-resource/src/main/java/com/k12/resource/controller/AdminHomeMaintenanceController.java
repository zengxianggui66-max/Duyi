package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.annotation.RequiresPermission;
import com.k12.resource.cache.HomeCacheService;
import com.k12.resource.cache.HomeScheduleService;
import com.k12.common.dto.HomePreviewVO;
import com.k12.resource.service.HomePreviewService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/home")
public class AdminHomeMaintenanceController {

    private final HomePreviewService homePreviewService;
    private final HomeCacheService homeCacheService;
    private final HomeScheduleService homeScheduleService;
    public AdminHomeMaintenanceController(HomePreviewService homePreviewService, HomeCacheService homeCacheService, HomeScheduleService homeScheduleService) {
        this.homePreviewService = homePreviewService;
        this.homeCacheService = homeCacheService;
        this.homeScheduleService = homeScheduleService;
    }


    @GetMapping("/preview")
    @RequiresPermission("admin:home:view")
    public Result<HomePreviewVO> preview(
            @RequestParam(defaultValue = "primary") String stageKey) {
        return Result.success(homePreviewService.buildPreview(stageKey));
    }

    @GetMapping("/cache/stats")
    @RequiresPermission("admin:home:view")
    public Result<Map<String, Object>> cacheStats() {
        return Result.success(homeCacheService.stats());
    }

    @PostMapping("/cache/invalidate")
    @RequiresPermission("admin:home:edit")
    public Result<Void> invalidateCache() {
        homeCacheService.evictAll();
        return Result.success(null);
    }

    @PostMapping("/schedule/run")
    @RequiresPermission("admin:home:edit")
    public Result<Map<String, Object>> runSchedule() {
        int changed = homeScheduleService.runOnce();
        return Result.success(Map.of("changed", changed));
    }
}
