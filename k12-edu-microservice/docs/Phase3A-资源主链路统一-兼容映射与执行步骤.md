# Phase 3A：资源主链路统一 - 兼容映射与执行步骤（评审稿）

> 关联文档：  
> - `Phase3A-资源主链路统一-接口契约.md`  
> - `Phase3A-资源主链路统一-字段字典.md`  
> - `Phase3A-资源主链路统一-枚举与状态机.md`

---

## 1. 现状差距清单（基于当前仓库）

## 1.1 数据层差距

- `resource_main` 仅有 `primary_chinese` 数据，topic/culture/competition/edu_resource 未映射。
- `v_admin_resource_main` 当前只 join `oss_primary_chinese_resource`，未 union 其他来源。
- `edu_resource` 与宽表存在双写链路，尚未形成“单主链读”。

## 1.2 接口层差距

- 管理端读在 `/api/admin/resource-main`，写在 `/api/admin/resources`。
- 前台仍按频道分别调用 `/topic`、`/culture`、`/competition`、`/resources/browse`、`/primary-chinese`。
- 资源行为接口仍是 `/api/resource/view|download|collect`，未按统一 `canonicalResourceId` 路由。

---

## 2. 旧新接口映射表（兼容期）

| 旧接口 | 新接口（目标） | 兼容策略 |
|---|---|---|
| `/api/primary-chinese/page` | `/api/resources/page` | 旧接口内转发并适配字段 |
| `/api/resources/browse` | `/api/resources/page` | 保留 catalog 参数兼容，内部统一查询 |
| `/api/topic/resources/page` | `/api/resources/page?contentDomain=topic` | 旧接口内转发 |
| `/api/culture/resources/page` | `/api/resources/page?contentDomain=culture` | 旧接口内转发 |
| `/api/competition/resources/page` | `/api/resources/page?contentDomain=competition` | 旧接口内转发 |
| `/api/resource/view` | `/api/resources/{id}/view` | 行为层增加 ID 解析 |
| `/api/resource/download` | `/api/resources/{id}/download` | 行为层增加 ID 解析 |
| `/api/resource/collect` | `/api/resources/{id}/collect` | 行为层增加 ID 解析 |

---

## 3. 字段兼容映射表（旧字段 -> 新字段）

| 旧字段 | 新字段 | 说明 |
|---|---|---|
| `id`（源表） | `canonicalResourceId` | 统一对外主键 |
| `source_type` | `sourceType` | 命名统一 camelCase |
| `status` | `legacyStatus` | 仅兼容 |
| `type` | `resourceType` | 语义统一 |
| `grade_name` | `gradeName` | 命名统一 |
| `catalog_node_id` | `catalogNodeId` | 命名统一 |
| `oss_url` | `ossUrl` | 命名统一 |
| `file_ext` | `fileExt` | 命名统一 |

---

## 4. Phase 3A 详细执行步骤（落地顺序）

## 4.1 Step A1：契约冻结（先做）

- 产出并评审 4 份文档（本批文档）。
- 锁定统一 DTO、枚举、状态机、兼容映射。
- 形成“新增字段必须先入字典”的流程约束。

**产出物**：
- 接口契约文档
- 字段字典
- 枚举状态机文档
- 兼容映射与执行步骤文档

## 4.2 Step A2：网关与路径对齐（再做）

- 统一前台读入口：`/api/resources/*`
- 统一管理写入口：`/api/admin/resources/*`
- `/api/admin/resource-main` 标记为过渡读入口（或合并至新入口）

**检查点**：
- 新代码禁止新增 `/api/topic|culture|competition` 直连。
- 新代码禁止新增 `/api/resource/view|download|collect` 直连。

## 4.3 Step A3：统一 DTO 与组装器（最后做）

- 后端新增 `ResourceMainQueryDTO/VO/DetailVO` 的前台版本契约 DTO。
- 建立 `ResourceQueryAssembler`（参数标准化）和 `ResourceVisibilityPolicy`（可见性规则）。
- 管理端、前台、搜索、行为接口共享同一套 ID 解析与状态判定。

**检查点**：
- page/detail/stats/types 使用同一份查询构造器。
- 行为接口全部接收 `canonicalResourceId`。

---

## 5. 质量门禁（3A 必须通过）

- **契约门禁**：接口变更必须更新文档与字段字典。
- **编译门禁**：后端 DTO 与前端 TS 类型同步。
- **回归门禁**：旧接口兼容用例 + 新接口主链路用例均通过。
- **一致性门禁**：`page.total == stats.total == types.count汇总`（同条件）。

---

## 6. 建议的验收脚本分组

## 6.1 合约验收（Contract）

- C-A1：`/api/resources/page` 参数合法性。
- C-A2：`/api/resources/detail/{id}` 字段完整性。
- C-A3：`/api/resources/stats` 与 page 总数一致性。
- C-A4：`/api/resources/types` 与列表类型分布一致性。

## 6.2 兼容验收（Compatibility）

- C-B1：旧接口返回 200 且数据结构不破坏。
- C-B2：旧接口结果与新接口同条件抽样一致。
- C-B3：旧接口日志命中兼容标签（便于 3G 下线统计）。

## 6.3 行为验收（Behavior）

- C-C1：view/download/collect 全链路使用 `canonicalResourceId`。
- C-C2：行为计数回写与详情统计一致。

---

## 7. 风险与回滚

## 7.1 风险

- 统一入口切换后，专区页面可能出现参数语义差异（如 `resourceForm`、`category`）。
- 旧接口兼容适配不全会导致历史页面白屏或空列表。

## 7.2 回滚策略

- 保留旧接口原 controller，但把新逻辑 behind feature flag。
- 任何线上异常可立即切回旧查询实现，保留统一接口路径不变。
- 统一日志中增加 `route_version` 字段，支持按版本回放排查。

---

## 8. 评审结论模板（可直接复用）

- [ ] 统一接口路径与命名通过
- [ ] 字段字典通过（新增字段已收敛）
- [ ] 双状态状态机通过
- [ ] 兼容映射策略通过
- [ ] 执行顺序与门禁通过
- [ ] 进入 Phase 3B（数据库梳理）条件满足

