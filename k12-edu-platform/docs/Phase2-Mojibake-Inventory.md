# Phase 2 — 乱码文件清单（Mojibake Inventory）

> Step 1 策略：**建清单 + 修 P0**，不在一次迭代中无差别重写全站（见 `admin-phase2-detailed-development-plan.md` §9.5）。

## 优先级定义

| 级别 | 范围 | Step 1 要求 |
|------|------|-------------|
| **P0** | 管理端侧栏/菜单/登录页可见中文 | 必须正常 |
| **P1** | admin views 按钮、表格列、提示文案 | 逐步修复 |
| **P2** | 后端 Java 注释、日志 message | 随触达文件修 |
| **P3** | 历史 SQL 注释、旧文档 | 不阻塞 |

## P0 扫描结果（2026-06-21）

对以下路径做 UTF-8 乱码特征扫描（`�`、`锟`、`鍙` 等）：

- `k12-edu-platform/src/admin/**/*.vue`
- `k12-edu-platform/src/admin/**/*.ts`
- `sys_menu` 菜单 title（sql/78 已 utf8mb4）

**结论：P0 未发现阻塞性乱码。** 侧栏菜单由 `sql/78` 写入正确中文（搜索运营、数据分析等）。

## 已知需关注项（P1/P2，非阻塞）

| 路径 | 类型 | 说明 | 计划 |
|------|------|------|------|
| 部分 Java 源文件 | 注释换行过多 | 不影响编译与 UI | 触达时统一格式 |
| `docs/Phase9-D-验收.md` | 文档过时 | 仍写 `admin:dashboard:view` | 随 Step 1 权限统一更新 |
| deprecated `AdminResourceController` | 旧接口 | 已标记 @Deprecated | Phase 3 统一资源域后移除 |

## 扫描命令（可重复执行）

PowerShell（admin 前端）：

```powershell
Get-ChildItem -Recurse k12-edu-platform\src\admin -Include *.vue,*.ts |
  Select-String -Pattern '[\u0080-\uFFFF]{3,}|�|锟' |
  Select-Object Path, LineNumber, Line
```

MySQL 菜单检查：

```sql
SELECT id, title, path FROM sys_menu WHERE status = 1 AND hidden = 0 ORDER BY sort;
```

## Step 1 验收

- [x] 本清单文档存在
- [x] P0 扫描无阻塞项
- [ ] P1 项在 Sprint 2+ 按模块推进（上传/审核改造时顺带修文案）

## 编码规范（后续写入）

1. 源文件保存为 **UTF-8 无 BOM**
2. SQL 脚本头部 `SET NAMES utf8mb4;`
3. 禁止在业务代码中硬编码 GBK 转义串
4. 新增中文仅通过 i18n 或 SQL seed，避免复制粘贴自错误编码文档
