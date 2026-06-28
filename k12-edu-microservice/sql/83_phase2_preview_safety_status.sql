-- Phase 2 Step 3 — 预览/文件安全状态列（幂等）
-- 前置：sql/80 / sql/82 已执行
-- 用法: scripts/run-sql83-preview-safety.cmd

USE `xinketang`;
SET NAMES utf8mb4;

-- ---------- preview_status ----------
SET @need_preview := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oss_primary_chinese_resource'
    AND COLUMN_NAME = 'preview_status'
);

SET @ddl_preview := IF(
  @need_preview = 0,
  'ALTER TABLE `oss_primary_chinese_resource`
     ADD COLUMN `preview_status` TINYINT DEFAULT NULL
     COMMENT ''预览状态：0不可预览 1可预览 2待检测 3失败''
     AFTER `allow_preview`',
  'SELECT ''skip: preview_status exists'' AS migration_note'
);
PREPARE stmt_preview FROM @ddl_preview;
EXECUTE stmt_preview;
DEALLOCATE PREPARE stmt_preview;

-- ---------- file_safety_status ----------
SET @need_safety := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'oss_primary_chinese_resource'
    AND COLUMN_NAME = 'file_safety_status'
);

SET @ddl_safety := IF(
  @need_safety = 0,
  'ALTER TABLE `oss_primary_chinese_resource`
     ADD COLUMN `file_safety_status` TINYINT DEFAULT NULL
     COMMENT ''文件安全：0未知 1待检测 2安全 3风险''
     AFTER `preview_status`',
  'SELECT ''skip: file_safety_status exists'' AS migration_note'
);
PREPARE stmt_safety FROM @ddl_safety;
EXECUTE stmt_safety;
DEALLOCATE PREPARE stmt_safety;

-- 已有资源：按 allow_preview / 扩展名粗回填
UPDATE `oss_primary_chinese_resource`
SET `preview_status` = CASE
    WHEN `oss_url` IS NULL OR `oss_url` = '' THEN 2
    WHEN `allow_preview` = 1 THEN 1
    WHEN `allow_preview` = 0 THEN 0
    ELSE 2
  END
WHERE `preview_status` IS NULL;

UPDATE `oss_primary_chinese_resource`
SET `file_safety_status` = CASE
    WHEN `oss_url` IS NULL OR `oss_url` = '' THEN 1
    WHEN LOWER(REPLACE(IFNULL(`file_ext`, ''), '.', '')) IN ('exe','bat','cmd','com','scr','vbs','js','jar','msi','dll') THEN 3
    WHEN LOWER(REPLACE(IFNULL(`file_ext`, ''), '.', '')) IN ('zip','rar','7z') THEN 1
    WHEN LOWER(REPLACE(IFNULL(`file_ext`, ''), '.', '')) IN ('pdf','doc','docx','ppt','pptx','xls','xlsx','txt','jpg','jpeg','png','gif','webp','mp4','mp3','wav','m4a','webm') THEN 2
    ELSE 0
  END
WHERE `file_safety_status` IS NULL;

SELECT '=== preview/safety columns ===' AS section;
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'oss_primary_chinese_resource'
  AND COLUMN_NAME IN ('preview_status', 'file_safety_status')
ORDER BY ORDINAL_POSITION;
