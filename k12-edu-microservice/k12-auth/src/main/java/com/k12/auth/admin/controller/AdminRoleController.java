package com.k12.auth.admin.controller;

import com.k12.auth.admin.service.AdminRoleService;
import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.AdminPermissionVO;
import com.k12.common.dto.AdminRoleCreateDTO;
import com.k12.common.dto.AdminRoleUpdateDTO;
import com.k12.common.dto.AdminRoleVO;
import com.k12.common.dto.AdminUserRoleAssignDTO;
import com.k12.common.dto.RolePermissionAssignDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminRoleController {

    private final AdminRoleService adminRoleService;
    public AdminRoleController(AdminRoleService adminRoleService) {
        this.adminRoleService = adminRoleService;
    }


    @GetMapping("/roles")
    @RequiresPermission("admin:role:view")
    public Result<List<AdminRoleVO>> listRoles() {
        return Result.success(adminRoleService.listRoles());
    }

    @PostMapping("/roles")
    @RequiresPermission("admin:role:create")
    @AdminLog(module = "role", action = "create", permission = "admin:role:create")
    public Result<AdminRoleVO> createRole(@RequestBody AdminRoleCreateDTO dto) {
        return Result.success(adminRoleService.createRole(dto));
    }

    @PutMapping("/roles/{id}")
    @RequiresPermission("admin:role:edit")
    @AdminLog(module = "role", action = "update", permission = "admin:role:edit")
    public Result<AdminRoleVO> updateRole(@PathVariable Long id, @RequestBody AdminRoleUpdateDTO dto) {
        return Result.success(adminRoleService.updateRole(id, dto));
    }

    @GetMapping("/roles/{id}/permissions")
    @RequiresPermission("admin:role:view")
    public Result<List<Long>> getRolePermissions(@PathVariable Long id) {
        return Result.success(adminRoleService.getRolePermissionIds(id));
    }

    @PutMapping("/roles/{id}/permissions")
    @RequiresPermission("admin:role:assign_permission")
    @AdminLog(module = "role", action = "assign_permission", permission = "admin:role:assign_permission")
    public Result<Void> assignRolePermissions(@PathVariable Long id, @RequestBody RolePermissionAssignDTO dto) {
        adminRoleService.assignRolePermissions(id, dto);
        return Result.success(null);
    }

    /** 角色配置用权限树；当前用户权限码仍走 GET /api/admin/permissions（AdminAuthController） */
    @GetMapping("/permissions/tree")
    @RequiresPermission("admin:role:view")
    public Result<List<AdminPermissionVO>> listPermissionTree() {
        return Result.success(adminRoleService.listPermissionTree());
    }

    @PutMapping("/users/{id}/roles")
    @RequiresPermission("admin:admin_user:assign_role")
    @AdminLog(module = "user", action = "assign_admin_roles", permission = "admin:admin_user:assign_role")
    public Result<Void> assignUserRoles(
            @PathVariable Long id,
            @RequestBody AdminUserRoleAssignDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long operatorId) {
        adminRoleService.assignUserRoles(id, dto, operatorId);
        return Result.success(null);
    }

    @GetMapping("/users/{id}/roles")
    @RequiresPermission("admin:admin_user:assign_role")
    public Result<List<Long>> getUserRoles(@PathVariable Long id) {
        return Result.success(adminRoleService.getUserRoleIds(id));
    }
}
