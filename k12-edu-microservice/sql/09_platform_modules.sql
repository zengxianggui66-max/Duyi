-- ============================================================
-- 新课堂教育 — 09 资讯 / 备课 / 组卷 / 会员等平台表
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

CREATE TABLE `article` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(255) NOT NULL,
    `summary` VARCHAR(500) DEFAULT '',
    `content` MEDIUMTEXT,
    `cover_url` VARCHAR(500) DEFAULT '',
    `category` VARCHAR(30) DEFAULT 'policy',
    `category_name` VARCHAR(30) DEFAULT '',
    `author` VARCHAR(50) DEFAULT '',
    `view_count` INT DEFAULT 0,
    `comment_count` INT DEFAULT 0,
    `like_count` INT DEFAULT 0,
    `tags` VARCHAR(500) DEFAULT '',
    `status` TINYINT DEFAULT 1,
    `deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资讯文章';

CREATE TABLE `lesson_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `topic` VARCHAR(255) NOT NULL,
    `grade_level` VARCHAR(20) NOT NULL,
    `subject` VARCHAR(30) NOT NULL,
    `grade` VARCHAR(30) DEFAULT '',
    `version` VARCHAR(30) DEFAULT '',
    `types` VARCHAR(100) DEFAULT '',
    `courseware_url` VARCHAR(500) DEFAULT '',
    `lesson_plan_url` VARCHAR(500) DEFAULT '',
    `study_guide_url` VARCHAR(500) DEFAULT '',
    `exercises_url` VARCHAR(500) DEFAULT '',
    `status` TINYINT DEFAULT 1,
    `deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能备课记录';

CREATE TABLE `exam_paper` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `grade_level` VARCHAR(20) NOT NULL,
    `subject` VARCHAR(30) NOT NULL,
    `difficulty` TINYINT DEFAULT 3,
    `total_score` INT DEFAULT 100,
    `duration` INT DEFAULT 90,
    `question_config` JSON,
    `questions` JSON,
    `answer_url` VARCHAR(500) DEFAULT '',
    `download_count` INT DEFAULT 0,
    `status` TINYINT DEFAULT 1,
    `deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='智能组卷记录';

CREATE TABLE `user_collection` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `resource_id` BIGINT NOT NULL,
    `type` VARCHAR(20) DEFAULT 'resource',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_resource` (`user_id`, `resource_id`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏（旧表名）';

CREATE TABLE `download_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `resource_id` BIGINT NOT NULL,
    `resource_title` VARCHAR(255) DEFAULT '',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='下载记录';

CREATE TABLE `member_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `order_no` VARCHAR(64) NOT NULL,
    `member_level` TINYINT NOT NULL,
    `amount` DECIMAL(10,2) NOT NULL,
    `duration_days` INT DEFAULT 365,
    `pay_status` TINYINT DEFAULT 0,
    `pay_time` DATETIME DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员订单';
