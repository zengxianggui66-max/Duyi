-- Phase 9-A: search ops admin permissions + menu
USE `xinketang`;
SET NAMES utf8mb4;

-- ---------- 1. 权限 ----------
INSERT INTO `sys_permission` (`code`, `name`, `type`, `module`, `parent_id`, `sort`, `status`) VALUES
('admin:search:view',    '查看搜索运营', 'menu',   'search', 0, 68, 1),
('admin:search:reindex', '重建搜索索引', 'button', 'search', 0, 69, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `type` = VALUES(`type`),
  `module` = VALUES(`module`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);

-- button 权限 parent_id 指向 menu 权限
UPDATE `sys_permission` p_btn
INNER JOIN `sys_permission` p_menu ON p_menu.code = 'admin:search:view'
SET p_btn.parent_id = p_menu.id
WHERE p_btn.code = 'admin:search:reindex';

-- ---------- 2. 侧栏菜单（顶级，sort 介于首页与系统管理之间） ----------
INSERT INTO `sys_menu` (`id`, `parent_id`, `title`, `path`, `name`, `icon`, `component`, `permission_code`, `sort`, `hidden`, `status`) VALUES
(8, 0, '搜索运营', '/admin/search/overview', 'AdminSearchOps', 'Search', 'admin/views/search/SearchOpsShell', 'admin:search:view', 67, 0, 1)
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `title` = VALUES(`title`),
  `path` = VALUES(`path`),
  `name` = VALUES(`name`),
  `icon` = VALUES(`icon`),
  `component` = VALUES(`component`),
  `permission_code` = VALUES(`permission_code`),
  `sort` = VALUES(`sort`),
  `hidden` = 0,
  `status` = VALUES(`status`);

-- ---------- 3. 角色授权 ----------
-- super_admin：全权限自动包含（INSERT IGNORE 补漏）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:search:view', 'admin:search:reindex');

-- operator：只读搜索运营
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 4, p.id FROM `sys_permission` p WHERE p.code = 'admin:search:view';

-- content_admin：查看 + 重建索引
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 2, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:search:view', 'admin:search:reindex');

-- system_admin：查看（索引重建由 content_admin / super_admin 负责）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 7, p.id FROM `sys_permission` p WHERE p.code = 'admin:search:view';

SELECT '=== search permissions ===' AS section;
SELECT code, name, type FROM sys_permission WHERE module = 'search' AND status = 1;

SELECT '=== operator search perms ===' AS section;
SELECT p.code FROM sys_role r
JOIN sys_role_permission rp ON rp.role_id = r.id
JOIN sys_permission p ON p.id = rp.permission_id
WHERE r.code = 'operator' AND p.module = 'search';
