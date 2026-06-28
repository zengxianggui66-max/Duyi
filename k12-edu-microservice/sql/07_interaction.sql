-- ============================================================
-- 新课堂教育 — 07 收藏 / 分享 / 搜索 / 评分
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

CREATE TABLE `collection` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `resource_id` BIGINT NOT NULL,
    `resource_type` VARCHAR(32) NOT NULL DEFAULT 'resource' COMMENT 'resource|primary_chinese',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_resource_type` (`user_id`, `resource_id`, `resource_type`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_resource_id` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

CREATE TABLE `share_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `resource_id` BIGINT NOT NULL,
    `resource_type` VARCHAR(32) NOT NULL DEFAULT 'resource' COMMENT 'resource|primary_chinese',
    `user_id` BIGINT DEFAULT NULL,
    `share_type` VARCHAR(20) NOT NULL COMMENT 'wechat/qq/work_wechat/link',
    `share_platform` VARCHAR(50) DEFAULT NULL,
    `ip_address` VARCHAR(50) DEFAULT NULL,
    `user_agent` VARCHAR(500) DEFAULT NULL,
    `share_url` VARCHAR(500) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_resource_id` (`resource_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享记录表';

CREATE TABLE `resource_rating` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `resource_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `rating` INT NOT NULL COMMENT '1-5',
    `comment` TEXT,
    `status` INT DEFAULT 1,
    `deleted` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_resource_user` (`resource_id`, `user_id`),
    KEY `idx_resource_id` (`resource_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源评分';

CREATE TABLE `search_hot_keyword` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `keyword` VARCHAR(100) NOT NULL,
    `search_count` INT DEFAULT 0,
    `status` INT DEFAULT 1,
    `deleted` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_keyword` (`keyword`),
    KEY `idx_search_count` (`search_count` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热搜关键词';

CREATE TABLE `search_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `keyword` VARCHAR(100) NOT NULL,
    `search_type` VARCHAR(50) DEFAULT 'resource',
    `status` INT DEFAULT 1,
    `deleted` INT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索历史';

INSERT INTO `search_hot_keyword` (`keyword`, `search_count`, `status`) VALUES
('小升初真题', 1256, 1),
('中考数学', 984, 1),
('英语语法', 856, 1),
('人教版课件', 743, 1),
('奥数竞赛', 698, 1),
('素描教程', 567, 1),
('少儿舞蹈', 534, 1),
('艺考真题', 489, 1),
('课堂实录', 456, 1),
('电子教材', 423, 1);
