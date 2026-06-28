-- Phase 6-B：角色独立菜单 + content_admin Guard 验收权限
-- 前置：sql/64_phase6a_perm_fix.sql
-- mysql -u root -p xinketang --default-character-set=utf8mb4 < sql/65_admin_role_phase6b.sql
USE `xinketang`;
SET NAMES utf8mb4;

-- ---------- 1. 角色权限独立侧栏菜单 ----------
INSERT INTO `sys_menu` (`id`, `parent_id`, `title`, `path`, `name`, `icon`, `component`, `permission_code`, `sort`, `hidden`, `status`)
VALUES
(57, 0, '角色权限', '/admin/roles', 'AdminRoles', 'Key',
 'admin/views/roles/RoleList', 'admin:role:view', 43, 0, 1)
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `permission_code` = VALUES(`permission_code`),
  `sort` = VALUES(`sort`),
  `hidden` = 0,
  `status` = VALUES(`status`);

-- super_admin
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.id FROM `sys_permission` p WHERE p.code = 'admin:role:view';

-- system_admin 已有 admin:role:*，无需重复

-- ---------- 2. content_admin 验收 Guard：可进管理员账号页并尝试分配，但不可动 super_admin ----------
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 2, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:admin_user:view', 'admin:admin_user:assign_role', 'admin:user:edit', 'admin:user:reset_password');

-- ---------- 3. 校验 ----------
SELECT '=== 角色权限菜单 ===' AS section;
SELECT id, title, path, permission_code FROM sys_menu WHERE id = 57;

SELECT '=== content_admin 用户管理相关权限 ===' AS section;
SELECT p.code FROM sys_role r
JOIN sys_role_permission rp ON rp.role_id = r.id
JOIN sys_permission p ON p.id = rp.permission_id
WHERE r.code = 'content_admin' AND (p.code LIKE 'admin:%user%' OR p.code LIKE 'admin:admin_user%')
ORDER BY p.code;
