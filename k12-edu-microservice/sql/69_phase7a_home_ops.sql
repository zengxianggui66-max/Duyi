-- Phase 7-A: home ops CMS (banner / quick entry / hot word)
-- Depends: 49 admin RBAC (admin:home:view/edit)
-- OpenSearch: NOT required; search-type hot words use MySQL /api/search/all
USE `xinketang`;
SET NAMES utf8mb4;

-- ----------------------------------------------------------
-- home_banner
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `home_banner` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `slot_code`       VARCHAR(32)  NOT NULL DEFAULT 'home_hero' COMMENT 'placement slot',
  `title`           VARCHAR(100) NOT NULL,
  `subtitle`        VARCHAR(200) DEFAULT NULL,
  `cta_text`        VARCHAR(30)  DEFAULT NULL COMMENT 'button label',
  `icon`            VARCHAR(16)  DEFAULT NULL,
  `bg_color_start`  CHAR(7)      DEFAULT '#667EEA',
  `bg_color_end`    CHAR(7)      DEFAULT '#764BA2',
  `image_url`       VARCHAR(500) DEFAULT NULL,
  `nav_target`      JSON         NOT NULL COMMENT 'NavTarget JSON, see Phase7-A doc',
  `stage_keys`      JSON         DEFAULT NULL COMMENT 'visible stage codes; NULL=all',
  `start_time`      DATETIME     DEFAULT NULL,
  `end_time`        DATETIME     DEFAULT NULL,
  `sort`            SMALLINT     DEFAULT 0,
  `status`          TINYINT      DEFAULT 1 COMMENT '0=off 1=on',
  `remark`          VARCHAR(200) DEFAULT NULL,
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_banner_slot` (`slot_code`, `status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='home carousel banners';

-- ----------------------------------------------------------
-- home_quick_entry
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `home_quick_entry` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `entry_key`     VARCHAR(32)  NOT NULL,
  `title`         VARCHAR(50)  NOT NULL,
  `description`   VARCHAR(100) DEFAULT NULL,
  `icon`          VARCHAR(16)  DEFAULT NULL,
  `accent_color`  CHAR(7)      DEFAULT '#4facfe',
  `nav_target`    JSON         NOT NULL,
  `stage_keys`    JSON         DEFAULT NULL,
  `sort`          SMALLINT     DEFAULT 0,
  `status`        TINYINT      DEFAULT 1,
  `remark`        VARCHAR(200) DEFAULT NULL,
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_entry_key` (`entry_key`),
  KEY `idx_entry_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='home quick function cards';

-- ----------------------------------------------------------
-- home_hot_word (ops config; separate from search_hot_keyword stats)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `home_hot_word` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `label`        VARCHAR(50)  NOT NULL COMMENT 'display text in header',
  `action_type`  VARCHAR(16)  NOT NULL COMMENT 'browse|search',
  `nav_target`   JSON         NOT NULL,
  `badge`        VARCHAR(16)  DEFAULT NULL,
  `stage_keys`   JSON         DEFAULT NULL,
  `start_time`   DATETIME     DEFAULT NULL,
  `end_time`     DATETIME     DEFAULT NULL,
  `sort`         SMALLINT     DEFAULT 0,
  `status`       TINYINT      DEFAULT 1,
  `remark`       VARCHAR(200) DEFAULT NULL,
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_hot_word_label` (`label`),
  KEY `idx_hot_word_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='header hot words (ops config)';

-- ----------------------------------------------------------
-- seed: migrate from HomePage.vue / hotWordActions.ts (idempotent)
-- ----------------------------------------------------------
DELETE FROM `home_banner` WHERE `slot_code` = 'home_hero' AND `title` IN (
  '开通即享全站资源', '2024秋季同步备课精选', '生涯规划'
);

INSERT INTO `home_banner`
  (`slot_code`, `title`, `subtitle`, `cta_text`, `icon`, `bg_color_start`, `bg_color_end`, `nav_target`, `sort`, `status`)
VALUES
('home_hero', '开通即享全站资源', '海量优质资源，持续更新，下载无限制', '立即开通', '💎', '#667EEA', '#764BA2',
 JSON_OBJECT('type', 'vip'), 30, 1),
('home_hero', '2024秋季同步备课精选', '汇聚优质教学资源，助力新学期教学', '立即查看', '📚', '#FF6B6B', '#FF8E53',
 JSON_OBJECT('type', 'route', 'routePath', '/lesson'), 20, 1),
('home_hero', '生涯规划', '职业探索、志愿填报，助力学生规划未来', '立即查看', '🧭', '#14B8A6', '#0D9488',
 JSON_OBJECT('type', 'route', 'routePath', '/topic'), 10, 1);

DELETE FROM `home_quick_entry` WHERE `entry_key` IN (
  'courseware', 'prepare', 'theme-meeting', 'exam', 'resource', 'career', 'culture', 'competition', 'topic'
);

INSERT INTO `home_quick_entry`
  (`entry_key`, `title`, `description`, `icon`, `accent_color`, `nav_target`, `sort`, `status`)
VALUES
('courseware', '课件资源', '海量课件、一键下载', '📚', '#4facfe',
 JSON_OBJECT('type', 'browse', 'stageKey', 'primary', 'subjectKey', 'chinese', 'versionKey', 'tongbian2024',
   'query', JSON_OBJECT('module', '同步备课')), 90, 1),
('prepare', '备课中心', '教案学案、同步资源', '📋', '#43e97b',
 JSON_OBJECT('type', 'route', 'routePath', '/lesson/smart'), 80, 1),
('theme-meeting', '主题班会', '班会育人、心理健康', '🎯', '#409eff',
 JSON_OBJECT('type', 'route', 'routePath', '/topic'), 70, 1),
('exam', '智能组卷', '智能组卷、精准出题', '📝', '#f093fb',
 JSON_OBJECT('type', 'route', 'routePath', '/exam/smart'), 60, 1),
('resource', '教育资源', '全站资源、分类检索', '📦', '#fa709a',
 JSON_OBJECT('type', 'browse', 'stageKey', 'primary', 'subjectKey', 'chinese', 'versionKey', 'tongbian2024',
   'query', JSON_OBJECT('module', '同步备课')), 50, 1),
('career', '生涯规划', '职业探索、志愿填报', '🧭', '#14b8a6',
 JSON_OBJECT('type', 'route', 'routePath', '/topic'), 40, 1),
('culture', '传统文化', '巴蜀研学·成都', '🏮', '#d97706',
 JSON_OBJECT('type', 'route', 'routePath', '/culture'), 30, 1),
('competition', '竞赛专区', '学科竞赛、奥数', '🏅', '#f39c12',
 JSON_OBJECT('type', 'route', 'routePath', '/competition'), 20, 1),
('topic', '专题资源', '寒暑假·升学·成都绵阳', '📚', '#8b5cf6',
 JSON_OBJECT('type', 'route', 'routePath', '/topic'), 10, 1);

DELETE FROM `home_hot_word` WHERE `label` IN (
  '一年级语文', '期中试卷', '教案模板', '中考复习'
);

INSERT INTO `home_hot_word`
  (`label`, `action_type`, `nav_target`, `sort`, `status`)
VALUES
('一年级语文', 'browse',
 JSON_OBJECT('type', 'browse', 'stageKey', 'primary', 'subjectKey', 'chinese', 'versionKey', 'tongbian2024',
   'volumeName', '一年级上册', 'query', JSON_OBJECT('module', '同步备课')), 40, 1),
('期中试卷', 'browse',
 JSON_OBJECT('type', 'browse', 'stageKey', 'primary', 'subjectKey', 'chinese', 'versionKey', 'tongbian2024',
   'volumeName', '一年级上册', 'query', JSON_OBJECT('module', '期中')), 30, 1),
('教案模板', 'search',
 JSON_OBJECT('type', 'search', 'stageKey', 'primary', 'subjectKey', 'chinese', 'versionKey', 'tongbian2024',
   'volumeName', '一年级上册', 'keyword', '教案', 'searchEngine', 'mysql',
   'query', JSON_OBJECT('module', '同步备课')), 20, 1),
('中考复习', 'browse',
 JSON_OBJECT('type', 'browse', 'stageKey', 'junior', 'subjectKey', 'chinese', 'versionKey', 'tongbian2024',
   'query', JSON_OBJECT('module', '一轮复习')), 10, 1);
