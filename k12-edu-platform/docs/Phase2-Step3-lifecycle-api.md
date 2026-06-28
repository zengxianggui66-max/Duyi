# Phase 2 第三步 — 上传资源生命周期 API 契约

## 状态模型

| 用户动作 | legacy `status` | `audit_status` | `publish_status` | 前台公开可见 |
|---------|-----------------|----------------|------------------|-------------|
| 保存草稿 | -1 | -1 | 0 | 否 |
| 提交审核 | 0 | 0 | 0 | 否 |
| 审核通过/上架 | 1 | 1 | 1 | 是 |
| 驳回 | 2 | 2 | 0 | 否 |

## C 端写入接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/primary-chinese/draft/save` | 保存/更新草稿（`status=-1`），可不传文件 |
| POST | `/api/primary-chinese/draft/{id}/submit` | 草稿提交审核（`-1 → 0`），服务端 `validateForSubmit` |
| POST | `/api/primary-chinese/draft/submit` | 聚合：saveDraft + submitDraft（请求体同 save） |
| DELETE | `/api/primary-chinese/draft/{id}` | 删除草稿 |
| POST | `/api/primary-chinese/rejected/{id}/clone-draft` | 驳回复制为新草稿 |

**已禁用：** `POST /api/primary-chinese/save` — 返回 400，提示走 draft/submit。

## 公开列表查询

`GET /api/primary-chinese/page`、`/list` 及 browse 路径：

- **无 `uploaderId`** 且 **未传 `status`** → 服务端默认 `status=1`（仅已发布）
- **有 `uploaderId`**（我的资源）→ 不传 `status` 时查 `status>=0`（含待审/驳回）

## 预览 / 文件安全字段（sql/83）

| 字段 | 值 | 含义 |
|------|-----|------|
| `preview_status` | 0/1/2/3 | 不可预览 / 可预览 / 待检测 / 失败 |
| `file_safety_status` | 0/1/2/3 | 未知 / 待检测 / 安全 / 风险 |

写入时机：`saveDraft`、`submitDraft` 调用 `ResourcePreviewSafetySync`。

## 搜索索引

`SearchDocumentSyncService.syncPrimaryById`：仅 `status=1` 入索引；草稿/待审会删除文档。

## 管理端

待审队列：`GET /api/admin/resources/pending`（提交审核后可见）。
