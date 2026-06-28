# Phase 9 验收修复说明

## SQL

```bash
mysql -u root -p xinketang < sql/79_phase9_post_acceptance_fix.sql
```

- 从 **auditor** 移除 `admin:dashboard:view` / `admin:analytics:view`
- 修正菜单 91 组件路径为 `Dashboard.vue`

## 网关

`k12-search-admin-gone`：`/api/search/admin/**` → **404**（须重启 gateway 9001）

## Resource 兜底

`DeprecatedSearchAdminController`：直连 8082 时也返回 404

## 验收

```bash
mvn install -pl k12-common,k12-resource,k12-gateway -am -DskipTests
# 重启 gateway、auth、resource
node scripts/phase9-acceptance-test.mjs
```

公共工具：`scripts/phase9-test-utils.mjs`
