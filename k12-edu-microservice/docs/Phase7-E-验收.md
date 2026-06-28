# Phase 7-E 验收

## 前置

1. 执行 `sql/71_phase7e_ops_channel_schema.sql`
2. 执行 `sql/71_phase7e_ops_channel.sql`
3. 重启 `k12-resource` 与 `k12-gateway`（新增 `/api/channel/**` 路由）
4. `mvn install -pl k12-common,k12-resource -am`

## 自动化

```bash
node scripts/phase7e-acceptance-test.mjs
```

## 手工抽查

| 步骤 | 预期 |
|------|------|
| Admin → 首页配置 → 升学入口 | 5 条可编辑 |
| Admin → 首页配置 → 特色频道 | banhui/jingsai 等 5 频道 |
| 改传统文化频道 Tab 名 | `/feature/chuantong` 页 Tab 更新 |
| 顶栏升学入口 | 仍走 func-channels API |
