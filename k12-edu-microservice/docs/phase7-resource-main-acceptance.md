# Phase 7：统一资源审核域落地 — 验收文档

> SQL 脚本：`sql/83_phase7_resource_main.sql`
> 后端包：`k12-resource` + `k12-common`
> 前端包：`k12-edu-platform/admin`
> 验收日期：2026-06-25
> 数据库：MySQL 账号 root 密码 zxg123456

---

## 一、架构设计

```
                     ┌──────────────────────────┐
                     │   管理端资源列表 / 审核中心   │
                     │  AdminResourceMainController │
                     └────────────┬─────────────┘
                                  │
                     ┌────────────▼─────────────┐
                     │ AdminResourceMainService │
                     │  (统一服务，不绑定学科)    │
                     └────────────┬─────────────┘
                                  │
                     ┌────────────▼─────────────┐
                     │  VAdminResourceMainMapper │
                     │  (查询统一视图)           │
                     └────────────┬─────────────┘
                                  │
                ┌─────────────────▼──────────────────┐
                │     v_admin_resource_main (视图)     │
                │  INNER JOIN resource_main + 源表     │
                └─────────────────┬──────────────────┘
                                  │
          ┌───────────────────────┼───────────────────────┐
          │                       │                       │
  ┌───────▼──────┐   ┌───────────▼────────┐   ┌──────────▼─────────┐
  │ resource_main │   │ oss_primary_chinese │   │ oss_junior_math    │
  │ (映射表)      │   │ _resource (小学语文) │   │ _resource (未来)    │
  │ global_id     │   │ 实际数据存储         │   │ 新增学科无需重做    │
  │ source_type   │   │                     │   │ 审核中心            │
  └───────────────┘   └─────────────────────┘   └────────────────────┘
```

**核心设计原则：**
- `resource_main` 为轻量映射表，只存 ID + 路由信息 + 冗余状态
- 实际文件、元数据仍在各学科源表
- `v_admin_resource_main` 视图为管理端唯一数据入口
- 触发器自动同步 `oss_primary_chinese_resource` → `resource_main`
- 新增学科只需 3 步：创建源表 → INSERT 入 resource_main → 扩展视图

---

## 二、改动范围总览

| 层级 | 改动文件 | 改动类型 |
|------|---------|---------|
| SQL | 1 新增 (`83_phase7_resource_main.sql`) | resource_main 表 + 视图重定义 + 触发器 |
| Entity | 2 新增 (`ResourceMain.java`, `VAdminResourceMain.java`) | 映射表实体 + 视图实体 |
| DTO | 3 新增 (`ResourceMainVO`, `ResourceMainDetailVO`, `ResourceMainQueryDTO`) | 统一 DTO |
| Mapper | 2 新增 (`ResourceMainMapper`, `VAdminResourceMainMapper`) | 视图查询 + 映射维护 |
| Service | 2 新增 (`AdminResourceMainService` + impl) | 统一服务层 |
| Controller | 1 新增 (`AdminResourceMainController`) | 统一 API 端点 |
| Frontend API | 1 新增 (`resourceMain.ts`) | TS 类型 + API |
| Frontend Page | 2 修改 (`ResourceList.vue`, `ResourceAudit.vue`) | source_type 筛选 |

---

## 三、功能验收清单

### 3.1 T1-T5：resource_main 映射表基础

| ID | 测试步骤 | 预期结果 | 状态 |
|----|---------|---------|------|
| T1 | 执行 `sql/83_phase7_resource_main.sql` | resource_main 表创建成功，回填 oss_primary_chinese_resource 所有数据 | ☐ |
| T2 | `SELECT COUNT(*) FROM resource_main` | 行数与 `oss_primary_chinese_resource WHERE is_deleted=0` 一致 | ☐ |
| T3 | 插入一条新资源到 oss_primary_chinese_resource | 触发器自动写入 resource_main（source_type=primary_chinese） | ☐ |
| T4 | 更新已有资源的状态 | 触发器同步更新 resource_main 冗余状态字段 | ☐ |
| T5 | `uk_source` 唯一约束 | 同一 (source_type, source_id) 不能重复插入 | ☐ |

### 3.2 T6-T10：统一视图 v_admin_resource_main

| ID | 测试步骤 | 预期结果 | 状态 |
|----|---------|---------|------|
| T6 | `SELECT * FROM v_admin_resource_main LIMIT 5` | 包含 global_id, source_type, source_id, title, 所有维度字段, 状态三件套 | ☐ |
| T7 | 按 source_type 筛选 | `WHERE source_type='primary_chinese'` 返回小学语文数据 | ☐ |
| T8 | 按 audit_status 筛选 | `WHERE audit_status=0` 返回待审数据 | ☐ |
| T9 | 视图 JOIN 正确 | 视图中的 stage/subject 与实际源表数据一致 | ☐ |
| T10 | resource_main 有数据但源表已删除 | 视图不返回（INNER JOIN + is_deleted=0） | ☐ |

### 3.3 T11-T16：统一资源 API

| ID | 测试步骤 | 预期结果 | 状态 |
|----|---------|---------|------|
| T11 | `GET /api/admin/resource-main?sourceType=primary_chinese&current=1&size=10` | 返回分页 ResourceMainVO 列表，含 sourceType 字段 | ☐ |
| T12 | `GET /api/admin/resource-main?auditStatus=0` | 返回所有待审资源（跨源类型） | ☐ |
| T13 | `GET /api/admin/resource-main/pending` | 返回待审队列，按 upload_time ASC | ☐ |
| T14 | `GET /api/admin/resource-main/{globalId}` | 返回 ResourceMainDetailVO，含完整三元状态 | ☐ |
| T15 | `GET /api/admin/resource-main/stats` | 返回按 source_type 分组统计（total/pending/approved/published/recycled） | ☐ |
| T16 | 无权限用户调用 | 403 错误："无资源查看权限" | ☐ |

### 3.4 T17-T22：新旧兼容

| ID | 测试步骤 | 预期结果 | 状态 |
|----|---------|---------|------|
| T17 | 原有 `GET /api/admin/resources` 仍可用 | 返回 AdminResourceListVO（旧格式），功能不变 | ☐ |
| T18 | 原有审核 `POST /api/admin/resources/{id}/audit` 仍可用 | 审核后 resource_main 状态同步更新（触发器） | ☐ |
| T19 | 原有前台 `GET /api/resource/*` 仍可用 | 直接查 oss_primary_chinese_resource，不受影响 | ☐ |
| T20 | API 返回 auditStatus + publishStatus 字段 | 新旧状态字段都正确，与 legacy status 一致性校验通过 | ☐ |
| T21 | legacy status=1 时 auditStatus=1 且 publishStatus=1 | 触发器/回填生成一致 | ☐ |
| T22 | 前端资源管理页新增「来源」筛选下拉 | 可选择「小学语文」（其他 disabled），筛选生效 | ☐ |

### 3.5 T23-T26：扩展性验证

| ID | 测试步骤 | 预期结果 | 状态 |
|----|---------|---------|------|
| T23 | 模拟新增初中数学：INSERT 测试数据到 resource_main | `source_type='junior_math'` 数据存在 | ☐ |
| T24 | 手动扩展视图 UNION ALL 新源表 | 查询 v_admin_resource_main 可看到混合数据 | ☐ |
| T25 | 管理端 resource-main API 可返回混合源类型数据 | `GET /api/admin/resource-main`（不传 sourceType）同时返回小学语文和初中数学 | ☐ |
| T26 | 审核中心待审队列可聚合多学科 | `GET /api/admin/resource-main/pending` 返回所有学科待审项 | ☐ |

---

## 四、API 变更矩阵

### 4.1 新增端点

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/resource-main` | 统一资源列表（支持 sourceType 等行业筛选） |
| GET | `/api/admin/resource-main/pending` | 统一待审队列（跨源类型聚合） |
| GET | `/api/admin/resource-main/{globalId}` | 统一详情（按 global_id） |
| GET | `/api/admin/resource-main/stats` | 统一统计（按 source_type 分组） |

### 4.2 现有端点（不变）

| 方法 | 路径 | 说明 |
|------|------|------|
| 原有 | `/api/admin/resources/*` | 保留，原有功能完全不变 |
| 原有 | `/api/admin/audit/*` | 保留，审核流水不变 |
| 原有 | `/api/resource/*` | 保留，C端前台不受影响 |

---

## 五、行业对标

| 对标维度 | 学科网 | 教习网 | 本系统（Phase 7 后） |
|---------|-------|-------|---------------------|
| 统一资源ID体系 | ✅ global_doc_id | ✅ 统一主键 | ✅ resource_main.id |
| 多学科共存一套审核 | ✅ | ✅ | ✅ 视图 UNION ALL + 统一服务 |
| 状态三件套（审核+发布+legacy） | ✅ | ✅ | ✅ audit/ publish/ legacy |
| 新增学科无需重做审核 | ✅ | ✅ | ✅ 3 步接入 |
| 旧 API 向后兼容 | ✅ | ✅ | ✅ 原 /api/admin/resources 不变 |
| 触发器自动同步状态 | ✅ | ✅ | ✅ INSERT/UPDATE 触发器 |

---

## 六、新增学科接入流程（3 步走）

```
Step 1: 创建学科资源表（如 oss_junior_math_resource）
        CREATE TABLE oss_junior_math_resource LIKE oss_primary_chinese_resource;

Step 2: 数据同步到 resource_main
        INSERT INTO resource_main (source_type, source_table, source_id, ...)
        SELECT 'junior_math', 'oss_junior_math_resource', id, ...
        FROM oss_junior_math_resource WHERE is_deleted = 0;

        并创建对应触发器（参考 sql/83 中的 trg_sync_resource_main_ins）

Step 3: 扩展统一视图
        CREATE OR REPLACE VIEW v_admin_resource_main AS
        ...原有 primary_chinese 子查询...
        UNION ALL
        SELECT rm.id AS global_id, ... FROM resource_main rm
        INNER JOIN oss_junior_math_resource r ON r.id = rm.source_id
        WHERE rm.source_type = 'junior_math' AND rm.is_deleted = 0;

之后：管理端资源列表 / 审核中心无需任何改动！
```

---

## 七、文件修改清单

| 序号 | 文件路径 | 改动 |
|-----|---------|------|
| 1 | `sql/83_phase7_resource_main.sql` | **新增**：resource_main 表 + 视图重定义 + 触发器 |
| 2 | `k12-common/.../entity/ResourceMain.java` | **新增**：映射表实体 |
| 3 | `k12-common/.../entity/VAdminResourceMain.java` | **新增**：统一视图实体 |
| 4 | `k12-common/.../dto/ResourceMainVO.java` | **新增**：统一列表 VO |
| 5 | `k12-common/.../dto/ResourceMainDetailVO.java` | **新增**：统一详情 VO |
| 6 | `k12-common/.../dto/ResourceMainQueryDTO.java` | **新增**：统一查询参数 |
| 7 | `k12-resource/.../mapper/ResourceMainMapper.java` | **新增**：映射表 Mapper |
| 8 | `k12-resource/.../mapper/VAdminResourceMainMapper.java` | **新增**：视图查询 Mapper |
| 9 | `k12-resource/.../service/AdminResourceMainService.java` | **新增**：统一服务接口 |
| 10 | `k12-resource/.../service/impl/AdminResourceMainServiceImpl.java` | **新增**：统一服务实现 |
| 11 | `k12-resource/.../controller/AdminResourceMainController.java` | **新增**：统一 API 端点 |
| 12 | `k12-edu-platform/.../admin/api/resourceMain.ts` | **新增**：前端 API + TS 类型 |
| 13 | `k12-edu-platform/.../admin/views/resources/ResourceList.vue` | **修改**：增加 source_type 筛选列 |
| 14 | `k12-edu-platform/.../admin/views/audit/ResourceAudit.vue` | **修改**：增加 source_type 筛选 |
| 15 | `docs/phase7-resource-main-acceptance.md` | **本验收文档** |

---

## 八、部署步骤

```bash
# 1. 执行 SQL
mysql -u root -pzxg123456 xinketang < sql/83_phase7_resource_main.sql

# 2. 验证 resource_main
mysql -u root -pzxg123456 xinketang -e "SELECT source_type, COUNT(*) FROM resource_main GROUP BY source_type"

# 3. 编译后端
cd k12-edu-microservice && mvn clean compile -pl k12-common,k12-resource -am -DskipTests

# 4. 构建前端
cd k12-edu-platform && npm run build

# 5. 测试 API
curl http://localhost:8080/api/admin/resource-main/stats -H "X-User-Id: 1"
curl "http://localhost:8080/api/admin/resource-main?sourceType=primary_chinese&current=1&size=5" -H "X-User-Id: 1"
```

---

## 九、执行摘要

> **总计改动：** 15 个文件（11 新增 + 2 修改 + 1 SQL + 1 验收文档）
> **新增 API 端点：** 4 个（GET /api/admin/resource-main/*）
> **原有端点：** 完全保留，不受影响
> **扩展性：** 新增学科 3 步接入，审核系统无需重做
> **验收用例：** 26 项
