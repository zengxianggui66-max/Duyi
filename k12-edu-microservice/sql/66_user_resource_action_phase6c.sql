-- Phase 6-C：用户行为流水（浏览 / 下载 / 搜索）
USE `xinketang`;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `user_resource_action` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`       BIGINT       NOT NULL COMMENT '用户 ID',
  `action_type`   VARCHAR(20)  NOT NULL COMMENT 'view/download/search',
  `resource_id`   BIGINT       DEFAULT NULL,
  `resource_type` VARCHAR(32)  DEFAULT NULL COMMENT 'primary_chinese/resource 等',
  `title`         VARCHAR(255) DEFAULT NULL COMMENT '资源标题或搜索摘要',
  `keyword`       VARCHAR(255) DEFAULT NULL COMMENT '搜索词（action_type=search）',
  `hit_count`     INT          DEFAULT NULL COMMENT '搜索命中数',
  `ip`            VARCHAR(64)  DEFAULT NULL,
  `user_agent`    VARCHAR(512) DEFAULT NULL,
  `source_api`    VARCHAR(128) DEFAULT NULL COMMENT '触发 API',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `create_time`),
  KEY `idx_user_action` (`user_id`, `action_type`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户资源行为流水';

-- 确保 view_behavior 权限存在（Phase 6-A 可能已插入）
INSERT INTO `sys_permission` (`code`, `name`, `type`, `module`, `parent_id`, `sort`, `status`) VALUES
('admin:user:view_behavior', '查看用户行为流水', 'button', 'user', 4, 44, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `status` = VALUES(`status`);

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.id FROM `sys_permission` p WHERE p.code = 'admin:user:view_behavior';

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 5, p.id FROM `sys_permission` p WHERE p.code = 'admin:user:view_behavior';

SELECT '=== user_resource_action ===' AS section;
SHOW CREATE TABLE user_resource_action\G
