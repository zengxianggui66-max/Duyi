package com.k12.common.util;

import com.k12.common.BusinessException;
import org.springframework.util.StringUtils;

import java.util.Set;

public final class RoleUtils {

    /** 注册默认身份：待管理员分配 */
    public static final String DEFAULT_REGISTER_ROLE = "pending";

    public static final Set<String> REGISTER_ROLES = Set.of("teacher", "student", "parent");

    /** 管理员可分配的身份（不含 admin） */
    public static final Set<String> ADMIN_ASSIGNABLE_ROLES = Set.of("teacher", "student", "parent", DEFAULT_REGISTER_ROLE);

    /** 管理端 staff 入口身份（C 端 user.role 字段） */
    public static final String STAFF_ROLE = "admin";

    private RoleUtils() {
    }

    public static String resolveRegisterRole(String role) {
        if (!StringUtils.hasText(role)) {
            return DEFAULT_REGISTER_ROLE;
        }
        if (!REGISTER_ROLES.contains(role)) {
            throw new BusinessException(400, "身份参数无效");
        }
        return role;
    }

    public static void requireAdminAssignableRole(String role) {
        if (!StringUtils.hasText(role) || !ADMIN_ASSIGNABLE_ROLES.contains(role)) {
            throw new BusinessException(400, "请选择有效的身份：教师、学生、家长或待分配");
        }
    }

    /** 是否为管理端 staff（user.role=admin） */
    public static boolean isAdmin(String role) {
        return STAFF_ROLE.equals(role);
    }

    /** @deprecated 使用 {@link #isAdmin(String)} 或 {@link #isStaff(String)} */
    public static boolean isStaff(String role) {
        return isAdmin(role);
    }

    public static void requireAdminRole(String role) {
        requireStaffRole(role);
    }

    public static void requireStaffRole(String role) {
        if (!isAdmin(role)) {
            throw new BusinessException(403, "需要管理员权限");
        }
    }

    public static String getRoleName(String role) {
        if (role == null) {
            return "未知";
        }
        return switch (role) {
            case STAFF_ROLE -> "管理员";
            case "teacher" -> "教师";
            case "student" -> "学生";
            case "parent" -> "家长";
            case DEFAULT_REGISTER_ROLE -> "待分配";
            default -> "用户";
        };
    }
}
