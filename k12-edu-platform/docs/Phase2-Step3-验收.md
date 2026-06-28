# Phase 2 第三步 — 上传资源生命周期验收

对照开发计划 §569–589。

## 前置

1. Gateway `9001` + k12-resource `8082` 已启动
2. 已执行 `sql/82`（audit/publish 列）、`sql/83`（preview/safety 列）
   - Windows: `k12-edu-microservice\scripts\run-sql83-preview-safety.cmd`

## 自动化

```bash
cd k12-edu-microservice
node scripts/phase2-step3-upload-lifecycle-acceptance.mjs
```

| 编号 | 验收项 |
|------|--------|
| T25 | saveDraft 后 status=-1 |
| T26 | submitDraft 后 status=0，audit_status=0 |
| T27 | 公开 page 不含待审资源 id |
| T28 | admin pending 含待审资源 |
| T29 | POST /save 返回 400 |
| T30 | 无 uploaderId 的 page 默认不含 status=0 |

## 手工 Smoke（F-U1）

1. 登录教师账号 → 上传页填写元数据 + 文件 → **保存草稿**
2. 前台学科列表 / 搜索 **搜不到** 该标题
3. **我的资源 - 草稿箱** 可见 → 编辑 → **提交审核**
4. 跳转 **我的资源 - 待审核**；前台仍不可见
5. 管理端 **待审队列** 可见该资源 → 通过/驳回（第五步细验）

## 前端构建

```bash
cd k12-edu-platform && npm run build
```
