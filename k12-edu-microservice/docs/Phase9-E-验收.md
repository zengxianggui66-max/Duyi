# Phase 9-E 验收：权限与角色

## 范围

- 权限码：`admin:search:view|edit|reindex`（75/76 已有）+ 新增 `admin:analytics:view`
- 菜单：搜索运营 / 数据分析 父子结构
- 角色映射：operator / content_admin / system_admin / super_admin / finance_admin（废弃标记）
- 旧「控制台」侧栏隐藏，路由 `/admin/dashboard` 重定向至 `/admin/analytics/overview`

## 部署

```bash
mysql -u root -p xinketang < sql/78_phase9e_search_analytics_rbac.sql
mvn install -pl k12-common,k12-auth,k12-resource,k12-gateway -am -DskipTests
# 重启 gateway、auth、resource；刷新前端
```

## 菜单结构

```
搜索运营 (/admin/search)
  ├─ 搜索概览
  ├─ 无结果词
  ├─ 同义词库
  ├─ 搜索重定向
  ├─ 搜索热词
  └─ 索引状态
  （意图规则：hidden=1，仅路由保留）

数据分析 (/admin/analytics)
  ├─ 运营概览（原 Dashboard）
  ├─ 资源分析
  └─ 用户与行为（简版）
```

## 角色映射

| 角色 | 搜索 | 分析 | 说明 |
|------|------|------|------|
| operator | view | view | 只读 |
| content_admin | view + edit + reindex | view | 词典编辑、索引重建 |
| system_admin | view | view | 全量系统配置侧 |
| super_admin | 全部 | 全部 | 自动补漏 |
| finance_admin | 无 | 无 | 角色名标记废弃 |

## 自动化验收

```bash
node scripts/phase9e-acceptance-test.mjs
# 串联 9-A～9-E
node scripts/phase9-acceptance-test.mjs
```

| ID | 用例 | 预期 |
|----|------|------|
| E1 | admin permissions | 含 analytics + search 全套 |
| E2 | admin menus | 搜索/数据分析子菜单齐全 |
| E3 | operator | view + analytics，无 edit/reindex |
| E4 | content_admin | 含 edit + reindex |
| E5 | operator analytics API | 200 |
| E6 | operator reindex | 403 |
| E7 | content_admin users API | 200 |
| E8 | auditor analytics | 403 |

## 手工验收

1. operator 登录：见「搜索运营」「数据分析」，无重建索引按钮
2. content_admin：同义词/重定向可编辑，索引页可重建
3. admin 侧栏无旧「控制台」，运营概览在数据分析下
4. `/admin/search/no-results` 独立页可用
