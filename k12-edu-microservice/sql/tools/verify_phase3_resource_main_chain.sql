-- ============================================================
-- Phase 3 resource main chain verification (read-only)
-- Usage:
--   mysql -u root -p xinketang < sql/tools/verify_phase3_resource_main_chain.sql
-- Phase 3K: T3 四源孤儿 + T3b primary_chinese 孤儿 + T3c COMMENT 标记
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

SELECT 'T1 resource_main source coverage' AS section;
SELECT source_type, COUNT(*) AS cnt
FROM resource_main
GROUP BY source_type
ORDER BY source_type;

SELECT 'T2 v_admin_resource_main source coverage' AS section;
SELECT source_type, COUNT(*) AS cnt
FROM v_admin_resource_main
GROUP BY source_type
ORDER BY source_type;

SELECT 'T3 unmapped legacy source rows (topic/culture/competition/edu)' AS section;
SELECT 'topic_resource' AS source_type, COUNT(*) AS unmapped
FROM topic_resource t
LEFT JOIN resource_main rm
  ON rm.source_type = 'topic_resource'
 AND rm.source_id = t.id
WHERE t.is_deleted = 0
  AND rm.id IS NULL
UNION ALL
SELECT 'culture_resource', COUNT(*)
FROM culture_resource c
LEFT JOIN resource_main rm
  ON rm.source_type = 'culture_resource'
 AND rm.source_id = c.id
WHERE c.is_deleted = 0
  AND rm.id IS NULL
UNION ALL
SELECT 'competition_resource', COUNT(*)
FROM competition_resource c
LEFT JOIN resource_main rm
  ON rm.source_type = 'competition_resource'
 AND rm.source_id = c.id
WHERE c.is_deleted = 0
  AND rm.id IS NULL
UNION ALL
SELECT 'edu_resource', COUNT(*)
FROM edu_resource e
LEFT JOIN resource_main rm
  ON rm.source_type = 'edu_resource'
 AND rm.source_id = e.id
WHERE e.is_deleted = 0
  AND rm.id IS NULL;

SELECT 'T3b primary_chinese unmapped' AS section;
SELECT COUNT(*) AS primary_chinese_unmapped
FROM oss_primary_chinese_resource p
LEFT JOIN resource_main rm
  ON rm.source_type = 'primary_chinese'
 AND rm.source_id = p.id
WHERE p.is_deleted = 0
  AND rm.id IS NULL;

SELECT 'T3c tier comment markers (Phase 3K)' AS section;
SELECT TABLE_NAME, LEFT(IFNULL(TABLE_COMMENT, ''), 80) AS comment_prefix
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN (
    'resource_main', 'edu_resource', 'edu_resource_file', 'edu_resource_dimension', 'edu_resource_placement',
    'oss_primary_chinese_resource', 'topic_resource', 'culture_resource', 'competition_resource', 'article',
    'sys_search_document',
    'resource_category', 'edu_resource_tag', 'edu_resource_region', 'edu_resource_search_flat'
  )
ORDER BY TABLE_NAME;

SELECT 'T4 migration quality metrics' AS section;
SELECT metric_key, metric_value
FROM v_resource_migration_quality
ORDER BY metric_key;

SELECT 'T5 unified read feature flags' AS section;
SELECT config_key, config_value
FROM sys_config
WHERE config_key IN (
  'feature.resourceUnifiedRead.enabled',
  'feature.primaryChineseUnifiedRead.enabled',
  'feature.topicUnifiedRead.enabled',
  'feature.cultureUnifiedRead.enabled',
  'feature.competitionUnifiedRead.enabled'
)
ORDER BY config_key;

SELECT 'T6 legacy api usage recent 7 days' AS section;
SELECT api_path, SUM(hit_count) AS hit_count_7d, MAX(last_hit_time) AS last_hit_time
FROM legacy_api_usage_stat
WHERE stat_date >= DATE_SUB(CURRENT_DATE(), INTERVAL 7 DAY)
GROUP BY api_path
ORDER BY hit_count_7d DESC, api_path;
