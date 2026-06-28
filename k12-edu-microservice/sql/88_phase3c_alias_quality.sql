-- ============================================================
-- 88: Phase 3C - 目录别名表 + 数据回填质量看板
-- 目标：
--   1) 用 resource_catalog_alias 解决旧资源标题/课文名与新目录节点不一致的问题
--   2) 用 v_resource_migration_quality 输出资源主链路迁移质量指标
-- 说明：
--   本脚本幂等，可重复执行；最后的 SELECT 只读输出验收指标。
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `resource_catalog_alias` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `source_type` VARCHAR(64) NOT NULL COMMENT '来源类型，如 primary_chinese',
  `legacy_title` VARCHAR(255) NOT NULL COMMENT '旧资源标题/课文名',
  `alias_title` VARCHAR(255) DEFAULT NULL COMMENT '别名，用于人工识别',
  `catalog_node_id` BIGINT NOT NULL COMMENT '映射目录节点ID',
  `confidence` TINYINT NOT NULL DEFAULT 100 COMMENT '置信度 0-100',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
  `notes` VARCHAR(1000) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_source_legacy_title` (`source_type`, `legacy_title`),
  KEY `idx_catalog_node_id` (`catalog_node_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源目录别名映射';

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

SELECT metric_key, metric_value
FROM `v_resource_migration_quality`
ORDER BY metric_key;
