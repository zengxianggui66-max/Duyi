-- 用户下载/浏览记录（个人中心 P0-P2）
-- 执行前请备份；download_record 若已含下列字段可跳过 ALTER
USE `xinketang`;
SET NAMES utf8mb4;

ALTER TABLE `download_record`
    ADD COLUMN `resource_type` VARCHAR(32) DEFAULT 'primary_chinese' COMMENT '资源来源' AFTER `resource_id`,
    ADD COLUMN `file_ext` VARCHAR(20) DEFAULT '' AFTER `resource_title`,
    ADD COLUMN `file_size` BIGINT DEFAULT 0 AFTER `file_ext`,
    ADD COLUMN `subject` VARCHAR(50) DEFAULT '' AFTER `file_size`,
    ADD COLUMN `grade_name` VARCHAR(50) DEFAULT '' AFTER `subject`,
    ADD COLUMN `teaching_type` VARCHAR(30) DEFAULT '' AFTER `grade_name`,
    ADD COLUMN `stage_key` VARCHAR(20) DEFAULT '' AFTER `teaching_type`;

CREATE TABLE IF NOT EXISTS `view_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `resource_id` BIGINT NOT NULL,
    `resource_type` VARCHAR(32) DEFAULT 'primary_chinese',
    `title` VARCHAR(255) DEFAULT '',
    `subject` VARCHAR(50) DEFAULT '',
    `stage_key` VARCHAR(20) DEFAULT '',
    `stage` VARCHAR(20) DEFAULT '',
    `grade_name` VARCHAR(50) DEFAULT '',
    `teaching_type` VARCHAR(30) DEFAULT '',
    `file_ext` VARCHAR(20) DEFAULT '',
    `oss_url` VARCHAR(500) DEFAULT '',
    `detail_url` VARCHAR(1024) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_resource` (`user_id`, `resource_id`, `resource_type`),
    KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户浏览记录';
