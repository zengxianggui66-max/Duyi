# Phase 6-D · 用户运营增强验收

范围：用户备注、CSV 导出、批量启停、auditor 数据范围（AUDIT_UPLOADER）、详情「管理操作」Tab。

---

## 一、部署

```bash
mysql ... < sql/67_admin_user_phase6d.sql

mvn -pl k12-auth,k12-common -am package -DskipTests
# 重启 auth(8081)、gateway(9001)；前端刷新
```

---

## 二、API

| 方法 | 路径 | 权限 |
|------|------|------|
| GET | `/api/admin/users/export` | `admin:user:export` |
| POST | `/api/admin/users/batch-status` | `admin:user:edit` |
| GET | `/api/admin/users/{id}/remarks` | `admin:user:view` |
| POST | `/api/admin/users/{id}/remarks` | `admin:user:remark` |
| GET | `/api/admin/users/{id}/operation-logs` | `admin:user:view` |

---

## 三、功能验收

| # | 场景 | 操作 | 预期 |
|---|------|------|------|
| 1 | 备注 | admin 在用户详情「运营备注」添加内容 | 列表可见操作人、时间 |
| 2 | 导出 | 平台用户列表点「导出 CSV」 | 下载文件，字段与当前筛选一致 |
| 3 | 批量禁用 | 勾选 2 个普通用户 → 批量禁用 | 状态变禁用；详情「管理操作」可见 disable/batch_status |
| 4 | auditor 范围 | auditor 登录 `/admin/users` | 仅见 `resource_audit_log` 中自己审核过的资源上传者 |
| 5 | auditor 403 | auditor 打开未审核上传者详情 | 403 |
| 6 | Guard | 批量选择含 super_admin staff | 单项失败或 Guard 403（staff 在 admin-users 页） |

---

## 四、自动化

```bash
node scripts/phase6d-acceptance-test.mjs
```

---

## 五、结论

| 项 | 结果 |
|----|------|
| user_admin_remark 表 | ☑ |
| 备注 / 导出 / 批量启停 | ☑ |
| auditor AUDIT_UPLOADER | ☑ |
| 管理操作 Tab | ☑ |
| 总体 | ☑ **Go** |

验收：`phase6d-acceptance-test.mjs` 8/8 通过（2026-06-24）
