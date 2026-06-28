# Phase 3C：数据回填质量看板 + 别名表（执行与验收清单）

## 一、阶段目标

Phase 3C 聚焦“回填质量可观测 + 目录语义兼容”：

1. 新增目录别名表，解决旧资源标题与新目录节点不一致问题；
2. 新增资源主链回填质量看板指标，持续发现脏数据；
3. 提供可重复执行的验收脚本，保证上线前可核验。

## 二、执行内容

## 1) 数据库对象

执行 `sql/88_phase3c_alias_quality.sql`：

- 新建 `resource_catalog_alias`
- 新建 `v_resource_migration_quality`

质量看板指标覆盖：

- 未挂载资源
- 空文件资源
- 已审核未发布
- 已发布但不可见
- 有目录无资源节点
- 有资源无目录孤儿数据

## 2) 管理端 API

- 目录别名：
  - `GET /api/admin/resource-main/catalog-aliases`
  - `POST /api/admin/resource-main/catalog-aliases`
- 质量看板：
  - `GET /api/admin/quality/analytics/migration-dashboard`

## 3) 自动化验收

- `scripts/phase3c-acceptance-test.mjs`
  - 执行 88 SQL（可跳过）
  - 验证表/视图存在
  - 验证别名 API 可写可读
  - 验证质量看板 API 返回完整指标
  - 继承 P0 发布门禁

## 三、验收标准

1. `resource_catalog_alias` 表存在；
2. `v_resource_migration_quality` 至少返回 6 条指标；
3. 别名 upsert/list API 正常；
4. 质量看板 API 指标字段齐全；
5. P0 门禁仍为 PASS（3C 变更不破坏主链稳定性）。

## 四、测试步骤

```bash
cd k12-edu-microservice
node scripts/phase3c-acceptance-test.mjs
```

可选环境变量：

- `PHASE3C_SKIP_SQL=1`：只验 API 与门禁
- `MYSQL_BIN` / `MYSQL_USER` / `MYSQL_PASSWORD` / `MYSQL_DB`

## 五、风险与控制

1. **风险：别名误配导致目录挂载错误。**  
   **控制：** 通过 `confidence` 与 `status` 管理，并要求人工复核。

2. **风险：质量指标被误解为业务报警。**  
   **控制：** 3C 指标为治理指标，不直接改变前台可见逻辑。

3. **风险：3C SQL 重复执行污染数据。**  
   **控制：** 采用幂等建表/视图 + 唯一键约束（`source_type + legacy_title`）。
