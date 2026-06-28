-- Phase 3I-A: 质量治理 - 旧接口调用看板菜单
-- mysql -u root -p xinketang < sql/92_phase3i_a_legacy_usage_menu.sql
USE `xinketang`;
SET NAMES utf8mb4;

INSERT INTO `sys_menu`
  (`id`, `parent_id`, `title`, `path`, `name`, `icon`, `component`, `permission_code`, `sort`, `hidden`, `status`)
SELECT
  106,
  q.id,
  '旧接口调用',
  '/admin/quality/legacy-api-usage',
  'AdminLegacyApiUsage',
  NULL,
  'admin/views/quality/LegacyApiUsage.vue',
  'admin:analytics:view',
  40,
  0,
  1
FROM `sys_menu` q
WHERE q.`name` = 'AdminQualityShell'
LIMIT 1
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `title` = VALUES(`title`),
  `path` = VALUES(`path`),
  `name` = VALUES(`name`),
  `component` = VALUES(`component`),
  `permission_code` = VALUES(`permission_code`),
  `sort` = VALUES(`sort`),
  `hidden` = 0,
  `status` = 1;

SELECT id, parent_id, title, path, permission_code, sort
FROM sys_menu
WHERE name = 'AdminLegacyApiUsage';
