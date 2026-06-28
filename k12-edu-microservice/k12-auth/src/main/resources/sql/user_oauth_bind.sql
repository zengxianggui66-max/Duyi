-- 用户第三方绑定表（执行 11_user_oauth_bind.sql 或本文件）
USE `xinketang`;

CREATE TABLE IF NOT EXISTS `user_oauth_bind` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `oauth_type` VARCHAR(20) NOT NULL,
    `oauth_id` VARCHAR(100) NOT NULL,
    `nickname` VARCHAR(50) DEFAULT '',
    `avatar` VARCHAR(500) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_oauth` (`oauth_type`, `oauth_id`),
    UNIQUE KEY `uk_user_type` (`user_id`, `oauth_type`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户第三方账号绑定';
