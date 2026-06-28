package com.k12.resource.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.AuditRejectReasonWriteDTO;
import com.k12.common.dto.ResourceAuditLogQueryDTO;
import com.k12.common.dto.ResourceAuditLogVO;
import com.k12.common.entity.AuditRejectReason;
import com.k12.resource.service.AdminAuditService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Phase 4-B 审核中心：审核流水 + 驳回原因模板
 */
@RestController
@RequestMapping("/api/admin/audit")
public class AdminAuditController {

    private final AdminAuditService adminAuditService;
    public AdminAuditController(AdminAuditService adminAuditService) {
        this.adminAuditService = adminAuditService;
    }


    @GetMapping("/logs")
    @RequiresPermission("admin:audit:records")
    public Result<Page<ResourceAuditLogVO>> logs(
            ResourceAuditLogQueryDTO query,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminAuditService.listLogs(query, userId));
    }

    @GetMapping("/reject-reasons")
    @RequiresPermission("admin:audit:view")
    public Result<List<AuditRejectReason>> rejectReasons(
            @RequestParam(defaultValue = "false") boolean includeDisabled,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail("请先登录");
        }
        return Result.success(adminAuditService.listRejectReasons(includeDisabled));
    }

    @GetMapping("/reject-reasons/by-category")
    @RequiresPermission("admin:audit:view")
    public Result<Map<String, List<AuditRejectReason>>> rejectReasonsByCategory(
            @RequestParam(defaultValue = "false") boolean includeDisabled,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.fail("请先登录");
        }
        return Result.success(adminAuditService.listRejectReasonsByCategory(includeDisabled));
    }

    @PostMapping("/reject-reasons")
    @RequiresPermission("admin:audit:reasons:edit")
    @AdminLog(module = "audit", action = "create_reject_reason", permission = "admin:audit:reasons:edit")
    public Result<AuditRejectReason> createRejectReason(
            @Valid @RequestBody AuditRejectReasonWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminAuditService.createRejectReason(dto, userId));
    }

    @PutMapping("/reject-reasons/{id}")
    @RequiresPermission("admin:audit:reasons:edit")
    @AdminLog(module = "audit", action = "update_reject_reason", permission = "admin:audit:reasons:edit")
    public Result<AuditRejectReason> updateRejectReason(
            @PathVariable Long id,
            @Valid @RequestBody AuditRejectReasonWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminAuditService.updateRejectReason(id, dto, userId));
    }

    @PutMapping("/reject-reasons/{id}/status")
    @RequiresPermission("admin:audit:reasons:edit")
    @AdminLog(module = "audit", action = "toggle_reject_reason", permission = "admin:audit:reasons:edit")
    public Result<Void> setRejectReasonStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminAuditService.setRejectReasonStatus(id, status, userId);
        return Result.success(null);
    }

    @DeleteMapping("/reject-reasons/{id}")
    @RequiresPermission("admin:audit:reasons:edit")
    @AdminLog(module = "audit", action = "delete_reject_reason", permission = "admin:audit:reasons:edit")
    public Result<Void> deleteRejectReason(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminAuditService.deleteRejectReason(id, userId);
        return Result.success(null);
    }
}
