# Phase 3L · Domain 决策与双轨收敛（L-α）

> **范围**：主题班会 / 专题 / 资讯管理端 domain 边界 + 前台 canonical 路由  
> **依赖**：Phase 3J-2 内容中心 Shell、Phase 3K 表分层冻结  
> **L-α 交付**：路由 redirect + 本文档（不含 Admin CRUD 深化）

---

## 1. 双轨现状与 L-α 收敛

| 业务 | Canonical（内容页） | Legacy（入口壳） | L-α 动作 |
|------|---------------------|------------------|----------|
| 主题班会 | `/theme-class-meeting` | `/feature/banhui` | **redirect → canonical** |
| 专题资源 | `/topic` | `/feature/zhuanti` | **redirect → canonical** |
| 生涯规划 | `/topic`（专题域） | `/feature/shengya` | 已有 redirect |

**原则**：`ops_channel` 只负责入口配置（Tab / 精品专辑 / 关键词 / Banner 文案）；**列表与详情内容**走统一资源读路径（`resource_main` / 各 COMPAT 源 browse API）。

### 路由变更（L-α）

```typescript
// k12-edu-platform/src/router/index.ts
{ path: '/feature/banhui', redirect: '/theme-class-meeting' }
{ path: '/feature/zhuanti', redirect: '/topic' }
```

首页「最新专题」更多链接：`/feature/topic` → `/topic`。

### 后续 L-β（未在本 PR）

- `useFeatureChannel` 对 banhui/zhuanti 的 `loadData` 委托到 `useThemeClassMeeting` / `useTopicZone` 同源 API
- 废弃 legacy FeatureChannel 渲染这两类频道（redirect 后用户不可达，但代码仍保留给其他 type）

---

## 2. 主题班会 Domain 决策

### 2.1 结论：**不新建表**

| 方案 |  verdict |
|------|----------|
| 新建 `theme_class_meeting_*` 表 | ❌ 违反 3K PRIMARY 策略，增加 COMPAT 源 |
| 继续 `primary_chinese.module='主题班会'` | ⚠️ J3 过渡可用；新写入应迁 PRIMARY |
| **`edu_resource` + dimension + `resource_main`** | ✅ **终态** |

### 2.2 分层职责

```
ops_channel (code=banhui)
  ├── ops_channel_tab        → 前台 Tab 名 + search_keyword
  └── ops_channel_featured_album → 精品专辑入口（标题/排序/封面）

edu_resource (PRIMARY)
  └── edu_resource_dimension.module_id → edu_module「主题班会」

resource_main (PRIMARY)
  └── 统一列表 / Admin 资源中心 / 全站搜索索引
```

### 2.3 常量约定

| 键 | 值 | 用途 |
|----|-----|------|
| `THEME_CLASS_MEETING_MODULE` | `主题班会` | browse `module` 参数（J3 已用） |
| `THEME_CLASS_MEETING_MODULE_KEY` | `theme_class_meeting` | Admin 筛选 / 搜索 doc_type（3L-β 引入） |
| `ops_channel.code` | `banhui` | 频道 bootstrap |

### 2.4 Admin 路径（3L-γ 目标）

| 操作 | 路径 |
|------|------|
| Tab / 专辑配置 | 频道中心 → 功能入口频道（`code=banhui`） |
| 班会资源 CRUD | 资源中心 → `sourceType=edu_resource` + module=主题班会 |
| 内容中心 Tab | `/admin/content/theme-class-meeting`（跳转 + 只读预览 → 3L-γ 升级 CRUD） |

### 2.5 与 3K-β 联动

写入 `edu_resource` 后应触发 `ResourceMainUpsertService`，否则 `verify_phase3_resource_main_chain.sql` T3 覆盖率漂移。

---

## 3. 专题 Domain（与班会对称）

| 层 | 职责 |
|----|------|
| `topic_resource` + `TopicAlbum` | COMPAT 源；`TopicOps` 专辑 CRUD（3J-2 已有） |
| `ops_channel` (`code=zhuanti`) | 入口 Tab / 专辑 |
| `/topic` + `useTopicZone` | Canonical 前台；`topicApi` 专用读 API |
| `resource_main` | 跨源 Admin 列表 `sourceType=topic_resource` |

**L-α**：仅 redirect；L-β 令 `/feature/zhuanti` 与 `/topic` 列表 API 一致。

---

## 4. 资讯 Domain（推翻 Phase7-D 剩余约束）

Phase7-D §六原声明「资讯正文不做 Admin CRUD」——**3J-4 已实现** `AdminArticleController` + `NewsOps`。

### 4.1 3K 边界（不变）

- 资讯正文留 **`article` 表 + k12-article**；**不**映射 `resource_main` / `edu_resource`（见 `Phase3K-表分层冻结清单.md` §4）
- 搜索：`SearchDocumentSyncService.syncArticleById` → `sys_search_document`

### 4.2 3L-γ 待补（非 L-α）

| 能力 | 现状 | 目标 |
|------|------|------|
| 审核 | status 0/1，创建默认发布 | 待审核(2) + 通过/驳回 |
| 频道 | 前端静态 `newsZone.ts` | DB 或 Admin 可配置 |
| 置顶/精选 | 表单已有 | 列表快捷操作 |

---

## 5. L-α 验收

```bash
cd k12-edu-microservice
node scripts/phase3l-alpha-acceptance.mjs
```

| ID | 预期 |
|----|------|
| Lα1 | `router/index.ts` 含 banhui/zhuanti redirect |
| Lα2 | `LatestContent.vue` morePath=`/topic` |
| Lα3 | `featureChannelRegistry` 班会 path=`/theme-class-meeting` |
| Lα4 | domain doc 存在 |

---

## 6. 里程碑顺序

```
L-α  redirect + domain doc     ← 本 PR
L-β  composable 数据源收敛
L-γ  ThemeClassMeetingOps CRUD + 资讯审核
```
