-- P2 迁移：分享记录资源类型

ALTER TABLE `share_record`
    ADD COLUMN `resource_type` VARCHAR(32) NOT NULL DEFAULT 'resource'
        COMMENT 'resource|primary_chinese' AFTER `resource_id`;
