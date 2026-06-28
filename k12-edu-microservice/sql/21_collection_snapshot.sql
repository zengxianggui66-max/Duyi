-- ============================================================
-- 收藏表扩展：资源快照字段（列表展示与按学段/学科/类型筛选）
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

ALTER TABLE `collection`
    ADD COLUMN `title` VARCHAR(255) DEFAULT NULL COMMENT '资源标题快照' AFTER `resource_type`,
    ADD COLUMN `stage` VARCHAR(32) DEFAULT NULL COMMENT '学段名称：小学/初中/高中/美术/舞蹈' AFTER `title`,
    ADD COLUMN `stage_key` VARCHAR(16) DEFAULT NULL COMMENT '学段键：primary/junior/senior/art/dance' AFTER `stage`,
    ADD COLUMN `subject` VARCHAR(64) DEFAULT NULL COMMENT '学科名称快照' AFTER `stage_key`,
    ADD COLUMN `subject_key` VARCHAR(32) DEFAULT NULL COMMENT '学科键：chinese/math/...' AFTER `subject`,
    ADD COLUMN `module` VARCHAR(64) DEFAULT NULL COMMENT '栏目：同步备课等' AFTER `subject_key`,
    ADD COLUMN `teaching_type` VARCHAR(64) DEFAULT NULL COMMENT '资源类型：课件/教案/试卷等' AFTER `module`,
    ADD COLUMN `grade_name` VARCHAR(64) DEFAULT NULL COMMENT '年级册别快照' AFTER `teaching_type`,
    ADD COLUMN `file_ext` VARCHAR(16) DEFAULT NULL COMMENT '文件扩展名' AFTER `grade_name`,
    ADD COLUMN `oss_url` VARCHAR(512) DEFAULT NULL COMMENT '文件访问地址快照' AFTER `file_ext`;

ALTER TABLE `collection`
    ADD KEY `idx_user_stage_key` (`user_id`, `stage_key`),
    ADD KEY `idx_user_teaching_type` (`user_id`, `teaching_type`);
