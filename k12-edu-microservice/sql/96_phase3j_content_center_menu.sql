-- Phase 3J-2: 内容中心 Admin 菜单 + 权限
-- 部署顺序：89 → 91 → 93 → 94 → 95 → 96
-- mysql -u root -p xinketang < sql/96_phase3j_content_center_menu.sql
USE `xinketang`;
SET NAMES utf8mb4;

-- ---------- 1. 权限 ----------
INSERT INTO `sys_permission` (`code`, `name`, `type`, `module`, `parent_id`, `sort`, `status`) VALUES
('admin:content:view', '查看内容中心', 'menu', 'content', 0, 72, 1),
('admin:content:edit', '编辑内容中心', 'menu', 'content', 0, 73, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `type` = VALUES(`type`),
  `module` = VALUES(`module`),
  `sort` = VALUES(`sort`),
  `status` = 1;

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id
FROM `sys_role` r
CROSS JOIN `sys_permission` p
WHERE r.code IN ('super_admin', 'system_admin', 'content_admin', 'operator')
  AND p.code IN ('admin:content:view', 'admin:content:edit');

-- ---------- 2. 内容中心父级（运营管理 id=96 下） ----------
INSERT INTO `sys_menu`
  (`id`, `parent_id`, `title`, `path`, `name`, `icon`, `component`, `permission_code`, `sort`, `hidden`, `status`)
VALUES
  (120, 96, '内容中心', '/admin/content/topic', 'AdminContentShell', 'Document', NULL, NULL, 2, 0, 1)
ON DUPLICATE KEY UPDATE
  `parent_id` = 96,
  `title` = '内容中心',
  `path` = '/admin/content/topic',
  `name` = 'AdminContentShell',
  `icon` = 'Document',
  `component` = NULL,
  `permission_code` = NULL,
  `sort` = 2,
  `hidden` = 0,
  `status` = 1;

-- 搜索运营 sort 后移
UPDATE `sys_menu` SET `sort` = 3 WHERE `id` = 8 AND `parent_id` = 96;

SET @content_id = (SELECT id FROM `sys_menu` WHERE `name` = 'AdminContentShell' LIMIT 1);

-- ---------- 3. 子菜单（路由由前端 Shell tabs 驱动，DB 子项供侧栏可选） ----------
INSERT INTO `sys_menu`
  (`id`, `parent_id`, `title`, `path`, `name`, `icon`, `component`, `permission_code`, `sort`, `hidden`, `status`)
VALUES
  (121, @content_id, '专题资料', '/admin/content/topic', 'AdminTopicOps', NULL,
   'admin/views/content/TopicOps.vue', 'admin:content:view', 10, 0, 1),
  (122, @content_id, '传统文化', '/admin/content/culture', 'AdminCultureOps', NULL,
   'admin/views/content/CultureOps.vue', 'admin:content:view', 20, 0, 1),
  (123, @content_id, '竞赛专区', '/admin/content/competition', 'AdminCompetitionOps', NULL,
   'admin/views/content/CompetitionOps.vue', 'admin:content:view', 30, 0, 1),
  (124, @content_id, '教育资讯', '/admin/content/news', 'AdminNewsOps', NULL,
   'admin/views/content/NewsOps.vue', 'admin:content:view', 40, 0, 1),
  (125, @content_id, '主题班会', '/admin/content/theme-class-meeting', 'AdminThemeClassMeetingOps', NULL,
   'admin/views/content/ThemeClassMeetingOps.vue', 'admin:content:view', 50, 0, 1)
ON DUPLICATE KEY UPDATE
  `parent_id` = @content_id,
  `title` = VALUES(`title`),
  `path` = VALUES(`path`),
  `name` = VALUES(`name`),
  `component` = VALUES(`component`),
  `permission_code` = VALUES(`permission_code`),
  `sort` = VALUES(`sort`),
  `hidden` = 0,
  `status` = 1;

-- ---------- 4. 校验 ----------
SELECT '=== 运营管理子菜单 ===' AS section;
SELECT id, parent_id, title, path, sort
FROM sys_menu
WHERE parent_id = 96 AND hidden = 0
ORDER BY sort, id;

SELECT '=== 内容中心子菜单 ===' AS section;
SELECT id, parent_id, title, path, permission_code, sort
FROM sys_menu
WHERE parent_id = @content_id AND hidden = 0
ORDER BY sort, id;
