# 学段资源（单元文档）后端 API 文档

## 项目信息
- 模块名称：k12-resource
- 基础路径：`/api/unit-doc`
- 数据库表：`oss_unit_doc` (xinketang)
- 开发完成时间：2026-05-10

---

## 一、API 接口列表

### 1. 创建单元文档
- **接口**: `POST /api/unit-doc/create`
- **描述**: 创建新的单元文档记录
- **请求体**:
  ```json
  {
    "unitName": "第一单元",
    "originalFilename": "第一单元知识必记.doc",
    "ossBucket": "qier-duuyi",
    "ossObjectKey": "第一单元知识必记.doc",
    "ossUrl": "https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/...",
    "fileSizeKb": 19
  }
  ```
- **响应**: 创建的文档对象（包含ID）

---

### 2. 根据ID查询
- **接口**: `GET /api/unit-doc/{id}`
- **描述**: 根据主键ID查询文档详情
- **路径参数**: `id` - 文档ID
- **响应**: 文档对象

---

### 3. 根据单元名称查询
- **接口**: `GET /api/unit-doc/by-unit?unitName=第一单元`
- **描述**: 根据单元名称查询所有相关文档
- **查询参数**: `unitName` - 单元名称
- **响应**: 文档列表

---

### 4. 分页查询
- **接口**: `GET /api/unit-doc/list`
- **描述**: 分页查询文档列表
- **查询参数**:
  - `current` (默认1) - 当前页
  - `size` (默认10) - 每页大小
  - `unitName` (可选) - 单元名称（模糊查询）
  - `originalFilename` (可选) - 文件名（模糊查询）
  - `fileType` (可选) - 文件类型（doc/docx/pdf等）
  - `sortField` (默认uploadTime) - 排序字段
  - `sortOrder` (默认desc) - 排序方向（asc/desc）
- **响应**:
  ```json
  {
    "list": [...],
    "total": 100,
    "current": 1,
    "size": 10
  }
  ```

---

### 5. 查询所有（不分页）
- **接口**: `GET /api/unit-doc/all`
- **描述**: 查询所有文档（可根据条件过滤）
- **查询参数**: 同分页查询参数（不包括current和size）
- **响应**: 文档列表

---

### 6. 文件名模糊搜索
- **接口**: `GET /api/unit-doc/search?filename=知识`
- **描述**: 根据文件名模糊搜索文档
- **查询参数**: `filename` - 文件名关键词
- **响应**: 文档列表

---

### 7. 根据文件类型查询
- **接口**: `GET /api/unit-doc/by-type?fileType=doc`
- **描述**: 根据文件扩展名查询文档
- **查询参数**: `fileType` - 文件类型（doc/docx/pdf/ppt/pptx等）
- **响应**: 文档列表

---

### 8. 根据文件大小范围查询
- **接口**: `GET /api/unit-doc/by-size?minKb=10&maxKb=1000`
- **描述**: 查询文件大小在某个范围内的文档
- **查询参数**:
  - `minKb` - 最小文件大小（KB）
  - `maxKb` - 最大文件大小（KB）
- **响应**: 文档列表

---

### 9. 更新文档
- **接口**: `PUT /api/unit-doc/update`
- **描述**: 更新文档信息
- **请求体**: 文档对象（包含ID）
- **响应**: `true`/`false`

---

### 10. 更新OSS URL
- **接口**: `PUT /api/unit-doc/update-oss-url?id=1&ossUrl=xxx`
- **描述**: 更新文档的OSS访问URL
- **查询参数**:
  - `id` - 文档ID
  - `ossUrl` - 新的OSS URL
- **响应**: `true`/`false`

---

### 11. 删除文档（逻辑删除）
- **接口**: `DELETE /api/unit-doc/delete/{id}`
- **描述**: 逻辑删除文档（设置is_deleted=1）
- **路径参数**: `id` - 文档ID
- **响应**: `true`/`false`

---

### 12. 获取统计信息
- **接口**: `GET /api/unit-doc/stats`
- **描述**: 获取文档统计信息（总数、总大小、平均大小、最新上传时间）
- **响应**:
  ```json
  {
    "total_count": 100,
    "total_size_kb": 51200,
    "avg_size_kb": 512.0,
    "latest_upload": "2026-05-10 09:00:00"
  }
  ```

---

### 13. 获取所有单元名称
- **接口**: `GET /api/unit-doc/unit-names`
- **描述**: 获取所有不重复的单元名称列表
- **响应**: 单元名称字符串列表

---

### 14. 批量创建文档
- **接口**: `POST /api/unit-doc/batch-create`
- **描述**: 批量创建文档记录
- **请求体**: 文档对象数组
- **响应**:
  ```json
  {
    "successCount": 8,
    "totalCount": 10,
    "errors": [...]
  }
  ```

---

### 15. 根据单元名称统计文档数量
- **接口**: `GET /api/unit-doc/count-by-unit?unitName=第一单元`
- **描述**: 统计指定单元的文档数量
- **查询参数**: `unitName` - 单元名称
- **响应**: 文档数量（Integer）

---

### 16. 获取所有文档总大小
- **接口**: `GET /api/unit-doc/total-size`
- **描述**: 获取所有文档的总大小（单位：MB）
- **响应**: 总大小（Double，保留两位小数）

---

## 二、实体类字段说明

### UnitDoc 实体类
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 主键ID，自增 |
| unitName | String | 单元名称 |
| originalFilename | String | 原始文件名 |
| ossBucket | String | OSS Bucket名称（默认：qier-duuyi） |
| ossObjectKey | String | OSS文件路径 |
| ossUrl | String | OSS访问URL |
| fileSizeKb | Integer | 文件大小（KB） |
| uploadTime | LocalDateTime | 上传时间 |
| isDeleted | Integer | 是否删除：0=正常，1=删除 |

### 附加方法
- `getFileSizeMb()` - 获取文件大小（MB，保留两位小数）
- `getFileType()` - 根据文件名获取文件类型（doc/docx/pdf/ppt/pptx/xls/xlsx/other）

---

## 三、前端集成建议

### 1. 在 FeatureChannel.vue 中使用
```javascript
// api/unitDoc.js
import request from '@/utils/request'

export function getUnitDocs(params) {
  return request({
    url: '/api/unit-doc/list',
    method: 'get',
    params
  })
}

export function getUnitDocById(id) {
  return request({
    url: `/api/unit-doc/${id}`,
    method: 'get'
  })
}

export function createUnitDoc(data) {
  return request({
    url: '/api/unit-doc/create',
    method: 'post',
    data
  })
}
```

### 2. 在 Store 中调用
```javascript
// store/modules/unitDoc.js
import { getUnitDocs } from '@/api/unitDoc'

const state = {
  list: [],
  total: 0
}

const actions = {
  async fetchUnitDocs({ commit }, query) {
    const res = await getUnitDocs(query)
    commit('SET_LIST', res.data.list)
    commit('SET_TOTAL', res.data.total)
  }
}
```

---

## 四、数据库配置

### 确保数据库连接正确
检查 `application.yml` 中的数据库配置：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/xinketang?useUnicode=true&characterEncoding=utf-8
    username: root
    password: your_password
```

### 执行初始化SQL
```bash
mysql -u root -p xinketang < sql/oss_unit_doc.sql
```

---

## 五、启动与测试

### 1. 启动 k12-resource 服务
```bash
cd k12-edu-microservice/k12-resource
mvn spring-boot:run
```

### 2. 测试API
```bash
# 查询所有文档
curl <http://localhost:8082/api/unit-doc/list>

# 根据单元名称查询
curl "<http://localhost:8082/api/unit-doc/by-unit?unitName=第一单元>"

# 创建文档
curl -X POST <http://localhost:8082/api/unit-doc/create> \\
  -H "Content-Type: application/json" \\
  -d '{"unitName":"测试单元","originalFilename":"test.doc","ossUrl":"...","fileSizeKb":20}'
```

---

## 六、注意事项

1. **逻辑删除**: 删除操作只是设置 `is_deleted=1`，不会真正删除数据
2. **文件类型过滤**: `fileType` 参数根据文件名后缀匹配（doc/docx/pdf等）
3. **OSS URL 更新**: 当OSS中的文件URL变化时，可以调用更新接口同步
4. **分页参数**: `current` 从1开始，`size` 默认10
5. **排序**: 支持按 `uploadTime`、`unitName`、`fileSize` 排序

---

## 七、已创建的文件清单

1. `k12-common/src/main/java/com/k12/common/entity/UnitDoc.java` - 实体类
2. `k12-common/src/main/java/com/k12/common/dto/UnitDocQueryDTO.java` - 查询DTO
3. `k12-resource/src/main/java/com/k12/resource/mapper/UnitDocMapper.java` - Mapper接口
4. `k12-resource/src/main/java/com/k12/resource/service/UnitDocService.java` - Service接口
5. `k12-resource/src/main/java/com/k12/resource/service/impl/UnitDocServiceImpl.java` - Service实现类
6. `k12-resource/src/main/java/com/k12/resource/controller/UnitDocController.java` - Controller
7. `sql/oss_unit_doc.sql` - 数据库初始化脚本

---

## 八、后续扩展建议

1. **文件上传接口**: 集成OSS上传功能，自动生成 `oss_url`
2. **权限控制**: 添加Spring Security，限制创建/删除操作
3. **缓存优化**: 对频繁查询的单元名称列表添加Redis缓存
4. **文件预览**: 集成文档预览功能（PDF、Office文档等）
5. **批量操作**: 扩展批量删除、批量更新等功能
