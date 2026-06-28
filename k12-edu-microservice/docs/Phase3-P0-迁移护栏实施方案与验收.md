# Phase 3-P0：迁移护栏实施方案与验收

## 目标

在资源主链路改造前，先完成“四件事”：

1. 可观测：资源来源治理台账可查可更新；
2. 可灰度：统一读取开关可按频道控制；
3. 可回滚：新旧接口结果可对比，异常可阻断切流；
4. 可发布：发布门禁脚本可自动拦截不达标版本。

> 本阶段不新增业务功能，仅建设迁移护栏。

## 先做什么（P0-1 基础护栏）

### 1) 资源来源治理台账

- 新增表：`resource_migration_ledger`
- 核心字段：
  - `source_type`
  - `legacy_source_table`
  - `target_main_chain`
  - `backfill_status`
  - `frontend_switch_status`
  - `legacy_api_offline_status`
  - `compare_status`
  - `compare_pass_rate`
- 初始化种子覆盖：`primary_chinese`、`topic_resource`、`culture_resource`、`competition_resource`、`article_attachment`

### 2) 管理端台账 API

- `GET /api/admin/resource-main/migration-ledger`
- `PUT /api/admin/resource-main/migration-ledger/{sourceType}`

用于迁移过程持续登记和状态同步（含自动回写比对结果）。

## 再做什么（P0-2 灰度与比对）

### 3) 灰度切换开关（复用 `sys_config`）

新增 runtime 开关：

- `resourceUnifiedReadEnabled`
- `topicUnifiedReadEnabled`
- `cultureUnifiedReadEnabled`
- `competitionUnifiedReadEnabled`
- `primaryChineseUnifiedReadEnabled`

已接入现有：

- `/api/admin/system/feature-flags`
- `/api/public/feature-flags`
- `PUT /api/admin/system/config?group=feature`

### 4) 新旧接口结果比对机制

新增脚本：`scripts/phase3p0-compare-resource-apis.mjs`

按来源对比（旧接口 vs `/api/admin/resource-main`）：

- 总数 `total`
- 资源 ID 集合（旧 `id` vs 新 `sourceId`）
- 类型统计（按 `type/resourceType/category/resourceForm` 归一）
- 排序一致性（前 10 条）
- 空结果异常（单边为空）

并将结果回写台账：

- `compareStatus`
- `comparePassRate`
- `notes`

## 最后做什么（P0-3 发布门禁）

### 5) 定义并执行发布门禁

新增脚本：`scripts/phase3p0-release-gate.mjs`

默认门禁规则：

- 总通过率 `>= 95%`
- 不允许空结果异常（可通过 `P0_ALLOW_EMPTY_ANOMALY=true` 临时放开）

默认门禁不通过时直接退出码 `1`，阻断切流。

## 验收标准

1. `resource_migration_ledger` 表存在且有 5 条来源种子。
2. `GET /api/admin/resource-main/migration-ledger` 可返回台账列表。
3. `PUT /api/admin/resource-main/migration-ledger/{sourceType}` 可更新状态字段。
4. 管理端功能开关页可看到并切换 5 个统一读取开关。
5. `phase3p0-compare-resource-apis.mjs` 能输出各来源比对结果。
6. 比对脚本执行后，台账中 `compare_status/compare_pass_rate/last_compared_at` 可变化。
7. `phase3p0-release-gate.mjs` 在阈值不达标时返回非零退出码。

## 测试步骤

### A. SQL 初始化

1. 执行：`sql/87_phase3p0_guardrails.sql`
2. 验证：
   - `SELECT * FROM resource_migration_ledger;`
   - `SELECT config_key, config_value FROM sys_config WHERE group_code='feature' AND config_key LIKE 'feature.%UnifiedRead.%';`

### B. API 验证

1. 登录 admin 获取 token；
2. 调用 `GET /api/admin/resource-main/migration-ledger`；
3. 调用 `PUT /api/admin/resource-main/migration-ledger/topic_resource`，示例 body：

```json
{
  "backfillStatus": 1,
  "frontendSwitchStatus": 1,
  "legacyApiOfflineStatus": 0,
  "compareStatus": 1,
  "comparePassRate": 98,
  "notes": "topic 已进入灰度"
}
```

### C. 比对与门禁

1. 执行：`node scripts/phase3p0-compare-resource-apis.mjs`
2. 执行：`node scripts/phase3p0-release-gate.mjs`
3. 覆盖异常场景：
   - 设置 `P0_COMPARE_PASS_THRESHOLD=100`，验证门禁阻断；
   - 设置 `P0_ALLOW_EMPTY_ANOMALY=true`，验证可临时放行。
