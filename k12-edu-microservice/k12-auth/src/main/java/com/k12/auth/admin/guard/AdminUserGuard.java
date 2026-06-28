package com.k12.auth.admin.guard;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.auth.admin.entity.SysRole;
import com.k12.auth.admin.mapper.AdminRoleMapper;
import com.k12.auth.admin.mapper.AdminUserRoleMapper;
import com.k12.auth.mapper.UserMapper;
import com.k12.common.BusinessException;
import com.k12.common.entity.User;
import com.k12.common.mapper.SharedAdminRbacMapper;
import com.k12.common.service.AdminPermissionService;
import com.k12.common.util.RoleUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Phase 6-B：后台 staff 账号保护（super_admin 不可被非 super_admin 操作）
 */
@Component
public class AdminUserGuard {

    private final SharedAdminRbacMapper sharedAdminRbacMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final UserMapper userMapper;
    public AdminUserGuard(SharedAdminRbacMapper sharedAdminRbacMapper, AdminUserRoleMapper adminUserRoleMapper, AdminRoleMapper adminRoleMapper, UserMapper userMapper) {
        this.sharedAdminRbacMapper = sharedAdminRbacMapper;
        this.adminUserRoleMapper = adminUserRoleMapper;
        this.adminRoleMapper = adminRoleMapper;
        this.userMapper = userMapper;
    }


    public boolean isSuperAdmin(Long userId) {
        return userId != null && sharedAdminRbacMapper.countSuperAdminRole(userId) > 0;
    }

    public boolean targetHasSuperAdmin(Long targetUserId) {
        return isSuperAdmin(targetUserId);
    }

    /**
     * 启停 / 重置密码等账号级操作
     */
    public void assertCanManageStaffAccount(Long operatorId, Long targetUserId) {
        User target = requireStaffUser(targetUserId);
        if (!targetHasSuperAdmin(target.getId())) {
            return;
        }
        if (!isSuperAdmin(operatorId)) {
            throw new BusinessException(403, "仅超级管理员可管理 super_admin 账号");
        }
    }

    /**
     * 分配后台 sys_role
     */
    public void assertCanAssignRoles(Long operatorId, Long targetUserId, List<Long> newRoleIds) {
        User target = requireStaffUser(targetUserId);
        Long superAdminRoleId = requireSuperAdminRoleId();
        List<Long> roleIds = newRoleIds != null ? newRoleIds : Collections.emptyList();

        List<Long> currentRoleIds = adminUserRoleMapper.selectRoleIdsByUserId(target.getId());
        boolean targetIsSuperAdmin = currentRoleIds.contains(superAdminRoleId);
        boolean newIncludesSuperAdmin = roleIds.contains(superAdminRoleId);
        boolean operatorIsSuperAdmin = isSuperAdmin(operatorId);

        if (targetIsSuperAdmin && !operatorIsSuperAdmin) {
            throw new BusinessException(403, "仅超级管理员可修改 super_admin 账号的后台角色");
        }

        if (newIncludesSuperAdmin && !operatorIsSuperAdmin) {
            throw new BusinessException(403, "仅超级管理员可分配 super_admin 角色");
        }

        if (targetIsSuperAdmin && !newIncludesSuperAdmin) {
            if (!operatorIsSuperAdmin) {
                throw new BusinessException(403, "仅超级管理员可移除 super_admin 角色");
            }
            if (sharedAdminRbacMapper.countActiveSuperAdminUsers() <= 1) {
                throw new BusinessException(400, "不可移除最后一个超级管理员");
            }
        }
    }

    private User requireStaffUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!RoleUtils.isAdmin(user.getRole())) {
            throw new BusinessException(400, "仅 staff 账号（user.role=admin）可在此管理");
        }
        return user;
    }

    private Long requireSuperAdminRoleId() {
        SysRole role = adminRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getCode, AdminPermissionService.SUPER_ADMIN)
                .last("LIMIT 1"));
        if (role == null) {
            throw new BusinessException(500, "未找到 super_admin 角色配置");
        }
        return role.getId();
    }
}
