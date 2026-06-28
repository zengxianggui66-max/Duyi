package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.AdminSystemPreviewStatusVO;
import com.k12.common.dto.AdminSystemStorageStatusVO;
import com.k12.resource.service.SystemPreviewStatusService;
import com.k12.resource.service.SystemStorageStatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 8-C：存储 / 预览健康探测（只读）
 */
@RestController
@RequestMapping("/api/admin/system")
public class AdminSystemStatusController {

    private final SystemStorageStatusService systemStorageStatusService;
    private final SystemPreviewStatusService systemPreviewStatusService;
    public AdminSystemStatusController(SystemStorageStatusService systemStorageStatusService, SystemPreviewStatusService systemPreviewStatusService) {
        this.systemStorageStatusService = systemStorageStatusService;
        this.systemPreviewStatusService = systemPreviewStatusService;
    }


    @GetMapping("/storage/status")
    @RequiresPermission("admin:system:config_view")
    public Result<AdminSystemStorageStatusVO> storageStatus() {
        return Result.success(systemStorageStatusService.probe());
    }

    @GetMapping("/preview/status")
    @RequiresPermission("admin:system:config_view")
    public Result<AdminSystemPreviewStatusVO> previewStatus() {
        return Result.success(systemPreviewStatusService.probe());
    }
}
