-- P1：搜索行为日志（支撑 CTR / 零结果率统计）
CREATE TABLE IF NOT EXISTS `search_query_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `client_key` VARCHAR(64) DEFAULT NULL COMMENT 'IP 或 u:userId',
  `keyword` VARCHAR(255) NOT NULL COMMENT '搜索词',
  `hit_count` INT NOT NULL DEFAULT 0 COMMENT '命中条数',
  `page` INT NOT NULL DEFAULT 1,
  `stage` VARCHAR(32) DEFAULT NULL,
  `channel` VARCHAR(32) DEFAULT NULL,
  `type` VARCHAR(32) DEFAULT NULL,
  `sort` VARCHAR(32) DEFAULT NULL,
  `blocked_code` VARCHAR(32) DEFAULT NULL COMMENT 'sensitive_word/rate_limit 等',
  `cost_ms` INT DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sql_keyword_time` (`keyword`, `create_time`),
  KEY `idx_sql_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全站搜索查询日志';
