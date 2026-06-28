-- Phase 6-A 权限 ID 冲突修复
-- 原因：sql/62 误用 id 28-30（已被 sql/54 资源 Phase3 占用），
--       ON DUPLICATE KEY 只更新 name/type，不会写入 admin:admin_user:view 等 code，
--       导致侧栏「管理员账号」菜单被权限过滤掉。
-- mysql -u root -p xinketang --default-character-set=utf8mb4 < sql/64_phase6a_perm_fix.sql
USE `xinketang`;
SET NAMES utf8mb4;

-- ---------- 1. 按 code 插入（勿占用 id 28-30） ----------
INSERT INTO `sys_permission` (`code`, `name`, `type`, `module`, `parent_id`, `sort`, `status`) VALUES
('admin:user:view_behavior',    '查看用户行为',   'button', 'user', 4, 44, 1),
('admin:admin_user:view',       '查看管理员账号', 'menu',   'user', 4, 45, 1),
('admin:admin_user:assign_role','分配后台角色',   'button', 'user', 4, 46, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `type` = VALUES(`type`),
  `module` = VALUES(`module`),
  `parent_id` = VALUES(`parent_id`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);

-- ---------- 2. 角色授权（按 code，不用硬编码 id） ----------
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:user:view_behavior', 'admin:admin_user:view', 'admin:admin_user:assign_role');

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 5, p.id FROM `sys_permission` p WHERE p.code = 'admin:user:view_behavior';

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 7, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:admin_user:view', 'admin:admin_user:assign_role');

-- 清理误绑：customer_service 不应拥有 sql/54 的资源推荐（若曾执行错误的 62 授权 id=28）
DELETE rp FROM `sys_role_permission` rp
JOIN `sys_permission` p ON p.id = rp.permission_id
WHERE rp.role_id = 5 AND p.code = 'admin:resource:recommend';

-- system_admin 误绑资源置顶
DELETE rp FROM `sys_role_permission` rp
JOIN `sys_permission` p ON p.id = rp.permission_id
WHERE rp.role_id = 7 AND p.code = 'admin:resource:top';

-- ---------- 3. 确保菜单存在 ----------
INSERT INTO `sys_menu` (`id`, `parent_id`, `title`, `path`, `name`, `icon`, `component`, `permission_code`, `sort`, `hidden`, `status`)
VALUES
(55, 0, '管理员账号', '/admin/admin-users', 'AdminStaffUsers', 'UserFilled',
 'admin/views/users/AdminUserList', 'admin:admin_user:view', 42, 0, 1)
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `permission_code` = VALUES(`permission_code`),
  `sort` = VALUES(`sort`),
  `hidden` = 0,
  `status` = VALUES(`status`);

UPDATE `sys_menu` SET
  `title` = '平台用户',
  `path` = '/admin/users',
  `component` = 'admin/views/users/UserList',
  `sort` = 41
WHERE `id` = 4;

-- ---------- 4. 校验 ----------
SELECT '=== Phase6 user permissions ===' AS section;
SELECT id, code, name FROM sys_permission
WHERE code IN ('admin:user:view_behavior', 'admin:admin_user:view', 'admin:admin_user:assign_role')
ORDER BY sort;

SELECT '=== admin-users menu ===' AS section;
SELECT id, title, path, permission_code, hidden, status FROM sys_menu WHERE id IN (4, 55);

SELECT '=== staff users ===' AS section;
SELECT id, username, nickname, role, status FROM `user` WHERE role = 'admin' AND deleted = 0;
