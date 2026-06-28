-- ============================================================
-- 89: Phase 3G - 旧接口调用量统计
-- 目标：
--   1) 统计旧读接口调用量（日维度）
--   2) 为旧接口下线提供量化门禁
-- 说明：
--   表结构由 LegacyApiUsageInterceptor / LegacyApiUsageService 写入。
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `legacy_api_usage_stat` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `api_path` VARCHAR(128) NOT NULL COMMENT '旧接口路径',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `hit_count` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '调用次数',
  `last_hit_time` DATETIME NULL COMMENT '最后调用时间',
  `sample_query` VARCHAR(255) NULL COMMENT '最近一次 query 样例（截断）',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_path_date` (`api_path`, `stat_date`),
  KEY `idx_stat_date` (`stat_date`),
  KEY `idx_path` (`api_path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='旧接口调用量统计（日维度）';

SELECT 'legacy_api_usage_stat ready' AS info;
