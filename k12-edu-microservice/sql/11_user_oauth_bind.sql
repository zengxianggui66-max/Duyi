-- 用户第三方账号绑定表（支持同一用户绑定微信/QQ/企微）
USE `xinketang`;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `user_oauth_bind` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `oauth_type` VARCHAR(20) NOT NULL COMMENT 'wechat/qq/wework',
    `oauth_id` VARCHAR(100) NOT NULL COMMENT '第三方唯一标识',
    `nickname` VARCHAR(50) DEFAULT '' COMMENT '第三方昵称',
    `avatar` VARCHAR(500) DEFAULT '' COMMENT '第三方头像',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_oauth` (`oauth_type`, `oauth_id`),
    UNIQUE KEY `uk_user_type` (`user_id`, `oauth_type`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户第三方账号绑定';

-- 将 user 表已有 oauth 数据迁移到绑定表
INSERT INTO `user_oauth_bind` (`user_id`, `oauth_type`, `oauth_id`, `nickname`, `avatar`)
SELECT `id`, `oauth_type`, `oauth_id`, IFNULL(`nickname`, ''), IFNULL(`avatar`, '')
FROM `user`
WHERE `oauth_type` IS NOT NULL AND `oauth_type` != ''
  AND `oauth_id` IS NOT NULL AND `oauth_id` != ''
  AND `deleted` = 0
ON DUPLICATE KEY UPDATE `nickname` = VALUES(`nickname`);
