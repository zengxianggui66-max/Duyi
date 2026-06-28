@echo off
REM Phase 2 Step 1 — 导入测试账号（避免 PowerShell 展开 BCrypt 中的 $）
REM 用法: scripts\run-phase2-step1-sql.cmd
REM 可选: set MYSQL_PWD=yourpassword

cd /d "%~dp0.."
if defined MYSQL_PWD (
  mysql -u root -p%MYSQL_PWD% xinketang --default-character-set=utf8mb4 < sql\81_phase2_step1_baseline.sql
) else (
  mysql -u root xinketang --default-character-set=utf8mb4 < sql\81_phase2_step1_baseline.sql
)
if errorlevel 1 exit /b 1
echo SQL 81 applied.
exit /b 0
