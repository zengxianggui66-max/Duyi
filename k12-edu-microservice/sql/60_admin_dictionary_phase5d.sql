-- Phase 5-D：字典/标签管理端菜单 + 资源属性标签表
USE `xinketang`;
SET NAMES utf8mb4;

-- ----------------------------------------------------------
-- edu_browse_tag 浏览/上传资源属性标签（运营可配）
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `edu_browse_tag` (
  `id`              SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`            VARCHAR(50)  NOT NULL COMMENT '标签编码，如 sync / free',
  `name`            VARCHAR(50)  NOT NULL COMMENT '显示名称',
  `tag_group`       VARCHAR(30)  NOT NULL DEFAULT 'core' COMMENT 'core|stage|module',
  `applicable_stages` JSON       DEFAULT NULL COMMENT '适用学段 code 数组，空=全学段',
  `applicable_modules` JSON      DEFAULT NULL COMMENT '适用栏目 name/code 数组，空=全栏目',
  `sort`            SMALLINT     DEFAULT 0,
  `status`          TINYINT      DEFAULT 1,
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源浏览/上传属性标签';

INSERT INTO `edu_browse_tag` (`id`, `code`, `name`, `tag_group`, `applicable_stages`, `applicable_modules`, `sort`, `status`) VALUES
(1,  'sync',         '同步',   'core',   NULL, NULL, 1,  1),
(2,  'quality',      '精品',   'core',   NULL, NULL, 2,  1),
(3,  'free',         '免费',   'core',   NULL, NULL, 3,  1),
(4,  'has_answer',   '有答案', 'core',   NULL, NULL, 4,  1),
(5,  'text_version', '文字版', 'core',   NULL, NULL, 5,  1),
(6,  'exam_level',   '考级',   'stage',  '["art","dance"]', NULL, 10, 1),
(7,  'art_exam',     '艺考',   'stage',  '["art","dance"]', NULL, 11, 1),
(8,  'competition',  '竞赛',   'module', NULL, '["竞赛"]', 20, 1),
(9,  'review',       '复习',   'module', NULL, '["专题复习"]', 21, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `tag_group` = VALUES(`tag_group`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);

-- 管理端菜单（复用 taxonomy 权限）
INSERT INTO `sys_menu` (`id`, `parent_id`, `title`, `path`, `name`, `icon`, `component`, `permission_code`, `sort`, `hidden`, `status`)
VALUES
(53, 0, '业务字典', '/admin/dictionaries', 'AdminDictionaries', 'Notebook', 'admin/views/dictionary/Dictionaries', 'admin:taxonomy:view', 56, 0, 1),
(54, 0, '资源标签', '/admin/tags', 'AdminTags', 'PriceTag', 'admin/views/dictionary/Tags', 'admin:taxonomy:view', 57, 0, 1)
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `permission_code` = VALUES(`permission_code`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);
