# Phase 2 Step 2 — 上传页 catalog/tree 参数规则

> 阶段 1 交付：明确 `/api/catalog/tree` 在上传场景的调用约定（阶段 3 前端切换时遵循）

## 1. API

```
GET /api/catalog/tree
```

实现：`CatalogController` → `CatalogService.getTree`

## 2. 参数说明

| 参数 | 必填 | 说明 |
|------|------|------|
| `schemeCode` | 推荐 | 目录方案编码。同步教材单元树默认 `textbook_unit` |
| `schemeId` | 可选 | 方案 ID，与 `schemeCode` 二选一 |
| `volumeKey` | 可选 | 册别 key，如 `y1s1`（一年级上册）。有 DB 节点时优先匹配 |
| `gradeName` | 推荐 | 教材册别显示名，如 `一年级上册`。与上传表单 `formData.gradeName` 一致 |
| `edition` | 推荐 | 教材版本名，如 `统编版(2024)`。无 DB 节点时用于 legacy JSON 兜底 |
| `subject` | 推荐 | 学科**中文名**，如 `语文`（与上传 `subjectLabel` 一致） |

## 3. 推荐调用顺序（上传页）

1. 用户选定：**学段** → **学科** → **栏目** → **版本** → **册别（gradeName）**
2. 调用 `GET /api/catalog/schemes?stage=&subject=`（可选，解析默认 scheme）
3. 调用 `GET /api/catalog/tree`：
   ```http
   GET /api/catalog/tree?schemeCode=textbook_unit&gradeName=一年级上册&edition=统编版(2024)&subject=语文
   ```
4. 用户选择单元/课文节点 → 写入 `catalogNodeId`、`unitName`、`lessonName`、`catalogPath`

## 4. 解析优先级（服务端）

1. 按 `schemeId` / `schemeCode` 定位 `edu_catalog_scheme`
2. 在 `edu_catalog_node` 中按 `volumeKey`、`gradeName`、`subject` 过滤
3. 有节点 → 返回 DB 树（仅 `status=1`）
4. 无节点且 `schemeCode=textbook_unit` → `UnitCatalogStaticLoader` JSON 兜底
5. 仍无 → 空数组（前端提示「暂无目录，请联系管理员配置」）

## 5. 与旧接口关系

| 旧 | 新 | 迁移 |
|----|-----|------|
| `GET /api/primary-chinese/unit-tree` | `GET /api/catalog/tree` | 阶段 3 上传页切换 |
| `getUploadFilterOptions` DISTINCT | `GET /api/taxonomy/*` | 阶段 1 已纯 taxonomy |

## 6. 校验（阶段 1）

上传保存时若带 `catalogNodeId`：

- 节点必须存在（`edu_catalog_node`）
- `status` 必须为 `1`（启用）

禁用节点不出现在 C 端树中，且提交时返回 400。

## 7. volumeKey 推导（前端辅助）

当 taxonomy volume 返回 `code`（如 `grade1_up`）时，可映射为 catalog `volumeKey`（如 `y1s1`）。  
阶段 3 实现时统一在 `catalogSource.ts` 维护 **volume code → volumeKey** 映射表，避免各处硬编码。

## 8. 验收用例（阶段 3 手工）

| 场景 | 参数 | 预期 |
|------|------|------|
| 小学语文一上统编 | gradeName=一年级上册, subject=语文, edition=统编版 | 返回单元列表 |
| 切换版本 | 仅改 edition | 树刷新 |
| 禁用单元 | 管理端禁用某节点 | 树不含该节点；带 id 提交 400 |
