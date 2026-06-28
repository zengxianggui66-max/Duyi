# Phase 3B：数据库关系冻结与回填（执行与验收清单）

## 一、阶段目标

Phase 3B 关注“数据主链路”：

1. `resource_main` 结构补齐（统一字段）；
2. `topic/culture/competition/edu_resource` 回填到 `resource_main`；
3. `v_admin_resource_main` 扩展为多来源统一视图；
4. 建立可重复执行、可自动验收的 DB 脚本与检查项。

## 二、执行顺序（先做什么 -> 再做什么 -> 最后做什么）

## Step 1（先做）：结构冻结

执行 `sql/86_phase3b_resource_main_chain.sql`，补齐 `resource_main` 字段：

- `content_domain`
- `canonical_resource_id`
- `legacy_source_table`
- `legacy_source_id`

并保证 `canonical_resource_id` 默认回填为 `id`。

## Step 2（再做）：多来源回填

同一脚本内幂等回填：

- `topic_resource -> resource_main`
- `culture_resource -> resource_main`
- `competition_resource -> resource_main`
- `edu_resource -> resource_main`

要求：可重复执行，不产生重复映射。

## Step 3（最后做）：统一视图冻结

重建 `v_admin_resource_main`，以 `UNION ALL` 输出多来源统一列集，确保管理端统一查询稳定。

## 三、验收标准（通过条件）

1. `resource_main` 包含 4 个新增字段；
2. `resource_main` 覆盖以下 `source_type`：
   - `primary_chinese`
   - `topic_resource`
   - `culture_resource`
   - `competition_resource`
   - `edu_resource`
3. 未映射数为 0（`topic/culture/competition/edu`）；
4. `v_admin_resource_main` 可按 `source_type` 查询到上述来源；
5. 脚本可重复执行（至少连续执行两次）且结果稳定；
6. 继承 P0 发布门禁通过（防止 3B 破坏前台一致性）。

## 四、自动化验收脚本

新增脚本：

- `scripts/phase3b-db-acceptance.mjs`
  - 执行 86 SQL（可配置跳过）
  - 执行结构/覆盖/未映射/视图校验
- `scripts/phase3b-acceptance-test.mjs`
  - 串行执行 `phase3b-db-acceptance.mjs`
  - 串行执行 `phase3p0-release-gate.mjs`
  - 任一失败即阻断

## 五、测试步骤（可直接执行）

在 `k12-edu-microservice` 目录执行：

```bash
node scripts/phase3b-db-acceptance.mjs
node scripts/phase3b-acceptance-test.mjs
```

可选环境变量：

- `MYSQL_BIN`：mysql 可执行路径（默认自动探测）
- `MYSQL_USER`：默认 `root`
- `MYSQL_PASSWORD`：默认 `zxg123456`
- `MYSQL_DB`：默认 `xinketang`
- `PHASE3B_SKIP_SQL=1`：只做校验，不执行 86 SQL

## 六、风险与控制

1. **风险：脚本执行环境找不到 mysql。**  
   **控制：** 支持 `MYSQL_BIN` 明确指定 mysql.exe。

2. **风险：回填后 view 列语义不一致。**  
   **控制：** 验收脚本固定检查 `source_type` 覆盖和未映射清零。

3. **风险：3B 修复破坏前台一致性。**  
   **控制：** 3B 全量验收继承 P0 发布门禁，失败即阻断。
