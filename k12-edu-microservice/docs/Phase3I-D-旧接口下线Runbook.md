# Phase 3I-D：旧读 API 下线 Runbook

## 前置条件（全部满足后再开 D1 开关）

1. **3I-B** 生产灰度全开（`feature.resourceUnifiedRead.enabled` + 各频道开关）
2. **3I-A** 监控路径 **连续 7 天 hitCount = 0**：
   - `/api/primary-chinese/page`
   - `/api/resources/browse`（及 `/stats`、`/suites`、`/module-stats`）
   - `/api/topic/resources/page`
   - `/api/culture/resources/page`
   - `/api/competition/resources/page`
3. `phase3g-no-new-legacy-calls-gate.mjs` 持续 PASS

```bash
node scripts/phase3g-legacy-usage-trend.mjs
node scripts/phase3g-no-new-legacy-calls-gate.mjs
```

---

## D1 — 410 兼容期（已实现，默认关）

### 部署

```bash
mysql ... < sql/94_phase3i_d_legacy_read_410.sql
# 重启 k12-resource + k12-auth（feature-flags 元数据）
```

### 开启开关

管理端：**系统配置 → 功能开关 → 旧读 API 410 下线**  
或 SQL：`UPDATE sys_config SET config_value='true' WHERE config_key='feature.legacyReadApi410.enabled';`

### 行为

| 类型 | 处理 |
|------|------|
| legacy **读** GET（page/list/browse/detail 等） | **410 Gone** + `Location: /api/resources/*` |
| upload / submit / view·download 计数 POST | **不受影响** |
| filter-options / mine / draft 等 | **白名单，仍 200** |

### 路径映射示例

| 旧路径 | Location |
|--------|----------|
| `/api/primary-chinese/page` | `/api/resources/page?sourceType=primary_chinese` |
| `/api/resources/browse/suites` | `/api/resources/suites?sourceType=primary_chinese` |
| `/api/topic/resources/page` | `/api/resources/page?sourceType=topic_resource` |
| `/api/topic/resources/{id}` | `/api/resources/resolve-global-id?sourceType=topic_resource&sourceId={id}` |

### 验收

```bash
node scripts/phase3i-d-legacy-410-test.mjs
# 开关已开时：
PHASE3I_EXPECT_LEGACY_410=true node scripts/phase3i-d-legacy-410-test.mjs
```

**回滚**：关 `feature.legacyReadApi410.enabled` → 立即恢复 legacy 200（无需发版）。

---

## D2 — 观察 3 天

- 管理端 **质量治理 → 旧接口调用**：legacy 命中应趋近 0；若 410 后仍有 legacy 统计，说明有客户端未迁移
- 监控 410 响应量（网关/access log）；**无异常激增**再进入 D3
- 建议每日跑：`phase3i-d-legacy-410-test.mjs` + `phase3g-legacy-usage-trend.mjs`

---

## D3 — 物理删除读 Controller（410 稳定 3 天后）

**删除**（读方法，保留写）：

- `ResourceBrowseController` 整类或全部 GET
- `PrimaryChineseResourceController`：`/page`、`/list`、`/{id}`、`/suites`、`/module-stats`
- `TopicController` / `CultureStudyController` / `CompetitionController`：resources 读 GET

**保留**：upload、submit、filter-options、mine、draft、POST view/download 等写链路。

更新 `LegacyApiUsageInterceptor` / `LegacyReadApi410Service` 路径表（删已不存在的 path）。

---

## D4 — 删前端 legacy 读函数

在 **gate 白名单清零** 后：

- 删 `browse.ts`、`primaryChinese.ts` 等 **读** 导出
- `resourceGateway.ts` 去掉 legacy fallback 分支
- 跑 `phase3g-no-new-legacy-calls-gate.mjs`（baseline 文件数下降）

写链路（`useMyResources`、`uploadPersist` 等）本阶段不动。

---

## 相关文件

| 组件 | 路径 |
|------|------|
| 410 映射 | `k12-resource/.../legacy/LegacyReadApi410Service.java` |
| 410 拦截 | `k12-resource/.../interceptor/LegacyReadApi410Interceptor.java` |
| SQL 开关 | `sql/94_phase3i_d_legacy_read_410.sql` |
| 验收脚本 | `scripts/phase3i-d-legacy-410-test.mjs` |
