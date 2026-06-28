# Phase 2 Step 1 — 基础验收线 Smoke Test

> 对应 `admin-phase2-detailed-development-plan.md` 第一步（483–503 行）。  
> 自动化入口：`k12-edu-microservice/scripts/phase2-step1-baseline-acceptance.mjs`

## 1. 目标

在二期业务改造（上传数据源、审核强化等）之前，固化：

- 前端可构建、后端可编译
- 管理端身份与 RBAC（前端路由 + 后端 API 双层）
- 核心操作日志可追溯
- 乱码按优先级治理（不大范围改业务）

## 2. 环境与账号

| 服务 | 端口 |
|------|------|
| Gateway | 9001 |
| k12-auth | 8081 |
| k12-resource | 8082 |
| 前端 dev | 5173 |

| 账号 | 密码 | 用途 |
|------|------|------|
| admin | admin123 | 超级管理员 |
| auditor | admin123 | 仅审核，无搜索/分析/系统配置 |
| content_admin | admin123 | 内容+搜索编辑+分析 |
| operator | admin123 | 搜索/分析只读 |
| normal_user | admin123 | C 端学生，**不能进后台** |
| staff_no_role | admin123 | role=admin 但未分配 sys_role |

SQL 初始化（按顺序，**请用 cmd 重定向**，勿在 PowerShell 直接粘贴含 `$` 的 BCrypt）：

```bash
cmd /c "mysql -u root -p xinketang --default-character-set=utf8mb4 < sql/79_phase9_post_acceptance_fix.sql"
scripts\run-phase2-step1-sql.cmd
```

或设置 `MYSQL_PWD` 后验收脚本会尝试自动导入 sql/81。

## 3. 工程闸门（最先执行）

| ID | 检查项 | 命令 | 通过标准 |
|----|--------|------|----------|
| B1 | 前端构建 | `cd k12-edu-platform && npm run build` | exit 0 |
| B2 | 后端编译 | `cd k12-edu-microservice && mvnw.cmd -pl k12-common,k12-auth,k12-resource,k12-gateway -am -DskipTests compile` | exit 0 |

自动化脚本中默认**跳过**构建（耗时），需强制检查时：

```bash
set PHASE2_CHECK_BUILD=1
node scripts/phase2-step1-baseline-acceptance.mjs
```

## 4. 路由与身份（手工 + 自动）

### 4.1 前端路由（手工）

| 场景 | 操作 | 预期 |
|------|------|------|
| F1 | 未登录访问 `/admin/analytics/overview` | 跳转 `/admin/login?redirect=...` |
| F2 | `normal_user` 登录后访问 `/admin` | `/admin/403` |
| F3 | `admin` 登录 | 进入 `/admin/analytics/overview` |
| F4 | `auditor` 直链 `/admin/analytics/overview` | `/admin/403` |
| F5 | `operator` 侧栏 | 有「搜索运营」「数据分析」，无系统配置/角色权限 |

实现位置：

- `src/admin/router/guards.ts` — token → bootstrap → isStaff → permission
- `src/admin/store/adminAuth.ts` — `isStaff`、`hasPermission`、`hasAnalyticsView`

### 4.2 后端 API（自动化 S1–S20）

| ID | 用例 | 预期 |
|----|------|------|
| S1 | 未登录 `GET /api/admin/menus` | 401 |
| S2 | admin `GET /api/admin/auth/me` | 200 + roles |
| S3 | normal_user `GET /api/admin/auth/me` | 403 需要管理员权限 |
| S4 | staff_no_role `GET /api/admin/auth/me` | 403 未分配后台角色 |
| S5 | admin menus | 含「数据分析」「搜索运营」子菜单 |
| S6–S10 | operator/content/auditor 搜索与分析 API | 见脚本 |
| S11–S14 | stats 权限统一为 `admin:analytics:view` | operator 200 / auditor 403 |
| S15 | auditor `PUT /api/admin/system/config` | 403 |
| S16 | legacy `/api/search/admin/**` | 404 或不可用 |
| S17 | auditor permissions | 无 dashboard/analytics |
| S18–S19 | 操作日志 | audit/resource/search 至少一项有记录；auditor 查日志 403 |

## 5. 权限设计落点

| 层级 | 机制 | 文件 |
|------|------|------|
| 网关 | JWT → X-User-Id | `k12-gateway/.../AuthGlobalFilter.java` |
| 后端 | `@RequiresPermission` + 拦截器 | `AdminPermissionInterceptor.java`、各 Controller |
| 菜单 | `sys_menu.permission_code` + 角色映射 | `sql/78`、`sql/79` |
| 按钮 | `v-permission` | 各 admin views |
| 数据分析 | `admin:analytics:view`（旧 `admin:dashboard:view` 仅前端兼容） | `AdminStatsController`、`AdminAnalyticsController` |

**Step 1 权限修复**：`GET /api/admin/resources/stats` 与 deprecated `/api/admin/resource/stats` 已从 `admin:dashboard:view` 改为 `admin:analytics:view`，与 `/api/admin/stats/resources` 一致。

## 6. 操作日志最小集

| 模块 | action 示例 | 验收 |
|------|-------------|------|
| audit | approve / reject | S18 |
| search | reindex / create_synonym | S19 |
| system | update_config | Phase 8 脚本 |
| resource | update / publish | Phase 8-A |

查询：`GET /api/admin/system/logs?module=...`（需 `admin:system:log_view`）

## 7. 乱码治理

见 `docs/Phase2-Mojibake-Inventory.md`。Step 1 **阻塞项**：P0 清零；P1+ 列入后续 Sprint。

## 8. 执行命令

```bash
# 仅 Step 1 基线（约 20 项 API）
node scripts/phase2-step1-baseline-acceptance.mjs

# 基线 + Phase 9 全量回归（推荐发版前）
node scripts/phase2-step1-baseline-acceptance.mjs
# 默认 PHASE2_INCLUDE_PHASE9=1

# 仅基线，不跑 Phase 9
set PHASE2_INCLUDE_PHASE9=0
node scripts/phase2-step1-baseline-acceptance.mjs

# 含构建检查
set PHASE2_CHECK_BUILD=1
node scripts/phase2-step1-baseline-acceptance.mjs
```

## 9. Step 1 签字标准

| 类别 | 必须 |
|------|------|
| B1/B2 构建编译 | ✅（发版前） |
| S1–S20 自动化 | ✅ |
| F1–F5 手工路由 | ✅（首次或 UI 变更后） |
| Phase 9 回归 | 推荐 ✅ |
| 乱码 P0 | ✅ |

**通过后**方可进入第二步「上传页数据源改造」。

## 10. 故障排查

| 失败项 | 处理 |
|--------|------|
| S3/S4 | 执行 `sql/81_phase2_step1_baseline.sql` |
| S16 | 重启 gateway(9001) + resource(8082) |
| S17 | 执行 `sql/79_phase9_post_acceptance_fix.sql` |
| S11–S14 | 确认已部署 resources stats 权限修复并 recompile resource 服务 |
| 连接拒绝 | 确认 gateway 9001 与各微服务已启动 |
