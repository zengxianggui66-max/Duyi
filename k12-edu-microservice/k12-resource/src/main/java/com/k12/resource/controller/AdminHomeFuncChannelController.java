package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.HomeFuncChannelAdminVO;
import com.k12.common.dto.HomeFuncChannelWriteDTO;
import com.k12.resource.service.AdminHomeFuncChannelService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/home/func-channels")
public class AdminHomeFuncChannelController {

    private final AdminHomeFuncChannelService adminHomeFuncChannelService;
    public AdminHomeFuncChannelController(AdminHomeFuncChannelService adminHomeFuncChannelService) {
        this.adminHomeFuncChannelService = adminHomeFuncChannelService;
    }


    @GetMapping
    @RequiresPermission("admin:home:view")
    public Result<List<HomeFuncChannelAdminVO>> list(
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(adminHomeFuncChannelService.list(includeDisabled));
    }

    @PutMapping("/{id}")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "update_func_channel", permission = "admin:home:edit")
    public Result<HomeFuncChannelAdminVO> update(
            @PathVariable Long id,
            @Valid @RequestBody HomeFuncChannelWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminHomeFuncChannelService.update(id, dto, userId));
    }

    @PutMapping("/{id}/status")
    @RequiresPermission("admin:home:edit")
    @AdminLog(module = "home", action = "update_func_channel_status", permission = "admin:home:edit")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam int status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminHomeFuncChannelService.updateStatus(id, status, userId);
        return Result.success(null);
    }
}
