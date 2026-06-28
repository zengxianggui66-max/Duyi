-- ============================================================
-- 51 管理端 RBAC 第二阶段（角色/按钮权限/数据范围/操作日志）
-- 前置：sql/49_admin_rbac_baseline.sql
-- mysql -u root -p xinketang --default-character-set=utf8mb4
-- source sql/51_admin_rbac_phase2.sql
-- ============================================================
USE xinketang;
SET NAMES utf8mb4;

-- ---------- 1. 扩展现有表（可重复执行，列已存在则跳过） ----------
SET @db = DATABASE();

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sys_role' AND COLUMN_NAME = 'description') = 0,
  'ALTER TABLE `sys_role` ADD COLUMN `description` VARCHAR(255) DEFAULT NULL COMMENT ''角色描述''',
  'SELECT 1');
PREPARE _s FROM @sql; EXECUTE _s; DEALLOCATE PREPARE _s;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sys_role' AND COLUMN_NAME = 'is_builtin') = 0,
  'ALTER TABLE `sys_role` ADD COLUMN `is_builtin` TINYINT NOT NULL DEFAULT 0 COMMENT ''内置角色不可删''',
  'SELECT 1');
PREPARE _s FROM @sql; EXECUTE _s; DEALLOCATE PREPARE _s;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sys_role' AND COLUMN_NAME = 'update_time') = 0,
  'ALTER TABLE `sys_role` ADD COLUMN `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP',
  'SELECT 1');
PREPARE _s FROM @sql; EXECUTE _s; DEALLOCATE PREPARE _s;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sys_permission' AND COLUMN_NAME = 'module') = 0,
  'ALTER TABLE `sys_permission` ADD COLUMN `module` VARCHAR(32) DEFAULT NULL COMMENT ''所属模块''',
  'SELECT 1');
PREPARE _s FROM @sql; EXECUTE _s; DEALLOCATE PREPARE _s;

-- ---------- 2. 数据范围表 ----------
CREATE TABLE IF NOT EXISTS `sys_data_scope` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL,
  `scope_type` VARCHAR(32) NOT NULL COMMENT 'ALL|SELF|STAGE_SUBJECT',
  `scope_value` JSON DEFAULT NULL COMMENT '如 {"stages":["primary"],"subjects":["chinese"]}',
  `status` TINYINT NOT NULL DEFAULT 1,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_scope` (`role_id`, `scope_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色数据权限';

-- ---------- 3. 操作日志表 ----------
CREATE TABLE IF NOT EXISTS `sys_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `username` VARCHAR(64) DEFAULT NULL,
  `module` VARCHAR(32) DEFAULT NULL,
  `action` VARCHAR(64) DEFAULT NULL,
  `permission` VARCHAR(64) DEFAULT NULL,
  `request_uri` VARCHAR(255) DEFAULT NULL,
  `request_method` VARCHAR(16) DEFAULT NULL,
  `request_params` TEXT,
  `ip` VARCHAR(64) DEFAULT NULL,
  `status` TINYINT DEFAULT 1 COMMENT '0失败 1成功',
  `error_msg` VARCHAR(500) DEFAULT NULL,
  `duration_ms` INT DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `create_time`),
  KEY `idx_module` (`module`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理端操作日志';

-- ---------- 4. 角色：admin → super_admin，并补 6 个内置角色 ----------
UPDATE `sys_role` SET `code` = 'super_admin', `name` = '超级管理员', `description` = '拥有全部权限', `is_builtin` = 1 WHERE `id` = 1;

INSERT INTO `sys_role` (`id`, `code`, `name`, `description`, `status`, `is_builtin`) VALUES
(2, 'content_admin', '内容管理员', '资源、分类、首页内容管理', 1, 1),
(3, 'auditor', '审核员', '资源审核', 1, 1),
(4, 'operator', '运营人员', '首页与分类运营', 1, 1),
(5, 'customer_service', '客服', '用户查询与基础操作', 1, 1),
(6, 'finance_admin', '会员/订单管理员', '会员与订单（预留）', 1, 1),
(7, 'system_admin', '系统配置管理员', '角色权限与系统配置', 1, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `description` = VALUES(`description`),
  `is_builtin` = VALUES(`is_builtin`),
  `status` = VALUES(`status`);

-- ---------- 5. 按钮/API 权限（菜单 1-7 保持不变） ----------
INSERT INTO `sys_permission` (`id`, `code`, `name`, `type`, `module`, `parent_id`, `sort`, `status`) VALUES
(8,  'admin:resource:create',           '新增资源',     'button', 'resource', 2, 21, 1),
(9,  'admin:resource:edit',             '编辑资源',     'button', 'resource', 2, 22, 1),
(10, 'admin:resource:delete',           '删除资源',     'button', 'resource', 2, 23, 1),
(11, 'admin:resource:export',           '导出资源',     'button', 'resource', 2, 24, 1),
(12, 'admin:resource:publish',          '发布资源',     'button', 'resource', 2, 25, 1),
(13, 'admin:resource:offline',          '下架资源',     'button', 'resource', 2, 26, 1),
(14, 'admin:audit:approve',             '审核通过',     'button', 'audit',    3, 31, 1),
(15, 'admin:audit:reject',              '审核驳回',     'button', 'audit',    3, 32, 1),
(16, 'admin:user:edit',                 '编辑用户',     'button', 'user',     4, 41, 1),
(17, 'admin:user:reset_password',       '重置密码',     'button', 'user',     4, 42, 1),
(18, 'admin:user:assign_role',          '分配后台角色', 'button', 'user',     4, 43, 1),
(19, 'admin:taxonomy:edit',             '编辑分类',     'button', 'taxonomy', 5, 51, 1),
(20, 'admin:home:edit',                 '编辑首页',     'button', 'home',     6, 61, 1),
(21, 'admin:system:role_manage',        '角色管理',     'button', 'system',   7, 71, 1),
(22, 'admin:system:permission_manage',  '权限管理',     'button', 'system',   7, 72, 1),
(23, 'admin:system:config_edit',        '系统配置',     'button', 'system',   7, 73, 1),
(24, 'admin:role:view',                 '查看角色',     'button', 'role',     7, 81, 1),
(25, 'admin:role:create',               '创建角色',     'button', 'role',     7, 82, 1),
(26, 'admin:role:edit',                 '编辑角色',     'button', 'role',     7, 83, 1),
(27, 'admin:role:assign_permission',    '分配权限',     'button', 'role',     7, 84, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `type` = VALUES(`type`),
  `module` = VALUES(`module`),
  `parent_id` = VALUES(`parent_id`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);

UPDATE `sys_permission` SET `module` = 'dashboard' WHERE `id` = 1;
UPDATE `sys_permission` SET `module` = 'resource'  WHERE `id` = 2;
UPDATE `sys_permission` SET `module` = 'audit'     WHERE `id` = 3;
UPDATE `sys_permission` SET `module` = 'user'      WHERE `id` = 4;
UPDATE `sys_permission` SET `module` = 'taxonomy'  WHERE `id` = 5;
UPDATE `sys_permission` SET `module` = 'home'      WHERE `id` = 6;
UPDATE `sys_permission` SET `module` = 'system'    WHERE `id` = 7;

-- ---------- 6. 角色-权限绑定（重建内置角色 1～7，可重复执行） ----------
-- 仅清内置角色映射，保留自定义角色的权限关联
DELETE FROM `sys_role_permission` WHERE `role_id` BETWEEN 1 AND 7;

-- super_admin：全部（INSERT IGNORE 防止跳过 DELETE 时主键冲突）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, `id` FROM `sys_permission` WHERE `status` = 1;

-- content_admin
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(2,1),(2,2),(2,5),(2,6),(2,8),(2,9),(2,10),(2,11),(2,12),(2,13),(2,19),(2,20);

-- auditor（Phase 4-B 后需再执行 sql/57 或 sql/57b，否则无「审核记录」菜单）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(3,1),(3,3),(3,14),(3,15);

-- operator
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(4,1),(4,5),(4,6),(4,19),(4,20);

-- customer_service
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(5,1),(5,4),(5,16),(5,17);

-- finance_admin（预留，仅用户只读）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(6,1),(6,4);

-- system_admin
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(7,1),(7,7),(7,21),(7,22),(7,23),(7,24),(7,25),(7,26),(7,27);

-- admin 用户绑定 super_admin
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, 1 FROM `user` u WHERE u.username = 'admin';

-- ---------- 7. 数据范围种子（仅重建内置角色） ----------
DELETE FROM `sys_data_scope` WHERE `role_id` BETWEEN 1 AND 7;
INSERT IGNORE INTO `sys_data_scope` (`role_id`, `scope_type`, `scope_value`, `status`) VALUES
(1, 'ALL', NULL, 1),
(2, 'STAGE_SUBJECT', JSON_OBJECT('stages', JSON_ARRAY('primary'), 'subjects', JSON_ARRAY('chinese', 'math')), 1),
(3, 'ALL', NULL, 1),
(4, 'ALL', NULL, 1),
(5, 'ALL', NULL, 1),
(6, 'ALL', NULL, 1),
(7, 'ALL', NULL, 1);

-- ---------- 8. 校验 ----------
SELECT '=== 角色 ===' AS section;
SELECT id, code, name, is_builtin, status FROM sys_role ORDER BY id;

SELECT '=== 权限数量 ===' AS section;
SELECT type, COUNT(*) AS cnt FROM sys_permission WHERE status = 1 GROUP BY type;

SELECT '=== auditor 权限 ===' AS section;
SELECT r.code AS role_code, p.code AS permission_code
FROM sys_role r
JOIN sys_role_permission rp ON rp.role_id = r.id
JOIN sys_permission p ON p.id = rp.permission_id
WHERE r.code = 'auditor'
ORDER BY p.sort;
