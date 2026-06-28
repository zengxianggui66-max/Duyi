package com.k12.resource.controller;

import com.k12.common.PageResult;
import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.AdminContentPackageWriteDTO;
import com.k12.common.dto.AdminContentQueryDTO;
import com.k12.common.entity.CompetitionTrainingPackage;
import com.k12.resource.service.admin.AdminContentCompetitionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * Phase 3J-2：竞赛专区内容中心 Admin（套卷 CRUD）
 */
@RestController
@RequestMapping("/api/admin/competition")
public class AdminCompetitionController {

    private final AdminContentCompetitionService adminContentCompetitionService;

    public AdminCompetitionController(AdminContentCompetitionService adminContentCompetitionService) {
        this.adminContentCompetitionService = adminContentCompetitionService;
    }

    @GetMapping("/packages")
    @RequiresPermission("admin:content:view")
    public Result<PageResult<CompetitionTrainingPackage>> listPackages(AdminContentQueryDTO query) {
        return Result.success(adminContentCompetitionService.listPackages(query));
    }

    @GetMapping("/packages/{id}")
    @RequiresPermission("admin:content:view")
    public Result<CompetitionTrainingPackage> getPackage(@PathVariable Long id) {
        return Result.success(adminContentCompetitionService.getPackage(id));
    }

    @PostMapping("/packages")
    @RequiresPermission("admin:content:edit")
    @AdminLog(module = "content", action = "create_competition_package", permission = "admin:content:edit")
    public Result<CompetitionTrainingPackage> createPackage(@Valid @RequestBody AdminContentPackageWriteDTO dto) {
        return Result.success(adminContentCompetitionService.createPackage(dto));
    }

    @PutMapping("/packages/{id}")
    @RequiresPermission("admin:content:edit")
    @AdminLog(module = "content", action = "update_competition_package", permission = "admin:content:edit")
    public Result<CompetitionTrainingPackage> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody AdminContentPackageWriteDTO dto) {
        return Result.success(adminContentCompetitionService.updatePackage(id, dto));
    }

    @PutMapping("/packages/{id}/status")
    @RequiresPermission("admin:content:edit")
    @AdminLog(module = "content", action = "update_competition_package_status", permission = "admin:content:edit")
    public Result<Void> updatePackageStatus(@PathVariable Long id, @RequestParam int status) {
        adminContentCompetitionService.updatePackageStatus(id, status);
        return Result.success(null);
    }

    @DeleteMapping("/packages/{id}")
    @RequiresPermission("admin:content:edit")
    @AdminLog(module = "content", action = "delete_competition_package", permission = "admin:content:edit")
    public Result<Void> deletePackage(@PathVariable Long id) {
        adminContentCompetitionService.deletePackage(id);
        return Result.success(null);
    }
}
