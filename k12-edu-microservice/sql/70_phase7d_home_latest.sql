-- Phase 7-D: home latest content columns (material / topic / news)
-- Depends: 69 home ops, admin:home:view/edit
USE `xinketang`;
SET NAMES utf8mb4;

-- ----------------------------------------------------------
-- home_latest_column
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `home_latest_column` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `column_key`   VARCHAR(32)  NOT NULL COMMENT 'material|topic|news',
  `title`        VARCHAR(50)  NOT NULL,
  `more_path`    VARCHAR(200) NOT NULL DEFAULT '/',
  `data_source`  VARCHAR(16)  NOT NULL COMMENT 'rule|manual|api',
  `rule_json`    JSON         DEFAULT NULL COMMENT 'rule query when data_source=rule',
  `sort`         SMALLINT     DEFAULT 0,
  `status`       TINYINT      DEFAULT 1 COMMENT '0=off 1=on',
  `remark`       VARCHAR(200) DEFAULT NULL,
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_column_key` (`column_key`),
  KEY `idx_latest_col_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='home latest content column config';

-- ----------------------------------------------------------
-- home_latest_item (manual list when data_source=manual)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `home_latest_item` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `column_id`       BIGINT UNSIGNED NOT NULL,
  `title`           VARCHAR(200) NOT NULL,
  `item_date`       DATE         DEFAULT NULL,
  `resource_id`     BIGINT       DEFAULT NULL,
  `resource_source` VARCHAR(32)  DEFAULT NULL COMMENT 'edu_resource|oss_primary_chinese|edu_resource_suite',
  `link_path`       VARCHAR(200) DEFAULT NULL COMMENT 'custom link when no resource/article',
  `article_id`      BIGINT       DEFAULT NULL,
  `sort`            SMALLINT     DEFAULT 0,
  `status`          TINYINT      DEFAULT 1,
  `remark`          VARCHAR(200) DEFAULT NULL,
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_latest_item_col` (`column_id`, `status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='home latest column manual items';

-- ----------------------------------------------------------
-- seed: migrate from LatestContent.vue defaults (idempotent)
-- ----------------------------------------------------------
INSERT INTO `home_latest_column` (`column_key`, `title`, `more_path`, `data_source`, `rule_json`, `sort`, `status`)
VALUES
  ('material', '最新资料', '/resource/sync', 'rule',
   JSON_OBJECT('moduleNames', JSON_ARRAY('试卷'), 'limit', 8, 'orderBy', 'upload_time_desc'),
   30, 1),
  ('topic', '最新专题', '/feature/topic', 'rule',
   JSON_OBJECT('moduleNames', JSON_ARRAY('同步备课'), 'resourceTypeNames', JSON_ARRAY('课件'), 'limit', 8, 'orderBy', 'upload_time_desc'),
   20, 1),
  ('news', '最新资讯', '/news', 'api', NULL, 10, 1)
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `more_path` = VALUES(`more_path`),
  `data_source` = VALUES(`data_source`),
  `rule_json` = VALUES(`rule_json`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);
