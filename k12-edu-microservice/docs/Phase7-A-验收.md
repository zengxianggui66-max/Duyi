# Phase 7-A · 首页运营配置验收

> Banner / 快捷入口 / 顶栏热门词 · MySQL CMS · **不依赖 OpenSearch**

---

## 前置条件

1. 执行 `sql/69_phase7a_home_ops.sql`（三表 + 种子）
2. 后端已编译部署：`mvn install -pl k12-common,k12-resource -am`
3. 网关 **9001**、resource **8082** 已启动
4. 前端 `VITE_USE_HOME_OPS_API` 未设为 `false`（默认走 API）

---

## 自动化验收

```bash
node scripts/phase7a-acceptance-test.mjs
# 或指定网关
PHASE7_GATEWAY=http://localhost:9001 node scripts/phase7a-acceptance-test.mjs
```

| ID | 项 | 预期 |
|----|-----|------|
| A1 | 种子数据 | banners ≥3、quick-entries ≥5、hot-words ≥4 |
| A2 | Admin 列表 | `GET /api/admin/home/banners` 200 |
| A3 | 禁用快捷入口 | C 端列表不再包含该卡片 |
| A4 | browse 热词 | `一年级语文` navTarget.type=browse |
| A5 | search 热词 | `教案模板` searchEngine=mysql |
| A6 | 无效 nav_target | Admin POST 返回 400 |

---

## 手工验收

### Admin

1. 登录 `/admin/login`（content_admin / admin123）
2. 进入 **首页配置 → 轮播 Banner**，修改某条标题并保存
3. 刷新 C 端首页，轮播标题已更新（A2）
4. **快捷入口** 禁用一条，C 端卡片消失（A3）
5. **热门词** 新增 browse 型词，顶栏点击进学科浏览页（A4）
6. 新增 search 型词，点击后带 keyword 进浏览页（A5）
7. 保存缺少 subjectKey 的 browse 配置 → 400（A6）

### C 端

| 页面 | 数据源 |
|------|--------|
| `HomePage.vue` 轮播/快捷功能 | `GET /api/home/banners`、`/quick-entries` |
| `AppHeader.vue` 热门词 | `GET /api/home/hot-words` |
| 降级 | `VITE_USE_HOME_OPS_API=false` → `homeOpsStatic.ts` |

---

## API 速查

| 类型 | 路径 |
|------|------|
| C 端 | `GET /api/home/banners`、`/quick-entries`、`/hot-words`、`/hero` |
| Admin | `GET/POST/PUT/DELETE /api/admin/home/banners|quick-entries|hot-words` |
| 权限 | `admin:home:view` / `admin:home:edit` |

---

## 与 OpenSearch

- `NavTarget.searchEngine=mysql` 为默认；hosts 为空时 7-A 功能全部正常（A7）
- `auto` 预留 Phase 7-F，当前忽略

---

## 交付清单

- [x] `sql/69_phase7a_home_ops.sql`
- [x] 后端 Entity/Mapper/Service/Controller
- [x] Admin 三子页 + 子路由
- [x] C 端切源 + feature flag
- [x] `scripts/phase7a-acceptance-test.mjs`
