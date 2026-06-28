-- 修复手机号用户与 uk_oauth 冲突：空字符串改为 NULL，允许多个非 OAuth 用户共存
USE xinketang;

UPDATE `user`
SET `oauth_type` = NULL, `oauth_id` = NULL
WHERE (`oauth_type` IS NULL OR `oauth_type` = '')
  AND (`oauth_id` IS NULL OR `oauth_id` = '');

ALTER TABLE `user`
    MODIFY COLUMN `oauth_type` VARCHAR(20) NULL DEFAULT NULL COMMENT '第三方登录类型',
    MODIFY COLUMN `oauth_id` VARCHAR(100) NULL DEFAULT NULL COMMENT '第三方 openid';
