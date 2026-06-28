package com.k12.resource.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.AdminPrimaryResourceQueryDTO;
import com.k12.common.dto.AdminResourceAuditInsightsVO;
import com.k12.common.dto.AdminResourceBatchAuditDTO;
import com.k12.common.dto.AdminResourceBatchDTO;
import com.k12.common.dto.AdminResourceBatchResultVO;
import com.k12.common.dto.AdminResourceDetailVO;
import com.k12.common.dto.AdminResourceListVO;
import com.k12.common.dto.AdminResourceUpdateDTO;
import com.k12.resource.service.AdminPrimaryResourceService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/resources")
public class AdminResourcesController {

    private final AdminPrimaryResourceService adminPrimaryResourceService;
    public AdminResourcesController(AdminPrimaryResourceService adminPrimaryResourceService) {
        this.adminPrimaryResourceService = adminPrimaryResourceService;
    }


    @GetMapping
    @RequiresPermission("admin:resource:view")
    public Result<Page<AdminResourceListVO>> list(
            AdminPrimaryResourceQueryDTO query,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminPrimaryResourceService.listPage(query, userId));
    }

    @GetMapping("/pending")
    @RequiresPermission("admin:audit:view")
    public Result<Page<AdminResourceListVO>> pending(
            AdminPrimaryResourceQueryDTO query,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminPrimaryResourceService.listPending(query, userId));
    }

    @GetMapping("/stats")
    @RequiresPermission("admin:analytics:view")
    public Result<Map<String, Object>> stats(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminPrimaryResourceService.getResourceStats(userId));
    }

    @GetMapping("/{id}")
    @RequiresPermission("admin:resource:view")
    public Result<AdminResourceDetailVO> detail(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminPrimaryResourceService.getDetail(id, userId));
    }

    @PutMapping("/{id}")
    @RequiresPermission("admin:resource:edit")
    @AdminLog(module = "resource", action = "update", permission = "admin:resource:edit")
    public Result<Void> update(
            @PathVariable Long id,
            @RequestBody AdminResourceUpdateDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminPrimaryResourceService.update(id, dto, userId);
        return Result.success(null);
    }

    @PostMapping("/{id}/publish")
    @RequiresPermission("admin:resource:edit")
    @AdminLog(module = "resource", action = "publish", permission = "admin:resource:edit")
    public Result<Void> publish(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminPrimaryResourceService.publish(id, userId);
        return Result.success(null);
    }

    @PostMapping("/{id}/offline")
    @RequiresPermission("admin:resource:edit")
    @AdminLog(module = "resource", action = "offline", permission = "admin:resource:edit")
    public Result<Void> offline(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminPrimaryResourceService.offline(id, userId);
        return Result.success(null);
    }

    @PostMapping("/{id}/audit")
    @RequiresPermission("admin:audit:view")
    @AdminLog(module = "audit", action = "audit", permission = "admin:audit:approve")
    public Result<Void> audit(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String reason,
            @RequestHeader(value = "X-User-Id", required = false) Long auditorId,
            @RequestHeader(value = "X-User-Name", required = false) String auditorName) {
        adminPrimaryResourceService.auditResource(id, status, reason, auditorId, auditorName);
        return Result.success(null);
    }

    @PostMapping("/batch-audit")
    @RequiresPermission("admin:audit:view")
    @AdminLog(module = "audit", action = "batch_audit", permission = "admin:audit:approve")
    public Result<AdminResourceBatchResultVO> batchAudit(
            @RequestBody AdminResourceBatchAuditDTO body,
            @RequestHeader(value = "X-User-Id", required = false) Long auditorId,
            @RequestHeader(value = "X-User-Name", required = false) String auditorName) {
        return Result.success(adminPrimaryResourceService.batchAudit(
                body.getIds(), body.getAction(), body.getReason(), auditorId, auditorName));
    }

    @GetMapping("/{id}/audit-insights")
    @RequiresPermission("admin:audit:view")
    public Result<AdminResourceAuditInsightsVO> auditInsights(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminPrimaryResourceService.getAuditInsights(id, userId));
    }

    @PostMapping("/{id}/restore")
    @RequiresPermission("admin:resource:edit")
    @AdminLog(module = "resource", action = "restore", permission = "admin:resource:edit")
    public Result<Void> restore(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminPrimaryResourceService.restoreFromRecycle(id, userId);
        return Result.success(null);
    }

    @PostMapping("/{id}/recommend")
    @RequiresPermission("admin:resource:recommend")
    @AdminLog(module = "resource", action = "recommend", permission = "admin:resource:recommend")
    public Result<Void> recommend(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean enabled,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminPrimaryResourceService.setRecommend(id, enabled, userId);
        return Result.success(null);
    }

    @PostMapping("/{id}/top")
    @RequiresPermission("admin:resource:top")
    @AdminLog(module = "resource", action = "top", permission = "admin:resource:top")
    public Result<Void> top(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean enabled,
            @RequestParam(required = false) Integer topSort,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminPrimaryResourceService.setTop(id, enabled, topSort, userId);
        return Result.success(null);
    }

    @PostMapping("/batch")
    @RequiresPermission("admin:resource:batch")
    @AdminLog(module = "resource", action = "batch", permission = "admin:resource:batch")
    public Result<AdminResourceBatchResultVO> batch(
            @RequestBody AdminResourceBatchDTO body,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminPrimaryResourceService.batchAction(body.getIds(), body.getAction(), userId));
    }

    @DeleteMapping("/{id}")
    @RequiresPermission("admin:resource:delete")
    @AdminLog(module = "resource", action = "delete", permission = "admin:resource:delete")
    public Result<Void> delete(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminPrimaryResourceService.moveToRecycle(id, userId);
        return Result.success(null);
    }
}
