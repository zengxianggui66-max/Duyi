# Phase 8-A · 审计补洞 + 日志 API 验收

范围：全站登录日志 API、统一操作日志 API、`@AdminLog` 补洞、权限 `admin:system:log_view`。

---

## 一、部署

```bash
mysql -u root -p xinketang --default-character-set=utf8mb4 < sql/72_phase8a_system_logs.sql

mvn install -pl k12-common,k12-auth,k12-gateway -am -DskipTests
# 重启 auth(8081)、gateway(9001)
```

---

## 二、新增 API

| 方法 | 路径 | 权限 |
|------|------|------|
| GET | `/api/admin/system/logs` | `admin:system:log_view` |
| GET | `/api/admin/system/login-logs` | `admin:system:log_view` |
| GET | `/api/admin/operation-logs` | `admin:system:log_view`（旧路径，行为一致） |

### 操作日志筛选参数

`module`, `username`, `action`, `status`, `startTime`, `endTime`, `current`, `size`

### 登录日志筛选参数

`username`, `loginType`, `success`, `staffOnly`, `startTime`, `endTime`, `current`, `size`

---

## 三、自动化验收

```bash
node scripts/phase8a-acceptance-test.mjs
```

| ID | 场景 | 预期 |
|----|------|------|
| A1 | admin 登录后台 | `user_login_log` 含 `loginType=admin, success=1` |
| A2 | 登录日志 API | 200 |
| A3 | 编辑资源 | `sys_operation_log` module=resource action=update |
| A4 | 导出用户 CSV | module=user action=export |
| A5 | 403 也记日志 | assign_admin_roles status=0 增加 |
| A6 | 旧 API 兼容 | operation-logs 与 system/logs total 一致 |
| A7 | 无 log_view | auditor → 403 |

---

## 四、结论

| 项 | 结果 |
|----|------|
| 8-A 自动化 | ☐ 7/7 |
| 总体 | ☐ Go / ☐ No-Go |

验收人：________　日期：________
