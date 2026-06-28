-- ============================================================
-- 补丁：edu_resource_brand 补 logo_url（M1 脚本若表已存在则不会加列）
-- 执行：mysql -u root -p xinketang < sql/33_patch_brand_logo_url.sql
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

SET @col_logo := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'xinketang'
      AND TABLE_NAME = 'edu_resource_brand'
      AND COLUMN_NAME = 'logo_url'
);
SET @sql_logo := IF(@col_logo = 0,
    'ALTER TABLE `edu_resource_brand` ADD COLUMN `logo_url` VARCHAR(500) DEFAULT NULL COMMENT ''品牌 Logo'' AFTER `publisher`',
    'SELECT ''skip logo_url'' AS msg');
PREPARE stmt FROM @sql_logo;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
