-- ============================================================
-- 新课堂教育 — 02 教材体系核心维度表（DDL）
-- 学段 / 学科 / 版本 / 年级 / 学期 / 册别 / 学科-版本关系
-- 执行：00 → 01_drop → 本文件 → 03~09 → 99_seed_all.sql（库名 xinketang）
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------
-- edu_stage 学段
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `edu_subject_edition`;
DROP TABLE IF EXISTS `edu_subject`;
DROP TABLE IF EXISTS `edu_grade`;
DROP TABLE IF EXISTS `edu_stage`;

CREATE TABLE `edu_stage` (
  `id`          TINYINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code`        VARCHAR(20)  NOT NULL COMMENT '编码：primary/junior/senior/art/dance',
  `name`        VARCHAR(20)  NOT NULL COMMENT '名称',
  `icon`        VARCHAR(10)  DEFAULT NULL COMMENT '图标',
  `sort`        SMALLINT     DEFAULT 0 COMMENT '排序',
  `status`      TINYINT      DEFAULT 1 COMMENT '0停用 1启用',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学段表';

-- ----------------------------------------------------------
-- edu_subject 学科（按学段）
-- ----------------------------------------------------------
CREATE TABLE `edu_subject` (
  `id`          SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `stage_id`    TINYINT UNSIGNED NOT NULL,
  `code`        VARCHAR(50)  NOT NULL COMMENT '学科编码，学段内唯一：chinese/math',
  `name`        VARCHAR(30)  NOT NULL,
  `icon`        VARCHAR(10)  DEFAULT NULL,
  `sort`        SMALLINT     DEFAULT 0,
  `status`      TINYINT      DEFAULT 1,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stage_code` (`stage_id`, `code`),
  KEY `idx_stage_id` (`stage_id`),
  CONSTRAINT `fk_subject_stage` FOREIGN KEY (`stage_id`) REFERENCES `edu_stage` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学科表';

-- ----------------------------------------------------------
-- edu_edition 教材版本
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `edu_edition`;

CREATE TABLE `edu_edition` (
  `id`          SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`        VARCHAR(30)  NOT NULL COMMENT '版本编码：tongbian2024/renjiao',
  `name`        VARCHAR(50)  NOT NULL COMMENT '显示名称',
  `short_name`  VARCHAR(20)  DEFAULT NULL,
  `publisher`   VARCHAR(100) DEFAULT NULL COMMENT '出版社',
  `year_label`  VARCHAR(20)  DEFAULT NULL COMMENT '年份标识',
  `sort`        SMALLINT     DEFAULT 0,
  `status`      TINYINT      DEFAULT 1,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教材版本表';

-- ----------------------------------------------------------
-- edu_grade 年级
-- ----------------------------------------------------------
CREATE TABLE `edu_grade` (
  `id`          TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `stage_id`    TINYINT UNSIGNED NOT NULL,
  `code`        VARCHAR(30)  DEFAULT NULL COMMENT '编码：grade1/grade7',
  `name`        VARCHAR(20)  NOT NULL COMMENT '一年级/初一/高一',
  `sort`        SMALLINT     DEFAULT 0,
  `status`      TINYINT      DEFAULT 1,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stage_name` (`stage_id`, `name`),
  KEY `idx_stage_id` (`stage_id`),
  CONSTRAINT `fk_grade_stage` FOREIGN KEY (`stage_id`) REFERENCES `edu_stage` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='年级表';

-- ----------------------------------------------------------
-- edu_semester 学期
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `edu_semester`;

CREATE TABLE `edu_semester` (
  `id`          TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`        VARCHAR(20)  NOT NULL COMMENT 'first/second',
  `name`        VARCHAR(20)  NOT NULL,
  `sort`        SMALLINT     DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学期表';

-- ----------------------------------------------------------
-- edu_volume 册别
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `edu_volume`;

CREATE TABLE `edu_volume` (
  `id`          TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code`        VARCHAR(20)  NOT NULL COMMENT 'up/down/full',
  `name`        VARCHAR(20)  NOT NULL COMMENT '上册/下册/全册',
  `sort`        SMALLINT     DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='册别表';

-- ----------------------------------------------------------
-- edu_subject_edition 学科可用版本（多对多）
-- ----------------------------------------------------------
CREATE TABLE `edu_subject_edition` (
  `id`          INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `subject_id`  SMALLINT UNSIGNED NOT NULL,
  `edition_id`  SMALLINT UNSIGNED NOT NULL,
  `sort`        SMALLINT DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subject_edition` (`subject_id`, `edition_id`),
  KEY `idx_edition_id` (`edition_id`),
  CONSTRAINT `fk_se_subject` FOREIGN KEY (`subject_id`) REFERENCES `edu_subject` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_se_edition` FOREIGN KEY (`edition_id`) REFERENCES `edu_edition` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学科-教材版本关联';

SET FOREIGN_KEY_CHECKS = 1;
