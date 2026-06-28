# Phase 8-B · 系统配置存储 + PUT 审计验收

范围：`sys_config` 表、GET/PUT `/api/admin/system/config`、secret 脱敏、写操作审计。

---

## 一、部署

```bash
mysql -u root -p xinketang --default-character-set=utf8mb4 < sql/73_phase8b_system_config.sql

mvn install -pl k12-common,k12-auth,k12-gateway -am -DskipTests
# 重启 auth(8081)、gateway(9001)
```

---

## 二、API

| 方法 | 路径 | 权限 |
|------|------|------|
| GET | `/api/admin/system/config?group=upload` | `admin:system:config_view` |
| PUT | `/api/admin/system/config?group=upload` | `admin:system:config_edit` |

### 支持分组

`upload` / `preview` / `storage` / `sms` / `oauth` / `feature`

### PUT Body 示例（upload）

```json
{ "maxSizeMb": 512, "allowedFormats": ["pdf","docx"] }
```

secret 字段传 `******` 表示保持不变。

---

## 三、运行时生效策略

| 类型 | 配置项 | 说明 |
|------|--------|------|
| 需重启 | `maxSizeMb`、MinIO endpoint/密钥 | `fields[].requiresRestart=true`，页面提示「下次重启生效」 |
| 可热更新 | preview 开关、feature 开关、allowedFormats | 写入 DB 即时可读；业务侧按需接入刷新 |

> 8-B 阶段：**配置持久化 + 审计** 为主；resource/auth 运行时读取 DB 为后续 8-C/8-D 接入点。

---

## 四、自动化验收

```bash
node scripts/phase8b-acceptance-test.mjs
```

| ID | 场景 | 预期 |
|----|------|------|
| B1 | GET upload | 含 maxSizeMb、allowedFormats |
| B2 | PUT maxSize | 200 + sys_operation_log update_config |
| B3 | GET oauth | secret 脱敏，无明文 |
| B4 | PUT oauth 保留 secret | 只改 appId，configured 仍为 true |
| B5 | auditor PUT | 403 |
| B6 | maxSize=0 | 400 |

---

## 五、结论

| 项 | 结果 |
|----|------|
| 8-B 自动化 | ☐ 6/6 |
| 总体 | ☐ Go / ☐ No-Go |

验收人：________　日期：________
