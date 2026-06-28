# Phase 3A：资源契约定稿（执行与验收清单）

## 一、阶段目标

Phase 3A 只做“统一语言”冻结，不做旧链路下线：

1. 冻结统一 DTO、枚举、状态机（`auditStatus` / `publishStatus` 双状态）。
2. 冻结统一 API 契约（前台 `/api/resources/*`，管理 `/api/admin/resources/*`）。
3. 冻结字段字典（含 `canonicalResourceId`、`contentDomain`、`sourceType`、`placementType`）。
4. 冻结旧新兼容映射（旧字段/旧接口 -> 新契约）。

## 二、执行顺序（先做什么 -> 再做什么 -> 最后做什么）

## Step 1（先做）：契约冻结（文档层）

冻结以下文档为 3A 基线：

- `docs/Phase3A-资源主链路统一-接口契约.md`
- `docs/Phase3A-资源主链路统一-字段字典.md`
- `docs/Phase3A-资源主链路统一-枚举与状态机.md`
- `docs/Phase3A-资源主链路统一-兼容映射与执行步骤.md`
- `docs/Phase3A-资源主链路统一-文档目录.md`

并明确：新增字段、路径、状态值必须先更新上述文档再改代码。

## Step 2（再做）：代码面契约对齐检查

对齐并确认以下现状（不要求本阶段彻底迁移）：

- 后端已有统一入口与过渡入口并存：
  - `/api/resources`（写入口）
  - `/api/admin/resources`（管理写）
  - `/api/admin/resource-main`（过渡统一读）
  - 旧入口 `/api/primary-chinese|topic|culture|competition|resources/browse`
- 前端仍存在旧入口调用，符合“兼容期并存”预期，但禁止新增新的旧入口调用。

## Step 3（最后做）：自动化验收与门禁

新增并执行：

- `scripts/phase3a-contract-acceptance.mjs`
  - 检查 3A 文档完整性
  - 检查文档关键术语是否齐全（统一 ID、四个统一字段、双状态）
  - 检查关键控制器/前端 API 文件是否存在
- `scripts/phase3a-acceptance-test.mjs`
  - 串行执行 3A 契约验收
  - 串行执行 Phase3-P0 门禁（`phase3p0-release-gate.mjs`）
  - 输出 3A 结果与最终退出码

## 三、验收标准（通过条件）

1. 3A 五份文档全部存在。
2. 接口契约文档包含：
   - `/api/resources/page`
   - `/api/admin/resources`
3. 字段字典文档包含：
   - `canonicalResourceId`
   - `contentDomain`
   - `sourceType`
   - `placementType`
4. 状态机文档包含：
   - `auditStatus`
   - `publishStatus`
5. 兼容映射文档包含旧新映射表（旧接口 -> 新接口）。
6. 契约验收脚本通过 + P0 门禁通过（`PASS`）。

## 四、测试步骤（可直接执行）

在 `k12-edu-microservice` 目录执行：

```bash
node scripts/phase3a-contract-acceptance.mjs
node scripts/phase3a-acceptance-test.mjs
```

预期：

- `Phase3-A Contract Acceptance: 全通过`
- `Phase3-A Full Acceptance: 全通过`
- 若 P0 门禁失败，3A 全量验收应失败并阻断。

## 五、风险与控制

1. **风险：文档冻结后代码漂移。**  
   **控制：** 契约验收脚本纳入回归，每次发布前执行。

2. **风险：误把兼容并存视为“迁移已完成”。**  
   **控制：** 本阶段验收通过仅代表“语言统一”，不代表“旧入口归零”。

3. **风险：团队把“容差策略”误解为放水。**  
   **控制：** 参考 `docs/Phase3-P0-新旧对比口径约定.md`，仅排序容差，数据一致性不容差。
