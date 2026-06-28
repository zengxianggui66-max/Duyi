-- Phase 5-C：管理端教材目录树菜单
USE `xinketang`;
SET NAMES utf8mb4;

INSERT INTO `sys_menu` (`id`, `parent_id`, `title`, `path`, `name`, `icon`, `component`, `permission_code`, `sort`, `hidden`, `status`)
VALUES (52, 0, '教材目录树', '/admin/catalog', 'AdminCatalog', 'Share', 'admin/views/catalog/CatalogTree', 'admin:taxonomy:view', 55, 0, 1)
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `permission_code` = VALUES(`permission_code`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);

-- content_admin(2) / operator(4) 已有 admin:taxonomy:view，无需额外授权
