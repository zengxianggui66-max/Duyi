# 管理端第二阶段详细开发方案

## 1. 阶段目标

第二阶段不是简单继续增加后台菜单，而是把平台升级为“前台资源业务闭环 + 管理端内容治理闭环”。

核心目标：

- 前台上传资源形成标准流程：保存草稿、提交审核、审核驳回、修改重提、审核通过、上架展示、下架返修。
- 前台资源列表、资源详情、搜索筛选、上传页、个人中心全部使用统一分类、目录、字典数据源。
- 管理端审核中心从“人工通过/驳回”升级为可预览、可质检、可追责、可统计的内容治理台。
- 后端从单一小学语文资源表逐步过渡到统一资源审核域，避免后续数学、英语、初中、高中、专题、试卷各走一套逻辑。
- 不开发会员、订单、支付、会员权益、财务等模块。本系统定位为内部成员资源平台，优先建设权限、审核、资源质量和运营治理。

第二阶段完成后，平台应能证明：

- 用户知道自己上传资源处于什么状态。
- 管理员知道哪些资源需要审核、为什么驳回、谁审核过、何时审核过。
- 前台只展示已审核且已上架资源。
- 分类目录来自后台统一配置，不再出现“小学显示七八九年级”这类行业不规范问题。
- 每个关键动作都可审计、可追责、可测评。

## 2. 现有系统对应关系

### 2.1 前台业务面

当前前台重点页面和逻辑：

- 上传入口：`src/views/resource/ResourceUpload.vue`
- 上传表单逻辑：`src/composables/useResourceUploadForm.ts`
- 我的资源：`src/views/user/MyResources.vue`
- 我的资源逻辑：`src/composables/useMyResources.ts`
- 学科资源页：`src/views/resource/SubjectDetailPage.vue`
- 资源 API：`src/api/primaryChinese.ts`
- 前台 taxonomy API：`src/api/taxonomy.ts`
- 前台 catalog API：`src/api/catalog.ts`
- 当前仍需治理的本地常量：`src/constants/uploadOptions.ts`

前台二期的关键不是换页面，而是让这些页面围绕同一套资源生命周期工作。

### 2.2 管理端治理面

当前管理端已经具备一期骨架：

- 资源管理：`src/admin/views/resources/ResourceList.vue`
- 资源详情：`src/admin/views/resources/ResourceDetail.vue`
- 待审队列：`src/admin/views/audit/ResourceAudit.vue`
- 审核记录：`src/admin/views/audit/AuditRecords.vue`
- 驳回模板：`src/admin/views/audit/RejectReasons.vue`
- 分类维度：`src/admin/views/taxonomy/Taxonomy.vue`
- 教材目录树：`src/admin/views/catalog/CatalogTree.vue`
- 字典与标签：`src/admin/views/dictionary/*`
- 搜索运营：`src/admin/views/search/*`
- 数据统计：`src/admin/views/analytics/*`
- 系统日志与配置：`src/admin/views/system/*`

二期要把这些管理能力和前台实际业务强绑定：

- 上传页的学段、年级、学科、版本、册别、栏目、类型，必须来自管理端维护的数据。
- 我的资源展示的草稿、待审、驳回、已发布、已下架，必须和管理端审核状态一致。
- 审核中心的通过、驳回、下架、返修，必须反映到前台资源可见性。
- 首页运营、搜索热词、推荐资源，只能选择已审核且已上架资源。

### 2.3 后端服务面

后端继续采用现有微服务架构，不单独重做管理端后端：

- `k12-auth`：登录、管理员身份、角色、权限。
- `k12-resource`：资源上传、草稿、审核、发布、目录、字典、资源质量。
- `k12-gateway`：统一入口、鉴权、限流、日志。
- `k12-common`：公共实体、DTO、常量。
- 其他服务继续按业务边界演进，二期不新增会员订单能力。

管理端 API 统一走：

- `/api/admin/resources/**`
- `/api/admin/audit/**`
- `/api/admin/taxonomy/**`
- `/api/admin/catalog/**`
- `/api/admin/dictionaries/**`
- `/api/admin/search/**`
- `/api/admin/system/**`

## 3. 标准资源生命周期

第二阶段必须统一资源状态模型，避免把“审核通过”和“上架发布”混成一个状态。

推荐状态：

| 业务动作 | audit_status | publish_status | 前台可见 | 我的资源位置 | 管理端位置 |
| --- | --- | --- | --- | --- | --- |
| 保存草稿 | 草稿 | 未上架 | 否 | 草稿箱 | 不进入待审 |
| 提交审核 | 待审 | 未上架 | 否 | 待审核 | 审核中心 |
| 审核驳回 | 驳回 | 未上架 | 否 | 未通过 | 审核记录 |
| 修改重提 | 待审 | 未上架 | 否 | 待审核 | 审核中心 |
| 审核通过 | 通过 | 未上架或已上架 | 取决于发布状态 | 已通过/已发布 | 资源管理 |
| 上架发布 | 通过 | 已上架 | 是 | 已发布 | 资源管理 |
| 管理下架 | 通过或复审中 | 已下架 | 否 | 已下架 | 资源管理 |
| 回收站 | 通过/驳回/草稿 | 回收站 | 否 | 不默认展示 | 回收站 |

行业要求：

- 草稿不进入搜索、推荐、首页、下载。
- 待审不进入前台列表。
- 驳回必须有原因，且用户可基于驳回内容复制为草稿重新提交。
- 审核通过不等于必须立即上架，后续要支持定时上架和复审。
- 下架不是物理删除，资源和日志仍可追溯。

## 4. 第二阶段模块设计

### 4.1 上传资源标准化

目标：把上传页从“表单提交”升级为“平台资源入库流程”。

前台功能：

- 上传入口保留在当前主站顶部和个人中心。
- 按来源自动带入上下文：学段、学科、版本、册别、目录节点、栏目、资源类型。
- 支持直接上传和从学科页带上下文上传。
- 保存草稿：进入个人中心“我的资源 - 草稿箱”。
- 提交审核：进入个人中心“待审核”，同时进入管理端审核中心。
- 驳回后：用户在“未通过”中查看驳回原因、审核人、审核时间，可复制为草稿后重新提交。
- 上传成功按钮文案统一改为“提交审核”，不要再叫“发布资源”。
- 资源标题建议自动生成，但允许用户编辑。
- 文件预览失败不阻断保存草稿，但提交审核前要提示预览状态。

后端规则：

- `saveDraft` 只保存为草稿，不触发审核。
- `submitDraft` 校验必填元数据、文件状态、分类目录有效性。
- `cloneRejectedToDraft` 必须保留原驳回资源和审核记录，不能覆盖历史。
- 提交审核后用户不能直接编辑原记录，只能撤回或等待审核；如需修改，走复制草稿或撤回流程。
- 所有上传、保存草稿、提交审核、撤回、删除草稿都写用户操作日志。

需要优化的字段：

- `stage`：学段。
- `grade`/`gradeName`：年级。
- `subject`：学科。
- `edition`/`editionName`：教材版本。
- `volume`：册别。
- `module`：栏目。
- `catalogNodeId`：目录节点。
- `catalogPath`：目录路径。
- `resourceType`/`teachingType`：资源类型。
- `tags`：资源标签。
- `source`：资源来源。
- `copyrightStatus`：版权状态。
- `fileSafetyStatus`：文件安全状态。
- `previewStatus`：预览状态。

### 4.2 统一分类、目录、字典源

目标：解决前台各页面常量分散、联动不一致的问题。

必须统一的数据：

- 学段：小学、初中、高中、素质拓展等。
- 年级：小学一至六年级，初中七至九年级，高中高一至高三。
- 学科：按学段绑定。
- 教材版本：按学段、学科绑定。
- 册别：按学段、年级、学科绑定。
- 目录树：按学段、学科、版本、册别绑定。
- 栏目：同步备课、试卷、精品资料、专题资源等。
- 资源类型：课件、教案、学案、试卷、练习、素材、视频、音频等。
- 标签：同步、精品、免费、有答案、文字版等。
- 地区、年份、考试类型、难度、使用场景。

前台接入原则：

- 上传页不再长期依赖 `src/constants/uploadOptions.ts`。
- 资源列表筛选不再自己维护一套年级、学科、资源类型。
- 搜索筛选和上传筛选来自同一套 taxonomy/catalog/dictionary API。
- 本地常量只保留兜底默认值和 UI 展示映射，不作为主数据源。

后台校验原则：

- 小学阶段只能保存一年级至六年级。
- 初中阶段只能保存七年级至九年级。
- 高中阶段只能保存高一至高三。
- 学科必须属于当前学段。
- 教材版本必须属于当前学段和学科。
- 目录节点必须属于当前版本和册别。
- 禁用的分类、目录、字典不能用于新资源提交。

### 4.3 我的资源中心优化

目标：让用户清楚知道自己上传资源在哪里、为什么没通过、下一步能做什么。

页面结构：

- 全部。
- 草稿箱。
- 待审核。
- 未通过。
- 已通过。
- 已发布。
- 已下架。

每条资源展示：

- 标题。
- 学段、年级、学科。
- 教材版本、册别、目录路径。
- 栏目、资源类型、标签。
- 文件格式、文件大小、预览状态。
- 审核状态、发布状态。
- 上传时间、更新时间。
- 审核人、审核时间、驳回原因。

按状态允许的操作：

| 状态 | 用户操作 |
| --- | --- |
| 草稿 | 编辑、提交审核、删除草稿 |
| 待审核 | 查看、撤回审核申请 |
| 驳回 | 查看原因、复制为草稿、重新提交 |
| 审核通过未上架 | 查看、等待上架 |
| 已发布 | 查看前台页、查看数据 |
| 已下架 | 查看下架原因、复制为草稿重提 |

关键细节：

- “保存草稿”保存到个人中心草稿箱。
- 草稿只属于上传者本人，管理员可在资源管理中按权限查看，但不进入审核队列。
- 驳回资源不允许直接覆盖，应复制新草稿，保证历史可追溯。
- 我的资源统计要拆分草稿、待审、驳回、已发布、已下架。

### 4.4 管理端审核中心强化

目标：审核中心从一期的队列审核，升级为内容质量治理台。

页面：

- `/admin/audit/resources`：待审资源队列。
- `/admin/audit/records`：审核记录。
- `/admin/audit/reject-reasons`：驳回模板。
- `/admin/audit/quality`：质量检测队列，二期可新增。

待审队列筛选：

- 学段。
- 年级。
- 学科。
- 教材版本。
- 册别。
- 栏目。
- 资源类型。
- 上传者。
- 文件格式。
- 预览状态。
- 文件安全状态。
- 敏感词状态。
- 重复检测状态。
- 提交时间。
- 超时状态。

审核预览抽屉：

- 资源基础信息。
- 文件预览。
- 目录路径。
- 上传者信息。
- 历史审核记录。
- 敏感词提示。
- 重复资源提示。
- 文件安全状态。
- 预览服务状态。
- 版权/来源字段。

审核动作：

- 通过。
- 驳回。
- 批量通过。
- 批量驳回。
- 转交复审。
- 标记低质。
- 标记版权风险。

驳回原因模板：

- 分类错误。
- 年级/学段不匹配。
- 学科不匹配。
- 教材版本或册别错误。
- 目录节点错误。
- 标题不规范。
- 文件无法预览。
- 文件格式不符合要求。
- 内容重复。
- 内容质量不足。
- 存在敏感词。
- 版权或来源不清。

审核日志字段：

- `resource_id`
- `auditor_id`
- `action`
- `before_audit_status`
- `after_audit_status`
- `before_publish_status`
- `after_publish_status`
- `reason`
- `quality_flags`
- `created_at`

### 4.5 统一资源审核域

目标：从“小学语文主资源库”过渡到平台级资源治理模型。

短期做法：

- 继续兼容 `oss_primary_chinese_resource`。
- 使用 `v_admin_resource_main` 作为统一管理视图。
- 管理端资源列表和审核中心逐步改为读取统一资源视图。

中期做法：

- 新增标准主表 `resource_main`。
- 各业务资源表作为扩展表或历史来源。
- 所有新上传资源进入 `resource_main`。

统一字段建议：

- `id`
- `source_type`
- `business_type`
- `title`
- `description`
- `stage`
- `grade`
- `subject`
- `edition`
- `volume`
- `module`
- `catalog_node_id`
- `catalog_path`
- `resource_type`
- `tags`
- `uploader_id`
- `org_id`
- `file_id`
- `file_name`
- `file_ext`
- `file_size`
- `oss_url`
- `preview_status`
- `file_safety_status`
- `sensitive_status`
- `duplicate_status`
- `audit_status`
- `publish_status`
- `visibility`
- `source`
- `copyright_status`
- `created_at`
- `updated_at`
- `deleted_at`

前台查询原则：

- 公开资源列表必须加条件：`audit_status = 通过` 且 `publish_status = 已上架`。
- 我的资源可以查询本人草稿、待审、驳回、下架。
- 管理端按权限查询全量或数据权限范围内的资源。

### 4.6 权限、数据范围和追责

目标：达到内部平台、后期测评和上市前治理要求。

角色继续沿用：

- 超级管理员。
- 内容管理员。
- 审核员。
- 运营人员。
- 客服/用户管理员。
- 系统管理员。

权限分三层：

- 菜单权限：能不能进入模块。
- 操作权限：能不能新增、编辑、删除、审核、上下架、导出。
- 数据权限：能看哪些学段、学科、地区、组织、上传者的数据。

二期新增重点：

- 审核员只能处理自己权限范围内的学段、学科资源。
- 内容管理员可以编辑元数据，但不一定能审核。
- 运营人员可以配置首页推荐，但只能选择已发布资源。
- 系统管理员可以改配置，但不能绕过内容审核。
- 导出资源和导出用户数据必须单独授权。

必须写操作日志的动作：

- 登录后台。
- 修改角色权限。
- 修改数据权限。
- 保存草稿。
- 提交审核。
- 审核通过。
- 审核驳回。
- 批量审核。
- 上架。
- 下架。
- 删除或恢复。
- 修改分类、目录、字典。
- 修改首页运营配置。
- 修改系统配置。
- 导出数据。

日志最低字段：

- 操作者。
- 操作模块。
- 操作动作。
- 资源或对象 ID。
- 请求路径。
- 请求参数摘要。
- 操作前状态。
- 操作后状态。
- IP。
- User-Agent。
- 耗时。
- 成功或失败。
- 错误信息。

### 4.7 资源质量治理

目标：让审核不只是人工判断，而是有规则、有提示、有数据。

二期先做轻量能力：

- 敏感词词库维护。
- 标题敏感词检测。
- 描述敏感词检测。
- 文件名敏感词检测。
- 重复资源检测：先按文件 hash、标题、文件大小做基础匹配。
- 文件安全状态：待检测、安全、风险、未知。
- 预览失败队列。
- 低质资源标记。
- 版权/来源字段。
- 驳回原因统计。
- 审核员工作量统计。

后续增强：

- 正文 OCR/解析后敏感词检测。
- 相似度算法。
- 病毒扫描服务。
- 版权风险规则。
- AI 辅助审核建议。

### 4.8 前台资源列表和搜索映射

目标：前台用户看到的资源必须是治理后的结果。

资源列表：

- 只展示审核通过且已上架资源。
- 支持按统一 taxonomy/catalog/dictionary 筛选。
- 目录树点击后只查询该目录及允许的子目录范围。
- 列表标签来自后台标签字典。
- 已下架资源访问详情页时提示“资源已下架”。

搜索：

- 只索引已审核且已上架资源。
- 下架后要从搜索索引移除或标记不可见。
- 搜索无结果词进入管理端搜索运营。
- 热门搜索词可由后台配置或由统计生成。
- 搜索重定向和同义词由管理端维护。

首页运营：

- Banner、快捷入口、推荐资源、专题入口只能引用合法发布资源或合法频道。
- 已下架资源自动从首页推荐候选中移除。
- 首页配置修改写操作日志。

## 5. 开发顺序

### 第一步：二期基础验收线

目标：确保一期基础稳定，然后再扩展。

开发内容：

- 确认前端 `npm run build` 通过。
- 确认后端使用 `mvnw.cmd` 编译通过。
- 补充管理端 smoke test 清单。
- 整理乱码文件优先级，不一次性大范围改动业务文件。
- 确认 `/admin` 未登录、普通用户、无权限管理员的路由行为。
- 确认后端 `/api/admin/**` 不是只靠前端隐藏按钮。

验收标准：

- 前端可构建。
- 后端可编译。
- 管理员能进入后台。
- 普通用户不能进入后台。
- 无权限按钮不可见，接口也不可调用。
- 操作日志能记录核心动作。

**落地（Step 1 已完成）**：

- 文档：`k12-edu-platform/docs/Phase2-Step1-Smoke.md`、`Phase2-Mojibake-Inventory.md`
- 脚本：`k12-edu-microservice/scripts/phase2-step1-baseline-acceptance.mjs`（含 Phase 9 回归）
- SQL：`sql/81_phase2_step1_baseline.sql`（normal_user / staff_no_role）
- 权限修复：`/api/admin/resources/stats` 统一为 `admin:analytics:view`

### 第二步：上传页数据源改造

目标：先解决“小学出现七八九年级”这类前台业务错误。

开发内容：

- 上传页学段从 `/api/taxonomy/stages` 加载。
- 年级从 `/api/taxonomy/grades?stageId=...` 加载。
- 学科从 `/api/taxonomy/subjects?stageId=...` 加载。
- 教材版本从 `/api/taxonomy/editions` 按学段、学科过滤。
- 册别从 `/api/taxonomy/volumes` 加载。
- 栏目从 `/api/taxonomy/modules` 加载。
- 资源类型从 `/api/taxonomy/resource-types` 加载。
- 单元/课文从 `/api/catalog/tree` 加载。
- `uploadOptions.ts` 改为兜底，不再作为主数据源。

验收标准：

- 小学只显示一至六年级。
- 初中只显示七至九年级。
- 高中只显示高一至高三。
- 选择学段后，学科、年级、栏目同步刷新。
- 选择版本和册别后，目录树按条件加载。
- 禁用分类不会出现在上传页。

**阶段 0–1 已落地（后端）**：

- 文档：`Phase2-Step2-Baseline.md`、`Phase2-Step2-Catalog-Upload.md`、`docs/Phase2-Step2-验收.md`
- API：`GET /api/taxonomy/grades?stage=primary`
- 校验：`UploadPlacementValidator`（saveDraft/submit/save）
- `UploadPlacementService` 纯 taxonomy，不再 merge DISTINCT
- 脚本：`scripts/phase2-step2-upload-taxonomy-acceptance.mjs`

**阶段 2–3 已落地（前端读源 + 主上传表单）**：

- 统一读源：`src/composables/taxonomySource.ts`、`src/composables/catalogSource.ts`；管理端保存后 `invalidateTaxonomyCache` / `invalidateCatalogCache`
- 级联 composable：`src/composables/useUploadTaxonomyCascade.ts`（学段→学科/年级/栏目→版本/册别/资源类型→目录树；选中节点写入 `catalogNodeId`）
- 中枢：`useResourceUploadForm.ts` 移除 `subjectsByGrade` / `gradesByGrade` / `textbookVersionsByGrade` 主路径；`currentSubjects` / `currentGrades` 由 API 异步加载；`refreshDictionaryOptions` 保留（标签/考试场景走 dictionary API）
- 步骤组件：`UploadStepBasic.vue` 学段 `v-for="stage in stageOptions"`；学科/年级绑定 `{ label: name, value: code }`；`fromBrowse` 锁定上下文保留
- `UploadStepPlacement.vue` 去掉 `primaryChineseApi.getUploadFilterOptions` merge；册别仅 `taxonomyApi.getVolumes`；目录树 `catalogApi.getTree`；节点选中同步 `catalogNodeId`
- 次要入口：`ResourceUploadDialog.vue` 复用 `useUploadTaxonomyCascade`
- 构建：`npm run build` 通过

**阶段 4–5 已落地（uploadOptions 降级 + 联动闭环）**：

- `uploadOptions.ts` 文件头 `@deprecated`；仅 `taxonomySource` / `dictionarySource` 在 API catch 分支引用
- `constants/index.ts` 不再 re-export `uploadOptions`；子类型迁至 `uploadResourceSubTypes.ts`
- 离线兜底：`taxonomyUsingOfflineFallback` 标记 API 失败；上传页 banner + Toast；`canSubmit=false` 但草稿仍可保存
- 边界：学科页带入锁定学段/学科；切换栏目按 `uploadSchema` 显隐 catalog 并重载 resource-types；切换册别/版本清空 unit/lesson 并重载目录树
- 素质拓展（美术/舞蹈）年级走 `GET /api/taxonomy/grades`（edu_grade）；C 端 API 默认 `includeDisabled=false`

**阶段 6 已落地（验收与自动化）**：

- 验收矩阵：`docs/Phase2-Step2-验收.md`（528–535 ↔ T1–T20 / F-U1–F-U4）
- 脚本：`scripts/phase2-step2-upload-taxonomy-acceptance.mjs`（23 项：年级、级联、catalog、禁用、权限）
- 手工 Smoke：F-U1 全流程、F-U2 名称同步、F-U3 禁用版本、F-U4 离线兜底

### 第三步：上传资源生命周期改造

目标：让用户上传后进入标准审核流程。

开发内容：

- 上传页按钮文案统一为“保存草稿”和“提交审核”。
- 保存草稿调用 `saveDraft`。
- 提交审核调用 `submitDraft` 或上传后直接创建待审记录。
- 提交审核前校验元数据、文件、目录、资源类型。
- 文件预览状态、文件安全状态写入资源记录。
- 提交审核成功后跳转“我的资源 - 待审核”。
- 保存草稿成功后跳转“我的资源 - 草稿箱”或留在当前页提示。

验收标准：

- 保存草稿后前台搜索不到。
- 草稿能在个人中心草稿箱看到。
- 草稿能继续编辑并提交审核。
- 提交审核后管理端审核中心可见。
- 提交审核后前台搜索不到。

**阶段 3 已落地（2026-06）**：

- 后端：`PublicResourceQuerySupport` 公开 list/browse 默认 `status=1`；`POST /save` 禁用；`POST /draft/submit` 聚合；`sql/83` preview/safety 列
- 前端：主上传页统一 `saveDraft→submitDraft`；提交后跳 `/my-resources?status=pending`；我的资源 Tab + query
- 文档/脚本：`docs/Phase2-Step3-*.md`；`scripts/phase2-step3-upload-lifecycle-acceptance.mjs`
- **部署**：修改 Java 后需重启 k12-resource(8082) 再跑 T25–T30

### 第四步：我的资源中心改造

目标：用户能完整理解资源状态和下一步动作。

开发内容：

- 增加或优化状态 Tab：全部、草稿箱、待审核、未通过、已通过、已发布、已下架。
- 展示审核状态和发布状态。
- 展示驳回原因、审核人、审核时间。
- 驳回资源支持复制为草稿。
- 草稿支持删除。
- 待审资源支持查看，后续可增加撤回。
- 已发布资源支持跳转前台详情。

验收标准：

- 草稿、待审、驳回、已发布、已下架数量正确。
- 驳回原因可见。
- 驳回资源复制为草稿后，原记录仍保留。
- 已下架资源不在前台公开列表出现。

### 第五步：管理端审核中心强化

目标：审核员能高效、规范、可追责地审核资源。

开发内容：

- 待审队列增加学段、年级、学科、栏目、类型、预览状态、文件安全状态筛选。
- 审核预览抽屉展示资源元数据、目录路径、文件信息、质量提示。
- 驳回模板按问题类型分类。
- 批量审核记录每条资源结果。
- 审核记录支持按资源、审核员、动作、时间筛选。
- 审核通过只改变 `audit_status`，是否自动上架由系统配置或按钮决定。

验收标准：

- 审核通过后写审核日志。
- 审核驳回必须填写原因。
- 批量审核返回成功和失败数量。
- 无审核权限的管理员不能调用审核接口。
- 审核记录可追溯。

### 第六步：管理端分类目录治理

目标：让后台维护的数据真正控制前台。

开发内容：

- 分类维度增加唯一性校验。
- 分类维度增加启用/禁用状态。
- 学科绑定学段。
- 年级绑定学段。
- 版本绑定学段和学科。
- 目录树绑定学段、学科、版本、册别。
- 删除分类前检查是否已有资源使用。
- 已被资源引用的分类只能禁用，不能物理删除。

验收标准：

- 管理端禁用某年级后，上传页不可选择。
- 已有关联资源的目录节点不能直接删除。
- 目录树调整不破坏已发布资源详情页。
- 前台筛选、上传页、管理端使用同一套数据。

### 第七步：统一资源审核域落地

目标：为多学科、多学段、多资源类型扩展打基础。

开发内容：

- 管理端资源列表优先读取统一视图。
- 审核中心优先读取统一视图。
- API 返回 `auditStatus` 和 `publishStatus`。
- 保留 legacy `status` 兼容旧前台。
- 新增统一资源 DTO。
- 设计 `resource_main` 表迁移方案。

验收标准：

- 管理端能看到不同学段、不同学科资源。
- 前台旧功能不受影响。
- 新旧状态字段一致。
- 后续新增数学、英语、试卷资源不需要重做一套审核中心。

### 第八步：质量治理和运营统计

目标：补齐后期测评会关注的运营、审核和质量数据。

开发内容：

- 敏感词词库。
- 文件安全状态。
- 重复资源提示。
- 预览失败队列。
- 驳回原因统计。
- 审核员工作量统计。
- 审核 SLA：平均审核时长、超时待审。
- 资源增长、通过率、驳回率、下架率。
- 搜索无结果词、热门资源、低访问资源。

验收标准：

- 审核中心能看到质量提示。
- 管理端能看到资源质量大盘。
- 搜索无结果词能进入后台。
- 审核超时资源能筛选。

## 6. 推荐接口规划

### 6.1 前台上传和我的资源

```http
GET    /api/taxonomy/stages
GET    /api/taxonomy/grades?stageId={stageId}
GET    /api/taxonomy/subjects?stageId={stageId}
GET    /api/taxonomy/editions?stageId={stageId}&subjectId={subjectId}
GET    /api/taxonomy/volumes?stageId={stageId}&subjectId={subjectId}&editionId={editionId}
GET    /api/taxonomy/modules?stageId={stageId}
GET    /api/taxonomy/resource-types?stageId={stageId}&subjectId={subjectId}&moduleId={moduleId}
GET    /api/catalog/tree?stageId={stageId}&subjectId={subjectId}&editionId={editionId}&volumeId={volumeId}

POST   /api/resources/drafts
PUT    /api/resources/drafts/{id}
POST   /api/resources/drafts/{id}/submit
DELETE /api/resources/drafts/{id}
POST   /api/resources/rejected/{id}/clone-draft
GET    /api/resources/mine
GET    /api/resources/mine/stats
GET    /api/resources/mine/{id}/audit-info
```

### 6.2 管理端审核和资源治理

```http
GET    /api/admin/audit/resources
GET    /api/admin/audit/logs
POST   /api/admin/resources/{id}/audit
POST   /api/admin/resources/batch-audit
GET    /api/admin/audit/reject-reasons
POST   /api/admin/audit/reject-reasons
PUT    /api/admin/audit/reject-reasons/{id}
PUT    /api/admin/audit/reject-reasons/{id}/status

GET    /api/admin/resources
GET    /api/admin/resources/{id}
PUT    /api/admin/resources/{id}
POST   /api/admin/resources/{id}/publish
POST   /api/admin/resources/{id}/offline
POST   /api/admin/resources/{id}/restore
GET    /api/admin/resources/{id}/audit-insights
```

### 6.3 管理端分类目录

```http
GET    /api/admin/taxonomy/stages
GET    /api/admin/taxonomy/grades
GET    /api/admin/taxonomy/subjects
GET    /api/admin/taxonomy/editions
GET    /api/admin/taxonomy/volumes
GET    /api/admin/taxonomy/modules
GET    /api/admin/taxonomy/resource-types

GET    /api/admin/catalog/schemes
GET    /api/admin/catalog/tree
GET    /api/admin/catalog/nodes
POST   /api/admin/catalog/nodes
PUT    /api/admin/catalog/nodes/{id}
PUT    /api/admin/catalog/nodes/{id}/status
DELETE /api/admin/catalog/nodes/{id}
```

## 7. 数据表和迁移建议

二期优先补强以下表或视图：

- `resource_audit_log`：审核记录。
- `admin_reject_reason`：驳回原因模板。
- `resource_quality_check`：资源质量检测记录。
- `resource_sensitive_word`：敏感词词库。
- `resource_duplicate_check`：重复资源检测记录。
- `resource_file_check`：文件安全检测记录。
- `resource_main`：中期统一资源主表。
- `v_admin_resource_main`：短期统一资源管理视图。
- `sys_operation_log`：操作日志。
- `sys_data_scope`：数据权限范围。

迁移策略：

- 第一阶段保留旧 `status`。
- 第二阶段接口同时返回 `status`、`auditStatus`、`publishStatus`。
- 第三阶段前台公开查询改用 `auditStatus + publishStatus`。
- 第四阶段管理端和审核中心切到统一视图。
- 第五阶段新增资源写入 `resource_main`，旧表逐步作为历史源。

## 8. 安全和测评要求

必须满足：

- 后端接口权限校验不能只依赖前端路由。
- 管理员操作必须有日志。
- 数据导出必须单独授权。
- 文件上传必须限制格式、大小、数量。
- 文件预览失败不能误判为审核通过。
- 敏感配置不能暴露到前端。
- 删除必须软删除或进入回收站。
- 审核、上架、下架、驳回必须可追溯。
- 前台只展示合法发布资源。
- 分类、目录、标签统一来源。

建议测试覆盖：

- 权限测试。
- 状态流转测试。
- 上传校验测试。
- 分类联动测试。
- 审核接口测试。
- 前台可见性测试。
- 操作日志测试。
- 构建和编译测试。

## 9. 第二阶段总体验收清单

### 9.1 上传闭环

- 保存草稿成功。
- 草稿在个人中心可见。
- 草稿不在前台搜索和列表出现。
- 提交审核成功。
- 待审资源进入管理端队列。
- 驳回资源显示原因。
- 驳回资源可复制为草稿。
- 审核通过后按发布状态展示。
- 下架资源前台不可见。

### 9.2 分类目录

- 小学只显示一至六年级。
- 初中只显示七至九年级。
- 高中只显示高一至高三。
- 学科随学段联动。
- 版本随学段和学科联动。
- 目录随版本和册别联动。
- 前台筛选和上传页数据一致。
- 管理端禁用项前台不可选。

### 9.3 审核中心

- 待审队列筛选可用。
- 预览抽屉显示完整元数据。
- 通过写审核日志。
- 驳回必须填写原因。
- 批量审核结果准确。
- 无权限不能审核。
- 审核记录可按条件查询。

### 9.4 资源公开展示

- 只有审核通过且已上架资源进入前台。
- 草稿、待审、驳回、下架不公开。
- 首页推荐不能选择非发布资源。
- 搜索索引不包含未发布资源。
- 已下架资源详情页有合理提示。

### 9.5 工程质量

- `npm run build` 通过。
- `mvnw.cmd -pl k12-resource -am -DskipTests compile` 通过。
- 新增接口有权限校验。
- 新增关键动作有操作日志。
- 不引入会员、订单、支付、会员权益逻辑。
- 乱码文件按模块逐步清理，不在一次迭代中无差别重写全站。

## 10. 建议排期

### Sprint 1：上传数据源和状态闭环

- 上传页接入 taxonomy/catalog/dictionary API。
- 修正上传按钮、保存草稿、提交审核文案和状态。
- 我的资源补齐草稿、待审、驳回、已发布、已下架。
- 验收“小学不出现七八九年级”。

### Sprint 2：审核中心细节强化

- 待审队列筛选增强。
- 审核预览抽屉补质量信息。
- 驳回模板分类。
- 批量审核结果明细。
- 审核日志查询增强。

### Sprint 3：统一资源域和前台可见性

- 管理端切换统一资源视图。
- 前台公开查询强制 `audit_status + publish_status`。
- 首页推荐、搜索索引过滤未发布资源。
- 设计并准备 `resource_main` 迁移。

### Sprint 4：质量治理和测评数据

- 敏感词词库。
- 重复资源检测基础版。
- 文件安全状态。
- 预览失败队列。
- 审核 SLA。
- 资源质量和审核统计大盘。

## 11. 最终执行建议

第二阶段先做前台资源业务标准化，再强化管理端审核中心，最后做质量治理和统计。原因是管理端的价值必须落在前台真实业务流上：用户上传什么、如何分类、谁审核、为什么驳回、什么时候公开展示，这些环节先统一，后续再扩展多学科、多资源类型、专题资源、搜索运营和质量测评才不会散。

当前最优先的开发顺序：

1. 上传页统一数据源。
2. 保存草稿和提交审核闭环。
3. 我的资源状态页完善。
4. 审核中心筛选、预览、驳回、日志强化。
5. 前台公开展示统一按审核状态和发布状态过滤。
6. 管理端分类目录治理规则补齐。
7. 统一资源审核域落地。
8. 质量治理和运营统计。

