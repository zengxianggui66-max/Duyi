-- Phase 7-E: feature channels (ops) + home func channel admin support
-- Depends: 27 home_func_channel, admin:home:view/edit
USE `xinketang`;
SET NAMES utf8mb4;

-- ----------------------------------------------------------
-- ops_channel (特色频道页 bootstrap)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `ops_channel` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`         VARCHAR(32)  NOT NULL COMMENT 'banhui|shengya|chuantong|jingsai|zhuanti',
  `name`         VARCHAR(50)  NOT NULL,
  `icon`         VARCHAR(16)  DEFAULT NULL,
  `description`  VARCHAR(500) DEFAULT NULL,
  `bg_gradient`  VARCHAR(200) DEFAULT NULL,
  `route_path`   VARCHAR(200) DEFAULT NULL,
  `stats_json`   JSON         DEFAULT NULL COMMENT '{total,elite,free}',
  `ui_json`      JSON         DEFAULT NULL COMMENT '{showGradeFilter,showSubjectFilter,eliteTitle,eliteDesc}',
  `sort`         SMALLINT     DEFAULT 0,
  `status`       TINYINT      DEFAULT 1,
  `remark`       VARCHAR(200) DEFAULT NULL,
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ops_channel_code` (`code`),
  KEY `idx_ops_channel_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='feature channel page config';

CREATE TABLE IF NOT EXISTS `ops_channel_tab` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `channel_code`   VARCHAR(32)  NOT NULL,
  `tab_key`        VARCHAR(32)  NOT NULL,
  `tab_name`       VARCHAR(50)  NOT NULL,
  `icon`           VARCHAR(16)  DEFAULT NULL,
  `search_keyword` VARCHAR(200) DEFAULT NULL,
  `sort`           SMALLINT     DEFAULT 0,
  `status`         TINYINT      DEFAULT 1,
  `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ops_channel_tab` (`channel_code`, `tab_key`),
  KEY `idx_ops_tab_channel` (`channel_code`, `status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='feature channel tabs';

CREATE TABLE IF NOT EXISTS `ops_channel_featured_album` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `channel_code`    VARCHAR(32)  NOT NULL,
  `title`           VARCHAR(100) NOT NULL,
  `icon`            VARCHAR(16)  DEFAULT NULL,
  `meta`            VARCHAR(100) DEFAULT NULL,
  `resource_count`  INT          DEFAULT 0,
  `download_count`  INT          DEFAULT 0,
  `cover_gradient`  VARCHAR(200) DEFAULT NULL,
  `link_path`       VARCHAR(200) DEFAULT NULL,
  `sort`            SMALLINT     DEFAULT 0,
  `status`          TINYINT      DEFAULT 1,
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ops_album_channel` (`channel_code`, `status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='feature channel elite albums';
