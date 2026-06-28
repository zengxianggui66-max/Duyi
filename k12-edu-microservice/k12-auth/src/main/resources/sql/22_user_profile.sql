-- 用户资料扩展：性别、生日（可重复执行，已存在则跳过）
USE xinketang;

SET @db = DATABASE();

SET @exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user' AND COLUMN_NAME = 'gender'
);
SET @sql = IF(
    @exists = 0,
    'ALTER TABLE `user` ADD COLUMN `gender` TINYINT NOT NULL DEFAULT 0 COMMENT ''0保密 1男 2女'' AFTER `email`',
    'SELECT ''skip: gender already exists'' AS msg'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'user' AND COLUMN_NAME = 'birthday'
);
SET @sql = IF(
    @exists = 0,
    'ALTER TABLE `user` ADD COLUMN `birthday` DATE DEFAULT NULL COMMENT ''生日'' AFTER `gender`',
    'SELECT ''skip: birthday already exists'' AS msg'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
