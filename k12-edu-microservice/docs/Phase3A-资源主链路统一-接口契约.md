# Phase 3A：资源主链路统一 - 接口契约（评审稿）

> 阶段名称：Phase 3 - Resource Main Chain Unification  
> 子阶段：Phase 3A 资源契约定稿  
> 适用范围：`k12-resource`、`k12-article`、`k12-edu-platform`

---

## 1. 目标与边界

### 1.1 目标

- 前台资源读取统一走 `/api/resources/*`。
- 管理端资源管理统一走 `/api/admin/resources/*`。
- 所有资源行为（浏览/下载/收藏/搜索）统一使用 `canonicalResourceId`。
- 旧接口进入兼容期，不再作为新业务主入口。

### 1.2 非目标

- 不在 Phase 3A 直接下线旧接口。
- 不在 Phase 3A 直接删除 `oss_primary_chinese_resource/topic_resource/culture_resource/competition_resource`。
- 不在 Phase 3A 改动页面 URL 结构。

---

## 2. 当前基线（来自现网代码）

- 管理端统一读接口已存在：`/api/admin/resource-main`（只读）。
- 管理端写接口仍在：`/api/admin/resources`（审核、上架、下架、推荐、批量）。
- 前台专区接口分散：`/api/topic/*`、`/api/culture/*`、`/api/competition/*`、`/api/resources/browse/*`、`/api/primary-chinese/*`。
- `resource_main` 当前仅覆盖 `source_type=primary_chinese`，未覆盖 topic/culture/competition/edu_resource。

---

## 3. 统一接口清单（To-Be）

## 3.1 前台统一读（Public Resource API）

### GET `/api/resources/page`

- **用途**：统一资源分页查询（学科/专题/文化/竞赛/资讯附件）。
- **核心 Query 参数**：
  - `contentDomain`
  - `stage`
  - `subject`
  - `edition`
  - `volumeId`
  - `catalogNodeId`
  - `module`
  - `resourceType`
  - `region`
  - `topicCategory`
  - `keyword`
  - `current`
  - `size`
  - `sort`

### GET `/api/resources/detail/{resourceId}`

- **用途**：统一资源详情查询。
- **说明**：`resourceId` 即 `canonicalResourceId`。

### GET `/api/resources/stats`

- **用途**：统一统计（总量、类型分布、频道分布）。
- **约束**：统计口径与 `/api/resources/page` 同筛选条件。

### GET `/api/resources/types`

- **用途**：返回可用资源类型与数量。
- **约束**：与 page/stats 同 scope。

### POST `/api/resources/{resourceId}/view`

- **用途**：统一记录浏览行为。

### POST `/api/resources/{resourceId}/download`

- **用途**：统一记录下载行为并返回可下载信息。

### POST `/api/resources/{resourceId}/collect`

- **用途**：统一收藏行为。

---

## 3.2 管理端统一写（Admin Resource API）

### GET `/api/admin/resources`

- **用途**：统一资源管理列表（替代按来源拆分的管理入口）。

### POST `/api/admin/resources`

- **用途**：统一创建资源（主链路写入）。

### PUT `/api/admin/resources/{resourceId}`

- **用途**：统一更新资源基础信息与挂载关系。

### POST `/api/admin/resources/{resourceId}/audit`

- **用途**：统一审核动作（通过/驳回）。

### POST `/api/admin/resources/{resourceId}/publish`

- **用途**：统一发布（上架）动作。

### POST `/api/admin/resources/{resourceId}/offline`

- **用途**：统一下架动作。

### POST `/api/admin/resources/{resourceId}/placement`

- **用途**：统一挂载（目录/频道/专题/首页板块）。

### POST `/api/admin/resources/batch`

- **用途**：统一批量动作（上架、下架、推荐、回收等）。

---

## 3.3 统一响应契约（关键字段）

### ResourceListItem（核心）

- `canonicalResourceId`: number
- `contentDomain`: string
- `sourceType`: string
- `title`: string
- `resourceType`: string
- `auditStatus`: number
- `publishStatus`: number
- `fileSafetyStatus`: number
- `catalogNodeId`: number | null
- `downloadCount`: number
- `viewCount`: number
- `uploadTime`: string

### ResourceDetail（核心）

- 包含 ListItem 全字段 + 文件详情 + 维度详情 + 挂载详情 + 来源追踪：
  - `files: ResourceFile[]`
  - `dimensions: ResourceDimension[]`
  - `placements: ResourcePlacement[]`
  - `legacySourceTable`
  - `legacySourceId`

---

## 4. 旧接口兼容策略

## 4.1 兼容期保留清单

- `/api/primary-chinese/*`
- `/api/resources/browse/*`
- `/api/topic/resources/page`
- `/api/culture/resources/page`
- `/api/competition/resources/page`

## 4.2 兼容规则

- 旧接口内部转调统一资源服务，不再直接访问各自旧查询逻辑。
- 响应结构保持旧格式，但数据源来自统一主链路。
- 所有旧接口必须带调用量日志标签（用于 3G 下线判定）。

---

## 5. 实施顺序（接口维度）

1. 新建统一读接口壳（不切流量）。
2. 接入统一查询服务，完成 `page/detail/stats/types`。
3. 新建统一行为接口（view/download/collect）并接入统一 ID。
4. 管理端写接口抽象为统一写网关（source adapter）。
5. 旧接口改内转发，前台逐页迁移到 `/api/resources/*`。

---

## 6. 验收口径（3A接口层）

- 同筛选条件下，`/api/resources/page.total` 与 `/api/resources/stats.total` 一致。
- `catalogNodeId` 查询不依赖课文中文名模糊匹配。
- `resourceId` 在详情/下载/收藏/搜索链路保持同一值（`canonicalResourceId`）。
- 管理端审核通过后不上架，发布后前台可见（双状态语义不回退）。

---

## 7. 风险与控制

- **风险**：旧接口与新接口统计口径不一致。  
  **控制**：统一走同一个 QueryAssembler + VisibilityPolicy。

- **风险**：管理端读写基址不一致导致联调混乱。  
  **控制**：Phase 3A 统一规范为 `/api/admin/resources/*`，`/api/admin/resource-main` 仅保留过渡读入口。

- **风险**：前台专区切换后出现字段缺失。  
  **控制**：先发布兼容映射层，再切页面请求。

