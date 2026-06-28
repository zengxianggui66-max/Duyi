package com.k12.auth.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.auth.admin.service.AdminOperationLogService;
import com.k12.auth.admin.service.AdminUserRemarkService;
import com.k12.auth.admin.service.AdminUserScopeService;
import com.k12.auth.admin.service.AdminUserService;
import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.AdminUserBatchStatusDTO;
import com.k12.common.dto.AdminUserDetailVO;
import com.k12.common.dto.AdminUserListVO;
import com.k12.common.dto.AdminUserRemarkCreateDTO;
import com.k12.common.dto.AdminUserRemarkVO;
import com.k12.common.dto.AdminUserUpdateDTO;
import com.k12.common.dto.AdminOperationLogVO;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Phase 6-A/D：管理端用户（平台用户 + staff 查询）
 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final AdminUserRemarkService adminUserRemarkService;
    private final AdminOperationLogService adminOperationLogService;
    private final AdminUserScopeService adminUserScopeService;
    public AdminUserController(AdminUserService adminUserService, AdminUserRemarkService adminUserRemarkService, AdminOperationLogService adminOperationLogService, AdminUserScopeService adminUserScopeService) {
        this.adminUserService = adminUserService;
        this.adminUserRemarkService = adminUserRemarkService;
        this.adminOperationLogService = adminOperationLogService;
        this.adminUserScopeService = adminUserScopeService;
    }


    private static final DateTimeFormatter CSV_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping
    @RequiresPermission("admin:user:view")
    public Result<Page<AdminUserListVO>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String portalRole,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Boolean staffOnly,
            @RequestHeader(value = "X-User-Id", required = false) Long operatorId) {
        return Result.success(adminUserService.listUsers(
                current, size, keyword, portalRole, status, staffOnly, operatorId));
    }

    @GetMapping("/export")
    @RequiresPermission("admin:user:export")
    @AdminLog(module = "user", action = "export", permission = "admin:user:export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String portalRole,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Boolean staffOnly,
            @RequestHeader(value = "X-User-Id", required = false) Long operatorId) {
        List<AdminUserListVO> rows = adminUserService.listUsersForExport(
                keyword, portalRole, status, staffOnly, operatorId);
        StringBuilder sb = new StringBuilder();
        sb.append("id,username,nickname,phone,portalRole,status,registerFrom,createTime,lastLoginTime\n");
        for (AdminUserListVO row : rows) {
            sb.append(row.getId()).append(',')
                    .append(csv(row.getUsername())).append(',')
                    .append(csv(row.getNickname())).append(',')
                    .append(csv(row.getPhoneMasked())).append(',')
                    .append(csv(row.getPortalRoleName() != null ? row.getPortalRoleName() : row.getPortalRole())).append(',')
                    .append(row.getStatus() != null && row.getStatus() == 1 ? "正常" : "禁用").append(',')
                    .append(csv(row.getRegisterFrom())).append(',')
                    .append(csv(row.getCreateTime() != null ? CSV_TIME.format(row.getCreateTime()) : "")).append(',')
                    .append(csv(row.getLastLoginTime() != null ? CSV_TIME.format(row.getLastLoginTime()) : ""))
                    .append('\n');
        }
        byte[] body = sb.toString().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"users-export.csv\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(body);
    }

    @GetMapping("/{id}")
    @RequiresPermission("admin:user:view")
    public Result<AdminUserDetailVO> detail(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long operatorId) {
        return Result.success(adminUserService.getUserDetail(id, operatorId));
    }

    @PutMapping("/{id}")
    @RequiresPermission("admin:user:edit")
    @AdminLog(module = "user", action = "update", permission = "admin:user:edit")
    public Result<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long operatorId) {
        adminUserService.updateUser(id, dto, operatorId);
        return Result.success(null);
    }

    @PostMapping("/{id}/disable")
    @RequiresPermission("admin:user:edit")
    @AdminLog(module = "user", action = "disable", permission = "admin:user:edit")
    public Result<Void> disable(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long operatorId) {
        adminUserService.disableUser(id, operatorId);
        return Result.success(null);
    }

    @PostMapping("/{id}/enable")
    @RequiresPermission("admin:user:edit")
    @AdminLog(module = "user", action = "enable", permission = "admin:user:edit")
    public Result<Void> enable(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long operatorId) {
        adminUserService.enableUser(id, operatorId);
        return Result.success(null);
    }

    @PostMapping("/{id}/reset-password")
    @RequiresPermission("admin:user:reset_password")
    @AdminLog(module = "user", action = "reset_password", permission = "admin:user:reset_password")
    public Result<String> resetPassword(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long operatorId) {
        return Result.success(adminUserService.resetPassword(id, operatorId));
    }

    /** 兼容旧接口 */
    @PutMapping("/{id}/status")
    @RequiresPermission("admin:user:edit")
    @AdminLog(module = "user", action = "update_status", permission = "admin:user:edit")
    public Result<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestHeader(value = "X-User-Id", required = false) Long operatorId) {
        if (status != null && status == 0) {
            adminUserService.disableUser(id, operatorId);
        } else {
            adminUserService.enableUser(id, operatorId);
        }
        return Result.success(null);
    }

    @PostMapping("/batch-status")
    @RequiresPermission("admin:user:edit")
    @AdminLog(module = "user", action = "batch_status", permission = "admin:user:edit")
    public Result<Integer> batchStatus(
            @Valid @RequestBody AdminUserBatchStatusDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long operatorId) {
        return Result.success(adminUserService.batchUpdateStatus(dto, operatorId));
    }

    @GetMapping("/{id}/remarks")
    @RequiresPermission("admin:user:view")
    public Result<Page<AdminUserRemarkVO>> remarks(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-User-Id", required = false) Long operatorId) {
        return Result.success(adminUserRemarkService.listRemarks(id, current, size, operatorId));
    }

    @PostMapping("/{id}/remarks")
    @RequiresPermission("admin:user:remark")
    @AdminLog(module = "user", action = "add_remark", permission = "admin:user:remark")
    public Result<AdminUserRemarkVO> addRemark(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserRemarkCreateDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long operatorId,
            @RequestHeader(value = "X-Username", required = false) String operatorName) {
        return Result.success(adminUserRemarkService.addRemark(id, dto, operatorId, operatorName));
    }

    @GetMapping("/{id}/operation-logs")
    @RequiresPermission("admin:user:view")
    public Result<Page<AdminOperationLogVO>> operationLogs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-User-Id", required = false) Long operatorId) {
        adminUserScopeService.assertCanViewUser(operatorId, id);
        return Result.success(adminOperationLogService.listUserTargetLogs(id, current, size));
    }

    private static String csv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
