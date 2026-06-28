-- ============================================================
-- 新课堂教育 — 08 兼容业务表（宽表 / dict / 通用 resource）
-- 当前 P0-P2 主用 oss_primary_chinese_resource
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

-- 小学语文 OSS 宽表（与 PrimaryChineseResource 实体一致）
CREATE TABLE `oss_primary_chinese_resource` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `stage` VARCHAR(20) NOT NULL DEFAULT '小学',
    `subject` VARCHAR(30) NOT NULL DEFAULT '语文',
    `module` VARCHAR(30) NOT NULL DEFAULT '同步备课',
    `type` VARCHAR(30) NOT NULL DEFAULT '教案',
    `grade_name` VARCHAR(30) NOT NULL DEFAULT '一年级上册',
    `edition` VARCHAR(30) NOT NULL DEFAULT '人教版',
    `unit_name` VARCHAR(50) NOT NULL,
    `lesson_name` VARCHAR(128) DEFAULT NULL,
    `title` VARCHAR(200) NOT NULL,
    `original_filename` VARCHAR(255) NOT NULL,
    `file_ext` VARCHAR(20) DEFAULT NULL,
    `oss_bucket` VARCHAR(50) NOT NULL DEFAULT 'qier-duuyi',
    `oss_object_key` VARCHAR(500) NOT NULL,
    `oss_url` VARCHAR(1000) NOT NULL,
    `file_size_kb` INT NOT NULL DEFAULT 0,
    `download_count` INT NOT NULL DEFAULT 0,
    `view_count` INT NOT NULL DEFAULT 0,
    `status` INT NOT NULL DEFAULT 1 COMMENT '-1草稿 0待审 1已发布 2不通过 3下架',
    `uploader_id` BIGINT DEFAULT NULL,
    `upload_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    `sort` INT DEFAULT 0,
    `remark` VARCHAR(500) DEFAULT NULL,
    `allow_preview` TINYINT NOT NULL DEFAULT 1,
    `lesson_plan_json` TEXT DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_pcr_grade` (`grade_name`),
    KEY `idx_pcr_edition` (`edition`),
    KEY `idx_pcr_module` (`module`),
    KEY `idx_pcr_type` (`type`),
    KEY `idx_pcr_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='小学语文等资源OSS宽表';

CREATE TABLE `oss_unit_doc` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `unit_name` VARCHAR(50) NOT NULL,
    `original_filename` VARCHAR(100) NOT NULL,
    `oss_bucket` VARCHAR(50) NOT NULL DEFAULT 'qier-duuyi',
    `oss_object_key` VARCHAR(200) NOT NULL,
    `oss_url` VARCHAR(500) NOT NULL,
    `file_size_kb` INT NOT NULL,
    `upload_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `is_deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_unit_name` (`unit_name`),
    KEY `idx_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单元知识文档OSS表';

CREATE TABLE `dict` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `type` VARCHAR(50) NOT NULL,
    `code` VARCHAR(100) NOT NULL,
    `name` VARCHAR(100) DEFAULT NULL,
    `label` VARCHAR(100) DEFAULT NULL,
    `icon` VARCHAR(50) DEFAULT NULL,
    `group_key` VARCHAR(50) DEFAULT NULL,
    `group_name` VARCHAR(100) DEFAULT NULL,
    `grade_levels` VARCHAR(200) DEFAULT NULL,
    `short_name` VARCHAR(50) DEFAULT NULL,
    `sort` INT DEFAULT 0,
    `status` INT DEFAULT 1,
    `description` VARCHAR(500) DEFAULT NULL,
    `value` VARCHAR(100) DEFAULT NULL,
    `deleted` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_type_code` (`type`, `code`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典表（旧版筛选兼容）';

CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `icon` VARCHAR(255) DEFAULT NULL,
    `sort` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学科分类（简版）';

CREATE TABLE `resource_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `parent_id` BIGINT DEFAULT 0,
    `level` TINYINT DEFAULT 1,
    `sort_order` INT DEFAULT 0,
    `icon` VARCHAR(50) DEFAULT '',
    `status` TINYINT DEFAULT 1,
    `deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源分类树';

CREATE TABLE `resource` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(255) NOT NULL,
    `description` TEXT,
    `cover_url` VARCHAR(500) DEFAULT '',
    `file_url` VARCHAR(500) DEFAULT '',
    `file_size` BIGINT DEFAULT 0,
    `file_format` VARCHAR(20) DEFAULT '',
    `storage_path` VARCHAR(500) DEFAULT NULL,
    `original_name` VARCHAR(255) DEFAULT NULL,
    `content_type` VARCHAR(100) DEFAULT NULL,
    `is_previewable` TINYINT DEFAULT 0,
    `upload_status` VARCHAR(20) DEFAULT 'success',
    `storage_type` VARCHAR(20) DEFAULT 'local',
    `thumbnail_url` VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
    `duration` INT DEFAULT NULL COMMENT '音视频时长(秒)',
    `page_count` INT DEFAULT NULL COMMENT '文档页数',
    `grade_level` VARCHAR(20) NOT NULL,
    `subject` VARCHAR(30) NOT NULL,
    `grade` VARCHAR(30) DEFAULT '',
    `version` VARCHAR(30) DEFAULT '',
    `resource_type` VARCHAR(30) NOT NULL,
    `exam_type` VARCHAR(30) DEFAULT '',
    `category_id` BIGINT DEFAULT 0,
    `tags` VARCHAR(500) DEFAULT '',
    `download_count` INT DEFAULT 0,
    `view_count` INT DEFAULT 0,
    `collect_count` INT DEFAULT 0,
    `rating` DECIMAL(3,2) DEFAULT 0.00,
    `rating_count` INT DEFAULT 0,
    `score` DECIMAL(2,1) DEFAULT 5.0,
    `is_free` TINYINT DEFAULT 1,
    `author_id` BIGINT DEFAULT 0,
    `author_name` VARCHAR(50) DEFAULT '',
    `status` TINYINT DEFAULT 1,
    `deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_grade_subject` (`grade_level`, `subject`),
    KEY `idx_download_count` (`download_count` DESC),
    KEY `idx_rating` (`rating` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用教育资源表';

CREATE TABLE `resource_audit` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `resource_id` BIGINT NOT NULL,
    `status` TINYINT NOT NULL COMMENT '0待审 1通过 2驳回',
    `reason` VARCHAR(500) DEFAULT NULL,
    `auditor_id` BIGINT DEFAULT NULL,
    `auditor_name` VARCHAR(50) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_resource_id` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源审核记录';

CREATE TABLE `upload_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT DEFAULT NULL,
    `file_name` VARCHAR(255) DEFAULT NULL,
    `file_path` VARCHAR(500) DEFAULT NULL,
    `file_size` BIGINT DEFAULT 0,
    `file_format` VARCHAR(20) DEFAULT NULL,
    `content_type` VARCHAR(100) DEFAULT NULL,
    `status` VARCHAR(20) DEFAULT NULL,
    `error_message` VARCHAR(500) DEFAULT NULL,
    `resource_id` BIGINT DEFAULT NULL,
    `ip_address` VARCHAR(50) DEFAULT NULL,
    `device_info` VARCHAR(200) DEFAULT NULL,
    `deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上传记录';

CREATE TABLE `file_format_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `extension` VARCHAR(20) NOT NULL,
    `format_name` VARCHAR(50) DEFAULT NULL,
    `category` VARCHAR(30) DEFAULT NULL,
    `mime_type` VARCHAR(100) DEFAULT NULL,
    `max_size_mb` INT DEFAULT 50,
    `is_previewable` TINYINT DEFAULT 1,
    `preview_type` VARCHAR(30) DEFAULT NULL,
    `icon` VARCHAR(50) DEFAULT NULL,
    `status` INT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_extension` (`extension`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='上传格式配置';

INSERT INTO `file_format_config` (`extension`, `format_name`, `category`, `mime_type`, `max_size_mb`, `is_previewable`, `preview_type`) VALUES
('doc', 'Word', 'doc', 'application/msword', 50, 1, 'office'),
('docx', 'Word', 'doc', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 50, 1, 'office'),
('ppt', 'PPT', 'doc', 'application/vnd.ms-powerpoint', 100, 1, 'office'),
('pptx', 'PPT', 'doc', 'application/vnd.openxmlformats-officedocument.presentationml.presentation', 100, 1, 'office'),
('pdf', 'PDF', 'doc', 'application/pdf', 50, 1, 'pdf'),
('mp3', '音频', 'audio', 'audio/mpeg', 30, 1, 'audio'),
('mp4', '视频', 'video', 'video/mp4', 500, 1, 'video'),
('zip', '压缩包', 'archive', 'application/zip', 200, 0, 'download_only');

INSERT INTO `category` (`name`, `icon`, `sort`, `status`) VALUES
('语文', 'icon-chinese', 1, 1),
('数学', 'icon-math', 2, 1),
('英语', 'icon-english', 3, 1);

INSERT INTO `dict` (`type`, `code`, `name`, `label`, `value`, `sort`, `status`) VALUES
('grade_level', 'preschool', '幼儿', '幼儿', 'preschool', 0, 1),
('grade_level', 'primary', '小学', '小学', 'primary', 1, 1),
('grade_level', 'junior', '初中', '初中', 'middle', 2, 1),
('grade_level', 'senior', '高中', '高中', 'high', 3, 1),
('grade_level', 'art', '美术', '美术', 'art', 4, 1),
('grade_level', 'dance', '舞蹈', '舞蹈', 'dance', 5, 1);
