# Phase 2 Step 1 验收

详细 Smoke 清单见：`k12-edu-platform/docs/Phase2-Step1-Smoke.md`

## 快速执行

```bash
# 1. 测试账号（若 S3/S4 未过）
scripts\run-phase2-step1-sql.cmd
# 或: set MYSQL_PWD=... 后 node scripts/phase2-step1-baseline-acceptance.mjs 会自动 seed

# 2. 编译 resource（stats 权限修复后）
mvnw.cmd -pl k12-resource -am -DskipTests compile
# 重启 resource(8082)

# 3. 验收
node scripts/phase2-step1-baseline-acceptance.mjs
```

## 构建门禁（发版前）

```bash
cd ../k12-edu-platform && npm run build
cd ../k12-edu-microservice && mvnw.cmd -pl k12-common,k12-auth,k12-resource,k12-gateway -am -DskipTests compile
```

或 `PHASE2_CHECK_BUILD=1 node scripts/phase2-step1-baseline-acceptance.mjs`

## 用例概览

| 范围 | 数量 | 脚本 ID |
|------|------|---------|
| 身份/RBAC | S1–S17 | baseline |
| 操作日志 | S18–S20 | baseline |
| Phase 9 回归 | 46 项 | phase9-acceptance-test.mjs（默认串联） |
