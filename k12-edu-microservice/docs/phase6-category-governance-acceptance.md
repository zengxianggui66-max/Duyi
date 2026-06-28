# Phase 6：管理端分类目录治理 — 验收文档

> 文件编码：SQL `82_phase6_category_governance.sql`
> 后端包：`k12-resource` + `k12-common`
> 前端包：`k12-edu-platform/admin`
> 验收日期：2026-06-25

---

## 一、改动范围总览

| 层级 | 改动文件数 | 改动类型 |
|------|-----------|---------|
| SQL | 1 新增 | DDL + 数据回填 + 视图 |
| Entity | 3 修改 | 增加字段 |
| DTO | 3 修改 | 增加字段 |
| Mapper | 1 修改 | 新增 4 个统计方法 |
| Service | 3 修改 | 接口 + 实现业务逻辑增强 |
| Controller | 2 修改 | 接口扩展 + 状态开关 |
| Frontend API | 2 修改 | 类型 + API 增强 |
| Frontend Page | 2 修改 | 表单 + 交互增强 |
| Validator | 1 修改 | 上传禁用校验 |
| C端 API | 1 修改 | volume 状态过滤 |

---

## 二、功能验收清单（32 项）

### 2.1 T1-T6：教材版本绑定学段·学科

| ID | 测试步骤 | 预期结果 | 状态 |
|----|---------|---------|------|
| T1 | 管理端新建版本，选择学段=小学、学科=语文 | 保存成功，列表显示学段「小学」、学科「语文」 | ☐ |
| T2 | 新建版本，不选学段学科（留空=通用） | 保存成功，列表显示「通用」 | ☐ |
| T3 | 编辑版本，将学段从小学改为初中 | 保存成功，该版本在小学学段中不再出现 | ☐ |
| T4 | 小学学段下搜版本（C端: `GET /api/taxonomy/editions?stage=primary&subject=chinese`） | 只返回绑定了小学或通用的版本 | ☐ |
| T5 | 高中专用版本不应出现在小学筛选结果中 | C端不返回 | ☐ |
| T6 | 修改版本编码为已存在的编码 | 400 错误："版本编码已存在" | ☐ |

### 2.2 T7-T12：册别状态·多维绑定

| ID | 测试步骤 | 预期结果 | 状态 |
|----|---------|---------|------|
| T7 | 册别列表新增「状态」列，管理端可启用/禁用 | 禁用后上传页下拉不再展示该册别 | ☐ |
| T8 | 新建册别，绑定学段=小学、学科=语文、版本=人教版 | 保存成功，列表显示完整的绑定路径 | ☐ |
| T9 | 册别绑定学段后，其他学段上传页不可选 | 初中上传页不展示小学绑定的册别 | ☐ |
| T10 | 修改册别绑定后，上传页联动刷新 | 上传页选择对应维度后册别下拉同步更新 | ☐ |
| T11 | 禁用某册别（管理端 toggle） | 上传页 `GET /api/taxonomy/volumes?stage=primary` 不返回 | ☐ |
| T12 | 重新启用册别 | 上传页恢复展示 | ☐ |

### 2.3 T13-T18：目录树绑定

| ID | 测试步骤 | 预期结果 | 状态 |
|----|---------|---------|------|
| T13 | 管理端查看目录方案列表 | 显示 scheme 的学段/学科/版本/册别绑定信息 | ☐ |
| T14 | 选择绑定了学段的目录方案，上传页目录树联动 | 仅展示该方案下的节点 | ☐ |
| T15 | 目录方案绑定信息可正常读取 | `GET /api/admin/catalog/schemes` 返回 stageName 等字段 | ☐ |
| T16 | 管理端目录树查看时，按方案维度过滤 | 筛选合法 | ☐ |
| T17 | 调整目录树的单元顺序 | 上传页和前台按新顺序展示 | ☐ |
| T18 | 目录树新增单元/课文 | 前端目录选择器即时更新 | ☐ |

### 2.4 T19-T24：删除前检查资源引用

| ID | 测试步骤 | 预期结果 | 状态 |
|----|---------|---------|------|
| T19 | 删除已被资源引用的学段 | 400 错误："该学段已被 X 个资源引用，无法删除，建议改为禁用" | ☐ |
| T20 | 删除未被任何资源引用的学段（且无子学科） | 删除成功 | ☐ |
| T21 | 删除已被资源引用的年级 | 400 错误，提示引用数量 | ☐ |
| T22 | 删除已被资源引用的版本 | 400 错误，提示引用数量 | ☐ |
| T23 | 删除已被资源挂载的目录节点 | 400 错误："该节点已被 X 个资源挂载，无法删除，建议改为禁用" | ☐ |
| T24 | 删除未被引用的目录节点（叶子节点） | 删除成功 | ☐ |

### 2.5 T25-T30：禁用场景闭环

| ID | 测试步骤 | 预期结果 | 状态 |
|----|---------|---------|------|
| T25 | 管理端禁用某年级（如"一年级"） | 上传页不可选择"一年级"；前台筛选不展示 | ☐ |
| T26 | 上传页提交已被禁用分类的资源 | 400 错误："学段/学科/年级已禁用" | ☐ |
| T27 | 禁用前已提交的资源详情页不受影响 | 已发布资源详情页正常展示原有分类信息 | ☐ |
| T28 | 管理端禁用某学科 | 上传页、前台筛选均不可选该学科 | ☐ |
| T29 | 重新启用后，立即恢复可选 | 上传页刷新后可选择 | ☐ |
| T30 | 管理端删除被引用的分类，弹出"改为禁用"按钮 | 用户点击后自动执行禁用操作 | ☐ |

### 2.6 T31-T32：数据一致性

| ID | 测试步骤 | 预期结果 | 状态 |
|----|---------|---------|------|
| T31 | 管理端修改分类名称后 | 前台筛选、上传页、管理端列表同步更新 | ☐ |
| T32 | 管理端修改绑定关系后 | 所有访问该绑定关系的接口立即生效 | ☐ |

---

## 三、API 变更矩阵

### 3.1 管理端新增/变更 API

| 方法 | 路径 | 变更说明 |
|------|------|---------|
| GET | `/api/admin/taxonomy/editions` | 新增 `stageId`、`subjectId` 筛选参数 |
| GET | `/api/admin/taxonomy/volumes` | 新增 `stageId`、`subjectId`、`editionId`、`includeDisabled` 参数 |
| PUT | `/api/admin/taxonomy/volumes/{id}/status` | **新增**：册别状态开关 |
| GET | `/api/admin/taxonomy/*` | 删除接口增加资源引用检查 (400 错误码) |
| GET | `/api/admin/catalog/schemes` | 返回新增 `stageName`、`subjectName`、`editionName`、`volumeName` 字段 |
| DELETE | `/api/admin/catalog/nodes/{id}` | 增加资源挂载检查 (400 错误码) |

### 3.2 C端 API 变更

| 方法 | 路径 | 变更说明 |
|------|------|---------|
| GET | `/api/taxonomy/volumes` | 增加 volume.status 过滤（includeDisabled=false 时过滤） |

---

## 四、验收环境准备

### 4.1 数据库迁移

```bash
# 1. 执行 Phase 6 DDL + 数据迁移脚本
mysql -u root -p xinketang < sql/82_phase6_category_governance.sql

# 2. 验证字段新增
DESC edu_edition;   # 确认 stage_id、subject_id 存在
DESC edu_volume;    # 确认 status、stage_id、subject_id、edition_id 存在
DESC edu_catalog_scheme;  # 确认 stage_id、subject_id、edition_id、volume_id 存在

# 3. 验证数据回填
SELECT id, code, name, stage_id, subject_id FROM edu_edition WHERE stage_id IS NOT NULL;

# 4. 验证视图
SELECT * FROM v_taxonomy_resource_ref ORDER BY dim_type, ref_count DESC;
```

### 4.2 后端编译

```bash
cd k12-edu-microservice
mvn clean compile -pl k12-common,k12-resource -am -DskipTests
```

### 4.3 前端构建

```bash
cd k12-edu-platform
npm install
npm run build
```

### 4.4 测试数据准备

```sql
-- 创建测试版本（绑定小学+语文）
INSERT INTO edu_edition (code, name, short_name, stage_id, subject_id, sort, status)
VALUES ('test_renjiao_small', '人教版测试版', '人教测试', 1, 1, 100, 1);

-- 创建测试册别
INSERT INTO edu_volume (code, name, sort, status, stage_id, subject_id, edition_id)
VALUES ('test_up', '测试上册', 10, 1, 1, 1, (SELECT id FROM edu_edition WHERE code = 'test_renjiao_small'));

-- 禁用某年级用于测试
UPDATE edu_grade SET status = 0 WHERE code = 'grade6' LIMIT 1;

-- 验证引用计数（需有实际资源数据）
SELECT 'stage' AS dim_type, r.stage AS dim_code, COUNT(*) AS ref_count
FROM oss_primary_chinese_resource r WHERE r.is_deleted = 0
GROUP BY r.stage;
```

---

## 五、行业对标结论

| 对标维度 | 学科网 | 教习网 | 本系统（Phase 6 验收后） |
|---------|-------|-------|------------------------|
| 版本绑定学段+学科 | ✅ | ✅ | ✅ |
| 年级按学段隔离 | ✅ | ✅ | ✅ |
| 册别按版本+学科绑定 | ✅ | ✅ | ✅ |
| 禁用分类不影响已发布详情 | ✅ | ✅ | ✅ |
| 删除分类需检查资源引用 | ✅ | ✅ | ✅ |
| 目录树按多维维度隔离 | ✅ | ✅ | ✅ |
| 前后台统一数据源 | ✅ | ✅ | ✅ |
| 删除被拒时引导禁用 | ✅ | ✅ | ✅ |

---

## 六、文件修改清单

| 序号 | 文件路径 | 改动 |
|-----|---------|------|
| 1 | `sql/82_phase6_category_governance.sql` | **新增**：DDL+迁移+视图 |
| 2 | `k12-common/.../entity/EduEdition.java` | 新增 `stageId`、`subjectId` |
| 3 | `k12-common/.../entity/EduVolume.java` | 新增 `status`、`stageId`、`subjectId`、`editionId` |
| 4 | `k12-common/.../entity/EduCatalogScheme.java` | 新增 `stageId`、`subjectId`、`editionId`、`volumeId` |
| 5 | `k12-common/.../dto/AdminTaxonomyEditionWriteDTO.java` | 新增 `stageId`、`subjectId` |
| 6 | `k12-common/.../dto/AdminTaxonomyVolumeWriteDTO.java` | 新增 `status`、`stageId`、`subjectId`、`editionId` |
| 7 | `k12-common/.../dto/CatalogSchemeVO.java` | 新增维度绑定展示字段 |
| 8 | `k12-resource/.../mapper/PrimaryChineseResourceMapper.java` | 新增 `countByEdition`、`countByGrade`、`countByStage`、`countBySubject` |
| 9 | `k12-resource/.../service/AdminTaxonomyService.java` | 接口签名扩展 |
| 10 | `k12-resource/.../service/impl/AdminTaxonomyServiceImpl.java` | 业务逻辑增强（删除保护+绑定+状态） |
| 11 | `k12-resource/.../service/impl/AdminCatalogServiceImpl.java` | 方案绑定+删除保护 |
| 12 | `k12-resource/.../controller/AdminTaxonomyController.java` | 接口扩展+册别状态开关 |
| 13 | `k12-resource/.../service/UploadPlacementValidator.java` | 学段状态校验 |
| 14 | `k12-resource/.../service/TaxonomyReadService.java` | volumes 状态过滤 |
| 15 | `k12-edu-platform/.../admin/api/taxonomy.ts` | 类型+API 增强 |
| 16 | `k12-edu-platform/.../admin/api/catalog.ts` | SchemeItem 类型增强 |
| 17 | `k12-edu-platform/.../admin/views/taxonomy/Taxonomy.vue` | 表单级联选择+删除友好提示 |
| 18 | `k12-edu-platform/.../admin/views/catalog/CatalogTree.vue` | 方案绑定展示+删除提示 |
| 19 | `docs/phase6-category-governance-acceptance.md` | **本验收文档** |

---

## 七、执行摘要

> **总计改动：** 19 个文件  
> **新增 SQL 脚本：** 1 个  
> **新增 API 端点：** 1 个（PUT /api/admin/taxonomy/volumes/{id}/status）  
> **变更 API 端点：** 6 个（参数增加或返回增加）  
> **删除保护维度：** 学段、学科、年级、版本、目录节点（共 5 个维度）  
> **验收用例：** 32 项
