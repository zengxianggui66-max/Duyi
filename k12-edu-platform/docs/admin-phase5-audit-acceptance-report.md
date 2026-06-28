# 第五步：管理端审核中心强化 — 验收报告

> 验收日期：2026-06-25
> 审核人：___________
> 状态：☐ 通过 / ☐ 不通过

---

## 一、环境准备

| 项目 | 说明 |
|------|------|
| **SQL 迁移** | 执行 `k12-edu-microservice/sql/84_phase2_audit_enhancement.sql` |
| **后端** | 重新编译 `k12-edu-microservice`，重启 `k12-resource` 服务 |
| **前端** | `k12-edu-platform` 重新构建 |
| **测试账号** | 管理员 admin / 审核员 auditor_role / 内容编辑 content_editor |
| **配置** | `k12.audit.auto-publish: true`（默认） |

---

## 二、验收测试用例

### 2.1 审核通过 — 自动上架（auto-publish=true）

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| T1.1 | 1. 以审核员登录，进入「待审队列」<br>2. 找到一条 status=0 的资源<br>3. 点击「通过」→ 确认 | `status` → 1(PUBLISHED)<br>`audit_status` → 1(审核通过)<br>`publish_status` → 1(已上架)<br>「审核记录」中 action = `approve_publish` | ☐ |
| T1.2 | 检查 `resource_audit_log` 表 | 新增 1 条记录，`action='approve_publish'`，`before_status=0`，`after_status=1` | ☐ |
| T1.3 | 检查 `resource_audit` 表 | 新增 1 条审核记录 | ☐ |
| T1.4 | 通过后在前台能搜索到 | 搜索索引已同步，前台可见 | ☐ |

### 2.2 审核驳回

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| T2.1 | 1. 点击待审资源「驳回」<br>2. 不填写原因点击确认 | 前端警告"请填写驳回原因"，无法提交 | ☐ |
| T2.2 | 1. 选择驳回模板（如"文件格式不支持"）<br>2. 点击确认驳回 | `status` → 2(REJECTED)<br>`audit_status` → 2(已驳回)<br>日志 action=`reject` | ☐ |
| T2.3 | 不选模板，手动填写原因后驳回 | 同样生效，原因写入日志 | ☐ |
| T2.4 | API 直接调用驳回接口，不传 reason | HTTP 400 "驳回时必须填写原因" | ☐ |

### 2.3 驳回模板按分类

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| T3.1 | 进入「驳回原因模板」→ 新增模板 | 可看到「问题分类」下拉：通用/内容质量/格式规范/版权合规/分类挂载/其他 | ☐ |
| T3.2 | 新建「内容质量」类模板 "知识点错误" | 表格中分类列显示"内容质量" | ☐ |
| T3.3 | 编辑模板，修改分类为「格式规范」 | 保存后分类更新 | ☐ |
| T3.4 | 打开「待审队列」→ 驳回弹窗 | 常用原因下拉按分类分组展示（el-option-group） | ☐ |
| T3.5 | `GET /api/admin/audit/reject-reasons/by-category` | 返回按分类 key 分组的 JSON | ☐ |
| T3.6 | 禁用某条模板 | 驳回弹窗不再展示该模板 | ☐ |

### 2.4 批量审核

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| T4.1 | 1. 勾选 3 条待审资源<br>2. 点击「批量通过」 | 弹窗显示：成功 3 条，跳过 0 条<br>3 条全部通过 | ☐ |
| T4.2 | 1. 勾选 3 条中混入 1 条已通过的资源<br>2. 批量通过 | 成功 2 条，跳过 1 条，跳过原因显示"#X（非待审核）" | ☐ |
| T4.3 | 1. 勾选 2 条 → 批量驳回<br>2. 填写原因 → 确认 | 弹窗显示成功/跳过数量和跳过明细 | ☐ |
| T4.4 | 批量通过，不勾选任何资源 | 按钮 disabled，或弹出"请选择资源" | ☐ |

### 2.5 审核与上架分离（auto-publish=false）

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| T5.1 | 1. 修改配置 `k12.audit.auto-publish: false`，重启<br>2. 审核通过一条待审资源 | `status` 保持 0(PENDING)<br>`audit_status` → 1(审核通过)<br>`publish_status` → 0(未上架)<br>日志 action=`approve_audit` | ☐ |
| T5.2 | 前台搜索该资源 | **搜索不到**（未上架） | ☐ |
| T5.3 | 管理员手动「发布」该资源 | `status` → 1(PUBLISHED)<br>`publish_status` → 1(已上架)<br>搜索可见 | ☐ |

### 2.6 待审队列筛选

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| T6.1 | 筛选「学段=小学」 | 表格仅展示学段为"小学"的记录 | ☐ |
| T6.2 | 筛选「学科=语文」 | 表格仅展示学科为"语文"的记录 | ☐ |
| T6.3 | 筛选「栏目=课文原文」 | 表格仅展示 module="课文原文" 的记录 | ☐ |
| T6.4 | 筛选「类型=PDF」 | 表格仅展示 type="PDF" 的记录 | ☐ |
| T6.5 | 输入年级筛选 | 年级名称模糊匹配 | ☐ |
| T6.6 | 筛选「预览状态=可预览」 | 表格仅展示 fileExt 为 pdf/jpg/png/gif/webp 的资源 | ☐ |
| T6.7 | 筛选「预览状态=不可预览」 | 表格仅展示 ppt/doc/docx/mp4 等不可预览格式 | ☐ |
| T6.8 | 组合筛选（学段+学科+可预览） | 取交集 | ☐ |
| T6.9 | 点击「重置」 | 所有筛选条件清空，重新加载全部 | ☐ |

### 2.7 审核记录增强

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| T7.1 | 进入「审核记录」| 表格新增时间范围选择器 | ☐ |
| T7.2 | 选择动作「通过·上架」 | 仅展示 action=approve_publish 的记录 | ☐ |
| T7.3 | 选择动作「通过·待发布」 | 仅展示 action=approve_audit 的记录 | ☐ |
| T7.4 | 选择时间范围 "2026-06-01 → 2026-06-25" | 时间范围内的流水 | ☐ |
| T7.5 | 点击「详情」 | 跳转到 `/admin/resources/{id}` 资源详情页 | ☐ |
| T7.6 | 点击「重置」 | 所有筛选清空，加载全部 | ☐ |

### 2.8 权限校验

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| T8.1 | 无 `admin:audit:approve` 权限的账号 → 调用审核通过接口 | HTTP 403 "无审核通过权限" | ☐ |
| T8.2 | 无 `admin:audit:reject` 权限的账号 → 调用驳回接口 | HTTP 403 "无审核驳回权限" | ☐ |
| T8.3 | 无 `admin:audit:reasons:edit` 权限 → 创建/编辑/删除模板 | HTTP 403 / 按钮不可见 | ☐ |
| T8.4 | 无 `admin:audit:records` 权限 → 查看审核记录 | HTTP 403 | ☐ |
| T8.5 | 无审核权限的账号访问待审队列 | 前端"通过""驳回"按钮不显示（v-permission） | ☐ |

### 2.9 预览抽屉增强

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| T9.1 | 点击待审资源的「预览」 | 抽屉展示元数据（含栏目、目录路径） | ☐ |
| T9.2 | 有 catalogPath 的资源 | 「目录路径」行展示完整路径 | ☐ |
| T9.3 | 敏感词提示正常 | 含敏感词的资源有红色 alert 提示 | ☐ |
| T9.4 | 疑似重复提示 | 标题/文件名相同的资源展示黄色 alert | ☐ |
| T9.5 | 文件安全提示 | safe/pending/risk 三种状态标签展示 | ☐ |

### 2.10 SQL 验证

| ID | 验证内容 | SQL | 预期结果 | 通过 |
|----|----------|-----|----------|------|
| T10.1 | category 列存在 | `DESC audit_reject_reason` | 存在 category 列，类型 TINYINT | ☐ |
| T10.2 | 存量模板 category 默认值 | `SELECT id, content, category FROM audit_reject_reason` | category 均 ≥ 0 | ☐ |
| T10.3 | 默认模板已插入 | `SELECT COUNT(*) FROM audit_reject_reason` | 至少 15 条（含存量） | ☐ |

---

## 三、API 接口清单

| 方法 | 路径 | 说明 | 新增？ |
|------|------|------|--------|
| GET | `/api/admin/resources/pending` | 待审队列（支持阶段/学科/栏目/类型/年级筛选） | 参数扩展 |
| POST | `/api/admin/resources/{id}/audit` | 单条审核（action 区分 approve_publish / approve_audit / reject） | 逻辑增强 |
| POST | `/api/admin/resources/batch-audit` | 批量审核（返回 successCount / skipCount / skipReasons） | 已有 |
| GET | `/api/admin/audit/logs` | 审核记录（新增 startTime / endTime 筛选） | 参数扩展 |
| GET | `/api/admin/audit/reject-reasons` | 驳回模板列表 | 已有 |
| GET | `/api/admin/audit/reject-reasons/by-category` | 按分类分组模板 | **新增** |
| POST | `/api/admin/audit/reject-reasons` | 新增模板（含 category） | 参数扩展 |
| PUT | `/api/admin/audit/reject-reasons/{id}` | 编辑模板（含 category） | 参数扩展 |
| PUT | `/api/admin/audit/reject-reasons/{id}/status` | 启用/禁用 | 已有 |
| DELETE | `/api/admin/audit/reject-reasons/{id}` | 删除 | 已有 |

---

## 四、数据流验证

### 4.1 审核通过（auto-publish=true）

```
前端 POST /api/admin/resources/{id}/audit?status=1
  └─ AdminPrimaryResourceServiceImpl.auditResource()
       ├─ 权限校验: admin:audit:approve
       ├─ resource.setAuditStatus(1)
       ├─ resource.setStatus(1)         // 自动上架
       ├─ resource.setPublishStatus(1)
       ├─ primaryChineseResourceMapper.updateById(resource)
       ├─ resourceAuditMapper.insert(audit)  → resource_audit 表
       ├─ adminAuditService.appendAuditLog(..., "approve_publish")
       │    └─ resourceAuditLogMapper.insert  → resource_audit_log 表
       └─ syncSearchIndex(id)            → 同步搜索索引
```

### 4.2 审核通过（auto-publish=false）

```
同上，区别：
  ├─ resource.setStatus(不变)          // 不自动上架
  ├─ resource.setPublishStatus(0)
  └─ action = "approve_audit"
```

### 4.3 驳回

```
前端 POST /api/admin/resources/{id}/audit?status=2&reason=xxx
  ├─ 权限校验: admin:audit:reject
  ├─ reason 为空? → 抛出 400
  ├─ resource.setStatus(2)
  ├─ resource.setAuditStatus(2)
  └─ action = "reject"
```

---

## 五、状态机对照表

| 操作 | status | audit_status | publish_status | 日志 action |
|------|--------|-------------|----------------|-------------|
| 提交审核 | 0(PENDING) | 0(AUDIT_PENDING) | 0(UNPUBLISHED) | — |
| 审核通过（自动上架） | 1(PUBLISHED) | 1(AUDIT_APPROVED) | 1(PUBLISHED) | `approve_publish` |
| 审核通过（待发布） | 0(PENDING) | 1(AUDIT_APPROVED) | 0(UNPUBLISHED) | `approve_audit` |
| 审核驳回 | 2(REJECTED) | 2(AUDIT_REJECTED) | 0(UNPUBLISHED) | `reject` |
| 手动发布 | 1(PUBLISHED) | 1(APPROVED) | 1(PUBLISHED) | —（publish 接口） |
| 下架 | 3(OFFLINE) | 1(APPROVED) | 2(OFFLINE) | — |

---

## 六、回归验证（不影响现有功能）

| 检查项 | 验证方式 | 通过 |
|--------|----------|------|
| 资源列表正常分页查询 | GET `/api/admin/resources` | ☐ |
| 资源详情展示模块/路径 | GET `/api/admin/resources/{id}` | ☐ |
| 资源编辑正常保存 | PUT `/api/admin/resources/{id}` | ☐ |
| 单独发布/下架功能 | POST `/.../publish` / `/offline` | ☐ |
| 搜索索引同步正常 | 审核后前台可搜到 | ☐ |
| 权限注解未冲突 | `@RequiresPermission` 正常拦截 | ☐ |

---

## 七、验收结论

| 项目 | 测试用例数 | 通过数 | 存在问题 |
|------|-----------|--------|----------|
| 审核通过 | 4 | | |
| 审核驳回 | 4 | | |
| 驳回模板分类 | 6 | | |
| 批量审核 | 4 | | |
| 上架分离 | 3 | | |
| 待审筛选 | 9 | | |
| 审核记录 | 6 | | |
| 权限校验 | 5 | | |
| 预览抽屉 | 5 | | |
| SQL 验证 | 3 | | |
| 回归验证 | 6 | | |
| **合计** | **55** | | |

### 结论

☐ **验收通过** — 所有测试用例通过，无遗留问题，可上线。

☐ **有条件通过** — 存在 _____ 项非阻塞问题，需记录在案，后续修复后上线。

☐ **不通过** — 存在阻塞问题：_________________________

---

审核人签字：___________
日期：2026-06-25
