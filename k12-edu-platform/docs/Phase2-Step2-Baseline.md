# Phase 2 Step 2 — 基线快照（阶段 0）

> 日期：2026-06-24  
> 前置：Step 1 验收 21/21 全绿

## 1. 环境与服务

| 项 | 状态 |
|----|------|
| Gateway 9001 | 运行中 |
| k12-auth 8081 | 需本地启动 |
| k12-resource 8082 | 需重启以加载 Step2 后端改动 |
| Step 1 基线脚本 | **21/21 PASS** |
| `VITE_USE_TAXONOMY_API` | 默认 `true`（`!== 'false'`） |
| `VITE_USE_DICTIONARY_API` | 默认 `true` |

## 2. Taxonomy 读 API 快照（A1–A6）

执行：`node -e` 或 `phase5-acceptance-test.mjs` A 段

| ID | API | 快照（2026-06-24） |
|----|-----|-------------------|
| A1 | `GET /api/taxonomy/stages` | 6 条：`primary,junior,senior,art,dance,preschool` |
| A2 | `GET /api/taxonomy/subjects?stage=primary` | 8 条：语文,数学,英语,科学,道德与法治… |
| A3 | `GET /api/taxonomy/editions?stage=primary&subject=chinese` | 9 条 |
| A4 | `GET /api/taxonomy/volumes?stage=primary` | 12 条：一年级上册…六年级下册 |
| A5 | `GET /api/taxonomy/modules?stage=primary` | 29 条：同步备课,开学专区… |
| A6 | `GET /api/taxonomy/resource-types?stage=primary&subject=语文&module=同步备课` | 10 条：教案,学案,课件… |

**阶段 1 新增：** `GET /api/taxonomy/grades?stage=primary` → 预期 **6** 条（`grade1`…`grade6`）

## 3. 种子数据（管理端）

| SQL | 内容 | 用途 |
|-----|------|------|
| `sql/03_dimension_taxonomy.sql` | edu_stage/subject/grade 等 | taxonomy 基础 |
| `sql/59_admin_catalog_phase5c.sql` | 目录方案 | catalog |
| `sql/34_*` / catalog seed | 单元树 | `/api/catalog/tree` |
| `sql/99_seed_all.sql` | 全量种子 | 本地联调 |

管理端入口：

- `/admin/taxonomy` — 分类维度 CRUD
- `/admin/catalog` — 教材目录树

## 4. 改造前问题清单（前端）

| # | 问题 | 位置 | 严重度 |
|---|------|------|--------|
| P1 | 基本信息步年级/学科来自 `uploadOptions.ts` 常量 | `useResourceUploadForm.ts` → `gradesByGrade` / `subjectsByGrade` | **高** |
| P2 | 学段 UI 写死 6 个 radio，非 API 驱动 | `UploadStepBasic.vue` | 中 |
| P3 | placement 仍 merge `primaryChineseApi.getUploadFilterOptions` 历史 DISTINCT | `UploadStepPlacement.vue` | **高** |
| P4 | 单元树仍走 `primaryChineseApi.getUnitTree`，非 `/api/catalog/tree` | `UploadStepPlacement.vue` | **高** |
| P5 | `ResourceUploadDialog.vue` 内联 `gradesByLevel` | 次要入口 | 中 |
| P6 | 后端 `getUploadFilterOptions` 曾 merge 脏数据 | `UploadPlacementService` | **已修（阶段1）** |
| P7 | 上传写接口无 taxonomy 校验 | `PrimaryChineseResourceService` | **已修（阶段1）** |

### 4.1 小学年级选项（改造前）

`uploadOptions.ts` 中 `gradesByGrade.primary` 为 `grade_1`…`grade_6`（**逻辑正确**），但与 DB `grade1` 编码不一致；且未与 `gradeName`（册别）分离，易与 placement 合并列表混淆。

### 4.2 Placement 册别（改造前）

`UploadStepPlacement` 将 `taxonomy volumes` 与 `getUploadFilterOptions.gradeNames`（含历史 DISTINCT）**合并**，可能引入非本学段册别名。

## 5. 阶段 0 签字

- [x] Step 1 验收全绿
- [x] Taxonomy A1–A6 API 有数据
- [x] 问题清单与 API 快照文档化
- [ ] 手工截图（F-U0）：上传页小学年级 / placement 册别（UI 改造后对比）

## 6. 下一步

阶段 2–6 前端 + 验收已完成，见 `Phase2-Step2-验收.md` 与 `phase2-step2-upload-taxonomy-acceptance.mjs`。

下一步：第三步上传资源生命周期改造（保存草稿 / 提交审核 / 跳转我的资源）。
