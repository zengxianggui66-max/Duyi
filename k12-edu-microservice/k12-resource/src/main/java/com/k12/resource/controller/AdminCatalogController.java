package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.AdminCatalogNodeWriteDTO;
import com.k12.common.dto.CatalogNodeVO;
import com.k12.common.dto.CatalogSchemeVO;
import com.k12.common.entity.EduCatalogNode;
import com.k12.resource.service.AdminCatalogService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Phase 5-C：教材目录树管理端 CRUD
 */
@RestController
@RequestMapping("/api/admin/catalog")
public class AdminCatalogController {

    private final AdminCatalogService adminCatalogService;
    public AdminCatalogController(AdminCatalogService adminCatalogService) {
        this.adminCatalogService = adminCatalogService;
    }


    @GetMapping("/schemes")
    @RequiresPermission("admin:taxonomy:view")
    public Result<List<CatalogSchemeVO>> listSchemes() {
        return Result.success(adminCatalogService.listSchemes());
    }

    @GetMapping("/tree")
    @RequiresPermission("admin:taxonomy:view")
    public Result<List<CatalogNodeVO>> getTree(
            @RequestParam(required = false) Integer schemeId,
            @RequestParam(required = false) String schemeCode,
            @RequestParam(required = false) String volumeKey,
            @RequestParam(required = false) String gradeName,
            @RequestParam(required = false) String subject,
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(adminCatalogService.getAdminTree(
                schemeId, schemeCode, volumeKey, gradeName, subject, includeDisabled));
    }

    @GetMapping("/nodes")
    @RequiresPermission("admin:taxonomy:view")
    public Result<List<com.k12.common.dto.AdminCatalogNodeAdminVO>> listNodes(
            @RequestParam Integer schemeId,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) String volumeKey,
            @RequestParam(defaultValue = "true") boolean includeDisabled) {
        return Result.success(adminCatalogService.listNodesFlat(schemeId, parentId, volumeKey, includeDisabled));
    }

    @PostMapping("/nodes")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "catalog", action = "create_node", permission = "admin:taxonomy:edit")
    public Result<EduCatalogNode> createNode(
            @Valid @RequestBody AdminCatalogNodeWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminCatalogService.createNode(dto, userId));
    }

    @PutMapping("/nodes/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "catalog", action = "update_node", permission = "admin:taxonomy:edit")
    public Result<EduCatalogNode> updateNode(
            @PathVariable Long id,
            @Valid @RequestBody AdminCatalogNodeWriteDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(adminCatalogService.updateNode(id, dto, userId));
    }

    @PutMapping("/nodes/{id}/status")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "catalog", action = "toggle_node", permission = "admin:taxonomy:edit")
    public Result<Void> setNodeStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminCatalogService.setNodeStatus(id, status, userId);
        return Result.success(null);
    }

    @DeleteMapping("/nodes/{id}")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "catalog", action = "delete_node", permission = "admin:taxonomy:edit")
    public Result<Void> deleteNode(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        adminCatalogService.deleteNode(id, userId);
        return Result.success(null);
    }

    @PostMapping("/import-unit-json")
    @RequiresPermission("admin:taxonomy:edit")
    @AdminLog(module = "catalog", action = "import_unit_json", permission = "admin:taxonomy:edit")
    public Result<Map<String, Object>> importUnitJson(
            @RequestParam String volumeKey,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        int count = adminCatalogService.importVolumeFromStaticJson(volumeKey, userId);
        return Result.success(Map.of("volumeKey", volumeKey, "importedCount", count));
    }
}
