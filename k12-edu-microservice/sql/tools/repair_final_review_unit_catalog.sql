-- 修复期末复习「单元复习课件」资源 catalog_node_id / unit_name
-- 场景：OSS 文件在「单元复习课件/第一单元复习课件.pptx」，误挂到父文件夹「单元复习」
-- 用法：mysql -u root -p xinketang < sql/tools/repair_final_review_unit_catalog.sql

USE `xinketang`;
SET NAMES utf8mb4;

-- 按标题关键词将父文件夹下的资源归位到对应叶子节点
UPDATE `oss_primary_chinese_resource` r
INNER JOIN `edu_catalog_node` parent ON parent.id = r.catalog_node_id
  AND parent.code LIKE '%pack_final_review_复习课件_单元复习'
INNER JOIN `edu_catalog_node` leaf ON leaf.parent_id = parent.id
  AND leaf.node_type = 'leaf'
  AND JSON_UNQUOTE(JSON_EXTRACT(leaf.meta, '$.reviewScope')) = 'unit'
SET
  r.catalog_node_id = leaf.id,
  r.unit_name = JSON_UNQUOTE(JSON_EXTRACT(leaf.meta, '$.canonicalUnit'))
WHERE r.module = '期末复习'
  AND r.is_deleted = 0
  AND (
    (JSON_UNQUOTE(JSON_EXTRACT(leaf.meta, '$.canonicalUnit')) LIKE '第一单元%' AND r.title LIKE '%第一单元%')
    OR (JSON_UNQUOTE(JSON_EXTRACT(leaf.meta, '$.canonicalUnit')) = '汉语拼音' AND r.title LIKE '%第二%三%单元%')
    OR (JSON_UNQUOTE(JSON_EXTRACT(leaf.meta, '$.canonicalUnit')) LIKE '第四单元%' AND r.title LIKE '%第四单元%')
    OR (JSON_UNQUOTE(JSON_EXTRACT(leaf.meta, '$.canonicalUnit')) LIKE '第五单元%' AND r.title LIKE '%第五单元%')
    OR (JSON_UNQUOTE(JSON_EXTRACT(leaf.meta, '$.canonicalUnit')) LIKE '第六单元%' AND r.title LIKE '%第六单元%')
    OR (JSON_UNQUOTE(JSON_EXTRACT(leaf.meta, '$.canonicalUnit')) LIKE '第七单元%' AND r.title LIKE '%第七单元%')
    OR (JSON_UNQUOTE(JSON_EXTRACT(leaf.meta, '$.canonicalUnit')) LIKE '第八单元%' AND r.title LIKE '%第八单元%')
    OR (JSON_UNQUOTE(JSON_EXTRACT(leaf.meta, '$.canonicalUnit')) = '我上学了' AND r.title LIKE '%我上学了%')
  );

-- 同步 edu_resource_placement
INSERT INTO `edu_resource_placement` (`resource_id`, `catalog_node_id`, `is_primary`, `sort`)
SELECT r.id, r.catalog_node_id, 1, 0
FROM `oss_primary_chinese_resource` r
WHERE r.module = '期末复习'
  AND r.is_deleted = 0
  AND r.catalog_node_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `edu_resource_placement` p
    WHERE p.resource_id = r.id AND p.catalog_node_id = r.catalog_node_id
  );
