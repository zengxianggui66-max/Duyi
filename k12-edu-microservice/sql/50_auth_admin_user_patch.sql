-- ============================================================
-- 50 管理端 admin 用户补丁（独立于 06_auth.sql，可重复执行）
-- 适用：已执行过 sql/06_auth.sql 的库
--
-- 问题：06 种子中 admin 的 BCrypt 哈希实际对应密码 123456，非 admin123
-- 本脚本：修正密码 + 确保 admin 角色与管理端 RBAC 绑定
--
-- mysql -u root -p xinketang --default-character-set=utf8mb4
-- source sql/50_auth_admin_user_patch.sql
-- ============================================================
USE xinketang;
SET NAMES utf8mb4;

-- admin123 的 BCrypt 哈希（Spring BCryptPasswordEncoder）
SET @admin_pwd := '$2a$10$MZlQWDC7/eI7dkbXopadk.w/YgwU6lHU/lIRUK5SardpKvboOqlMu';

-- 1. 若 admin 已存在：更新密码与角色
UPDATE `user`
SET
  `password` = @admin_pwd,
  `nickname` = IF(`nickname` IS NULL OR `nickname` = '', '管理员', `nickname`),
  `role` = 'admin',
  `member_level` = 2,
  `status` = 1,
  `deleted` = 0
WHERE `username` = 'admin';

-- 2. 若 admin 不存在：插入（不覆盖已有 id=1 的其他用户）
INSERT INTO `user` (
  `id`, `username`, `password`, `nickname`, `role`,
  `member_level`, `status`, `deleted`, `oauth_type`, `oauth_id`
)
SELECT 1, 'admin', @admin_pwd, '管理员', 'admin', 2, 1, 0, NULL, NULL
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'admin');

-- 3. 绑定管理端 RBAC（需先执行 sql/49_admin_rbac_baseline.sql）
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `user` u
JOIN `sys_role` r ON r.code IN ('super_admin', 'admin')
WHERE u.username = 'admin';

-- 4. 校验
SELECT '=== admin 用户 ===' AS section;
SELECT id, username, nickname, role, status, LEFT(password, 29) AS pwd_prefix
FROM `user`
WHERE username = 'admin';

SELECT '=== admin 角色绑定 ===' AS section;
SELECT ur.user_id, u.username, r.code AS role_code
FROM sys_user_role ur
JOIN `user` u ON u.id = ur.user_id
JOIN sys_role r ON r.id = ur.role_id
WHERE u.username = 'admin';
