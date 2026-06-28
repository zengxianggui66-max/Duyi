# Phase 7-F · 运营收尾（缓存 / 定时 / OpenSearch auto）

> **范围**：C 端读缓存、定时上下线、Admin 聚合预览、searchEngine=auto  
> **OpenSearch**：hosts 未配置时 auto 自动回退 MySQL（与 P3 手册一致）

---

## 一、能力清单

| 能力 | 实现 |
|------|------|
| C 端读缓存 60s | `HomeCacheService` 内存 TTL |
| Admin 写后失效 | `HomeCacheEvictAspect` |
| 定时上下线 | `HomeScheduleService` 每分钟扫 banner/hot_word |
| Admin 预览 | `GET /api/admin/home/preview` |
| C 端 bootstrap | `GET /api/home/bootstrap` |
| 清缓存 | `POST /api/admin/home/cache/invalidate` |
| 手动跑定时 | `POST /api/admin/home/schedule/run` |
| searchEngine=auto | `/api/search/all?searchEngine=auto` |

配置：`k12.home.cache.bootstrap-ttl-seconds=60`

---

## 二、Admin 路由

| 路径 | 组件 |
|------|------|
| `/admin/home-config/preview` | `HomePreview.vue` |

---

## 三、验收

```bash
node scripts/phase7f-acceptance-test.mjs
node scripts/phase7-acceptance-test.mjs   # 7-A~7-F 全量
```

| # | 项 | 预期 |
|---|-----|------|
| F1 | cache stats | enabled=true |
| F2 | admin preview | hero + latestColumns |
| F3 | C bootstrap | 200 |
| F4 | cache invalidate | 200 |
| F5 | schedule run | changed≥0 |
| F6 | searchEngine=auto | 200（hosts 空则 MySQL 回退） |
| F7 | search engine health | 200 |

---

## 四、OpenSearch 启用（可选）

见 [P3 OpenSearch 启用手册](./P3%20OpenSearch%20启用手册.md)：

```yaml
k12.search.engine:
  enabled: true
  hosts: localhost:9200
  traffic-percent: 0   # 灰度 0，仅 auto 热词走引擎
```

---

## 五、Phase 7 完成

7-A ~ 7-F 全部交付，首页运营配置闭环。
