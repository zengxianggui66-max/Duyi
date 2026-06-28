-- Phase 6-D: user ops (remarks / export / batch / auditor scope)
USE `xinketang`;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `user_admin_remark` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`        BIGINT       NOT NULL,
  `admin_user_id`  BIGINT       NOT NULL,
  `admin_username` VARCHAR(64)  DEFAULT NULL,
  `content`        VARCHAR(500) NOT NULL,
  `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `sys_permission` (`code`, `name`, `type`, `module`, `parent_id`, `sort`, `status`) VALUES
('admin:user:remark', 'User remark', 'button', 'user', 4, 47, 1),
('admin:user:export', 'Export users', 'button', 'user', 4, 48, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `status` = VALUES(`status`);

UPDATE `sys_permission` SET `name` = '用户备注' WHERE `code` = 'admin:user:remark';
UPDATE `sys_permission` SET `name` = '导出用户' WHERE `code` = 'admin:user:export';

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.id FROM `sys_permission` p WHERE p.code IN ('admin:user:remark', 'admin:user:export');

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 5, p.id FROM `sys_permission` p WHERE p.code IN ('admin:user:remark', 'admin:user:export');

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 3, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:user:view', 'admin:user:view_behavior');

DELETE FROM `sys_data_scope` WHERE `role_id` = 3 AND `scope_type` = 'ALL';
INSERT INTO `sys_data_scope` (`role_id`, `scope_type`, `scope_value`, `status`) VALUES
(3, 'AUDIT_UPLOADER', NULL, 1)
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);

SELECT code, name FROM sys_permission WHERE code LIKE 'admin:user:%' ORDER BY sort;
