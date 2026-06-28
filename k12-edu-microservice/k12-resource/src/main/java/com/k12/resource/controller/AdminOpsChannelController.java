package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.*;
import com.k12.resource.service.OpsChannelService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/operation/channels")
public class AdminOpsChannelController {

    private final OpsChannelService opsChannelService;
    public AdminOpsChannelController(OpsChannelService opsChannelService) {
        this.opsChannelService = opsChannelService;
    }


    @GetMapping
    @RequiresPermission("admin:home:view")
    public Result<List<OpsChannelAdminVO>> list(
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(opsChannelService.listAdmin(includeDisabled));
    }

    @PutMapping("/{id}")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "update_ops_channel", permission = "admin:home:edit")
    public Result<OpsChannelAdminVO> updateChannel(
            @PathVariable Long id,
            @Valid @RequestBody OpsChannelWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(opsChannelService.updateChannel(id, dto, userId));
    }

    @PutMapping("/{id}/status")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "update_ops_channel_status", permission = "admin:home:edit")
    public Result<Void> updateChannelStatus(
            @PathVariable Long id,
            @RequestParam int status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        opsChannelService.updateChannelStatus(id, status, userId);
        return Result.success(null);
    }

    @GetMapping("/{code}/tabs")
    @RequiresPermission("admin:home:view")
    public Result<List<OpsChannelTabVO>> listTabs(
            @PathVariable String code,
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(opsChannelService.listTabs(code, includeDisabled));
    }

    @PutMapping("/tabs/{tabId}")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "update_ops_channel_tab", permission = "admin:home:edit")
    public Result<OpsChannelTabVO> updateTab(
            @PathVariable Long tabId,
            @Valid @RequestBody OpsChannelTabWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(opsChannelService.updateTab(tabId, dto, userId));
    }

    @GetMapping("/{code}/albums")
    @RequiresPermission("admin:home:view")
    public Result<List<OpsChannelAlbumVO>> listAlbums(
            @PathVariable String code,
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(opsChannelService.listAlbums(code, includeDisabled));
    }

    @PostMapping("/{code}/albums")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "create_ops_channel_album", permission = "admin:home:edit")
    public Result<OpsChannelAlbumVO> createAlbum(
            @PathVariable String code,
            @Valid @RequestBody OpsChannelAlbumWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(opsChannelService.createAlbum(code, dto, userId));
    }

    @PutMapping("/albums/{albumId}")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "update_ops_channel_album", permission = "admin:home:edit")
    public Result<OpsChannelAlbumVO> updateAlbum(
            @PathVariable Long albumId,
            @Valid @RequestBody OpsChannelAlbumWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(opsChannelService.updateAlbum(albumId, dto, userId));
    }

    @DeleteMapping("/albums/{albumId}")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "delete_ops_channel_album", permission = "admin:home:edit")
    public Result<Void> deleteAlbum(
            @PathVariable Long albumId,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        opsChannelService.deleteAlbum(albumId, userId);
        return Result.success(null);
    }
}
