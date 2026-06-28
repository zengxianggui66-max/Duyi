# Phase 5-F · 首页学科导航矩阵

## 目标

将首页左侧学科栏 + 学科详情浮层（同步备课 / 复习备考 / 升学区块）纳入 **taxonomy 统一数据源**，支持管理端按学科配置：

- 可见栏目（如幼儿无「国学阅读」）
- 同步备课资料类型白名单
- 教材版本（沿用 Phase 5 学科-版本绑定）

## 交付物

| 类型 | 路径 |
|------|------|
| SQL | `sql/68_phase5f_subject_nav.sql` |
| C 端 API | `GET /api/home/subject-nav?stage=&subject=` |
| 后端服务 | `HomeSubjectNavService`, `ModuleNavGroupResolver` |
| 管理端 | `/admin/taxonomy` 学科编辑：绑定栏目 + 同步资料类型 |
| 前端 C 端 | `HomePage.vue`, `useHomeSubjectDetailOptions.ts`, `useStageSubject.ts` |
| 路由守卫 | `SubjectDetailPage` → `ensureSubjectRouteValid` |

## 数据表

### edu_module_subject

| 字段 | 说明 |
|------|------|
| module_id | 栏目 ID |
| subject_id | 学科 ID |
| sort | 排序 |
| status | 0 隐藏 / 1 展示 |

**默认策略**：表内无该学科记录 → 展示该学段下全部栏目（与 Phase 5 一致）

### edu_subject_resource_type

| 字段 | 说明 |
|------|------|
| subject_id | 学科 ID |
| resource_type_id | 叶子类型 ID（group_code=teach） |
| sort / status | 排序与启停 |

**默认策略**：表内无记录 → 使用 teach 分组全部启用叶子类型

## API 契约

### GET /api/home/subject-nav

**Query**

| 参数 | 必填 | 示例 |
|------|------|------|
| stage | 是 | `primary` |
| subject | 是 | `chinese` |

**Response 示例**

```json
{
  "code": 200,
  "data": {
    "subject": { "code": "chinese", "name": "语文", "stageCode": "primary" },
    "syncPrep": {
      "editions": [{ "code": "tongbian2024", "name": "统编版(2024)", "isNew": true }],
      "resourceTypes": [{ "code": "courseware", "name": "课件" }]
    },
    "reviewPrep": {
      "label": "试题试卷",
      "title": "复习备考",
      "modules": [{ "code": "monthly", "name": "月考" }]
    },
    "promotionPrep": {
      "label": "小升初",
      "title": "小升初",
      "modules": [{ "code": "xsc_real", "name": "小升初真题" }]
    }
  }
}
```

栏目分组由后端 `ModuleNavGroupResolver` 根据 `module_category` + 学段规则计算，前端不再维护 `PROMOTION_PATTERNS` 正则。

## 部署步骤

1. 执行 SQL：`sql/68_phase5f_subject_nav.sql`
2. 重启 **k12-resource**（新 Controller/Service）
3. 重启 **k12-gateway**（若缓存路由）
4. 前端无需额外 env（沿用 `VITE_USE_TAXONOMY_API`）

## 验收清单

| # | 场景 | 操作 | 预期 |
|---|------|------|------|
| F1 | subject-nav API | `GET /api/home/subject-nav?stage=primary&subject=chinese` | 200，含 editions / resourceTypes / reviewPrep / promotionPrep |
| F2 | 幼儿无国学 | 查 `preschool` + 任意 subject | reviewPrep 无「国学阅读」 |
| F3 | 小学语文有国学 | primary + chinese | reviewPrep 含「国学阅读」（种子绑定后） |
| F4 | 管理端绑栏目 | taxonomy 学科编辑取消某栏目 | C 端浮层该 tag 消失 |
| F5 | 管理端绑类型 | 学科仅保留「课件」「教案」 | 浮层资料类型仅 2 项 |
| F6 | 首页左栏 | 切换学段 | 学科列表与 `/api/taxonomy/subjects` 一致 |
| F7 | 无效学科 URL | 访问已禁用学科 bookmark | 重定向首页，无白屏 |
| F8 | 改学科名 | 管理端改展示名 | URL code 不变，展示名更新 |

## 与 Phase 7 关系

- **Phase 5-F**：学科导航矩阵（本阶段）
- **Phase 7-A~F**：Banner / 热词 / Panel 等运营位（独立，可并行）

## 权限

沿用 Phase 5：`admin:taxonomy:view` / `admin:taxonomy:edit`
