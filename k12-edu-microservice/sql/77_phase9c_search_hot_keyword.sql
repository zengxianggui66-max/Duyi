-- Phase 9-C: search_hot_keyword boost_score for admin weighting
USE `xinketang`;
SET NAMES utf8mb4;

SET @db := DATABASE();
SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'search_hot_keyword' AND COLUMN_NAME = 'boost_score'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE `search_hot_keyword` ADD COLUMN `boost_score` INT NOT NULL DEFAULT 0 COMMENT ''运营人工加权（参与排序）'' AFTER `search_count`',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SELECT '=== search_hot_keyword boost_score ===' AS section;
SHOW COLUMNS FROM search_hot_keyword LIKE 'boost_score';
