# Phase 7-B · 热门词 × 搜索提示联动

> **前置**：Phase 7-A 已完成 `home_hot_word` 表、Admin CRUD、顶栏热门词 API  
> **范围**：搜索框 suggest 接入运营热词 + Admin 批量排序  
> **原则**：运营词优先于统计热搜；`/api/search/hot-keywords` 统计榜 **不改**

---

## 一、与 7-A 的分工

| 能力 | 7-A | 7-B |
|------|-----|-----|
| `home_hot_word` 表 + 种子 | ✅ | — |
| Admin CRUD `/hot-words` | ✅ | 增强：批量排序 |
| 顶栏「热门：」行 | ✅ | — |
| 搜索框 **空输入 suggest** | 统计 `search_hot_keyword` | **运营词优先** |
| suggest 热词 **browse/search 跳转** | — | ✅ `navTarget` |
| 与统计热搜关系 | — | **互不影响** |

---

## 二、后端改动

### 2.1 Search suggest

`SearchServiceImpl.buildSuggest`：

1. 空输入：history → **ops hot words** → stats hot keywords  
2. 有输入：原有匹配逻辑 → 末尾追加 ops/stats 热词（label 包含 keyword）

`SearchSuggestItemVO` 新增 `navTarget`（来自 `home_hot_word`）。

### 2.2 Admin 批量排序

```http
PUT /api/admin/home/hot-words/reorder
{ "items": [ { "id": 1, "sort": 40 }, { "id": 2, "sort": 30 } ] }
```

权限：`admin:home:edit`

---

## 三、前端改动

| 文件 | 改动 |
|------|------|
| `api/search.ts` | `SearchSuggestItem.navTarget`；`resolveSuggestionRoute` 优先解析 navTarget |
| `AppHeader.vue` | suggest 热词点击走 `resolveHotWordNavigation` |
| `HomeHotWords.vue` | ↑↓ 调整顺序 +「保存排序」 |

---

## 四、验收标准

| # | 项 | 预期 |
|---|-----|------|
| B1 | `GET /api/search/suggest?q=` | 热词组含运营词（如「一年级语文」）且带 `navTarget` |
| B2 | Admin 调整排序 | suggest 与顶栏顺序同步变化 |
| B3 | `GET /api/search/hot-keywords` | 仍返回统计榜，不受运营词影响 |
| B4 | 点击 suggest 中 browse 热词 | 进学科浏览页 |
| B5 | 点击 suggest 中 search 热词 | 带 keyword 进浏览/搜索 |

---

## 五、实施顺序

1. 后端 suggest + reorder API  
2. 前端 suggest 跳转 + Admin 排序 UI  
3. `scripts/phase7b-acceptance-test.mjs`  
4. `Phase7-B-验收.md`

**下一步**：Phase 7-C（`home_panel_tab_config` / `home_panel_featured` Admin 化）
