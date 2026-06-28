# K12 微服务 — 数据库脚本（统一库 `xinketang`）

> 设计说明：[数据库设计方案.md](../../k12-edu-platform/docs/数据库设计方案.md)  
> 产品与架构讨论汇总：[资源平台架构与多学科品牌设计方案.md](../../k12-edu-platform/docs/资源平台架构与多学科品牌设计方案.md)  
> 方案 A 实施计划（DB/后端/前端分阶段）：[方案A-学科网式落地实施计划.md](../../k12-edu-platform/docs/方案A-学科网式落地实施计划.md)

## 说明

- **唯一业务库名**：`xinketang`（已废弃 `k12_edu`，`00_init_database.sql` 会 `DROP DATABASE k12_edu`）
- 所有微服务 `application.yml` 数据源已指向 `jdbc:mysql://.../xinketang`
- 旧版分散 SQL（`edu_dimension_tables.sql`、`k12_complete_database.sql` 等）已删除，逻辑合并到本目录编号脚本

## 全新安装执行顺序

| 顺序 | 脚本 | 说明 |
|------|------|------|
| 1 | `00_init_database.sql` | 建库 `xinketang`，删除旧库 `k12_edu` |
| 2 | `01_drop_all_tables.sql` | 清空库内全部表（重建必跑） |
| 3 | `02_dimension_core.sql` | 学段/学科/版本/年级/学期/册别 |
| 4 | `03_dimension_taxonomy.sql` | 资源类型树、栏目、场景、地区、频道 |
| 5 | `04_catalog.sql` | 单元、课文、知识点 |
| 6 | `05_resource.sql` | 规范资源主表、多文件、维度关联、视图 |
| 7 | `06_auth.sql` | 用户、短信验证码 |
| 8 | `07_interaction.sql` | 收藏、分享、搜索、评分 |
| 9 | `08_legacy_business.sql` | 宽表 `oss_primary_chinese_resource`、dict、resource 等 |
| 10 | `09_platform_modules.sql` | 资讯、备课、组卷、会员订单 |
| 11 | `99_seed_all.sql` | 维度/栏目/类型种子 + 班会 dict |
| 12 | `10_seed_y1s2_chinese_demo.sql` | **可选** 一年级下册语文演示资源（学科详情页） |

### PowerShell 一键示例

```powershell
$mysql = "mysql -u root -p你的密码"
& cmd /c "$mysql < sql/00_init_database.sql"
foreach ($f in @(
  "01_drop_all_tables","02_dimension_core","03_dimension_taxonomy",
  "04_catalog","05_resource","06_auth","07_interaction",
  "08_legacy_business","09_platform_modules","99_seed_all"
)) {
  & cmd /c "$mysql xinketang < sql/$f.sql"
}
```

## 首页专区增量脚本

| 脚本 | 说明 |
|------|------|
| `23_home_panel_tab_config.sql` | Tab 查询配置表 |
| `24_home_panel_p1.sql` | 演示资源 + 升学 Tab 分学段配置 |
| `25_home_panel_featured.sql` | 运营置顶位 |
| `26_home_panel_demo_patch.sql` | **已跑过 24/25 时**单独补丁（六年级期中、高中历史、升学专题） |
| `27_brand_baseline.sql` | **M1** 品牌表 + 宽表 `brand_code`/`sub_type` + 种子（方案 A） |
| `28_catalog_scheme.sql` | **M2** 目录方案表 + `edu_catalog_node` |
| `29_seed_catalog_qicai_y1s1.sql` | **M2** 七彩一年级上册目录种子 |
| `30_seed_catalog_zhuangyuan.sql` | **M2** 状元四层分类树种子 |

**M2 执行顺序**（在 27 之后）：

```text
28 → 29 → 30
```

| `31_resource_placement.sql` | **M3** 资源挂载表 `edu_resource_placement` |
| `31_seed_placement_backfill.sql` | **M3 可选** 按课文名回填 placement + `catalog_path` |

**M3 执行顺序**（在 M2 之后）：

```text
31 → 31_seed_placement_backfill（可选，有演示资源时建议跑）
```

| `32_m4_resource_write_enhance.sql` | **M4** `file_role` 说明 + 视图 `v_resource_browse` |
| `33_patch_brand_logo_url.sql` | **补丁** `edu_resource_brand.logo_url`（`/api/catalog/schemes` 报错时必跑） |
| `34_seed_catalog_primary_2023_full.sql` | **小学 1-6 全科目录**（统编语文 + 人教数学 + PEP 英语，12 册 × 3 科） |

**小学全科目录执行**（在 28、27 之后；会覆盖 `29` 中同册别旧节点）：

```text
34_seed_catalog_primary_2023_full.sql
```

重新生成 SQL：`python sql/tools/generate_primary_catalog.py`（语文目录见 `sql/tools/primary_chinese_curriculum.py`）

| `35_seed_qicai_primary_baseline.sql` | **七彩小学** 扩展栏目、`edu_catalog_unit_alias`（12 册语文）、统编树包外节点（期中/期末复习、国学阅读、教师工作包） |
| `tools/check_home_panel_real_data.sql` | **数据体检** 首页三大专区真实可跳转资源校验（缺维度/脏数据/候选抽样） |
| `tools/fix_home_panel_real_data.sql` | **修复模板** 根据体检结果修复 `edu_resource` 状态异常（含前后快照） |

**七彩 baseline 执行**（在 `34` 之后）：

```text
35_seed_qicai_primary_baseline.sql
```

重新生成：`python sql/tools/generate_qicai_primary_baseline.py`

**M4 说明**：上传/保存走 `edu_resource` 主表 + 同 ID 双写 `oss_primary_chinese_resource`；新 API `POST/PUT /api/resources`。

**常见报错**：`Unknown column 'logo_url'` → 执行 `33_patch_brand_logo_url.sql` 后重启 k12-resource。

## 模块内增量脚本

`k12-auth`、`k12-resource` 下 `auth_*.sql`、`p0~p2_migration.sql` 仅用于**已上线库的增量**；全新安装以本目录脚本为准，无需再跑迁移。

## 注意

- `01_drop_all_tables.sql` 与 `02`~`05` 中的 `DROP TABLE` 会**删除全部数据**，生产环境请先备份。
- 测试数据（原 `oss_primary_chinese_resource_test_data.sql`）未默认导入；学科详情页演示请执行 `10_seed_y1s2_chinese_demo.sql`。
