-- ============================================================
-- resource 表补充媒体元数据字段（与 Resource 实体对齐）
-- 执行：mysql -u root -p xinketang < sql/16_resource_media_columns.sql
-- ============================================================
USE `xinketang`;

ALTER TABLE `resource`
  ADD COLUMN `thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL' AFTER `storage_type`,
  ADD COLUMN `duration` INT DEFAULT NULL COMMENT '音视频时长(秒)' AFTER `thumbnail_url`,
  ADD COLUMN `page_count` INT DEFAULT NULL COMMENT '文档页数' AFTER `duration`;
