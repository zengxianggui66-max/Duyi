# Phase3-P0：新旧对比口径约定（严格 vs 容差）

## 目的

统一说明 `scripts/phase3p0-compare-resource-apis.mjs` 的比对策略，避免把“工程容差”误解为“放水”。

本约定只用于 **迁移过程的可发布门禁**，不改变业务真实数据，不绕过异常拦截。

## 一、统一前提（所有来源都必须满足）

无论采用严格还是容差，以下约束始终生效：

1. `total` 必须一致；
2. `idSet` 必须一致（按全量分页收集，不只看第一页）；
3. `typeStats` 必须一致（按来源字段映射后比较）；
4. `emptyAnomaly` 必须为 `false`（禁止单边空结果）；
5. 总体门禁仍要求 `passRate >= 95%`，否则 `BLOCK`。

> 结论：容差仅作用于“排序判定”，不作用于“总数、集合、类型、空结果”。

## 二、为何 `primary_chinese` / `culture_resource` 使用容差

### 1) `primary_chinese`（`sortPolicy=tolerant`）

原因：旧链路在 `upload_time` 同值时，缺少稳定二级排序；同一口径下可能出现“第一页顺序抖动”，导致 `sort` 偶发失败，但数据集本身一致。

风险控制：

- 仍强制 `total/idSet/typeStats/emptyAnomaly` 全通过；
- `idSet` 已升级为全量分页比对，避免仅第一页误判。

### 2) `culture_resource`（`sortPolicy=tolerant`）

原因：旧接口默认排序权重含 `isElite + sort`，新统一视图排序字段模型不完全等价；迁移期允许排序轻微差异，但不允许数据缺失或类型错配。

风险控制：

- `total/idSet/typeStats/emptyAnomaly` 仍是硬门槛；
- 只放宽 `sort`，不放宽任何数据一致性指标。

## 三、为何 `topic_resource` / `competition_resource` 使用严格

这两类来源在旧新链路上排序语义可对齐（默认 `sort desc`），迁移后应达到完全一致。

因此：

- `sortPolicy=strict`；
- 五项检查必须全部通过（`5/5`）。

## 四、字段映射约定（避免“类型统计”假失败）

为保证 `typeStats` 比较的是同一业务维度：

- `topic_resource`、`competition_resource`：旧侧 `resourceForm` 对新侧 `type`；
- `culture_resource`：旧侧 `category` 对新侧 `module`；
- `primary_chinese`：按 `type/resourceType` 常规映射。

## 五、团队执行原则

1. 容差策略是“迁移期稳定性保护”，不是长期状态；
2. 任何来源若出现 `emptyAnomaly=true`，一票否决，立即 `BLOCK`；
3. 当统一读 API（`/api/resources/page`）完全替代旧接口后，应逐步回收容差，恢复全严格；
4. 门禁记录以 `resource_migration_ledger.compareStatus/comparePassRate` 为准，保证可追溯。

## 六、判断一句话

**容差 != 放水。**  
容差只允许“排序展示差异”，不允许“数据内容差异”。
