-- P3 搜索引擎影子接入：对比日志 + 查询路径埋点
-- mysql -u root -p xinketang --default-character-set=utf8mb4
-- source sql/48_search_p3_engine.sql
USE xinketang;
SET NAMES utf8mb4;

SET @db := DATABASE();

-- search_query_log 增加 api_path（区分 /search/all 与 /search/suggest）
SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE search_query_log ADD COLUMN api_path VARCHAR(64) DEFAULT NULL COMMENT ''API 路径 all/suggest'' AFTER sort',
    'SELECT ''skip api_path'' AS info'
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'search_query_log' AND COLUMN_NAME = 'api_path'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 影子对比日志
CREATE TABLE IF NOT EXISTS `search_engine_shadow_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `keyword` VARCHAR(255) NOT NULL COMMENT '搜索词',
  `api_path` VARCHAR(64) DEFAULT 'all' COMMENT 'all/suggest',
  `mysql_cost_ms` INT DEFAULT NULL,
  `engine_cost_ms` INT DEFAULT NULL,
  `mysql_top_doc_ids` TEXT DEFAULT NULL COMMENT 'MySQL Top10 docId JSON 数组',
  `engine_top_doc_ids` TEXT DEFAULT NULL COMMENT '引擎 Top10 docId JSON 数组',
  `top3_overlap_rate` DECIMAL(5,4) DEFAULT NULL,
  `top10_overlap_rate` DECIMAL(5,4) DEFAULT NULL,
  `mysql_total` INT DEFAULT NULL,
  `engine_total` INT DEFAULT NULL,
  `engine_error` VARCHAR(512) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_shadow_keyword_time` (`keyword`, `create_time`),
  KEY `idx_shadow_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='P3 搜索引擎影子对比日志';
