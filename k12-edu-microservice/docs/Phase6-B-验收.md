# Phase 6-B · 后台账号 + super_admin 保护验收

范围：`/admin/admin-users` 完整流程、`AdminUserGuard` 全链路、角色独立菜单、用户写操作记 `sys_operation_log`。

---

## 一、部署

```bash
mysql ... < sql/64_phase6a_perm_fix.sql   # 若未执行
mysql ... < sql/65_admin_role_phase6b.sql

mvn -pl k12-auth,k12-common -am package -DskipTests
# 重启 auth(8081)、gateway(9001)；前端刷新
```

---

## 二、AdminUserGuard 规则

| 操作 | 非 super_admin 操作 super_admin 账号 | super_admin 操作普通 staff |
|------|--------------------------------------|----------------------------|
| 分配后台角色 | **403** | 200 |
| 分配 super_admin 角色给他人 | **403** | 200 |
| 移除最后一个 super_admin | — | **400** |
| 禁用 / 启用 / 重置密码 | **403** | 200 |

接入点：
- `AdminRoleService.assignUserRoles`
- `AdminUserServiceImpl.disableUser / enableUser / resetPassword`

---

## 三、功能验收

| # | 场景 | 操作 | 预期 |
|---|------|------|------|
| 1 | 管理员列表 | `/admin/admin-users` | 显示后台角色列（Tag） |
| 2 | Guard 403 | **content_admin** 对 **admin** 分配角色 | 403，前端提示 |
| 3 | Guard 通过 | **super_admin** 对 **content_admin** 分配角色 | 200，列表刷新 |
| 4 | 禁用保护 | content_admin 禁用 admin | 403 |
| 5 | 角色菜单 | 侧栏「角色权限」→ `/admin/roles` | 与系统设置 Tab 功能一致 |
| 6 | 操作日志 | 系统设置 → 操作日志，模块 **user** | 可见 assign_admin_roles / disable 等 |
| 7 | 失败也记日志 | 触发 403 的分配角色 | `sys_operation_log.status=0` |

---

## 四、API 验收（curl 示例）

```bash
# content_admin token → 改 admin 用户角色
curl -X PUT "http://localhost:9001/api/admin/users/1/roles" \
  -H "Authorization: Bearer $CONTENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"roleIds":[2]}'
# 期望：{"code":403,...}

# super_admin → 改 content_admin
curl -X PUT "http://localhost:9001/api/admin/users/{contentAdminUserId}/roles" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"roleIds":[2]}'
# 期望：200
```

---

## 五、测试账号

| 账号 | 密码 | 后台角色 | 用途 |
|------|------|----------|------|
| admin | admin123 | super_admin | 放行用例 |
| content_admin | admin123 | content_admin | Guard 403 用例（需 sql/65 授权 assign_role） |

---

## 六、结论

| 项 | 结果 |
|----|------|
| AdminUserGuard | ☐ |
| 角色独立菜单 | ☐ |
| 操作日志 module=user | ☐ |
| 总体 | ☐ Go / ☐ No-Go |

验收人：________　日期：________
