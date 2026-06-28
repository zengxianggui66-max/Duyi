-- ============================================================
-- M3 可选：按课文名/节点回填 placement + catalog_path
-- 执行：mysql -u root -p xinketang < sql/31_seed_placement_backfill.sql
-- 依赖：29/30 目录种子 + 宽表已有资源
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

-- 1) 宽表 catalog_node_id / catalog_path 与 lesson 节点对齐
UPDATE `oss_primary_chinese_resource` r
INNER JOIN `edu_catalog_node` n
    ON n.`name` = r.`lesson_name` AND n.`node_type` = 'lesson' AND n.`status` = 1
SET
    r.`catalog_node_id` = n.`id`,
    r.`catalog_path` = n.`name_path`
WHERE r.`is_deleted` = 0
  AND r.`lesson_name` IS NOT NULL
  AND r.`lesson_name` != ''
  AND (r.`catalog_node_id` IS NULL OR r.`catalog_path` IS NULL);

-- 2) 写入 placement（幂等）
INSERT IGNORE INTO `edu_resource_placement` (`resource_id`, `catalog_node_id`, `is_primary`, `sort`)
SELECT r.`id`, n.`id`, 1, 0
FROM `oss_primary_chinese_resource` r
INNER JOIN `edu_catalog_node` n
    ON n.`name` = r.`lesson_name` AND n.`node_type` = 'lesson' AND n.`status` = 1
WHERE r.`is_deleted` = 0
  AND r.`lesson_name` IS NOT NULL
  AND r.`lesson_name` != '';

-- 3) 已有 catalog_node_id 的资源补 placement
INSERT IGNORE INTO `edu_resource_placement` (`resource_id`, `catalog_node_id`, `is_primary`, `sort`)
SELECT r.`id`, r.`catalog_node_id`, 1, 0
FROM `oss_primary_chinese_resource` r
WHERE r.`is_deleted` = 0
  AND r.`catalog_node_id` IS NOT NULL
  AND r.`catalog_node_id` > 0;
