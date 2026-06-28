-- 修复期末复习「专项复习课件」资源 catalog_node_id
-- OSS 路径：复习课件/专项复习课件/专项N：xxx.ppt（无知识点子文件夹）
-- 用法（PowerShell）：
--   Get-Content sql/tools/repair_final_review_special_catalog.sql -Raw -Encoding UTF8 | mysql -u root -p xinketang --default-character-set=utf8mb4

USE `xinketang`;
SET NAMES utf8mb4;

-- 复习课件 · 专项复习
UPDATE `oss_primary_chinese_resource` r
INNER JOIN `edu_catalog_node` parent ON parent.id = r.catalog_node_id
  AND parent.code LIKE '%pack_final_review%'
  AND parent.code LIKE '%专项复习'
  AND parent.code LIKE '%复习课件%'
INNER JOIN `edu_catalog_node` leaf ON leaf.parent_id = parent.id
  AND leaf.node_type = 'leaf'
  AND leaf.code LIKE '%专项_%'
SET r.catalog_node_id = leaf.id,
    r.unit_name = leaf.name
WHERE r.module = '期末复习' AND r.is_deleted = 0
  AND leaf.name = '拼音与识字' AND (r.title LIKE '%汉语拼音%' OR r.title LIKE '%生字%' OR r.title LIKE '%拼音%');

UPDATE `oss_primary_chinese_resource` r
INNER JOIN `edu_catalog_node` parent ON parent.id = r.catalog_node_id
  AND parent.code LIKE '%pack_final_review%'
  AND parent.code LIKE '%专项复习'
  AND parent.code LIKE '%复习课件%'
INNER JOIN `edu_catalog_node` leaf ON leaf.parent_id = parent.id AND leaf.name = '词语与句子'
SET r.catalog_node_id = leaf.id, r.unit_name = leaf.name
WHERE r.module = '期末复习' AND r.is_deleted = 0 AND r.catalog_node_id = parent.id
  AND (r.title LIKE '%词语%' OR r.title LIKE '%句子%' OR r.title LIKE '%标点%');

UPDATE `oss_primary_chinese_resource` r
INNER JOIN `edu_catalog_node` parent ON parent.id = r.catalog_node_id
  AND parent.code LIKE '%pack_final_review%'
  AND parent.code LIKE '%专项复习'
  AND parent.code LIKE '%复习课件%'
INNER JOIN `edu_catalog_node` leaf ON leaf.parent_id = parent.id AND leaf.name = '阅读与鉴赏'
SET r.catalog_node_id = leaf.id, r.unit_name = leaf.name
WHERE r.module = '期末复习' AND r.is_deleted = 0 AND r.catalog_node_id = parent.id AND r.title LIKE '%阅读%';

UPDATE `oss_primary_chinese_resource` r
INNER JOIN `edu_catalog_node` parent ON parent.id = r.catalog_node_id
  AND parent.code LIKE '%pack_final_review%'
  AND parent.code LIKE '%专项复习'
  AND parent.code LIKE '%复习课件%'
INNER JOIN `edu_catalog_node` leaf ON leaf.parent_id = parent.id AND leaf.name = '口语交际'
SET r.catalog_node_id = leaf.id, r.unit_name = leaf.name
WHERE r.module = '期末复习' AND r.is_deleted = 0 AND r.catalog_node_id = parent.id AND r.title LIKE '%口语交际%';

UPDATE `oss_primary_chinese_resource` r
INNER JOIN `edu_catalog_node` parent ON parent.id = r.catalog_node_id
  AND parent.code LIKE '%pack_final_review%'
  AND parent.code LIKE '%专项复习'
  AND parent.code LIKE '%复习课件%'
INNER JOIN `edu_catalog_node` leaf ON leaf.parent_id = parent.id AND leaf.name = '习作'
SET r.catalog_node_id = leaf.id, r.unit_name = leaf.name
WHERE r.module = '期末复习' AND r.is_deleted = 0 AND r.catalog_node_id = parent.id
  AND (r.title LIKE '%写话%' OR r.title LIKE '%看图%');

UPDATE `oss_primary_chinese_resource` r
INNER JOIN `edu_catalog_node` parent ON parent.id = r.catalog_node_id
  AND parent.code LIKE '%pack_final_review%'
  AND parent.code LIKE '%专项复习'
  AND parent.code LIKE '%复习课件%'
INNER JOIN `edu_catalog_node` leaf ON leaf.parent_id = parent.id AND leaf.name = '古诗与积累'
SET r.catalog_node_id = leaf.id, r.unit_name = leaf.name
WHERE r.module = '期末复习' AND r.is_deleted = 0 AND r.catalog_node_id = parent.id
  AND (r.title LIKE '%积累%' OR r.title LIKE '%背诵%' OR r.title LIKE '%古诗%');

INSERT INTO `edu_resource_placement` (`resource_id`, `catalog_node_id`, `is_primary`, `sort`)
SELECT r.id, r.catalog_node_id, 1, 0
FROM `oss_primary_chinese_resource` r
WHERE r.module = '期末复习' AND r.is_deleted = 0 AND r.catalog_node_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `edu_resource_placement` p
    WHERE p.resource_id = r.id AND p.catalog_node_id = r.catalog_node_id
  );
