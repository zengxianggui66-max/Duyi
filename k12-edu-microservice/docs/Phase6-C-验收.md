# Phase 6-C · 用户行为流水验收

范围：`user_resource_action` 表、view/download/search 埋点双写、管理端用户详情四 Tab（浏览 / 下载 / 搜索 / 登录），权限 `admin:user:view_behavior`。

---

## 一、部署

```bash
mysql ... < sql/66_user_resource_action_phase6c.sql

mvn -pl k12-resource,k12-common -am package -DskipTests
# 重启 resource(8082)、gateway(9001)；前端刷新
```

---

## 二、数据流

| 行为 | 埋点入口 | 存储 |
|------|----------|------|
| 浏览 | `ViewRecordServiceImpl.upsertView`（需登录） | `user_resource_action` action_type=view |
| 下载 | `DownloadRecordServiceImpl.addRecord`（需登录） | `user_resource_action` action_type=download |
| 搜索 | `SearchLogService.logQuery`（需 userId） | `user_resource_action` action_type=search |
| 登录 | auth 登录时写入 | `user_login_log`（只读展示，不入 action 表） |

说明：未登录用户的 primary-chinese 聚合 view/download **不会**进入用户流水。

---

## 三、API

| 方法 | 路径 | 权限 |
|------|------|------|
| GET | `/api/admin/users/{id}/stats` | `admin:user:view`（含 view/download/search/login 计数） |
| GET | `/api/admin/users/{id}/actions?actionType=view\|download\|search` | `admin:user:view_behavior` |
| GET | `/api/admin/users/{id}/login-logs` | `admin:user:view_behavior` |

---

## 四、功能验收

| # | 场景 | 操作 | 预期 |
|---|------|------|------|
| 1 | 权限 | 无 `view_behavior` 的账号打开用户详情 | 不显示浏览/下载/搜索/登录 Tab |
| 2 | 统计 | admin 打开 teacher_demo 详情 | 头部显示上传/收藏/浏览/下载/搜索/登录计数 |
| 3 | **核心** | 门户 **teacher_demo** 登录 → 下载任意资源 | **30 秒内**管理端该用户「下载」Tab 出现一条记录（标题、时间、IP） |
| 4 | 浏览 | 门户登录用户打开资源详情 | 「浏览」Tab 可见对应记录 |
| 5 | 搜索 | 门户登录用户搜索关键词 | 「搜索」Tab 可见关键词与命中数 |
| 6 | 登录 | 用户再次登录 | 「登录」Tab 可见成功记录 |
| 7 | 403 | 无权限账号调 actions API | 403 |

---

## 五、curl 示例

```bash
# 下载 Tab 数据
curl "http://localhost:9001/api/admin/users/{userId}/actions?actionType=download&current=1&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 登录流水
curl "http://localhost:9001/api/admin/users/{userId}/login-logs?current=1&size=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

---

## 六、测试账号

| 账号 | 密码 | 用途 |
|------|------|------|
| admin | admin123 | 管理端查看流水 |
| teacher_demo | admin123 或重置后 `123456` | 门户产生下载/浏览行为（OAuth 注册账号若无法登录，管理端可重置密码） |

---

## 七、自动化验收

```bash
# 前置：resource(8082)、gateway(9001) 已用 Phase 6-C 代码重启
node scripts/phase6c-acceptance-test.mjs
```

---

## 八、结论

| 项 | 结果 |
|----|------|
| user_resource_action 表 | ☑ |
| view/download 埋点双写 | ☑ |
| 详情四 Tab + view_behavior | ☑ |
| 下载 30s 内可见 | ☑（约 20ms，经网关） |
| 总体 | ☑ **Go** |

验收人：自动化脚本 `phase6c-acceptance-test.mjs`　日期：2026-06-24

### 本次验收说明

1. **teacher_demo** 为 OAuth 注册账号，门户密码可能非种子脚本哈希；验收前已通过 `POST /api/admin/users/11/reset-password` 重置为 `123456`。
2. **resource + gateway 必须重启**：旧 gateway 未路由 `/actions`、`/login-logs` 至 8082，会导致 500。
3. 自动化脚本覆盖：权限、403、浏览/下载/搜索埋点、30s 内下载可见、stats 计数、登录流水。
