-- ============================================================
-- K12 教育平台（新课堂）数据库建表脚本
-- 数据库：MySQL 8.0+
-- 字符集：utf8mb4
-- 创建日期：2026-05-12
-- ============================================================

-- 使用数据库（请根据实际情况修改）
-- USE `xinketang`;

-- ============================================================
-- 1. 学段表
-- ============================================================
DROP TABLE IF EXISTS `edu_stage`;
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

-- 初始数据
INSERT INTO `edu_stage` (`stage_key`, `stage_name`, `sort_order`) VALUES
('primary', '小学', 1),
('junior', '初中', 2),
('senior', '高中', 3),
('art', '美术', 4),
('dance', '舞蹈', 5);

-- ============================================================
-- 2. 学科表
-- ============================================================
DROP TABLE IF EXISTS `edu_subject`;
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

-- 初始数据：小学
INSERT INTO `edu_subject` (`stage_id`, `subject_key`, `subject_name`, `is_new`, `sort_order`) VALUES
(1, 'chinese', '语文', 0, 1),
(1, 'math', '数学', 0, 2),
(1, 'english', '英语', 1, 3);

-- 初始数据：初中
INSERT INTO `edu_subject` (`stage_id`, `subject_key`, `subject_name`, `is_new`, `sort_order`) VALUES
(2, 'chinese', '语文', 0, 1),
(2, 'math', '数学', 0, 2),
(2, 'english', '英语', 0, 3),
(2, 'physics', '物理', 0, 4),
(2, 'chemistry', '化学', 0, 5);

-- 初始数据：高中
INSERT INTO `edu_subject` (`stage_id`, `subject_key`, `subject_name`, `is_new`, `sort_order`) VALUES
(3, 'chinese', '语文', 0, 1),
(3, 'math', '数学', 0, 2),
(3, 'english', '英语', 0, 3),
(3, 'physics', '物理', 0, 4),
(3, 'chemistry', '化学', 0, 5);

-- 初始数据：美术
INSERT INTO `edu_subject` (`stage_id`, `subject_key`, `subject_name`, `is_new`, `sort_order`) VALUES
(4, 'art', '美术', 0, 1);

-- 初始数据：舞蹈
INSERT INTO `edu_subject` (`stage_id`, `subject_key`, `subject_name`, `is_new`, `sort_order`) VALUES
(5, 'dance', '舞蹈', 0, 1);

-- ============================================================
-- 3. 教材版本表
-- ============================================================
DROP TABLE IF EXISTS `edu_edition`;
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

-- 初始数据：小学语文版本
INSERT INTO `edu_edition` (`subject_id`, `edition_key`, `edition_name`, `publisher`, `is_new`, `sort_order`) VALUES
(1, 'renjiao', '人教版', '人民教育出版社', 1, 1),
(1, 'tongbian', '统编版(2024)', '人民教育出版社', 1, 2),
(1, 'beishida', '北师大版', '北京师范大学出版社', 0, 3);

-- 初始数据：小学数学版本
INSERT INTO `edu_edition` (`subject_id`, `edition_key`, `edition_name`, `publisher`, `is_new`, `sort_order`) VALUES
(2, 'renjiao', '人教版', '人民教育出版社', 1, 1),
(2, 'beishida', '北师大版', '北京师范大学出版社', 0, 2),
(2, 'sujiao', '苏教版', '江苏教育出版社', 0, 3);

-- 初始数据：小学英语版本
INSERT INTO `edu_edition` (`subject_id`, `edition_key`, `edition_name`, `publisher`, `is_new`, `sort_order`) VALUES
(3, 'renjiao', '人教版', '人民教育出版社', 1, 1);

-- ============================================================
-- 4. 册别表
-- ============================================================
DROP TABLE IF EXISTS `edu_volume`;
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

-- 初始数据：小学册别
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

-- 初始数据：初中册别
INSERT INTO `edu_volume` (`stage_id`, `volume_key`, `volume_name`, `grade_level`, `semester`, `is_new`, `sort_order`) VALUES
(2, 'j7s1', '七年级上册', '七年级', '上册', 1, 1),
(2, 'j7s2', '七年级下册', '七年级', '下册', 1, 2),
(2, 'j8s1', '八年级上册', '八年级', '上册', 1, 3),
(2, 'j8s2', '八年级下册', '八年级', '下册', 1, 4),
(2, 'j9s1', '九年级上册', '九年级', '上册', 1, 5),
(2, 'j9s2', '九年级下册', '九年级', '下册', 1, 6);

-- 初始数据：高中册别
INSERT INTO `edu_volume` (`stage_id`, `volume_key`, `volume_name`, `grade_level`, `semester`, `is_new`, `sort_order`) VALUES
(3, 's10s1', '必修一', '高一', '上册', 1, 1),
(3, 's10s2', '必修二', '高一', '下册', 1, 2),
(3, 's11s1', '选择性必修一', '高二', '上册', 0, 3),
(3, 's11s2', '选择性必修二', '高二', '下册', 0, 4);

-- 初始数据：美术册别
INSERT INTO `edu_volume` (`stage_id`, `volume_key`, `volume_name`, `grade_level`, `semester`, `is_new`, `sort_order`) VALUES
(4, 'a1s1', '一年级上册', '一年级', '上册', 1, 1),
(4, 'a1s2', '一年级下册', '一年级', '下册', 1, 2),
(4, 'a2s1', '二年级上册', '二年级', '上册', 1, 3),
(4, 'a2s2', '二年级下册', '二年级', '下册', 1, 4),
(4, 'a3s1', '三年级上册', '三年级', '上册', 0, 5),
(4, 'a3s2', '三年级下册', '三年级', '下册', 0, 6);

-- 初始数据：舞蹈册别
INSERT INTO `edu_volume` (`stage_id`, `volume_key`, `volume_name`, `grade_level`, `semester`, `is_new`, `sort_order`) VALUES
(5, 'd1s1', '一年级上册', '一年级', '上册', 1, 1),
(5, 'd1s2', '一年级下册', '一年级', '下册', 1, 2),
(5, 'd2s1', '二年级上册', '二年级', '上册', 1, 3),
(5, 'd2s2', '二年级下册', '二年级', '下册', 1, 4);

-- ============================================================
-- 5. 单元目录表
-- ============================================================
DROP TABLE IF EXISTS `edu_unit`;
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

-- 初始数据：小学语文一年级下册 人教版 单元
INSERT INTO `edu_unit` (`subject_id`, `edition_id`, `volume_id`, `unit_name`, `unit_order`, `parent_id`) VALUES
(1, 1, 2, '第一单元·识字', 1, 0),
(1, 1, 2, '第二单元·课文', 2, 0),
(1, 1, 2, '第三单元·课文', 3, 0),
(1, 1, 2, '第四单元·课文', 4, 0),
(1, 1, 2, '第五单元·识字', 5, 0),
(1, 1, 2, '第六单元·课文', 6, 0),
(1, 1, 2, '第七单元·课文', 7, 0),
(1, 1, 2, '第八单元·课文', 8, 0);

-- 子单元（课文）- 第一单元
INSERT INTO `edu_unit` (`subject_id`, `edition_id`, `volume_id`, `unit_name`, `unit_order`, `parent_id`) VALUES
(1, 1, 2, '春夏秋冬', 1, 1),
(1, 1, 2, '姓氏歌', 2, 1),
(1, 1, 2, '小青蛙', 3, 1),
(1, 1, 2, '猜字谜', 4, 1);

-- 子单元（课文）- 第二单元
INSERT INTO `edu_unit` (`subject_id`, `edition_id`, `volume_id`, `unit_name`, `unit_order`, `parent_id`) VALUES
(1, 1, 2, '吃水不忘挖井人', 1, 2),
(1, 1, 2, '我多想去看看', 2, 2),
(1, 1, 2, '一个接一个', 3, 2),
(1, 1, 2, '四个太阳', 4, 2);

-- ============================================================
-- 6. 栏目配置表
-- ============================================================
DROP TABLE IF EXISTS `edu_module`;
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

-- 高中栏目
INSERT INTO `edu_module` (`stage_id`, `module_name`, `module_key`, `sort_order`) VALUES
(3, '同步备课', 'sync_prep', 1),
(3, '开学专区', 'opening', 2),
(3, '月考', 'monthly', 3),
(3, '期中', 'midterm', 4),
(3, '期末', 'final', 5),
(3, '学业水平', 'academic', 6),
(3, '一轮复习', 'review_1', 7),
(3, '二轮专题', 'review_2', 8),
(3, '三轮冲刺', 'review_3', 9),
(3, '高考模拟', 'gk_sim', 10),
(3, '高考真题', 'gk_real', 11),
(3, '真题汇编', 'real_collection', 12),
(3, '寒假', 'winter', 13),
(3, '暑假', 'summer', 14),
(3, '作文', 'composition', 15),
(3, '阅读', 'reading', 16),
(3, '竞赛', 'competition', 17),
(3, '纯素材', 'material', 18);

-- 美术栏目
INSERT INTO `edu_module` (`stage_id`, `module_name`, `module_key`, `sort_order`) VALUES
(4, '同步备课', 'sync_prep', 1),
(4, '开学专区', 'opening', 2),
(4, '月考', 'monthly', 3),
(4, '期中', 'midterm', 4),
(4, '期末', 'final', 5),
(4, '专题复习', 'topic_review', 6),
(4, '暑假', 'summer', 7),
(4, '寒假', 'winter', 8),
(4, '竞赛', 'competition', 9);

-- 舞蹈栏目
INSERT INTO `edu_module` (`stage_id`, `module_name`, `module_key`, `sort_order`) VALUES
(5, '同步备课', 'sync_prep', 1),
(5, '开学专区', 'opening', 2),
(5, '月考', 'monthly', 3),
(5, '期中', 'midterm', 4),
(5, '期末', 'final', 5),
(5, '专题复习', 'topic_review', 6),
(5, '寒假', 'winter', 7),
(5, '暑假', 'summer', 8),
(5, '竞赛', 'competition', 9);

-- ============================================================
-- 7. 资源类型表
-- ============================================================
DROP TABLE IF EXISTS `edu_resource_type`;
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

-- ============================================================
-- 8. 资源主表（核心表）
-- ============================================================
DROP TABLE IF EXISTS `edu_resource`;
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

    -- 基础索引
    INDEX `idx_stage_subject` (`stage_id`, `subject_id`),
    INDEX `idx_edition_volume` (`edition_id`, `volume_id`),
    INDEX `idx_module_type` (`module_id`, `type_id`),
    INDEX `idx_unit` (`unit_id`),
    INDEX `idx_status_deleted` (`status`, `is_deleted`),
    INDEX `idx_upload_time` (`upload_time`),
    INDEX `idx_sort` (`sort`),

    -- 联合索引（覆盖主要筛选条件）
    INDEX `idx_filter_composite` (
        `stage_id`, `subject_id`, `edition_id`, `volume_id`,
        `module_id`, `type_id`, `status`, `is_deleted`
    ),

    -- 外键约束
    CONSTRAINT `fk_resource_stage` FOREIGN KEY (`stage_id`) REFERENCES `edu_stage` (`id`),
    CONSTRAINT `fk_resource_subject` FOREIGN KEY (`subject_id`) REFERENCES `edu_subject` (`id`),
    CONSTRAINT `fk_resource_edition` FOREIGN KEY (`edition_id`) REFERENCES `edu_edition` (`id`),
    CONSTRAINT `fk_resource_volume` FOREIGN KEY (`volume_id`) REFERENCES `edu_volume` (`id`),
    CONSTRAINT `fk_resource_unit` FOREIGN KEY (`unit_id`) REFERENCES `edu_unit` (`id`),
    CONSTRAINT `fk_resource_module` FOREIGN KEY (`module_id`) REFERENCES `edu_module` (`id`),
    CONSTRAINT `fk_resource_type` FOREIGN KEY (`type_id`) REFERENCES `edu_resource_type` (`id`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教育资源资源主表';

-- ============================================================
-- 9. 资源标签关联表（可选扩展）
-- ============================================================
DROP TABLE IF EXISTS `edu_resource_tag`;
CREATE TABLE `edu_resource_tag` (
    `id`            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `resource_id`   BIGINT UNSIGNED NOT NULL COMMENT '资源ID',
    `tag_name`      VARCHAR(50)  NOT NULL COMMENT '标签名称',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_resource_id` (`resource_id`),
    INDEX `idx_tag_name` (`tag_name`),
    CONSTRAINT `fk_tag_resource` FOREIGN KEY (`resource_id`) REFERENCES `edu_resource` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源标签关联表';

-- ============================================================
-- 10. 成套资源定义表（可选扩展）
-- ============================================================
DROP TABLE IF EXISTS `edu_resource_suite`;
CREATE TABLE `edu_resource_suite` (
    `id`            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '成套资源ID',
    `stage_id`      BIGINT UNSIGNED NOT NULL COMMENT '学段ID',
    `subject_id`    BIGINT UNSIGNED NOT NULL COMMENT '学科ID',
    `edition_id`    BIGINT UNSIGNED NOT NULL COMMENT '版本ID',
    `volume_id`     BIGINT UNSIGNED NOT NULL COMMENT '册别ID',
    `module_id`     BIGINT UNSIGNED NOT NULL COMMENT '栏目ID',
    `suite_name`    VARCHAR(100) NOT NULL COMMENT '成套资源名称',
    `description`   VARCHAR(500) DEFAULT '' COMMENT '描述',
    `cover_url`     VARCHAR(500) DEFAULT '' COMMENT '封面图片URL',
    `file_count`    INT          DEFAULT 0 NOT NULL COMMENT '包含文件数量',
    `status`        TINYINT      DEFAULT 1 NOT NULL COMMENT '状态：0=禁用 1=启用',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL,
    INDEX `idx_stage_subject` (`stage_id`, `subject_id`),
    INDEX `idx_edition_volume` (`edition_id`, `volume_id`),
    INDEX `idx_module` (`module_id`),
    INDEX `idx_status` (`status`),
    CONSTRAINT `fk_suite_stage` FOREIGN KEY (`stage_id`) REFERENCES `edu_stage` (`id`),
    CONSTRAINT `fk_suite_subject` FOREIGN KEY (`subject_id`) REFERENCES `edu_subject` (`id`),
    CONSTRAINT `fk_suite_edition` FOREIGN KEY (`edition_id`) REFERENCES `edu_edition` (`id`),
    CONSTRAINT `fk_suite_volume` FOREIGN KEY (`volume_id`) REFERENCES `edu_volume` (`id`),
    CONSTRAINT `fk_suite_module` FOREIGN KEY (`module_id`) REFERENCES `edu_module` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成套资源定义表';

-- ============================================================
-- 11. 成套资源明细表（可选扩展）
-- ============================================================
DROP TABLE IF EXISTS `edu_resource_suite_item`;
CREATE TABLE `edu_resource_suite_item` (
    `id`            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '明细ID',
    `suite_id`      BIGINT UNSIGNED NOT NULL COMMENT '成套资源ID',
    `resource_id`   BIGINT UNSIGNED NOT NULL COMMENT '单份资源ID',
    `sort_order`    INT          DEFAULT 0 NOT NULL COMMENT '排序',
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE KEY `uk_suite_resource` (`suite_id`, `resource_id`),
    INDEX `idx_suite_id` (`suite_id`),
    INDEX `idx_resource_id` (`resource_id`),
    CONSTRAINT `fk_suite_item_suite` FOREIGN KEY (`suite_id`) REFERENCES `edu_resource_suite` (`id`),
    CONSTRAINT `fk_suite_item_resource` FOREIGN KEY (`resource_id`) REFERENCES `edu_resource` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成套资源明细表';

-- ============================================================
-- 完成
-- ============================================================
SELECT 'K12 教育平台数据库初始化完成' AS result;
