# K12 教育平台数据库设计文档

## 一、设计概述

### 1.1 设计目标
为 K12 教育平台（新课堂）设计一套完整、规范、可扩展的数据库结构，支持：
- 多学段（小学/初中/高中/美术/舞蹈）
- 多学科（语文/数学/英语/物理/化学等）
- 多版本教材（人教版/统编版/北师大版等）
- 多册别年级（一年级上册~六年级下册等）
- 单元目录树结构
- 资源分类管理（栏目/类型）
- 资源存储与检索（OSS 文件存储）

### 1.2 设计原则
- **规范化**：消除冗余，遵循第三范式
- **可扩展性**：支持未来新增学段、学科、版本
- **查询效率**：针对高频查询场景建立联合索引
- **数据一致性**：外键约束 + 逻辑删除（软删除）

---

## 二、数据维度分析

根据前端页面 `SubjectDetailPage.vue` 及关联组件，提取以下核心数据维度：

| 维度 | 前端来源 | 说明 |
|------|----------|------|
| 学段(Stage) | `subjectConfig.ts` - stages | 小学/初中/高中/美术/舞蹈 |
| 学科(Subject) | `subjectConfig.ts` - subjectDataMap | 各学段下的学科列表 |
| 版本(Edition) | `subjectConfig.ts` - subjectVersionsMap | 人教版/统编版/北师大版等 |
| 册别(Volume) | `volumeData.ts` - volumeDataMap | 一年级上册/下册等 |
| 单元(Unit) | `unitData.ts` - unitDataMap | 单元-课文树形结构 |
| 栏目(Module) | `subjectConfig.ts` - columnConfig | 同步备课/月考/期中/期末等 |
| 资源类型(Type) | `useResourceFilter.ts` | 课件/教案/练习/试卷/学案等 |
| 资源(Resource) | `oss_primary_chinese_resource` | 实际文件资源记录 |

---

## 三、表结构设计

### 3.1 学段表 `edu_stage`

```sql
CREATE TABLE `edu_stage` (
    `id`          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '学段ID',
    `stage_key`   VARCHAR(20)  NOT NULL UNIQUE COMMENT '学段标识：primary/junior/senior/art/dance',
    `stage_name`  VARCHAR(20)  NOT NULL COMMENT '学段名称：小学/初中/高中/美术/舞蹈',
    `sort_order`  INT          DEFAULT 0 NOT NULL COMMENT '排序权重',
    `status`      TINYINT      DEFAULT 1 NOT NULL COMMENT '状态：0=禁用 1=启用',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    INDEX `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学段配置表';
```

**初始数据：**
```sql
INSERT INTO `edu_stage` (`stage_key`, `stage_name`, `sort_order`) VALUES
('primary', '小学', 1),
('junior', '初中', 2),
('senior', '高中', 3),
('art', '美术', 4),
('dance', '舞蹈', 5);
```

---

### 3.2 学科表 `edu_subject`

```sql
CREATE TABLE `edu_subject` (
    `id`          BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '学科ID',
    `stage_id`    BIGINT UNSIGNED NOT NULL COMMENT '所属学段ID',
    `subject_key` VARCHAR(30)  NOT NULL COMMENT '学科标识：chinese/math/english/physics等',
    `subject_name` VARCHAR(30) NOT NULL COMMENT '学科名称：语文/数学/英语等',
    `is_new`      TINYINT      DEFAULT 0 NOT NULL COMMENT '是否新课标：0=否 1=是',
    `sort_order`  INT          DEFAULT 0 NOT NULL COMMENT '排序权重',
    `status`      TINYINT      DEFAULT 1 NOT NULL COMMENT '状态：0=禁用 1=启用',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    UNIQUE KEY `uk_stage_subject` (`stage_id`, `subject_key`),
    INDEX `idx_stage_status` (`stage_id`, `status`, `sort_order`),
    CONSTRAINT `fk_subject_stage` FOREIGN KEY (`stage_id`) REFERENCES `edu_stage` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学科配置表';
```

**初始数据（示例）：**
```sql
-- 小学学科
INSERT INTO `edu_subject` (`stage_id`, `subject_key`, `subject_name`, `is_new`, `sort_order`) VALUES
(1, 'chinese', '语文', 0, 1),
(1, 'math', '数学', 0, 2),
(1, 'english', '英语', 1, 3);

-- 初中学科
INSERT INTO `edu_subject` (`stage_id`, `subject_key`, `subject_name`, `is_new`, `sort_order`) VALUES
(2, 'chinese', '语文', 0, 1),
(2, 'math', '数学', 0, 2),
(2, 'english', '英语', 0, 3),
(2, 'physics', '物理', 0, 4),
(2, 'chemistry', '化学', 0, 5);
```

---

### 3.3 教材版本表 `edu_edition`

```sql
CREATE TABLE `edu_edition` (
    `id`            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '版本ID',
    `subject_id`    BIGINT UNSIGNED NOT NULL COMMENT '所属学科ID',
    `edition_key`   VARCHAR(30)  NOT NULL COMMENT '版本标识：renjiao/tongbian/beishida等',
    `edition_name`  VARCHAR(30)  NOT NULL COMMENT '版本名称：人教版/统编版/北师大版等',
    `publisher`     VARCHAR(100) DEFAULT '' COMMENT '出版社名称',
    `is_new`        TINYINT      DEFAULT 0 NOT NULL COMMENT '是否新教材：0=否 1=是',
    `sort_order`    INT          DEFAULT 0 NOT NULL COMMENT '排序权重',
    `status`        TINYINT      DEFAULT 1 NOT NULL COMMENT '状态：0=禁用 1=启用',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    UNIQUE KEY `uk_subject_edition` (`subject_id`, `edition_key`),
    INDEX `idx_subject_status` (`subject_id`, `status`, `sort_order`),
    CONSTRAINT `fk_edition_subject` FOREIGN KEY (`subject_id`) REFERENCES `edu_subject` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教材版本配置表';
```

**初始数据（示例）：**
```sql
-- 小学语文版本
INSERT INTO `edu_edition` (`subject_id`, `edition_key`, `edition_name`, `publisher`, `is_new`, `sort_order`) VALUES
(1, 'renjiao', '人教版', '人民教育出版社', 1, 1),
(1, 'tongbian', '统编版(2024)', '人民教育出版社', 1, 2),
(1, 'beishida', '北师大版', '北京师范大学出版社', 0, 3);
```

---

### 3.4 册别表 `edu_volume`

```sql
CREATE TABLE `edu_volume` (
    `id`            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '册别ID',
    `stage_id`      BIGINT UNSIGNED NOT NULL COMMENT '所属学段ID',
    `volume_key`    VARCHAR(20)  NOT NULL COMMENT '册别标识：y1s1/y1s2/y2s1等',
    `volume_name`   VARCHAR(30)  NOT NULL COMMENT '册别名称：一年级上册/一年级下册等',
    `grade_level`   VARCHAR(20)  NOT NULL COMMENT '年级：一年级/二年级/七年级/必修一等',
    `semester`      VARCHAR(10)  NOT NULL COMMENT '学期：上册/下册/全一册',
    `is_new`        TINYINT      DEFAULT 0 NOT NULL COMMENT '是否新教材：0=否 1=是',
    `sort_order`    INT          DEFAULT 0 NOT NULL COMMENT '排序权重',
    `status`        TINYINT      DEFAULT 1 NOT NULL COMMENT '状态：0=禁用 1=启用',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    UNIQUE KEY `uk_stage_volume` (`stage_id`, `volume_key`),
    INDEX `idx_stage_status` (`stage_id`, `status`, `sort_order`),
    CONSTRAINT `fk_volume_stage` FOREIGN KEY (`stage_id`) REFERENCES `edu_stage` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='册别（年级）配置表';
```

**初始数据（示例）：**
```sql
-- 小学册别
INSERT INTO `edu_volume` (`stage_id`, `volume_key`, `volume_name`, `grade_level`, `semester`, `is_new`, `sort_order`) VALUES
(1, 'y1s1', '一年级上册', '一年级', '上册', 1, 1),
(1, 'y1s2', '一年级下册', '一年级', '下册', 1, 2),
(1, 'y2s1', '二年级上册', '二年级', '上册', 1, 3),
(1, 'y2s2', '二年级下册', '二年级', '下册', 1, 4),
(1, 'y3s1', '三年级上册', '三年级', '上册', 1, 5),
(1, 'y3s2', '三年级下册', '三年级', '下册', 1, 6),
(1, 'y4s1', '四年级上册', '四年级', '上册', 0, 7),
(1, 'y4s2', '四年级下册', '四年级', '下册', 0, 8),
(1, 'y5s1', '五年级上册', '五年级', '上册', 0, 9),
(1, 'y5s2', '五年级下册', '五年级', '下册', 0, 10),
(1, 'y6s1', '六年级上册', '六年级', '上册', 0, 11),
(1, 'y6s2', '六年级下册', '六年级', '下册', 0, 12);
```

---

### 3.5 单元目录表 `edu_unit`

```sql
CREATE TABLE `edu_unit` (
    `id`            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '单元ID',
    `subject_id`    BIGINT UNSIGNED NOT NULL COMMENT '所属学科ID',
    `edition_id`    BIGINT UNSIGNED NOT NULL COMMENT '所属版本ID',
    `volume_id`     BIGINT UNSIGNED NOT NULL COMMENT '所属册别ID',
    `unit_name`     VARCHAR(100) NOT NULL COMMENT '单元名称：第一单元·识字',
    `unit_order`    INT          DEFAULT 0 NOT NULL COMMENT '单元排序',
    `parent_id`     BIGINT UNSIGNED DEFAULT 0 COMMENT '父单元ID：0=根单元（一级单元）',
    `status`        TINYINT      DEFAULT 1 NOT NULL COMMENT '状态：0=禁用 1=启用',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    INDEX `idx_subject_edition_volume` (`subject_id`, `edition_id`, `volume_id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_status` (`status`),
    CONSTRAINT `fk_unit_subject` FOREIGN KEY (`subject_id`) REFERENCES `edu_subject` (`id`),
    CONSTRAINT `fk_unit_edition` FOREIGN KEY (`edition_id`) REFERENCES `edu_edition` (`id`),
    CONSTRAINT `fk_unit_volume` FOREIGN KEY (`volume_id`) REFERENCES `edu_volume` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单元目录表';
```

**设计说明：**
- 采用**邻接表模型**（parent_id）存储树形结构，支持多级单元
- 一级单元：`parent_id = 0`，如"第一单元·识字"
- 二级单元：`parent_id = 一级单元ID`，如"春夏秋冬"、"姓氏歌"
- 前端 `unitData.ts` 中的 `subUnits` 对应二级单元

**初始数据（示例）：**
```sql
-- 小学语文一年级下册 人教版 单元
INSERT INTO `edu_unit` (`subject_id`, `edition_id`, `volume_id`, `unit_name`, `unit_order`, `parent_id`) VALUES
(1, 1, 2, '第一单元·识字', 1, 0),
(1, 1, 2, '第二单元·课文', 2, 0),
(1, 1, 2, '第三单元·课文', 3, 0),
(1, 1, 2, '第四单元·课文', 4, 0),
(1, 1, 2, '第五单元·识字', 5, 0),
(1, 1, 2, '第六单元·课文', 6, 0),
(1, 1, 2, '第七单元·课文', 7, 0),
(1, 1, 2, '第八单元·课文', 8, 0);

-- 子单元（课文）
INSERT INTO `edu_unit` (`subject_id`, `edition_id`, `volume_id`, `unit_name`, `unit_order`, `parent_id`) VALUES
(1, 1, 2, '春夏秋冬', 1, 1),      -- 所属第一单元·识字
(1, 1, 2, '姓氏歌', 2, 1),
(1, 1, 2, '吃水不忘挖井人', 1, 2), -- 所属第二单元·课文
(1, 1, 2, '我多想去看看', 2, 2);
```

---

### 3.6 栏目配置表 `edu_module`

```sql
CREATE TABLE `edu_module` (
    `id`            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '栏目ID',
    `stage_id`      BIGINT UNSIGNED NOT NULL COMMENT '所属学段ID',
    `module_name`   VARCHAR(50)  NOT NULL COMMENT '栏目名称：同步备课/月考/期中/期末等',
    `module_key`    VARCHAR(50)  NOT NULL COMMENT '栏目标识：sync_exam/monthly/midterm等',
    `sort_order`    INT          DEFAULT 0 NOT NULL COMMENT '排序权重',
    `status`        TINYINT      DEFAULT 1 NOT NULL COMMENT '状态：0=禁用 1=启用',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    UNIQUE KEY `uk_stage_module` (`stage_id`, `module_key`),
    INDEX `idx_stage_status` (`stage_id`, `status`, `sort_order`),
    CONSTRAINT `fk_module_stage` FOREIGN KEY (`stage_id`) REFERENCES `edu_stage` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源栏目配置表';
```

**初始数据：**
```sql
-- 小学栏目
INSERT INTO `edu_module` (`stage_id`, `module_name`, `module_key`, `sort_order`) VALUES
(1, '同步备课', 'sync_prep', 1),
(1, '开学专区', 'opening', 2),
(1, '月考', 'monthly', 3),
(1, '期中', 'midterm', 4),
(1, '期末', 'final', 5),
(1, '小升初真题', 'xsc_real', 6),
(1, '小升初模拟', 'xsc_sim', 7),
(1, '专题复习', 'topic_review', 8),
(1, '真题汇编', 'real_collection', 9),
(1, '暑假', 'summer', 10),
(1, '寒假', 'winter', 11),
(1, '作文', 'composition', 12),
(1, '阅读', 'reading', 13),
(1, '竞赛', 'competition', 14);

-- 初中栏目
INSERT INTO `edu_module` (`stage_id`, `module_name`, `module_key`, `sort_order`) VALUES
(2, '同步备课', 'sync_prep', 1),
(2, '开学专区', 'opening', 2),
(2, '月考', 'monthly', 3),
(2, '期中', 'midterm', 4),
(2, '期末', 'final', 5),
(2, '学业水平', 'academic', 6),
(2, '一轮复习', 'review_1', 7),
(2, '二轮专题', 'review_2', 8),
(2, '三轮冲刺', 'review_3', 9),
(2, '中考模拟', 'zk_sim', 10),
(2, '中考真题', 'zk_real', 11),
(2, '真题汇编', 'real_collection', 12),
(2, '暑假', 'summer', 13),
(2, '寒假', 'winter', 14),
(2, '作文', 'composition', 15),
(2, '阅读', 'reading', 16),
(2, '竞赛', 'competition', 17),
(2, '纯素材', 'material', 18);
```

---

### 3.7 资源类型表 `edu_resource_type`

```sql
CREATE TABLE `edu_resource_type` (
    `id`            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '类型ID',
    `type_name`     VARCHAR(30)  NOT NULL COMMENT '类型名称：课件/教案/练习/试卷等',
    `type_key`      VARCHAR(30)  NOT NULL COMMENT '类型标识：courseware/lesson_plan/exercise等',
    `category`      VARCHAR(20)  DEFAULT 'resource' COMMENT '分类：resource=普通资源 teacher_book=教师用书',
    `sort_order`    INT          DEFAULT 0 NOT NULL COMMENT '排序权重',
    `status`        TINYINT      DEFAULT 1 NOT NULL COMMENT '状态：0=禁用 1=启用',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    UNIQUE KEY `uk_type_key` (`type_key`),
    INDEX `idx_category_status` (`category`, `status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源类型配置表';
```

**初始数据：**
```sql
-- 普通资源类型
INSERT INTO `edu_resource_type` (`type_name`, `type_key`, `category`, `sort_order`) VALUES
('课件', 'courseware', 'resource', 1),
('教案', 'lesson_plan', 'resource', 2),
('练习', 'exercise', 'resource', 3),
('试卷', 'exam_paper', 'resource', 4),
('学案', 'study_guide', 'resource', 5),
('电子课本', 'textbook', 'resource', 6),
('教学反思', 'reflection', 'resource', 7),
('音频/朗读', 'audio', 'resource', 8),
('视频', 'video', 'resource', 9),
('知识点', 'knowledge', 'resource', 10);

-- 教师用书类型
INSERT INTO `edu_resource_type` (`type_name`, `type_key`, `category`, `sort_order`) VALUES
('课堂实录', 'class_record', 'teacher_book', 11),
('讲义', 'lecture', 'teacher_book', 12),
('说课稿', 'lesson_talk', 'teacher_book', 13),
('教学设计', 'teaching_design', 'teacher_book', 14),
('预习', 'preview', 'teacher_book', 15),
('教学总结', 'summary', 'teacher_book', 16),
('逐字稿', 'script', 'teacher_book', 17),
('公开课', 'open_class', 'teacher_book', 18);
```

---

### 3.8 资源主表 `edu_resource`

> **替代原有 `oss_primary_chinese_resource`，更通用化**

```sql
CREATE TABLE `edu_resource` (
    `id`                BIGINT UNSIGNED AUTO_INCREMENT COMMENT '资源唯一ID' PRIMARY KEY,
    `stage_id`          BIGINT UNSIGNED NOT NULL COMMENT '学段ID',
    `subject_id`        BIGINT UNSIGNED NOT NULL COMMENT '学科ID',
    `edition_id`        BIGINT UNSIGNED NOT NULL COMMENT '版本ID',
    `volume_id`         BIGINT UNSIGNED NOT NULL COMMENT '册别ID',
    `unit_id`           BIGINT UNSIGNED DEFAULT NULL COMMENT '单元ID（可为空，表示不属于特定单元）',
    `module_id`         BIGINT UNSIGNED NOT NULL COMMENT '栏目ID',
    `type_id`           BIGINT UNSIGNED NOT NULL COMMENT '资源类型ID',
    `title`             VARCHAR(200) NOT NULL COMMENT '资源标题',
    `description`       VARCHAR(500) DEFAULT '' COMMENT '资源描述',
    `original_filename` VARCHAR(150) NOT NULL COMMENT '原始文件名',
    `file_ext`          VARCHAR(20)  NOT NULL COMMENT '文件扩展名：ppt/pptx/doc/pdf/rar/mp4等',
    `oss_bucket`        VARCHAR(64)  DEFAULT 'qier-duuyi' NOT NULL COMMENT 'OSS存储桶',
    `oss_object_key`    VARCHAR(255) NOT NULL COMMENT 'OSS文件唯一路径',
    `oss_url`           VARCHAR(500) NOT NULL COMMENT 'OSS访问URL',
    `file_size_kb`      INT UNSIGNED NOT NULL COMMENT '文件大小（KB）',
    `download_count`    INT          DEFAULT 0 NOT NULL COMMENT '下载次数',
    `view_count`        INT UNSIGNED DEFAULT 0 NOT NULL COMMENT '浏览次数',
    `status`            TINYINT      DEFAULT 1 NOT NULL COMMENT '状态：0=待审核 1=已发布 2=审核不通过 3=下架',
    `uploader_id`       BIGINT UNSIGNED NULL COMMENT '上传者ID',
    `upload_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '上传时间',
    `update_time`       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted`        TINYINT      DEFAULT 0 NOT NULL COMMENT '是否删除：0=正常 1=已删除',
    `sort`              INT          DEFAULT 0 NOT NULL COMMENT '排序权重',
    `remark`            VARCHAR(255) DEFAULT '' NULL COMMENT '备注',

    -- 联合索引（覆盖前端主要查询场景）
    INDEX `idx_stage_subject` (`stage_id`, `subject_id`),
    INDEX `idx_edition_volume` (`edition_id`, `volume_id`),
    INDEX `idx_module_type` (`module_id`, `type_id`),
    INDEX `idx_unit` (`unit_id`),
    INDEX `idx_status_deleted` (`status`, `is_deleted`),
    INDEX `idx_upload_time` (`upload_time`),
    INDEX `idx_sort` (`sort`),

    -- 外键约束
    CONSTRAINT `fk_resource_stage` FOREIGN KEY (`stage_id`) REFERENCES `edu_stage` (`id`),
    CONSTRAINT `fk_resource_subject` FOREIGN KEY (`subject_id`) REFERENCES `edu_subject` (`id`),
    CONSTRAINT `fk_resource_edition` FOREIGN KEY (`edition_id`) REFERENCES `edu_edition` (`id`),
    CONSTRAINT `fk_resource_volume` FOREIGN KEY (`volume_id`) REFERENCES `edu_volume` (`id`),
    CONSTRAINT `fk_resource_unit` FOREIGN KEY (`unit_id`) REFERENCES `edu_unit` (`id`),
    CONSTRAINT `fk_resource_module` FOREIGN KEY (`module_id`) REFERENCES `edu_module` (`id`),
    CONSTRAINT `fk_resource_type` FOREIGN KEY (`type_id`) REFERENCES `edu_resource_type` (`id`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教育资源资源主表';
```

---

### 3.9 资源标签关联表 `edu_resource_tag`（可选扩展）

```sql
CREATE TABLE `edu_resource_tag` (
    `id`            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `resource_id`   BIGINT UNSIGNED NOT NULL COMMENT '资源ID',
    `tag_name`      VARCHAR(50)  NOT NULL COMMENT '标签名称',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_resource_id` (`resource_id`),
    INDEX `idx_tag_name` (`tag_name`),
    CONSTRAINT `fk_tag_resource` FOREIGN KEY (`resource_id`) REFERENCES `edu_resource` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源标签关联表';
```

---

## 四、索引设计策略

### 4.1 高频查询场景分析

| 查询场景 | 条件字段 | 建议索引 |
|----------|----------|----------|
| 按学段+学科查资源 | stage_id, subject_id | `idx_stage_subject` |
| 按版本+册别查资源 | edition_id, volume_id | `idx_edition_volume` |
| 按栏目+类型查资源 | module_id, type_id | `idx_module_type` |
| 按单元查资源 | unit_id | `idx_unit` |
| 分页列表（带状态过滤） | status, is_deleted, upload_time | `idx_status_deleted` + `idx_upload_time` |
| 成套资源分组查询 | stage_id, subject_id, edition_id, volume_id, module_id | 覆盖索引 |

### 4.2 联合索引建议

针对前端 `buildApiParams()` 构建的查询条件，建议增加以下联合索引：

```sql
-- 覆盖主要筛选条件的联合索引（最左前缀原则）
ALTER TABLE `edu_resource` ADD INDEX `idx_filter_composite` (
    `stage_id`,      -- 学段
    `subject_id`,    -- 学科
    `edition_id`,    -- 版本
    `volume_id`,     -- 册别
    `module_id`,     -- 栏目
    `type_id`,       -- 类型
    `status`,        -- 状态
    `is_deleted`     -- 删除标记
);

-- 针对单元筛选的索引
ALTER TABLE `edu_resource` ADD INDEX `idx_unit_filter` (
    `unit_id`,
    `status`,
    `is_deleted`
);
```

---

## 五、数据流转关系

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│  edu_stage  │────▶│ edu_subject │────▶│ edu_edition │
│   (学段)     │     │   (学科)     │     │   (版本)     │
└─────────────┘     └─────────────┘     └──────┬──────┘
       │                                       │
       │         ┌─────────────┐              │
       └────────▶│ edu_volume  │◀─────────────┘
                 │   (册别)     │
                 └──────┬──────┘
                        │
                 ┌──────▼──────┐
                 │  edu_unit   │
                 │   (单元)     │
                 └──────┬──────┘
                        │
    ┌───────────────────┼───────────────────┐
    │                   │                   │
    ▼                   ▼                   ▼
┌─────────┐      ┌──────────┐      ┌──────────────┐
│edu_module│      │edu_resource│      │edu_resource_type│
│ (栏目)   │      │  (资源)   │      │   (资源类型)    │
└─────────┘      └──────────┘      └──────────────┘
```

---

## 六、与前端数据映射

| 前端配置/状态 | 数据库表 | 映射字段 |
|--------------|----------|----------|
| `stages` / `currentStage` | `edu_stage` | `stage_key` → `stage_name` |
| `subjectDataMap` / `currentSubject` | `edu_subject` | `subject_key` → `subject_name` |
| `subjectVersionsMap` / `selectedVersionName` | `edu_edition` | `edition_key` → `edition_name` |
| `volumeDataMap` / `currentGradeLevelName` | `edu_volume` | `volume_key` → `volume_name` |
| `unitDataMap` / `currentUnitList` | `edu_unit` | `unit_name` + `parent_id` 树形结构 |
| `columnConfig` / `activeColumn` | `edu_module` | `module_name` |
| `resourceTypes` / `activeResourceType` | `edu_resource_type` | `type_name` |
| `apiResources` / `suitePackages` | `edu_resource` | 资源记录 + OSS 链接 |

---

## 七、迁移策略（从旧表到新表）

### 7.1 旧表结构
原有 `oss_primary_chinese_resource` 为扁平化设计，字段包含：
- `stage`, `subject`, `module`, `type`, `grade_name`, `edition`, `unit_name`（均为字符串）

### 7.2 迁移步骤

```sql
-- Step 1: 创建新表（执行上述建表 SQL）

-- Step 2: 数据迁移（示例）
INSERT INTO `edu_resource` (
    `stage_id`, `subject_id`, `edition_id`, `volume_id`,
    `unit_id`, `module_id`, `type_id`, `title`,
    `original_filename`, `file_ext`, `oss_bucket`, `oss_object_key`,
    `oss_url`, `file_size_kb`, `download_count`, `view_count`,
    `status`, `uploader_id`, `upload_time`, `update_time`, `is_deleted`, `sort`, `remark`
)
SELECT
    (SELECT id FROM edu_stage WHERE stage_name = o.stage LIMIT 1),
    (SELECT id FROM edu_subject s JOIN edu_stage st ON s.stage_id = st.id WHERE st.stage_name = o.stage AND s.subject_name = o.subject LIMIT 1),
    (SELECT id FROM edu_edition e JOIN edu_subject s ON e.subject_id = s.id JOIN edu_stage st ON s.stage_id = st.id WHERE st.stage_name = o.stage AND s.subject_name = o.subject AND e.edition_name = o.edition LIMIT 1),
    (SELECT id FROM edu_volume v JOIN edu_stage st ON v.stage_id = st.id WHERE st.stage_name = o.stage AND v.volume_name = o.grade_name LIMIT 1),
    (SELECT id FROM edu_unit u WHERE u.unit_name = o.unit_name LIMIT 1),
    (SELECT id FROM edu_module m JOIN edu_stage st ON m.stage_id = st.id WHERE st.stage_name = o.stage AND m.module_name = o.module LIMIT 1),
    (SELECT id FROM edu_resource_type WHERE type_name = o.type LIMIT 1),
    o.title, o.original_filename, o.file_ext, o.oss_bucket, o.oss_object_key,
    o.oss_url, o.file_size_kb, o.download_count, o.view_count,
    o.status, o.uploader_id, o.upload_time, o.update_time, o.is_deleted, o.sort, o.remark
FROM `oss_primary_chinese_resource` o
WHERE o.is_deleted = 0;

-- Step 3: 验证数据一致性
-- Step 4: 切换应用读取新表
-- Step 5: 归档旧表（保留一段时间）
```

---

## 八、扩展建议

### 8.1 未来可扩展表

| 表名 | 用途 | 触发条件 |
|------|------|----------|
| `edu_resource_collection` | 成套资源定义 | 需要显式管理成套资源时 |
| `edu_resource_collection_item` | 成套资源明细 | 成套资源与单份资源关联 |
| `edu_download_record` | 下载记录 | 需要精细化下载统计时 |
| `edu_view_record` | 浏览记录 | 需要用户级浏览分析时 |
| `edu_favorite` | 用户收藏 | 实现收藏功能时 |
| `edu_rating` | 用户评分 | 实现资源评分时 |

### 8.2 分表分库建议

当 `edu_resource` 数据量超过 500 万条时，建议：
- **按学段分表**：`edu_resource_primary`, `edu_resource_junior`, `edu_resource_senior`
- **或按学科分表**：每个学科独立一张资源表
- **归档策略**：已下架/删除资源迁移到历史表

---

## 九、附录：完整 SQL 文件

完整建表 SQL 见同目录 `database-schema.sql` 文件。
