# Phase 7-C · 首页专区 Admin 验收

## 自动化

```bash
node scripts/phase7c-acceptance-test.mjs
```

| ID | 项 |
|----|-----|
| C1 | Admin tabs 列表 |
| C2 | 更新 Tab label |
| C3 | featured 列表含标题 |
| C4 | 无效资源 400 |
| C5 | preview API |
| C6 | C 端 sync-prep 200 |

## 手工

1. Admin → 首页配置 → **专区 Tab**：改「课件」展示名并保存  
2. **置顶推荐**：新增/编辑置顶，确认资源 ID 校验  
3. 点击 Tab 行「预览」查看前 8 条  
4. C 端首页同步备课区：置顶资源仍在首位
