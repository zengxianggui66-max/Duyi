# Phase 9-D 验收：内容运营驾驶舱

## 范围

- 增强 `/admin/dashboard` 为「内容运营驾驶舱」
- **用户增长**：k12-auth `user` 表日聚合 → `GET /api/admin/analytics/users`
- **资源/互动/审核/分布/排行**：k12-resource MySQL 聚合（带 `scopedWrapper` 数据范围）→ `GET /api/admin/analytics/dashboard`
- 权限：`admin:analytics:view`（Phase 9-E 迁移；旧 `admin:dashboard:view` 仅前端 `hasAnalyticsView()` 兼容）
- **不做**：会员转化、付费下载、订单 GMV、DAU 留存

## 部署

```bash
mvn install -pl k12-common,k12-auth,k12-resource,k12-gateway -am -DskipTests
# 重启 gateway(9001)、auth(8081)、resource(8082)
```

网关需将 `/api/admin/analytics/users` 路由到 **8081**，其余 `/api/admin/analytics/*` 走 **8082**。

## API

| 方法 | 路径 | 服务 | 权限 |
|------|------|------|------|
| GET | `/api/admin/analytics/users?days=30` | auth | `admin:analytics:view` |
| GET | `/api/admin/analytics/dashboard?days=7` | resource | `admin:analytics:view` |

`days` 范围：7～90（默认 dashboard=7，users=30）。

### Dashboard 响应要点

| 字段 | 说明 |
|------|------|
| `summary` | 资源总量、待审、已发布、下载/浏览/收藏累计、周期内新增 |
| `resourceUploadTrend` | 日新增 + 累计 |
| `actionTrend` | 近 N 天 view/download/collect（`user_resource_action` + `collection`） |
| `audit` | 近 N 天 approve/reject 与通过率 |
| `stageDistribution` / `subjectDistribution` | 学段/学科分布 |
| `topByDownload/View/Collect` | Top 10，可跳转资源详情 |
| `topUploaders` | 近 N 天按上传者统计 |
| `scoped` / `scopeHint` | content_admin 数据范围提示 |

## 自动化验收

```bash
node scripts/phase9d-acceptance-test.mjs
```

| ID | 用例 | 预期 |
|----|------|------|
| D1 | GET users | 200，`totalUsers` ≥ 0 |
| D2 | GET dashboard summary | 200，含 `totalResources` |
| D3 | upload/action trend | 7 天序列完整 |
| D4 | audit stats | approved/rejected 数值 |
| D5 | 分布 + Top 列表 | 数组结构正确 |
| D6 | content_admin scoped | `scoped=true`，总量 ≤ admin |
| D7 | auditor GET dashboard | 403 |

## 手工验收

1. admin 登录控制台：8 张 KPI 卡片 + 用户/上传趋势柱图
2. 切换「近 7 天 / 近 30 天」刷新互动趋势与审核通过率
3. 热门资源 Top 10 切换下载/浏览/收藏，点击「详情」进入资源页
4. content_admin 登录：见范围提示，统计不超过全站
5. auditor 无控制台权限时无法访问 API

## 与 Phase 9 闭环

```
搜索运营(9-A~C) 发现问题词/热词
    ↓
驾驶舱(9-D) 看资源增长、互动、审核通过率、分布与 Top 资源
    ↓
补资源 / 优化审核 → 指标改善
```
