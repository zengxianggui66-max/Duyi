-- ============================================================
-- M3：资源挂载表（catalog 节点 ↔ 宽表资源）
-- 执行：mysql -u root -p xinketang < sql/31_resource_placement.sql
-- 依赖：28_catalog_scheme.sql
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `edu_resource_placement` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `resource_id` BIGINT UNSIGNED NOT NULL COMMENT 'oss_primary_chinese_resource.id',
    `catalog_node_id` BIGINT UNSIGNED NOT NULL,
    `is_primary` TINYINT NOT NULL DEFAULT 1 COMMENT '1=主挂载',
    `module_id` SMALLINT UNSIGNED DEFAULT NULL,
    `resource_type_id` SMALLINT UNSIGNED DEFAULT NULL,
    `sort` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_res_node` (`resource_id`, `catalog_node_id`),
    KEY `idx_node` (`catalog_node_id`),
    KEY `idx_resource` (`resource_id`),
    KEY `idx_placement_browse` (`catalog_node_id`, `resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源目录挂载';
