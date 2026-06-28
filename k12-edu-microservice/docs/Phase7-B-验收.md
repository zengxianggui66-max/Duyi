# Phase 7-B · 热门词 × 搜索提示验收

> 依赖 Phase 7-A（`sql/69` 已执行）

---

## 自动化

```bash
node scripts/phase7b-acceptance-test.mjs
```

| ID | 项 |
|----|-----|
| B1 | suggest 空输入含运营热词 + navTarget |
| B2 | 运营词顺序与 `/api/home/hot-words` 一致 |
| B3 | `/api/search/hot-keywords` 统计榜独立 |
| B4 | browse 热词 navTarget.type=browse |
| B5 | Admin reorder API 200 |

---

## 手工

1. 首页点击搜索框（不输入）→ 下拉「热门」组应出现「一年级语文」等运营词  
2. Admin 热门词 ↑↓ 保存 → 顶栏与 suggest 顺序同步  
3. 点击 suggest 中「期中试卷」→ 进 browse 页（module=期中）  
4. 点击「教案模板」→ 带 keyword 搜索

---

## 交付清单

- [x] `SearchSuggestItemVO.navTarget`
- [x] suggest 优先 ops hot words
- [x] `PUT /api/admin/home/hot-words/reorder`
- [x] Admin 排序 UI + 前端 suggest 跳转
