@echo off
REM Phase 2 — 幂等补全 audit_status / publish_status（sql/82，可重复执行）
REM sql/80 已执行过的环境：本脚本仅校验并刷新 VIEW/回填，不会重复 ADD COLUMN
REM 用法: scripts\run-sql82-resource-status.cmd
REM 可选: set MYSQL_PWD=yourpassword

setlocal
cd /d "%~dp0.."

set "MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if not exist "%MYSQL_BIN%" set "MYSQL_BIN=mysql"

if defined MYSQL_PWD (
  set "MYSQL_AUTH=-u root -p%MYSQL_PWD%"
) else (
  set "MYSQL_AUTH=-u root -pzxg123456"
)

"%MYSQL_BIN%" %MYSQL_AUTH% --default-character-set=utf8mb4 xinketang < sql\82_phase2_resource_status_idempotent.sql
if errorlevel 1 exit /b 1

echo sql/82 applied.
exit /b 0
