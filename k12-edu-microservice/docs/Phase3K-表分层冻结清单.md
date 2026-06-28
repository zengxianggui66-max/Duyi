# Phase 3K：表分层冻结清单

> 冻结周期：v3K ~ 下一大版本（1 个版本周期，COMMENT + 文档，不 DROP）  
> 基准：Phase 3B 回填完成 + Phase 3J 内容中心 + `inventory_all_tables.csv`（2026-06-28，122 表）  
> DELETE 候选：[`Phase3K-delete-candidates.md`](./Phase3K-delete-candidates.md)

---

## 1. 三层 + 待废弃定义

| 层级 | 表数量 | 策略 |
|------|--------|------|
| **PRIMARY** | 5 | 读写主路径；新功能只写这里 |
| **COMPAT** | 5 | 只读/同步；Adapter/专区 Service 写；禁止新 Controller 直连 Entity |
| **SEARCH_INDEX** | 1 | 仅 `SearchDocumentSyncService` 写 |
| **DEPRECATED** | 4 | 标记废弃；`row_count=0` 且无引用 → DELETE 候选 |

全库其余 **107** 张表/视图 = `OTHER`（见 `sql/tools/inventory_all_tables.csv`）。

---

## 2. 11 张资源表读写矩阵（K1）

### 2.1 PRIMARY（5）

| 表 | tier | 主要写入 | 主要读取/API | 禁止事项 |
|----|------|----------|--------------|----------|
| `resource_main` | PRIMARY | SQL 86 回填；3K-β 待补 upsert | `ResourceMainMapper`, `/api/resources/*`, `v_admin_resource_main` | 新功能绕过索引直查源表 |
| `edu_resource` | PRIMARY | `ResourceWriteService` | `EduResourceMapper`, Admin 资源中心 | 专区 Controller 直写 |
| `edu_resource_file` | PRIMARY | `ResourceWriteService` | `EduResourceFileMapper` | — |
| `edu_resource_dimension` | PRIMARY | `ResourceWriteService` | `EduResourceDimensionMapper` | — |
| `edu_resource_placement` | PRIMARY | `ResourcePlacementService` | `EduResourcePlacementMapper` | — |

### 2.2 COMPAT（5）

| 表 | tier | 主要写入 | 主要读取/API | 禁止事项 |
|----|------|----------|--------------|----------|
| `oss_primary_chinese_resource` | COMPAT | `PrimaryChineseResourceService`, `ResourceSyncService` | `/api/primary-chinese/*`, browse（410 迁移中） | 新 Controller import Entity |
| `topic_resource` | COMPAT | `TopicZoneService`, `TopicSourceAdapter` | `topicApi`, `/topic-zone` | 新 Controller import Entity |
| `culture_resource` | COMPAT | `CultureSourceAdapter` | `cultureStudyApi`, `/culture` | 新 Controller import Entity |
| `competition_resource` | COMPAT | `CompetitionSourceAdapter` | `competitionApi`, `/competition-zone` | 新 Controller import Entity |
| `article` | COMPAT | `ArticleServiceImpl`（k12-article） | `/api/news/*`, Admin `NewsOps` | 强制入 `edu_resource`；**不计入 K3 五源** |

### 2.3 SEARCH_INDEX（1）

| 表 | tier | 主要写入 | 主要读取/API | 禁止事项 |
|----|------|----------|--------------|----------|
| `sys_search_document` | SEARCH_INDEX | `SearchDocumentSyncService` | `SearchServiceImpl`, `/api/search/*` | 业务 Service 直写 `documentMapper` |

---

## 3. 4 张 DEPRECATE 表审计（K2）

| 表 | Java | 前端 | row_count | Verdict |
|----|------|------|----------:|---------|
| `resource_category` | Entity+Mapper（`@Deprecated`），无 Service | 0 | 0 | **DEPRECATE** |
| `edu_resource_tag` | 无 Entity | 0（docs） | 0 | **DELETE_CANDIDATE** |
| `edu_resource_region` | 无 Entity（Javadoc×2） | 0 | 0 | **DELETE_CANDIDATE** |
| `edu_resource_search_flat` | 无 Entity | 0（docs） | 0 | **DELETE_CANDIDATE** |

**KEEP 降级**：`row_count>0` 或非注释代码引用 → 降级 KEEP + 迁移计划。  
明细：[`Phase3K-delete-candidates.md`](./Phase3K-delete-candidates.md)

---

## 4. article 与 resource_main 边界

- 资讯正文、评论、收藏留在 **k12-article** + `article` 表。
- **不**强制映射到 `resource_main` / `edu_resource`（Phase 3B 原则）。
- 全站搜索：`SearchDocumentSyncService.syncArticleById` → `sys_search_document`（`doc_type=news`）。
- Admin 运营：`/admin/content/news` → `/api/admin/articles`。

---

## 5. sys_search_document 唯一写入路径

```
SearchDocumentSyncService.upsert/delete
  ← syncPrimaryById / syncTopicById / syncCultureById / syncCompetitionById
  ← syncArticleById（k12-resource internal API ← k12-article）
  ← rebuildAll / syncStageSubjectBrowseAsync
  → SearchEngineSyncService（引擎副本，只读上游）
```

**禁止**：`SearchServiceImpl`、Admin 或业务模块直接 `SysSearchDocumentMapper.insert/update/delete`。

---

## 6. 已知差距与 3K-β backlog

| 风险 | 现状 | 3K 控制 | 后续 |
|------|------|---------|------|
| 新 topic 未进 `resource_main` | 无 Java upsert | nightly **K3** 告警 | 3K-β：`ResourceMainUpsertService` + Adapter hook |
| 双写 primary ↔ edu | `ResourceSyncService` 同 ID | 文档标注 PRIMARY+COMPAT 交界 | 长期只留 `edu_resource` |
| 前台 legacy browse | 3G gate + 410 关 | `legacy_api_usage_stat` T6 | 归零后开 410 |
| 全库盘点 | ✅ `inventory_all_tables.mjs` | Step1 完成 | 定期 nightly 刷新 CSV |

**Step 5 已完成**：`phase3k-compat-controller-gate.mjs`、`ResourceCategory` `@Deprecated`。  
**3K-β 未做**：ResourceMain 实时 sync。

---

## 7. 删除候选（row_count=0 快照 2026-06-28）

| 表 | 快照 row_count | 下版本动作 |
|----|----------------:|------------|
| `edu_resource_tag` | 0 | DROP（见 delete-candidates.md） |
| `edu_resource_region` | 0 | DROP + 清理 Sinology Javadoc |
| `edu_resource_search_flat` | 0 | DROP |
| `resource_category` | 0 | 先删 Entity/Mapper，再 DROP |

---

## 8. 运维：verify SQL / nightly CI / 回滚

```bash
# COMMENT 冻结
mysql -u root -pzxg123456 xinketang < sql/97_phase3k_table_tier_comments.sql

# 只读核验（T3/T3b 孤儿 + T3c COMMENT + T4 质量视图）
mysql -u root -pzxg123456 xinketang < sql/tools/verify_phase3_resource_main_chain.sql

# K1–K3 自动化验收
node scripts/phase3k-db-acceptance.mjs

# 全库盘点
node sql/tools/inventory_all_tables.mjs --format=csv --out=sql/tools/inventory_all_tables.csv
```

| 组件 | 说明 |
|------|------|
| `.github/workflows/phase3k-db-nightly.yml` | UTC 02:00；verify + acceptance；`continue-on-error` 告警 |
| `scripts/phase3k-compat-controller-gate.mjs` | PR 门禁：禁止新 COMPAT Controller |
| `PHASE3K_SKIP_SQL=1` | 跳过 97 重放，仅校验 |

**回滚**：COMMENT 可重跑旧文案；无 DDL DROP；97 脚本幂等可重复执行。

---

## 9. 验收映射（K1–K3）

| ID | 预期 | 验证方式 |
|----|------|----------|
| **K1** | 冻结清单覆盖 11 张资源表 | 本文 §2 含 5 PRIMARY + 5 COMPAT + 1 SEARCH；`phase3k-db-acceptance.mjs` → `K1-doc-exists` |
| **K2** | tag/region/search_flat 无 Entity 且 DEPRECATE；`resource_category` `@Deprecated` | §3 + SQL COMMENT + `K2-deprecated-no-entity` |
| **K3** | `resource_main` 五源 0 孤儿、视图覆盖 | `K3-unmapped-all-zero` + `K3-view-coverage` + verify T3/T3b + `v_resource_migration_quality` 可读 |

与 Phase 3B 关系：`phase3b-db-acceptance.mjs` **B4=0** 与 K3 等价（K3 额外含 `primary_chinese` 独立 T3b）。
