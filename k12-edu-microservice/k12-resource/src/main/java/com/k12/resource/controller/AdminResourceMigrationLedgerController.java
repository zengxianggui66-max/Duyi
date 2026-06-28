package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.ResourceMigrationLedgerUpdateDTO;
import com.k12.common.entity.ResourceMigrationLedger;
import com.k12.resource.service.AdminResourceMigrationLedgerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Phase 3-P0: 资源来源治理台账
 */
@RestController
@RequestMapping("/api/admin/resource-main")
public class AdminResourceMigrationLedgerController {

    private final AdminResourceMigrationLedgerService ledgerService;

    public AdminResourceMigrationLedgerController(AdminResourceMigrationLedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/migration-ledger")
    @RequiresPermission("admin:resource:view")
    public Result<List<ResourceMigrationLedger>> list(
            @RequestHeader(value = "X-User-Id", required = false) Long adminUserId) {
        return Result.success(ledgerService.list(adminUserId));
    }

    @PutMapping("/migration-ledger/{sourceType}")
    @RequiresPermission("admin:resource:edit")
    public Result<ResourceMigrationLedger> upsert(
            @PathVariable String sourceType,
            @RequestBody ResourceMigrationLedgerUpdateDTO body,
            @RequestHeader(value = "X-User-Id", required = false) Long adminUserId) {
        return Result.success(ledgerService.upsert(sourceType, body, adminUserId));
    }
}
