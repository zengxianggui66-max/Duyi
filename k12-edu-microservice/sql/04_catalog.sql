-- ============================================================
-- 新课堂教育 — 04 教材目录表（DDL）
-- 单元 / 课文课时 / 知识点
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `edu_knowledge_point`;
DROP TABLE IF EXISTS `edu_lesson`;
DROP TABLE IF EXISTS `edu_unit`;

-- ----------------------------------------------------------
-- edu_unit 单元
-- ----------------------------------------------------------
CREATE TABLE `edu_unit` (
  `id`          INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `subject_id`  SMALLINT UNSIGNED NOT NULL,
  `grade_id`    TINYINT UNSIGNED NOT NULL,
  `edition_id`  SMALLINT UNSIGNED NOT NULL,
  `volume_id`   TINYINT UNSIGNED NOT NULL,
  `semester_id` TINYINT UNSIGNED DEFAULT NULL COMMENT '可选：关联学期',
  `code`        VARCHAR(50)  DEFAULT NULL COMMENT '单元编码',
  `name`        VARCHAR(200) NOT NULL COMMENT '单元名称',
  `sort`        SMALLINT     DEFAULT 0,
  `status`      TINYINT      DEFAULT 1,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_catalog` (`subject_id`, `grade_id`, `edition_id`, `volume_id`),
  KEY `idx_grade_edition_volume` (`grade_id`, `edition_id`, `volume_id`),
  CONSTRAINT `fk_unit_subject` FOREIGN KEY (`subject_id`) REFERENCES `edu_subject` (`id`),
  CONSTRAINT `fk_unit_grade` FOREIGN KEY (`grade_id`) REFERENCES `edu_grade` (`id`),
  CONSTRAINT `fk_unit_edition` FOREIGN KEY (`edition_id`) REFERENCES `edu_edition` (`id`),
  CONSTRAINT `fk_unit_volume` FOREIGN KEY (`volume_id`) REFERENCES `edu_volume` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教材单元表';

-- ----------------------------------------------------------
-- edu_lesson 课文/课时
-- ----------------------------------------------------------
CREATE TABLE `edu_lesson` (
  `id`          INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `unit_id`     INT UNSIGNED NOT NULL,
  `code`        VARCHAR(50)  DEFAULT NULL,
  `name`        VARCHAR(200) NOT NULL COMMENT '课文或课时名称',
  `lesson_no`   SMALLINT     DEFAULT NULL COMMENT '课时序号',
  `sort`        SMALLINT     DEFAULT 0,
  `status`      TINYINT      DEFAULT 1,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_unit_id` (`unit_id`),
  CONSTRAINT `fk_lesson_unit` FOREIGN KEY (`unit_id`) REFERENCES `edu_unit` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课文/课时表';

-- ----------------------------------------------------------
-- edu_knowledge_point 知识点（可选树形）
-- ----------------------------------------------------------
CREATE TABLE `edu_knowledge_point` (
  `id`          INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `subject_id`  SMALLINT UNSIGNED DEFAULT NULL,
  `lesson_id`   INT UNSIGNED DEFAULT NULL,
  `parent_id`   INT UNSIGNED NOT NULL DEFAULT 0,
  `code`        VARCHAR(50)  DEFAULT NULL,
  `name`        VARCHAR(200) NOT NULL,
  `sort`        SMALLINT     DEFAULT 0,
  `status`      TINYINT      DEFAULT 1,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_subject_id` (`subject_id`),
  KEY `idx_lesson_id` (`lesson_id`),
  KEY `idx_parent_id` (`parent_id`),
  CONSTRAINT `fk_kp_subject` FOREIGN KEY (`subject_id`) REFERENCES `edu_subject` (`id`),
  CONSTRAINT `fk_kp_lesson` FOREIGN KEY (`lesson_id`) REFERENCES `edu_lesson` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识点表';

SET FOREIGN_KEY_CHECKS = 1;
