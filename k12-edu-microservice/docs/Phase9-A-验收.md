# Phase 9-A 验收：搜索运营看板

## 范围

- 管理端 `/admin/search`（搜索概览 + 索引状态）
- 后端 `/api/admin/search/*`（从 `/api/search/admin/*` 迁移）
- 权限 `admin:search:view` / `admin:search:reindex`
- SQL：`sql/75_phase9a_search_ops.sql`

## 部署

```bash
mysql -u root -p xinketang --default-character-set=utf8mb4 < sql/75_phase9a_search_ops.sql
mvn install -pl k12-common,k12-resource,k12-gateway -am -DskipTests
# 重启 k12-resource (8082) 与 k12-gateway (9001)
```

## 自动化验收

```bash
node scripts/phase9a-acceptance-test.mjs
```

| ID | 用例 | 预期 |
|----|------|------|
| A1 | GET `/api/admin/search/stats?days=7` | 200，`totalQueries` 为数字 |
| A2 | GET `stats?days=30` | 200 |
| A3 | GET `/api/admin/search/p3-readiness` | 200 |
| A4 | GET `/api/admin/search/engine/health` | 200 |
| A5 | auditor 访问 stats | 403 |
| A6 | 旧路径 `/api/search/admin/stats`（含匿名） | HTTP 404，不可再返回有效 stats |
| A6b | 同上（无 Token） | HTTP 404 |
| A7 | POST reindex | 200 且 `sys_operation_log` module=search |

## 手工验收

1. 使用 `admin` 登录管理端，侧栏出现「搜索运营」
2. 搜索概览：切换 7/30 天，Top 词表格可导出 CSV
3. 索引状态：可见 OpenSearch 健康与 P3 评估；可触发重建（content_admin+）
4. `auditor` 账号无「搜索运营」菜单

## 角色权限

| 角色 | view | reindex |
|------|------|---------|
| super_admin | ✅ | ✅ |
| content_admin | ✅ | ✅ |
| operator | ✅ | ❌ |
| system_admin | ✅ | ❌ |
| auditor | ❌ | ❌ |
