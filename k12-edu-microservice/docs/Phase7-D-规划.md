# Phase 7-D · 最新内容三列

> **范围**：首页「最新资料 / 最新专题 / 最新资讯」三列配置化  
> **原则**：material/topic 走资源规则或手工清单；news 列继续调 `k12-article`  
> **权限**：复用 `admin:home:view` / `admin:home:edit`

---

## 一、与现有 C 端关系

| 列 | 当前 | 7-D 目标 |
|----|------|----------|
| 最新资料 | `LatestContent.vue` 静态 | `home_latest_column` + rule 查询 |
| 最新专题 | 静态 | rule 查询（module/类型） |
| 最新资讯 | 部分接 `newsApi.getHome()` | 列元数据 Admin 化，数据仍走 article |

---

## 二、数据表

### `home_latest_column`

| 字段 | 说明 |
|------|------|
| column_key | material / topic / news（唯一） |
| title | 列标题 |
| more_path | 「更多」跳转路径 |
| data_source | rule / manual / api |
| rule_json | rule 模式下的查询条件 |

### `home_latest_item`（manual 模式）

手工维护标题、日期、资源 ID、资讯 ID 或自定义链接。

---

## 三、API

### C 端

| Method | Path | 说明 |
|--------|------|------|
| GET | `/api/home/latest-columns?stageKey=primary` | 返回启用列及条目（api 列 items 为空，前台自行拉资讯） |

### Admin

Base: `/api/admin/home/latest-columns`

| Method | Path | 说明 |
|--------|------|------|
| GET | `/` | 列配置列表 |
| PUT | `/{id}` | 更新列（标题/更多链接/数据源/rule） |
| PUT | `/{id}/status` | 启停 |
| GET | `/{id}/preview` | 预览解析结果 |
| GET | `/{id}/items` | manual 条目列表 |
| POST | `/{id}/items` | 新增 manual 条目 |
| PUT | `/items/{itemId}` | 更新条目 |
| DELETE | `/items/{itemId}` | 删除条目 |

---

## 四、Admin 子路由

| 路径 | 组件 |
|------|------|
| `/admin/home-config/latest-columns` | `HomeLatestColumns.vue` |

---

## 五、验收标准

| # | 项 | 预期 |
|---|-----|------|
| D1 | 执行 sql/70 | 三列种子 |
| D2 | Admin 列表 | material/topic/news 可读 |
| D3 | 改列标题 | 保存成功 |
| D4 | material preview | rule 查询 items ≥ 0 |
| D5 | C 端 latest-columns | 200，含 3 列 |
| D6 | 禁用列 | C 端该列不出现 |

---

## 六、不做（7-D）

- 资讯正文 Admin CRUD（仍走 article 模块）
- OpenSearch 规则引擎（**7-F**）
- 特色频道（**7-E**）
