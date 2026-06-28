-- Phase 3J-1: 频道中心菜单文案统一（Plan A 运营管理 id=96 下）
-- 部署顺序：89 → 91 → 93 → 94 → 95
-- mysql -u root -p xinketang < sql/95_phase3j_menu_channel_center.sql
USE `xinketang`;
SET NAMES utf8mb4;

-- 运营管理父级仍指向频道中心入口
UPDATE `sys_menu` SET
  `title` = '运营管理',
  `path` = '/admin/home-config/banners'
WHERE `id` = 96;

-- 原「首页配置」改为「频道中心」（路由仍为 /admin/home-config）
UPDATE `sys_menu` SET
  `title` = '频道中心',
  `path` = '/admin/home-config/banners',
  `name` = 'AdminChannelCenter',
  `icon` = 'Connection'
WHERE `id` = 6;

-- 若历史上 name 仍为 AdminHomeConfig，同步标题即可
UPDATE `sys_menu` SET
  `title` = '频道中心',
  `path` = '/admin/home-config/banners'
WHERE `name` IN ('AdminHomeConfig', 'AdminChannelCenter') AND `parent_id` = 96;

-- ---------- 校验 ----------
SELECT '=== 运营管理子菜单 ===' AS section;
SELECT id, parent_id, title, path, name, sort
FROM sys_menu
WHERE parent_id = 96 AND hidden = 0
ORDER BY sort, id;
