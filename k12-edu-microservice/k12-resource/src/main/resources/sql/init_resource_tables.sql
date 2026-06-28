-- =============================================
-- K12 教育平台 - 资源管理扩展表
-- =============================================

-- 收藏表
CREATE TABLE IF NOT EXISTS `collection` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `resource_id` BIGINT NOT NULL COMMENT '资源ID',
    `resource_type` VARCHAR(32) NOT NULL DEFAULT 'resource' COMMENT 'resource|primary_chinese',
    `title` VARCHAR(255) DEFAULT NULL COMMENT '资源标题快照',
    `stage` VARCHAR(32) DEFAULT NULL COMMENT '学段名称',
    `stage_key` VARCHAR(16) DEFAULT NULL COMMENT '学段键',
    `subject` VARCHAR(64) DEFAULT NULL COMMENT '学科名称',
    `subject_key` VARCHAR(32) DEFAULT NULL COMMENT '学科键',
    `module` VARCHAR(64) DEFAULT NULL COMMENT '栏目',
    `teaching_type` VARCHAR(64) DEFAULT NULL COMMENT '资源类型',
    `grade_name` VARCHAR(64) DEFAULT NULL COMMENT '年级册别',
    `file_ext` VARCHAR(16) DEFAULT NULL COMMENT '文件扩展名',
    `oss_url` VARCHAR(512) DEFAULT NULL COMMENT '文件地址',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_resource_type` (`user_id`, `resource_id`, `resource_type`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_resource_id` (`resource_id`),
    KEY `idx_user_stage_key` (`user_id`, `stage_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- 学科分类表
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学科分类表';

-- 字典表（筛选维度）
CREATE TABLE IF NOT EXISTS `dict` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type` VARCHAR(50) NOT NULL COMMENT '类型: grade_level, subject, version, resource_type, exam_type',
    `label` VARCHAR(100) NOT NULL COMMENT '显示标签',
    `value` VARCHAR(100) NOT NULL COMMENT '存储值',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典表';

-- 资源审核记录表
CREATE TABLE IF NOT EXISTS `resource_audit` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `resource_id` BIGINT NOT NULL COMMENT '资源ID',
    `status` TINYINT NOT NULL COMMENT '审核状态 0-待审核 1-通过 2-驳回',
    `reason` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
    `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
    `auditor_name` VARCHAR(50) DEFAULT NULL COMMENT '审核人名称',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_resource_id` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源审核记录表';

-- =============================================
-- 初始化数据
-- =============================================

-- 学科分类
INSERT INTO `category` (`name`, `icon`, `sort`, `status`) VALUES
('语文', 'icon-chinese', 1, 1),
('数学', 'icon-math', 2, 1),
('英语', 'icon-english', 3, 1),
('物理', 'icon-physics', 4, 1),
('化学', 'icon-chemistry', 5, 1),
('生物', 'icon-biology', 6, 1),
('历史', 'icon-history', 7, 1),
('地理', 'icon-geography', 8, 1),
('政治', 'icon-politics', 9, 1),
('科学', 'icon-science', 10, 1);

-- 字典 - 年级
INSERT INTO `dict` (`type`, `label`, `value`, `sort`, `status`) VALUES
('grade_level', '幼儿', 'preschool', 0, 1),
('grade_level', '小学', 'primary', 1, 1),
('grade_level', '初中', 'middle', 2, 1),
('grade_level', '高中', 'high', 3, 1);

-- 字典 - 资源类型
INSERT INTO `dict` (`type`, `label`, `value`, `sort`, `status`) VALUES
('resource_type', '课件', 'courseware', 1, 1),
('resource_type', '教案', 'lesson_plan', 2, 1),
('resource_type', '试卷', 'exam', 3, 1),
('resource_type', '学案', 'study_plan', 4, 1),
('resource_type', '素材', 'material', 5, 1),
('resource_type', '习题', 'exercise', 6, 1);

-- 字典 - 教材版本
INSERT INTO `dict` (`type`, `label`, `value`, `sort`, `status`) VALUES
('version', '人教版', 'pep', 1, 1),
('version', '部编版', 'moe', 2, 1),
('version', '北师大版', 'bnu', 3, 1),
('version', '苏教版', 'js', 4, 1),
('version', '沪教版', 'sh', 5, 1),
('version', '通用版', 'general', 6, 1);

-- 字典 - 备考类型
INSERT INTO `dict` (`type`, `label`, `value`, `sort`, `status`) VALUES
('exam_type', '同步练习', 'sync', 1, 1),
('exam_type', '期中考试', 'midterm', 2, 1),
('exam_type', '期末考试', 'final', 3, 1),
('exam_type', '中考', 'zhongkao', 4, 1),
('exam_type', '高考', 'gaokao', 5, 1),
('exam_type', '竞赛', 'contest', 6, 1);
