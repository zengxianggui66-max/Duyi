-- Admin sidebar menu reorder (Plan A): 6 top-level groups
-- 数据分析 → 资源中心 → 内容分类 → 运营管理 → 用户与权限 → 系统管理
-- mysql -u root -p xinketang < sql/91_admin_menu_reorder_plan_a.sql
USE `xinketang`;
SET NAMES utf8mb4;

-- ---------- 1. 新建四个分组父级（permission_code 留空：由子菜单权限裁剪） ----------
INSERT INTO `sys_menu`
  (`id`, `parent_id`, `title`, `path`, `name`, `icon`, `component`, `permission_code`, `sort`, `hidden`, `status`)
VALUES
  (94, 0, '资源中心', '/admin/resources', 'AdminResourceHub', 'FolderOpened', NULL, NULL, 20, 0, 1),
  (95, 0, '内容分类', '/admin/taxonomy', 'AdminContentTaxonomy', 'Collection', NULL, NULL, 30, 0, 1),
  (96, 0, '运营管理', '/admin/home-config/banners', 'AdminOpsHub', 'Promotion', NULL, NULL, 40, 0, 1),
  (97, 0, '用户与权限', '/admin/users', 'AdminUserRbac', 'User', NULL, NULL, 50, 0, 1)
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

-- ---------- 2. 一级分组 sort ----------
UPDATE `sys_menu` SET `sort` = 10 WHERE `id` = 9;  -- 数据分析
UPDATE `sys_menu` SET `sort` = 90 WHERE `id` = 7;  -- 系统管理

-- ---------- 3. 资源中心：资源管理 + 审核中心 ----------
UPDATE `sys_menu` SET
  `parent_id` = 94,
  `sort` = 1,
  `icon` = NULL
WHERE `id` = 2;

UPDATE `sys_menu` SET
  `parent_id` = 94,
  `sort` = 2,
  `path` = '/admin/audit',
  `name` = 'AdminAudit',
  `component` = NULL,
  `icon` = 'Checked'
WHERE `id` = 3;

-- 质量治理（若已部署 phase8 quality）归入资源中心
UPDATE `sys_menu` SET
  `parent_id` = 94,
  `sort` = 3,
  `icon` = NULL
WHERE `name` = 'AdminQualityShell';

-- ---------- 4. 内容分类：分类维度 + 目录/字典/标签 ----------
UPDATE `sys_menu` SET
  `parent_id` = 95,
  `title` = '分类维度',
  `sort` = 1,
  `icon` = NULL
WHERE `id` = 5;

UPDATE `sys_menu` SET `parent_id` = 95, `sort` = 2, `icon` = NULL WHERE `id` = 52;
UPDATE `sys_menu` SET `parent_id` = 95, `sort` = 3, `icon` = NULL WHERE `id` = 53;
UPDATE `sys_menu` SET `parent_id` = 95, `sort` = 4, `icon` = NULL WHERE `id` = 54;

-- ---------- 5. 运营管理：首页配置 + 搜索运营 ----------
UPDATE `sys_menu` SET
  `parent_id` = 96,
  `sort` = 1,
  `icon` = NULL
WHERE `id` = 6;

UPDATE `sys_menu` SET
  `parent_id` = 96,
  `sort` = 2,
  `path` = '/admin/search',
  `component` = NULL,
  `icon` = NULL
WHERE `id` = 8;

-- ---------- 6. 用户与权限：平台用户 + 管理员 + 角色 ----------
UPDATE `sys_menu` SET `parent_id` = 97, `sort` = 1, `icon` = NULL WHERE `id` = 4;
UPDATE `sys_menu` SET `parent_id` = 97, `sort` = 2, `icon` = NULL WHERE `id` = 55;
UPDATE `sys_menu` SET `parent_id` = 97, `sort` = 3, `icon` = NULL WHERE `id` = 57;

-- 隐藏路由仍挂在平台用户下
UPDATE `sys_menu` SET `parent_id` = 4 WHERE `id` = 56;

-- ---------- 7. 修复系统管理子菜单（78 与 74 曾复用 id 81-85） ----------
INSERT INTO `sys_menu`
  (`id`, `parent_id`, `title`, `path`, `name`, `icon`, `component`, `permission_code`, `sort`, `hidden`, `status`)
VALUES
  (100, 7, '操作日志', '/admin/system/logs', 'AdminSystemLogs', 'Document',
   'admin/views/system/SystemOperationLogs', 'admin:system:log_view', 1, 0, 1),
  (101, 7, '登录日志', '/admin/system/login-logs', 'AdminSystemLoginLogs', 'User',
   'admin/views/system/SystemLoginLogs', 'admin:system:log_view', 2, 0, 1),
  (102, 7, '上传配置', '/admin/system/upload-config', 'AdminSystemUploadConfig', 'Upload',
   'admin/views/system/SystemUploadConfig', 'admin:system:config_view', 3, 0, 1),
  (103, 7, '预览配置', '/admin/system/preview-config', 'AdminSystemPreviewConfig', 'View',
   'admin/views/system/SystemPreviewConfig', 'admin:system:config_view', 4, 0, 1),
  (104, 7, '存储状态', '/admin/system/storage', 'AdminSystemStorage', 'FolderOpened',
   'admin/views/system/SystemStorageStatus', 'admin:system:config_view', 5, 0, 1),
  (105, 7, '功能开关', '/admin/system/feature-flags', 'AdminSystemFeatureFlags', 'Switch',
   'admin/views/system/SystemFeatureFlags', 'admin:system:config_edit', 6, 0, 1)
ON DUPLICATE KEY UPDATE
  `parent_id` = 7,
  `title` = VALUES(`title`),
  `path` = VALUES(`path`),
  `name` = VALUES(`name`),
  `icon` = VALUES(`icon`),
  `component` = VALUES(`component`),
  `permission_code` = VALUES(`permission_code`),
  `sort` = VALUES(`sort`),
  `hidden` = 0,
  `status` = 1;

-- 旧 id 80 若仍为系统子项，隐藏避免与 100 重复
UPDATE `sys_menu` SET `hidden` = 1
WHERE `id` = 80 AND `parent_id` = 7 AND `path` = '/admin/system/logs';

-- ---------- 8. 校验 ----------
SELECT '=== top-level menus ===' AS section;
SELECT id, title, path, sort
FROM sys_menu
WHERE parent_id = 0 AND hidden = 0 AND status = 1
ORDER BY sort, id;

SELECT '=== resource hub children ===' AS section;
SELECT id, title, path, sort
FROM sys_menu
WHERE parent_id = 94 AND hidden = 0
ORDER BY sort, id;

SELECT '=== ops hub children ===' AS section;
SELECT id, title, path, sort
FROM sys_menu
WHERE parent_id = 96 AND hidden = 0
ORDER BY sort, id;

SELECT '=== system children ===' AS section;
SELECT id, title, path, sort
FROM sys_menu
WHERE parent_id = 7 AND hidden = 0
ORDER BY sort, id;
