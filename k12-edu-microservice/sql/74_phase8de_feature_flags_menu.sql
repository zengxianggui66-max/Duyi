-- Phase 8-D/E: feature flags seed + system submenus
USE `xinketang`;
SET NAMES utf8mb4;

-- ---------- 1. 功能开关种子 ----------
INSERT IGNORE INTO `sys_config`
  (`config_key`, `config_value`, `value_type`, `group_code`, `description`, `requires_restart`) VALUES
('feature.taxonomyApi.enabled', 'true', 'bool', 'feature', '分类维度 API 切源', 0),
('feature.dictionaryApi.enabled', 'true', 'bool', 'feature', '字典标签 API 切源', 0),
('feature.catalogBrowse.enabled', 'false', 'bool', 'feature', '目录树浏览（buildTime）', 0),
('feature.masterWrite.enabled', 'false', 'bool', 'feature', '主表上传写入（buildTime）', 0),
('feature.searchEngineAuto.enabled', 'false', 'bool', 'feature', '搜索 searchEngine=auto', 0);

-- ---------- 2. 系统管理子菜单（parent_id=7 系统设置） ----------
UPDATE `sys_menu` SET
  `title` = '系统管理',
  `path` = '/admin/system/logs',
  `component` = 'admin/views/system/SystemConfigShell',
  `sort` = 70
WHERE `id` = 7;

INSERT INTO `sys_menu` (`id`, `parent_id`, `title`, `path`, `name`, `icon`, `component`, `permission_code`, `sort`, `hidden`, `status`) VALUES
(80, 7, '操作日志', '/admin/system/logs', 'AdminSystemLogs', 'Document', 'admin/views/system/SystemOperationLogs', 'admin:system:log_view', 1, 0, 1),
(81, 7, '登录日志', '/admin/system/login-logs', 'AdminSystemLoginLogs', 'User', 'admin/views/system/SystemLoginLogs', 'admin:system:log_view', 2, 0, 1),
(82, 7, '上传配置', '/admin/system/upload-config', 'AdminSystemUploadConfig', 'Upload', 'admin/views/system/SystemUploadConfig', 'admin:system:config_view', 3, 0, 1),
(83, 7, '预览配置', '/admin/system/preview-config', 'AdminSystemPreviewConfig', 'View', 'admin/views/system/SystemPreviewConfig', 'admin:system:config_view', 4, 0, 1),
(84, 7, '存储状态', '/admin/system/storage', 'AdminSystemStorage', 'FolderOpened', 'admin/views/system/SystemStorageStatus', 'admin:system:config_view', 5, 0, 1),
(85, 7, '功能开关', '/admin/system/feature-flags', 'AdminSystemFeatureFlags', 'Switch', 'admin/views/system/SystemFeatureFlags', 'admin:system:config_edit', 6, 0, 1)
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `title` = VALUES(`title`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `permission_code` = VALUES(`permission_code`),
  `sort` = VALUES(`sort`),
  `hidden` = 0,
  `status` = VALUES(`status`);

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:system:log_view', 'admin:system:config_view', 'admin:system:config_edit');

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 7, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:system:log_view', 'admin:system:config_view', 'admin:system:config_edit');
