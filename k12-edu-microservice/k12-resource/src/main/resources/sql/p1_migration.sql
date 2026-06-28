-- P1 迁移：教案 JSON、草稿状态说明
-- status = -1 表示草稿（应用层 ResourceStatusConstants.DRAFT）

ALTER TABLE `xinketang`.`oss_primary_chinese_resource`
    ADD COLUMN `lesson_plan_json` TEXT DEFAULT NULL COMMENT '结构化教案JSON' AFTER `allow_preview`;
