# Phase 3：资源主链路统一 - 深度验收报告

验收时间：2026-06-27

## 1. 总体结论

Phase 3 尚未完全完成，但已经从“规划阶段”推进到“主链路雏形可构建”阶段。

当前完成度判断：

| 阶段 | 结论 | 说明 |
| --- | --- | --- |
| Phase 3-P0 迁移护栏 | 基本完成 | 已有迁移台账、灰度开关、旧新对比口径、旧接口统计表和拦截器。 |
| Phase 3A 资源契约 | 基本完成 | 接口契约、字段字典、枚举状态机、兼容映射文档齐全。 |
| Phase 3B 数据库梳理 | 部分完成 | 已有 `86_phase3b_resource_main_chain.sql`，但需在目标库执行并通过覆盖率验收。 |
| Phase 3C 数据回填与质量看板 | 部分完成 | 已有目录别名表和质量视图脚本；仍需真实回填、差异报告、异常清单闭环。 |
| Phase 3D 前台统一资源服务 | 部分完成 | 已有 `/api/resources/*` 和 `ResourceMainServiceImpl`；学科资源页仍未完全迁移。 |
| Phase 3E 管理端统一 | 部分完成 | `/api/admin/resource-main` 已扩展写动作，但仍是硬编码 sourceType 分支，未完全 SourceAdapter 化。 |
| Phase 3F 前台迁移 | 部分完成 | Topic/Culture/Competition 已接入 `resourceGateway` 灰度读；学科主页面、我的资源、上传仍大量旧接口。 |
| Phase 3G 旧接口下线 | 未完成 | 已有统计与准备包，但旧接口仍在生产调用路径中。 |
| Phase 3H 乱码治理 | 部分完成 | 已修复一个会影响筛选语义的前端核心文件和两个 Phase3 SQL；后端 Java/旧 SQL 仍有乱码。 |

## 2. 已核验的落地能力

### 2.1 文档与脚本

已存在：

- `docs/Phase3A-资源主链路统一-接口契约.md`
- `docs/Phase3A-资源主链路统一-字段字典.md`
- `docs/Phase3A-资源主链路统一-枚举与状态机.md`
- `docs/Phase3A-资源主链路统一-兼容映射与执行步骤.md`
- `docs/Phase3B-资源主链路统一-数据库梳理与迁移计划.md`
- `sql/86_phase3b_resource_main_chain.sql`
- `sql/87_phase3p0_guardrails.sql`
- `sql/88_phase3c_alias_quality.sql`
- `sql/89_phase3g_legacy_api_usage.sql`

### 2.2 后端统一读链路

已存在：

- `ResourceMainController`
- `ResourceMainService`
- `ResourceMainServiceImpl`
- `/api/resources/page`
- `/api/resources/detail/{resourceId}`
- `/api/resources/resolve-global-id`
- `/api/resources/stats`
- `/api/resources/types`
- `/api/resources/{resourceId}/preview`
- `/api/resources/{resourceId}/view`
- `/api/resources/{resourceId}/download`
- `/api/resources/{resourceId}/collect`

风险：

- `ResourceMainServiceImpl` 仍直接依赖多个具体 service：`PrimaryChineseResourceService`、`TopicZoneService`、`CultureStudyService`、`CompetitionZoneService`。
- 当前不是插件化 `SourceAdapter`，后续新增主题班会、资讯附件、其他学科时仍要改主服务。
- `types()` 当前用分页 1000 条聚合，数据量变大后统计不准确，应改为 SQL group by。

### 2.3 管理端统一读写链路

已存在：

- `/api/admin/resource-main`
- `/api/admin/resource-main/pending`
- `/api/admin/resource-main/{globalId}`
- `/api/admin/resource-main/stats`
- `/api/admin/resource-main/{globalId}/audit`
- `/api/admin/resource-main/{globalId}/publish`
- `/api/admin/resource-main/{globalId}/offline`
- `/api/admin/resource-main/{globalId}/recommend`
- `/api/admin/resource-main/{globalId}/top`
- `/api/admin/resource-main/{globalId}/placement`
- `/api/admin/resource-main/batch`
- `/api/admin/resource-main/batch-audit`

风险：

- 非 `primary_chinese` 来源的写动作是简化实现，主要更新 `status` / `isElite` / `sort`。
- `placement` 目前只支持 `primary_chinese`。
- `top` 目前只支持 `primary_chinese`。
- 审核洞察对非学科资源仍是占位返回。

### 2.4 前端统一网关

已存在：

- `src/api/resourceGateway.ts`
- Topic/Culture/Competition 已通过灰度开关尝试读 `/resources/page`。
- 详情页已出现 `resolveGlobalId -> getUnifiedDetail -> view/download` 链路。
- 请求取消能力已在 `resourceGateway` 中实现。

风险：

- `SubjectDetailPage`、`useApiResources`、`useLessonHub`、`useBrowseTypeStats` 仍使用 `browseApi` / `primaryChineseApi`。
- `MyResources`、上传草稿、个人中心仍使用 `/primary-chinese/*`。
- Topic/Culture/Competition 的筛选维度仍有部分回退到旧接口，例如 region/duration 等统一视图尚未完整表达。

## 3. 本次补齐内容

本次验收过程中已补齐：

1. 重建 `k12-edu-platform/src/composables/useResourceBrowseContext.ts`。
   - 修复文件乱码。
   - 修复 `全部` 比较值损坏导致 `type=全部` 被写入 URL 的风险。
   - 保留面包屑、URL 同步、详情回跳能力。

2. 重建 `sql/88_phase3c_alias_quality.sql`。
   - 修复注释和表注释乱码。
   - 保留 `resource_catalog_alias` 与 `v_resource_migration_quality`。

3. 重建 `sql/89_phase3g_legacy_api_usage.sql`。
   - 修复旧接口统计表注释乱码。
   - 明确由 `LegacyApiUsageInterceptor` 写入。

4. 完成构建验收。
   - `mvn -pl k12-resource -am -DskipTests compile` 通过。
   - `npm run build` 通过。

5. 完成真实库只读验收。
   - 验收脚本：`sql/tools/verify_phase3_resource_main_chain.sql`
   - 执行方式：MySQL 只读查询，不写入业务数据。

## 3.1 真实库验收结果

### resource_main 覆盖

| source_type | resource_main 数量 |
| --- | ---: |
| primary_chinese | 330 |
| topic_resource | 20 |
| culture_resource | 19 |
| competition_resource | 12 |
| edu_resource | 23 |

### v_admin_resource_main 覆盖

| source_type | 视图数量 |
| --- | ---: |
| primary_chinese | 330 |
| topic_resource | 20 |
| culture_resource | 19 |
| competition_resource | 12 |
| edu_resource | 25 |

### 未映射旧来源

| source_type | 未映射数量 |
| --- | ---: |
| topic_resource | 0 |
| culture_resource | 0 |
| competition_resource | 0 |
| edu_resource | 0 |

结论：Phase 3B 的核心映射已打通，`topic/culture/competition/edu_resource -> resource_main` 当前库中无未映射记录。

### 质量指标

| 指标 | 数量 | 判定 |
| --- | ---: | --- |
| approved_not_published | 0 | 通过 |
| published_but_invisible | 0 | 通过 |
| empty_file_resources | 7 | 待修复 |
| orphan_resources_without_catalog | 7 | 待修复 |
| unplaced_resources | 112 | 待修复 |
| catalog_nodes_without_resources | 1376 | 可接受但需区分“空目录”和“应有资源目录” |

结论：资源状态链路较稳定，但目录挂载和文件完整性还没有收口。

### 统一读灰度开关

当前全部为 `true`：

- `feature.resourceUnifiedRead.enabled`
- `feature.primaryChineseUnifiedRead.enabled`
- `feature.topicUnifiedRead.enabled`
- `feature.cultureUnifiedRead.enabled`
- `feature.competitionUnifiedRead.enabled`

### 旧接口调用量

最近 7 天仍有调用：

| api_path | hit_count_7d |
| --- | ---: |
| `/api/primary-chinese/page` | 11 |
| `/api/competition/resources/page` | 9 |
| `/api/topic/resources/page` | 9 |
| `/api/culture/resources/page` | 6 |

结论：Phase 3G 旧接口下线尚未完成，前台仍有回退或直接调用旧接口。

## 4. 未完成项与继续执行顺序

### P1：先完成数据库实迁移验收

必须在目标库执行并保留输出：

1. `sql/86_phase3b_resource_main_chain.sql`
2. `sql/87_phase3p0_guardrails.sql`
3. `sql/88_phase3c_alias_quality.sql`
4. `sql/89_phase3g_legacy_api_usage.sql`

验收 SQL：

```sql
SELECT source_type, COUNT(*) AS cnt
FROM resource_main
GROUP BY source_type
ORDER BY source_type;

SELECT source_type, COUNT(*) AS cnt
FROM v_admin_resource_main
GROUP BY source_type
ORDER BY source_type;

SELECT metric_key, metric_value
FROM v_resource_migration_quality
ORDER BY metric_key;
```

完成标准：

- `resource_main` 至少覆盖 `primary_chinese`、`topic_resource`、`culture_resource`、`competition_resource`、`edu_resource`。
- `v_admin_resource_main` 能按上述来源查询。
- 迁移质量视图输出不报错，异常指标有负责人和清理计划。

### P2：把 `ResourceMainServiceImpl` 改成 SourceAdapter

当前主服务直接 switch sourceType，下一步应拆为：

- `ResourceSourceAdapter`
- `PrimaryChineseResourceAdapter`
- `TopicResourceAdapter`
- `CultureResourceAdapter`
- `CompetitionResourceAdapter`
- `EduResourceAdapter`

完成标准：

- 新增来源不修改 `ResourceMainServiceImpl` 主流程。
- view/download/collect/preview/update/audit/publish/offline 由 adapter 执行。

### P3：学科资源页迁移到 resourceGateway

优先迁移：

- `useApiResources`
- `useLessonHub`
- `useBrowseTypeStats`
- `SubjectDetailPage` 中的 suite/stats/detail 辅助读取

完成标准：

- 学科资源主列表优先走 `/api/resources/page`。
- 目录点击以 `catalogNodeId` 为主查询条件。
- 旧 `/api/resources/browse` 只作为灰度回退。

### P4：补齐统一视图维度

当前 Topic/Culture/Competition 统一读存在维度损耗，导致前端按 region/duration 等筛选时仍回退旧接口。

建议给统一视图补充：

- `region`
- `duration_type`
- `resource_form`
- `category`
- `external_url`
- `cover_url`
- `tags`

完成标准：

- Topic/Culture/Competition 列表在常用筛选下不再回退旧接口。

### P5：旧接口下线门禁

完成顺序：

1. 确认 `legacy_api_usage_stat` 持续写入。
2. 管理端增加旧接口调用量视图。
3. 旧接口连续 7 天核心路径调用量为 0。
4. 旧 controller 改为兼容壳，内部转调新服务。
5. 删除旧 mapper/service/sql seed 路径。

## 5. 最终判断

当前第三阶段不能声明“全部完成”。可以声明：

> Phase 3A 和主链路雏形已完成；Phase 3B/3C/3D/3E 已进入可验收/可灰度阶段；Phase 3F/3G/3H 仍未收口。

下一步最关键不是继续写更多规划，而是按 P1-P5 收口：

1. 跑真实库迁移与质量报告。
2. 把前台学科资源页切到统一 API。
3. 把后端 sourceType 分支改成 adapter。
4. 开旧接口统计，清零后下线旧链路。
5. 持续清理乱码，避免中文筛选和提示再次损坏。
