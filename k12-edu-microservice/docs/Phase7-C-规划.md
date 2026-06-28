# Phase 7-C · 首页专区 Admin 化

> **范围**：`home_panel_tab_config` Tab 配置 + `home_panel_featured` 置顶推荐  
> **原则**：复用现有 C 端查询引擎（`HomePanelService`），Admin 只做配置 CRUD  
> **权限**：复用 `admin:home:view` / `admin:home:edit`

---

## 一、与现有 C 端关系

| 表 | C 端 API（已有） | 7-C Admin |
|----|------------------|-----------|
| `home_panel_tab_config` | `GET /api/home/panels/*` | 列表 / 编辑 / 启停 |
| `home_panel_featured` | 同上（置顶合并进列表） | CRUD / 启停 |

C 端 **不改路由**；Admin 改配置后，首页三大专区即时生效。

---

## 二、Admin API

Base: `/api/admin/home/panels`

### Tab 配置

| Method | Path | 说明 |
|--------|------|------|
| GET | `/tabs?panelCode=&includeDisabled=true` | 列表 |
| PUT | `/tabs/{id}` | 更新 label/sort/模块 JSON/关键词 |
| PUT | `/tabs/{id}/status?status=0\|1` | 启停 |

### 置顶推荐

| Method | Path | 说明 |
|--------|------|------|
| GET | `/featured?panelCode=&tabKey=` | 列表（含资源标题） |
| POST | `/featured` | 新增（校验资源 status=1） |
| PUT | `/featured/{id}` | 更新 |
| PUT | `/featured/{id}/status` | 启停 |
| DELETE | `/featured/{id}` | 删除 |

### 预览

| Method | Path | 说明 |
|--------|------|------|
| GET | `/preview?panelCode=&tabKey=&stageKey=` | 调用 `HomePanelService` 返回前 N 条 |

---

## 三、Admin 子路由

| 路径 | 组件 |
|------|------|
| `/admin/home-config/panel-tabs` | `HomePanelTabs.vue` |
| `/admin/home-config/panel-featured` | `HomePanelFeatured.vue` |

---

## 四、验收标准

| # | 项 | 预期 |
|---|-----|------|
| C1 | Admin 列表 Tab | sync_prep 等配置可读 |
| C2 | 改 Tab 展示名 | 保存成功 |
| C3 | 禁用 Tab | C 端该 Tab 查询 400/无配置 |
| C4 | 置顶列表 | 含 resourceTitle |
| C5 | 无效资源 ID | POST featured 400 |
| C6 | preview | 返回 items ≥ 0 |
| C7 | C 端 panels API | 仍 200，置顶资源在首位 |

---

## 五、不做（7-C）

- 新建 Tab 行（uk 约束 + 种子已全，后续按需扩展）
- `home_func_channel` Admin（**7-E**）
- 最新内容三列（**7-D**）
