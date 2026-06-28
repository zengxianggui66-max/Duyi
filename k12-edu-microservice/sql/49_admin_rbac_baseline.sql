-- 管理端 RBAC 基线（第一阶段）
-- mysql -u root -p xinketang --default-character-set=utf8mb4
-- source sql/49_admin_rbac_baseline.sql
USE xinketang;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(32) NOT NULL COMMENT '角色编码',
  `name` VARCHAR(64) NOT NULL COMMENT '角色名称',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0禁用 1正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色';

CREATE TABLE IF NOT EXISTS `sys_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(64) NOT NULL COMMENT '权限编码',
  `name` VARCHAR(64) NOT NULL COMMENT '权限名称',
  `type` VARCHAR(16) NOT NULL DEFAULT 'menu' COMMENT 'menu/api/button',
  `path` VARCHAR(255) DEFAULT NULL,
  `method` VARCHAR(16) DEFAULT NULL,
  `parent_id` BIGINT DEFAULT 0,
  `sort` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限';

CREATE TABLE IF NOT EXISTS `sys_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `parent_id` BIGINT NOT NULL DEFAULT 0,
  `title` VARCHAR(64) NOT NULL,
  `path` VARCHAR(128) NOT NULL COMMENT '前端路由 path',
  `name` VARCHAR(64) NOT NULL COMMENT '路由 name',
  `icon` VARCHAR(64) DEFAULT NULL,
  `component` VARCHAR(128) DEFAULT NULL,
  `permission_code` VARCHAR(64) DEFAULT NULL,
  `sort` INT NOT NULL DEFAULT 0,
  `hidden` TINYINT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_menu_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理端菜单';

CREATE TABLE IF NOT EXISTS `sys_role_permission` (
  `role_id` BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  PRIMARY KEY (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色';

-- 角色
INSERT INTO sys_role (id, code, name, status) VALUES
(1, 'admin', '超级管理员', 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 权限点
INSERT INTO sys_permission (id, code, name, type, sort, status) VALUES
(1, 'admin:dashboard:view', '控制台', 'menu', 1, 1),
(2, 'admin:resource:view', '资源管理', 'menu', 2, 1),
(3, 'admin:audit:view', '审核中心', 'menu', 3, 1),
(4, 'admin:user:view', '用户管理', 'menu', 4, 1),
(5, 'admin:taxonomy:view', '分类目录', 'menu', 5, 1),
(6, 'admin:home:view', '首页配置', 'menu', 6, 1),
(7, 'admin:system:view', '系统设置', 'menu', 7, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 角色绑定全部权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE status = 1;

-- admin 用户绑定 admin 角色
INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 菜单
DELETE FROM sys_menu WHERE id BETWEEN 1 AND 7;
INSERT INTO sys_menu (id, parent_id, title, path, name, icon, component, permission_code, sort, hidden, status) VALUES
(1, 0, '控制台', '/admin/dashboard', 'AdminDashboard', 'DataBoard', 'admin/views/dashboard/Dashboard', 'admin:dashboard:view', 1, 0, 1),
(2, 0, '资源管理', '/admin/resources', 'AdminResources', 'FolderOpened', 'admin/views/resources/ResourceList', 'admin:resource:view', 2, 0, 1),
(3, 0, '审核中心', '/admin/audit/resources', 'AdminAuditResources', 'Document', 'admin/views/audit/ResourceAudit', 'admin:audit:view', 3, 0, 1),
(4, 0, '用户管理', '/admin/users', 'AdminUsers', 'User', 'admin/views/users/UserList', 'admin:user:view', 4, 0, 1),
(5, 0, '分类目录', '/admin/taxonomy', 'AdminTaxonomy', 'Collection', 'admin/views/taxonomy/Taxonomy', 'admin:taxonomy:view', 5, 0, 1),
(6, 0, '首页配置', '/admin/home-config', 'AdminHomeConfig', 'HomeFilled', 'admin/views/home-config/HomeConfig', 'admin:home:view', 6, 0, 1),
(7, 0, '系统设置', '/admin/system', 'AdminSystem', 'Setting', 'admin/views/system/SystemSettings', 'admin:system:view', 7, 0, 1);
