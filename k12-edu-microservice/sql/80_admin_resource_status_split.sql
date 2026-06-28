-- Phase 10-A: split resource review status and publish status.
-- Keep legacy status for compatibility; new admin APIs expose audit_status and publish_status.

ALTER TABLE `oss_primary_chinese_resource`
  ADD COLUMN `audit_status` TINYINT DEFAULT NULL COMMENT '审核状态：-1草稿 0待审核 1通过 2驳回 3复审中' AFTER `status`,
  ADD COLUMN `publish_status` TINYINT DEFAULT NULL COMMENT '发布状态：0未上架 1已上架 2已下架 3定时上架 4回收站' AFTER `audit_status`;

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
