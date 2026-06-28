SELECT '--- user tables ---' AS info;
SHOW TABLES LIKE '%user%';
SELECT '--- check sys_user ---' AS info;
SHOW COLUMNS FROM sys_user;
SELECT id, username FROM sys_user LIMIT 5;
