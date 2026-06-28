package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.AdminDictionaryBrowseTagWriteDTO;
import com.k12.common.dto.AdminDictionaryTeachingSceneWriteDTO;
import com.k12.common.entity.EduBrowseTag;
import com.k12.common.entity.EduTeachingScene;
import com.k12.resource.service.AdminDictionaryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Phase 5-D：标签管理端 CRUD（教学场景 / 资源属性标签）
 */
@RestController
@RequestMapping("/api/admin/tags")
public class AdminTagController {

    private final AdminDictionaryService adminDictionaryService;
    public AdminTagController(AdminDictionaryService adminDictionaryService) {
        this.adminDictionaryService = adminDictionaryService;
    }


    // ==================== 教学场景 ====================

    @GetMapping("/teaching-scenes")
    @RequiresPermission("admin:taxonomy:view")
    public Result<List<EduTeachingScene>> listTeachingScenes(
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(adminDictionaryService.listTeachingScenes(includeDisabled));
    }

    @PostMapping("/teaching-scenes")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "tag", action = "create_teaching_scene", permission = "admin:taxonomy:edit")
    public Result<EduTeachingScene> createTeachingScene(
            @Valid @RequestBody AdminDictionaryTeachingSceneWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminDictionaryService.createTeachingScene(dto, userId));
    }

    @PutMapping("/teaching-scenes/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "tag", action = "update_teaching_scene", permission = "admin:taxonomy:edit")
    public Result<EduTeachingScene> updateTeachingScene(
            @PathVariable Integer id,
            @Valid @RequestBody AdminDictionaryTeachingSceneWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminDictionaryService.updateTeachingScene(id, dto, userId));
    }

    @PutMapping("/teaching-scenes/{id}/status")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "tag", action = "toggle_teaching_scene", permission = "admin:taxonomy:edit")
    public Result<Void> setTeachingSceneStatus(
            @PathVariable Integer id,
            @RequestParam Integer status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminDictionaryService.setTeachingSceneStatus(id, status, userId);
        return Result.success(null);
    }

    @DeleteMapping("/teaching-scenes/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "tag", action = "delete_teaching_scene", permission = "admin:taxonomy:edit")
    public Result<Void> deleteTeachingScene(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminDictionaryService.deleteTeachingScene(id, userId);
        return Result.success(null);
    }

    // ==================== 资源属性标签 ====================

    @GetMapping("/browse-tags")
    @RequiresPermission("admin:taxonomy:view")
    public Result<List<EduBrowseTag>> listBrowseTags(
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(adminDictionaryService.listBrowseTags(includeDisabled));
    }

    @PostMapping("/browse-tags")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "tag", action = "create_browse_tag", permission = "admin:taxonomy:edit")
    public Result<EduBrowseTag> createBrowseTag(
            @Valid @RequestBody AdminDictionaryBrowseTagWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminDictionaryService.createBrowseTag(dto, userId));
    }

    @PutMapping("/browse-tags/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "tag", action = "update_browse_tag", permission = "admin:taxonomy:edit")
    public Result<EduBrowseTag> updateBrowseTag(
            @PathVariable Integer id,
            @Valid @RequestBody AdminDictionaryBrowseTagWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminDictionaryService.updateBrowseTag(id, dto, userId));
    }

    @PutMapping("/browse-tags/{id}/status")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "tag", action = "toggle_browse_tag", permission = "admin:taxonomy:edit")
    public Result<Void> setBrowseTagStatus(
            @PathVariable Integer id,
            @RequestParam Integer status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminDictionaryService.setBrowseTagStatus(id, status, userId);
        return Result.success(null);
    }

    @DeleteMapping("/browse-tags/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "tag", action = "delete_browse_tag", permission = "admin:taxonomy:edit")
    public Result<Void> deleteBrowseTag(
            @PathVariable Integer id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminDictionaryService.deleteBrowseTag(id, userId);
        return Result.success(null);
    }
}
