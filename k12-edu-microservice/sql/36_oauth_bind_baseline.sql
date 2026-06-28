-- ============================================================
-- 36 OAuth 第三方登录 · 绑定表 baseline
-- 生成：manual
-- 执行：mysql -u root -pzxg123456 xinketang < sql/36_oauth_bind_baseline.sql
-- 依赖：06_auth.sql（user 表需有 oauth_type / oauth_id 列）
-- 说明：支持同一用户绑定微信/QQ/企业微信，并迁移 user 表历史 OAuth 数据
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

-- ==================== 1. user 表补列（若全新安装则 06_auth.sql 已含，此处幂等） ====================
-- 注意：06_auth.sql 已定义 oauth_type / oauth_id，本段仅做兼容补齐
ALTER TABLE `user`
  ADD COLUMN IF NOT EXISTS `oauth_type` VARCHAR(20) DEFAULT NULL COMMENT '主第三方登录类型: wechat/qq/wework'
  AFTER `phone`;
ALTER TABLE `user`
  ADD COLUMN IF NOT EXISTS `oauth_id` VARCHAR(100) DEFAULT NULL COMMENT '主第三方 openid'
  AFTER `oauth_type`;
ALTER TABLE `user`
  ADD INDEX IF NOT EXISTS `idx_oauth` (`oauth_type`, `oauth_id`);

-- ==================== 2. user_oauth_bind 绑定表 ====================
DROP TABLE IF EXISTS `user_oauth_bind`;
CREATE TABLE `user_oauth_bind` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '本站用户ID',
    `oauth_type` VARCHAR(20) NOT NULL COMMENT '平台类型: wechat / qq / wework',
    `oauth_id` VARCHAR(100) NOT NULL COMMENT '第三方用户唯一标识(openid)',
    `nickname` VARCHAR(50) DEFAULT '' COMMENT '第三方昵称',
    `avatar` VARCHAR(500) DEFAULT '' COMMENT '第三方头像URL',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_oauth` (`oauth_type`, `oauth_id`),
    UNIQUE KEY `uk_user_type` (`user_id`, `oauth_type`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户第三方账号绑定表';

-- ==================== 3. 迁移 user 表已有 OAuth 数据到绑定表 ====================
INSERT INTO `user_oauth_bind` (`user_id`, `oauth_type`, `oauth_id`, `nickname`, `avatar`)
SELECT `id`,
       `oauth_type`,
       `oauth_id`,
       IFNULL(`nickname`, ''),
       IFNULL(`avatar`, '')
FROM `user`
WHERE `oauth_type` IS NOT NULL
  AND `oauth_type` != ''
  AND `oauth_id` IS NOT NULL
  AND `oauth_id` != ''
  AND `deleted` = 0
ON DUPLICATE KEY UPDATE `nickname` = VALUES(`nickname`),
                        `avatar`   = VALUES(`avatar`);

-- ==================== 4. 验证 ====================
SELECT 'user_oauth_bind 表创建完成' AS status,
       COUNT(*) AS record_count
FROM `user_oauth_bind`;
