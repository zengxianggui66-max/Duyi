-- ============================================================
-- 86：Phase 3B — 资源主链路统一（数据库梳理与回填草案）
-- 目标：
--   1) 补齐 resource_main 统一字段（幂等）
--   2) 回填 topic/culture/competition/edu_resource 到 resource_main（幂等）
--   3) 扩展 v_admin_resource_main 为多来源统一视图
--
-- 执行：
--   mysql -u root -pzxg123456 xinketang < sql/86_phase3b_resource_main_chain.sql
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------
-- 0. 前置检查（无 resource_main 则停止）
-- ----------------------------------------------------------
SELECT '>>> check resource_main exists' AS info;
SELECT COUNT(*) AS resource_main_exists
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'resource_main';

-- ----------------------------------------------------------
-- 1. resource_main 结构补齐（幂等：使用存储过程跳过已存在的列，MySQL不支持ADD COLUMN IF NOT EXISTS）
-- ----------------------------------------------------------
DROP PROCEDURE IF EXISTS `_add_col`;
DELIMITER $$
CREATE PROCEDURE `_add_col`(IN `_tbl` VARCHAR(64), IN `_col` VARCHAR(64), IN `_def` TEXT)
BEGIN
  DECLARE `_cnt` INT DEFAULT 0;
  SELECT COUNT(*) INTO `_cnt`
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = `_tbl`
      AND COLUMN_NAME = `_col`;
  IF `_cnt` = 0 THEN
    SET @_sql = CONCAT('ALTER TABLE `', `_tbl`, '` ADD COLUMN `', `_col`, '` ', `_def`);
    PREPARE _stmt FROM @_sql;
    EXECUTE _stmt;
    DEALLOCATE PREPARE _stmt;
  ELSE
    SELECT CONCAT('列 ', `_tbl`, '.', `_col`, ' 已存在，跳过') AS info;
  END IF;
END$$
DELIMITER ;

CALL `_add_col`('resource_main', 'content_domain',       'VARCHAR(40)  DEFAULT NULL COMMENT ''内容域'' AFTER `source_type`');
CALL `_add_col`('resource_main', 'canonical_resource_id', 'BIGINT UNSIGNED DEFAULT NULL COMMENT ''统一资源ID（默认同 id）'' AFTER `id`');
CALL `_add_col`('resource_main', 'legacy_source_table',   'VARCHAR(80)  DEFAULT NULL COMMENT ''迁移期来源表追踪'' AFTER `source_table`');
CALL `_add_col`('resource_main', 'legacy_source_id',      'BIGINT UNSIGNED DEFAULT NULL COMMENT ''迁移期来源ID追踪'' AFTER `source_id`');

DROP PROCEDURE IF EXISTS `_add_col`;

UPDATE `resource_main`
SET `canonical_resource_id` = `id`
WHERE `canonical_resource_id` IS NULL;

-- ----------------------------------------------------------
-- 2. 从 topic_resource 回填（幂等）
-- ----------------------------------------------------------
INSERT INTO `resource_main`
(`source_type`,`content_domain`,`source_table`,`legacy_source_table`,`source_id`,`legacy_source_id`,
 `title`,`stage_code`,`subject_code`,`audit_status`,`publish_status`,`legacy_status`,
 `uploader_id`,`upload_time`,`is_deleted`,`canonical_resource_id`)
SELECT
  'topic_resource',
  'topic',
  'topic_resource',
  'topic_resource',
  t.id,
  t.id,
  t.title,
  t.grade_stage,
  t.subject,
  CASE WHEN t.status = 1 THEN 1 ELSE 0 END,
  CASE WHEN t.status = 1 THEN 1 ELSE 0 END,
  t.status,
  NULL,
  t.create_time,
  t.is_deleted,
  NULL
FROM `topic_resource` t
ON DUPLICATE KEY UPDATE
  `content_domain` = VALUES(`content_domain`),
  `title` = VALUES(`title`),
  `stage_code` = VALUES(`stage_code`),
  `subject_code` = VALUES(`subject_code`),
  `audit_status` = VALUES(`audit_status`),
  `publish_status` = VALUES(`publish_status`),
  `legacy_status` = VALUES(`legacy_status`),
  `is_deleted` = VALUES(`is_deleted`);

-- ----------------------------------------------------------
-- 3. 从 culture_resource 回填（幂等）
-- ----------------------------------------------------------
INSERT INTO `resource_main`
(`source_type`,`content_domain`,`source_table`,`legacy_source_table`,`source_id`,`legacy_source_id`,
 `title`,`stage_code`,`subject_code`,`audit_status`,`publish_status`,`legacy_status`,
 `uploader_id`,`upload_time`,`is_deleted`,`canonical_resource_id`)
SELECT
  'culture_resource',
  'culture',
  'culture_resource',
  'culture_resource',
  c.id,
  c.id,
  c.title,
  NULL,
  NULL,
  CASE WHEN c.status = 1 THEN 1 ELSE 0 END,
  CASE WHEN c.status = 1 THEN 1 ELSE 0 END,
  c.status,
  NULL,
  c.create_time,
  c.is_deleted,
  NULL
FROM `culture_resource` c
ON DUPLICATE KEY UPDATE
  `content_domain` = VALUES(`content_domain`),
  `title` = VALUES(`title`),
  `audit_status` = VALUES(`audit_status`),
  `publish_status` = VALUES(`publish_status`),
  `legacy_status` = VALUES(`legacy_status`),
  `is_deleted` = VALUES(`is_deleted`);

-- ----------------------------------------------------------
-- 4. 从 competition_resource 回填（幂等）
-- ----------------------------------------------------------
INSERT INTO `resource_main`
(`source_type`,`content_domain`,`source_table`,`legacy_source_table`,`source_id`,`legacy_source_id`,
 `title`,`stage_code`,`subject_code`,`audit_status`,`publish_status`,`legacy_status`,
 `uploader_id`,`upload_time`,`is_deleted`,`canonical_resource_id`)
SELECT
  'competition_resource',
  'competition',
  'competition_resource',
  'competition_resource',
  cp.id,
  cp.id,
  cp.title,
  cp.grade_stage,
  cp.subject,
  CASE WHEN cp.status = 1 THEN 1 ELSE 0 END,
  CASE WHEN cp.status = 1 THEN 1 ELSE 0 END,
  cp.status,
  NULL,
  cp.create_time,
  cp.is_deleted,
  NULL
FROM `competition_resource` cp
ON DUPLICATE KEY UPDATE
  `content_domain` = VALUES(`content_domain`),
  `title` = VALUES(`title`),
  `stage_code` = VALUES(`stage_code`),
  `subject_code` = VALUES(`subject_code`),
  `audit_status` = VALUES(`audit_status`),
  `publish_status` = VALUES(`publish_status`),
  `legacy_status` = VALUES(`legacy_status`),
  `is_deleted` = VALUES(`is_deleted`);

-- ----------------------------------------------------------
-- 5. 从 edu_resource 回填（幂等）
-- ----------------------------------------------------------
INSERT INTO `resource_main`
(`source_type`,`content_domain`,`source_table`,`legacy_source_table`,`source_id`,`legacy_source_id`,
 `title`,`stage_code`,`subject_code`,`audit_status`,`publish_status`,`legacy_status`,
 `uploader_id`,`upload_time`,`is_deleted`,`canonical_resource_id`)
SELECT
  'edu_resource',
  'subject_resource',
  'edu_resource',
  'edu_resource',
  e.id,
  e.id,
  e.title,
  CAST(rd.stage_id AS CHAR),
  CAST(rd.subject_id AS CHAR),
  CASE WHEN e.status IN (1,3,4) THEN 1 WHEN e.status = 2 THEN 2 WHEN e.status = -1 THEN -1 ELSE 0 END,
  CASE WHEN e.status = 1 THEN 1 WHEN e.status = 3 THEN 2 WHEN e.status = 4 THEN 4 ELSE 0 END,
  e.status,
  e.uploader_id,
  e.upload_time,
  e.is_deleted,
  NULL
FROM `edu_resource` e
LEFT JOIN `edu_resource_dimension` rd ON rd.resource_id = e.id
ON DUPLICATE KEY UPDATE
  `content_domain` = VALUES(`content_domain`),
  `title` = VALUES(`title`),
  `stage_code` = VALUES(`stage_code`),
  `subject_code` = VALUES(`subject_code`),
  `audit_status` = VALUES(`audit_status`),
  `publish_status` = VALUES(`publish_status`),
  `legacy_status` = VALUES(`legacy_status`),
  `uploader_id` = VALUES(`uploader_id`),
  `upload_time` = VALUES(`upload_time`),
  `is_deleted` = VALUES(`is_deleted`);

UPDATE `resource_main`
SET `canonical_resource_id` = `id`
WHERE `canonical_resource_id` IS NULL;

-- ----------------------------------------------------------
-- 6. 扩展统一视图 v_admin_resource_main（多来源）
-- ----------------------------------------------------------
CREATE OR REPLACE VIEW `v_admin_resource_main` AS

-- 6.1 primary_chinese
SELECT
  rm.id AS global_id,
  rm.source_type,
  rm.source_table,
  rm.source_id,
  r.title,
  r.stage,
  r.subject,
  r.module,
  r.type,
  r.grade_name,
  r.edition,
  r.brand_code,
  r.sub_type,
  r.unit_name,
  r.lesson_name,
  r.catalog_node_id,
  r.catalog_path,
  r.original_filename,
  r.file_ext,
  r.oss_bucket,
  r.oss_object_key,
  r.oss_url,
  r.file_size_kb,
  r.status AS legacy_status,
  r.audit_status,
  r.publish_status,
  r.uploader_id,
  r.upload_time,
  r.update_time,
  r.is_deleted,
  r.sort,
  r.remark,
  r.is_recommend,
  r.is_top,
  r.top_sort,
  r.is_free,
  r.download_count,
  r.view_count,
  r.allow_preview,
  r.preview_status,
  r.file_safety_status,
  r.lesson_plan_json
FROM `resource_main` rm
INNER JOIN `oss_primary_chinese_resource` r
  ON r.id = rm.source_id
 AND rm.source_type = 'primary_chinese'
 AND rm.is_deleted = 0

UNION ALL

-- 6.2 topic_resource
SELECT
  rm.id AS global_id,
  rm.source_type,
  rm.source_table,
  rm.source_id,
  t.title,
  t.grade_stage AS stage,
  t.subject,
  t.category AS module,
  t.resource_form AS type,
  NULL AS grade_name,
  NULL AS edition,
  NULL AS brand_code,
  NULL AS sub_type,
  NULL AS unit_name,
  NULL AS lesson_name,
  NULL AS catalog_node_id,
  NULL AS catalog_path,
  NULL AS original_filename,
  t.file_format AS file_ext,
  NULL AS oss_bucket,
  NULL AS oss_object_key,
  t.file_url AS oss_url,
  0 AS file_size_kb,
  t.status AS legacy_status,
  CASE WHEN t.status = 1 THEN 1 ELSE 0 END AS audit_status,
  CASE WHEN t.status = 1 THEN 1 ELSE 0 END AS publish_status,
  NULL AS uploader_id,
  t.create_time AS upload_time,
  t.update_time,
  t.is_deleted,
  t.sort,
  t.summary AS remark,
  t.is_elite AS is_recommend,
  0 AS is_top,
  0 AS top_sort,
  t.is_free,
  t.download_count,
  t.view_count,
  1 AS allow_preview,
  1 AS preview_status,
  0 AS file_safety_status,
  NULL AS lesson_plan_json
FROM `resource_main` rm
INNER JOIN `topic_resource` t
  ON t.id = rm.source_id
 AND rm.source_type = 'topic_resource'
 AND rm.is_deleted = 0

UNION ALL

-- 6.3 culture_resource
SELECT
  rm.id AS global_id,
  rm.source_type,
  rm.source_table,
  rm.source_id,
  c.title,
  NULL AS stage,
  NULL AS subject,
  c.category AS module,
  c.resource_kind AS type,
  NULL AS grade_name,
  NULL AS edition,
  NULL AS brand_code,
  NULL AS sub_type,
  NULL AS unit_name,
  NULL AS lesson_name,
  NULL AS catalog_node_id,
  NULL AS catalog_path,
  NULL AS original_filename,
  c.file_format AS file_ext,
  NULL AS oss_bucket,
  NULL AS oss_object_key,
  IF(c.resource_kind='external', c.external_url, c.file_url) AS oss_url,
  0 AS file_size_kb,
  c.status AS legacy_status,
  CASE WHEN c.status = 1 THEN 1 ELSE 0 END AS audit_status,
  CASE WHEN c.status = 1 THEN 1 ELSE 0 END AS publish_status,
  NULL AS uploader_id,
  c.create_time AS upload_time,
  c.update_time,
  c.is_deleted,
  c.sort,
  c.summary AS remark,
  c.is_elite AS is_recommend,
  0 AS is_top,
  0 AS top_sort,
  c.is_free,
  c.download_count,
  c.view_count,
  IF(c.resource_kind='external', 0, 1) AS allow_preview,
  IF(c.resource_kind='external', 0, 1) AS preview_status,
  0 AS file_safety_status,
  NULL AS lesson_plan_json
FROM `resource_main` rm
INNER JOIN `culture_resource` c
  ON c.id = rm.source_id
 AND rm.source_type = 'culture_resource'
 AND rm.is_deleted = 0

UNION ALL

-- 6.4 competition_resource
SELECT
  rm.id AS global_id,
  rm.source_type,
  rm.source_table,
  rm.source_id,
  cp.title,
  cp.grade_stage AS stage,
  cp.subject,
  cp.category AS module,
  cp.resource_form AS type,
  NULL AS grade_name,
  NULL AS edition,
  NULL AS brand_code,
  NULL AS sub_type,
  NULL AS unit_name,
  NULL AS lesson_name,
  NULL AS catalog_node_id,
  NULL AS catalog_path,
  NULL AS original_filename,
  cp.file_format AS file_ext,
  NULL AS oss_bucket,
  NULL AS oss_object_key,
  cp.file_url AS oss_url,
  0 AS file_size_kb,
  cp.status AS legacy_status,
  CASE WHEN cp.status = 1 THEN 1 ELSE 0 END AS audit_status,
  CASE WHEN cp.status = 1 THEN 1 ELSE 0 END AS publish_status,
  NULL AS uploader_id,
  cp.create_time AS upload_time,
  cp.update_time,
  cp.is_deleted,
  cp.sort,
  cp.summary AS remark,
  cp.is_elite AS is_recommend,
  0 AS is_top,
  0 AS top_sort,
  cp.is_free,
  cp.download_count,
  cp.view_count,
  1 AS allow_preview,
  1 AS preview_status,
  0 AS file_safety_status,
  NULL AS lesson_plan_json
FROM `resource_main` rm
INNER JOIN `competition_resource` cp
  ON cp.id = rm.source_id
 AND rm.source_type = 'competition_resource'
 AND rm.is_deleted = 0

UNION ALL

-- 6.5 edu_resource
SELECT
  rm.id AS global_id,
  rm.source_type,
  rm.source_table,
  rm.source_id,
  e.title,
  st.name AS stage,
  sb.name AS subject,
  md.name AS module,
  rt.name AS type,
  gd.name AS grade_name,
  ed.name AS edition,
  NULL AS brand_code,
  NULL AS sub_type,
  NULL AS unit_name,
  NULL AS lesson_name,
  p.catalog_node_id,
  NULL AS catalog_path,
  e.original_filename,
  e.file_ext,
  e.oss_bucket,
  e.oss_object_key,
  e.oss_url,
  e.file_size_kb,
  e.status AS legacy_status,
  CASE WHEN e.status IN (1,3,4) THEN 1 WHEN e.status = 2 THEN 2 WHEN e.status = -1 THEN -1 ELSE 0 END AS audit_status,
  CASE WHEN e.status = 1 THEN 1 WHEN e.status = 3 THEN 2 WHEN e.status = 4 THEN 4 ELSE 0 END AS publish_status,
  e.uploader_id,
  e.upload_time,
  e.update_time,
  e.is_deleted,
  e.sort,
  e.remark,
  0 AS is_recommend,
  0 AS is_top,
  0 AS top_sort,
  e.is_free,
  e.download_count,
  e.view_count,
  1 AS allow_preview,
  1 AS preview_status,
  0 AS file_safety_status,
  e.lesson_plan_json
FROM `resource_main` rm
INNER JOIN `edu_resource` e
  ON e.id = rm.source_id
 AND rm.source_type = 'edu_resource'
 AND rm.is_deleted = 0
LEFT JOIN `edu_resource_dimension` d ON d.resource_id = e.id
LEFT JOIN `edu_stage` st ON st.id = d.stage_id
LEFT JOIN `edu_subject` sb ON sb.id = d.subject_id
LEFT JOIN `edu_module` md ON md.id = d.module_id
LEFT JOIN `edu_resource_type` rt ON rt.id = d.resource_type_id
LEFT JOIN `edu_grade` gd ON gd.id = d.grade_id
LEFT JOIN `edu_edition` ed ON ed.id = d.edition_id
LEFT JOIN `edu_resource_placement` p ON p.resource_id = e.id AND p.is_primary = 1;

-- ----------------------------------------------------------
-- 7. 校验
-- ----------------------------------------------------------
SELECT '>>> resource_main by source_type' AS info;
SELECT source_type, COUNT(*) AS cnt
FROM `resource_main`
WHERE is_deleted = 0
GROUP BY source_type
ORDER BY cnt DESC;

SELECT '>>> unmapped check' AS info;
SELECT 'topic_unmapped' AS k, COUNT(*) AS v
FROM topic_resource t
LEFT JOIN resource_main rm ON rm.source_type='topic_resource' AND rm.source_id=t.id
WHERE t.is_deleted=0 AND rm.id IS NULL
UNION ALL
SELECT 'culture_unmapped', COUNT(*)
FROM culture_resource c
LEFT JOIN resource_main rm ON rm.source_type='culture_resource' AND rm.source_id=c.id
WHERE c.is_deleted=0 AND rm.id IS NULL
UNION ALL
SELECT 'competition_unmapped', COUNT(*)
FROM competition_resource c
LEFT JOIN resource_main rm ON rm.source_type='competition_resource' AND rm.source_id=c.id
WHERE c.is_deleted=0 AND rm.id IS NULL
UNION ALL
SELECT 'edu_unmapped', COUNT(*)
FROM edu_resource e
LEFT JOIN resource_main rm ON rm.source_type='edu_resource' AND rm.source_id=e.id
WHERE e.is_deleted=0 AND rm.id IS NULL;

SELECT '>>> v_admin_resource_main by source_type' AS info;
SELECT source_type, COUNT(*) AS cnt
FROM `v_admin_resource_main`
GROUP BY source_type
ORDER BY cnt DESC;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 回滚建议（按需手工）：
-- 1) 仅回滚视图：用 sql/83_phase7_resource_main.sql 重建 v_admin_resource_main
-- 2) 仅回滚数据：DELETE FROM resource_main WHERE source_type IN ('topic_resource','culture_resource','competition_resource','edu_resource');
-- 3) 仅回滚新增字段：ALTER TABLE resource_main DROP COLUMN content_domain, DROP COLUMN canonical_resource_id, DROP COLUMN legacy_source_table, DROP COLUMN legacy_source_id;
-- ============================================================

