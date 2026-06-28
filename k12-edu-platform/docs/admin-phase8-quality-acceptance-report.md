# 第八步：质量治理和运营统计 — 验收报告

> 验收日期：2026-06-____  
> 审核人：___________  
> 状态：☐ 通过 / ☐ 不通过

---

## 一、环境准备

| 项目 | 说明 |
|------|------|
| **SQL 迁移** | 执行 `k12-edu-microservice/sql/85_phase8_quality_governance.sql` |
| **后端** | 重新编译 `k12-edu-microservice`，重启 `k12-resource` 服务 |
| **前端** | `k12-edu-platform` 重新构建 |
| **测试账号** | 管理员 admin (role_id=1) / 审核员 auditor_role |
| **新建文件** | Entity(5): `SysSensitiveWord` `PreviewFailQueue` `ResourceDailyStats` `AuditorWorkload` `LowAccessResourceVO` |
|  | DTO(9): `SensitiveWordDTO` `PreviewFailQueueVO` `AuditWorkloadVO` `AuditSlaVO` `RejectStatsVO` `QualityDashboardVO` `GrowthTrendVO` `DailyStatsPointVO` `PageResult` |
|  | Mapper(3): `SysSensitiveWordMapper` `PreviewFailQueueMapper` `QualityAnalyticsMapper` + XML |
|  | Service(3): `SensitiveWordService/Impl` `PreviewFailService/Impl` `QualityAnalyticsService/Impl` |
|  | Controller(3): `AdminSensitiveWordController` `AdminPreviewFailController` `AdminQualityAnalyticsController` |
|  | Vue(4): `QualityShell` `QualityDashboard` `SensitiveWords` `PreviewFails` + 路由注册 |
|  | API(1): `api/quality.ts` |

---

## 二、功能模块验收

### 2.1 敏感词词库管理

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| QC-01 | 1. 管理员登录，展开「质量治理」→「敏感词库」<br>2. 页面加载完成 | 展示分页列表，包含12条种子数据（赌博、暴力、毒品等） | ☐ |
| QC-02 | 1. 搜索框输入「賭」<br>2. 点击查询 | 匹配模糊搜索，列出含该关键字的敏感词 | ☐ |
| QC-03 | 按「分类」筛选→选择"政治" | 仅展示 category=1 的敏感词 | ☐ |
| QC-04 | 按「级别」筛选→选择"阻断" | 仅展示 level=2 的敏感词 | ☐ |
| QC-05 | 按「状态」筛选→选择"禁用" | 仅展示已禁用的敏感词 | ☐ |
| QC-06 | 1. 点击「新增敏感词」<br>2. 填写词="代考"、分类="广告"、级别="阻断"、备注="违规广告"<br>3. 确认 | 弹窗关闭，列表刷新，新增行显示在表格中；`sys_sensitive_word` 表插入一条记录 | ☐ |
| QC-07 | 1. 新建时不填写敏感词<br>2. 点击确认 | 前端校验提示"请输入敏感词"，无法提交 | ☐ |
| QC-08 | 1. 点击某敏感词行「编辑」<br>2. 修改分类为"其他"、级别改为"警告"<br>3. 保存 | 弹窗关闭，列表刷新，该行分类和级别更新 | ☐ |
| QC-09 | 1. 点击某敏感词行「禁用」<br>2. 确认 | 该行 status 变为 0，标签显示"禁用"；`sys_sensitive_word.status=0` | ☐ |
| QC-10 | 同上，再点击「启用」 | 状态恢复启用 | ☐ |
| QC-11 | 1. 勾选 3 行敏感词<br>2. 点击「批量禁用」 | 3 行状态全部变为禁用 | ☐ |
| QC-12 | 1. 勾选 2 行→点击「批量启用」 | 2 行恢复启用 | ☐ |
| QC-13 | 1. 点击某行「删除」<br>2. 确认弹窗点确定 | 该行消失，`sys_sensitive_word` 记录被物理删除 | ☐ |
| QC-14 | `GET /api/admin/quality/sensitive-words/stats` | 返回按 category 分组的 count 数据 | ☐ |

### 2.2 文件安全状态 — 审核中心展示

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| QC-15 | 1. 审核员进入「待审队列」<br>2. 查看表格列 | 新增「文件安全」列，每行显示彩色标签：<br>safe=绿色"安全"、pending=橙色"待确认"、risk=红色"有风险"、unknown=蓝色"未知" | ☐ |
| QC-16 | 鼠标悬停在文件安全标签上 | tooltip 显示完整描述文案（如"文件类型安全，未发现风险"） | ☐ |
| QC-17 | 1. 筛选栏选择「文件安全」→"有风险"<br>2. 点击查询 | 仅展示 `fileSafetyStatus='risk'` 的待审资源 | ☐ |
| QC-18 | 选择「文件安全」→"待确认" | 仅展示 `fileSafetyStatus='pending'` 的资源 | ☐ |

### 2.3 审核超时筛选

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| QC-19 | 1. 待审队列筛选栏选择「审核超时」→"超24h待审"<br>2. 查询 | 仅展示 upload_time > 24小时前且 audit_status=0 的资源 | ☐ |
| QC-20 | 选择「审核超时」→"超48h待审" | 展示 >48h 的待审资源（是 >24h 的子集） | ☐ |
| QC-21 | 选择「审核超时」→"超72h待审" | 展示 >72h 的待审资源 | ☐ |
| QC-22 | 清除筛选，点击「重置」 | 文件安全、审核超时筛选同时清空，恢复全量列表 | ☐ |

### 2.4 预览失败队列

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| QC-23 | 1. 管理员进入「质量治理」→「预览失败」<br>2. 页面加载 | 展示分页列表，列出 `preview_fail_queue` 中 status=0（待处理）的记录 | ☐ |
| QC-24 | 筛选栏选择「状态」→"已处理" | 展示已处理/已忽略的记录 | ☐ |
| QC-25 | 1. 输入标题关键词搜索 → 查询 | 模糊匹配 title 筛选 | ☐ |
| QC-26 | 点击资源标题（有 globalId 时） | 跳转到 `/admin/resources/{globalId}` 详情页 | ☐ |
| QC-27 | 1. 找到一条待处理记录<br>2. 点击「标记已处理」→ 确认 | 该行 status → 1（已处理），"状态"标签变绿色，操作列显示处理人姓名和处理时间 | ☐ |
| QC-28 | 1. 找到一条待处理记录<br>2. 点击「忽略」→ 确认 | 该行 status → 2（已忽略），"状态"标签变灰色 | ☐ |
| QC-29 | 刷新页面，筛选"待处理" | 已处理/已忽略的记录不再显示 | ☐ |
| QC-30 | `GET /api/admin/quality/preview-fails/stats` | 返回按 status 分组的 count 数据 | ☐ |

### 2.5 驳回归因统计

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| QC-31 | 管理员进入「质量治理」→「质量大盘」 | 右侧「驳回原因分布」表格展示按分类+原因分组的驳回次数及占比 | ☐ |
| QC-32 | 切换天数为"近 7 天" | 驳回统计数据范围缩小到7天 | ☐ |
| QC-33 | `GET /api/admin/quality/analytics/reject-stats?days=30` | 返回驳回原因排行，含 categoryName、reason、rejectCount、percentage 字段 | ☐ |

### 2.6 审核员工作量统计

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| QC-34 | 质量大盘「审核员工作量 Top10」表格 | 展示审核员姓名、通过数、驳回数、合计、平均耗时 | ☐ |
| QC-35 | 平均耗时列格式 | 显示为 "XhYm" 或 "Ym" 格式（如 "2h35m"） | ☐ |
| QC-36 | `GET /api/admin/quality/analytics/workload?days=30&limit=10` | 返回按 totalCount 降序排列的审核员数据 | ☐ |

### 2.7 审核 SLA

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| QC-37 | 质量大盘 SLA 卡片行 | 显示四项：平均审核时长、超时>24h、超时>48h、当日审核完成数 | ☐ |
| QC-38 | 平均审核时长格式 | 显示为 "Xh Ym" 格式（如 "1h 23m"） | ☐ |
| QC-39 | 超时数值与待审队列筛选一致 | 质量大盘「超时>24h」数值 ≥ 待审队列"超24h待审"筛选出的数量 | ☐ |
| QC-40 | `GET /api/admin/quality/analytics/sla?days=30` | 返回 avgAuditDurationSec、overtime24h/48h/72hCount、totalPendingCount 等字段 | ☐ |

### 2.8 资源增长趋势（通过率/驳回率/下架率）

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| QC-41 | 质量大盘「资源总数」「审核通过率」「驳回率」「下架率」卡片 | 四项数值正常显示（通过率/驳回率为百分比） | ☐ |
| QC-42 | `GET /api/admin/quality/analytics/growth?days=30` | 返回 totalCount、publishedCount、approvalRate、rejectionRate、offlineRate、dailyPoints | ☐ |
| QC-43 | 运营概览 Dashboard「质量风险快报」区域 | 显示"驳回率"卡片（数据来自质量系统） | ☐ |

### 2.9 低访问资源

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| QC-44 | `GET /api/admin/quality/analytics/low-access?pageNum=1&pageSize=20` | 返回已上架>30天 且（浏览<10 或 下载<3）的资源列表 | ☐ |
| QC-45 | 响应中包含 accessLevel 字段 | 值为"零访问"/"低浏览"/"低下载"之一 | ☐ |
| QC-46 | 低访问资源分页过滤：按学段筛选 | 仅展示对应学段的低访问资源 | ☐ |
| QC-47 | 低访问资源分页过滤：按学科筛选 | 仅展示对应学科的低访问资源 | ☐ |

### 2.10 运营概览 — 质量风险快报

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| QC-48 | 管理员进入「运营概览」Dashboard | 运营卡片下方出现「质量风险快报」标题和6个质量卡片 | ☐ |
| QC-49 | 质量快报卡片内容 | 文件安全风险数、预览失败（待处理）、超时>24h、低访问资源、敏感词库（已启用词数）、驳回率（近30天） | ☐ |
| QC-50 | 质量快报数据与质量大盘一致 | 两处显示的数字对应一致 | ☐ |
| QC-51 | 无质量权限的用户登录 Dashboard（content_editor） | 「质量风险快报」区域不渲染，不影响运营卡片正常显示 | ☐ |

### 2.11 质量大盘总览

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| QC-52 | 管理员进入「质量治理」→「质量大盘」 | 页面正常渲染，包含质量风险、SLA、增长趋势、驳回原因、审核员工作量等全部模块 | ☐ |
| QC-53 | 切换天数"近 7 天"/"近 30 天" | 所有面板数据随天数变化刷新 | ☐ |
| QC-54 | 点击「刷新」按钮 | 所有数据重新加载，loading 状态正常 | ☐ |
| QC-55 | `GET /api/admin/quality/analytics/dashboard?days=30` | 返回完整的 QualityDashboardVO JSON，包含 sla、rejectStats、auditorWorkload、growthTrend 及所有计数 | ☐ |

### 2.12 权限隔离

| ID | 测试步骤 | 预期结果 | 通过 |
|----|----------|----------|------|
| QC-56 | 以无质量权限的审核员登录 | 左侧菜单不显示「质量治理」 | ☐ |
| QC-57 | 直接在地址栏输入 `/admin/quality/dashboard` | 返回 403 或跳转到 403 页面 | ☐ |
| QC-58 | 无 `admin:quality:sensitive_edit` 的角色 | 敏感词列表不显示「新增」「编辑」「删除」「批量」按钮 | ☐ |
| QC-59 | 无 `admin:quality:preview_fail` 的角色 | 预览失败菜单不可见 | ☐ |
| QC-60 | 无 `admin:quality:workload` 的角色 | 审核员工作量 API 返回 403 | ☐ |

---

## 三、数据库验证

| ID | 验证项 | 验证方法 | 通过 |
|----|--------|----------|------|
| DB-01 | `sys_sensitive_word` 表存在且有12条种子 | `SELECT COUNT(*) FROM sys_sensitive_word WHERE status=1` ≥ 12 | ☐ |
| DB-02 | `preview_fail_queue` 表已回填预览失败数据 | `SELECT COUNT(*) FROM preview_fail_queue WHERE status=0` ≥ 0 | ☐ |
| DB-03 | `resource_daily_stats` 有最近30天统计 | `SELECT COUNT(*) FROM resource_daily_stats WHERE stat_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)` = 30 | ☐ |
| DB-04 | `auditor_workload` 有审核员数据 | `SELECT COUNT(*) FROM auditor_workload WHERE stat_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)` ≥ 0 | ☐ |
| DB-05 | `v_low_access_resource` 视图可查询 | `SELECT COUNT(*) FROM v_low_access_resource` 返回数字 | ☐ |
| DB-06 | `resource_audit_log` 有 `idx_auditor_action` 索引 | `SHOW INDEX FROM resource_audit_log WHERE Key_name='idx_auditor_action'` 返回记录 | ☐ |
| DB-07 | `sys_menu` 有质量治理菜单项 | `SELECT COUNT(*) FROM sys_menu WHERE name='AdminQualityShell'` = 1 | ☐ |
| DB-08 | `sys_permission` 有7条质量权限 | `SELECT COUNT(*) FROM sys_permission WHERE code LIKE 'admin:quality:%'` = 7 | ☐ |
| DB-09 | super_admin (role_id=1) 拥有全部质量权限 | `SELECT COUNT(*) FROM sys_role_permission rp JOIN sys_permission p ON rp.permission_id=p.id WHERE rp.role_id=1 AND p.code LIKE 'admin:quality:%'` = 7 | ☐ |

---

## 四、文件变更清单

### 新增文件

| 文件路径 | 说明 |
|---------|------|
| `sql/85_phase8_quality_governance.sql` | DDL + 种子数据 + 数据回填 + 权限 + 验证 |
| `k12-common/entity/SysSensitiveWord.java` | 敏感词 Entity |
| `k12-common/entity/PreviewFailQueue.java` | 预览失败队列 Entity |
| `k12-common/entity/ResourceDailyStats.java` | 资源日统计 Entity |
| `k12-common/entity/AuditorWorkload.java` | 审核员工作量 Entity |
| `k12-common/dto/LowAccessResourceVO.java` | 低访问资源 VO |
| `k12-common/dto/SensitiveWordDTO.java` | 敏感词增/改请求 DTO |
| `k12-common/dto/PreviewFailQueueVO.java` | 预览失败队列 VO |
| `k12-common/dto/AuditWorkloadVO.java` | 审核员工作量 VO |
| `k12-common/dto/AuditSlaVO.java` | 审核 SLA VO |
| `k12-common/dto/RejectStatsVO.java` | 驳回原因统计 VO |
| `k12-common/dto/QualityDashboardVO.java` | 质量大盘 VO |
| `k12-common/dto/GrowthTrendVO.java` | 增长趋势 VO |
| `k12-common/dto/DailyStatsPointVO.java` | 日统计数据点 VO |
| `k12-common/dto/PageResult.java` | 通用分页结果 DTO |
| `k12-resource/mapper/SysSensitiveWordMapper.java` | 敏感词 Mapper |
| `k12-resource/mapper/PreviewFailQueueMapper.java` | 预览失败 Mapper |
| `k12-resource/mapper/QualityAnalyticsMapper.java` | 质量分析 Mapper |
| `k12-resource/resources/mapper/QualityAnalyticsMapper.xml` | 质量分析 XML |
| `k12-resource/service/SensitiveWordService.java` | 敏感词 Service 接口 |
| `k12-resource/service/PreviewFailService.java` | 预览失败 Service 接口 |
| `k12-resource/service/QualityAnalyticsService.java` | 质量分析 Service 接口 |
| `k12-resource/service/impl/SensitiveWordServiceImpl.java` | 敏感词 Service 实现 |
| `k12-resource/service/impl/PreviewFailServiceImpl.java` | 预览失败 Service 实现 |
| `k12-resource/service/impl/QualityAnalyticsServiceImpl.java` | 质量分析 Service 实现 |
| `k12-resource/controller/AdminSensitiveWordController.java` | 敏感词 Controller |
| `k12-resource/controller/AdminPreviewFailController.java` | 预览失败 Controller |
| `k12-resource/controller/AdminQualityAnalyticsController.java` | 质量分析 Controller |
| `k12-edu-platform/src/admin/api/quality.ts` | 前端质量 API |
| `k12-edu-platform/src/admin/views/quality/QualityShell.vue` | 质量治理外壳页 |
| `k12-edu-platform/src/admin/views/quality/QualityDashboard.vue` | 质量大盘页 |
| `k12-edu-platform/src/admin/views/quality/SensitiveWords.vue` | 敏感词管理页 |
| `k12-edu-platform/src/admin/views/quality/PreviewFails.vue` | 预览失败队列页 |

### 修改文件

| 文件路径 | 变更说明 |
|---------|---------|
| `k12-edu-platform/src/admin/router/routes.ts` | 添加质量治理路由（quality Shell + 3 子路由） |
| `k12-edu-platform/src/admin/api/resources.ts` | `AdminPrimaryResource` 新增 `fileSafetyStatus`、`previewStatus` 字段 |
| `k12-edu-platform/src/admin/views/audit/ResourceAudit.vue` | 增加文件安全状态列 + 超时筛选 + 安全筛选 |
| `k12-edu-platform/src/admin/views/dashboard/Dashboard.vue` | 增加「质量风险快报」卡片行 + 质量数据并行加载 |

---

## 五、API 接口清单

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/admin/quality/sensitive-words` | `admin:quality:sensitive_view` | 分页查询敏感词 |
| POST | `/api/admin/quality/sensitive-words` | `admin:quality:sensitive_edit` | 新增敏感词 |
| PUT | `/api/admin/quality/sensitive-words/{id}` | `admin:quality:sensitive_edit` | 编辑敏感词 |
| DELETE | `/api/admin/quality/sensitive-words/{id}` | `admin:quality:sensitive_edit` | 删除敏感词 |
| PUT | `/api/admin/quality/sensitive-words/batch-status` | `admin:quality:sensitive_edit` | 批量启用/禁用 |
| GET | `/api/admin/quality/sensitive-words/stats` | `admin:quality:sensitive_view` | 按分类统计 |
| GET | `/api/admin/quality/preview-fails` | `admin:quality:preview_fail` | 分页查询队列 |
| POST | `/api/admin/quality/preview-fails/{id}/handle` | `admin:quality:preview_fail` | 标记已处理 |
| POST | `/api/admin/quality/preview-fails/{id}/ignore` | `admin:quality:preview_fail` | 标记已忽略 |
| GET | `/api/admin/quality/preview-fails/stats` | `admin:quality:preview_fail` | 状态统计 |
| GET | `/api/admin/quality/analytics/dashboard` | `admin:quality:sensitive_view` | 质量大盘 |
| GET | `/api/admin/quality/analytics/sla` | `admin:quality:sla` | 审核 SLA |
| GET | `/api/admin/quality/analytics/workload` | `admin:quality:workload` | 审核员工作量 |
| GET | `/api/admin/quality/analytics/reject-stats` | `admin:quality:sensitive_view` | 驳回原因统计 |
| GET | `/api/admin/quality/analytics/growth` | `admin:quality:sensitive_view` | 增长趋势 |
| GET | `/api/admin/quality/analytics/low-access` | `admin:quality:low_access` | 低访问资源 |

---

## 六、版本记录

| 版本 | 日期 | 作者 | 变更 |
|------|------|------|------|
| 1.0 | 2026-06-25 | AI Agent | 初始版本，Phase 8 全部功能 |
