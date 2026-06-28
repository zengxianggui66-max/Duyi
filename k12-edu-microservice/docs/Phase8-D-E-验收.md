# Phase 8-D · 功能开关 Admin 化验收

## 部署

```bash
mysql ... < sql/74_phase8de_feature_flags_menu.sql
mvn install -pl k12-common,k12-auth,k12-gateway -am -DskipTests
# 重启 auth + gateway
node scripts/phase8d-acceptance-test.mjs
```

## API

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/public/feature-flags` | 无登录，仅 runtime 布尔 |
| GET | `/api/admin/system/feature-flags` | Admin 含 buildTime 标注 |
| PUT | `/api/admin/system/config?group=feature` | 修改开关 + 审计 |

## 验收

| ID | 场景 | 预期 |
|----|------|------|
| D1 | 公开 API | 200，含 homeOpsApiEnabled |
| D2 | Admin 列表 | flags ≥ 4 |
| D3 | PUT 开关 | update_config 日志 |
| D4 | 公开 API 同步 | 与 DB 一致 |

---

# Phase 8-E · 前端 6 页验收

## 路由

| 路径 | 页面 |
|------|------|
| `/admin/system/logs` | 操作日志 |
| `/admin/system/login-logs` | 登录日志 |
| `/admin/system/upload-config` | 上传配置 |
| `/admin/system/preview-config` | 预览配置 |
| `/admin/system/storage` | 存储状态 |
| `/admin/system/feature-flags` | 功能开关 |

```bash
node scripts/phase8e-acceptance-test.mjs
```

| ID | 预期 |
|----|------|
| E1–E6 | 各页面对应 API 200 |
