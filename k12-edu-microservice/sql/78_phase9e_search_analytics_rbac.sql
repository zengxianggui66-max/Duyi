-- Phase 9-E: 搜索运营 + 数据分析权限、菜单与角色映射
-- 前置：sql/75_phase9a_search_ops.sql, sql/76_phase9b_search_lexicon.sql
-- mysql -u root -p xinketang < sql/78_phase9e_search_analytics_rbac.sql
USE `xinketang`;
SET NAMES utf8mb4;

-- ---------- 1. 权限（搜索权限 75/76 已存在，补 analytics） ----------
INSERT INTO `sys_permission` (`code`, `name`, `type`, `module`, `parent_id`, `sort`, `status`) VALUES
('admin:analytics:view', '查看数据分析', 'menu', 'analytics', 0, 71, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `type` = VALUES(`type`),
  `module` = VALUES(`module`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);

-- 确保搜索 button 权限 parent 正确
UPDATE `sys_permission` p_btn
INNER JOIN `sys_permission` p_menu ON p_menu.code = 'admin:search:view'
SET p_btn.parent_id = p_menu.id
WHERE p_btn.code IN ('admin:search:reindex', 'admin:search:edit');

-- ---------- 2. 搜索运营：父级 + 子菜单 ----------
UPDATE `sys_menu` SET
  `title` = '搜索运营',
  `path` = '/admin/search',
  `name` = 'AdminSearchOps',
  `icon` = 'Search',
  `component` = NULL,
  `permission_code` = 'admin:search:view',
  `sort` = 67,
  `hidden` = 0,
  `status` = 1
WHERE `id` = 8;

INSERT INTO `sys_menu` (`id`, `parent_id`, `title`, `path`, `name`, `icon`, `component`, `permission_code`, `sort`, `hidden`, `status`) VALUES
(81, 8, '搜索概览',     '/admin/search/overview',    'AdminSearchOverview',    NULL, 'admin/views/search/SearchOverview',       'admin:search:view', 1, 0, 1),
(82, 8, '无结果词',     '/admin/search/no-results',  'AdminSearchNoResults',   NULL, 'admin/views/search/SearchNoResults',      'admin:search:view', 2, 0, 1),
(83, 8, '同义词库',     '/admin/search/synonyms',    'AdminSearchSynonyms',    NULL, 'admin/views/search/SearchSynonyms',       'admin:search:view', 3, 0, 1),
(84, 8, '搜索重定向',   '/admin/search/redirects',   'AdminSearchRedirects',   NULL, 'admin/views/search/SearchRedirects',      'admin:search:view', 4, 0, 1),
(85, 8, '搜索热词',     '/admin/search/hot-keywords','AdminSearchHotKeywords', NULL, 'admin/views/search/SearchHotKeywords',    'admin:search:view', 5, 0, 1),
(86, 8, '索引状态',     '/admin/search/index',       'AdminSearchIndex',       NULL, 'admin/views/search/SearchIndexStatus',    'admin:search:view', 6, 0, 1),
(87, 8, '意图规则',     '/admin/search/intent-rules','AdminSearchIntentRules', NULL, 'admin/views/search/SearchIntentRules',    'admin:search:view', 7, 1, 1)
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `title` = VALUES(`title`),
  `path` = VALUES(`path`),
  `name` = VALUES(`name`),
  `component` = VALUES(`component`),
  `permission_code` = VALUES(`permission_code`),
  `sort` = VALUES(`sort`),
  `hidden` = VALUES(`hidden`),
  `status` = VALUES(`status`);

-- ---------- 3. 数据分析：父级 + 子菜单 ----------
INSERT INTO `sys_menu` (`id`, `parent_id`, `title`, `path`, `name`, `icon`, `component`, `permission_code`, `sort`, `hidden`, `status`) VALUES
(9, 0, '数据分析', '/admin/analytics', 'AdminAnalytics', 'DataAnalysis', NULL, 'admin:analytics:view', 68, 0, 1),
(91, 9, '运营概览', '/admin/analytics/overview', 'AdminAnalyticsOverview', NULL, 'admin/views/dashboard/Dashboard', 'admin:analytics:view', 1, 0, 1),
(92, 9, '资源分析', '/admin/analytics/resources', 'AdminAnalyticsResources', NULL, 'admin/views/analytics/AnalyticsResources', 'admin:analytics:view', 2, 0, 1),
(93, 9, '用户与行为', '/admin/analytics/users', 'AdminAnalyticsUsers', NULL, 'admin/views/analytics/AnalyticsUsers', 'admin:analytics:view', 3, 0, 1)
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `title` = VALUES(`title`),
  `path` = VALUES(`path`),
  `name` = VALUES(`name`),
  `icon` = VALUES(`icon`),
  `component` = VALUES(`component`),
  `permission_code` = VALUES(`permission_code`),
  `sort` = VALUES(`sort`),
  `hidden` = VALUES(`hidden`),
  `status` = VALUES(`status`);

-- 旧「控制台」菜单隐藏（路由保留兼容，侧栏改走数据分析）
UPDATE `sys_menu` SET
  `title` = '控制台（已迁移）',
  `hidden` = 1,
  `sort` = 0
WHERE `id` = 1;

-- finance_admin 角色标记废弃（不做会员模块）
UPDATE `sys_role` SET
  `name` = '会员/订单（已废弃）',
  `description` = '预留角色，当前版本不做会员/订单模块，请勿分配'
WHERE `code` = 'finance_admin';

-- ---------- 4. 角色-权限映射 ----------
-- super_admin：补漏新权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:search:view', 'admin:search:edit', 'admin:search:reindex', 'admin:analytics:view');

-- operator：搜索看板 + 热词 + 词典 + 分析只读
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 4, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:search:view', 'admin:analytics:view');

-- content_admin：+ 同义词/重定向编辑 + 索引重建
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 2, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:search:view', 'admin:search:edit', 'admin:search:reindex', 'admin:analytics:view');

-- system_admin：搜索 + 分析查看（索引重建由 content_admin / super_admin）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 7, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:search:view', 'admin:analytics:view');

-- finance_admin：确保无搜索/分析权限
DELETE rp FROM `sys_role_permission` rp
INNER JOIN `sys_permission` p ON p.id = rp.permission_id
INNER JOIN `sys_role` r ON r.id = rp.role_id
WHERE r.code = 'finance_admin'
  AND p.code IN ('admin:search:view', 'admin:search:edit', 'admin:search:reindex', 'admin:analytics:view');

-- auditor：不应持有控制台/分析权限（避免绕过 analytics RBAC）
DELETE rp FROM `sys_role_permission` rp
INNER JOIN `sys_permission` p ON p.id = rp.permission_id
INNER JOIN `sys_role` r ON r.id = rp.role_id
WHERE r.code = 'auditor'
  AND p.code IN ('admin:dashboard:view', 'admin:analytics:view');

-- ---------- 5. 测试账号 operator（验收用，可重复执行） ----------
SET @op_pwd := '$2a$10$MZlQWDC7/eI7dkbXopadk.w/YgwU6lHU/lIRUK5SardpKvboOqlMu';
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`, `deleted`)
SELECT 'operator', @op_pwd, '运营人员', 'admin', 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'operator');
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id FROM `user` u JOIN `sys_role` r ON r.code = 'operator'
WHERE u.username = 'operator';

-- ---------- 6. 校验 ----------
SELECT '=== search & analytics permissions ===' AS section;
SELECT code, name, type, module FROM sys_permission
WHERE module IN ('search', 'analytics') AND status = 1
ORDER BY sort;

SELECT '=== search menus ===' AS section;
SELECT id, parent_id, title, path, hidden FROM sys_menu WHERE id = 8 OR parent_id = 8 ORDER BY sort;

SELECT '=== analytics menus ===' AS section;
SELECT id, parent_id, title, path FROM sys_menu WHERE id = 9 OR parent_id = 9 ORDER BY sort;

SELECT '=== operator perms ===' AS section;
SELECT p.code FROM sys_role r
JOIN sys_role_permission rp ON rp.role_id = r.id
JOIN sys_permission p ON p.id = rp.permission_id
WHERE r.code = 'operator' AND p.module IN ('search', 'analytics')
ORDER BY p.code;

SELECT '=== content_admin search/analytics ===' AS section;
SELECT p.code FROM sys_role r
JOIN sys_role_permission rp ON rp.role_id = r.id
JOIN sys_permission p ON p.id = rp.permission_id
WHERE r.code = 'content_admin' AND p.module IN ('search', 'analytics')
ORDER BY p.code;

SELECT '=== finance_admin search/analytics (expect 0) ===' AS section;
SELECT COUNT(*) AS cnt FROM sys_role r
JOIN sys_role_permission rp ON rp.role_id = r.id
JOIN sys_permission p ON p.id = rp.permission_id
WHERE r.code = 'finance_admin' AND p.module IN ('search', 'analytics');
