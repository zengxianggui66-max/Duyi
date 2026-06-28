# K12教育平台 - 后端字典筛选API文档

## 概述

本文档描述了支持小学/初中/高中/美术/舞蹈五个学段的资源筛选API接口。

---

## 1. 数据库升级

### 1.1 执行SQL脚本

按顺序执行以下SQL脚本：

```bash
# 1. 升级字典表结构
sql/alter_dict_table.sql

# 2. 初始化完整字典数据（包含美术/舞蹈）
sql/complete_dict_data.sql

# 3. 插入示例资源数据
sql/sample_resources.sql
```

### 1.2 dict 表结构

```sql
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| type | VARCHAR | 类型：grade_level, subject, resource_type, textbook_version, exam_type, grade |
| label | VARCHAR | 显示标签（前端展示用） |
| value | VARCHAR | 存储值（与前端 code 对应） |
| name | VARCHAR | 名称（兼容字段） |
| code | VARCHAR | 编码（与前端 key 对应） |
| icon | VARCHAR | 图标emoji |
| group_key | VARCHAR | 分组键（用于前端分组展示） |
| grade_levels | VARCHAR | 适用的学段（逗号分隔：primary,junior,senior,art,dance） |
| short_name | VARCHAR | 简称 |
| sort | INT | 排序 |
| status | INT | 状态：0-禁用，1-启用 |
| description | VARCHAR | 描述 |
| deleted | INT | 逻辑删除标记 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

---

## 2. API 接口

### 2.1 获取学段筛选数据

**接口**: `GET /api/dict/filters`

**参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| gradeLevel | String | 是 | 学段：primary, junior, senior, art, dance |

**返回示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "gradeLevels": [
      { "id": 1, "code": "primary", "label": "小学", "icon": "🌈" },
      { "id": 2, "code": "junior", "label": "初中", "icon": "📘" },
      { "id": 3, "code": "senior", "label": "高中", "icon": "🎓" },
      { "id": 4, "code": "art", "label": "美术", "icon": "🎨" },
      { "id": 5, "code": "dance", "label": "舞蹈", "icon": "💃" }
    ],
    "subjects": [
      // 根据 gradeLevel 返回对应的学科
      { "id": 101, "code": "chinese", "label": "语文", "icon": "📖" },
      ...
    ],
    "resourceTypes": [
      // 根据 gradeLevel 返回对应的资源类型
      { "id": 201, "code": "paper_handout", "label": "讲义/练习册", "groupKey": "paper", "icon": "📄" },
      ...
    ],
    "textbookVersions": [
      // 根据 gradeLevel 返回对应的教材版本
      { "id": 301, "code": "pep_primary", "label": "人教版（部编版）", "groupKey": "domestic" },
      ...
    ],
    "examTypes": [
      // 根据 gradeLevel 返回对应的备考类型
      { "id": 401, "code": "sync_monthly", "label": "单元/月考", "groupKey": "sync", "icon": "📝" },
      ...
    ]
  }
}
```

### 2.2 按学段查询字典

**接口**: `GET /api/dict/grade`

**参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | String | 否 | 字典类型 |
| gradeLevel | String | 否 | 学段 |

**返回示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": [
    { "id": 101, "code": "chinese", "label": "语文", "icon": "📖" }
  ]
}
```

### 2.3 获取资源列表

**接口**: `GET /api/resource/list`

**参数**:
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| gradeLevel | String | 否 | 学段：primary, junior, senior, art, dance |
| subject | String | 否 | 学科编码 |
| version | String | 否 | 教材版本编码 |
| resourceType | String | 否 | 资源类型编码 |
| examType | String | 否 | 备考类型编码 |
| keyword | String | 否 | 关键词搜索 |
| current | Integer | 否 | 页码，默认1 |
| size | Integer | 否 | 每页条数，默认15 |

**返回示例**:
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "人教版三年级语文上册全套教案",
        "gradeLevel": "primary",
        "subject": "chinese",
        "version": "pep_primary",
        "resourceType": "doc_courseware",
        "examType": "sync_term"
      }
    ],
    "total": 100,
    "current": 1,
    "size": 15
  }
}
```

---

## 3. 字典类型说明

### 3.1 学段 (grade_level)
| code | label | icon |
|------|-------|------|
| primary | 小学 | 🌈 |
| junior | 初中 | 📘 |
| senior | 高中 | 🎓 |
| art | 美术 | 🎨 |
| dance | 舞蹈 | 💃 |

### 3.2 学科 (subject)

#### 小学 (primary)
| code | label |
|------|-------|
| chinese | 语文 |
| math | 数学 |
| english | 英语 |
| science | 科学 |
| steam | STEAM |
| programming | 编程 |
| calligraphy | 书法 |
| art | 美术 |
| pe | 体育 |
| music | 音乐 |

#### 美术 (art)
| code | label |
|------|-------|
| art_child_creative | 儿童创意美术 |
| art_child_basic | 少儿美术基础 |
| art_sketch | 素描 |
| art_color | 色彩 |
| art_cartoon | 动漫/卡通 |
| art_chinese | 国画 |
| art_calligraphy | 书法 |
| art_handcraft | 手工/黏土 |
| art_exam | 艺考美术 |

#### 舞蹈 (dance)
| code | label |
|------|-------|
| dance_chinese | 中国舞 |
| dance_folk | 少儿民族舞 |
| dance_classical | 古典舞 |
| dance_latin | 拉丁舞 |
| dance_modern | 摩登舞 |
| dance_street | 街舞/流行舞 |
| dance_ballet | 芭蕾基训 |
| dance_exam | 艺考舞蹈 |

### 3.3 资源类型分组 (group_key)

| group_key | 说明 |
|-----------|------|
| paper | 纸质类 |
| media | 音视频类 |
| doc | 文档类 |
| digital | 数字化工具 |
| ext | 拓展资源 |

### 3.4 教材版本分组 (group_key)

| group_key | 说明 |
|-----------|------|
| domestic | 国内课标版 |
| international | 国际原版 |
| institution | 机构定制 |
| exam | 考级教材 |
| manual | 教程手册 |

### 3.5 备考类型分组 (group_key)

| group_key | 说明 |
|-----------|------|
| sync | 校内同步 |
| entrance | 升学备考 |
| cert | 能力认证 |
| exam | 考级 |
| competition | 比赛 |

---

## 4. 前端对接说明

### 4.1 调用示例

```javascript
// 获取小学学段的筛选数据
const filters = await fetch('/api/dict/filters?gradeLevel=primary');

// 获取美术学段的筛选数据
const artFilters = await fetch('/api/dict/filters?gradeLevel=art');

// 获取舞蹈学段的筛选数据
const danceFilters = await fetch('/api/dict/filters?gradeLevel=dance');
```

### 4.2 字段映射

前端 constants/index.ts 中的 key 与后端 code 字段对应：

```typescript
// 前端 key = 后端 code
const subject = { key: 'chinese', name: '语文' }  // 后端: code = 'chinese'
```

---

## 5. 测试步骤

1. 执行数据库升级SQL
2. 重启 k12-resource 服务
3. 访问 `GET /api/dict/filters?gradeLevel=primary` 验证小学数据
4. 访问 `GET /api/dict/filters?gradeLevel=art` 验证美术数据
5. 访问 `GET /api/dict/filters?gradeLevel=dance` 验证舞蹈数据
6. 测试资源列表筛选功能
