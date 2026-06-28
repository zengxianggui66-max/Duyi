-- P2/P3：搜索点击日志（CTR / 直跳准确率）
CREATE TABLE IF NOT EXISTS `search_click_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL,
  `keyword` VARCHAR(255) NOT NULL COMMENT '搜索词',
  `doc_id` VARCHAR(64) DEFAULT NULL COMMENT '点击 docId',
  `resource_id` BIGINT DEFAULT NULL,
  `resource_type` VARCHAR(32) DEFAULT NULL,
  `click_type` VARCHAR(32) NOT NULL DEFAULT 'result' COMMENT 'result/redirect/recommend',
  `position` INT DEFAULT NULL COMMENT '结果列表位置，从 1 起',
  `detail_route` VARCHAR(500) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_scl_keyword_time` (`keyword`, `create_time`),
  KEY `idx_scl_doc_time` (`doc_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全站搜索点击日志';
