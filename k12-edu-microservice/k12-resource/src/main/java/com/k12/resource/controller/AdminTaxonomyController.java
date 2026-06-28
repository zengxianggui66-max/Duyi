package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.*;
import com.k12.common.entity.*;
import com.k12.resource.service.AdminTaxonomyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Phase 5-B：分类维度管理端 CRUD
 */
@RestController
@RequestMapping("/api/admin/taxonomy")
public class AdminTaxonomyController {

    private final AdminTaxonomyService adminTaxonomyService;
    public AdminTaxonomyController(AdminTaxonomyService adminTaxonomyService) {
        this.adminTaxonomyService = adminTaxonomyService;
    }


    // ==================== 学段 ====================

    @GetMapping("/stages")
    @RequiresPermission("admin:taxonomy:view")
    public Result<List<EduStage>> listStages(
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(adminTaxonomyService.listStages(includeDisabled));
    }

    @PostMapping("/stages")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "create_stage", permission = "admin:taxonomy:edit")
    public Result<EduStage> createStage(
            @Valid @RequestBody AdminTaxonomyStageWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminTaxonomyService.createStage(dto, userId));
    }

    @PutMapping("/stages/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "update_stage", permission = "admin:taxonomy:edit")
    public Result<EduStage> updateStage(
            @PathVariable Integer id,
            @Valid @RequestBody AdminTaxonomyStageWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminTaxonomyService.updateStage(id, dto, userId));
    }

    @PutMapping("/stages/{id}/status")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "toggle_stage", permission = "admin:taxonomy:edit")
    public Result<Void> setStageStatus(
            @PathVariable Integer id,
            @RequestParam Integer status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminTaxonomyService.setStageStatus(id, status, userId);
        return Result.success(null);
    }

    @DeleteMapping("/stages/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "delete_stage", permission = "admin:taxonomy:edit")
    public Result<Void> deleteStage(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminTaxonomyService.deleteStage(id, userId);
        return Result.success(null);
    }

    // ==================== 学科 ====================

    @GetMapping("/subjects")
    @RequiresPermission("admin:taxonomy:view")
    public Result<List<AdminTaxonomySubjectAdminVO>> listSubjects(
            @RequestParam(required = false) Integer stageId,
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(adminTaxonomyService.listSubjects(stageId, includeDisabled));
    }

    @PostMapping("/subjects")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "create_subject", permission = "admin:taxonomy:edit")
    public Result<AdminTaxonomySubjectAdminVO> createSubject(
            @Valid @RequestBody AdminTaxonomySubjectWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminTaxonomyService.createSubject(dto, userId));
    }

    @PutMapping("/subjects/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "update_subject", permission = "admin:taxonomy:edit")
    public Result<AdminTaxonomySubjectAdminVO> updateSubject(
            @PathVariable Integer id,
            @Valid @RequestBody AdminTaxonomySubjectWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminTaxonomyService.updateSubject(id, dto, userId));
    }

    @PutMapping("/subjects/{id}/status")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "toggle_subject", permission = "admin:taxonomy:edit")
    public Result<Void> setSubjectStatus(
            @PathVariable Integer id,
            @RequestParam Integer status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminTaxonomyService.setSubjectStatus(id, status, userId);
        return Result.success(null);
    }

    @DeleteMapping("/subjects/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "delete_subject", permission = "admin:taxonomy:edit")
    public Result<Void> deleteSubject(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminTaxonomyService.deleteSubject(id, userId);
        return Result.success(null);
    }

    // ==================== 教材版本 ====================

    /** Phase 6：支持按学段/学科筛选版本 */
    @GetMapping("/editions")
    @RequiresPermission("admin:taxonomy:view")
    public Result<List<EduEdition>> listEditions(
            @RequestParam(required = false) Integer stageId,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(adminTaxonomyService.listEditions(stageId, subjectId, includeDisabled));
    }

    @PostMapping("/editions")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "create_edition", permission = "admin:taxonomy:edit")
    public Result<EduEdition> createEdition(
            @Valid @RequestBody AdminTaxonomyEditionWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminTaxonomyService.createEdition(dto, userId));
    }

    @PutMapping("/editions/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "update_edition", permission = "admin:taxonomy:edit")
    public Result<EduEdition> updateEdition(
            @PathVariable Integer id,
            @Valid @RequestBody AdminTaxonomyEditionWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminTaxonomyService.updateEdition(id, dto, userId));
    }

    @PutMapping("/editions/{id}/status")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "toggle_edition", permission = "admin:taxonomy:edit")
    public Result<Void> setEditionStatus(
            @PathVariable Integer id,
            @RequestParam Integer status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminTaxonomyService.setEditionStatus(id, status, userId);
        return Result.success(null);
    }

    @DeleteMapping("/editions/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "delete_edition", permission = "admin:taxonomy:edit")
    public Result<Void> deleteEdition(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminTaxonomyService.deleteEdition(id, userId);
        return Result.success(null);
    }

    // ==================== 年级 ====================

    @GetMapping("/grades")
    @RequiresPermission("admin:taxonomy:view")
    public Result<List<EduGrade>> listGrades(
            @RequestParam(required = false) Integer stageId,
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(adminTaxonomyService.listGrades(stageId, includeDisabled));
    }

    @PostMapping("/grades")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "create_grade", permission = "admin:taxonomy:edit")
    public Result<EduGrade> createGrade(
            @Valid @RequestBody AdminTaxonomyGradeWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminTaxonomyService.createGrade(dto, userId));
    }

    @PutMapping("/grades/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "update_grade", permission = "admin:taxonomy:edit")
    public Result<EduGrade> updateGrade(
            @PathVariable Integer id,
            @Valid @RequestBody AdminTaxonomyGradeWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminTaxonomyService.updateGrade(id, dto, userId));
    }

    @PutMapping("/grades/{id}/status")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "toggle_grade", permission = "admin:taxonomy:edit")
    public Result<Void> setGradeStatus(
            @PathVariable Integer id,
            @RequestParam Integer status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminTaxonomyService.setGradeStatus(id, status, userId);
        return Result.success(null);
    }

    @DeleteMapping("/grades/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "delete_grade", permission = "admin:taxonomy:edit")
    public Result<Void> deleteGrade(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminTaxonomyService.deleteGrade(id, userId);
        return Result.success(null);
    }

    // ==================== 册别（上/下册） ====================

    /** Phase 6：支持多维过滤 */
    @GetMapping("/volumes")
    @RequiresPermission("admin:taxonomy:view")
    public Result<List<EduVolume>> listVolumes(
            @RequestParam(required = false) Integer stageId,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) Integer editionId,
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(adminTaxonomyService.listVolumes(stageId, subjectId, editionId, includeDisabled));
    }

    @PostMapping("/volumes")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "create_volume", permission = "admin:taxonomy:edit")
    public Result<EduVolume> createVolume(
            @Valid @RequestBody AdminTaxonomyVolumeWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminTaxonomyService.createVolume(dto, userId));
    }

    @PutMapping("/volumes/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "update_volume", permission = "admin:taxonomy:edit")
    public Result<EduVolume> updateVolume(
            @PathVariable Integer id,
            @Valid @RequestBody AdminTaxonomyVolumeWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminTaxonomyService.updateVolume(id, dto, userId));
    }

    /** Phase 6：册别启用/禁用开关 */
    @PutMapping("/volumes/{id}/status")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "toggle_volume", permission = "admin:taxonomy:edit")
    public Result<Void> setVolumeStatus(
            @PathVariable Integer id,
            @RequestParam Integer status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminTaxonomyService.setVolumeStatus(id, status, userId);
        return Result.success(null);
    }

    @DeleteMapping("/volumes/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "delete_volume", permission = "admin:taxonomy:edit")
    public Result<Void> deleteVolume(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminTaxonomyService.deleteVolume(id, userId);
        return Result.success(null);
    }

    // ==================== 栏目 ====================

    @GetMapping("/modules")
    @RequiresPermission("admin:taxonomy:view")
    public Result<List<AdminTaxonomyModuleAdminVO>> listModules(
            @RequestParam(required = false) Integer stageId,
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(adminTaxonomyService.listModules(stageId, includeDisabled));
    }

    @PostMapping("/modules")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "create_module", permission = "admin:taxonomy:edit")
    public Result<AdminTaxonomyModuleAdminVO> createModule(
            @Valid @RequestBody AdminTaxonomyModuleWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminTaxonomyService.createModule(dto, userId));
    }

    @PutMapping("/modules/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "update_module", permission = "admin:taxonomy:edit")
    public Result<AdminTaxonomyModuleAdminVO> updateModule(
            @PathVariable Integer id,
            @Valid @RequestBody AdminTaxonomyModuleWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminTaxonomyService.updateModule(id, dto, userId));
    }

    @PutMapping("/modules/{id}/status")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "toggle_module", permission = "admin:taxonomy:edit")
    public Result<Void> setModuleStatus(
            @PathVariable Integer id,
            @RequestParam Integer status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminTaxonomyService.setModuleStatus(id, status, userId);
        return Result.success(null);
    }

    @DeleteMapping("/modules/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "delete_module", permission = "admin:taxonomy:edit")
    public Result<Void> deleteModule(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminTaxonomyService.deleteModule(id, userId);
        return Result.success(null);
    }

    // ==================== 资源类型 ====================

    @GetMapping("/resource-types")
    @RequiresPermission("admin:taxonomy:view")
    public Result<List<EduResourceType>> listResourceTypes(
            @RequestParam(required = false) Integer parentId,
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(adminTaxonomyService.listResourceTypes(parentId, includeDisabled));
    }

    @PostMapping("/resource-types")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "create_resource_type", permission = "admin:taxonomy:edit")
    public Result<EduResourceType> createResourceType(
            @Valid @RequestBody AdminTaxonomyResourceTypeWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminTaxonomyService.createResourceType(dto, userId));
    }

    @PutMapping("/resource-types/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "update_resource_type", permission = "admin:taxonomy:edit")
    public Result<EduResourceType> updateResourceType(
            @PathVariable Integer id,
            @Valid @RequestBody AdminTaxonomyResourceTypeWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminTaxonomyService.updateResourceType(id, dto, userId));
    }

    @PutMapping("/resource-types/{id}/status")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "toggle_resource_type", permission = "admin:taxonomy:edit")
    public Result<Void> setResourceTypeStatus(
            @PathVariable Integer id,
            @RequestParam Integer status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminTaxonomyService.setResourceTypeStatus(id, status, userId);
        return Result.success(null);
    }

    @DeleteMapping("/resource-types/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "taxonomy", action = "delete_resource_type", permission = "admin:taxonomy:edit")
    public Result<Void> deleteResourceType(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminTaxonomyService.deleteResourceType(id, userId);
        return Result.success(null);
    }
}
