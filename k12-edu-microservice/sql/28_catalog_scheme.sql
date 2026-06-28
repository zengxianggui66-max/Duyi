-- ============================================================
-- M2：目录方案与节点表（方案 A catalog）
-- 执行：mysql -u root -p xinketang < sql/28_catalog_scheme.sql
-- 依赖：27_brand_baseline.sql
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `edu_catalog_scheme` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `code` VARCHAR(50) NOT NULL COMMENT 'textbook_unit | zy_taxonomy | zy_taxonomy_unit',
    `name` VARCHAR(100) NOT NULL,
    `brand_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联 edu_resource_brand.id',
    `display_mode` VARCHAR(30) NOT NULL COMMENT 'lesson_hub | category_list | unit_matrix',
    `meta` JSON DEFAULT NULL,
    `sort` SMALLINT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_scheme_code` (`code`),
    KEY `idx_brand` (`brand_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源目录方案';

CREATE TABLE IF NOT EXISTS `edu_catalog_node` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `scheme_id` INT UNSIGNED NOT NULL,
    `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0,
    `code` VARCHAR(80) NOT NULL,
    `name` VARCHAR(200) NOT NULL,
    `name_path` VARCHAR(800) NOT NULL,
    `depth` TINYINT NOT NULL DEFAULT 0,
    `node_type` VARCHAR(20) NOT NULL COMMENT 'folder|unit|lesson|section|leaf',
    `sort` SMALLINT NOT NULL DEFAULT 0,
    `icon` VARCHAR(20) DEFAULT NULL,
    `meta` JSON DEFAULT NULL,
    `status` TINYINT NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    KEY `idx_scheme_parent` (`scheme_id`, `parent_id`, `sort`),
    KEY `idx_name_path` (`scheme_id`, `name_path`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一目录树节点';

-- scheme 种子（brand_id 按 code 关联）
INSERT INTO `edu_catalog_scheme` (`code`, `name`, `brand_id`, `display_mode`, `meta`, `sort`, `status`)
SELECT 'textbook_unit', '七彩教材单元树', b.id, 'lesson_hub', NULL, 10, 1
FROM `edu_resource_brand` b WHERE b.code = 'qicai'
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `brand_id` = VALUES(`brand_id`),
    `display_mode` = VALUES(`display_mode`),
    `sort` = VALUES(`sort`);

INSERT INTO `edu_catalog_scheme` (`code`, `name`, `brand_id`, `display_mode`, `meta`, `sort`, `status`)
SELECT 'zy_taxonomy', '状元产品分类树', b.id, 'category_list', NULL, 20, 1
FROM `edu_resource_brand` b WHERE b.code = 'zhuangyuan'
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `brand_id` = VALUES(`brand_id`),
    `display_mode` = VALUES(`display_mode`),
    `sort` = VALUES(`sort`);

INSERT INTO `edu_catalog_scheme` (`code`, `name`, `brand_id`, `display_mode`, `meta`, `sort`, `status`)
SELECT 'zy_taxonomy_unit', '状元单元矩阵', b.id, 'unit_matrix', JSON_OBJECT('attachTo', 'lesson_plan_units'), 21, 1
FROM `edu_resource_brand` b WHERE b.code = 'zhuangyuan'
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`),
    `brand_id` = VALUES(`brand_id`),
    `display_mode` = VALUES(`display_mode`),
    `sort` = VALUES(`sort`);
