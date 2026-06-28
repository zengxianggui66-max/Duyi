-- ============================================================
-- 58 修复 Phase 4-B 审核权限 ID 冲突（57 用了 28-30，与 sql/54 资源权限冲突）
-- 现象：侧边栏只有「待审队列」，无「审核记录」「驳回模板」
-- mysql -u root -p xinketang --default-character-set=utf8mb4
-- source sql/58_admin_audit_phase4b_perm_fix.sql
-- ============================================================
USE xinketang;
SET NAMES utf8mb4;

-- 按 code 插入（避免与 sql/54 的 id 28-30 冲突）
INSERT INTO `sys_permission` (`code`, `name`, `type`, `module`, `parent_id`, `sort`, `status`) VALUES
('admin:audit:records',      '审核记录',     'menu',   'audit', 3, 33, 1),
('admin:audit:reasons',      '驳回模板管理', 'menu',   'audit', 3, 34, 1),
('admin:audit:reasons:edit', '编辑驳回模板', 'button', 'audit', 3, 35, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `type` = VALUES(`type`),
  `module` = VALUES(`module`),
  `parent_id` = VALUES(`parent_id`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);

-- super_admin：全部审核扩展权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:audit:records', 'admin:audit:reasons', 'admin:audit:reasons:edit');

-- auditor：审核记录 + 驳回模板只读
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 3, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:audit:records', 'admin:audit:reasons');

-- content_admin：含编辑驳回模板
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 2, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:audit:records', 'admin:audit:reasons', 'admin:audit:reasons:edit');

-- 清理误绑：auditor 不应拥有 sql/54 的资源推荐/置顶（若曾执行错误的 57 授权）
DELETE rp FROM `sys_role_permission` rp
JOIN `sys_permission` p ON p.id = rp.permission_id
WHERE rp.role_id = 3 AND p.code IN ('admin:resource:recommend', 'admin:resource:top');

SELECT '=== audit permissions ===' AS section;
SELECT id, code, name FROM sys_permission WHERE code LIKE 'admin:audit:%' ORDER BY sort;

SELECT '=== auditor audit perms ===' AS section;
SELECT p.code FROM sys_role r
JOIN sys_role_permission rp ON rp.role_id = r.id
JOIN sys_permission p ON p.id = rp.permission_id
WHERE r.code = 'auditor' AND p.code LIKE 'admin:audit%'
ORDER BY p.code;
