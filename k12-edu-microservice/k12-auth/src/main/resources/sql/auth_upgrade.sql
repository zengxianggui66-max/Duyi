-- K12 教育资源平台 - 登录注册功能增量脚本
USE xinketang;

-- 1. user 表添加 OAuth 相关字段
ALTER TABLE `user` ADD COLUMN `oauth_type` VARCHAR(20) NULL DEFAULT NULL COMMENT '第三方登录类型: wechat/qq/wework' AFTER `phone`;
ALTER TABLE `user` ADD COLUMN `oauth_id` VARCHAR(100) NULL DEFAULT NULL COMMENT '第三方登录唯一标识(openid/unionid)' AFTER `oauth_type`;
ALTER TABLE `user` ADD INDEX `idx_phone` (`phone`);
ALTER TABLE `user` ADD UNIQUE INDEX `uk_oauth` (`oauth_type`, `oauth_id`);

-- 2. 短信验证码表
CREATE TABLE IF NOT EXISTS `sms_code` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `phone` VARCHAR(20) NOT NULL COMMENT '手机号',
    `code` VARCHAR(10) NOT NULL COMMENT '验证码',
    `type` VARCHAR(20) DEFAULT 'login' COMMENT '用途: login/register/reset',
    `ip` VARCHAR(50) DEFAULT '' COMMENT '请求IP',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0未使用 1已使用 2已过期',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_phone` (`phone`),
    INDEX `idx_expire` (`expire_time`)
) ENGINE=InnoDB COMMENT='短信验证码表';
