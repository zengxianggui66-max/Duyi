# Phase 7-D 验收

## 前置

1. 执行 `sql/70_phase7d_home_latest.sql`
2. 重启 `k12-resource`（`mvn install -pl k12-common,k12-resource -am` 后启动）
3. 网关 9001 可用

## 自动化

```bash
node scripts/phase7d-acceptance-test.mjs
```

## 手工抽查

| 步骤 | 预期 |
|------|------|
| Admin → 首页配置 → 最新内容 | 三列配置可见 |
| 改「最新资料」标题并保存 | 成功 |
| 预览 material 列 | 有真实资源条目 |
| 前台首页刷新 | 三列标题与数据更新；资讯列仍来自 article |

## 验收项对照

| ID | 说明 |
|----|------|
| D1 | SQL 种子三列 |
| D2 | Admin GET 列表 |
| D3 | PUT 改标题 |
| D4 | material preview |
| D5 | C 端 GET latest-columns |
| D6 | 禁用列后 C 端减少 |
