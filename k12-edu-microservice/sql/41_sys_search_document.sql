-- P1 全站统一搜索索引表
-- mysql -u root -p xinketang < sql/41_sys_search_document.sql
USE xinketang;
SET NAMES utf8mb4;

DROP TABLE IF EXISTS `sys_search_document`;

CREATE TABLE `sys_search_document` (
  `id`                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `doc_id`              VARCHAR(80)     NOT NULL COMMENT '全局文档ID resource:123 news:45 channel:topic',
  `doc_type`            VARCHAR(40)     NOT NULL COMMENT 'resource/news/channel/lesson/prep/feature/page/subject',
  `biz_id`              VARCHAR(64)     NOT NULL COMMENT '业务ID或业务key',
  `title`               VARCHAR(255)    NOT NULL,
  `subtitle`            VARCHAR(255)    DEFAULT '',
  `summary`             TEXT,
  `content_text`        MEDIUMTEXT      COMMENT '可搜索正文',
  `keyword_text`        TEXT            COMMENT '标签/别名/运营关键词',
  `stage_key`           VARCHAR(30)     DEFAULT NULL,
  `stage_name`          VARCHAR(30)     DEFAULT NULL,
  `subject_key`         VARCHAR(30)     DEFAULT NULL,
  `subject_name`        VARCHAR(30)     DEFAULT NULL,
  `grade_key`           VARCHAR(30)     DEFAULT NULL,
  `grade_name`          VARCHAR(30)     DEFAULT NULL,
  `channel_key`         VARCHAR(50)     DEFAULT NULL,
  `channel_name`        VARCHAR(50)     DEFAULT NULL,
  `module_key`          VARCHAR(50)     DEFAULT NULL,
  `module_name`         VARCHAR(50)     DEFAULT NULL,
  `resource_type_key`   VARCHAR(50)     DEFAULT NULL,
  `resource_type_name`  VARCHAR(50)     DEFAULT NULL,
  `route_path`          VARCHAR(500)    NOT NULL COMMENT '前端跳转地址',
  `cover_url`           VARCHAR(500)    DEFAULT '',
  `publish_time`        DATETIME        DEFAULT NULL,
  `view_count`          INT             DEFAULT 0,
  `download_count`      INT             DEFAULT 0,
  `quality_score`       DECIMAL(8,2)    DEFAULT 0,
  `hot_score`           DOUBLE          DEFAULT 0 COMMENT '热度综合分',
  `vip_flag`            TINYINT         DEFAULT 0 COMMENT '0=可见 1=VIP隐藏',
  `status`              TINYINT         DEFAULT 1 COMMENT '1=可搜 0=不可搜',
  `is_deleted`          TINYINT         DEFAULT 0,
  `update_time`         DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `create_time`         DATETIME        DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doc_id` (`doc_id`),
  KEY `idx_type_status` (`doc_type`, `status`, `is_deleted`),
  KEY `idx_stage_subject` (`stage_key`, `subject_key`),
  KEY `idx_channel_module` (`channel_key`, `module_key`),
  KEY `idx_publish_time` (`publish_time`),
  KEY `idx_hot` (`hot_score` DESC),
  FULLTEXT KEY `ft_search` (`title`, `subtitle`, `summary`, `content_text`, `keyword_text`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全站搜索统一索引文档';
