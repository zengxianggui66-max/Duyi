# ============================================================
# OAuth 数据库初始化脚本（执行 36_oauth_bind_baseline.sql）
# ============================================================
param(
    [string]$MySQLHost = "localhost",
    [string]$MySQLPort = "3306",
    [string]$MySQLUser = "root",
    [string]$MySQLPassword = "zxg123456",
    [string]$Database = "xinketang"
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$SqlFile = Resolve-Path "$ScriptDir\..\sql\36_oauth_bind_baseline.sql"
$MySQL = "mysql"

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  K12 新课堂教育 — OAuth 数据库初始化" -ForegroundColor Cyan
Write-Host "  SQL: $SqlFile" -ForegroundColor White
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""

# 1. MySQL 连通性检查
Write-Host "[1/3] MySQL 连接..." -ForegroundColor Yellow -NoNewline
$testArgs = @("-h", $MySQLHost, "-P", $MySQLPort, "-u", $MySQLUser, "-p$MySQLPassword", "-e", "SELECT 1")
$r = & $MySQL @testArgs 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host " FAIL" -ForegroundColor Red
    Write-Host $r -ForegroundColor Red
    exit 1
}
Write-Host " OK" -ForegroundColor Green

# 2. 执行 SQL
Write-Host "[2/3] 执行 36_oauth_bind_baseline.sql..." -ForegroundColor Yellow
$execArgs = @("-h", $MySQLHost, "-P", $MySQLPort, "-u", $MySQLUser, "-p$MySQLPassword")
Get-Content $SqlFile | & $MySQL @execArgs 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "  FAIL — 请检查 SQL 执行输出" -ForegroundColor Red
    exit 1
}
Write-Host "  OK" -ForegroundColor Green

# 3. 验证
Write-Host "[3/3] 验证表结构..." -ForegroundColor Yellow -NoNewline
$verifyArgs = @("-h", $MySQLHost, "-P", $MySQLPort, "-u", $MySQLUser, "-p$MySQLPassword", $Database, "-e", "SHOW TABLES LIKE 'user_oauth_bind'")
$tables = & $MySQL @verifyArgs 2>&1
if ($tables -match "user_oauth_bind") {
    Write-Host " OK — user_oauth_bind 就绪" -ForegroundColor Green
} else {
    Write-Host " FAIL" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "  OAuth 表初始化完成！" -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步：" -ForegroundColor White
Write-Host "  1. Gateway:  cd k12-gateway && mvn spring-boot:run" -ForegroundColor Gray
Write-Host "  2. k12-auth: cd k12-auth && mvn spring-boot:run" -ForegroundColor Gray
Write-Host "  3. 前端:     cd k12-edu-platform && npm run dev" -ForegroundColor Gray
Write-Host "  4. 测试:     http://localhost:5173/login → 选择身份 → 微信/QQ/企微" -ForegroundColor Gray
Write-Host ""
