package com.k12.resource.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.Result;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.ResourceCatalogAliasQueryDTO;
import com.k12.common.dto.ResourceCatalogAliasUpsertDTO;
import com.k12.common.entity.ResourceCatalogAlias;
import com.k12.resource.service.ResourceCatalogAliasService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Phase 3C：目录别名映射管理
 */
@RestController
@RequestMapping("/api/admin/resource-main")
public class AdminResourceCatalogAliasController {

    private final ResourceCatalogAliasService aliasService;

    public AdminResourceCatalogAliasController(ResourceCatalogAliasService aliasService) {
        this.aliasService = aliasService;
    }

    @GetMapping("/catalog-aliases")
    @RequiresPermission("admin:resource:view")
    public Result<Page<ResourceCatalogAlias>> list(
            ResourceCatalogAliasQueryDTO query,
            @RequestHeader(value = "X-User-Id", required = false) Long adminUserId) {
        return Result.success(aliasService.list(query, adminUserId));
    }

    @PostMapping("/catalog-aliases")
    @RequiresPermission("admin:resource:edit")
    public Result<ResourceCatalogAlias> upsert(
            @RequestBody ResourceCatalogAliasUpsertDTO body,
            @RequestHeader(value = "X-User-Id", required = false) Long adminUserId) {
        return Result.success(aliasService.upsert(body, adminUserId));
    }
}
