-- ============================================================
-- 新课堂教育 — 03 分类体系维度表（DDL）
-- 资源类型树 / 栏目 / 考试场景 / 教学场景 / 地区 / 文件格式 / 频道
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------
-- edu_resource_type 资源类型（树形）
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `edu_resource_teaching_scene`;
DROP TABLE IF EXISTS `edu_resource_region`;
DROP TABLE IF EXISTS `edu_resource_module`;
DROP TABLE IF EXISTS `edu_module_stage`;
DROP TABLE IF EXISTS `edu_resource_type`;

CREATE TABLE `edu_resource_type` (
  `id`            SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `parent_id`     SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0=一级分组',
  `code`          VARCHAR(50)  NOT NULL COMMENT '类型编码',
  `name`          VARCHAR(50)  NOT NULL COMMENT '显示名称',
  `icon`          VARCHAR(10)  DEFAULT NULL,
  `group_code`    VARCHAR(30)  DEFAULT NULL COMMENT '所属分组编码',
  `group_name`    VARCHAR(30)  DEFAULT NULL COMMENT '所属分组名称',
  `default_exts`  JSON         DEFAULT NULL COMMENT '推荐扩展名 JSON 数组',
  `allow_preview` TINYINT      DEFAULT 1 COMMENT '是否支持预览',
  `sort`          SMALLINT     DEFAULT 0,
  `status`        TINYINT      DEFAULT 1,
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_group_code` (`group_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源类型表（树形）';

-- ----------------------------------------------------------
-- edu_module 栏目/专区
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `edu_module`;

CREATE TABLE `edu_module` (
  `id`                SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`              VARCHAR(50)  NOT NULL COMMENT '栏目编码',
  `name`              VARCHAR(50)  NOT NULL COMMENT '栏目名称',
  `icon`              VARCHAR(10)  DEFAULT NULL,
  `module_category`   VARCHAR(30)  NOT NULL COMMENT 'sync|monthly|term|holiday|transition|exam|topic|composition|reading|competition|material|review',
  `applicable_stages` JSON         DEFAULT NULL COMMENT '适用学段 code 数组，空=全学段',
  `description`       VARCHAR(500) DEFAULT NULL,
  `sort`              SMALLINT     DEFAULT 0,
  `status`            TINYINT      DEFAULT 1,
  `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_module_category` (`module_category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源栏目/专区表';

-- ----------------------------------------------------------
-- edu_module_stage 栏目-学段关联（多对多）
-- ----------------------------------------------------------
CREATE TABLE `edu_module_stage` (
  `id`        INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `module_id` SMALLINT UNSIGNED NOT NULL,
  `stage_id`  TINYINT UNSIGNED NOT NULL,
  `sort`      SMALLINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_module_stage` (`module_id`, `stage_id`),
  KEY `idx_stage_id` (`stage_id`),
  CONSTRAINT `fk_ms_module` FOREIGN KEY (`module_id`) REFERENCES `edu_module` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ms_stage` FOREIGN KEY (`stage_id`) REFERENCES `edu_stage` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='栏目适用学段';

-- ----------------------------------------------------------
-- edu_exam_scene 考试/备考场景
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `edu_exam_scene`;

CREATE TABLE `edu_exam_scene` (
  `id`         TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`       VARCHAR(30)  NOT NULL,
  `name`       VARCHAR(50)  NOT NULL,
  `exam_level` VARCHAR(30)  DEFAULT NULL COMMENT 'unit|monthly|midterm|final|xsc|zk|gk|competition',
  `sort`       SMALLINT     DEFAULT 0,
  `status`     TINYINT      DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试/备考场景';

-- ----------------------------------------------------------
-- edu_teaching_scene 教学场景标签
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `edu_teaching_scene`;

CREATE TABLE `edu_teaching_scene` (
  `id`     TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`   VARCHAR(30)  NOT NULL,
  `name`   VARCHAR(30)  NOT NULL,
  `sort`   SMALLINT     DEFAULT 0,
  `status` TINYINT      DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教学场景标签';

-- ----------------------------------------------------------
-- edu_region 地区（省市区树）
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `edu_region`;

CREATE TABLE `edu_region` (
  `id`        INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `parent_id` INT UNSIGNED NOT NULL DEFAULT 0,
  `code`      VARCHAR(20)  NOT NULL,
  `name`      VARCHAR(50)  NOT NULL,
  `level`     TINYINT      DEFAULT 1 COMMENT '1省 2市 3区县',
  `sort`      SMALLINT     DEFAULT 0,
  `status`    TINYINT      DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地区表';

-- ----------------------------------------------------------
-- edu_file_format 文件格式字典
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `edu_file_format`;

CREATE TABLE `edu_file_format` (
  `id`           TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`         VARCHAR(20)  NOT NULL COMMENT 'word|ppt|pdf|audio|video|archive',
  `name`         VARCHAR(30)  NOT NULL,
  `extensions`   VARCHAR(100) NOT NULL COMMENT '逗号分隔扩展名',
  `mime_types`   VARCHAR(200) DEFAULT NULL,
  `preview_type` VARCHAR(30)  DEFAULT NULL COMMENT 'office|pdf|audio|video|download_only',
  `sort`         SMALLINT     DEFAULT 0,
  `status`       TINYINT      DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件格式字典';

-- ----------------------------------------------------------
-- edu_channel 特色频道
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `feature_category`;
DROP TABLE IF EXISTS `edu_channel`;

CREATE TABLE `edu_channel` (
  `id`          TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`        VARCHAR(30)  NOT NULL COMMENT 'main|banhui|patriotic',
  `name`        VARCHAR(50)  NOT NULL,
  `description` VARCHAR(500) DEFAULT NULL,
  `sort`        SMALLINT     DEFAULT 0,
  `status`      TINYINT      DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='特色频道';

CREATE TABLE `feature_category` (
  `id`          INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `channel_id`  TINYINT UNSIGNED NOT NULL,
  `parent_id`   INT UNSIGNED NOT NULL DEFAULT 0,
  `code`        VARCHAR(50)  NOT NULL,
  `name`        VARCHAR(100) NOT NULL,
  `icon`        VARCHAR(255) DEFAULT NULL,
  `sort`        SMALLINT     DEFAULT 0,
  `status`      TINYINT      DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_channel_code` (`channel_id`, `code`),
  KEY `idx_parent_id` (`parent_id`),
  CONSTRAINT `fk_fc_channel` FOREIGN KEY (`channel_id`) REFERENCES `edu_channel` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='特色频道分类树';

SET FOREIGN_KEY_CHECKS = 1;
