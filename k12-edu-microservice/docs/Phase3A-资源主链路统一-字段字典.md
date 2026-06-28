# Phase 3A：资源主链路统一 - 字段字典（评审稿）

> 关联文档：`Phase3A-资源主链路统一-接口契约.md`

---

## 1. 核心主键与来源追踪

| 字段 | 类型 | 必填 | 示例 | 说明 |
|---|---|---|---|---|
| `canonicalResourceId` | BIGINT | 是 | `1000231` | 统一资源 ID，建议直接映射 `resource_main.id` |
| `sourceType` | VARCHAR(40) | 是 | `primary_chinese` | 资源来源类型 |
| `contentDomain` | VARCHAR(40) | 是 | `subject_resource` | 业务内容域（频道域） |
| `legacySourceTable` | VARCHAR(80) | 否 | `topic_resource` | 迁移期来源表追踪 |
| `legacySourceId` | BIGINT | 否 | `5108` | 迁移期来源主键追踪 |

---

## 2. 主资源信息（edu_resource）

| 字段 | 类型 | 必填 | 示例 | 说明 |
|---|---|---|---|---|
| `title` | VARCHAR(200) | 是 | `七年级期末冲刺卷` | 资源主标题 |
| `subtitle` | VARCHAR(200) | 否 | `成都版` | 副标题 |
| `description` | TEXT | 否 | `适配2026考纲` | 资源描述 |
| `coverUrl` | VARCHAR(1000) | 否 | `https://.../cover.jpg` | 封面 |
| `resourceType` | VARCHAR(50) | 是 | `exam_paper` | 统一资源类型代码 |
| `isFree` | TINYINT | 是 | `1` | 是否免费（1免费/0付费） |
| `uploaderId` | BIGINT | 否 | `20001` | 上传者 |
| `uploadTime` | DATETIME | 是 | `2026-06-27 09:00:00` | 上传时间 |

---

## 3. 生命周期状态（双状态）

| 字段 | 类型 | 必填 | 示例 | 说明 |
|---|---|---|---|---|
| `auditStatus` | TINYINT | 是 | `1` | 审核状态：-1草稿 0待审 1通过 2驳回 3复审 |
| `publishStatus` | TINYINT | 是 | `0` | 发布状态：0未上架 1已上架 2已下架 3定时 4回收站 |
| `legacyStatus` | TINYINT | 否 | `1` | 兼容旧状态，仅过渡使用 |
| `fileSafetyStatus` | TINYINT | 否 | `2` | 文件安全状态：0未知 1待确认 2安全 3风险 |
| `previewStatus` | TINYINT | 否 | `1` | 预览状态 |

---

## 4. 文件域（edu_resource_file）

| 字段 | 类型 | 必填 | 示例 | 说明 |
|---|---|---|---|---|
| `fileRole` | VARCHAR(20) | 是 | `main` | main/attachment/answer/analysis/audio/video |
| `originalFilename` | VARCHAR(255) | 否 | `期末卷.pdf` | 原始文件名 |
| `fileExt` | VARCHAR(20) | 否 | `pdf` | 扩展名 |
| `mimeType` | VARCHAR(100) | 否 | `application/pdf` | MIME 类型 |
| `ossBucket` | VARCHAR(100) | 否 | `qier-duuyi` | OSS bucket |
| `ossObjectKey` | VARCHAR(500) | 否 | `resource/2026/...` | OSS object key |
| `ossUrl` | VARCHAR(1000) | 否 | `https://...` | 访问地址 |
| `previewUrl` | VARCHAR(1000) | 否 | `https://.../preview` | 预览地址 |
| `allowPreview` | TINYINT | 是 | `1` | 是否允许预览 |

---

## 5. 维度域（edu_resource_dimension）

| 字段 | 类型 | 必填 | 示例 | 说明 |
|---|---|---|---|---|
| `stageId` | INT | 否 | `2` | 学段 ID |
| `subjectId` | INT | 否 | `21` | 学科 ID |
| `gradeId` | INT | 否 | `3` | 年级 ID |
| `editionId` | INT | 否 | `5` | 版本 ID |
| `volumeId` | INT | 否 | `12` | 册别 ID |
| `moduleId` | INT | 否 | `8` | 栏目 ID |
| `resourceTypeId` | INT | 否 | `10` | 资源类型 ID |
| `regionId` | INT | 否 | `510100` | 地区 ID（可选） |

---

## 6. 挂载域（edu_resource_placement）

| 字段 | 类型 | 必填 | 示例 | 说明 |
|---|---|---|---|---|
| `placementType` | VARCHAR(30) | 是 | `catalog_node` | 挂载类型：catalog_node/channel/topic/home_panel |
| `catalogNodeId` | BIGINT | 否 | `14012` | 教材目录节点 |
| `channelKey` | VARCHAR(50) | 否 | `topic` | 频道编码 |
| `topicKey` | VARCHAR(50) | 否 | `midterm_final` | 专题编码 |
| `homePanelKey` | VARCHAR(50) | 否 | `sync_prep` | 首页板块编码 |
| `isPrimary` | TINYINT | 是 | `1` | 主挂载标识 |
| `sort` | INT | 否 | `100` | 排序值 |

---

## 7. 频道域与来源域枚举字段

| 字段 | 值集合（建议） |
|---|---|
| `contentDomain` | `subject_resource` / `topic` / `culture` / `competition` / `class_meeting` / `news_related` |
| `sourceType` | `primary_chinese` / `topic_resource` / `culture_resource` / `competition_resource` / `article` / `edu_resource` |
| `placementType` | `catalog_node` / `channel` / `topic` / `home_panel` |

---

## 8. 字段治理规则

- 所有前台返回对象必须包含 `canonicalResourceId`，禁止再返回“仅源表 id”。
- 管理端动作 API 统一接收 `canonicalResourceId`，服务内部解析 `sourceType+sourceId`。
- `legacySourceTable/legacySourceId` 仅迁移期使用，Phase 3G 后可降级为审计字段。
- 新增字段必须先进入字段字典，再允许进接口 DTO 与 SQL。

