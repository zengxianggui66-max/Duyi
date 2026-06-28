# Phase 7 完成总结

## 子阶段

| 阶段 | 内容 | 验收脚本 |
|------|------|----------|
| 7-A | Banner / 快捷入口 / 热门词 CMS | phase7a |
| 7-B | 热门词 × search suggest | phase7b |
| 7-C | 专区 Tab / 置顶推荐 Admin | phase7c |
| 7-D | 最新内容三列 | phase7d |
| 7-E | 升学入口 + 特色频道 Admin | phase7e |
| 7-F | 缓存 / 定时 / 预览 / OS auto | phase7f |

## 一键验收

```bash
node scripts/phase7-acceptance-test.mjs
```

## Admin 入口

`/admin/home-config` → 轮播、快捷入口、热门词、专区 Tab、置顶、最新内容、升学入口、特色频道、聚合预览

## SQL 清单

| 脚本 | 说明 |
|------|------|
| 69 | home_banner / quick_entry / hot_word |
| 70 | home_latest_column |
| 71 | ops_channel 三表 |

## 运维

- 改配置后：Admin 写操作自动清缓存，或「聚合预览」页点清缓存
- 定时上下线：默认每分钟扫描；可 `POST /admin/home/schedule/run` 手动触发
- OpenSearch：默认 OFF；热词 `searchEngine=auto` 在 hosts 配置后生效
