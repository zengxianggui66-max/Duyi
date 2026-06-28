-- 幂等补全 edu_volume Phase6 字段（taxonomy/volumes 依赖 status 列）
USE `xinketang`;
SET NAMES utf8mb4;

SET @db = DATABASE();

-- status
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'edu_volume' AND COLUMN_NAME = 'status') = 0,
  'ALTER TABLE `edu_volume` ADD COLUMN `status` TINYINT DEFAULT 1 COMMENT ''0停用 1启用'' AFTER `sort`',
  'SELECT ''skip edu_volume.status'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE `edu_volume` SET `status` = 1 WHERE `status` IS NULL;

-- stage_id / subject_id / edition_id（Phase6 可选绑定，管理端用）
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'edu_volume' AND COLUMN_NAME = 'stage_id') = 0,
  'ALTER TABLE `edu_volume` ADD COLUMN `stage_id` TINYINT UNSIGNED DEFAULT NULL COMMENT ''学段ID'' AFTER `status`',
  'SELECT ''skip edu_volume.stage_id'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'edu_volume' AND COLUMN_NAME = 'subject_id') = 0,
  'ALTER TABLE `edu_volume` ADD COLUMN `subject_id` SMALLINT UNSIGNED DEFAULT NULL COMMENT ''学科ID'' AFTER `stage_id`',
  'SELECT ''skip edu_volume.subject_id'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'edu_volume' AND COLUMN_NAME = 'edition_id') = 0,
  'ALTER TABLE `edu_volume` ADD COLUMN `edition_id` SMALLINT UNSIGNED DEFAULT NULL COMMENT ''版本ID'' AFTER `subject_id`',
  'SELECT ''skip edu_volume.edition_id'' AS info'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SELECT 'edu_volume columns ready' AS result;
SHOW COLUMNS FROM `edu_volume`;
