# Phase 2 Step 2/6 — 上传页数据源改造验收

> 对照 `admin-phase2-detailed-development-plan.md` 第二步验收标准（528–535 行）

## 1. 验收标准 ↔ 方法

| 标准 | 验收方法 | 自动化 ID |
|------|----------|-----------|
| 小学仅一至六年级 | `GET /api/taxonomy/grades?stage=primary` 长度 = 6；名称含「一…六」或 code 为 `grade1`…`grade6` | T1, T1b |
| 初中七至九年级 | `stage=junior` 长度 = 3；名称含七/八/九 | T2, T2b |
| 高中高一至高三 | `stage=senior` 长度 = 3；名称含高一/高二/高三 | T3, T3b |
| 选学段后学科/年级/栏目刷新 | 切换 `stage` 参数，学科/年级/栏目列表与小学不同 | T9–T11 |
| 版本册别后目录树加载 | `gradeName` + `edition` + `subject` 调 `/api/catalog/tree` 有节点 | T12 |
| 换 edition 树变化或仍合法 | 两版本参数对比树签名 | T13 |
| 禁用分类不出现 | 管理端禁用学科 → C 端列表无；草稿提交 400 | T14–T16 |
| 上传元数据校验 | 跨学段册别 400；合法落位非 400 | T5–T6, T19 |
| 草稿持久化 | 合法 payload 返回 200（或 SKIP：缺 audit_status 列） | T19b |
| 权限 | 普通用户不可调管理端 taxonomy | T17 |
| 素质拓展年级 | `stage=art` 不含 K12 七年级/高一 | T20 |

## 2. 自动化脚本

```bash
cd k12-edu-microservice
node scripts/phase2-step2-upload-taxonomy-acceptance.mjs
```

环境变量（可选）：

- `PHASE9_GATEWAY` / `PHASE8_GATEWAY` — 默认 `http://localhost:9001`

**前置**：Gateway 9001、k12-resource 8082 已启动；账号 `admin/admin123`、`teacher_demo/teacher123`（或 `normal_user/admin123`）。

**数据库迁移约定**：

| 脚本 | 说明 |
|------|------|
| `sql/80_admin_resource_status_split.sql` | 首次 ADD 列（**本环境已执行，勿改、勿重复跑**） |
| `sql/82_phase2_resource_status_idempotent.sql` | 幂等补全/回填/VIEW；缺列或新环境补救 |
| `scripts/run-sql82-resource-status.cmd` | Windows 推荐入口（使用 MySQL 8.0 真实客户端） |

**预期**：T1–T20 全 PASS（T19b 在缺 `audit_status` 列时 SKIP；部分 SKIP 表示种子缺失）。

### 2.1 与 Phase 5 脚本关系

| 脚本 | 范围 |
|------|------|
| `phase2-step2-upload-taxonomy-acceptance.mjs` | 上传页 taxonomy + 落位校验 + 禁用项 |
| `phase5-acceptance-test.mjs` | 管理端 taxonomy CRUD + catalog 全链路 |

建议发版前两者均跑一遍。

### 2.2 可选：Playwright DOM 验收

当前仓库未集成 Playwright。若需 UI 层断言（上传页年级下拉 option 数量 = 6），可后续增加：

```text
tests/e2e/upload-taxonomy.spec.ts
  - 打开 /upload → 选小学 → expect grade select options = 6
  - 切换初中 → expect options = 3
```

阶段 6 以 **API 脚本 + 手工 Smoke** 为主。

## 3. 手工 Smoke（F 系列）

### F-U1：小学上传全流程

1. 使用 `teacher_demo` 登录前台。
2. 从学科页（小学语文）进入上传，或直达 `/upload?stage=primary&subject=chinese&module=同步备课`。
3. 确认学段/学科已锁定；年级下拉 **6 项**；placement 册别来自 API（无七八九年级册别）。
4. 选册别、版本、单元 → 保存草稿。
5. 个人中心「我的资源 → 草稿箱」：元数据（学段、学科、册别、版本、目录）与表单一致。

### F-U2：管理端改学科名 → 上传页一致

1. 管理端 `/admin/taxonomy` 修改小学「语文」显示名（如加「（测）」后缀）。
2. 上传页刷新（或等待 taxonomy 缓存 5min 过期 / 管理端保存触发 invalidate）。
3. 学科下拉显示新名称；`subject` code 仍为 `chinese`。

### F-U3：禁用版本 → 上传页不可选

1. 管理端禁用某教材版本（如统编版）。
2. C 端 `GET /api/taxonomy/editions?stage=primary&subject=chinese` 不含该项。
3. 上传页版本下拉无该项；若手工构造请求带该版本，草稿/提交返回 **400**。

### F-U4：离线兜底（阶段 4–5）

1. 停止 k12-resource 或断网模拟 API 失败。
2. 上传页出现「使用离线默认选项」banner；**提交审核**禁用，**保存草稿**仍可用。
3. 恢复服务后刷新，banner 消失，选项恢复 API 数据。

## 4. 签字

| 项 | 日期 | 结果 |
|----|------|------|
| 自动化 T1–T20 | | |
| F-U1 全流程 | | |
| F-U2 名称同步 | | |
| F-U3 禁用版本 | | |
| F-U4 离线兜底 | | |
| `npm run build` | | |
