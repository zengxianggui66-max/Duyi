# OAuth 第三方登录 — 启动与验证指南

## 当前状态

| 模块 | 状态 | 说明 |
|------|------|------|
| `user_oauth_bind` 表 SQL | ✅ | `sql/11_user_oauth_bind.sql` |
| AuthController 6 端点 | ✅ | 6 个 OAuth 端点全部实现 |
| OAuthServiceImpl | ✅ | Mock模式已启用，QQ/微信/企微 |
| OAuthStateService | ✅ | 内存存储，5分钟过期 |
| SecurityConfig | ✅ | 网关鉴权，auth微服务全放行 |
| Gateway 路由 | ✅ | `/api/auth/**` → k12-auth:8081 |
| 前端 Login.vue | ✅ | 三方登录按钮 |
| 前端 OAuthCallback.vue | ✅ | 授权回调处理 |
| 前端 OAuthSelectRole.vue | ✅ | 新用户选身份 |
| 前端 AccountSecurity.vue | ✅ | 绑定/解绑管理 |
| 前端 router/index.ts | ✅ | OAuth 路由已注册 |

## 启动步骤（3 步走）

### Step 1: 数据库初始化

```powershell
cd k12-edu-microservice

# 执行数据库初始化脚本
.\scripts\setup-oauth-db.ps1

# 或者手动执行 SQL：
# mysql -u root -pzxg123456 < sql\11_user_oauth_bind.sql
```

脚本会自动：
1. 检查 MySQL 连接
2. 确保 `xinketang` 数据库存在
3. 检查 `user_oauth_bind` 表是否创建
4. 验证 `user` 表的 `oauth_type`/`oauth_id` 字段

### Step 2: 启动后端服务

```powershell
# 终端1: 启动 Gateway（端口 9001）
cd k12-edu-microservice\k12-gateway
mvn spring-boot:run

# 终端2: 启动 k12-auth（端口 8081）
cd k12-edu-microservice\k12-auth
mvn spring-boot:run
```

### Step 3: 启动前端

```powershell
# 终端3: 启动前端（端口 5173）
cd k12-edu-platform
npm run dev
```

## 端到端验证

### 3.1 快速接口测试（PowerShell）

```powershell
cd k12-edu-microservice
.\scripts\test-oauth-e2e.ps1
```

### 3.2 浏览器体验

1. 打开 `http://localhost:5173/login`
2. 选择身份（教师/学生/家长）
3. 点击底部的 **"微" / "Q" / "企"** 三个微信/QQ/企微按钮
4. Mock 模式下会直接跳转到回调页 → 自动完成登录
5. 新用户会自动弹出身份选择页 → 选身份后完成注册

### 3.3 绑定/解绑测试

1. 用 `admin / admin123` 登录
2. 进入 个人中心 → 账号安全 → 第三方账号
3. 点击"绑定"微信 → Mock 模式直接完成绑定
4. 绑定后会显示"已绑定"
5. 点击"解绑"测试解绑流程

## Mock 模式说明

当前 `application.yml` 配置：
```yaml
oauth:
  mock:
    enabled: true  # true=模拟模式，false=走真实OAuth
```

Mock 模式下：
- 授权 URL 直接指向前端回调页，附带 `mock_xxx` code
- 回调后自动生成模拟的用户信息
- 不需要申请任何第三方 AppID

## 后续升级到真实 OAuth

1. 申请 **微信开放平台** AppID → `application.yml` 填入 `weixin.app-id/app-secret`
2. 申请 **QQ 互联** AppID → 填入 `qq.app-id/app-secret`
3. 设置 `oauth.mock.enabled: false`
4. 配置各平台回调地址为 `https://你的域名/login/oauth/callback`

## 常见问题排查

| 现象 | 原因 | 解决 |
|------|------|------|
| "系统繁忙" | user_oauth_bind 表不存在 | 执行 setup-oauth-db.ps1 |
| 401 登录已过期 | Gateway 未启动 | 启动 k12-gateway |
| 404 页面找不到 | 路由未注册 | 确认 router/index.ts 已更新 |
| OAuth 回调白屏 | 前端路由未注册 | 重建 router/index.ts |
| 绑定后看不到状态 | loadBinds 异常 | 检查 MySQL 连接 |
