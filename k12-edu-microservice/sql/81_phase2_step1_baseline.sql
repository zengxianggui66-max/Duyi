-- Phase 2 Step 1：基础验收线测试账号
-- 前置：sql/52、sql/78、sql/79
-- mysql -u root -p xinketang --default-character-set=utf8mb4 < sql/81_phase2_step1_baseline.sql
USE `xinketang`;
SET NAMES utf8mb4;

-- 与 admin123 相同 BCrypt（便于本地联调）
SET @pwd := '$2a$10$MZlQWDC7/eI7dkbXopadk.w/YgwU6lHU/lIRUK5SardpKvboOqlMu';

-- 普通 C 端用户（student），不应进入管理端
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `member_level`, `status`, `deleted`)
SELECT 'normal_user', @pwd, '普通学生', 'student', 0, 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'normal_user');

-- staff 身份但未分配 sys_role（应 403：未分配后台角色）
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `member_level`, `status`, `deleted`)
SELECT 'staff_no_role', @pwd, '未分配角色', 'admin', 0, 1, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'staff_no_role');

-- 确保 staff_no_role 无 sys_user_role（可重复执行）
DELETE ur FROM `sys_user_role` ur
INNER JOIN `user` u ON u.id = ur.user_id
WHERE u.username = 'staff_no_role';

SELECT '=== Phase2 Step1 测试账号 ===' AS section;
SELECT u.id, u.username, u.nickname, u.role, GROUP_CONCAT(r.code) AS admin_roles
FROM `user` u
LEFT JOIN sys_user_role ur ON ur.user_id = u.id
LEFT JOIN sys_role r ON r.id = ur.role_id
WHERE u.username IN ('normal_user', 'staff_no_role', 'admin', 'auditor', 'operator', 'content_admin')
GROUP BY u.id, u.username, u.nickname, u.role;

-- 密码：normal_user / staff_no_role 均为 admin123
