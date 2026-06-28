# Phase 3G 旧接口下线准备包

## 1) 调用量趋势（近 7/14 天）

执行：

```bash
node scripts/phase3g-legacy-usage-trend.mjs
```

当前观测（2026-06-27）：

- `/api/primary-chinese/page`: 11
- `/api/topic/resources/page`: 9
- `/api/competition/resources/page`: 9
- `/api/culture/resources/page`: 6

结论：旧读接口仍有调用量，暂不满足物理删除条件。

监控路径（Phase 3I-A）：

- `/api/primary-chinese/page`
- `/api/resources/browse`（及 `/stats`、`/suites`、`/module-stats`）
- `/api/topic/resources/page`
- `/api/culture/resources/page`
- `/api/competition/resources/page`

## 2) 可删除清单（分批次）

> 规则：先“禁新增调用”，再“调用量清零”，再“物理删除”。

### 批次 A（立即执行，低风险）

- **门禁**：启用 `scripts/phase3g-no-new-legacy-calls-gate.mjs`，阻止新增 legacy 直连。
- **统计**：持续运行 `legacy_api_usage_stat` 统计与 `legacy-api-usage` 管理查询。

### 批次 B（调用量连续 7 天为 0 后）

- **Controller 候选**：
  - `PrimaryChineseResourceController`（仅保留写链路相关 endpoint，逐步拆分）
  - `ResourceBrowseController`
  - `TopicController`（读 endpoint）
  - `CultureStudyController`（读 endpoint）
  - `CompetitionController`（读 endpoint）
- **前端 API 候选模块**：
  - `src/api/primaryChinese.ts`（读部分）
  - `src/api/browse.ts`
  - `src/api/topic.ts`（读部分）
  - `src/api/cultureStudy.ts`（读部分）
  - `src/api/competition.ts`（读部分）

### 批次 C（调用量连续 14 天为 0 后）

- **Mapper/service 清理候选**（按真实引用树二次确认）：
  - `ResourceBrowseMapper` 及 browse 旧读路径关联 service
  - `TopicResourceMapper` / `CultureResourceMapper` / `CompetitionResourceMapper` 的旧读入口调用点
- **SQL 种子路径候选**（需先审计依赖）：
  - `sql/12_culture_study.sql`
  - `sql/13_competition_zone.sql`
  - `sql/15_topic_zone.sql`
  - `sql/20_topic_resource_file_url.sql`
  - `sql/tools/seed_oss_*.sql`（仅旧链路演示种子）

## 3) 门禁与执行顺序

1. 开启门禁：`phase3g-no-new-legacy-calls-gate`
2. 观察趋势：7 天窗口 + 14 天窗口
3. 达到 0 调用后提删除 PR（先读链路、后兼容桥、再 mapper/sql）

## 4) 一键执行准备包

```bash
node scripts/phase3g-offline-prep-pack.mjs
```
