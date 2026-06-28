-- Phase 9 验收修复：auditor 隔离分析/控制台权限
-- 前置：sql/78_phase9e_search_analytics_rbac.sql
-- mysql -u root -p xinketang < sql/79_phase9_post_acceptance_fix.sql
USE `xinketang`;
SET NAMES utf8mb4;

-- auditor 仅负责审核，不应访问控制台/数据分析（旧 admin:dashboard:view 会绕过 analytics RBAC）
DELETE rp FROM `sys_role_permission` rp
INNER JOIN `sys_role` r ON r.id = rp.role_id
INNER JOIN `sys_permission` p ON p.id = rp.permission_id
WHERE r.code = 'auditor'
  AND p.code IN ('admin:dashboard:view', 'admin:analytics:view');

-- 同步修正菜单 91 组件路径（与前端路由 Dashboard.vue 一致）
UPDATE `sys_menu` SET
  `component` = 'admin/views/dashboard/Dashboard'
WHERE `id` = 91;

SELECT '=== auditor dashboard/analytics perms (expect 0) ===' AS section;
SELECT COUNT(*) AS cnt FROM sys_role r
JOIN sys_role_permission rp ON rp.role_id = r.id
JOIN sys_permission p ON p.id = rp.permission_id
WHERE r.code = 'auditor'
  AND p.code IN ('admin:dashboard:view', 'admin:analytics:view');

SELECT '=== auditor remaining perms ===' AS section;
SELECT p.code FROM sys_role r
JOIN sys_role_permission rp ON rp.role_id = r.id
JOIN sys_permission p ON p.id = rp.permission_id
WHERE r.code = 'auditor'
ORDER BY p.code;
