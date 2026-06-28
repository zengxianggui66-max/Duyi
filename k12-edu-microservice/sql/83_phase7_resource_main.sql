-- ============================================================
-- 83：Phase 7 — 统一资源审核域落地
-- 创建 resource_main 映射表 + 扩展统一视图 v_admin_resource_main
-- 依赖：02~04、08 (oss_primary_chinese_resource) 已执行
-- 执行：mysql -u root -p xinketang < sql/83_phase7_resource_main.sql
-- MySQL 账号：root  密码：zxg123456
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------
-- 1. resource_main 统一资源映射表（轻量，仅做 ID 映射 + 源追踪）
--    - 不做审计，审计数据仍在各源表（oss_primary_chinese_resource 等）
--    - 不做文件存储，文件仍在各源表
--    - 职责：提供全局唯一 resource_id + 源表路由
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `resource_main`;

CREATE TABLE `resource_main` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '全局统一资源ID',
  `source_type`     VARCHAR(40)  NOT NULL COMMENT '源表标识：primary_chinese | junior_math | junior_english | exam_paper 等',
  `source_table`    VARCHAR(80)  NOT NULL COMMENT '源表名：oss_primary_chinese_resource',
  `source_id`       BIGINT UNSIGNED NOT NULL COMMENT '源表主键',
  `title`           VARCHAR(200) DEFAULT NULL COMMENT '冗余标题（快速列表）',
  `stage_code`      VARCHAR(20)  DEFAULT NULL COMMENT '冗余学段编码',
  `subject_code`    VARCHAR(50)  DEFAULT NULL COMMENT '冗余学科编码',
  `audit_status`    TINYINT      DEFAULT NULL COMMENT '冗余：-1草稿 0待审 1通过 2驳回 3复审',
  `publish_status`  TINYINT      DEFAULT NULL COMMENT '冗余：0未上架 1已上架 2已下架 3定时 4回收站',
  `legacy_status`   TINYINT      DEFAULT NULL COMMENT '冗余：兼容旧 status',
  `uploader_id`     BIGINT       DEFAULT NULL COMMENT '冗余上传者',
  `upload_time`     DATETIME     DEFAULT NULL COMMENT '冗余上传时间',
  `is_deleted`      TINYINT      DEFAULT 0  COMMENT '逻辑删除',
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_source` (`source_type`, `source_id`),
  KEY `idx_audit_status` (`source_type`, `audit_status`, `is_deleted`),
  KEY `idx_publish_status` (`source_type`, `publish_status`, `is_deleted`),
  KEY `idx_uploader` (`uploader_id`, `is_deleted`),
  KEY `idx_stage_subject` (`stage_code`, `subject_code`),
  KEY `idx_upload_time` (`upload_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一资源主域映射表';

-- ----------------------------------------------------------
-- 2. 从 oss_primary_chinese_resource 回填现有数据
-- ----------------------------------------------------------
INSERT INTO `resource_main`
  (`source_type`, `source_table`, `source_id`, `title`, `stage_code`, `subject_code`,
   `audit_status`, `publish_status`, `legacy_status`, `uploader_id`, `upload_time`, `is_deleted`)
SELECT
  'primary_chinese',
  'oss_primary_chinese_resource',
  r.id,
  r.title,
  r.stage,
  r.subject,
  r.audit_status,
  r.publish_status,
  r.status,
  r.uploader_id,
  r.upload_time,
  r.is_deleted
FROM `oss_primary_chinese_resource` r
ON DUPLICATE KEY UPDATE
  `title`          = VALUES(`title`),
  `stage_code`     = VALUES(`stage_code`),
  `subject_code`   = VALUES(`subject_code`),
  `audit_status`   = VALUES(`audit_status`),
  `publish_status` = VALUES(`publish_status`),
  `legacy_status`  = VALUES(`legacy_status`),
  `uploader_id`    = VALUES(`uploader_id`),
  `is_deleted`     = VALUES(`is_deleted`);

-- ----------------------------------------------------------
-- 3. 重定义统一资源视图 v_admin_resource_main（全字段规范）
--    - 列顺序稳定，方便 Java Entity 映射
--    - source_type 用于区分来源
--    - 所有状态字段同时暴露 legacy/audit/publish
-- ----------------------------------------------------------
CREATE OR REPLACE VIEW `v_admin_resource_main` AS
SELECT
  rm.id              AS global_id,
  rm.source_type,
  rm.source_table,
  rm.source_id,
  rm.title,
  -- 分类维度
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
  -- 目录挂载
  r.catalog_node_id,
  r.catalog_path,
  -- 文件信息
  r.original_filename,
  r.file_ext,
  r.oss_bucket,
  r.oss_object_key,
  r.oss_url,
  r.file_size_kb,
  -- 状态三件套（legacy + audit + publish）
  r.status            AS legacy_status,
  r.audit_status,
  r.publish_status,
  -- 上传
  r.uploader_id,
  r.upload_time,
  r.update_time,
  r.is_deleted,
  -- 运营
  r.sort,
  r.remark,
  r.is_recommend,
  r.is_top,
  r.top_sort,
  r.is_free,
  r.download_count,
  r.view_count,
  -- 安全/预览
  r.allow_preview,
  r.preview_status,
  r.file_safety_status,
  r.lesson_plan_json
FROM `resource_main` rm
INNER JOIN `oss_primary_chinese_resource` r
  ON r.id = rm.source_id
 AND rm.source_type = 'primary_chinese'
 AND rm.is_deleted = 0;

-- ----------------------------------------------------------
-- 4. 状态同步触发器：oss_primary_chinese_resource 变更时自动更新 resource_main
-- ----------------------------------------------------------
DROP TRIGGER IF EXISTS `trg_sync_resource_main_ins`;
DROP TRIGGER IF EXISTS `trg_sync_resource_main_upd`;

DELIMITER //

CREATE TRIGGER `trg_sync_resource_main_ins`
AFTER INSERT ON `oss_primary_chinese_resource` FOR EACH ROW
BEGIN
  INSERT INTO `resource_main`
    (`source_type`, `source_table`, `source_id`, `title`,
     `stage_code`, `subject_code`, `audit_status`, `publish_status`,
     `legacy_status`, `uploader_id`, `upload_time`, `is_deleted`)
  VALUES
    ('primary_chinese', 'oss_primary_chinese_resource', NEW.id, NEW.title,
     NEW.stage, NEW.subject, NEW.audit_status, NEW.publish_status,
     NEW.status, NEW.uploader_id, NEW.upload_time, NEW.is_deleted)
  ON DUPLICATE KEY UPDATE
    `title`          = NEW.title,
    `stage_code`     = NEW.stage,
    `subject_code`   = NEW.subject,
    `audit_status`   = NEW.audit_status,
    `publish_status` = NEW.publish_status,
    `legacy_status`  = NEW.status,
    `uploader_id`    = NEW.uploader_id,
    `is_deleted`     = NEW.is_deleted;
END//

CREATE TRIGGER `trg_sync_resource_main_upd`
AFTER UPDATE ON `oss_primary_chinese_resource` FOR EACH ROW
BEGIN
  INSERT INTO `resource_main`
    (`source_type`, `source_table`, `source_id`, `title`,
     `stage_code`, `subject_code`, `audit_status`, `publish_status`,
     `legacy_status`, `uploader_id`, `upload_time`, `is_deleted`)
  VALUES
    ('primary_chinese', 'oss_primary_chinese_resource', NEW.id, NEW.title,
     NEW.stage, NEW.subject, NEW.audit_status, NEW.publish_status,
     NEW.status, NEW.uploader_id, NEW.upload_time, NEW.is_deleted)
  ON DUPLICATE KEY UPDATE
    `title`          = NEW.title,
    `stage_code`     = NEW.stage,
    `subject_code`   = NEW.subject,
    `audit_status`   = NEW.audit_status,
    `publish_status` = NEW.publish_status,
    `legacy_status`  = NEW.status,
    `uploader_id`    = NEW.uploader_id,
    `is_deleted`     = NEW.is_deleted;
END//

DELIMITER ;

-- ----------------------------------------------------------
-- 5. 未来学科扩展示例（接入初中数学时执行）
-- ----------------------------------------------------------
-- 示例：当 oss_junior_math_resource 表创建后
-- INSERT INTO resource_main (source_type, source_table, source_id, title, stage_code, subject_code,
--   audit_status, publish_status, legacy_status, uploader_id, upload_time, is_deleted)
-- SELECT 'junior_math', 'oss_junior_math_resource', id, title, stage, subject,
--   audit_status, publish_status, status, uploader_id, upload_time, is_deleted
-- FROM oss_junior_math_resource WHERE is_deleted = 0;
--
-- 然后 v_admin_resource_main 增加 UNION ALL 子句即可，
-- 管理端资源列表、审核中心无需任何改动！

-- ----------------------------------------------------------
-- 6. 验证
-- ----------------------------------------------------------
SELECT '>>> resource_main 总行数' AS info;
SELECT source_type, COUNT(*) AS cnt FROM `resource_main` GROUP BY source_type;

SELECT '>>> v_admin_resource_main 总行数' AS info;
SELECT source_type, COUNT(*) AS cnt FROM `v_admin_resource_main` GROUP BY source_type;

SELECT '>>> 资源主域统计' AS info;
SELECT
  source_type,
  COUNT(*) AS total,
  SUM(CASE WHEN audit_status = 0 THEN 1 ELSE 0 END) AS pending,
  SUM(CASE WHEN audit_status = 1 THEN 1 ELSE 0 END) AS approved,
  SUM(CASE WHEN publish_status = 1 THEN 1 ELSE 0 END) AS published
FROM `resource_main`
WHERE is_deleted = 0
GROUP BY source_type;

SET FOREIGN_KEY_CHECKS = 1;
-- ============================================================
-- 回滚：
-- DROP TRIGGER IF EXISTS `trg_sync_resource_main_ins`;
-- DROP TRIGGER IF EXISTS `trg_sync_resource_main_upd`;
-- DROP VIEW IF EXISTS `v_admin_resource_main`;
-- DROP TABLE IF EXISTS `resource_main`;
-- 然后重新执行 sql/82_phase2_resource_status_idempotent.sql 恢复旧视图
-- ============================================================
