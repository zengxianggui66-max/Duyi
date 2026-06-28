-- ============================================================
-- 82：管理端分类目录治理 — DDL + 数据迁移（一次性执行）
-- 依赖：02~04 已执行（edu_stage / edu_subject / edu_edition / edu_grade / edu_volume / edu_catalog_scheme）
-- 执行：mysql -u root -p xinketang < sql/82_phase6_category_governance.sql
-- 注意：本脚本仅需执行一次，重复执行会报 Duplicate column 错（表示已迁移成功）
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------
-- 1. edu_edition 版本表增强：学段+学科绑定
-- ----------------------------------------------------------
ALTER TABLE `edu_edition`
  ADD COLUMN `stage_id`   TINYINT UNSIGNED  DEFAULT NULL COMMENT '学段ID（NULL=通用）' AFTER `short_name`,
  ADD COLUMN `subject_id` SMALLINT UNSIGNED DEFAULT NULL COMMENT '学科ID（NULL=通用）' AFTER `stage_id`,
  ADD INDEX  `idx_edition_stage_subject` (`stage_id`, `subject_id`);

-- ----------------------------------------------------------
-- 2. edu_volume 册别表增强：状态 + 三维绑定
-- ----------------------------------------------------------
ALTER TABLE `edu_volume`
  ADD COLUMN `status`      TINYINT           DEFAULT 1   COMMENT '0停用 1启用' AFTER `sort`,
  ADD COLUMN `stage_id`    TINYINT UNSIGNED  DEFAULT NULL COMMENT '学段ID'       AFTER `status`,
  ADD COLUMN `subject_id`  SMALLINT UNSIGNED DEFAULT NULL COMMENT '学科ID'       AFTER `stage_id`,
  ADD COLUMN `edition_id`  SMALLINT UNSIGNED DEFAULT NULL COMMENT '版本ID'       AFTER `subject_id`,
  ADD INDEX  `idx_volume_bind` (`stage_id`, `subject_id`, `edition_id`);

-- ----------------------------------------------------------
-- 3. edu_catalog_scheme 目录方案：四维绑定
-- ----------------------------------------------------------
ALTER TABLE `edu_catalog_scheme`
  ADD COLUMN `stage_id`    TINYINT UNSIGNED  DEFAULT NULL COMMENT '学段ID' AFTER `brand_id`,
  ADD COLUMN `subject_id`  SMALLINT UNSIGNED DEFAULT NULL COMMENT '学科ID' AFTER `stage_id`,
  ADD COLUMN `edition_id`  SMALLINT UNSIGNED DEFAULT NULL COMMENT '版本ID' AFTER `subject_id`,
  ADD COLUMN `volume_id`   TINYINT UNSIGNED  DEFAULT NULL COMMENT '册别ID' AFTER `edition_id`,
  ADD INDEX  `idx_scheme_bind` (`stage_id`, `subject_id`, `edition_id`, `volume_id`);

-- ----------------------------------------------------------
-- 4. 资源引用统计视图（删除保护用）
-- ----------------------------------------------------------
DROP VIEW IF EXISTS `v_taxonomy_resource_ref`;

CREATE VIEW `v_taxonomy_resource_ref` AS
SELECT
  'stage'   AS dim_type, r.stage      AS dim_code, COUNT(*) AS ref_count
FROM `oss_primary_chinese_resource` r
WHERE r.is_deleted = 0 AND r.status >= 0
GROUP BY r.stage
UNION ALL
SELECT 'subject',  r.subject,    COUNT(*)
FROM `oss_primary_chinese_resource` r
WHERE r.is_deleted = 0 AND r.status >= 0
GROUP BY r.subject
UNION ALL
SELECT 'edition',  r.edition,    COUNT(*)
FROM `oss_primary_chinese_resource` r
WHERE r.is_deleted = 0 AND r.status >= 0
GROUP BY r.edition
UNION ALL
SELECT 'grade',    r.grade_name, COUNT(*)
FROM `oss_primary_chinese_resource` r
WHERE r.is_deleted = 0 AND r.status >= 0
GROUP BY r.grade_name;

-- ----------------------------------------------------------
-- 5. 数据回填：从 edu_subject_edition 关联表回填 edition 的学段学科
-- ----------------------------------------------------------
UPDATE `edu_edition` e
INNER JOIN (
  SELECT
    se.edition_id,
    MIN(s.stage_id)    AS stage_id,
    MIN(se.subject_id) AS subject_id
  FROM `edu_subject_edition` se
  INNER JOIN `edu_subject` s ON s.id = se.subject_id
  GROUP BY se.edition_id
) t ON t.edition_id = e.id
SET e.stage_id   = t.stage_id,
    e.subject_id = t.subject_id
WHERE e.stage_id IS NULL;

-- ----------------------------------------------------------
-- 6. 验证迁移结果
-- ----------------------------------------------------------
SELECT '>>> edu_edition 新增字段' AS info;
SELECT id, code, name, stage_id, subject_id FROM `edu_edition` LIMIT 5;

SELECT '>>> edu_volume 字段结构' AS info;
SHOW COLUMNS FROM `edu_volume` LIKE '%id';

SELECT '>>> edu_catalog_scheme 字段结构' AS info;
SHOW COLUMNS FROM `edu_catalog_scheme` LIKE '%id';

SELECT '>>> 资源引用视图 Top 10' AS info;
SELECT * FROM `v_taxonomy_resource_ref` ORDER BY ref_count DESC LIMIT 10;

SET FOREIGN_KEY_CHECKS = 1;
-- ============================================================
-- 回滚脚本（仅在需要时执行）：
-- ALTER TABLE `edu_edition`        DROP COLUMN `stage_id`, DROP COLUMN `subject_id`, DROP INDEX `idx_edition_stage_subject`;
-- ALTER TABLE `edu_volume`         DROP COLUMN `status`, DROP COLUMN `stage_id`, DROP COLUMN `subject_id`, DROP COLUMN `edition_id`, DROP INDEX `idx_volume_bind`;
-- ALTER TABLE `edu_catalog_scheme`  DROP COLUMN `stage_id`, DROP COLUMN `subject_id`, DROP COLUMN `edition_id`, DROP COLUMN `volume_id`, DROP INDEX `idx_scheme_bind`;
-- DROP VIEW IF EXISTS `v_taxonomy_resource_ref`;
-- ============================================================
