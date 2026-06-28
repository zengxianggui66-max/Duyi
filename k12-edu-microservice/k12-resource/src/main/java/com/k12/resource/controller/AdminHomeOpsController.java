package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.*;
import com.k12.resource.service.HomeOpsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Phase 7-A: home ops admin CRUD.
 */
@RestController
@RequestMapping("/api/admin/home")
public class AdminHomeOpsController {

    private final HomeOpsService homeOpsService;
    public AdminHomeOpsController(HomeOpsService homeOpsService) {
        this.homeOpsService = homeOpsService;
    }


    @GetMapping("/banners")
    @RequiresPermission("admin:home:view")
    public Result<List<HomeBannerVO>> listBanners(
            @RequestParam(defaultValue = "home_hero") String slotCode,
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(homeOpsService.listBanners(slotCode, null, !includeDisabled));
    }

    @PostMapping("/banners")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "create_banner", permission = "admin:home:edit")
    public Result<HomeBannerVO> createBanner(
            @Valid @RequestBody HomeBannerWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(homeOpsService.createBanner(dto, userId));
    }

    @PutMapping("/banners/{id}")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "update_banner", permission = "admin:home:edit")
    public Result<HomeBannerVO> updateBanner(
            @PathVariable Long id,
            @Valid @RequestBody HomeBannerWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(homeOpsService.updateBanner(id, dto, userId));
    }

    @PutMapping("/banners/{id}/status")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "update_banner_status", permission = "admin:home:edit")
    public Result<Void> updateBannerStatus(
            @PathVariable Long id,
            @RequestParam int status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        homeOpsService.updateBannerStatus(id, status, userId);
        return Result.success(null);
    }

    @DeleteMapping("/banners/{id}")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "delete_banner", permission = "admin:home:edit")
    public Result<Void> deleteBanner(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        homeOpsService.deleteBanner(id, userId);
        return Result.success(null);
    }

    @GetMapping("/quick-entries")
    @RequiresPermission("admin:home:view")
    public Result<List<HomeQuickEntryVO>> listQuickEntries(
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(homeOpsService.listQuickEntries(null, !includeDisabled));
    }

    @PostMapping("/quick-entries")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "create_quick_entry", permission = "admin:home:edit")
    public Result<HomeQuickEntryVO> createQuickEntry(
            @Valid @RequestBody HomeQuickEntryWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(homeOpsService.createQuickEntry(dto, userId));
    }

    @PutMapping("/quick-entries/{id}")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "update_quick_entry", permission = "admin:home:edit")
    public Result<HomeQuickEntryVO> updateQuickEntry(
            @PathVariable Long id,
            @Valid @RequestBody HomeQuickEntryWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(homeOpsService.updateQuickEntry(id, dto, userId));
    }

    @PutMapping("/quick-entries/{id}/status")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "update_quick_entry_status", permission = "admin:home:edit")
    public Result<Void> updateQuickEntryStatus(
            @PathVariable Long id,
            @RequestParam int status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        homeOpsService.updateQuickEntryStatus(id, status, userId);
        return Result.success(null);
    }

    @DeleteMapping("/quick-entries/{id}")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "delete_quick_entry", permission = "admin:home:edit")
    public Result<Void> deleteQuickEntry(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        homeOpsService.deleteQuickEntry(id, userId);
        return Result.success(null);
    }

    @GetMapping("/hot-words")
    @RequiresPermission("admin:home:view")
    public Result<List<HomeHotWordVO>> listHotWords(
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(homeOpsService.listHotWords(null, !includeDisabled));
    }

    @PostMapping("/hot-words")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "create_hot_word", permission = "admin:home:edit")
    public Result<HomeHotWordVO> createHotWord(
            @Valid @RequestBody HomeHotWordWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(homeOpsService.createHotWord(dto, userId));
    }

    @PutMapping("/hot-words/{id}")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "update_hot_word", permission = "admin:home:edit")
    public Result<HomeHotWordVO> updateHotWord(
            @PathVariable Long id,
            @Valid @RequestBody HomeHotWordWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(homeOpsService.updateHotWord(id, dto, userId));
    }

    @PutMapping("/hot-words/{id}/status")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "update_hot_word_status", permission = "admin:home:edit")
    public Result<Void> updateHotWordStatus(
            @PathVariable Long id,
            @RequestParam int status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        homeOpsService.updateHotWordStatus(id, status, userId);
        return Result.success(null);
    }

    @DeleteMapping("/hot-words/{id}")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "delete_hot_word", permission = "admin:home:edit")
    public Result<Void> deleteHotWord(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        homeOpsService.deleteHotWord(id, userId);
        return Result.success(null);
    }

    @PutMapping("/hot-words/reorder")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "reorder_hot_words", permission = "admin:home:edit")
    public Result<Void> reorderHotWords(
            @Valid @RequestBody HomeHotWordReorderDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        homeOpsService.reorderHotWords(dto, userId);
        return Result.success(null);
    }
}
