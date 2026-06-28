# Phase 3B：资源主链路统一 - 数据库梳理与迁移计划

> 阶段：Phase 3 / 子阶段：3B（数据库表梳理）  
> 基于仓库与本地数据库实查时间：2026-06-27

---

## 1. 目标与产出

## 1.1 目标

- 把资源相关表按“主链路 / 兼容层 / 待废弃”分层。
- 明确 `resource_main` 作为统一 ID 与来源索引表的演进方案。
- 形成可执行的回填脚本与统一管理视图扩展方案。

## 1.2 本阶段产出

- 表分层清单（当前态）。
- 差距证据（基于数据库实际统计）。
- 迁移策略与风险控制。
- SQL 草案：`sql/86_phase3b_resource_main_chain.sql`。

---

## 2. 现状证据（数据库实查）

## 2.1 `resource_main` 覆盖情况

- 当前仅有：`source_type=primary_chinese`。
- 行数：
  - `resource_main = 330`
  - `oss_primary_chinese_resource = 330`
  - `topic_resource = 20`
  - `culture_resource = 19`
  - `competition_resource = 12`
  - `edu_resource = 23`

## 2.2 未映射缺口

- `topic_unmapped = 20`
- `culture_unmapped = 19`
- `competition_unmapped = 12`
- `edu_unmapped = 23`

## 2.3 结构缺口

- `resource_main` 尚无：
  - `content_domain`
  - `canonical_resource_id`
  - `legacy_source_table`
  - `legacy_source_id`
- `edu_resource_placement` 尚无 `placement_type`（当前仍以 catalog 挂载为主）。

---

## 3. 表分层清单（To-Be）

## 3.1 主链路表（长期保留）

- `resource_main`：统一资源 ID 与来源索引。
- `edu_resource`：统一资源主事实表。
- `edu_resource_file`：文件与预览域。
- `edu_resource_dimension`：结构化维度域。
- `edu_resource_placement`：挂载关系域（后续补 `placement_type`）。

## 3.2 兼容层表（迁移期保留）

- `oss_primary_chinese_resource`
- `topic_resource`
- `culture_resource`
- `competition_resource`
- `article`（资讯主内容保留在资讯域，附件进入主链）

## 3.3 待废弃对象（Phase 3G 后评估）

- 直接绑定旧源表的旧查询视图和 Mapper。
- `/api/resources/browse/*` 旧查询路径。
- 前台专区直连旧表接口路径。

---

## 4. 关系治理方案

## 4.1 统一 ID 关系

- 对外统一：`canonicalResourceId = resource_main.id`。
- 对内路由：`resource_main.source_type + source_id`。

## 4.2 主链路读写关系

- 管理端与前台读逻辑逐步转向统一视图（或统一物化查询层）。
- 迁移期间允许源表继续写入，但必须同步 `resource_main`。

## 4.3 article 处理原则

- 资讯正文保留在 `article` 服务域，不强制入 `edu_resource`。
- 若资讯关联资料包/附件/PDF，附件进入 `edu_resource_file` 并映射到 `resource_main`。

---

## 5. 执行步骤（先做什么，再做什么，最后做什么）

## 5.1 第一步：结构补齐（先做）

- 为 `resource_main` 增加统一治理字段（幂等）。
- 准备 `source_type` 标准值与索引策略。

## 5.2 第二步：数据回填（再做）

- 回填 `topic/culture/competition/edu_resource -> resource_main`。
- 保持 `ON DUPLICATE KEY UPDATE`，支持重复执行。

## 5.3 第三步：统一视图扩展（最后做）

- 扩展 `v_admin_resource_main`：从单一 `primary_chinese` 到多来源 `UNION ALL`。
- 保证列顺序稳定，兼容现有 Java Entity 映射。

---

## 6. 风险与控制

- **风险1**：不同来源状态语义不一致（topic/culture/competition 仅有 `status`）。
  - **控制**：回填时做状态归一映射；3D 再引入统一状态策略服务。

- **风险2**：视图 union 后性能退化。
  - **控制**：优先在 `resource_main` 过滤 `source_type/is_deleted`，并加复合索引。

- **风险3**：管理端读写路径仍分裂。
  - **控制**：3D/3E 同步推进统一资源服务，3B 先把数据面打通。

---

## 7. 验收标准（3B）

- `resource_main` 至少覆盖 `primary_chinese + topic_resource + culture_resource + competition_resource + edu_resource`。
- `v_admin_resource_main` 可按 `source_type` 查询到多来源数据。
- 未映射数清零（或仅剩明确排除的历史脏数据并有清单）。
- 回填脚本可重复执行且不产生重复映射。

---

## 8. 对应 SQL 草案

- 文件：`sql/86_phase3b_resource_main_chain.sql`
- 作用：
  - 结构补齐（幂等）
  - 多来源回填（幂等）
  - 统一视图扩展（多来源）
  - 提供核验 SQL

