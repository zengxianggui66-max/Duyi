-- ============================================================
-- 90: Phase 3C - resource quality cleanup
-- This incremental script is safe to rerun.
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

START TRANSACTION;

CREATE TABLE IF NOT EXISTS `phase3c_quality_cleanup_audit` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `batch_no` VARCHAR(64) NOT NULL,
  `action_type` VARCHAR(64) NOT NULL,
  `source_type` VARCHAR(64) NOT NULL,
  `source_id` BIGINT UNSIGNED NOT NULL,
  `global_id` BIGINT UNSIGNED DEFAULT NULL,
  `old_catalog_node_id` BIGINT UNSIGNED DEFAULT NULL,
  `new_catalog_node_id` BIGINT UNSIGNED DEFAULT NULL,
  `old_catalog_path` VARCHAR(500) DEFAULT NULL,
  `new_catalog_path` VARCHAR(500) DEFAULT NULL,
  `old_is_deleted` TINYINT DEFAULT NULL,
  `new_is_deleted` TINYINT DEFAULT NULL,
  `note` VARCHAR(1000) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_batch_action_source` (`batch_no`, `action_type`, `source_type`, `source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Phase 3C resource quality cleanup audit';

SET @batch_no := 'phase3c_quality_cleanup_20260627';
SET @governance_scheme_name := CONVERT(0xE8B584E6BA90E6B2BBE79086E79BAEE5BD95 USING utf8mb4);
SET @governance_node_name := CONVERT(0xE5BE85E5BD92E6A1A3E8B584E6BA90 USING utf8mb4);
SET @governance_node_path := CONVERT(0x2FE8B584E6BA90E6B2BBE79086E79BAEE5BD952FE5BE85E5BD92E6A1A3E8B584E6BA90 USING utf8mb4);

SET @scheme_id := (
  SELECT `id` FROM `edu_catalog_scheme`
  WHERE `code` = 'resource_governance'
  LIMIT 1
);

INSERT INTO `edu_catalog_scheme` (`code`, `name`, `display_mode`, `meta`, `sort`, `status`)
SELECT
  'resource_governance',
  @governance_scheme_name,
  'quality_governance',
  JSON_OBJECT('scope', 'resource_quality', 'phase', 'Phase3C'),
  900,
  1
WHERE @scheme_id IS NULL;

SET @scheme_id := (
  SELECT `id` FROM `edu_catalog_scheme`
  WHERE `code` = 'resource_governance'
  LIMIT 1
);

SET @governance_node_id := (
  SELECT `id` FROM `edu_catalog_node`
  WHERE `scheme_id` = @scheme_id
    AND `code` = 'phase3_unclassified_resources'
  LIMIT 1
);

INSERT INTO `edu_catalog_node` (
  `scheme_id`, `parent_id`, `code`, `name`, `name_path`, `depth`, `node_type`, `sort`, `meta`, `status`
)
SELECT
  @scheme_id,
  0,
  'phase3_unclassified_resources',
  @governance_node_name,
  @governance_node_path,
  0,
  'folder',
  1,
  JSON_OBJECT('purpose', 'phase3_quality_cleanup', 'autoAssign', true),
  1
WHERE @governance_node_id IS NULL;

SET @governance_node_id := (
  SELECT `id` FROM `edu_catalog_node`
  WHERE `scheme_id` = @scheme_id
    AND `code` = 'phase3_unclassified_resources'
  LIMIT 1
);

CREATE TEMPORARY TABLE `tmp_phase3c_orphan_fix` (
  `source_id` BIGINT UNSIGNED NOT NULL PRIMARY KEY,
  `catalog_node_id` BIGINT UNSIGNED NOT NULL,
  `catalog_path` VARCHAR(500) NOT NULL,
  `unit_name` VARCHAR(100) DEFAULT NULL,
  `lesson_name` VARCHAR(128) DEFAULT NULL
) ENGINE=Memory;

INSERT INTO `tmp_phase3c_orphan_fix`
VALUES
  (10013, 2580, CONVERT(0x2FE8AFADE69687EFBC88E7BB9FE7BC96E78988EFBC892FE68891E4B88AE5ADA6E4BA862FE68891E698AFE4B8ADE59BBDE4BABA USING utf8mb4), CONVERT(0xE68891E4B88AE5ADA6E4BA86 USING utf8mb4), CONVERT(0xE68891E698AFE4B8ADE59BBDE4BABA USING utf8mb4)),
  (10014, 2580, CONVERT(0x2FE8AFADE69687EFBC88E7BB9FE7BC96E78988EFBC892FE68891E4B88AE5ADA6E4BA862FE68891E698AFE4B8ADE59BBDE4BABA USING utf8mb4), CONVERT(0xE68891E4B88AE5ADA6E4BA86 USING utf8mb4), CONVERT(0xE68891E698AFE4B8ADE59BBDE4BABA USING utf8mb4)),
  (10015, 2584, CONVERT(0x2FE8AFADE69687EFBC88E7BB9FE7BC96E78988EFBC892FE7ACACE4B880E58D95E58583EFBC9AE8AF86E5AD972F3120E5A4A9E59CB0E4BABA USING utf8mb4), CONVERT(0xE7ACACE4B880E58D95E58583EFBC9AE8AF86E5AD97 USING utf8mb4), CONVERT(0x3120E5A4A9E59CB0E4BABA USING utf8mb4)),
  (20001, 2580, CONVERT(0x2FE8AFADE69687EFBC88E7BB9FE7BC96E78988EFBC892FE68891E4B88AE5ADA6E4BA862FE68891E698AFE4B8ADE59BBDE4BABA USING utf8mb4), CONVERT(0xE68891E4B88AE5ADA6E4BA86 USING utf8mb4), CONVERT(0xE68891E698AFE4B8ADE59BBDE4BABA USING utf8mb4)),
  (20002, 2580, CONVERT(0x2FE8AFADE69687EFBC88E7BB9FE7BC96E78988EFBC892FE68891E4B88AE5ADA6E4BA862FE68891E698AFE4B8ADE59BBDE4BABA USING utf8mb4), CONVERT(0xE68891E4B88AE5ADA6E4BA86 USING utf8mb4), CONVERT(0xE68891E698AFE4B8ADE59BBDE4BABA USING utf8mb4)),
  (20003, 2580, CONVERT(0x2FE8AFADE69687EFBC88E7BB9FE7BC96E78988EFBC892FE68891E4B88AE5ADA6E4BA862FE68891E698AFE4B8ADE59BBDE4BABA USING utf8mb4), CONVERT(0xE68891E4B88AE5ADA6E4BA86 USING utf8mb4), CONVERT(0xE68891E698AFE4B8ADE59BBDE4BABA USING utf8mb4)),
  (20004, 2580, CONVERT(0x2FE8AFADE69687EFBC88E7BB9FE7BC96E78988EFBC892FE68891E4B88AE5ADA6E4BA862FE68891E698AFE4B8ADE59BBDE4BABA USING utf8mb4), CONVERT(0xE68891E4B88AE5ADA6E4BA86 USING utf8mb4), CONVERT(0xE68891E698AFE4B8ADE59BBDE4BABA USING utf8mb4));

INSERT INTO `phase3c_quality_cleanup_audit` (
  `batch_no`, `action_type`, `source_type`, `source_id`, `global_id`,
  `old_catalog_node_id`, `new_catalog_node_id`, `old_catalog_path`, `new_catalog_path`,
  `old_is_deleted`, `new_is_deleted`, `note`
)
SELECT
  @batch_no,
  'repair_orphan_catalog',
  'primary_chinese',
  r.`id`,
  rm.`id`,
  r.`catalog_node_id`,
  f.`catalog_node_id`,
  r.`catalog_path`,
  f.`catalog_path`,
  r.`is_deleted`,
  r.`is_deleted`,
  'Deterministic mapping from invalid legacy catalog_node_id to current textbook node.'
FROM `oss_primary_chinese_resource` r
JOIN `tmp_phase3c_orphan_fix` f ON f.`source_id` = r.`id`
LEFT JOIN `resource_main` rm ON rm.`source_type` = 'primary_chinese' AND rm.`source_id` = r.`id`
WHERE NOT EXISTS (
  SELECT 1 FROM `phase3c_quality_cleanup_audit` a
  WHERE a.`batch_no` = @batch_no
    AND a.`action_type` = 'repair_orphan_catalog'
    AND a.`source_type` = 'primary_chinese'
    AND a.`source_id` = r.`id`
);

UPDATE `oss_primary_chinese_resource` r
JOIN `tmp_phase3c_orphan_fix` f ON f.`source_id` = r.`id`
SET
  r.`catalog_node_id` = f.`catalog_node_id`,
  r.`catalog_path` = f.`catalog_path`,
  r.`unit_name` = COALESCE(f.`unit_name`, r.`unit_name`),
  r.`lesson_name` = COALESCE(f.`lesson_name`, r.`lesson_name`),
  r.`update_time` = NOW()
WHERE r.`is_deleted` = 0;

INSERT INTO `edu_resource_placement` (`resource_id`, `catalog_node_id`, `is_primary`, `sort`)
SELECT r.`id`, r.`catalog_node_id`, 1, COALESCE(r.`sort`, 0)
FROM `oss_primary_chinese_resource` r
JOIN `tmp_phase3c_orphan_fix` f ON f.`source_id` = r.`id`
WHERE r.`is_deleted` = 0
  AND r.`catalog_node_id` IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `edu_resource_placement` p
    WHERE p.`resource_id` = r.`id`
      AND p.`catalog_node_id` = r.`catalog_node_id`
  );

INSERT INTO `phase3c_quality_cleanup_audit` (
  `batch_no`, `action_type`, `source_type`, `source_id`, `global_id`,
  `old_catalog_node_id`, `new_catalog_node_id`, `old_catalog_path`, `new_catalog_path`,
  `old_is_deleted`, `new_is_deleted`, `note`
)
SELECT
  @batch_no,
  'move_unplaced_to_governance',
  'primary_chinese',
  r.`id`,
  rm.`id`,
  r.`catalog_node_id`,
  @governance_node_id,
  r.`catalog_path`,
  @governance_node_path,
  r.`is_deleted`,
  r.`is_deleted`,
  CONCAT('Original module/type: ', COALESCE(r.`module`, ''), '/', COALESCE(r.`type`, ''))
FROM `oss_primary_chinese_resource` r
LEFT JOIN `resource_main` rm ON rm.`source_type` = 'primary_chinese' AND rm.`source_id` = r.`id`
WHERE r.`is_deleted` = 0
  AND (r.`catalog_node_id` IS NULL OR r.`catalog_node_id` = 0)
  AND NOT EXISTS (
    SELECT 1 FROM `phase3c_quality_cleanup_audit` a
    WHERE a.`batch_no` = @batch_no
      AND a.`action_type` = 'move_unplaced_to_governance'
      AND a.`source_type` = 'primary_chinese'
      AND a.`source_id` = r.`id`
  );

UPDATE `oss_primary_chinese_resource`
SET
  `catalog_node_id` = @governance_node_id,
  `catalog_path` = @governance_node_path,
  `update_time` = NOW()
WHERE `is_deleted` = 0
  AND (`catalog_node_id` IS NULL OR `catalog_node_id` = 0);

INSERT INTO `edu_resource_placement` (`resource_id`, `catalog_node_id`, `is_primary`, `sort`)
SELECT r.`id`, r.`catalog_node_id`, 1, COALESCE(r.`sort`, 0)
FROM `oss_primary_chinese_resource` r
WHERE r.`is_deleted` = 0
  AND r.`catalog_node_id` = @governance_node_id
  AND NOT EXISTS (
    SELECT 1 FROM `edu_resource_placement` p
    WHERE p.`resource_id` = r.`id`
      AND p.`catalog_node_id` = r.`catalog_node_id`
  );

CREATE TEMPORARY TABLE `tmp_phase3c_empty_edu` (
  `source_id` BIGINT UNSIGNED NOT NULL PRIMARY KEY
) ENGINE=Memory;

INSERT INTO `tmp_phase3c_empty_edu`
VALUES (20111), (20112), (20113), (20114), (20115), (20118), (20119);

INSERT INTO `phase3c_quality_cleanup_audit` (
  `batch_no`, `action_type`, `source_type`, `source_id`, `global_id`,
  `old_catalog_node_id`, `new_catalog_node_id`, `old_catalog_path`, `new_catalog_path`,
  `old_is_deleted`, `new_is_deleted`, `note`
)
SELECT
  @batch_no,
  'soft_delete_empty_file',
  'edu_resource',
  e.`id`,
  rm.`id`,
  NULL,
  NULL,
  NULL,
  NULL,
  e.`is_deleted`,
  1,
  'Empty draft/test resource has no file metadata or URL.'
FROM `edu_resource` e
LEFT JOIN `resource_main` rm ON rm.`source_type` = 'edu_resource' AND rm.`source_id` = e.`id`
JOIN `tmp_phase3c_empty_edu` t ON t.`source_id` = e.`id`
WHERE (e.`oss_url` IS NULL OR e.`oss_url` = '')
  AND (e.`oss_object_key` IS NULL OR e.`oss_object_key` = '')
  AND (e.`original_filename` IS NULL OR e.`original_filename` = '')
  AND NOT EXISTS (
    SELECT 1 FROM `phase3c_quality_cleanup_audit` a
    WHERE a.`batch_no` = @batch_no
      AND a.`action_type` = 'soft_delete_empty_file'
      AND a.`source_type` = 'edu_resource'
      AND a.`source_id` = e.`id`
  );

UPDATE `edu_resource` e
JOIN `tmp_phase3c_empty_edu` t ON t.`source_id` = e.`id`
SET e.`is_deleted` = 1,
    e.`update_time` = NOW(),
    e.`remark` = CONCAT(COALESCE(e.`remark`, ''), IF(COALESCE(e.`remark`, '') = '', '', '; '), 'Phase3C: soft-deleted empty test/draft resource')
WHERE (e.`oss_url` IS NULL OR e.`oss_url` = '')
  AND (e.`oss_object_key` IS NULL OR e.`oss_object_key` = '')
  AND (e.`original_filename` IS NULL OR e.`original_filename` = '');

UPDATE `resource_main` rm
JOIN `tmp_phase3c_empty_edu` t ON t.`source_id` = rm.`source_id`
SET rm.`is_deleted` = 1,
    rm.`update_time` = NOW()
WHERE rm.`source_type` = 'edu_resource';

CREATE OR REPLACE VIEW `v_resource_migration_quality` AS
SELECT 'unplaced_resources' AS metric_key, COUNT(*) AS metric_value
FROM `v_admin_resource_main`
WHERE source_type IN ('primary_chinese', 'edu_resource')
  AND is_deleted = 0
  AND (catalog_node_id IS NULL OR catalog_node_id = 0)

UNION ALL

SELECT 'empty_file_resources', COUNT(*)
FROM `v_admin_resource_main`
WHERE source_type IN ('primary_chinese', 'edu_resource')
  AND is_deleted = 0
  AND (oss_url IS NULL OR oss_url = '')

UNION ALL

SELECT 'approved_not_published', COUNT(*)
FROM `v_admin_resource_main`
WHERE is_deleted = 0
  AND audit_status = 1
  AND publish_status IN (0, 2, 3)

UNION ALL

SELECT 'published_but_invisible', COUNT(*)
FROM `v_admin_resource_main`
WHERE is_deleted = 0
  AND publish_status = 1
  AND audit_status <> 1

UNION ALL

SELECT 'catalog_nodes_without_resources', COUNT(*)
FROM `edu_catalog_node` n
WHERE n.status = 1
  AND NOT EXISTS (
    SELECT 1
    FROM `oss_primary_chinese_resource` r
    WHERE r.is_deleted = 0
      AND r.audit_status = 1
      AND r.publish_status = 1
      AND r.catalog_node_id = n.id
  )

UNION ALL

SELECT 'orphan_resources_without_catalog', COUNT(*)
FROM `oss_primary_chinese_resource` r
WHERE r.is_deleted = 0
  AND r.catalog_node_id IS NOT NULL
  AND r.catalog_node_id > 0
  AND NOT EXISTS (
    SELECT 1
    FROM `edu_catalog_node` n
    WHERE n.id = r.catalog_node_id
  );

COMMIT;

SELECT `action_type`, COUNT(*) AS affected_rows
FROM `phase3c_quality_cleanup_audit`
WHERE `batch_no` = @batch_no
GROUP BY `action_type`
ORDER BY `action_type`;

SELECT `metric_key`, `metric_value`
FROM `v_resource_migration_quality`
ORDER BY `metric_key`;
