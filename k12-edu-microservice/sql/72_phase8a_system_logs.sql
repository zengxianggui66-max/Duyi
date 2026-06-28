-- Phase 8-A: system log permissions + login log index
USE `xinketang`;
SET NAMES utf8mb4;

-- ---------- 1. 登录日志索引（按 login_type + 时间筛选） ----------
SET @idx_exists = (
    SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'user_login_log'
      AND index_name = 'idx_login_type_time'
);
SET @ddl = IF(@idx_exists = 0,
    'ALTER TABLE `user_login_log` ADD INDEX `idx_login_type_time` (`login_type`, `create_time`)',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------- 2. 权限：查看系统日志 ----------
INSERT INTO `sys_permission` (`code`, `name`, `type`, `module`, `parent_id`, `sort`, `status`) VALUES
('admin:system:log_view', '查看系统日志', 'button', 'system', 7, 74, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `type` = VALUES(`type`),
  `module` = VALUES(`module`),
  `parent_id` = VALUES(`parent_id`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);

-- super_admin 全权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.id FROM `sys_permission` p WHERE p.code = 'admin:system:log_view';

-- system_admin
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 7, p.id FROM `sys_permission` p WHERE p.code = 'admin:system:log_view';

-- 原拥有 config_edit 的角色同步授予 log_view
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT rp.role_id, p2.id
FROM `sys_role_permission` rp
INNER JOIN `sys_permission` p ON p.id = rp.permission_id AND p.code = 'admin:system:config_edit'
INNER JOIN `sys_permission` p2 ON p2.code = 'admin:system:log_view';
