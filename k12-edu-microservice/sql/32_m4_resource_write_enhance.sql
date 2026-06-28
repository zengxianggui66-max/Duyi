-- ============================================================
-- M4：主表写入增强（file_role 说明 + 浏览视图）
-- 执行：mysql -u root -p xinketang < sql/32_m4_resource_write_enhance.sql
-- 依赖：05_resource.sql、27~31
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

-- file_role 支持 main/answer/seewo/video 等（与方案 A 一致）
ALTER TABLE `edu_resource_file`
  MODIFY COLUMN `file_role` VARCHAR(20) NOT NULL DEFAULT 'main'
  COMMENT 'main|answer|analysis|attachment|audio|video|seewo';

DROP VIEW IF EXISTS `v_resource_browse`;

CREATE VIEW `v_resource_browse` AS
SELECT
    r.`id`,
    r.`title`,
    r.`description`,
    r.`remark`,
    r.`original_filename`,
    r.`file_ext`,
    r.`oss_bucket`,
    r.`oss_object_key`,
    r.`oss_url`,
    r.`file_size_kb`,
    r.`status` AS edu_status,
    r.`download_count`,
    r.`view_count`,
    r.`uploader_id`,
    r.`upload_time`,
    r.`update_time`,
    r.`sort`,
    s.`name`  AS stage_name,
    sub.`name` AS subject_name,
    e.`name`  AS edition_name,
    g.`name`  AS grade_name,
    m.`name`  AS module_name,
    rt.`name` AS type_name,
    u.`name`  AS unit_name,
    p.`catalog_node_id`,
    n.`name_path` AS catalog_path,
    b.`code` AS brand_code,
    w.`sub_type`,
    w.`lesson_name`,
    w.`allow_preview`
FROM `edu_resource` r
LEFT JOIN `edu_resource_dimension` rd ON r.`id` = rd.`resource_id`
LEFT JOIN `edu_stage` s ON rd.`stage_id` = s.`id`
LEFT JOIN `edu_subject` sub ON rd.`subject_id` = sub.`id`
LEFT JOIN `edu_edition` e ON rd.`edition_id` = e.`id`
LEFT JOIN `edu_grade` g ON rd.`grade_id` = g.`id`
LEFT JOIN `edu_module` m ON rd.`module_id` = m.`id`
LEFT JOIN `edu_resource_type` rt ON rd.`resource_type_id` = rt.`id`
LEFT JOIN `edu_unit` u ON rd.`unit_id` = u.`id`
LEFT JOIN `edu_resource_placement` p ON p.`resource_id` = r.`id` AND p.`is_primary` = 1
LEFT JOIN `edu_catalog_node` n ON n.`id` = p.`catalog_node_id`
LEFT JOIN `oss_primary_chinese_resource` w ON w.`id` = r.`id` AND w.`is_deleted` = 0
LEFT JOIN `edu_resource_brand` b ON b.`code` = w.`brand_code`
WHERE r.`is_deleted` = 0;
