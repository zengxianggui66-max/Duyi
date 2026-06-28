-- 修正 primary_chinese 搜索索引的详情页跳转地址（带完整浏览上下文）
USE xinketang;
SET NAMES utf8mb4;

UPDATE sys_search_document d
INNER JOIN oss_primary_chinese_resource r ON d.biz_id = CAST(r.id AS CHAR)
SET d.route_path = CONCAT(
  '/resource/', r.id,
  '?from=subject&version=tongbian2024',
  CASE r.stage
    WHEN '小学' THEN '&stage=primary'
    WHEN '初中' THEN '&stage=junior'
    WHEN '高中' THEN '&stage=senior'
    WHEN '幼儿' THEN '&stage=preschool'
    WHEN '学前' THEN '&stage=preschool'
    WHEN '美术' THEN '&stage=art'
    WHEN '舞蹈' THEN '&stage=dance'
    ELSE ''
  END,
  CASE r.subject
    WHEN '语文' THEN '&subject=chinese'
    WHEN '数学' THEN '&subject=math'
    WHEN '英语' THEN '&subject=english'
    ELSE ''
  END,
  IF(r.brand_code IS NOT NULL AND r.brand_code != '', CONCAT('&brand=', r.brand_code), ''),
  IF(r.module IS NOT NULL AND r.module != '', CONCAT('&module=', r.module), ''),
  IF(r.type IS NOT NULL AND r.type != '', CONCAT('&type=', r.type), ''),
  IF(r.unit_name IS NOT NULL AND r.unit_name != '', CONCAT('&unit=', r.unit_name), ''),
  IF(r.lesson_name IS NOT NULL AND r.lesson_name != '', CONCAT('&lesson=', r.lesson_name), ''),
  IF(r.catalog_node_id IS NOT NULL AND r.catalog_node_id > 0, CONCAT('&catalogNodeId=', r.catalog_node_id), '')
)
WHERE d.doc_id LIKE 'primary_chinese:%';

SELECT doc_id, title, route_path FROM sys_search_document WHERE biz_id = '20018';
