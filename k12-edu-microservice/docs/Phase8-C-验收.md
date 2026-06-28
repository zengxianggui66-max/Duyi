# Phase 8-C · 存储与预览状态探测验收

范围：只读健康 API，resource 服务实现。

---

## 一、部署

```bash
mvn install -pl k12-common,k12-resource,k12-gateway -am -DskipTests
# 重启 resource(8082)、gateway(9001)
```

---

## 二、API

| 方法 | 路径 | 服务 | 权限 |
|------|------|------|------|
| GET | `/api/admin/system/storage/status` | resource | `admin:system:config_view` |
| GET | `/api/admin/system/preview/status` | resource | `admin:system:config_view` |

### storage 响应要点

- 不返回 accessKey / secret
- `reachable=false` 时 HTTP 仍为 **200**
- `localFallback` 含本地上传目录可写状态

### preview 响应要点

- LibreOffice：`--version` 探测（5s 超时），不做真实文档转换
- `sampleProbe` 固定 `skipped`

---

## 三、自动化验收

```bash
node scripts/phase8c-acceptance-test.mjs
```

| ID | 场景 | 预期 |
|----|------|------|
| C1 | storage status | 200，含 provider/reachable |
| C2 | MinIO 未启动 | reachable 可为 false，HTTP 仍 200 |
| C3 | preview status | 200，libreoffice 状态如实 |
| C4 | 无 config_view | auditor → 403 |

---

## 四、结论

| 项 | 结果 |
|----|------|
| 8-C 自动化 | ☐ 4/4 |
| 总体 | ☐ Go / ☐ No-Go |

验收人：________　日期：________
