-- P2.5：搜索性能指标字段 + 索引数据质量修正
-- mysql -u root -p xinketang --default-character-set=utf8mb4
-- source sql/46_search_p25_metrics_and_quality.sql
USE xinketang;
SET NAMES utf8mb4;

-- ========== 1. 查询日志扩展 ==========
SET @db := DATABASE();

SET @col := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=@db AND TABLE_NAME='search_query_log' AND COLUMN_NAME='content_domain');
SET @sql := IF(@col=0,
  'ALTER TABLE search_query_log ADD COLUMN content_domain VARCHAR(32) DEFAULT NULL COMMENT ''内容域 stage_resource/feature/prep/news'' AFTER intent_json',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ========== 2. 点击日志扩展 ==========
SET @col := (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA=@db AND TABLE_NAME='search_click_log' AND COLUMN_NAME='normalized_keyword');
SET @sql := IF(@col=0,
  'ALTER TABLE search_click_log ADD COLUMN normalized_keyword VARCHAR(255) DEFAULT NULL COMMENT ''归一化搜索词'' AFTER keyword, ADD COLUMN content_domain VARCHAR(32) DEFAULT NULL COMMENT ''内容域'' AFTER normalized_keyword',
  'SELECT 1');
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- ========== 3. 资源类型 key 归一（教学设计 → lesson_plan） ==========
UPDATE sys_search_document
SET resource_type_key = 'lesson_plan',
    resource_type_name = '教案'
WHERE status = 1 AND is_deleted = 0
  AND (resource_type_name IN ('教学设计', '教案') OR resource_type_name LIKE '%教学设计%')
  AND (resource_type_key IS NULL OR resource_type_key IN ('', 'lesson_plan', '教学设计'));

UPDATE sys_search_document
SET resource_type_key = 'courseware'
WHERE status = 1 AND is_deleted = 0
  AND resource_type_name IN ('课件', 'ppt', 'PPT')
  AND (resource_type_key IS NULL OR resource_type_key = '');

-- ========== 4. 生涯规划路由对齐 /feature/shengya ==========
UPDATE sys_search_document
SET route_path = '/feature/shengya'
WHERE channel_key = 'career' AND (route_path IS NULL OR route_path = '/topic');

UPDATE sys_search_intent_rule
SET target_payload = JSON_OBJECT('route_path', '/feature/shengya', 'contentDomain', 'feature')
WHERE pattern IN ('生涯规划', '职业规划', '选科', '志愿填报') AND intent_type = 'channel';

-- ========== 5. 静态 career 频道索引补路由 ==========
INSERT INTO sys_search_document (
  doc_id, doc_type, biz_id, title, subtitle, summary, content_text, keyword_text,
  channel_key, channel_name, route_path, status, is_deleted, quality_score, hot_score, publish_time
) VALUES
('channel:career', 'channel', 'career', '生涯规划', '特色频道 / 升学指导', '生涯规划与选科志愿填报', '生涯规划,选科,志愿填报,升学', '生涯,职业规划,选科,升学', 'career', '生涯规划', '/feature/shengya', 1, 0, 2, 10, NOW())
ON DUPLICATE KEY UPDATE route_path='/feature/shengya', channel_key='career', status=1, is_deleted=0;

SELECT 'query_log_cols' AS k, GROUP_CONCAT(COLUMN_NAME ORDER BY ORDINAL_POSITION) AS v
FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='search_query_log'
UNION ALL
SELECT 'click_log_cols', GROUP_CONCAT(COLUMN_NAME ORDER BY ORDINAL_POSITION)
FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='search_click_log'
UNION ALL
SELECT 'lesson_plan_key_count', CAST(COUNT(*) AS CHAR) FROM sys_search_document WHERE resource_type_key='lesson_plan' AND status=1
UNION ALL
SELECT 'career_route_ok', CAST(COUNT(*) AS CHAR) FROM sys_search_document WHERE doc_id='channel:career' AND route_path='/feature/shengya';
