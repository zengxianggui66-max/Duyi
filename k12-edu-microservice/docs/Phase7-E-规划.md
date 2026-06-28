# Phase 7-E · 升学入口与特色频道 Admin

> **范围**：`home_func_channel` 升学顶栏 + `ops_channel*` 特色频道页  
> **权限**：复用 `admin:home:view` / `admin:home:edit`

---

## 一、数据表

| 表 | 说明 |
|----|------|
| `home_func_channel` | 顶栏升学入口（已有，7-E Admin 化） |
| `ops_channel` | 特色频道页元数据 |
| `ops_channel_tab` | 频道 Tab + 搜索关键词 |
| `ops_channel_featured_album` | 精品专辑区 |

种子：`sql/71_phase7e_ops_channel_schema.sql` + `sql/71_phase7e_ops_channel.sql`（由 `scripts/gen-phase7e-seed.mjs` 生成）

---

## 二、API

### C 端

| Method | Path | 说明 |
|--------|------|------|
| GET | `/api/home/func-channels` | 已有 |
| GET | `/api/channel/{code}` | 频道页 bootstrap |

### Admin

| Base | 说明 |
|------|------|
| `/api/admin/home/func-channels` | 升学入口列表/编辑/启停 |
| `/api/admin/operation/channels` | 特色频道 CRUD + Tab/专辑 |

---

## 三、Admin 路由

| 路径 | 组件 |
|------|------|
| `/admin/home-config/func-channels` | `HomeFuncChannels.vue` |
| `/admin/home-config/feature-channels` | `HomeFeatureChannels.vue` |

---

## 四、验收标准

| # | 项 | 预期 |
|---|-----|------|
| E1 | SQL 71 | 5 个 ops_channel 种子 |
| E2 | Admin func-channels | ≥5 条升学入口 |
| E3 | 改升学入口名称 | 保存成功 |
| E4 | Admin operation/channels | 5 条特色频道 |
| E5 | GET /api/channel/banhui | mainTabs ≥ 10 |
| E6 | 改频道 Tab 名 | 保存成功 |
| E7 | C 端 func-channels | 仍 200 |

---

## 五、不做（7-E）

- 特色频道资源列表查询引擎改造（仍走 search API）
- 导航注册表 `featureChannelRegistry.ts` 全量 Admin 化（仅 FeatureChannel 通用页）
