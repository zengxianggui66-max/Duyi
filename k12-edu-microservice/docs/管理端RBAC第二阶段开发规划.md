# 管理端 RBAC 第二阶段开发规划

> 目标：后台能控制「谁能进、能看什么、能操作什么」。

## 一、交付范围（Step 1～13）

| 步骤 | 内容 | 状态 |
|------|------|------|
| 1 | 权限码规范 + `sql/51_admin_rbac_phase2.sql` | ✅ |
| 2 | 7 内置角色 + 按钮权限种子 | ✅ |
| 3 | Entity / Mapper / Service 重构 | ✅ |
| 4 | 角色 / 权限 CRUD API | ✅ |
| 5 | 网关路由 + `sql/52` 测试账号 | ✅ |
| 6 | 前端 isStaff + auditor 动态菜单 | ✅ |
| 7 | 写接口 `@RequiresPermission` 拦截 | ✅ |
| 8 | 业务页 `v-permission` 按钮 | ✅ |
| 9 | `sys_data_scope` 接入资源查询 | ✅ |
| 10 | 系统设置 - 角色权限管理 UI | ✅ |
| 11 | 用户管理 - 分配后台角色 | ✅ |
| 12 | `sys_operation_log` + AOP | ✅ |
| 13 | 验收矩阵（见下文） | ✅ |

## 二、数据库

```bash
mysql -u root -p xinketang --default-character-set=utf8mb4
source sql/49_admin_rbac_baseline.sql   # 若未执行
source sql/51_admin_rbac_phase2.sql
source sql/52_admin_rbac_test_users.sql
source sql/50_auth_admin_user_patch.sql # 确保 admin 密码与 super_admin 绑定
```

### 表结构

- 已有：`sys_role` / `sys_menu` / `sys_permission` / `sys_user_role` / `sys_role_permission`
- 新增：`sys_data_scope` / `sys_operation_log`

### 内置角色

| code | 名称 |
|------|------|
| super_admin | 超级管理员 |
| content_admin | 内容管理员 |
| auditor | 审核员 |
| operator | 运营人员 |
| customer_service | 客服 |
| finance_admin | 会员/订单管理员 |
| system_admin | 系统配置管理员 |

### 权限类型

- **menu**：控制侧栏 / 路由（如 `admin:audit:view`）
- **button**：控制按钮与写操作（如 `admin:audit:approve`）

### 数据范围 `sys_data_scope`

| scope_type | 含义 |
|------------|------|
| ALL | 全部数据 |
| SELF | 仅本人上传 |
| STAGE_SUBJECT | 指定学段 + 学科 |

## 三、API（k12-auth，经网关 9001）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/auth/me` | 当前用户 + roles + permissions |
| GET | `/api/admin/menus` | 当前用户菜单树 |
| GET | `/api/admin/permissions` | 当前用户权限码列表 |
| GET | `/api/admin/permissions/tree` | 角色配置用权限树 |
| GET | `/api/admin/roles` | 角色列表 |
| POST | `/api/admin/roles` | 创建角色 |
| PUT | `/api/admin/roles/{id}` | 更新角色 |
| PUT | `/api/admin/roles/{id}/permissions` | 分配权限 |
| PUT | `/api/admin/users/{id}/roles` | 分配用户后台角色 |

## 四、架构说明

### 双角色模型

1. **`user.role`**（C 端身份）：staff 账号统一为 `admin`，用于管理端入口粗筛。
2. **`sys_role`**（岗位角色）：`auditor`、`content_admin` 等，用于菜单 / 按钮 / 数据权限。

### 鉴权链路

```
JWT → 网关 X-User-Id / X-User-Role
  → AdminAccessInterceptor（user.role=admin）
  → AdminPermissionInterceptor（@RequiresPermission）
  → 业务 Service（数据范围 / 审核权限细分）
```

## 五、测试账号

| 用户名 | 密码 | 后台角色 | 预期菜单 |
|--------|------|----------|----------|
| admin | admin123 | super_admin | 全部 7 项 |
| auditor | admin123 | auditor | 控制台 + 审核中心 |
| content_admin | admin123 | content_admin | 控制台 + 资源 + 分类 + 首页 |

> 52 号脚本中 auditor 与 admin 使用相同 BCrypt（对应密码 `admin123`）。

## 六、验收矩阵

| 账号 | 可见菜单 | 可通过审核 API | 可删资源 |
|------|----------|----------------|----------|
| super_admin | 全部 | ✅ | ✅ |
| auditor | 控制台、审核 | ✅ | ❌ 403 |
| content_admin | 资源相关 | ❌ | ✅（有 delete 权限时） |
| teacher | — | ❌ 403 | ❌ |

## 七、相关文件

| 类型 | 路径 |
|------|------|
| SQL | `sql/51_admin_rbac_phase2.sql`, `sql/52_admin_rbac_test_users.sql` |
| 权限服务 | `k12-common/.../AdminPermissionService.java` |
| 拦截器 | `k12-common/.../AdminPermissionInterceptor.java` |
| 角色 API | `k12-auth/.../AdminRoleController.java` |
| 前端 Store | `k12-edu-platform/src/admin/store/adminAuth.ts` |
| 角色 UI | `k12-edu-platform/src/admin/components/RoleManage.vue` |

## 八、注意事项

1. 修改角色权限后，用户需 **重新登录** 或刷新 bootstrap 以更新前端 permissions。
2. `super_admin` 权限不可通过 UI 修改（内置保护）。
3. 仅 `user.role=admin` 的 staff 账号可分配 `sys_user_role`。
4. 不要重跑 `06_auth.sql`；admin 密码用 `50_auth_admin_user_patch.sql`。
