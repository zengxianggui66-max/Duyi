-- Phase 6-A：用户管理（平台用户列表/详情 + 登录流水 + 菜单）
USE `xinketang`;
SET NAMES utf8mb4;

-- ---------- 1. 用户登录流水 ----------
CREATE TABLE IF NOT EXISTS `user_login_log` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT       DEFAULT NULL COMMENT '失败时可能为空',
  `username`     VARCHAR(64)  DEFAULT NULL,
  `login_type`   VARCHAR(20)  NOT NULL COMMENT 'password/sms/oauth/admin',
  `success`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1成功 0失败',
  `fail_reason`  VARCHAR(128) DEFAULT NULL,
  `ip`           VARCHAR(64)  DEFAULT NULL,
  `user_agent`   VARCHAR(512) DEFAULT NULL,
  `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `create_time`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户登录流水';

-- ---------- 2. 权限扩展（按 code 插入，勿占用 id 28-30：已被 sql/54 使用） ----------
INSERT INTO `sys_permission` (`code`, `name`, `type`, `module`, `parent_id`, `sort`, `status`) VALUES
('admin:user:view_behavior',    '查看用户行为',   'button', 'user', 4, 44, 1),
('admin:admin_user:view',       '查看管理员账号', 'menu',   'user', 4, 45, 1),
('admin:admin_user:assign_role','分配后台角色',   'button', 'user', 4, 46, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `type` = VALUES(`type`),
  `module` = VALUES(`module`),
  `parent_id` = VALUES(`parent_id`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);

-- super_admin 全权限（含新增）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:user:view_behavior', 'admin:admin_user:view', 'admin:admin_user:assign_role');

-- customer_service：行为只读
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 5, p.id FROM `sys_permission` p WHERE p.code = 'admin:user:view_behavior';

-- system_admin：管理员账号 + 分配角色
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 7, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:admin_user:view', 'admin:admin_user:assign_role');
-- ---------- 3. 菜单 ----------
UPDATE `sys_menu` SET
  `title` = '平台用户',
  `path` = '/admin/users',
  `component` = 'admin/views/users/UserList',
  `sort` = 41
WHERE `id` = 4;

INSERT INTO `sys_menu` (`id`, `parent_id`, `title`, `path`, `name`, `icon`, `component`, `permission_code`, `sort`, `hidden`, `status`)
VALUES
(55, 0, '管理员账号', '/admin/admin-users', 'AdminStaffUsers', 'UserFilled',
 'admin/views/users/AdminUserList', 'admin:admin_user:view', 42, 0, 1)
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `permission_code` = VALUES(`permission_code`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);

-- 用户详情为隐藏路由（侧栏不展示）
INSERT INTO `sys_menu` (`id`, `parent_id`, `title`, `path`, `name`, `icon`, `component`, `permission_code`, `sort`, `hidden`, `status`)
VALUES
(56, 4, '用户详情', '/admin/users/:id', 'AdminUserDetail', NULL,
 'admin/views/users/UserDetail', 'admin:user:view', 0, 1, 1)
ON DUPLICATE KEY UPDATE
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `hidden` = 1,
  `status` = VALUES(`status`);
