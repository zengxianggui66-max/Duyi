# 方案 A 落地实施计划：学科网式主流程 + 品牌换树

> **方案 A**：保持一个 `SubjectDetailPage`（学科网骨架），通过 **品牌（系列）切换目录方案（Catalog Scheme）** 加载不同左侧树；右侧按 **展示模式（Display Mode）** 在「本课分组 / 分类列表 / 单元矩阵」间切换。  
> **原则**：前台 UX ≈ 学科网；后台数据 ≈ 品牌 + 分类树 + 资源挂载 + 规范主表；宽表仅作过渡读模型直至 M4 下线。  
> 关联文档：[资源平台架构与多学科品牌设计方案.md](./资源平台架构与多学科品牌设计方案.md)

---

## 目录

1. [目标与边界](#1-目标与边界)
2. [总体阶段图](#2-总体阶段图)
3. [数据库：分阶段调整](#3-数据库分阶段调整)
4. [后端：分阶段实现](#4-后端分阶段实现)
5. [前端：分阶段优化](#5-前端分阶段优化)
6. [前后端契约（统一 BrowseContext）](#6-前后端契约统一-browsecontext)
7. [七彩 / 状元种子与映射](#7-七彩--状元种子与映射)
8. [联调与验收清单](#8-联调与验收清单)
9. [风险与回滚](#9-风险与回滚)

---

## 1. 目标与边界

### 1.1 做完方案 A 后用户看到什么

| 能力 | 描述 |
|------|------|
| 学科网式浏览 | 版本 + 册别 → 左树 → 类型 Tab → 右侧列表/本课资源 |
| 品牌切换 | 顶区选择 **七彩课堂 / 状元版 / 全部**，左树与右侧模式随之切换 |
| 七彩课堂 | 教材单元树 + 课文工作台（按 type 分组，可二级 subType） |
| 状元版 | 产品分类树（上课课件/其他资源/教案/作业课件）+ 分类列表；「各单元教案」用单元 Tab |
| 平台栏目 | 同步备课 / 期中 / 期末等保留为 **快捷筛选**（与树正交） |
| 上传 | 从当前浏览上下文带入 brand + 目录节点 + module + type |
| 兼容 | 旧链接、`/api/primary-chinese/*` 在 M3 前仍可用 |

### 1.2 本期不做（可后续迭代）

- 首页专区全面改造（仍可用现有 `home_panel_*`）
- ES 全文检索、推荐算法
- 管理后台独立 CMS（可用 SQL + 导入脚本代替）
- 多租户 SaaS 隔离

### 1.3 技术栈不变

- 前端：Vue 3 + Vite + Element Plus（`k12-edu-platform`）
- 后端：`k12-resource` :8082，可选 gateway :9000
- 库：`xinketang`

---

## 2. 总体阶段图

```text
M0 基线冻结 ──► M1 品牌+契约 ──► M2 目录树API ──► M3 统一浏览API
      │              │                │                  │
      │              └────────────────┴──────────────────┤
      │                                                    ▼
      │              M4 主数据+同步宽表 ◄── M5 上传/导入 ◄── M6 体验打磨
```

| 阶段 | 周期建议 | 数据库 | 后端 | 前端 | 可演示 |
|------|----------|--------|------|------|--------|
| **M0** | 2–3 天 | 文档化现状 | 无破坏性变更 | 梳理组件依赖 | 现有页可用 |
| **M1** | 1 周 | 品牌表 + 宽表扩展字段 | brand 筛选透传 | 系列行 + URL + API 参数 | 按品牌筛列表 |
| **M2** | 1–2 周 | catalog 三表 + 种子 | `/catalog/tree` | CatalogSidebar 换树 | 七彩/状元左树 |
| **M3** | 1–2 周 | placement + 索引 | `/resources/browse` | useCatalogBrowse 收敛 | 新 API 主列表 |
| **M4** | 2 周 | edu 主写 + 同步任务 | ResourceWrite + Sync | 详情多文件 | 导入+预览 |
| **M5** | 1–2 周 | pack 可选 | 批量导入 | 上传向导增强 | 批量上卷 |
| **M6** | 1 周 | 优化索引 | stats/角标 | 去 demo、UI 抛光 | 对标学科网 |

**铁律**：每个阶段 **DB → 后端 API → 前端** 顺序发布；前端可 feature flag（`VITE_USE_CATALOG_BROWSE`）切新旧 API。

---

## 3. 数据库：分阶段调整

### M0：基线（仅文档）

- 确认 `oss_primary_chinese_resource`、`edu_resource`、`edu_module`、`edu_resource_type`、`edu_unit`、`edu_lesson` 现状。
- 导出一份 `y1s1` 单元清单与状元目录 JSON 备档。

### M1：品牌维度（最小侵入）

**脚本**：`sql/27_brand_baseline.sql`（建议新建）

```sql
-- 1) 品牌表
CREATE TABLE edu_resource_brand (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(32) NOT NULL UNIQUE,   -- qicai | zhuangyuan | platform
  name VARCHAR(50) NOT NULL,
  publisher VARCHAR(100) DEFAULT NULL,
  logo_url VARCHAR(500) DEFAULT NULL,
  sort SMALLINT DEFAULT 0,
  status TINYINT DEFAULT 1
);

-- 2) 宽表扩展（过渡期列表仍读宽表）
ALTER TABLE oss_primary_chinese_resource
  ADD COLUMN brand_code VARCHAR(32) DEFAULT NULL COMMENT '品牌编码' AFTER edition,
  ADD COLUMN catalog_node_id BIGINT UNSIGNED DEFAULT NULL,
  ADD COLUMN catalog_path VARCHAR(500) DEFAULT NULL,
  ADD COLUMN sub_type VARCHAR(50) DEFAULT NULL COMMENT '教案版/精品版/希沃等',
  ADD KEY idx_brand_browse (brand_code, grade_name, edition, module, is_deleted, status);
```

**种子**：

| code | name |
|------|------|
| `qicai` | 七彩课堂 |
| `zhuangyuan` | 状元版 |
| `platform` | 平台自建 |

**数据迁移**：历史行 `brand_code` 可为 NULL；新上传必须带 brand。

### M2：目录树（方案 A 核心）

**脚本**：`sql/28_catalog_scheme.sql`

```sql
CREATE TABLE edu_catalog_scheme (
  id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,  -- textbook_unit | zy_taxonomy
  name VARCHAR(100) NOT NULL,
  brand_id BIGINT UNSIGNED DEFAULT NULL,
  display_mode VARCHAR(30) NOT NULL, -- lesson_hub | category_list | unit_matrix
  meta JSON DEFAULT NULL,
  sort SMALLINT DEFAULT 0,
  status TINYINT DEFAULT 1,
  KEY idx_brand (brand_id)
);

CREATE TABLE edu_catalog_node (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  scheme_id INT UNSIGNED NOT NULL,
  parent_id BIGINT UNSIGNED NOT NULL DEFAULT 0,
  code VARCHAR(80) NOT NULL,
  name VARCHAR(200) NOT NULL,
  name_path VARCHAR(800) NOT NULL,   -- /上课课件/备课资源/说课稿
  depth TINYINT NOT NULL DEFAULT 0,
  node_type VARCHAR(20) NOT NULL,    -- folder|unit|lesson|section|leaf
  sort SMALLINT DEFAULT 0,
  icon VARCHAR(20) DEFAULT NULL,
  meta JSON DEFAULT NULL,            -- {formats:[], defaultModule, defaultType}
  status TINYINT DEFAULT 1,
  KEY idx_scheme_parent (scheme_id, parent_id, sort),
  KEY idx_name_path (scheme_id, name_path(191))
);

-- 闭包表（节点>5k时建议，M2 可暂缓）
-- edu_catalog_closure(ancestor_id, descendant_id, depth)
```

**scheme 种子**：

| code | brand | display_mode | 说明 |
|------|-------|--------------|------|
| `textbook_unit` | qicai | `lesson_hub` | 七彩教材树 |
| `zy_taxonomy` | zhuangyuan | `category_list` | 状元分类树 |
| `zy_taxonomy_unit` | zhuangyuan | `unit_matrix` | 挂载在「各单元完整教案」类节点 |

**脚本**：`sql/29_seed_catalog_qicai_y1s1.sql`、`sql/30_seed_catalog_zhuangyuan.sql`  
从 `unitData.ts` / 用户提供的状元目录生成 INSERT。

### M3：资源挂载

**脚本**：`sql/31_resource_placement.sql`

```sql
CREATE TABLE edu_resource_placement (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  resource_id BIGINT UNSIGNED NOT NULL,
  catalog_node_id BIGINT UNSIGNED NOT NULL,
  is_primary TINYINT DEFAULT 1,
  module_id SMALLINT UNSIGNED DEFAULT NULL,
  resource_type_id SMALLINT UNSIGNED DEFAULT NULL,
  sort INT DEFAULT 0,
  UNIQUE KEY uk_res_node (resource_id, catalog_node_id),
  KEY idx_node (catalog_node_id),
  KEY idx_resource (resource_id)
);
```

M3 前：列表仍可按宽表 `unit_name`/`lesson_name` + `brand_code` 查询。  
M3 后：列表 **优先** `placement` JOIN `edu_resource`（或宽表同步字段）。

### M4：主数据写入口强化

- 确保 `edu_resource`、`edu_resource_file` 字段满足：多文件 `file_role`（main/answer/seewo/video）。
- 视图 `v_resource_browse`（可选）：JOIN 维度 + brand + 主文件 URL，供 browse API 读。

**同步**：定时或写后触发 `ResourceSyncService` → 更新 `oss_primary_chinese_resource` 同 id 行（双写过渡期）。

### M5：资源包（可选）

```sql
CREATE TABLE edu_resource_pack (
  id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  brand_id BIGINT UNSIGNED NOT NULL,
  code VARCHAR(64) NOT NULL UNIQUE,
  title VARCHAR(200) NOT NULL,
  edition_id SMALLINT UNSIGNED,
  volume_id TINYINT UNSIGNED,
  subject_id SMALLINT UNSIGNED,
  catalog_scheme_id INT UNSIGNED,
  status TINYINT DEFAULT 1
);
```

### M6：清理与索引

- 删除或废弃宽表直写路径（仅同步入）。
- 补充 `idx_placement_browse` 组合索引。
- `module-stats` 按 brand 聚合物化（可选）。

---

## 4. 后端：分阶段实现

### M0：包结构规划（建议新增）

```text
com.k12.resource.catalog/
  CatalogSchemeMapper / CatalogNodeMapper
  CatalogService.java          -- 树、子树、breadcrumb
  CatalogSeedImporter.java     -- 从 JSON 灌节点

com.k12.resource.browse/
  ResourceBrowseService.java   -- 统一列表/统计
  ResourceBrowseMapper.java
  BrowseQueryDTO / BrowseItemVO

com.k12.resource.brand/
  BrandService.java

com.k12.resource.sync/
  ResourceSyncService.java     -- edu -> 宽表

com.k12.resource.imports/
  CatalogPathImportService.java
```

现有 `PrimaryChineseResource*` **保留至 M4**，内部逐步委托给 `ResourceBrowseService`。

### M1：品牌透传（1 周）

| 任务 | 说明 |
|------|------|
| `BrandController` | `GET /api/brands?stage=&subject=` |
| 扩展 `PrimaryChineseResourceQueryDTO` | `brandCode`, `subType` |
| `PrimaryChineseResourceMapper` | WHERE `brand_code = ?`（NULL 视为全部） |
| 写入 | `save` / `batch-save` 写 `brand_code`；`OssMetadataHelper` 不变 |

**验收**：`GET /api/primary-chinese/list?brandCode=qicai` 仅返回七彩数据。

### M2：目录树 API（1–2 周）

| API | 说明 |
|-----|------|
| `GET /api/catalog/schemes` | 参数：`brandCode, stage, subject` → 返回 scheme 列表 + `displayMode` |
| `GET /api/catalog/tree` | 参数：`schemeId, volumeKey, gradeName, edition, subject` → `List<CatalogNodeVO>` |
| `GET /api/catalog/node/{id}/breadcrumb` | 面包屑路径 |

**CatalogService 逻辑**：

1. 根据 `brandCode` 解析默认 `schemeId`。
2. 加载 `edu_catalog_node` 构建树（parent_id）。
3. M2 可与 `UnitCatalogService` 并存：七彩 scheme 可从 JSON **或** 导入的 node 表读取（优先 DB）。

**验收**：Postman 拉取状元树 4 层结构与文档一致。

### M3：统一浏览 API（1–2 周）

| API | 说明 |
|-----|------|
| `GET /api/resources/browse` | 分页列表（核心） |
| `GET /api/resources/browse/stats` | 当前节点下各 `type` 数量（类型 Tab 角标） |
| `GET /api/resources/browse/suites` | 成套（可迁移原 suites） |

**BrowseQueryDTO**（与前端 BrowseContext 对齐）：

```java
String brandCode;
Long schemeId;
Long catalogNodeId;      // 选中节点；后端展开子树
boolean includeSubtree;    // 默认 true
String stageName, subjectName, gradeName, edition;
Integer moduleId;        // 可选，平台栏目
String typeName;         // 可选
String keyword;
Integer page, size;
String sortField, sortOrder;
```

**查询策略（过渡期）**：

```text
if (placement 表有数据)
  JOIN placement + resource (+ 维度)
else
  fallback 宽表 brand_code + unit/lesson + catalog_path 前缀匹配
```

**兼容**：`PrimaryChineseResourceController.list/page` 改为调用 `ResourceBrowseService`，响应结构不变。

**验收**：前端切 `VITE_USE_CATALOG_BROWSE=true` 后，学科页列表与旧接口一致且支持 `catalogNodeId`。

### M4：写入与同步（2 周）

| API | 说明 |
|-----|------|
| `POST /api/resources` | 创建 `edu_resource` + files + placements |
| `PUT /api/resources/{id}` | 更新 |
| `POST /api/resources/import/batch` | 目录/OSS 批量（内部路径→node） |

**ResourceSyncService**：写后同步宽表字段（含 `brand_code`, `catalog_path`, `catalog_node_id`）。

**验收**：上传 1 条 → browse 可见 → 宽表也有 → 预览可用。

### M5：批量导入（1–2 周）

- 解析 OSS 路径：`qicai/tongbian2024/y1s1/chinese/同步备课/...`
- 自动 match 或创建 `catalog_node`
- 幂等：`(brand, oss_object_key)` 唯一

### M6：打磨

- `browse/stats` 性能（缓存 30s）
- 去除对 `UnitCatalogService` 静态 JSON 依赖（可选）
- Gateway 路由：`/api/catalog/**`, `/api/resources/**`, `/api/brands/**`

---

## 5. 前端：分阶段优化

### M0：重构准备（2–3 天）

| 任务 | 文件/动作 |
|------|-----------|
| 画依赖图 | `SubjectDetailPage` → composables 列表 |
| 抽离类型 | `types/browse.ts`：`BrowseContext`, `CatalogNode`, `DisplayMode` |
| Feature flag | `.env`：`VITE_USE_CATALOG_BROWSE=false` |
| 禁止新增 | 不再往 `useSubjectPageState.demoTopicListData` 加假数据 |

### M1：品牌系列行（1 周）

| 新增/修改 | 说明 |
|-----------|------|
| `config/resourceSeriesConfig.ts` | 七彩/状元/全部；关联 `defaultSchemeCode`、`displayMode` |
| `composables/useResourceSeries.ts` | `selectedBrandCode`，watch 触发刷新 |
| `components/subject/SeriesFilterBar.vue` | 系列 Tab（学科网「供应商」区上位） |
| `useResourceBrowseContext.ts` | query 增加 `brand`；面包屑增加系列名 |
| `api/brand.ts` | `GET /api/brands`（或先用本地 config） |
| `primaryChinese.ts` / 将来 `browse.ts` | 请求带 `brandCode` |

**SubjectDetailPage 接线**：

```text
SeriesFilterBar → useResourceSeries
  → useVersionVolume（出版社随系列变）
  → useApiResources / useLessonHub（带 brandCode）
```

**验收**：切换「状元版」后 API 带 `brandCode=zhuangyuan`；面包屑含「状元版」。

### M2：学科网左树组件化（1–2 周）

| 新增/修改 | 说明 |
|-----------|------|
| `api/catalog.ts` | `getSchemes`, `getTree` |
| `composables/useCatalogTree.ts` | 加载树、选中节点、`activeNodeId` |
| `components/catalog/CatalogSidebar.vue` | 通用树 UI（替换 CourseCatalog 内树部分或包装） |
| `useUnitDirectory.ts` | 标记 `@deprecated`，内部转调 `useCatalogTree`（adapter） |

**品牌 → scheme 自动选择**：

```ts
watch(selectedBrandCode, (brand) => {
  const scheme = resolveScheme(brand, stage, subject)
  loadTree(scheme.id)
})
```

**验收**：

- 七彩：树 = 单元/课/板块（与 unitData 一致或更全）
- 状元：树 = 四层分类，无「假单元」

### M3：统一浏览 composable（1–2 周）

| 新增/修改 | 说明 |
|-----------|------|
| `api/browse.ts` | `browse`, `browseStats`, `browseSuites` |
| `composables/useCatalogBrowse.ts` | **收敛** `useApiResources` + `useLessonHub` |

**Display Mode 路由**（核心）：

```ts
type DisplayMode = 'lesson_hub' | 'category_list' | 'unit_matrix'

function resolveDisplayMode(scheme, activeNode): DisplayMode {
  if (scheme.displayMode === 'unit_matrix' && isUnitMatrixNode(activeNode))
    return 'unit_matrix'
  if (scheme.displayMode === 'lesson_hub' && isLessonLeaf(activeNode))
    return 'lesson_hub'
  return 'category_list'
}
```

| 模式 | 右侧组件 | 学科网对应 |
|------|----------|------------|
| `lesson_hub` | `LessonGroupedList`（增强 subType） | 本课资源 |
| `category_list` | `CategoryResourceList`（新建） | 专辑/分类列表 |
| `unit_matrix` | `UnitMatrixPanel`（新建） | 按单元浏览教案 |

**SubjectDetailPage 简化**：

```text
CatalogSidebar + ResourcePanel（内 switch displayMode）
  保留 FilterBar（栏目）+ ResourceTypeBar（类型）
  移除 columnLayout=topic 时 demo 列表（brand 非空时）
```

**验收**：七彩点课文 → 分组列表；状元点「上课课件」→ 平铺列表；点「各单元完整教案」→ 八个单元 Tab。

### M4：上传与详情（2 周）

| 修改 | 说明 |
|------|------|
| `uploadRoute.ts` | brand, catalogNodeId, schemeId |
| `useResourceUploadForm.ts` | 挂载节点必选；课件版本/希沃 variant |
| `UploadStepPlacement.vue` | 树形选择挂载点（CatalogSidebar 复用） |
| `ResourceDetail.vue` | 多文件 Tab（主/答案/希沃） |
| `api/resources.ts` | `POST /api/resources` |

### M5：批量与运营

- 管理端脚本说明页（可选）：仅文档 + curl 示例
- 导入进度 Toast

### M6：学科网体验抛光（1 周）

| 项 | 说明 |
|----|------|
| 类型 Tab 角标 | `browseStats` → `ResourceTypeBar` |
| 空状态 | 统一 `EmptyState`，无 demo 回退 |
| 加载骨架 | 树与列表一致 |
| 滚动定位 | 切换课保持左树选中可见 |
| 收藏/分享 | query 带 `brand` |

---

## 6. 前后端契约（统一 BrowseContext）

### 6.1 URL Query（学科详情页）

```text
/subject/primary/chinese/tongbian2024
  ?brand=qicai
  &volume=y1s1
  &module=同步备课
  &type=课件
  &nodeId=12345          // catalog_node_id（M2+）
  &unit=我上学了         // 兼容旧链接，M3 可映射到 nodeId
  &lesson=我是中国人
  &mode=single
```

**兼容策略**：仅有 `unit`/`lesson` 无 `nodeId` 时，后端按 scheme + 名称解析 nodeId。

### 6.2 Browse API 响应（列表项）

```json
{
  "id": 10001,
  "title": "【七彩课堂】1 天地人 精品版课件",
  "brandCode": "qicai",
  "brandName": "七彩课堂",
  "type": "课件",
  "subType": "精品版",
  "module": "同步备课",
  "catalogPath": "/第一单元·识字/1 天地人",
  "files": [
    { "role": "main", "ext": "pptx", "ossUrl": "..." },
    { "role": "seewo", "ext": "ppt", "ossUrl": "..." }
  ],
  "uploadTime": "2026-05-18T10:00:00"
}
```

### 6.3 阶段发布顺序（避免前后端脱节）

```text
每周节奏示例：
  周一：合并 sql/2x 脚本， DBA 执行测试库
  周二-周三：后端 API + 单测
  周四-周五：前端对接 + feature flag
  周五：联调验收表勾选
```

---

## 7. 七彩 / 状元种子与映射

### 7.1 七彩（scheme=`textbook_unit`）

- 来源：`unitData.ts` → `29_seed_catalog_qicai_y1s1.sql`
- `node_type`：`unit` | `lesson` | `section`（单元导语、口语交际、习作、语文园地、单元复习）
- `meta.defaultModule`：`同步备课`；复习节点可标 `期中`/`期末`

### 7.2 状元（scheme=`zy_taxonomy`）

第一层节点（parent=0）：

| code | name | display_mode 继承 |
|------|------|-------------------|
| `class_ppt` | 一、上课课件 | category_list |
| `other_res` | 二、其他资源 | category_list |
| `lesson_plan` | 三、教案 | unit_matrix（子节点「各单元完整教案」） |
| `homework_ppt` | 四、作业课件 | category_list |

**上课课件** 子节点 → `meta.defaultType=课件`，`subType` 映射教案版/双减精华等。

**其他资源 → 备课资源** 8 项 → 各 `leaf` + `defaultType`。

**作业课件** → 资源两条 file：`main` + `answer`，或一条 resource 两个 file。

### 7.3 平台栏目与节点 meta（上传默认值）

| 栏目 module | 何时使用 |
|-------------|----------|
| 同步备课 | 七彩主路径；状元上课课件/教案 |
| 期中/期末 | 节点名含复习/检测卷 |
| 纯素材 | 教师工作包、计划总结 |

---

## 8. 联调与验收清单

### M1

- [ ] `brand` 切换后 list/page 仅该品牌
- [ ] 上传写入 `brand_code`
- [ ] URL 刷新保持 `brand`

### M2

- [ ] 七彩树节点数 ≥ unitData
- [ ] 状元树 4 层与文档一致
- [ ] 切换品牌 < 300ms 树刷新（有 loading）

### M3

- [ ] `nodeId` 子树列表正确
- [ ] 类型 Tab 角标与 stats 一致
- [ ] 旧 URL（仅 unit/lesson）可打开
- [ ] feature flag 关：旧 API 仍正常

### M4

- [ ] 多文件资源详情可下载/预览
- [ ] 宽表与 browse 数据一致（抽样 20 条）

### M5

- [ ] 批量导入 100 条幂等
- [ ] OSS 路径规范校验失败有明确错误

### M6

- [ ] 专题/考试布局无 demo 假数据（brand 已选）
- [ ] 与学科网流程对比走查通过（内部评审表）

---

## 9. 风险与回滚

| 风险 | 缓解 |
|------|------|
| 双写不一致 | M4 前以宽表为准；对账脚本比对 count |
| 树节点更名导致链接失效 | `node.code` 稳定不变；`name` 可改 |
| 状元/七彩串数据 | 强制 `brand_code` + scheme 隔离 |
| 工期膨胀 | 严格 M1–M3 先上线浏览；导入 M5 可后置 |

**回滚**：`VITE_USE_CATALOG_BROWSE=false`；browse API 下线后前端回 `primary-chinese`；DB 新表可保留不删。

---

## 附录 A：SubjectDetailPage 目标结构（M3 后）

```vue
<template>
  <div class="platform-page">
    <BrowseBreadcrumb :summary="browseSummary" />
    <SeriesFilterBar v-model="brandCode" />
    <FilterBar ... />  <!-- 学科 + 栏目 -->
    <div class="content-two-columns">
      <CatalogSidebar
        :scheme-id="schemeId"
        :tree="catalogTree"
        v-model:node-id="activeNodeId"
      />
      <div class="resource-list-area">
        <CourseCatalogHeader ... />  <!-- 版本/册别弹层，学科网上部 -->
        <ResourceTypeBar :stats="typeStats" />
        <AdvancedFilterBar ... />
        <ResourcePanel :mode="displayMode" ... />
      </div>
    </div>
  </div>
</template>
```

---

## 附录 B：建议 SQL 脚本编号（本方案）

| 脚本 | 阶段 |
|------|------|
| `27_brand_baseline.sql` | M1 |
| `28_catalog_scheme.sql` | M2 |
| `29_seed_catalog_qicai_y1s1.sql` | M2 |
| `30_seed_catalog_zhuangyuan.sql` | M2 |
| `31_resource_placement.sql` | M3 |
| `32_resource_pack.sql` | M5（可选） |
| `33_sync_wide_table_job.sql` | M4（存储过程或说明） |

---

## 附录 C：与旧文档关系

| 文档 | 关系 |
|------|------|
| [资源平台架构与多学科品牌设计方案.md](./资源平台架构与多学科品牌设计方案.md) | 架构原理、品牌映射 |
| [实施说明-P0-P1-P2.md](./实施说明-P0-P1-P2.md) | 旧接口验收；M3 后增补 browse 章节 |
| [数据库设计方案.md](./数据库设计方案.md) | 长期规范表；本计划 M4 对齐 |

---

## 修订记录

| 日期 | 说明 |
|------|------|
| 2026-05-20 | 初版：方案 A 分阶段 DB/后端/前端落地计划 |
