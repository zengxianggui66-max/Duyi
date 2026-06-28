# Phase 7-A · 首页运营配置（Banner / 快捷入口 / 热门词）

> **范围**：首屏轮播、快捷功能卡片、顶栏热门词  
> **原则**：与 Phase 5 taxonomy、Phase 5-F subject-nav、现有 home_panel / func_channel **同一套导航语义**；**不依赖 OpenSearch**  
> **OpenSearch**：按 [P3 OpenSearch 启用手册](./P3%20OpenSearch%20启用手册.md) **暂不启用**；7-A 只走 MySQL 配置 + 现有 `/api/search/all`（`sys_search_document`）

---

## 一、现状与 7-A 边界

| 区块 | 当前 | 7-A 目标 |
|------|------|----------|
| 轮播 Banner | `HomePage.vue` 静态 `bannerList` | `home_banner` + Admin CRUD |
| 快捷功能 | 静态 `featureCards` | `home_quick_entry` + Admin CRUD |
| 顶栏热门词 | `hotWordActions.ts` 静态 | `home_hot_word` + Admin CRUD |
| 左栏学科浮层 | Phase 5-F ✅ | 不改 |
| 同步/试卷/升学专区 | `home_panel_tab_config` + API ✅ | **7-C**（Admin 化现有表） |
| 顶栏升学入口 | `home_func_channel` + API ✅ | **7-E**（Admin 化） |
| 全站搜索 | `sys_search_document` + MySQL `/api/search/all` | **不改动引擎**；热门词「搜索型」仅调现有搜索 API |
| OpenSearch | 代码在、默认 `hosts=""` 未激活 | **7-A 不接入**；预留 `search_engine` 字段供 7-F |

### 不做（7-A）

- OpenSearch 集群部署 / 全量 sync / 影子对比
- Panel 置顶位、func-channel、最新内容三列（后续子阶段）
- 修改 `sys_search_document` 索引结构

---

## 二、统一导航模型 `nav_target`（可扩展）

Banner、快捷入口、热门词 **共用同一 JSON 契约**，与 `hotWordActions.ts`、`buildSubjectBrowseRoute`、`resolveHotWordNavigation` 对齐。

```json
{
  "type": "browse",
  "stageKey": "primary",
  "subjectKey": "chinese",
  "versionKey": "tongbian2024",
  "volumeName": "一年级上册",
  "query": {
    "module": "同步备课",
    "type": "课件",
    "brand": "qicai"
  }
}
```

| type | 含义 | 必填字段 | C 端行为 |
|------|------|----------|----------|
| `browse` | 学科资源浏览 | stageKey + subjectKey + versionKey（可默认） | `buildSubjectBrowseRoute` |
| `search` | 范围内标题搜索 | keyword；可选 stage/subject/module 缩小范围 | 带 query 进浏览页 **或** `/search?q=`（MySQL） |
| `route` | 站内固定路由 | `routePath` | `router.push(routePath)` |
| `external` | 外链 | `externalUrl` | 新窗口/当前窗（`openInNewTab`） |
| `scroll` | 首页锚点 | `scrollTarget`（如 `exam-module`） | 滚动 + 可选切换 func |
| `vip` | 会员页 | — | `/vip` |

**扩展预留（7-F / OpenSearch 期）**

```json
{
  "searchEngine": "mysql",
  "searchScope": { "docTypes": ["resource"], "stageKey": "primary" }
}
```

- `searchEngine`: `mysql`（默认）| `auto`（将来：有 OS 则 OS，否则 MySQL）
- 7-A 实现只读 `mysql`，忽略 `auto` 时仍走 MySQL

---

## 三、SQL/69 字段清单

> 脚本文件：`sql/69_phase7a_home_ops.sql`（DDL + 种子，幂等）

### 3.1 `home_banner` — 首页轮播

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT UNSIGNED PK AI | |
| slot_code | VARCHAR(32) NOT NULL DEFAULT 'home_hero' | 投放位；预留多位置 |
| title | VARCHAR(100) NOT NULL | 主标题 |
| subtitle | VARCHAR(200) DEFAULT NULL | 副标题/描述 |
| cta_text | VARCHAR(30) DEFAULT NULL | 按钮文案「立即查看」 |
| icon | VARCHAR(16) DEFAULT NULL | emoji 或图标 key |
| bg_color_start | CHAR(7) DEFAULT '#667EEA' | 渐变起 |
| bg_color_end | CHAR(7) DEFAULT '#764BA2' | 渐变止 |
| image_url | VARCHAR(500) DEFAULT NULL | 可选背景图（将来替换纯渐变） |
| nav_target | JSON NOT NULL | 统一跳转（见 §二） |
| stage_keys | JSON DEFAULT NULL | 可见学段 code 数组；NULL=全学段 |
| start_time | DATETIME DEFAULT NULL | 定时上线 |
| end_time | DATETIME DEFAULT NULL | 定时下线 |
| sort | SMALLINT DEFAULT 0 | 越大越靠前 |
| status | TINYINT DEFAULT 1 | 0 禁用 1 启用 |
| remark | VARCHAR(200) DEFAULT NULL | 运营备注 |
| create_time / update_time | DATETIME | |

**索引**：`uk_slot_sort` 不必；`idx_slot_status_sort (slot_code, status, sort)`

---

### 3.2 `home_quick_entry` — 快捷功能卡片

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT UNSIGNED PK AI | |
| entry_key | VARCHAR(32) NOT NULL | 稳定 key：`courseware/prepare/exam…` |
| title | VARCHAR(50) NOT NULL | 卡片标题 |
| description | VARCHAR(100) DEFAULT NULL | 副文案 |
| icon | VARCHAR(16) DEFAULT NULL | emoji |
| accent_color | CHAR(7) DEFAULT '#4facfe' | 卡片强调色 |
| nav_target | JSON NOT NULL | 统一跳转 |
| stage_keys | JSON DEFAULT NULL | 学段过滤 |
| sort | SMALLINT DEFAULT 0 | |
| status | TINYINT DEFAULT 1 | |
| remark | VARCHAR(200) DEFAULT NULL | |
| create_time / update_time | DATETIME | |

**索引**：`UNIQUE uk_entry_key (entry_key)`

---

### 3.3 `home_hot_word` — 顶栏热门词（运营配置）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT UNSIGNED PK AI | |
| label | VARCHAR(50) NOT NULL | 展示文案（顶栏 tag） |
| action_type | VARCHAR(16) NOT NULL | `browse` \| `search`（与 nav_target.type 一致） |
| nav_target | JSON NOT NULL | browse/search 参数 |
| badge | VARCHAR(16) DEFAULT NULL | 可选角标「热」「新」 |
| stage_keys | JSON DEFAULT NULL | 仅在对应学段顶栏展示；NULL=始终展示 |
| sort | SMALLINT DEFAULT 0 | 展示顺序 |
| status | TINYINT DEFAULT 1 | |
| start_time / end_time | DATETIME | 可选排期 |
| remark | VARCHAR(200) DEFAULT NULL | |
| create_time / update_time | DATETIME | |

**索引**：`UNIQUE uk_label (label)`（或 uk_label+slot 若将来分位置）

---

### 3.4 与现有搜索表的关系（不合并）

| 表 | 职责 | 7-A 关系 |
|----|------|----------|
| `home_hot_word` | **运营配置**展示顺序与跳转 | 7-A 新建；C 端顶栏 **优先读此表** |
| `search_hot_keyword` | **统计热搜**（search_count） | 保留；`/api/search/hot-keywords` 可作补充源 |
| `sys_search_document` | **可搜文档索引**（MySQL FT） | 搜索型热词仍调 `/api/search/all`；**不等待 OpenSearch** |
| `search_engine_shadow_log` | OS 影子对比 | 7-A 不写入 |

**推荐 C 端策略**

1. `GET /api/home/hot-words` → 仅 `home_hot_word`（运营可控）
2. `GET /api/search/hot-keywords` → 保持现状（统计榜）；搜索页侧边可用
3. 二者 **不强制合并**，避免运营词被统计词覆盖

---

### 3.5 权限种子（69 号 SQL 追加）

已有 `admin:home:view` / `admin:home:edit`（sql/49、51），7-A **复用**，不新增权限码。

可选（7-F）：`admin:home:publish` 发布审核流。

---

## 四、Admin 子路由表

父菜单保持 **首页配置** `/admin/home-config`（`admin:home:view`）。

| 子路由 | 组件 | 权限 | 说明 |
|--------|------|------|------|
| `/admin/home-config` | redirect → `/admin/home-config/banners` | view | 默认进轮播 |
| `/admin/home-config/banners` | `HomeBanners.vue` | view / edit | 轮播 CRUD + 预览 |
| `/admin/home-config/quick-entries` | `HomeQuickEntries.vue` | view / edit | 快捷入口 CRUD |
| `/admin/home-config/hot-words` | `HomeHotWords.vue` | view / edit | 热门词 CRUD + action 类型 |
| `/admin/home-config/preview` | `HomePreview.vue`（可选） | view | 聚合预览首屏 |

**后续子阶段（非 7-A）**

| 子路由 | 阶段 | 数据源 |
|--------|------|--------|
| `/admin/home-config/panels` | 7-C | `home_panel_tab_config` |
| `/admin/home-config/featured` | 7-C | `home_panel_featured` |
| `/admin/home-config/func-channels` | 7-E | `home_func_channel` |
| `/admin/home-config/latest-columns` | 7-D | 新表或配置 JSON |

**Sidebar 结构建议**

```
首页配置
  ├─ 轮播 Banner        (7-A)
  ├─ 快捷入口           (7-A)
  ├─ 热门词             (7-A)
  ├─ 专区 Tab           (7-C, disabled)
  ├─ 置顶推荐           (7-C, disabled)
  └─ 顶栏升学入口       (7-E, disabled)
```

---

## 五、OpenAPI 草案

> 服务：`k12-resource`（C 端 `/api/home/**`）、Admin（`/api/admin/home/**`）  
> 网关：9001；白名单与现 `home_panel`、`subject-nav` 一致

### 5.1 C 端 — 只读

#### `GET /api/home/banners`

Query: `slotCode=home_hero`（默认）, `stage=primary`（可选，按 stage_keys 过滤）

```yaml
responses:
  200:
    content:
      application/json:
        schema:
          type: object
          properties:
            code: { type: integer, example: 200 }
            data:
              type: array
              items:
                $ref: '#/components/schemas/HomeBannerVO'
```

#### `GET /api/home/quick-entries`

Query: `stage=primary`（可选）

Response: `HomeQuickEntryVO[]`

#### `GET /api/home/hot-words`

Query: `stage=primary`（可选）

Response: `HomeHotWordVO[]`

#### `GET /api/home/hero`（可选聚合，减少首屏请求）

Response:

```json
{
  "banners": [],
  "quickEntries": [],
  "hotWords": []
}
```

---

### 5.2 Admin — CRUD

Base: `/api/admin/home`  
Header: `Authorization: Bearer …`  
Permission: `admin:home:view`（GET）/ `admin:home:edit`（POST/PUT/DELETE）

| Method | Path | 说明 |
|--------|------|------|
| GET | `/banners?includeDisabled=true` | 列表 |
| POST | `/banners` | 新增 |
| PUT | `/banners/{id}` | 更新 |
| PUT | `/banners/{id}/status?status=0\|1` | 启停 |
| DELETE | `/banners/{id}` | 软删或硬删（建议 status=0） |
| GET/POST/PUT/… | `/quick-entries` | 同上模式 |
| GET/POST/PUT/… | `/hot-words` | 同上模式 |

**Write DTO 示例 `HomeBannerWriteDTO`**

```yaml
HomeBannerWriteDTO:
  required: [title, navTarget]
  properties:
    slotCode: { type: string, default: home_hero }
    title: { type: string, maxLength: 100 }
    subtitle: { type: string }
    ctaText: { type: string }
    icon: { type: string }
    bgColorStart: { type: string, pattern: '^#[0-9A-Fa-f]{6}$' }
    bgColorEnd: { type: string }
    imageUrl: { type: string, format: uri }
    navTarget: { $ref: '#/components/schemas/NavTarget' }
    stageKeys: { type: array, items: { type: string } }
    startTime: { type: string, format: date-time }
    endTime: { type: string, format: date-time }
    sort: { type: integer }
    status: { type: integer, enum: [0, 1] }
    remark: { type: string }
```

---

### 5.3 共享 Schema

```yaml
components:
  schemas:
    NavTarget:
      type: object
      required: [type]
      properties:
        type:
          type: string
          enum: [browse, search, route, external, scroll, vip]
        routePath: { type: string }
        stageKey: { type: string }
        subjectKey: { type: string }
        versionKey: { type: string }
        volumeName: { type: string }
        keyword: { type: string }
        scrollTarget: { type: string }
        externalUrl: { type: string, format: uri }
        openInNewTab: { type: boolean, default: false }
        query:
          type: object
          additionalProperties: { type: string }
        searchEngine:
          type: string
          enum: [mysql, auto]
          default: mysql
          description: 7-A 仅实现 mysql；auto 预留 OpenSearch
        searchScope:
          type: object
          description: 预留分面/文档类型过滤

    HomeBannerVO:
      allOf:
        - type: object
          properties:
            id: { type: integer, format: int64 }
            slotCode: { type: string }
            title: { type: string }
            subtitle: { type: string }
            ctaText: { type: string }
            icon: { type: string }
            bgColorStart: { type: string }
            bgColorEnd: { type: string }
            imageUrl: { type: string }
            sort: { type: integer }
        - properties:
            navTarget: { $ref: '#/components/schemas/NavTarget' }

    HomeQuickEntryVO:
      type: object
      properties:
        id: { type: integer }
        entryKey: { type: string }
        title: { type: string }
        description: { type: string }
        icon: { type: string }
        accentColor: { type: string }
        navTarget: { $ref: '#/components/schemas/NavTarget' }
        sort: { type: integer }

    HomeHotWordVO:
      type: object
      properties:
        id: { type: integer }
        label: { type: string }
        actionType: { type: string, enum: [browse, search] }
        badge: { type: string }
        navTarget: { $ref: '#/components/schemas/NavTarget' }
        sort: { type: integer }
```

---

## 六、前端切源（7-A 实施时）

| 文件 | 改动 |
|------|------|
| `HomePage.vue` | `bannerList` / `featureCards` → API |
| `AppHeader.vue` | `HOT_WORD_LABELS` → `GET /api/home/hot-words` |
| `resolveHotWordNavigation.ts` | 改为消费 `HomeHotWordVO.navTarget`（结构与现 `HotWordAction` 同构） |
| `HomeConfig.vue` | 拆子路由 + 三 Tab 表单（nav_target 可视化编辑器） |

**降级**：`VITE_USE_HOME_OPS_API=false` 时回退现有静态常量（对齐 Phase 5-E `taxonomySource` 模式）。

---

## 七、与 OpenSearch 的分层（充分规划）

```
┌─────────────────────────────────────────────────────────┐
│  Layer A · 首页运营 CMS（7-A）                             │
│  home_banner / home_quick_entry / home_hot_word         │
│  → 纯 MySQL，与搜索引擎无关                               │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼ 热门词 type=search
┌─────────────────────────────────────────────────────────┐
│  Layer B · 搜索运行时（现状，继续维护）                    │
│  sys_search_document + /api/search/all (MySQL FULLTEXT) │
│  search_hot_keyword（统计）                              │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼ 数据量 1万+ / P95 升高后再启用
┌─────────────────────────────────────────────────────────┐
│  Layer C · OpenSearch（P3 已预埋，默认 OFF）             │
│  hosts 为空 → noop；配置后 sync 自 sys_search_document   │
│  NavTarget.searchEngine=auto 时再切换                    │
└─────────────────────────────────────────────────────────┘
```

**统一性保证**

- 所有「可点击运营位」均用 `NavTarget`，后端 `HomeNavTargetResolver` 单点解析 → 与 `SubjectDetail` 路由守卫兼容
- Taxonomy 改名后：browse 型热词存 **code**（stageKey/subjectKey），展示名从 API 读 taxonomy
- 资源上架仍写 `sys_search_document`（现有钩子），**不因 7-A 增加 OS 依赖**

---

## 八、验收标准（7-A）

| # | 项 | 预期 |
|---|-----|------|
| A1 | 执行 sql/69 | 三表 + 种子（迁移自现静态配置） |
| A2 | Admin 改 Banner 标题 | 首页轮播即时更新 |
| A3 | Admin 禁用某快捷入口 | C 端卡片消失 |
| A4 | Admin 新增 browse 型热词 | 顶栏点击进学科浏览页 |
| A5 | Admin 新增 search 型热词 | 走 MySQL `/api/search/all`，**无需 OpenSearch** |
| A6 | 无效 nav_target | Admin 保存 400；C 端不展示 |
| A7 | OpenSearch hosts 为空 | 7-A 功能全部正常 |

---

## 九、实施顺序建议

1. `sql/69_phase7a_home_ops.sql` + 种子（从现 `bannerList` / `featureCards` / `HOT_WORD_ACTIONS` 导入）
2. 后端 `HomeOpsReadService` + Admin CRUD + `NavTargetResolver`
3. Admin 三子页 + 子路由
4. C 端 `HomePage` / `AppHeader` 切源 + feature flag 降级
5. `scripts/phase7a-acceptance-test.mjs`
6. 文档 `Phase7-A-验收.md`

**与 Phase 5-F**：可并行；7-A 不涉及 subject-nav。
