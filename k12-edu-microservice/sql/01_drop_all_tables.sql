-- ============================================================
-- 新课堂教育 — 01 清空 xinketang 库内全部业务表（重建前执行）
-- 警告：将删除所有数据，请先备份！
-- 执行：00 → 本文件 → 02~08 → 99
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP VIEW IF EXISTS `v_resource_full`;

-- 资源与关联
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

-- 目录
DROP TABLE IF EXISTS `edu_knowledge_point`;
DROP TABLE IF EXISTS `edu_lesson`;
DROP TABLE IF EXISTS `edu_unit`;

-- 分类维度
DROP TABLE IF EXISTS `edu_module_stage`;
DROP TABLE IF EXISTS `edu_resource_type`;
DROP TABLE IF EXISTS `edu_module`;
DROP TABLE IF EXISTS `edu_exam_scene`;
DROP TABLE IF EXISTS `edu_teaching_scene`;
DROP TABLE IF EXISTS `edu_region`;
DROP TABLE IF EXISTS `edu_file_format`;
DROP TABLE IF EXISTS `feature_category`;
DROP TABLE IF EXISTS `edu_channel`;
DROP TABLE IF EXISTS `edu_subject_edition`;
DROP TABLE IF EXISTS `edu_subject`;
DROP TABLE IF EXISTS `edu_grade`;
DROP TABLE IF EXISTS `edu_edition`;
DROP TABLE IF EXISTS `edu_semester`;
DROP TABLE IF EXISTS `edu_volume`;
DROP TABLE IF EXISTS `edu_stage`;

-- 互动 / 搜索
DROP TABLE IF EXISTS `resource_rating`;
DROP TABLE IF EXISTS `search_history`;
DROP TABLE IF EXISTS `search_hot_keyword`;
DROP TABLE IF EXISTS `share_record`;
DROP TABLE IF EXISTS `collection`;

-- 业务宽表 / 字典 / 通用资源
DROP TABLE IF EXISTS `oss_primary_chinese_resource`;
DROP TABLE IF EXISTS `oss_unit_doc`;
DROP TABLE IF EXISTS `resource_audit`;
DROP TABLE IF EXISTS `upload_record`;
DROP TABLE IF EXISTS `file_format_config`;
DROP TABLE IF EXISTS `dict`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `resource`;
DROP TABLE IF EXISTS `resource_category`;

-- 平台模块
DROP TABLE IF EXISTS `download_record`;
DROP TABLE IF EXISTS `user_collection`;
DROP TABLE IF EXISTS `member_order`;
DROP TABLE IF EXISTS `exam_paper`;
DROP TABLE IF EXISTS `lesson_plan`;
DROP TABLE IF EXISTS `article`;

-- 认证
DROP TABLE IF EXISTS `sms_code`;
DROP TABLE IF EXISTS `user`;

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'xinketang 全部旧表已删除' AS message;
