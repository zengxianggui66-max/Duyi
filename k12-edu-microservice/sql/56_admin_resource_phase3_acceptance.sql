-- ============================================================
-- 56 管理端资源治理 Phase 3-6 验收补丁
-- content_admin 补 batch/recommend/top 权限；校验 RBAC
-- mysql -u root -p xinketang --default-character-set=utf8mb4
-- source sql/56_admin_resource_phase3_acceptance.sql
-- ============================================================
USE xinketang;
SET NAMES utf8mb4;

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(2, 28), (2, 29), (2, 30);

SELECT '=== content_admin 资源相关权限 ===' AS section;
SELECT p.code, p.name
FROM sys_role r
JOIN sys_role_permission rp ON rp.role_id = r.id
JOIN sys_permission p ON p.id = rp.permission_id
WHERE r.code = 'content_admin' AND p.module IN ('resource', 'dashboard')
ORDER BY p.sort;

SELECT '=== auditor 菜单权限（应无 resource:view）===' AS section;
SELECT p.code
FROM sys_role r
JOIN sys_role_permission rp ON rp.role_id = r.id
JOIN sys_permission p ON p.id = rp.permission_id
WHERE r.code = 'auditor'
ORDER BY p.sort;

SELECT '=== 主表待审 vs 统计 ===' AS section;
SELECT
  (SELECT COUNT(*) FROM oss_primary_chinese_resource WHERE is_deleted = 0 AND status != 4) AS total_active,
  (SELECT COUNT(*) FROM oss_primary_chinese_resource WHERE is_deleted = 0 AND status = 0) AS pending_count,
  (SELECT COUNT(*) FROM oss_primary_chinese_resource WHERE is_deleted = 0 AND status = 1) AS published_count;
