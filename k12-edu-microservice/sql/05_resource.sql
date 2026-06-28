-- ============================================================
-- 新课堂教育 — 05 资源主数据表（DDL）
-- 资源 / 多文件 / 维度关联 / M:N / 成套 / 标签 / 搜索扁平 / 视图
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `edu_resource_search_flat`;
DROP TABLE IF EXISTS `edu_resource_feature`;
DROP TABLE IF EXISTS `edu_resource_tag`;
DROP TABLE IF EXISTS `edu_resource_teaching_scene`;
DROP TABLE IF EXISTS `edu_resource_region`;
DROP TABLE IF EXISTS `edu_resource_module`;
DROP TABLE IF EXISTS `edu_resource_suite_item`;
DROP TABLE IF EXISTS `edu_resource_suite`;
DROP TABLE IF EXISTS `edu_resource_dimension`;
DROP TABLE IF EXISTS `edu_resource_file`;
DROP TABLE IF EXISTS `edu_resource`;
DROP VIEW IF EXISTS `v_resource_full`;

-- ----------------------------------------------------------
-- edu_resource 资源主表
-- ----------------------------------------------------------
CREATE TABLE `edu_resource` (
  `id`                  BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `title`               VARCHAR(200) NOT NULL,
  `subtitle`            VARCHAR(200) DEFAULT NULL,
  `description`         TEXT         DEFAULT NULL,
  `cover_url`           VARCHAR(1000) DEFAULT NULL,
  `original_filename`   VARCHAR(255) DEFAULT NULL COMMENT '兼容：主文件原名',
  `file_ext`            VARCHAR(20)  DEFAULT NULL COMMENT '兼容：主文件扩展名',
  `oss_bucket`          VARCHAR(100) DEFAULT NULL COMMENT '兼容：主文件 OSS',
  `oss_object_key`      VARCHAR(500) DEFAULT NULL,
  `oss_url`             VARCHAR(1000) DEFAULT NULL,
  `file_size_kb`        INT          DEFAULT 0,
  `status`              TINYINT      DEFAULT 0 COMMENT '0草稿 1待审 2已发布 3下架 4不通过',
  `is_suite`            TINYINT      DEFAULT 0 COMMENT '是否成套入口',
  `suite_id`            BIGINT UNSIGNED DEFAULT NULL,
  `is_free`             TINYINT      DEFAULT 1,
  `member_level_required` VARCHAR(20) DEFAULT 'free',
  `lesson_plan_json`    JSON         DEFAULT NULL COMMENT '结构化教案',
  `search_text`         TEXT         DEFAULT NULL,
  `download_count`      INT          DEFAULT 0,
  `view_count`          INT          DEFAULT 0,
  `collect_count`       INT          DEFAULT 0,
  `share_count`         INT          DEFAULT 0,
  `uploader_id`         BIGINT       DEFAULT NULL,
  `auditor_id`          BIGINT       DEFAULT NULL,
  `audit_time`          DATETIME     DEFAULT NULL,
  `audit_remark`        VARCHAR(500) DEFAULT NULL,
  `publish_time`        DATETIME     DEFAULT NULL,
  `upload_time`         DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`         DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted`          TINYINT      DEFAULT 0,
  `sort`                INT          DEFAULT 0,
  `remark`              VARCHAR(500) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`, `is_deleted`),
  KEY `idx_publish_time` (`publish_time`),
  KEY `idx_download_count` (`download_count`),
  KEY `idx_uploader_id` (`uploader_id`),
  FULLTEXT KEY `ft_search_text` (`search_text`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教育资源主表';

-- ----------------------------------------------------------
-- edu_resource_file 资源文件（一资源多文件）
-- ----------------------------------------------------------
CREATE TABLE `edu_resource_file` (
  `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `resource_id`       BIGINT UNSIGNED NOT NULL,
  `file_role`         VARCHAR(20)  NOT NULL DEFAULT 'main' COMMENT 'main|answer|analysis|attachment|audio|video',
  `original_filename` VARCHAR(255) DEFAULT NULL,
  `file_ext`          VARCHAR(20)  DEFAULT NULL,
  `mime_type`         VARCHAR(100) DEFAULT NULL,
  `file_size_bytes`   BIGINT       DEFAULT 0,
  `duration_sec`      INT          DEFAULT NULL,
  `page_count`        SMALLINT     DEFAULT NULL,
  `oss_bucket`        VARCHAR(100) DEFAULT NULL,
  `oss_object_key`    VARCHAR(500) DEFAULT NULL,
  `oss_url`           VARCHAR(1000) DEFAULT NULL,
  `allow_preview`     TINYINT      DEFAULT 1,
  `preview_url`       VARCHAR(1000) DEFAULT NULL,
  `sort`              SMALLINT     DEFAULT 0,
  `status`            TINYINT      DEFAULT 1,
  `create_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_file_role` (`resource_id`, `file_role`),
  CONSTRAINT `fk_rf_resource` FOREIGN KEY (`resource_id`) REFERENCES `edu_resource` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源文件表';

-- ----------------------------------------------------------
-- edu_resource_dimension 单值维度关联（MVP 宽表）
-- ----------------------------------------------------------
CREATE TABLE `edu_resource_dimension` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `resource_id`      BIGINT UNSIGNED NOT NULL,
  `stage_id`         TINYINT UNSIGNED   DEFAULT NULL,
  `subject_id`       SMALLINT UNSIGNED  DEFAULT NULL,
  `edition_id`       SMALLINT UNSIGNED  DEFAULT NULL,
  `grade_id`         TINYINT UNSIGNED   DEFAULT NULL,
  `semester_id`      TINYINT UNSIGNED   DEFAULT NULL,
  `volume_id`        TINYINT UNSIGNED   DEFAULT NULL,
  `module_id`        SMALLINT UNSIGNED  DEFAULT NULL,
  `resource_type_id` SMALLINT UNSIGNED  DEFAULT NULL,
  `unit_id`          INT UNSIGNED       DEFAULT NULL,
  `lesson_id`        INT UNSIGNED       DEFAULT NULL,
  `exam_scene_id`    TINYINT UNSIGNED   DEFAULT NULL,
  `knowledge_point_id` INT UNSIGNED     DEFAULT NULL,
  `region_id`        INT UNSIGNED       DEFAULT NULL,
  `year`             SMALLINT           DEFAULT NULL COMMENT '真题年份',
  PRIMARY KEY (`id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_filter` (`stage_id`, `subject_id`, `grade_id`, `module_id`, `resource_type_id`),
  KEY `idx_unit_id` (`unit_id`),
  CONSTRAINT `fk_rd_resource` FOREIGN KEY (`resource_id`) REFERENCES `edu_resource` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源维度关联（单值）';

-- ----------------------------------------------------------
-- edu_resource_module 资源-栏目（多选）
-- ----------------------------------------------------------
CREATE TABLE `edu_resource_module` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `resource_id` BIGINT UNSIGNED NOT NULL,
  `module_id`   SMALLINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_module` (`resource_id`, `module_id`),
  KEY `idx_module_id` (`module_id`),
  CONSTRAINT `fk_rm_resource` FOREIGN KEY (`resource_id`) REFERENCES `edu_resource` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rm_module` FOREIGN KEY (`module_id`) REFERENCES `edu_module` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源栏目多选';

-- ----------------------------------------------------------
-- edu_resource_region 资源-地区（多选）
-- ----------------------------------------------------------
CREATE TABLE `edu_resource_region` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `resource_id` BIGINT UNSIGNED NOT NULL,
  `region_id`   INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_region` (`resource_id`, `region_id`),
  CONSTRAINT `fk_rr_resource` FOREIGN KEY (`resource_id`) REFERENCES `edu_resource` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rr_region` FOREIGN KEY (`region_id`) REFERENCES `edu_region` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源地区多选';

-- ----------------------------------------------------------
-- edu_resource_teaching_scene 资源-教学场景（多选）
-- ----------------------------------------------------------
CREATE TABLE `edu_resource_teaching_scene` (
  `id`                 BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `resource_id`        BIGINT UNSIGNED NOT NULL,
  `teaching_scene_id`  TINYINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_scene` (`resource_id`, `teaching_scene_id`),
  CONSTRAINT `fk_rts_resource` FOREIGN KEY (`resource_id`) REFERENCES `edu_resource` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rts_scene` FOREIGN KEY (`teaching_scene_id`) REFERENCES `edu_teaching_scene` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源教学场景多选';

-- ----------------------------------------------------------
-- edu_resource_suite 成套资源
-- ----------------------------------------------------------
CREATE TABLE `edu_resource_suite` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `title`       VARCHAR(200) NOT NULL,
  `description` TEXT         DEFAULT NULL,
  `module_id`   SMALLINT UNSIGNED DEFAULT NULL,
  `cover_url`   VARCHAR(1000) DEFAULT NULL,
  `status`      TINYINT      DEFAULT 1,
  `sort`        INT          DEFAULT 0,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_module_id` (`module_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成套资源';

CREATE TABLE `edu_resource_suite_item` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `suite_id`    BIGINT UNSIGNED NOT NULL,
  `resource_id` BIGINT UNSIGNED NOT NULL,
  `item_label`  VARCHAR(100) DEFAULT NULL COMMENT '卷A/课时1',
  `sort`        SMALLINT     DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_suite_resource` (`suite_id`, `resource_id`),
  KEY `idx_resource_id` (`resource_id`),
  CONSTRAINT `fk_rsi_suite` FOREIGN KEY (`suite_id`) REFERENCES `edu_resource_suite` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rsi_resource` FOREIGN KEY (`resource_id`) REFERENCES `edu_resource` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成套资源明细';

-- ----------------------------------------------------------
-- edu_resource_tag 资源标签
-- ----------------------------------------------------------
CREATE TABLE `edu_resource_tag` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `resource_id` BIGINT UNSIGNED NOT NULL,
  `tag_name`    VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_tag_name` (`tag_name`),
  CONSTRAINT `fk_rt_resource` FOREIGN KEY (`resource_id`) REFERENCES `edu_resource` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源标签';

-- ----------------------------------------------------------
-- edu_resource_feature 特色频道关联
-- ----------------------------------------------------------
CREATE TABLE `edu_resource_feature` (
  `id`                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `resource_id`          BIGINT UNSIGNED NOT NULL,
  `channel_id`           TINYINT UNSIGNED NOT NULL,
  `feature_category_id`  INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resource_feature` (`resource_id`, `channel_id`, `feature_category_id`),
  CONSTRAINT `fk_rf2_resource` FOREIGN KEY (`resource_id`) REFERENCES `edu_resource` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_rf2_channel` FOREIGN KEY (`channel_id`) REFERENCES `edu_channel` (`id`),
  CONSTRAINT `fk_rf2_category` FOREIGN KEY (`feature_category_id`) REFERENCES `feature_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源特色频道关联';

-- ----------------------------------------------------------
-- edu_resource_search_flat 列表搜索扁平表（可选）
-- ----------------------------------------------------------
CREATE TABLE `edu_resource_search_flat` (
  `resource_id`     BIGINT UNSIGNED NOT NULL,
  `stage_code`      VARCHAR(20)  DEFAULT NULL,
  `subject_code`    VARCHAR(50)  DEFAULT NULL,
  `edition_name`    VARCHAR(50)  DEFAULT NULL,
  `grade_name`      VARCHAR(20)  DEFAULT NULL,
  `module_codes`    VARCHAR(500) DEFAULT NULL,
  `type_codes`      VARCHAR(500) DEFAULT NULL,
  `file_exts`       VARCHAR(200) DEFAULT NULL,
  `publish_time`    DATETIME     DEFAULT NULL,
  `download_count`  INT          DEFAULT 0,
  `view_count`      INT          DEFAULT 0,
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源搜索扁平表';

-- ----------------------------------------------------------
-- v_resource_full 资源完整信息视图
-- ----------------------------------------------------------
CREATE OR REPLACE VIEW `v_resource_full` AS
SELECT
    r.id,
    r.title,
    r.subtitle,
    r.description,
    r.cover_url,
    r.original_filename,
    r.file_ext,
    r.oss_bucket,
    r.oss_object_key,
    r.oss_url,
    r.file_size_kb,
    r.status,
    r.is_suite,
    r.suite_id,
    r.download_count,
    r.view_count,
    r.collect_count,
    r.uploader_id,
    r.publish_time,
    r.upload_time,
    r.update_time,
    d.stage_id,
    st.name   AS stage_name,
    st.code   AS stage_code,
    d.subject_id,
    sub.name  AS subject_name,
    sub.code  AS subject_code,
    d.edition_id,
    ed.name   AS edition_name,
    d.grade_id,
    g.name    AS grade_name,
    d.semester_id,
    sem.name  AS semester_name,
    d.volume_id,
    vol.name  AS volume_name,
    d.module_id,
    m.name    AS module_name,
    m.code    AS module_code,
    d.resource_type_id,
    rt.name   AS resource_type_name,
    rt.code   AS resource_type_code,
    d.unit_id,
    u.name    AS unit_name,
    d.lesson_id,
    les.name  AS lesson_name,
    d.exam_scene_id,
    ex.name   AS exam_scene_name,
    d.year
FROM edu_resource r
LEFT JOIN edu_resource_dimension d ON r.id = d.resource_id
LEFT JOIN edu_stage st ON d.stage_id = st.id
LEFT JOIN edu_subject sub ON d.subject_id = sub.id
LEFT JOIN edu_edition ed ON d.edition_id = ed.id
LEFT JOIN edu_grade g ON d.grade_id = g.id
LEFT JOIN edu_semester sem ON d.semester_id = sem.id
LEFT JOIN edu_volume vol ON d.volume_id = vol.id
LEFT JOIN edu_module m ON d.module_id = m.id
LEFT JOIN edu_resource_type rt ON d.resource_type_id = rt.id
LEFT JOIN edu_unit u ON d.unit_id = u.id
LEFT JOIN edu_lesson les ON d.lesson_id = les.id
LEFT JOIN edu_exam_scene ex ON d.exam_scene_id = ex.id
WHERE r.is_deleted = 0;

SET FOREIGN_KEY_CHECKS = 1;
