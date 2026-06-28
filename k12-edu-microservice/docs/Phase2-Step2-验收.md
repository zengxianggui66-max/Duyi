# Phase 2 Step 2 验收（后端 · 阶段 0–1）

## 阶段 0

- [x] `Phase2-Step2-Baseline.md` — 问题清单 + API 快照
- [x] Step 1 基线 21/21
- [x] Taxonomy A1–A6 有数据

## 执行

```bash
# 1. 数据库（若 draft/save 报 audit_status 列不存在）
mysql -u root -p xinketang --default-character-set=utf8mb4 < sql/80_admin_resource_status_split.sql

# 2. 编译
mvnw.cmd -pl k12-common,k12-resource -am -DskipTests compile
# 3. 重启 k12-resource(8082)

# 4. 验收
node scripts/phase2-step2-upload-taxonomy-acceptance.mjs
```

## 用例

| ID | 说明 | 预期 |
|----|------|------|
| T1 | `GET /api/taxonomy/grades?stage=primary` | 6 条 |
| T2 | junior grades | 3 条 |
| T3 | senior grades | 3 条 |
| T4 | upload-filter-options 小学 | 不含七/八/九年级册别 |
| T5 | draft 小学 + 七年级上册 | code 400 |
| T6 | draft primary + 七年级上册 | code 400 |
| T7 | subjects primary 含 chinese | 200 |
| T8 | grades 免登录 | 200 |

## 手工（禁用学科）

1. 管理端 `/admin/taxonomy` 禁用小学某学科
2. `GET /api/taxonomy/subjects?stage=primary` 不含该项
3. `POST /api/primary-chinese/draft/save` 带该学科 → 400

## 相关文档

- `k12-edu-platform/docs/Phase2-Step2-Baseline.md`
- `k12-edu-platform/docs/Phase2-Step2-Catalog-Upload.md`
