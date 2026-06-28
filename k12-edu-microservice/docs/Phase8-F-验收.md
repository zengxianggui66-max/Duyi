# Phase 8-F · 全链路验收 + 合规矩阵

> Phase 8 最后一步：串联 8-A～8-E 自动化 + 合规对照 + 人工签字

---

## 一、部署前置

```bash
mysql ... < sql/72_phase8a_system_logs.sql
mysql ... < sql/73_phase8b_system_config.sql
mysql ... < sql/74_phase8de_feature_flags_menu.sql

mvn install -pl k12-common,k12-auth,k12-resource,k12-gateway -am -DskipTests
# 重启 auth(8081)、resource(8082)、gateway(9001)
# 前端刷新（8-E 六页）
```

---

## 二、全量自动化

```bash
node scripts/phase8-acceptance-test.mjs
```

| 阶段 | 脚本 | 用例数 |
|------|------|--------|
| 8-A | phase8a-acceptance-test.mjs | 7 |
| 8-B | phase8b-acceptance-test.mjs | 6 |
| 8-C | phase8c-acceptance-test.mjs | 4 |
| 8-D | phase8d-acceptance-test.mjs | 4 |
| 8-E | phase8e-acceptance-test.mjs | 6 |
| 8-F | phase8f-acceptance-test.mjs | 7 |
| **合计** | | **34** |

---

## 三、合规矩阵

| 合规项 | 证据来源 | 自动化 ID | 验收方式 |
|--------|----------|-----------|----------|
| 谁、何时、做了什么 | `sys_operation_log` | F1, A3–A5 | API 返回 username/createTime/module/action；total>0 |
| 登录成功/失败 | `user_login_log` | F2, A1–A2 | `success=0/1`、`loginType=admin` 筛选 |
| 配置变更可追溯 | `module=system` | F3, B2, D3 | PUT config 后 `action=update_config` |
| 敏感信息不泄露 | config GET 脱敏 | F4, F5, B3–B4 | oauth/storage secret 均为 `******` |
| 权限最小化 | RBAC | F6, A7, B5, C4 | auditor 对 logs/config/export 403 |
| 数据保留 | 文档 + 索引 | F7 | 见下文「保留策略」；`idx_login_type_time` 已建 |

---

## 四、数据保留策略（文档约定）

| 表 | 建议保留 | Phase 8 实现 |
|----|----------|--------------|
| `sys_operation_log` | 180 天 | 表 + 索引；清理 job **未实现**（测评前可手工归档） |
| `user_login_log` | 180 天 | 表 + `idx_login_type_time`；清理 job **未实现** |

> 生产部署前建议增加定时任务：`DELETE ... WHERE create_time < NOW() - INTERVAL 180 DAY`（分批执行）。

---

## 五、人工签字表

| # | 场景 | 操作 | 预期 | 结果 |
|---|------|------|------|------|
| M1 | Admin 操作日志页 | `/admin/system/logs` | 可筛选 module/action | ☐ |
| M2 | Admin 登录日志页 | `/admin/system/login-logs` | staff 筛选、失败原因可见 | ☐ |
| M3 | 上传配置 | 改 maxSizeMb 保存 | 提示需重启；日志有 system 记录 | ☐ |
| M4 | 存储状态 | `/admin/system/storage` | MinIO 不可达仍 200，reachable=false | ☐ |
| M5 | 功能开关 | 切换 homeOpsApi | 公开 API 同步变化 | ☐ |
| M6 | 角色权限 | system_admin vs auditor | 后者无 config_edit | ☐ |

验收人：________　日期：________

---

## 六、结论

| 项 | 结果 |
|----|------|
| 自动化 34/34 | ☐ |
| 合规矩阵 F1–F7 | ☐ |
| 人工 M1–M6 | ☐ |
| **Phase 8 总体** | ☐ Go / ☐ No-Go |
