# Phase 8 · 完成总结

> 系统日志与配置 — 测评 / 审计 / 合规

---

## 阶段一览

| 阶段 | 内容 | 验收 |
|------|------|------|
| 8-A | 审计补洞 + 日志 API | 7/7 |
| 8-B | sys_config + PUT 审计 | 6/6 |
| 8-C | 存储/预览健康探测 | 4/4 |
| 8-D | 功能开关 Admin + 公开 API | 4/4 |
| 8-E | Admin 六页 + 路由 | 6/6 |
| 8-F | 全链路 + 合规矩阵 | 7/7 |
| **合计** | | **34/34** |

```bash
node scripts/phase8-acceptance-test.mjs
```

---

## 关键 API

| 类型 | 路径 |
|------|------|
| 操作日志 | `GET /api/admin/system/logs` |
| 登录日志 | `GET /api/admin/system/login-logs` |
| 系统配置 | `GET/PUT /api/admin/system/config?group=` |
| 存储状态 | `GET /api/admin/system/storage/status` |
| 预览状态 | `GET /api/admin/system/preview/status` |
| 功能开关 | `GET /api/admin/system/feature-flags` |
| 公开开关 | `GET /api/public/feature-flags` |

---

## 前端路由

- `/admin/system/logs`
- `/admin/system/login-logs`
- `/admin/system/upload-config`
- `/admin/system/preview-config`
- `/admin/system/storage`
- `/admin/system/feature-flags`

---

## SQL 脚本

| 文件 | 说明 |
|------|------|
| `72_phase8a_system_logs.sql` | log_view 权限 + 登录索引 |
| `73_phase8b_system_config.sql` | sys_config 表 + 种子 |
| `74_phase8de_feature_flags_menu.sql` | feature 种子 + 子菜单 |

---

## 合规要点

- 写操作 `@AdminLog` 覆盖：用户导出、资源 CRUD、配置 PUT 等
- secret 类型配置 GET 脱敏，PUT 传 `******` 保留原值
- auditor 无 log_view / config_edit，敏感接口 403
- 日志保留 180 天：**文档约定**，清理 job 待生产前补充

---

## 后续可选

1. C 端 `featureFlags.ts` 合并 `/api/public/feature-flags`
2. resource/auth 运行时读取 `sys_config` 热更新 upload/preview
3. 日志归档定时任务
4. 资源 CSV 导出 + `@AdminLog(module=resource, action=export)`
