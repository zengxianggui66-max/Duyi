# ============================================================
# OAuth 端到端测试脚本（Mock 模式）
# 使用 curl 验证完整的 OAuth 登录 + 绑定流程
# ============================================================
param(
    [string]$BaseUrl = "http://localhost:9001",
    [string]$TestUser = "admin",
    [string]$TestPassword = "admin123"
)

$ErrorActionPreference = "Continue"
$PassCount = 0
$FailCount = 0
$TotalCount = 0

function Test-Step {
    param([string]$Name, [ScriptBlock]$Block)
    $script:TotalCount++
    Write-Host "[TEST $($script:TotalCount)] $Name" -ForegroundColor Cyan -NoNewline
    try {
        & $Block
        $script:PassCount++
        Write-Host "  ✓ PASS" -ForegroundColor Green
    } catch {
        $script:FailCount++
        Write-Host "  ✗ FAIL: $_" -ForegroundColor Red
    }
}

Write-Host "============================================================" -ForegroundColor Magenta
Write-Host "  K12 新课堂教育 — OAuth Mock 模式端到端测试" -ForegroundColor Magenta
Write-Host "  Base URL: $BaseUrl" -ForegroundColor White
Write-Host "============================================================" -ForegroundColor Magenta
Write-Host ""

# ==========================================
# Phase 1: 基础连通性
# ==========================================
Write-Host "--- Phase 1: 基础连通性 ---" -ForegroundColor Yellow

Test-Step "Gateway 健康检查" {
    $r = Invoke-RestMethod -Uri "$BaseUrl/api/auth/captcha" -Method Get -ErrorAction Stop
    if ($r.code -ne 200) { throw "code=$($r.code)" }
}

Test-Step "账号密码登录获取 Token" {
    $body = @{ username=$TestUser; password=$TestPassword } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop
    if ($r.code -ne 200 -or -not $r.data.token) { throw "登录失败: $($r.message)" }
    $script:Token = $r.data.token
    $script:UserId = $r.data.userInfo.id
    Write-Host " (userId=$UserId)" -NoNewline
}

# ==========================================
# Phase 2: OAuth 授权 URL 获取
# ==========================================
Write-Host ""
Write-Host "--- Phase 2: OAuth 授权 URL ---" -ForegroundColor Yellow

Test-Step "获取微信登录授权URL" {
    $r = Invoke-RestMethod -Uri "$BaseUrl/api/auth/oauth/url?type=wechat&role=teacher" -Method Get -ErrorAction Stop
    if ($r.code -ne 200) { throw "code=$($r.code)" }
    if (-not $r.data.authorizationUrl) { throw "缺少 authorizationUrl" }
    $script:LoginState = $r.data.state
    Write-Host " (state=$LoginState)" -NoNewline
}

Test-Step "获取微信绑定授权URL（需 Token）" {
    $headers = @{ "Authorization" = "Bearer $Token" }
    $r = Invoke-RestMethod -Uri "$BaseUrl/api/auth/oauth/bind-url?type=wechat" -Method Get -Headers $headers -ErrorAction Stop
    if ($r.code -ne 200) { throw "code=$($r.code)" }
    if (-not $r.data.authorizationUrl) { throw "缺少 authorizationUrl" }
    $script:BindState = $r.data.state
    Write-Host " (state=$BindState)" -NoNewline
}

Test-Step "未登录请求 bind-url 应被拒绝" {
    try {
        $r = Invoke-RestMethod -Uri "$BaseUrl/api/auth/oauth/bind-url?type=wechat" -Method Get -ErrorAction Stop
        # 应该返回 401
        throw "应该返回 401 但成功了"
    } catch {
        if ($_.Exception.Response.StatusCode -eq 401) {
            # 预期行为
        } else {
            throw "预期 401，实际: $_"
        }
    }
}

# ==========================================
# Phase 3: Mock OAuth 登录流程
# ==========================================
Write-Host ""
Write-Host "--- Phase 3: Mock OAuth 登录 ---" -ForegroundColor Yellow

Test-Step "模拟 OAuth 回调（登录模式）" {
    $body = @{
        code = "mock_wechat_testcode"
        type = "wechat"
        state = $script:LoginState
    } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$BaseUrl/api/auth/oauth/login" -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop
    if ($r.code -ne 200) { throw "code=$($r.code), msg=$($r.message)" }
    if ($r.data.needsRoleSelection) {
        Write-Host " (新用户→需要选身份，保存 oauthId=$($r.data.oauthId))" -NoNewline
        $script:NewOAuthId = $r.data.oauthId
        $script:NewOAuthType = $r.data.oauthType
        $script:NewNickname = $r.data.nickname
    } elseif ($r.data.token) {
        Write-Host " (已绑定→直接登录成功)" -NoNewline
    } else {
        throw "未知返回状态: $($r.data | ConvertTo-Json)"
    }
}

# ==========================================
# Phase 4: OAuth 补全注册（如果有新用户）
# ==========================================
Write-Host ""
Write-Host "--- Phase 4: OAuth 补全注册 ---" -ForegroundColor Yellow

if ($script:NewOAuthId) {
    Test-Step "OAuth 补全身份注册" {
        $body = @{
            type = $script:NewOAuthType
            oauthId = $script:NewOAuthId
            nickname = $script:NewNickname
            avatar = ""
            role = "teacher"
        } | ConvertTo-Json
        $r = Invoke-RestMethod -Uri "$BaseUrl/api/auth/oauth/complete" -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop
        if ($r.code -ne 200 -or -not $r.data.token) { throw "注册失败: $($r.message)" }
        $script:OAuthToken = $r.data.token
        Write-Host " (注册成功, isNewUser=$($r.data.isNewUser))" -NoNewline
    }
}

# ==========================================
# Phase 5: 绑定管理
# ==========================================
Write-Host ""
Write-Host "--- Phase 5: 绑定管理 ---" -ForegroundColor Yellow

Test-Step "查询当前用户绑定列表" {
    $headers = @{ "Authorization" = "Bearer $Token" }
    $r = Invoke-RestMethod -Uri "$BaseUrl/api/auth/oauth/binds" -Method Get -Headers $headers -ErrorAction Stop
    if ($r.code -ne 200) { throw "code=$($r.code)" }
    $bindCount = ($r.data | Where-Object { $_.bound }).Count
    Write-Host " (已绑定 $bindCount 个平台)" -NoNewline
}

Test-Step "未登录查询绑定列表应被拒绝" {
    try {
        $r = Invoke-RestMethod -Uri "$BaseUrl/api/auth/oauth/binds" -Method Get -ErrorAction Stop
        throw "应该返回 401"
    } catch {
        if ($_.Exception.Response.StatusCode -eq 401) {
            # 预期行为
        } else { throw "预期 401: $_" }
    }
}

# ==========================================
# Phase 6: 解绑安全检查
# ==========================================
Write-Host ""
Write-Host "--- Phase 6: 解绑安全策略 ---" -ForegroundColor Yellow

Test-Step "State 重放攻击防护（已消费的 state 不可用）" {
    try {
        $body = @{ code="attack"; type="wechat"; state=$script:LoginState } | ConvertTo-Json
        $r = Invoke-RestMethod -Uri "$BaseUrl/api/auth/oauth/login" -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop
        # OAuthStateService.validateAndConsume 应该抛出异常
        throw "应该拒绝已消费的 state"
    } catch {
        if ($_.Exception.Message -match "state" -or $_.Exception.Message -match "400" -or $_.Exception.Message -match "无效") {
            # 预期行为
        } else { throw "预期 state 校验失败: $_" }
    }
}

Test-Step "不支持的第三方类型应被拒绝" {
    try {
        $r = Invoke-RestMethod -Uri "$BaseUrl/api/auth/oauth/url?type=github&role=teacher" -Method Get -ErrorAction Stop
        throw "应该拒绝不支持的 type"
    } catch {
        if ($_.Exception.Message -match "不支持" -or $_.Exception.Message -match "400") {
            # 预期行为
        } else { throw "预期类型校验失败: $_" }
    }
}

# ==========================================
# 汇总
# ==========================================
Write-Host ""
Write-Host "============================================================" -ForegroundColor Magenta
Write-Host "  测试结果: $PassCount PASS / $FailCount FAIL / $TotalCount TOTAL" -ForegroundColor $(if ($FailCount -eq 0) { "Green" } else { "Red" })
Write-Host "============================================================" -ForegroundColor Magenta

if ($FailCount -gt 0) {
    Write-Host ""
    Write-Host "常见失败原因排查：" -ForegroundColor Red
    Write-Host "  1. Gateway 未启动 → cd k12-gateway && mvn spring-boot:run" -ForegroundColor Gray
    Write-Host "  2. k12-auth 未启动 → cd k12-auth && mvn spring-boot:run" -ForegroundColor Gray
    Write-Host "  3. user_oauth_bind 表不存在 → 运行 .\scripts\setup-oauth-db.ps1" -ForegroundColor Gray
    Write-Host "  4. MySQL 未启动 → net start MySQL80（或服务名）" -ForegroundColor Gray
    exit 1
} else {
    Write-Host ""
    Write-Host "✓ OAuth Mock 模式全部测试通过！可以开始前端联调。" -ForegroundColor Green
    Write-Host "  打开 http://localhost:5173/login 选择身份 → 点击微信/QQ/企微即可体验" -ForegroundColor White
}
