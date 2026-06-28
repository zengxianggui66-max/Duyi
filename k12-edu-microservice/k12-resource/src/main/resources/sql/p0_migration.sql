-- P0 迁移：收藏资源类型、小学语文资源扩展字段
-- 已整合至 sql/07_interaction.sql、sql/08_legacy_business.sql；新库无需执行

-- 1. 收藏表增加 resource_type，避免 primary_chinese 与 resource 表 ID 冲突
ALTER TABLE `collection`
    ADD COLUMN IF NOT EXISTS `resource_type` VARCHAR(32) NOT NULL DEFAULT 'resource'
        COMMENT 'resource=通用资源表 primary_chinese=小学语文资源表' AFTER `resource_id`;

-- MySQL 8.0.12 以下不支持 IF NOT EXISTS，可改为：
-- ALTER TABLE `collection` ADD COLUMN `resource_type` VARCHAR(32) NOT NULL DEFAULT 'resource' AFTER `resource_id`;

ALTER TABLE `collection` DROP INDEX IF EXISTS `uk_user_resource`;
ALTER TABLE `collection`
    ADD UNIQUE KEY `uk_user_resource_type` (`user_id`, `resource_id`, `resource_type`);

-- 2. 小学语文资源表扩展（表名以实际为准）
ALTER TABLE `xinketang`.`oss_primary_chinese_resource`
    ADD COLUMN IF NOT EXISTS `lesson_name` VARCHAR(128) DEFAULT NULL COMMENT '课文名称' AFTER `unit_name`;

ALTER TABLE `xinketang`.`oss_primary_chinese_resource`
    ADD COLUMN IF NOT EXISTS `allow_preview` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许预览 0否1是' AFTER `remark`;
