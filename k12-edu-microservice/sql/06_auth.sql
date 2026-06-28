-- ============================================================
-- 新课堂教育 — 06 认证与用户表
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt)',
    `nickname` VARCHAR(50) DEFAULT '' COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT '' COMMENT '头像URL',
    `email` VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '0保密 1男 2女',
    `birthday` DATE DEFAULT NULL COMMENT '生日',
    `phone` VARCHAR(20) DEFAULT '' COMMENT '手机号',
    `oauth_type` VARCHAR(20) DEFAULT NULL COMMENT 'wechat/qq/wework',
    `oauth_id` VARCHAR(100) DEFAULT NULL COMMENT '第三方 openid',
    `role` VARCHAR(20) DEFAULT 'user' COMMENT 'admin/teacher/user',
    `member_level` TINYINT DEFAULT 0 COMMENT '0免费 1基础 2高级',
    `member_expire_time` DATETIME DEFAULT NULL,
    `status` TINYINT DEFAULT 1 COMMENT '0禁用 1正常',
    `deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_oauth` (`oauth_type`, `oauth_id`),
    KEY `idx_phone` (`phone`),
    KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `sms_code` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `phone` VARCHAR(20) NOT NULL,
    `code` VARCHAR(10) NOT NULL,
    `type` VARCHAR(20) DEFAULT 'login' COMMENT 'login/register/reset',
    `ip` VARCHAR(50) DEFAULT '',
    `status` TINYINT DEFAULT 0 COMMENT '0未使用 1已使用 2已过期',
    `expire_time` DATETIME NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_phone` (`phone`),
    KEY `idx_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信验证码';

-- 管理员 admin / admin123（注意：旧库若已执行本脚本，密码实际为 123456，请执行 sql/50_auth_admin_user_patch.sql）
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `role`, `member_level`, `oauth_type`, `oauth_id`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '管理员', 'admin', 2, NULL, NULL);
