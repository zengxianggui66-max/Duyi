package com.k12.resource.controller;

import com.k12.common.PageResult;
import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.AdminContentPackageWriteDTO;
import com.k12.common.dto.AdminContentQueryDTO;
import com.k12.common.entity.CultureStudyPackage;
import com.k12.resource.service.admin.AdminContentCultureService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Phase 3J-2：传统文化内容中心 Admin（研学包 CRUD）
 */
@RestController
@RequestMapping("/api/admin/culture")
public class AdminCultureController {

    private final AdminContentCultureService adminContentCultureService;

    public AdminCultureController(AdminContentCultureService adminContentCultureService) {
        this.adminContentCultureService = adminContentCultureService;
    }

    @GetMapping("/packages")
    @RequiresPermission("admin:content:view")
    public Result<PageResult<CultureStudyPackage>> listPackages(AdminContentQueryDTO query) {
        return Result.success(adminContentCultureService.listPackages(query));
    }

    @GetMapping("/packages/{id}")
    @RequiresPermission("admin:content:view")
    public Result<CultureStudyPackage> getPackage(@PathVariable Long id) {
        return Result.success(adminContentCultureService.getPackage(id));
    }

    @PostMapping("/packages")
    @RequiresPermission("admin:content:edit")
    @AdminLog(module = "content", action = "create_culture_package", permission = "admin:content:edit")
    public Result<CultureStudyPackage> createPackage(@Valid @RequestBody AdminContentPackageWriteDTO dto) {
        return Result.success(adminContentCultureService.createPackage(dto));
    }

    @PutMapping("/packages/{id}")
    @RequiresPermission("admin:content:edit")
    @AdminLog(module = "content", action = "update_culture_package", permission = "admin:content:edit")
    public Result<CultureStudyPackage> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody AdminContentPackageWriteDTO dto) {
        return Result.success(adminContentCultureService.updatePackage(id, dto));
    }

    @PutMapping("/packages/{id}/status")
    @RequiresPermission("admin:content:edit")
    @AdminLog(module = "content", action = "update_culture_package_status", permission = "admin:content:edit")
    public Result<Void> updatePackageStatus(@PathVariable Long id, @RequestParam int status) {
        adminContentCultureService.updatePackageStatus(id, status);
        return Result.success(null);
    }

    @DeleteMapping("/packages/{id}")
    @RequiresPermission("admin:content:edit")
    @AdminLog(module = "content", action = "delete_culture_package", permission = "admin:content:edit")
    public Result<Void> deletePackage(@PathVariable Long id) {
        adminContentCultureService.deletePackage(id);
        return Result.success(null);
    }
}
