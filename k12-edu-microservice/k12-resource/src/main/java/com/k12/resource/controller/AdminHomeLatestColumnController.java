package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.*;
import com.k12.resource.service.HomeLatestColumnService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Phase 7-D: home latest columns admin.
 */
@RestController
@RequestMapping("/api/admin/home/latest-columns")
public class AdminHomeLatestColumnController {

    private final HomeLatestColumnService homeLatestColumnService;
    public AdminHomeLatestColumnController(HomeLatestColumnService homeLatestColumnService) {
        this.homeLatestColumnService = homeLatestColumnService;
    }


    @GetMapping
    @RequiresPermission("admin:home:view")
    public Result<List<HomeLatestColumnVO>> listColumns(
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(homeLatestColumnService.listAdminColumns(includeDisabled));
    }

    @PutMapping("/{id}")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "update_latest_column", permission = "admin:home:edit")
    public Result<HomeLatestColumnVO> updateColumn(
            @PathVariable Long id,
            @Valid @RequestBody HomeLatestColumnWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(homeLatestColumnService.updateColumn(id, dto, userId));
    }

    @PutMapping("/{id}/status")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "update_latest_column_status", permission = "admin:home:edit")
    public Result<Void> updateColumnStatus(
            @PathVariable Long id,
            @RequestParam int status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        homeLatestColumnService.updateColumnStatus(id, status, userId);
        return Result.success(null);
    }

    @GetMapping("/{id}/preview")
    @RequiresPermission("admin:home:view")
    public Result<List<HomeLatestItemVO>> preview(
            @PathVariable Long id,
            @RequestParam(required = false) String stageKey) {
        return Result.success(homeLatestColumnService.previewColumn(id, stageKey));
    }

    @GetMapping("/{id}/items")
    @RequiresPermission("admin:home:view")
    public Result<List<HomeLatestItemVO>> listItems(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(homeLatestColumnService.listManualItems(id, includeDisabled));
    }

    @PostMapping("/{id}/items")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "create_latest_item", permission = "admin:home:edit")
    public Result<HomeLatestItemVO> createItem(
            @PathVariable Long id,
            @Valid @RequestBody HomeLatestItemWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(homeLatestColumnService.createManualItem(id, dto, userId));
    }

    @PutMapping("/items/{itemId}")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "update_latest_item", permission = "admin:home:edit")
    public Result<HomeLatestItemVO> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody HomeLatestItemWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(homeLatestColumnService.updateManualItem(itemId, dto, userId));
    }

    @DeleteMapping("/items/{itemId}")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "delete_latest_item", permission = "admin:home:edit")
    public Result<Void> deleteItem(
            @PathVariable Long itemId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        homeLatestColumnService.deleteManualItem(itemId, userId);
        return Result.success(null);
    }
}
