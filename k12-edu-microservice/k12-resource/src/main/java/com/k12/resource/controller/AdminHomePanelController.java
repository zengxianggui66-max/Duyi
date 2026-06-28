package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.*;
import com.k12.resource.service.AdminHomePanelService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Phase 7-C: home panel tab config + featured admin APIs.
 */
@RestController
@RequestMapping("/api/admin/home/panels")
public class AdminHomePanelController {

    private final AdminHomePanelService adminHomePanelService;
    public AdminHomePanelController(AdminHomePanelService adminHomePanelService) {
        this.adminHomePanelService = adminHomePanelService;
    }


    @GetMapping("/tabs")
    @RequiresPermission("admin:home:view")
    public Result<List<HomePanelTabConfigVO>> listTabs(
            @RequestParam(required = false) String panelCode,
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(adminHomePanelService.listTabConfigs(panelCode, includeDisabled));
    }

    @PutMapping("/tabs/{id}")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "update_panel_tab", permission = "admin:home:edit")
    public Result<HomePanelTabConfigVO> updateTab(
            @PathVariable Long id,
            @Valid @RequestBody HomePanelTabConfigWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminHomePanelService.updateTabConfig(id, dto, userId));
    }

    @PutMapping("/tabs/{id}/status")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "update_panel_tab_status", permission = "admin:home:edit")
    public Result<Void> updateTabStatus(
            @PathVariable Long id,
            @RequestParam int status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminHomePanelService.updateTabConfigStatus(id, status, userId);
        return Result.success(null);
    }

    @GetMapping("/featured")
    @RequiresPermission("admin:home:view")
    public Result<List<HomePanelFeaturedVO>> listFeatured(
            @RequestParam(required = false) String panelCode,
            @RequestParam(required = false) String tabKey,
            @RequestParam(required = false) String filterKey,
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(adminHomePanelService.listFeatured(panelCode, tabKey, filterKey, includeDisabled));
    }

    @PostMapping("/featured")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "create_panel_featured", permission = "admin:home:edit")
    public Result<HomePanelFeaturedVO> createFeatured(
            @Valid @RequestBody HomePanelFeaturedWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminHomePanelService.createFeatured(dto, userId));
    }

    @PutMapping("/featured/{id}")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "update_panel_featured", permission = "admin:home:edit")
    public Result<HomePanelFeaturedVO> updateFeatured(
            @PathVariable Long id,
            @Valid @RequestBody HomePanelFeaturedWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminHomePanelService.updateFeatured(id, dto, userId));
    }

    @PutMapping("/featured/{id}/status")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "update_panel_featured_status", permission = "admin:home:edit")
    public Result<Void> updateFeaturedStatus(
            @PathVariable Long id,
            @RequestParam int status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminHomePanelService.updateFeaturedStatus(id, status, userId);
        return Result.success(null);
    }

    @DeleteMapping("/featured/{id}")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "delete_panel_featured", permission = "admin:home:edit")
    public Result<Void> deleteFeatured(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminHomePanelService.deleteFeatured(id, userId);
        return Result.success(null);
    }

    @GetMapping("/preview")
    @RequiresPermission("admin:home:view")
    public Result<HomePanelListVO> preview(
            @RequestParam String panelCode,
            @RequestParam String tabKey,
            @RequestParam(required = false) String filterKey,
            @RequestParam(required = false) String stageKey,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) String gradeName,
            @RequestParam(required = false) Integer limit) {
        return Result.success(adminHomePanelService.previewPanel(
                panelCode, tabKey, filterKey, stageKey, subjectName, gradeName, limit));
    }
}
