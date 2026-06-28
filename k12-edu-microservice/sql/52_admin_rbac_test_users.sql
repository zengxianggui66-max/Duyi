-- ============================================================
-- 52 管理端 RBAC 测试账号（auditor 等）
-- 前置：sql/49 + sql/51
-- mysql -u root -p xinketang --default-character-set=utf8mb4
-- source sql/52_admin_rbac_test_users.sql
-- ============================================================
USE xinketang;
SET NAMES utf8mb4;

-- auditor123 / content123 的 BCrypt
SET @auditor_pwd  := '$2a$10$MZlQWDC7/eI7dkbXopadk.w/YgwU6lHU/lIRUK5SardpKvboOqlMu';
SET @content_pwd  := @auditor_pwd;

INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `member_level`, `status`, `deleted`)
SELECT 'auditor', @auditor_pwd, '审核员', 'admin', 0, 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'auditor');

INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `member_level`, `status`, `deleted`)
SELECT 'content_admin', @content_pwd, '内容管理员', 'admin', 0, 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'content_admin');

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id FROM `user` u JOIN `sys_role` r ON r.code = 'auditor'
WHERE u.username = 'auditor';

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id FROM `user` u JOIN `sys_role` r ON r.code = 'content_admin'
WHERE u.username = 'content_admin';

-- 确保 admin 绑定 super_admin
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, 1 FROM `user` u WHERE u.username = 'admin';

SELECT '=== 测试账号 ===' AS section;
SELECT u.id, u.username, u.nickname, u.role, GROUP_CONCAT(r.code) AS admin_roles
FROM `user` u
LEFT JOIN sys_user_role ur ON ur.user_id = u.id
LEFT JOIN sys_role r ON r.id = ur.role_id
WHERE u.username IN ('admin', 'auditor', 'content_admin')
GROUP BY u.id, u.username, u.nickname, u.role;

-- 测试密码：auditor / auditor123（与 admin123 相同哈希，便于本地联调）
