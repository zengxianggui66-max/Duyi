-- ============================================================
-- M1：品牌维度基线（edu_resource_brand + 宽表 brand_code 等）
-- 执行：mysql -u root -p xinketang < sql/27_brand_baseline.sql
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

-- 1) 品牌主表
CREATE TABLE IF NOT EXISTS `edu_resource_brand` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `code` VARCHAR(32) NOT NULL COMMENT 'qicai | zhuangyuan | platform',
    `name` VARCHAR(50) NOT NULL,
    `publisher` VARCHAR(100) DEFAULT NULL,
    `logo_url` VARCHAR(500) DEFAULT NULL,
    `sort` SMALLINT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_brand_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源品牌/系列';

-- 1.1) 表已存在时补 logo_url（CREATE IF NOT EXISTS 不会改旧表结构）
SET @col_logo := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'xinketang' AND TABLE_NAME = 'edu_resource_brand' AND COLUMN_NAME = 'logo_url'
);
SET @sql_logo := IF(@col_logo = 0,
    'ALTER TABLE `edu_resource_brand` ADD COLUMN `logo_url` VARCHAR(500) DEFAULT NULL COMMENT ''品牌 Logo'' AFTER `publisher`',
    'SELECT ''skip logo_url''');
PREPARE stmt FROM @sql_logo;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2) 宽表扩展（过渡期列表仍读宽表）
SET @col_brand := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'xinketang' AND TABLE_NAME = 'oss_primary_chinese_resource' AND COLUMN_NAME = 'brand_code'
);
SET @sql_brand := IF(@col_brand = 0,
    'ALTER TABLE `oss_primary_chinese_resource` ADD COLUMN `brand_code` VARCHAR(32) DEFAULT NULL COMMENT ''品牌编码'' AFTER `edition`',
    'SELECT ''skip brand_code''');
PREPARE stmt FROM @sql_brand;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_node := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'xinketang' AND TABLE_NAME = 'oss_primary_chinese_resource' AND COLUMN_NAME = 'catalog_node_id'
);
SET @sql_node := IF(@col_node = 0,
    'ALTER TABLE `oss_primary_chinese_resource` ADD COLUMN `catalog_node_id` BIGINT UNSIGNED DEFAULT NULL AFTER `brand_code`',
    'SELECT ''skip catalog_node_id''');
PREPARE stmt FROM @sql_node;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_path := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'xinketang' AND TABLE_NAME = 'oss_primary_chinese_resource' AND COLUMN_NAME = 'catalog_path'
);
SET @sql_path := IF(@col_path = 0,
    'ALTER TABLE `oss_primary_chinese_resource` ADD COLUMN `catalog_path` VARCHAR(500) DEFAULT NULL AFTER `catalog_node_id`',
    'SELECT ''skip catalog_path''');
PREPARE stmt FROM @sql_path;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_sub := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'xinketang' AND TABLE_NAME = 'oss_primary_chinese_resource' AND COLUMN_NAME = 'sub_type'
);
SET @sql_sub := IF(@col_sub = 0,
    'ALTER TABLE `oss_primary_chinese_resource` ADD COLUMN `sub_type` VARCHAR(50) DEFAULT NULL COMMENT ''教案版/精品版/希沃等'' AFTER `catalog_path`',
    'SELECT ''skip sub_type''');
PREPARE stmt FROM @sql_sub;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_brand := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = 'xinketang' AND TABLE_NAME = 'oss_primary_chinese_resource' AND INDEX_NAME = 'idx_brand_browse'
);
SET @sql_idx := IF(@idx_brand = 0,
    'ALTER TABLE `oss_primary_chinese_resource` ADD KEY `idx_brand_browse` (`brand_code`, `grade_name`, `edition`, `module`, `is_deleted`, `status`)',
    'SELECT ''skip idx_brand_browse''');
PREPARE stmt FROM @sql_idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) 品牌种子
INSERT INTO `edu_resource_brand` (`code`, `name`, `publisher`, `sort`, `status`) VALUES
('qicai', '七彩课堂', '时代华语出版社', 10, 1),
('zhuangyuan', '状元版', '状元教育', 20, 1),
('platform', '平台自建', '平台', 30, 1)
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `publisher` = VALUES(`publisher`),
    `sort` = VALUES(`sort`),
    `status` = VALUES(`status`);

-- 4) 历史演示数据默认归入七彩（可按需注释）
UPDATE `oss_primary_chinese_resource`
SET `brand_code` = 'qicai'
WHERE `brand_code` IS NULL AND `is_deleted` = 0;
