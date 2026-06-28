-- Phase 9-B: search synonym/redirect admin + redirect table
USE `xinketang`;
SET NAMES utf8mb4;

-- ---------- 1. 搜索重定向表 ----------
CREATE TABLE IF NOT EXISTS `sys_search_redirect` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `keyword`       VARCHAR(80)     NOT NULL COMMENT '触发词（精确匹配，trim 后）',
  `title`         VARCHAR(100)    DEFAULT NULL COMMENT '展示标题，默认同 keyword',
  `route_path`    VARCHAR(500)    NOT NULL COMMENT '站内路由，如 /culture',
  `nav_target`    JSON            DEFAULT NULL COMMENT 'NavTarget JSON（可选，route 类型）',
  `priority`      INT             DEFAULT 0 COMMENT '越大越优先',
  `status`        TINYINT         DEFAULT 1 COMMENT '0=禁用 1=启用',
  `remark`        VARCHAR(200)    DEFAULT NULL,
  `create_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_keyword` (`keyword`),
  KEY `idx_status_priority` (`status`, `priority` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索运营重定向规则';

-- ---------- 2. 编辑权限 ----------
INSERT INTO `sys_permission` (`code`, `name`, `type`, `module`, `parent_id`, `sort`, `status`) VALUES
('admin:search:edit', '编辑搜索词典', 'button', 'search', 0, 70, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `type` = VALUES(`type`),
  `module` = VALUES(`module`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);

UPDATE `sys_permission` p_btn
INNER JOIN `sys_permission` p_menu ON p_menu.code = 'admin:search:view'
SET p_btn.parent_id = p_menu.id
WHERE p_btn.code = 'admin:search:edit';

-- super_admin 补漏
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.id FROM `sys_permission` p WHERE p.code = 'admin:search:edit';

-- content_admin：编辑
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 2, p.id FROM `sys_permission` p WHERE p.code = 'admin:search:edit';

-- ---------- 3. 种子：将部分硬编码迁移为 DB 规则（可重复执行） ----------
INSERT INTO `sys_search_redirect` (`keyword`, `title`, `route_path`, `priority`, `status`, `remark`) VALUES
('主题班会', '主题班会', '/theme-class-meeting', 100, 1, 'phase9b seed from EXACT_ROUTES'),
('传统文化', '传统文化', '/culture', 100, 1, 'phase9b seed'),
('竞赛专区', '竞赛专区', '/competition', 100, 1, 'phase9b seed'),
('教育资讯', '教育资讯', '/news', 100, 1, 'phase9b seed'),
('专题资源', '专题资源', '/topic', 100, 1, 'phase9b seed')
ON DUPLICATE KEY UPDATE
  `route_path` = VALUES(`route_path`),
  `title` = VALUES(`title`),
  `priority` = VALUES(`priority`),
  `status` = VALUES(`status`),
  `remark` = VALUES(`remark`);

SELECT '=== sys_search_redirect count ===' AS section;
SELECT COUNT(*) AS cnt FROM sys_search_redirect WHERE status = 1;
