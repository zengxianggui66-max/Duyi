# Phase 6-A · 用户列表 + 详情骨架验收

范围：平台用户 / 管理员账号拆分、列表分页搜索启停、详情只读（基本资料 + OAuth + 上传/收藏）。**不含**会员、促销、登录流水管理页。

---

## 一、部署前置

```bash
# 1. 执行 SQL（按序）
mysql -u root -p xinketang --default-character-set=utf8mb4 < sql/62_admin_user_phase6a.sql
mysql -u root -p xinketang --default-character-set=utf8mb4 < sql/63_phase6a_teacher_demo_seed.sql

# 2. 编译并重启
mvn -pl k12-auth,k12-resource,k12-gateway -am package -DskipTests
# 重启 auth(8081)、resource(8082)、gateway(9001)
# 前端 dev:5173
```

---

## 二、API 路由

| 能力 | 服务 | 路径 |
|------|------|------|
| 用户列表/详情/启停 | auth 8081 | `GET/PUT/POST /api/admin/users/**` |
| 后台角色分配 | auth 8081 | `GET/PUT /api/admin/users/{id}/roles` |
| 上传统计/列表 | resource 8082 | `GET /api/admin/users/{id}/stats`、`/resources` |
| 收藏列表 | resource 8082 | `GET /api/admin/users/{id}/collections` |

网关：`/api/admin/users/*/stats|resources|collections` → resource，其余 `/api/admin/users/**` → auth。

---

## 三、菜单与权限

| 菜单 | 路径 | 权限码 |
|------|------|--------|
| 平台用户 | `/admin/users` | `admin:user:view` |
| 用户详情（隐藏） | `/admin/users/:id` | `admin:user:view` |
| 管理员账号 | `/admin/admin-users` | `admin:admin_user:view` |

新增权限：`admin:user:view_behavior`、`admin:admin_user:view`、`admin:admin_user:assign_role`

---

## 四、功能验收（10 项）

| # | 场景 | 操作 | 预期 |
|---|------|------|------|
| 1 | 平台用户列表 | admin 打开 `/admin/users` | 不含 `role=admin` 的 staff；分页正常 |
| 2 | 搜索 | 关键词 `teacher_demo` | 命中演示教师 |
| 3 | 筛选 | 身份=教师、状态=正常 | 列表过滤正确 |
| 4 | 启停 | 禁用再启用某用户 | 状态标签切换，API 200 |
| 5 | 用户详情 | 点击「详情」 | 基本资料 Tab 完整 |
| 6 | OAuth | 详情 → OAuth Tab | `teacher_demo` 可见微信绑定 |
| 7 | 上传统计 | 详情页头部 | 显示「上传 3 条」 |
| 8 | 上传列表 | 上传 Tab | 列表 ≥3 条（执行 63 号种子后） |
| 9 | 收藏 | 收藏 Tab | 列表 5 条，头部「收藏 5 条」 |
| 10 | 管理员账号 | `/admin/admin-users` | 仅 staff；可分配后台角色、重置密码 |

---

## 五、角色差异

| 账号 | 密码 | 平台用户 | 管理员账号 | 行为 Tab |
|------|------|----------|------------|----------|
| admin | admin123 | ✓ | ✓ | ✓ |
| content_admin | admin123 | 按 RBAC | ✗ | 按权限 |
| auditor | admin123 | ✗ | ✗ | ✗ |

---

## 六、登录流水（后端）

任意门户登录成功后，`user_login_log` 应新增一条记录：

```sql
SELECT id, user_id, username, login_type, success, ip, create_time
FROM user_login_log
ORDER BY id DESC LIMIT 5;
```

---

## 七、已知限制（Phase 6-B）

- 详情页暂不支持编辑昵称/身份（API 已有 `PUT /users/{id}`，UI 未做）
- `super_admin` 账号保护、登录流水管理页未做
- 上传/收藏仅覆盖 `primary_chinese` 主表口径

---

## 八、验收结论

| 项 | 结果 |
|----|------|
| 编译 | `mvn -pl k12-auth,k12-resource,k12-common -am compile` |
| Teacher demo | 执行 `63_phase6a_teacher_demo_seed.sql` 后搜 `teacher_demo` |
| 总体 | ☐ Go / ☐ No-Go |

验收人：________　日期：________
