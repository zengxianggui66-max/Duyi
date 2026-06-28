@echo off
REM Phase 2 Step 3 — 幂等补全 preview_status / file_safety_status（sql/83）
REM 用法: scripts\run-sql83-preview-safety.cmd

setlocal
cd /d "%~dp0.."

set "MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if not exist "%MYSQL_BIN%" set "MYSQL_BIN=mysql"

if defined MYSQL_PWD (
  set "MYSQL_AUTH=-u root -p%MYSQL_PWD%"
) else (
  set "MYSQL_AUTH=-u root -pzxg123456"
)

"%MYSQL_BIN%" %MYSQL_AUTH% --default-character-set=utf8mb4 xinketang < sql\83_phase2_preview_safety_status.sql
if errorlevel 1 exit /b 1

echo sql/83 applied.
exit /b 0
