package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.*;
import com.k12.common.entity.*;
import com.k12.resource.service.AdminDictionaryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Phase 5-D：业务字典管理端 CRUD（考试场景 / 地区 / 文件格式）
 */
@RestController
@RequestMapping("/api/admin/dictionaries")
public class AdminDictionaryController {

    private final AdminDictionaryService adminDictionaryService;
    public AdminDictionaryController(AdminDictionaryService adminDictionaryService) {
        this.adminDictionaryService = adminDictionaryService;
    }


    // ==================== 考试场景 ====================

    @GetMapping("/exam-scenes")
    @RequiresPermission("admin:taxonomy:view")
    public Result<List<EduExamScene>> listExamScenes(
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(adminDictionaryService.listExamScenes(includeDisabled));
    }

    @PostMapping("/exam-scenes")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "dictionary", action = "create_exam_scene", permission = "admin:taxonomy:edit")
    public Result<EduExamScene> createExamScene(
            @Valid @RequestBody AdminDictionaryExamSceneWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminDictionaryService.createExamScene(dto, userId));
    }

    @PutMapping("/exam-scenes/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "dictionary", action = "update_exam_scene", permission = "admin:taxonomy:edit")
    public Result<EduExamScene> updateExamScene(
            @PathVariable Integer id,
            @Valid @RequestBody AdminDictionaryExamSceneWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminDictionaryService.updateExamScene(id, dto, userId));
    }

    @PutMapping("/exam-scenes/{id}/status")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "dictionary", action = "toggle_exam_scene", permission = "admin:taxonomy:edit")
    public Result<Void> setExamSceneStatus(
            @PathVariable Integer id,
            @RequestParam Integer status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminDictionaryService.setExamSceneStatus(id, status, userId);
        return Result.success(null);
    }

    @DeleteMapping("/exam-scenes/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "dictionary", action = "delete_exam_scene", permission = "admin:taxonomy:edit")
    public Result<Void> deleteExamScene(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminDictionaryService.deleteExamScene(id, userId);
        return Result.success(null);
    }

    // ==================== 文件格式 ====================

    @GetMapping("/file-formats")
    @RequiresPermission("admin:taxonomy:view")
    public Result<List<EduFileFormat>> listFileFormats(
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(adminDictionaryService.listFileFormats(includeDisabled));
    }

    @PostMapping("/file-formats")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "dictionary", action = "create_file_format", permission = "admin:taxonomy:edit")
    public Result<EduFileFormat> createFileFormat(
            @Valid @RequestBody AdminDictionaryFileFormatWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminDictionaryService.createFileFormat(dto, userId));
    }

    @PutMapping("/file-formats/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "dictionary", action = "update_file_format", permission = "admin:taxonomy:edit")
    public Result<EduFileFormat> updateFileFormat(
            @PathVariable Integer id,
            @Valid @RequestBody AdminDictionaryFileFormatWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminDictionaryService.updateFileFormat(id, dto, userId));
    }

    @PutMapping("/file-formats/{id}/status")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "dictionary", action = "toggle_file_format", permission = "admin:taxonomy:edit")
    public Result<Void> setFileFormatStatus(
            @PathVariable Integer id,
            @RequestParam Integer status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminDictionaryService.setFileFormatStatus(id, status, userId);
        return Result.success(null);
    }

    @DeleteMapping("/file-formats/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "dictionary", action = "delete_file_format", permission = "admin:taxonomy:edit")
    public Result<Void> deleteFileFormat(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminDictionaryService.deleteFileFormat(id, userId);
        return Result.success(null);
    }

    // ==================== 地区 ====================

    @GetMapping("/regions")
    @RequiresPermission("admin:taxonomy:view")
    public Result<List<EduRegion>> listRegions(
            @RequestParam(required = false) Integer parentId,
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(adminDictionaryService.listRegions(parentId, includeDisabled));
    }

    @PostMapping("/regions")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "dictionary", action = "create_region", permission = "admin:taxonomy:edit")
    public Result<EduRegion> createRegion(
            @Valid @RequestBody AdminDictionaryRegionWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminDictionaryService.createRegion(dto, userId));
    }

    @PutMapping("/regions/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "dictionary", action = "update_region", permission = "admin:taxonomy:edit")
    public Result<EduRegion> updateRegion(
            @PathVariable Integer id,
            @Valid @RequestBody AdminDictionaryRegionWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminDictionaryService.updateRegion(id, dto, userId));
    }

    @PutMapping("/regions/{id}/status")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "dictionary", action = "toggle_region", permission = "admin:taxonomy:edit")
    public Result<Void> setRegionStatus(
            @PathVariable Integer id,
            @RequestParam Integer status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminDictionaryService.setRegionStatus(id, status, userId);
        return Result.success(null);
    }

    @DeleteMapping("/regions/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "dictionary", action = "delete_region", permission = "admin:taxonomy:edit")
    public Result<Void> deleteRegion(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminDictionaryService.deleteRegion(id, userId);
        return Result.success(null);
    }
}
