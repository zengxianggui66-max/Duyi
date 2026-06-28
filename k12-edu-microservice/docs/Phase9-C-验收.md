# Phase 9-C 验收：搜索热词管理

## 范围

- 表 `search_hot_keyword` 增加 `boost_score`（`sql/77_phase9c_search_hot_keyword.sql`）
- 管理端 `/admin/search/hot-keywords` 列表与干预
- C 端 `/api/search/hot-keywords` 排序：`search_count + boost_score`
- **不**自动写入 `home_hot_word`；提供「复制到运营热词」

## 部署

```bash
mysql -u root -p xinketang < sql/77_phase9c_search_hot_keyword.sql
mvn install -pl k12-common,k12-resource -am -DskipTests
# 重启 k12-resource (8082)
```

## API

| 方法 | 路径 | 权限 |
|------|------|------|
| GET | `/api/admin/search/hot-keywords` | `admin:search:view` |
| PUT | `/hot-keywords/{id}/status?status=` | `admin:search:edit` |
| PUT | `/hot-keywords/{id}/boost?boostScore=` | `admin:search:edit` |

## 自动化验收

```bash
node scripts/phase9c-acceptance-test.mjs
```

| ID | 用例 | 预期 |
|----|------|------|
| C1 | GET admin hot-keywords | 200 |
| C2 | PUT boost | boostScore 回写 |
| C3 | 禁用后 GET `/api/search/hot-keywords` | 不含该词 |
| C4 | 公共 hot-keywords | 200 |
| C5 | auditor PUT status | 403 |
| C6 | boost 写 operation_log | module=search |
| C7 | 列表 rank 连续 1..n | true |

## 手工验收

1. 搜索运营 → 搜索热词：可见排名、次数、加权
2. 加权后 C 端 suggest/热搜排序变化（有效热度 = 次数 + 加权）
3. 「预览搜索」新开 `/search/result?q=...`
4. 「复制到运营热词」→ 首页配置出现禁用态条目（需 `admin:home:edit`）

## 与闭环关系

```
C端搜索 → search_query_log / search_hot_keyword
       → 管理端无结果词治理（9-B 同义词/重定向）
       → 搜索热词加权/禁用（9-C）
       → 补资源审核上架 + 索引重建（9-A）
       → 无结果率下降
```
