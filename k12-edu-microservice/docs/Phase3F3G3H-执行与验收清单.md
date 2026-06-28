# Phase 3F / 3G / 3H 执行与验收清单

## 一、目标

- 3F 前台迁移：前台列表请求统一收口到 `resourceGateway`，按灰度开关逐页切流，支持请求取消与最终态渲染。
- 3G 旧接口下线：建立旧读接口调用量统计，提供下线门禁依据与管理端可视查询。
- 3H 质量收口：乱码检查纳入自动验收，同时验证全学段（小学/初中/高中）目录维度接口可用。

## 二、先/中/后执行顺序

### 先做（迁移护栏）

1. 保持 P0 灰度开关可用：`resourceUnifiedReadEnabled` + 各频道开关。
2. 迁移期间每次切页前后执行：
   - `phase3p0-compare-resource-apis.mjs`
   - `phase3d-3e-acceptance-test.mjs`

### 中间做（3F 迁移实施）

1. 新增 `resourceGateway`：
   - 统一 `/api/resources/page` 调用入口；
   - 根据 `/api/public/feature-flags` 自动切换 unified / legacy；
   - 对 in-flight 请求执行 `AbortController` 取消。
2. 前台低风险页先切：
   - 竞赛专区列表；
   - 专题专区列表（存在 region 精细筛选时自动回退 legacy）；
   - 传统文化列表（存在 region/duration 精细筛选时自动回退 legacy）。
3. 页面不再直接多点拼接读 API，统一通过 gateway 决策。

### 最后做（3G 下线 + 3H 收口）

1. 新增旧接口调用量统计表 `legacy_api_usage_stat`。
2. 在资源服务层对旧读接口埋点统计（日维度 upsert）。
3. 提供管理端查询接口：
   - `GET /api/admin/resource-main/legacy-api-usage?days=7`
4. 乱码与全学段收口：
   - 自动扫描关键目录是否存在 `U+FFFD`；
   - 验证 `taxonomy/stages` 包含 `primary/junior/senior`；
   - 验证三个学段 `taxonomy/volumes` 接口可用。

## 三、验收脚本

- 3F：`node scripts/phase3f-acceptance-test.mjs`
- 3G：`node scripts/phase3g-acceptance-test.mjs`
- 3G 门禁：`node scripts/phase3g-no-new-legacy-calls-gate.mjs`（CI：`.github/workflows/phase3g-gate.yml`）
- 3H：`node scripts/phase3h-acceptance-test.mjs`
- 3I-A 灰度 Runbook：`docs/Phase3I-A-统一读灰度切流Runbook.md`

> 推荐顺序：
> 1) `phase3f-acceptance-test`  
> 2) `phase3g-acceptance-test`  
> 3) `phase3h-acceptance-test`

## 四、发布门禁建议

1. 3F 门禁：
   - 3F 脚本通过；
   - P0 release gate 通过（未出现空结果异常）。
2. 3G 门禁：
   - `legacy-api-usage` 接口可读；
   - 连续一个版本周期核心旧读接口调用量下降并趋近 0。
3. 3H 门禁：
   - 3H 脚本通过；
   - 关键目录无码点新增；
   - 三学段目录能力可回归。
