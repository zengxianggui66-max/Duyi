-- ============================================================
-- 修复：宽表已有 catalog_node_id 但缺少 edu_resource_placement 的资源
-- 执行：mysql -u root -p xinketang < sql/tools/repair_resource_placement.sql
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

INSERT INTO edu_resource_placement (resource_id, catalog_node_id, is_primary, sort)
SELECT r.id, r.catalog_node_id, 1, COALESCE(r.sort, 0)
FROM oss_primary_chinese_resource r
WHERE r.is_deleted = 0
  AND r.catalog_node_id IS NOT NULL
  AND r.catalog_node_id > 0
  AND NOT EXISTS (
    SELECT 1 FROM edu_resource_placement p
    WHERE p.resource_id = r.id AND p.catalog_node_id = r.catalog_node_id
  );

-- 可选：为 OSS 导入补默认 brand_code（七彩系列）
-- UPDATE oss_primary_chinese_resource
-- SET brand_code = 'qicai'
-- WHERE brand_code IS NULL OR TRIM(brand_code) = '';

SELECT COUNT(*) AS placement_rows FROM edu_resource_placement;
