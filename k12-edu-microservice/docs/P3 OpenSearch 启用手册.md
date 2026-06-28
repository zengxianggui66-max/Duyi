# P3 OpenSearch 启用手册

> 适用版本：P3 影子接入（OpenSearch + MySQL 双路）  
> 索引名：`k12_search_document_v1`  
> 服务：`k12-resource`（8082），网关（9001）

---

## 一、是否可以暂不启用？

**可以。** 这是当前 P3 的默认用法。

默认配置下 `hosts: ""`，引擎处于**未激活**状态：

| 能力 | 未配置 hosts 时的行为 |
|------|----------------------|
| `/api/search/all` | 仍走 MySQL，与现网一致 |
| 影子对比 | 不执行 |
| 全量 sync | 返回 0，直接跳过 |
| 增量同步 | noop，不影响业务 |
| health 接口 | 返回 `NOT_CONFIGURED` |

**结论：** 可先保留引擎代码与 SQL，等业务数据量上来再启动 OpenSearch 并填配置。在此之前 `sys_search_document`（MySQL 主索引）仍须正常维护。

**建议启用时机：**

- `sys_search_document` 接近 **1 万+** 条
- MySQL 搜索 P95 明显升高（如 > 500ms）
- 需要正文检索、中文分词、拼音、高亮、聚合分面等能力

---

## 二、前置条件

### 2.1 MySQL 表结构

确认已执行：

```bash
mysql -u root -p xinketang --default-character-set=utf8mb4
source sql/48_search_p3_engine.sql
```

会创建/变更：

- `search_engine_shadow_log` — 影子对比日志
- `search_query_log.api_path` — 区分 `all` / `suggest`

### 2.2 MySQL 索引数据

OpenSearch 的数据来源是 `sys_search_document`，不是业务表直读。

```sql
SELECT COUNT(*) FROM sys_search_document WHERE status = 1 AND is_deleted = 0;
-- 当前约 480 条
```

若为空，先重建 MySQL 索引：

```http
POST http://localhost:8082/api/search/admin/reindex
```

---

## 三、启动 OpenSearch（本地 9200）

项目内未内置 docker-compose，需自行启动 OpenSearch。

### 3.1 Docker（推荐）

```powershell
docker run -d --name opensearch `
  -p 9200:9200 -p 9600:9600 `
  -e "discovery.type=single-node" `
  -e "DISABLE_SECURITY_PLUGIN=true" `
  -e "OPENSEARCH_JAVA_OPTS=-Xms512m -Xmx512m" `
  opensearchproject/opensearch:2.14.0
```

验证：

```powershell
curl http://localhost:9200
```

期望返回集群名、版本号等 JSON。

### 3.2 已有集群

- 记录地址，如 `192.168.1.10:9200`
- 若启用 HTTPS / 账号密码，后续配置 `scheme`、`username`、`password`

---

## 四、配置 k12-resource

编辑 `k12-resource/src/main/resources/application.yml`（生产环境用 `application-prod.yml`）：

```yaml
k12:
  search:
    engine:
      enabled: false          # 第一阶段：不替换主搜
      shadow: true            # 开启影子双查
      fallback-to-mysql: true # 引擎失败时回退 MySQL（灰度阶段也要保持 true）
      provider: opensearch
      index-name: k12_search_document_v1
      hosts: localhost:9200   # 关键：非空才会激活引擎
      scheme: http
      username: ""
      password: ""
      traffic-percent: 0      # 影子阶段保持 0
```

**配置逻辑：**

| 配置项 | 说明 |
|--------|------|
| `hosts` | 为空 → `configured=false`，引擎完全不工作 |
| `shadow=true` + `hosts` 已配 | 异步写 `search_engine_shadow_log` |
| `enabled=false` | 前端永远收到 MySQL 结果 |
| `traffic-percent` | 仅 `enabled=true` 时生效，0–100 |

多节点示例：

```yaml
hosts: node1:9200,node2:9200
```

---

## 五、重启 k12-resource

```powershell
cd k12-edu-microservice/k12-resource
mvn spring-boot:run
```

- 服务端口：**8082**
- 网关端口：**9001**（`/api/search/**` 转发到 8082）

---

## 六、健康检查

### 6.1 直接调 resource（本地调试，无需 Token）

```http
GET http://localhost:8082/api/search/admin/engine/health
```

### 6.2 经网关（需要 JWT）

`/api/search/admin/**` 不在网关白名单，需 Header：

```http
GET http://localhost:9001/api/search/admin/engine/health
Authorization: Bearer <登录Token>
```

### 6.3 期望响应（配置正确且 OpenSearch 可达）

```json
{
  "code": 200,
  "data": {
    "provider": "opensearch",
    "indexName": "k12_search_document_v1",
    "configured": true,
    "enabled": false,
    "shadow": true,
    "fallbackToMysql": true,
    "trafficPercent": 0,
    "reachable": true,
    "status": "green",
    "clusterName": "docker-cluster",
    "numberOfNodes": 1,
    "activePrimaryShards": 1,
    "indexDocCount": 480
  }
}
```

### 6.4 常见状态

| status | 含义 | 处理 |
|--------|------|------|
| `NOT_CONFIGURED` | `hosts` 为空 | 填写 `hosts` 并重启 |
| `UNREACHABLE` | OpenSearch 未启动 / 端口错误 | 检查 Docker / 防火墙 |
| `OK` 但 `indexDocCount=0` | 索引未同步 | 执行全量 sync |
| `indexDocCount` ≠ MySQL 条数 | 数据不一致 | 重新 sync 或 reindex |

---

## 七、全量同步

```http
POST http://localhost:8082/api/search/admin/engine/sync
```

行为：

1. 自动创建索引 `k12_search_document_v1`（mapping 见 `k12-resource/src/main/resources/opensearch/k12_search_document_v1.json`）
2. 从 MySQL 读取 `status=1 AND is_deleted=0` 的文档
3. 每批 200 条 bulk 写入 OpenSearch

期望返回：

```json
{ "code": 200, "data": 480 }
```

OpenSearch 侧验证：

```powershell
curl "http://localhost:9200/k12_search_document_v1/_count"
```

也可通过 reindex 触发（MySQL 重建后会异步全量同步引擎）：

```http
POST http://localhost:8082/api/search/admin/reindex
```

---

## 八、影子模式（shadow=true, enabled=false）

### 8.1 请求流程

```
用户请求 /api/search/all?q=教案
  → MySQL 搜索（主路径，立即返回前端）
  → 异步 OpenSearch 查询
  → 写入 search_engine_shadow_log
```

**注意：**

- 仅**带 keyword** 的搜索会写影子日志
- 目前只对比 **`/search/all`**，suggest 尚未做引擎影子对比
- 对比为 **@Async 异步**，搜完立刻查表可能没有记录，等 1～2 秒

### 8.2 制造测试流量

```http
GET http://localhost:9001/api/search/all?q=教案
GET http://localhost:9001/api/search/all?q=语文
GET http://localhost:9001/api/search/all?q=主题班会
```

### 8.3 查看影子日志

```sql
SELECT
  keyword,
  mysql_cost_ms,
  engine_cost_ms,
  mysql_total,
  engine_total,
  top3_overlap_rate,
  top10_overlap_rate,
  engine_error,
  create_time
FROM search_engine_shadow_log
ORDER BY id DESC
LIMIT 20;
```

Top docId 明细：

```sql
SELECT keyword, mysql_top_doc_ids, engine_top_doc_ids
FROM search_engine_shadow_log
ORDER BY id DESC
LIMIT 5;
```

### 8.4 影子阶段验收指标（建议）

| 指标 | 建议阈值 | 说明 |
|------|----------|------|
| `top3_overlap_rate` 均值 | ≥ 0.6 | Top3 结果一致性 |
| `top10_overlap_rate` 均值 | ≥ 0.5 | Top10 一致性 |
| `engine_error` 为空比例 | 100% | 引擎稳定性 |
| `engine_cost_ms` vs `mysql_cost_ms` | 可接受或更优 | 性能参考 |
| `mysql_total` vs `engine_total` | 偏差 < 20% | 召回量一致性 |

汇总 SQL：

```sql
SELECT
  COUNT(*) AS samples,
  ROUND(AVG(top3_overlap_rate), 4) AS avg_top3,
  ROUND(AVG(top10_overlap_rate), 4) AS avg_top10,
  ROUND(AVG(engine_cost_ms), 1) AS avg_engine_ms,
  ROUND(AVG(mysql_cost_ms), 1) AS avg_mysql_ms,
  SUM(engine_error IS NOT NULL) AS error_count
FROM search_engine_shadow_log
WHERE create_time >= DATE_SUB(NOW(), INTERVAL 1 DAY);
```

---

## 九、灰度切换（enabled=true + traffic-percent）

影子数据稳定后，逐步把真实流量切到引擎。

### 9.1 阶段 1：10%

```yaml
k12:
  search:
    engine:
      enabled: true
      shadow: true              # 建议仍开，未命中灰度的用户继续对比
      fallback-to-mysql: true   # 必须保持 true
      traffic-percent: 10
```

**灰度规则：**

- 按 `clientKey`（IP 或 userId）哈希分桶，**同一用户/IP 始终在同一组**
- `hashCode % 100 < trafficPercent` → 走 OpenSearch
- 其余用户仍走 MySQL

引擎命中时：

```
OpenSearch 查 docId 列表
  → 按 docId 从 sys_search_document 组装结果
  → 返回前端
若失败且 fallback-to-mysql=true → 自动回退 MySQL
```

### 9.2 阶段 2：50%

```yaml
traffic-percent: 50
```

### 9.3 阶段 3：100%

```yaml
traffic-percent: 100
```

### 9.4 灰度期间监控

```sql
-- 近 1 天 /search/all 耗时
SELECT
  ROUND(AVG(cost_ms), 1) AS avg_ms,
  MAX(cost_ms) AS max_ms
FROM search_query_log
WHERE api_path = 'all'
  AND create_time >= DATE_SUB(NOW(), INTERVAL 1 DAY)
  AND blocked_code IS NULL;

-- 零结果率
SELECT
  ROUND(SUM(hit_count = 0) * 100.0 / COUNT(*), 2) AS zero_rate_pct
FROM search_query_log
WHERE api_path = 'all'
  AND create_time >= DATE_SUB(NOW(), INTERVAL 1 DAY);
```

---

## 十、增量同步（自动）

配置 `hosts` 后，以下场景会自动同步到 OpenSearch：

| 触发场景 | 机制 |
|----------|------|
| 资源发布/修改/下架 | `SearchDocumentSyncService` → `SearchEngineSyncService` |
| 资讯同步 | 同上 |
| `POST /admin/reindex` | MySQL 重建后 `syncFullAsync()` |

日常一般无需手动 sync；仅在首次接入、索引被误删、怀疑数据不一致时再调用 `POST /admin/engine/sync`。

---

## 十一、配置开关速查

| 阶段 | enabled | shadow | traffic-percent | 前端行为 |
|------|---------|--------|-----------------|----------|
| 未接入 | false | true/false | 0 | 纯 MySQL |
| 影子对比 | false | true | 0 | 纯 MySQL + 后台对比日志 |
| 灰度 10% | true | true | 10 | 10% 引擎，90% MySQL |
| 全量 | true | false* | 100 | 全引擎 + fallback |

\* 全量后 `shadow` 可关以减少双倍查询；也可保留一段时间继续对比。

**切换顺序（推荐）：**

```
shadow=true, enabled=false     → 只对比，不返回引擎结果
enabled=true, traffic-percent=10
enabled=true, traffic-percent=50
enabled=true, traffic-percent=100
fallback-to-mysql=true         → 始终保留
```

---

## 十二、常见问题

| 现象 | 原因 | 处理 |
|------|------|------|
| health 显示 `NOT_CONFIGURED` | `hosts: ""` | 填写地址并重启 |
| sync 返回 0 | hosts 未配置 | 同上 |
| 搜了但没有 shadow 日志 | keyword 为空 / hosts 未配 / shadow=false | 检查配置，用有词搜索 |
| `engine_error` 有值 | 索引不存在 / OpenSearch 不可用 | 先 sync，再查 health |
| `indexDocCount` 远小于 MySQL | sync 未完成 | 重跑 sync |
| 网关调 admin 401 | 未带 Token | 直连 8082 或加 Authorization |
| 灰度用户结果异常 | 引擎排序与 MySQL 不同 | 调 mapping/查询 DSL，或降低 traffic-percent |

---

## 十三、启用 Checklist

```
□ 1. 执行 sql/48_search_p3_engine.sql
□ 2. 确认 sys_search_document 有数据
□ 3. Docker 启动 OpenSearch，curl :9200 通
□ 4. application.yml 设置 hosts: localhost:9200
□ 5. enabled=false, shadow=true, traffic-percent=0
□ 6. 重启 k12-resource
□ 7. GET /admin/engine/health → reachable=true
□ 8. POST /admin/engine/sync → 条数与 MySQL 一致
□ 9. health.indexDocCount ≈ MySQL 文档数
□ 10. 前端搜几个词，search_engine_shadow_log 有记录
□ 11. overlap / error 指标达标
□ 12. enabled=true, traffic-percent=10 → 观察 1～2 天
□ 13. 50% → 100%，fallback-to-mysql 始终 true
```

---

## 十四、相关文档与文件

| 类型 | 路径 |
|------|------|
| 接入决策报告 | `docs/P3搜索引擎接入决策报告.md` |
| 量化评估 | `docs/P3搜索引擎影子接入-量化评估.md` |
| 影子日志 SQL | `sql/48_search_p3_engine.sql` |
| 就绪报表 SQL | `sql/47_search_p3_readiness_reports.sql` |
| 索引 mapping | `k12-resource/src/main/resources/opensearch/k12_search_document_v1.json` |
| 引擎配置 | `k12-resource/src/main/resources/application.yml` → `k12.search.engine` |
