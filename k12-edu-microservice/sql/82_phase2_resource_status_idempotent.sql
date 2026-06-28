-- Phase 2 Step 2 — 资源审核/发布状态列（幂等补全）
-- 前置：sql/80 已在生产/本地执行过的环境，本脚本可重复执行（缺列才 ALTER，否则跳过）
-- 新库全量初始化仍应先跑 sql/80；本脚本供「80 未跑成功 / 列缺失」补救
-- 用法（Windows 请用 scripts/run-sql82-resource-status.cmd）:
--   mysql -u root -p xinketang < sql/82_phase2_resource_status_idempotent.sql

USE `xinketang`;
SET NAMES utf8mb4;

-- ---------- audit_status ----------
SET @need_audit := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oss_primary_chinese_resource'
    AND COLUMN_NAME = 'audit_status'
);

SET @ddl_audit := IF(
  @need_audit = 0,
  'ALTER TABLE `oss_primary_chinese_resource`
     ADD COLUMN `audit_status` TINYINT DEFAULT NULL
     COMMENT ''审核状态：-1草稿 0待审核 1通过 2驳回 3复审中''
     AFTER `status`',
  'SELECT ''skip: audit_status exists'' AS migration_note'
);
PREPARE stmt_audit FROM @ddl_audit;
EXECUTE stmt_audit;
DEALLOCATE PREPARE stmt_audit;

-- ---------- publish_status ----------
SET @need_publish := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oss_primary_chinese_resource'
    AND COLUMN_NAME = 'publish_status'
);

SET @ddl_publish := IF(
  @need_publish = 0,
  'ALTER TABLE `oss_primary_chinese_resource`
     ADD COLUMN `publish_status` TINYINT DEFAULT NULL
     COMMENT ''发布状态：0未上架 1已上架 2已下架 3定时上架 4回收站''
     AFTER `audit_status`',
  'SELECT ''skip: publish_status exists'' AS migration_note'
);
PREPARE stmt_publish FROM @ddl_publish;
EXECUTE stmt_publish;
DEALLOCATE PREPARE stmt_publish;

-- ---------- 回填（与 sql/80 一致，仅 NULL 行） ----------
UPDATE `oss_primary_chinese_resource`
SET `audit_status` = CASE
    WHEN `status` = -1 THEN -1
    WHEN `status` = 0 THEN 0
    WHEN `status` IN (1, 3, 4) THEN 1
    WHEN `status` = 2 THEN 2
    ELSE 0
  END,
  `publish_status` = CASE
    WHEN `status` = 1 THEN 1
    WHEN `status` = 3 THEN 2
    WHEN `status` = 4 THEN 4
    ELSE 0
  END
WHERE `audit_status` IS NULL OR `publish_status` IS NULL;

-- ---------- 管理端统一视图（可重复 CREATE OR REPLACE） ----------
CREATE OR REPLACE VIEW `v_admin_resource_main` AS
SELECT
  id,
  'primary_chinese' AS source_type,
  stage,
  subject,
  module,
  type,
  grade_name,
  edition,
  brand_code,
  catalog_node_id,
  catalog_path,
  unit_name,
  lesson_name,
  title,
  original_filename,
  file_ext,
  oss_bucket,
  oss_object_key,
  oss_url,
  file_size_kb,
  status AS legacy_status,
  audit_status,
  publish_status,
  uploader_id,
  upload_time,
  update_time,
  is_deleted,
  sort,
  remark,
  is_recommend,
  is_top,
  top_sort,
  is_free,
  download_count,
  view_count
FROM `oss_primary_chinese_resource`;

-- ---------- 验收 ----------
SELECT '=== oss_primary_chinese_resource status columns ===' AS section;
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'oss_primary_chinese_resource'
  AND COLUMN_NAME IN ('status', 'audit_status', 'publish_status')
ORDER BY ORDINAL_POSITION;

SELECT '=== v_admin_resource_main ===' AS section;
SELECT COUNT(*) AS view_exists
FROM information_schema.VIEWS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'v_admin_resource_main';
