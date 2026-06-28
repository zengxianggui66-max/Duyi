# Phase 9-B 验收：搜索词典与重定向

## 范围

- 表 `sys_search_redirect`（新建）+ 复用 `sys_search_synonym`
- 管理端：同义词 CRUD、重定向 CRUD、意图规则只读
- 运行时：`SearchRedirectRuleService` + `SearchRedirectResolver` 优先级
- 无结果 Top 词 →「同义词草稿」闭环
- SQL：`sql/76_phase9b_search_lexicon.sql`

## 部署

```bash
mysql -u root -p xinketang --default-character-set=utf8mb4 < sql/76_phase9b_search_lexicon.sql
mvn install -pl k12-common,k12-resource -am -DskipTests
# 重启 k12-resource (8082)
```

## 重定向优先级

1. 泛学科词拦截（语文/数学等 → 不直达）
2. **DB 运营规则** `sys_search_redirect`（reason=`db_redirect_rule`）
3. 硬编码 `EXACT_ROUTES`
4. 目录精确匹配
5. 资源标题精确匹配

## 自动化验收

```bash
node scripts/phase9a-acceptance-test.mjs
node scripts/phase9b-acceptance-test.mjs
```

| ID | 用例 | 预期 |
|----|------|------|
| B1 | GET `/api/admin/search/synonyms` | 200 |
| B2 | POST 创建同义词 | 200，保存后 `SearchLexiconService.refreshFromDb()` |
| B3 | POST `/synonyms/draft?keyword=` | 200，status=0 草稿 |
| B4 | POST 重定向 keyword=语文 | 400 |
| B5 | 创建 DB 重定向 → GET `/api/search/redirect` | directHit=true, reason=db_redirect_rule |
| B6 | 种子「传统文化」重定向 | directHit=true |
| B7 | GET `/api/admin/search/intent-rules` | 200 只读列表 |
| B8 | auditor POST 同义词 | 403 |
| B9 | 创建同义词写 operation_log | module=search |

## 手工闭环

1. 搜索概览 → Top 无结果词 → 点击「同义词草稿」
2. 在同义词页补全同义词并启用
3. （可选）在重定向页为频道词配置 `/culture` 等路由
4. 次日查看无结果率是否下降

## 权限

| 操作 | 权限 |
|------|------|
| 查看词典/重定向/意图 | `admin:search:view` |
| 编辑同义词/重定向 | `admin:search:edit` |

content_admin / super_admin 拥有 edit；operator 仅 view。
