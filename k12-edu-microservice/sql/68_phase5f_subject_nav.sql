-- Phase 5-F: home subject nav matrix (module-subject + sync resource types)
USE `xinketang`;
SET NAMES utf8mb4;

-- ----------------------------------------------------------
-- edu_module_subject: which modules appear for a subject in home nav / browse
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `edu_module_subject` (
  `id`         INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `module_id`  SMALLINT UNSIGNED NOT NULL,
  `subject_id` SMALLINT UNSIGNED NOT NULL,
  `sort`       SMALLINT DEFAULT 0,
  `status`     TINYINT  DEFAULT 1 COMMENT '0=hidden 1=visible',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_module_subject` (`module_id`, `subject_id`),
  KEY `idx_subject_id` (`subject_id`),
  CONSTRAINT `fk_ms_mod` FOREIGN KEY (`module_id`) REFERENCES `edu_module` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ms_sub` FOREIGN KEY (`subject_id`) REFERENCES `edu_subject` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='栏目-学科可见性（首页浮层/浏览页）';

-- ----------------------------------------------------------
-- edu_subject_resource_type: sync-prep resource types per subject
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `edu_subject_resource_type` (
  `id`               INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `subject_id`       SMALLINT UNSIGNED NOT NULL,
  `resource_type_id` SMALLINT UNSIGNED NOT NULL,
  `sort`             SMALLINT DEFAULT 0,
  `status`           TINYINT  DEFAULT 1 COMMENT '0=hidden 1=visible',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_subject_resource_type` (`subject_id`, `resource_type_id`),
  KEY `idx_resource_type_id` (`resource_type_id`),
  CONSTRAINT `fk_srt_sub` FOREIGN KEY (`subject_id`) REFERENCES `edu_subject` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_srt_type` FOREIGN KEY (`resource_type_id`) REFERENCES `edu_resource_type` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学科同步备课资料类型白名单';

-- Guoxue reading module (primary chinese only by default)
INSERT IGNORE INTO `edu_module` (`id`, `code`, `name`, `icon`, `module_category`, `applicable_stages`, `description`, `sort`, `status`) VALUES
(44, 'guoxue_reading', '国学阅读', '📜', 'reading', '["primary"]', '国学经典阅读', 44, 1);

INSERT IGNORE INTO `edu_module_stage` (`module_id`, `stage_id`, `sort`)
SELECT 44, s.id, 44 FROM `edu_stage` s WHERE s.code = 'primary';

-- Default module-subject: all stage modules -> all subjects in that stage
INSERT IGNORE INTO `edu_module_subject` (`module_id`, `subject_id`, `sort`, `status`)
SELECT ms.module_id, sub.id, ms.sort, 1
FROM `edu_module_stage` ms
JOIN `edu_subject` sub ON sub.stage_id = ms.stage_id;

-- Guoxue reading: primary chinese only
DELETE ms FROM `edu_module_subject` ms
JOIN `edu_subject` sub ON ms.subject_id = sub.id
JOIN `edu_module` m ON ms.module_id = m.id
WHERE m.code = 'guoxue_reading'
  AND NOT (sub.stage_id = (SELECT id FROM `edu_stage` WHERE code = 'primary' LIMIT 1) AND sub.code = 'chinese');

-- Default sync resource types: teach-group leaf types for all subjects
INSERT IGNORE INTO `edu_subject_resource_type` (`subject_id`, `resource_type_id`, `sort`, `status`)
SELECT sub.id, rt.id, rt.sort, 1
FROM `edu_subject` sub
CROSS JOIN `edu_resource_type` rt
WHERE rt.parent_id > 0 AND rt.group_code = 'teach' AND rt.status = 1;
