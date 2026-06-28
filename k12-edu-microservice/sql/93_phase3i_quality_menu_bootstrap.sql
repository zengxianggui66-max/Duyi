-- Phase 3I: 质量治理 + 旧接口调用菜单补全（不依赖 85 是否已执行）
-- mysql -u root -p xinketang < sql/93_phase3i_quality_menu_bootstrap.sql
USE `xinketang`;
SET NAMES utf8mb4;

-- ---------- 1. 权限（质量 + 分析） ----------
INSERT INTO `sys_permission` (`code`, `name`, `type`, `module`, `parent_id`, `sort`, `status`) VALUES
('admin:quality:sensitive_view', '查看质量治理', 'menu',   'quality', 0, 80, 1),
('admin:quality:preview_fail',   '预览失败队列', 'menu',   'quality', 0, 81, 1),
('admin:analytics:view',         '查看数据分析', 'menu',   'analytics', 0, 71, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `type` = VALUES(`type`),
  `module` = VALUES(`module`),
  `sort` = VALUES(`sort`),
  `status` = 1;

-- super_admin + system_admin + content_admin + operator 可查看质量/分析
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `sys_role` r
CROSS JOIN `sys_permission` p
WHERE r.code IN ('super_admin', 'system_admin', 'content_admin', 'operator')
  AND p.code IN (
    'admin:quality:sensitive_view',
    'admin:quality:preview_fail',
    'admin:analytics:view'
  );

-- ---------- 2. 质量治理父级（挂在资源中心 94 下，permission 留空由子项裁剪） ----------
INSERT INTO `sys_menu`
  (`id`, `parent_id`, `title`, `path`, `name`, `icon`, `component`, `permission_code`, `sort`, `hidden`, `status`)
VALUES
  (110, 94, '质量治理', '/admin/quality', 'AdminQualityShell', 'Warning', NULL, NULL, 3, 0, 1)
ON DUPLICATE KEY UPDATE
  `parent_id` = 94,
  `title` = '质量治理',
  `path` = '/admin/quality',
  `name` = 'AdminQualityShell',
  `icon` = 'Warning',
  `component` = NULL,
  `permission_code` = NULL,
  `sort` = 3,
  `hidden` = 0,
  `status` = 1;

-- 若历史上已有 AdminQualityShell（非 id 110），统一到 94 下
UPDATE `sys_menu` SET
  `parent_id` = 94,
  `title` = '质量治理',
  `path` = '/admin/quality',
  `component` = NULL,
  `permission_code` = NULL,
  `sort` = 3,
  `hidden` = 0,
  `status` = 1
WHERE `name` = 'AdminQualityShell' AND `id` <> 110;

SET @quality_id = (SELECT id FROM `sys_menu` WHERE `name` = 'AdminQualityShell' LIMIT 1);

-- ---------- 3. 子菜单 ----------
INSERT INTO `sys_menu`
  (`id`, `parent_id`, `title`, `path`, `name`, `icon`, `component`, `permission_code`, `sort`, `hidden`, `status`)
VALUES
  (111, @quality_id, '质量大盘', '/admin/quality/dashboard', 'AdminQualityDashboard', NULL,
   'admin/views/quality/QualityDashboard.vue', 'admin:quality:sensitive_view', 10, 0, 1),
  (112, @quality_id, '敏感词库', '/admin/quality/sensitive-words', 'AdminSensitiveWords', NULL,
   'admin/views/quality/SensitiveWords.vue', 'admin:quality:sensitive_view', 20, 0, 1),
  (113, @quality_id, '预览失败', '/admin/quality/preview-fails', 'AdminPreviewFails', NULL,
   'admin/views/quality/PreviewFails.vue', 'admin:quality:preview_fail', 30, 0, 1),
  (106, @quality_id, '旧接口调用', '/admin/quality/legacy-api-usage', 'AdminLegacyApiUsage', NULL,
   'admin/views/quality/LegacyApiUsage.vue', 'admin:analytics:view', 40, 0, 1)
ON DUPLICATE KEY UPDATE
  `parent_id` = @quality_id,
  `title` = VALUES(`title`),
  `path` = VALUES(`path`),
  `name` = VALUES(`name`),
  `component` = VALUES(`component`),
  `permission_code` = VALUES(`permission_code`),
  `sort` = VALUES(`sort`),
  `hidden` = 0,
  `status` = 1;

-- 历史上 85 插入的子项（无固定 id）也挂到同一父级
UPDATE `sys_menu` SET `parent_id` = @quality_id, `path` = '/admin/quality/dashboard'
WHERE `name` = 'AdminQualityDashboard';
UPDATE `sys_menu` SET `parent_id` = @quality_id, `path` = '/admin/quality/sensitive-words'
WHERE `name` = 'AdminSensitiveWords';
UPDATE `sys_menu` SET `parent_id` = @quality_id, `path` = '/admin/quality/preview-fails'
WHERE `name` = 'AdminPreviewFails';
UPDATE `sys_menu` SET `parent_id` = @quality_id
WHERE `name` = 'AdminLegacyApiUsage';

-- ---------- 4. 校验 ----------
SELECT '=== 资源中心子菜单 ===' AS section;
SELECT id, parent_id, title, path, permission_code, sort
FROM sys_menu
WHERE parent_id = 94 AND hidden = 0
ORDER BY sort, id;

SELECT '=== 质量治理子菜单 ===' AS section;
SELECT id, parent_id, title, path, permission_code, sort
FROM sys_menu
WHERE parent_id = @quality_id AND hidden = 0
ORDER BY sort, id;

SELECT '=== admin 角色质量/分析权限 ===' AS section;
SELECT p.code
FROM sys_role r
JOIN sys_role_permission rp ON rp.role_id = r.id
JOIN sys_permission p ON p.id = rp.permission_id
WHERE r.code = 'super_admin'
  AND p.code IN ('admin:quality:sensitive_view', 'admin:quality:preview_fail', 'admin:analytics:view')
ORDER BY p.code;
