# Phase 3A：资源主链路统一 - 枚举与状态机（评审稿）

> 关联文档：`Phase3A-资源主链路统一-接口契约.md`、`Phase3A-资源主链路统一-字段字典.md`

---

## 1. 统一枚举定义

## 1.1 `auditStatus`

| 值 | 名称 | 含义 |
|---|---|---|
| `-1` | DRAFT | 草稿态，未进入审核流程 |
| `0` | PENDING | 待审核 |
| `1` | APPROVED | 审核通过 |
| `2` | REJECTED | 审核驳回 |
| `3` | RECHECKING | 复审中（可选扩展） |

## 1.2 `publishStatus`

| 值 | 名称 | 含义 |
|---|---|---|
| `0` | UNPUBLISHED | 未上架 |
| `1` | PUBLISHED | 已上架（前台可见） |
| `2` | OFFLINE | 已下架 |
| `3` | SCHEDULED | 定时上架 |
| `4` | RECYCLED | 回收站 |

## 1.3 `contentDomain`

| 值 | 名称 |
|---|---|
| `subject_resource` | 学段学科资源 |
| `topic` | 专题资源 |
| `culture` | 传统文化 |
| `competition` | 竞赛专区 |
| `class_meeting` | 主题班会 |
| `news_related` | 资讯关联附件 |

## 1.4 `sourceType`

| 值 | 名称 |
|---|---|
| `primary_chinese` | 小学语文宽表来源 |
| `topic_resource` | 专题来源 |
| `culture_resource` | 文化来源 |
| `competition_resource` | 竞赛来源 |
| `article` | 资讯来源 |
| `edu_resource` | 主表来源 |

---

## 2. 生命周期状态机（双状态解耦）

## 2.1 状态原则

- 审核与上架解耦：`auditStatus` 只表达审核结论，`publishStatus` 只表达前台可见性。
- 上架前置条件：`auditStatus=1`（APPROVED）。
- 回收站优先级最高：`publishStatus=4` 时不参与前台曝光。

## 2.2 状态流转

```text
草稿(-1,0)
  -> 提交审核
待审(0,0)
  -> 审核通过
已通过(1,0)
  -> 发布
已上架(1,1)
  -> 下架
已下架(1,2)
  -> 再发布
已上架(1,1)

待审(0,0)
  -> 驳回
驳回(2,0)
  -> 修改重提
待审(0,0)

任意非回收状态
  -> 回收
回收站(*,4)
  -> 恢复
已下架(1,2) 或 未上架(1,0)（按业务规则）
```

---

## 3. 统一可见性规则

## 3.1 前台可见（Public Visible）

- 必须满足：
  - `auditStatus = 1`
  - `publishStatus = 1`
  - `isDeleted = 0`

## 3.2 管理端待审核队列

- 必须满足：
  - `auditStatus = 0`
  - `publishStatus in (0,2,3)`（通常以 `0` 为主）
  - `isDeleted = 0`

## 3.3 已通过未上架

- 必须满足：
  - `auditStatus = 1`
  - `publishStatus = 0`

---

## 4. 动作与状态变更矩阵

| 动作 | 变更前条件 | 变更后 |
|---|---|---|
| 提交审核 | `auditStatus=-1 or 2` | `auditStatus=0` |
| 审核通过 | `auditStatus=0` | `auditStatus=1`，`publishStatus` 不自动变更 |
| 审核驳回 | `auditStatus=0` | `auditStatus=2`，`publishStatus=0` |
| 发布 | `auditStatus=1 and publishStatus in (0,2)` | `publishStatus=1` |
| 下架 | `publishStatus=1` | `publishStatus=2` |
| 回收 | `publishStatus!=4` | `publishStatus=4` |
| 恢复 | `publishStatus=4` | `publishStatus=2`（默认） |

---

## 5. 旧状态 `legacyStatus` 映射规范（兼容期）

| legacyStatus | auditStatus | publishStatus | 说明 |
|---|---|---|---|
| `-1` | `-1` | `0` | 草稿 |
| `0` | `0` | `0` | 待审 |
| `1` | `1` | `1` | 已发布（旧语义） |
| `2` | `2` | `0` | 驳回 |
| `3` | `1` | `2` | 下架 |
| `4` | `1` | `4` | 回收 |

> 说明：`legacyStatus` 仅用于老接口兼容显示，不再作为业务判定主依据。

---

## 6. 技术约束

- 后端判定权限、可操作状态统一通过状态机服务，不允许控制器/Mapper 各自写分支。
- 前端按钮显隐（上架/下架/恢复/回收）必须直接消费统一状态机规则。
- 批量动作执行前必须做“可执行子集过滤”，并返回跳过原因。

