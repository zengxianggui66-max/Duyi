# Phase 3I-A：统一读灰度切流 Runbook

## 1. 开关层级

| sys_config 键 | 前端字段 | 说明 |
|---------------|----------|------|
| `feature.resourceUnifiedRead.enabled` | `resourceUnifiedReadEnabled` | 总闸：关闭则全部走 legacy |
| `feature.primaryChineseUnifiedRead.enabled` | `primaryChineseUnifiedReadEnabled` | 学科页（primary_chinese / browse fallback） |
| `feature.topicUnifiedRead.enabled` | `topicUnifiedReadEnabled` | 专题专区 |
| `feature.cultureUnifiedRead.enabled` | `cultureUnifiedReadEnabled` | 传统文化 |
| `feature.competitionUnifiedRead.enabled` | `competitionUnifiedReadEnabled` | 竞赛专区 |

种子见 `sql/87_phase3p0_guardrails.sql`，**默认均为 `false`**。

管理端修改：`/admin/system/feature-flags`（无需重启）。

## 2. 切流顺序（生产推荐）

| 阶段 | 开启开关 | 验证页面 | 回滚 |
|------|----------|----------|------|
| P0 | staging 全开 | compare 脚本 | 关总开关 |
| P1 | primaryChinese + 总开关 | 学科页 SubjectDetail | 关 primary |
| P2 | topic | `/topic` | 关 topic |
| P3 | culture | `/culture` | 关 culture |
| P4 | competition | `/competition` | 关 competition |
| P5 | 确认总开关 ON | 全站读链路 | 关总开关 |

每阶段至少观察 **3 天**，全频道稳定 **7 天** 后再考虑旧读接口 410 下线（Phase 3I-D）。

## 3. 每阶段必跑脚本

```bash
cd k12-edu-microservice

node scripts/phase3p0-compare-resource-apis.mjs   # 含 page + suites/module-stats 对比（3I-B）
node scripts/phase3d-3e-acceptance-test.mjs
node scripts/phase3g-acceptance-test.mjs          # 含 G7/G8 unified 聚合端点
node scripts/phase3g-legacy-usage-trend.mjs
node scripts/phase3g-no-new-legacy-calls-gate.mjs
```

可选环境变量（学科页比对口径）：

| 变量 | 默认 | 说明 |
|------|------|------|
| `P3I_CMP_STAGE` | 小学 | 学段 |
| `P3I_CMP_SUBJECT` | 语文 | 学科 |
| `P3I_CMP_EDITION` | 统编版 | 版本 |
| `P3I_CMP_GRADE_NAME` | - | 年级（可选） |
| `P3I_CMP_CATALOG_NODE_ID` | - | 目录节点（设则追加 catalog 场景） |

## 4. 旧接口观测

- 管理端：**质量治理 → 旧接口调用**（`/admin/quality/legacy-api-usage`）
- API：`GET /api/admin/resource-main/legacy-api-usage?days=7`
- 统计路径（Phase 3I-A 修正后）：
  - `/api/primary-chinese/page`
  - `/api/resources/browse`（及 `/stats`、`/suites`、`/module-stats`）
  - `/api/topic/resources/page`
  - `/api/culture/resources/page`
  - `/api/competition/resources/page`

**下线门禁**：上述 5 个核心 path **连续 7 天 hitCount = 0**（browse 子 path 单独观测）。

## 5. 成功标准

- P0 compare 差异在容忍阈值内
- legacy 调用量**逐阶段下降**（不要求立即为 0）
- 无学科页「有目录无资源」类 P0 告警
- 前端无新增 legacy 直连（`phase3g-no-new-legacy-calls-gate.mjs` PASS）

## 6. 应急回滚

1. 管理端关闭对应频道开关或总开关
2. 确认 `/api/public/feature-flags` 返回 false（前端 10s 缓存）
3. 用户刷新后自动回 legacy，无需发版

## 7. 相关文档

- `docs/Phase3G-旧接口下线准备包.md`
- `docs/Phase3F3G3H-执行与验收清单.md`
- `sql/92_phase3i_a_legacy_usage_menu.sql`
