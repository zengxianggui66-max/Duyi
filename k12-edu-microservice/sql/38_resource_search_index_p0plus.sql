-- P0+ 全站搜索扁平索引表（跨 primary/topic/competition/culture 统一检索）
USE xinketang;

DROP TABLE IF EXISTS `resource_search_index`;

CREATE TABLE `resource_search_index` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `doc_id`          VARCHAR(64)     NOT NULL COMMENT 'resource_type:resource_id',
  `resource_id`     BIGINT UNSIGNED NOT NULL,
  `resource_type`   VARCHAR(32)     NOT NULL,
  `title`           VARCHAR(500)    NOT NULL,
  `summary`         VARCHAR(1000)   DEFAULT NULL,
  `search_text`     TEXT            COMMENT '可检索聚合文本',
  `stage_key`       VARCHAR(32)     DEFAULT NULL,
  `stage_name`      VARCHAR(32)     DEFAULT NULL,
  `channel_key`     VARCHAR(32)     DEFAULT NULL,
  `channel_name`    VARCHAR(64)     DEFAULT NULL,
  `subject`         VARCHAR(50)     DEFAULT NULL,
  `grade_name`      VARCHAR(50)     DEFAULT NULL,
  `edition_name`    VARCHAR(50)     DEFAULT NULL,
  `module_name`     VARCHAR(64)     DEFAULT NULL,
  `catalog_path`    VARCHAR(500)    DEFAULT NULL,
  `lesson_name`     VARCHAR(200)    DEFAULT NULL,
  `teaching_type`   VARCHAR(50)     DEFAULT NULL,
  `file_ext`        VARCHAR(20)     DEFAULT NULL,
  `detail_route`    VARCHAR(500)    NOT NULL,
  `download_count`  INT             DEFAULT 0,
  `view_count`      INT             DEFAULT 0,
  `hot_score`       DOUBLE          DEFAULT 0,
  `vip_flag`        TINYINT         DEFAULT 0 COMMENT '0=免费可见 1=VIP/精品',
  `status`          TINYINT         DEFAULT 1,
  `publish_time`    DATETIME        DEFAULT NULL,
  `update_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doc_id` (`doc_id`),
  KEY `idx_resource` (`resource_type`, `resource_id`),
  KEY `idx_filter` (`status`, `stage_key`, `channel_key`, `teaching_type`),
  KEY `idx_hot` (`hot_score` DESC),
  KEY `idx_publish` (`publish_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全站搜索扁平索引';
