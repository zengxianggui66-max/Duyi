-- ============================================================
-- 25 首页专区 P2：运营置顶位
-- 依赖：23、24
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `home_panel_featured` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `panel_code`      VARCHAR(32)  NOT NULL COMMENT 'sync_prep|paper_zone|promotion',
  `tab_key`         VARCHAR(64)  NOT NULL,
  `filter_key`      VARCHAR(64)  DEFAULT NULL COMMENT '升学专题或试卷升学学段',
  `stage_key`       VARCHAR(20)  DEFAULT NULL COMMENT 'primary|junior|senior|art|dance',
  `subject_name`    VARCHAR(50)  DEFAULT NULL,
  `grade_name`      VARCHAR(30)  DEFAULT NULL,
  `resource_id`     BIGINT       NOT NULL,
  `resource_source` VARCHAR(32)  NOT NULL COMMENT 'edu_resource|oss_primary_chinese|edu_resource_suite',
  `sort`            INT          DEFAULT 0 COMMENT '越大越靠前',
  `expire_time`     DATETIME     DEFAULT NULL,
  `status`          TINYINT      DEFAULT 1,
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_panel_query` (`panel_code`, `tab_key`, `stage_key`, `status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页专区置顶推荐';

DELETE FROM `home_panel_featured` WHERE `id` BETWEEN 1 AND 20;

INSERT INTO `home_panel_featured`
  (`id`, `panel_code`, `tab_key`, `filter_key`, `stage_key`, `subject_name`, `grade_name`, `resource_id`, `resource_source`, `sort`, `status`) VALUES
(1, 'sync_prep', 'courseware', NULL, 'primary', '语文', NULL, 10001, 'oss_primary_chinese', 100, 1),
(2, 'paper_zone', 'final', NULL, 'junior', NULL, '九年级下册', 10110, 'oss_primary_chinese', 100, 1),
(5, 'paper_zone', 'midterm', NULL, 'primary', NULL, '六年级下册', 10108, 'oss_primary_chinese', 100, 1),
(6, 'sync_prep', 'courseware', NULL, 'senior', '历史', NULL, 10130, 'oss_primary_chinese', 95, 1),
(3, 'promotion', 'middle', '真题', NULL, NULL, NULL, 10200, 'oss_primary_chinese', 100, 1),
(4, 'sync_prep', 'collection', NULL, 'senior', '历史', NULL, 3003, 'edu_resource_suite', 90, 1);
