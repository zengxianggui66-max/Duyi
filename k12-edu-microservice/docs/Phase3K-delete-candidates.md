# Phase 3K — 待废弃表 DELETE 候选

> 审计日期：2026-06-28  
> 数据库：`xinketang`（root 实查 `COUNT(*)`）  
> 规则：`row_count = 0` 且 grep 仅 SQL/docs/注释 → 本清单；下版本评估 DROP。

---

## 1. 四维审计总表（Step 2）

| 表 | Java grep | 前端 grep | row_count | Verdict |
|:---|:----------|:----------|----------:|:--------|
| `resource_category` | 2 文件（Entity + Mapper；Mapper 含 `@Select` 可执行 SQL；**无 Service 注入**） | 0 | **0** | **DEPRECATE** |
| `edu_resource_tag` | 0（无 Entity/Mapper） | 0（`src/` 无；docs 4 处） | **0** | **DELETE_CANDIDATE** |
| `edu_resource_region` | 0 代码；2 处 **Javadoc 注释**（`SinologyService`、`SinologyUnitBundleVO`；实现已走 `edu_resource_dimension`） | 0 | **0** | **DELETE_CANDIDATE** |
| `edu_resource_search_flat` | 0 | 0（docs 2 处） | **0** | **DELETE_CANDIDATE** |

### Java 引用明细

**`resource_category`**

| 文件 | 类型 |
|------|------|
| `k12-common/.../entity/ResourceCategory.java` | `@Deprecated` Entity |
| `k12-common/.../mapper/ResourceCategoryMapper.java` | `@Deprecated` Mapper（2 条 `@Select`） |

**`edu_resource_region`（仅注释，非可执行引用）**

| 文件 | 行 |
|------|-----|
| `k12-resource/.../SinologyService.java` | 156 — Javadoc |
| `k12-common/.../SinologyUnitBundleVO.java` | 33 — 字段注释 |

### 前端 / 文档引用

| 表 | `k12-edu-platform/src` | 文档 |
|----|------------------------|------|
| 全部 4 表 | **0** | `edu_resource_tag` / `region` / `search_flat` 见于 `docs/数据库设计方案.md` 等；`resource_category` 无 |

### SQL 引用

| 表 | 建表 / 迁移 |
|----|-------------|
| `resource_category` | `sql/08_legacy_business.sql` |
| `edu_resource_tag` | `sql/05_resource.sql` |
| `edu_resource_region` | `sql/05_resource.sql`, `sql/03_dimension_taxonomy.sql` |
| `edu_resource_search_flat` | `sql/05_resource.sql` |

---

## 2. DELETE 候选（下版本 DROP）

以下条件均已满足：`row_count = 0`、无 Entity、无 Service、无前端 `src` 引用。

| 表 | 替代方案 | DROP 前动作 |
|----|----------|-------------|
| `edu_resource_tag` | 标签走 `edu_resource.tags` 字段或 `edu_browse_tag` 维度 | 从 `05_resource.sql` 移除 CREATE；新增 `98_phase3k_drop_deprecated.sql`（可选） |
| `edu_resource_region` | `edu_resource_dimension` + `edu_region` | 清理 `SinologyService` / `SinologyUnitBundleVO` 过时 Javadoc |
| `edu_resource_search_flat` | `sys_search_document` + `SearchDocumentSyncService` | 同步更新 `k12-edu-platform/docs/database-design.md` |

### 建议 DROP 脚本草案（**勿在本版本执行**）

```sql
-- 98_phase3k_drop_deprecated_candidates.sql（下版本）
-- USE xinketang;
-- DROP TABLE IF EXISTS edu_resource_tag;
-- DROP TABLE IF EXISTS edu_resource_region;
-- DROP TABLE IF EXISTS edu_resource_search_flat;
```

---

## 3. DEPRECATE 保留（需先删 Java 再 DROP）

### `resource_category`

| 项 | 状态 |
|----|------|
| row_count | 0 |
| 运行时引用 | 无 Service/Controller 注入 |
| 代码残留 | `@Deprecated` Entity + Mapper（含 live `@Select`） |

**迁移计划（3K+1）**

1. 确认 Admin/前台无任何 `ResourceCategoryMapper` 注入（当前 grep 通过）。
2. 删除 `ResourceCategory.java`、`ResourceCategoryMapper.java`。
3. 将本表移入 §2 DELETE 候选，执行 DROP。
4. 从 `sql/08_legacy_business.sql` 移除 CREATE（或标记 historical）。

---

## 4. 复核命令

```bash
cd k12-edu-microservice

# 行数
mysql -u root -pzxg123456 xinketang -e "
  SELECT 'resource_category' t, COUNT(*) FROM resource_category
  UNION ALL SELECT 'edu_resource_tag', COUNT(*) FROM edu_resource_tag
  UNION ALL SELECT 'edu_resource_region', COUNT(*) FROM edu_resource_region
  UNION ALL SELECT 'edu_resource_search_flat', COUNT(*) FROM edu_resource_search_flat;"

# Java（在 k12-edu-microservice 根目录）
rg "resource_category|edu_resource_tag|edu_resource_region|edu_resource_search_flat" --glob "*.java"

# 前端 src
rg "resource_category|edu_resource_tag|edu_resource_region|edu_resource_search_flat" k12-edu-platform/src
```

DROP 前须再跑：`node sql/tools/inventory_all_tables.mjs` + `node scripts/phase3k-db-acceptance.mjs`。
