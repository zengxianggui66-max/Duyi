# Phase 3D/3E 执行与验收清单

## 目标

- 3D：统一资源读写能力可用（查询、详情、预览、下载、浏览、收藏、统计、类型）
- 3E：管理端资源主链写能力补齐，支持内容中心统一入口继续收口

## 本次落地

### 3D（后端统一资源服务）

- 新增服务：`ResourceMainService` / `ResourceMainServiceImpl`
- 新增接口：`ResourceMainController`（`/api/resources`）
  - `GET /api/resources/page`
  - `GET /api/resources/detail/{resourceId}`
  - `GET /api/resources/stats`
  - `GET /api/resources/types`
  - `GET /api/resources/{resourceId}/preview`
  - `POST /api/resources/{resourceId}/view`
  - `POST /api/resources/{resourceId}/download`
  - `POST /api/resources/{resourceId}/collect`
- 预览策略：
  - 优先 `edu_resource_file` 主文件
  - 外链资源直接返回 external 预览
  - 预览失败自动入 `preview_fail_queue`

### 3E（管理端统一 + 内容中心收口基础）

- 扩展 `AdminResourceMainController` 写接口（基于 globalId 路由）：
  - `PUT /api/admin/resource-main/{globalId}`
  - `POST /api/admin/resource-main/{globalId}/audit`
  - `POST /api/admin/resource-main/{globalId}/publish`
  - `POST /api/admin/resource-main/{globalId}/offline`
  - `POST /api/admin/resource-main/{globalId}/placement`
  - `POST /api/admin/resource-main/batch`
  - `POST /api/admin/resource-main/batch-audit`
- 当前写路径先落在 `primary_chinese`（其余 sourceType 返回明确提示，避免误写）
- 扩展 DTO：`AdminResourceUpdateDTO` 支持 `catalogNodeId`、`catalogPath`

## 验收步骤

```bash
cd k12-edu-microservice
node scripts/phase3d-3e-acceptance-test.mjs
```

## 通过标准

- `/api/resources/*` 核心接口可访问并返回成功结构
- `preview` 可返回可消费结构（含 external / 转码策略）
- `collect` 在登录态可执行
- `/api/admin/resource-main` 写接口可路由并执行（primary_chinese）
