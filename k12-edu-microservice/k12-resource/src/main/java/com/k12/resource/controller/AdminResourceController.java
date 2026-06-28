package com.k12.resource.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.ResourceQueryDTO;
import com.k12.common.entity.Resource;
import com.k12.resource.service.AdminResourceService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @deprecated 请使用 {@link AdminResourcesController}（/api/admin/resources，主表 oss_primary_chinese_resource）
 */
@Deprecated(since = "3.6", forRemoval = false)
@RestController
@RequestMapping("/api/admin/resource")
public class AdminResourceController {

    private final AdminResourceService adminResourceService;
    public AdminResourceController(AdminResourceService adminResourceService) {
        this.adminResourceService = adminResourceService;
    }


    @Deprecated(since = "3.6", forRemoval = false)
    @GetMapping("/list")
    @RequiresPermission("admin:resource:view")
    public Result<Page<Resource>> list(
            ResourceQueryDTO query,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminResourceService.listResources(query, userId));
    }

    @Deprecated(since = "3.6", forRemoval = false)
    @PostMapping
    @RequiresPermission("admin:resource:create")
    @AdminLog(module = "resource", action = "create", permission = "admin:resource:create")
    public Result<Void> create(@RequestBody Resource resource) {
        adminResourceService.createResource(resource);
        return Result.success(null);
    }

    @Deprecated(since = "3.6", forRemoval = false)
    @PutMapping("/{id}")
    @RequiresPermission("admin:resource:edit")
    @AdminLog(module = "resource", action = "update", permission = "admin:resource:edit")
    public Result<Void> update(@PathVariable Long id, @RequestBody Resource resource) {
        resource.setId(id);
        adminResourceService.updateResource(resource);
        return Result.success(null);
    }

    @Deprecated(since = "3.6", forRemoval = false)
    @DeleteMapping("/{id}")
    @RequiresPermission("admin:resource:delete")
    @AdminLog(module = "resource", action = "delete", permission = "admin:resource:delete")
    public Result<Void> delete(@PathVariable Long id) {
        adminResourceService.deleteResource(id);
        return Result.success(null);
    }

    @Deprecated(since = "3.6", forRemoval = false)
    @GetMapping("/pending")
    @RequiresPermission("admin:audit:view")
    public Result<Page<Resource>> pendingList(
            ResourceQueryDTO query,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminResourceService.listPendingResources(query, userId));
    }

    @Deprecated(since = "3.6", forRemoval = false)
    @PostMapping("/{id}/audit")
    @AdminLog(module = "audit", action = "audit", permission = "admin:audit:approve")
    public Result<Void> audit(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String reason,
            @RequestHeader(value = "X-User-Id", required = false) Long auditorId,
            @RequestHeader(value = "X-User-Name", required = false) String auditorName) {
        adminResourceService.auditResource(id, status, reason, auditorId, auditorName);
        return Result.success(null);
    }

    @Deprecated(since = "3.6", forRemoval = false)
    @GetMapping("/stats")
    @RequiresPermission("admin:analytics:view")
    public Result<Map<String, Object>> stats() {
        return Result.success(adminResourceService.getResourceStats());
    }
}
