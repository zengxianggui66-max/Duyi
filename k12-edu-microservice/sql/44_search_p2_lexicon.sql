-- P2 搜索词典与意图规则
-- mysql -u root -p xinketang < sql/44_search_p2_lexicon.sql
USE xinketang;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_search_synonym` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `word`          VARCHAR(80)     NOT NULL COMMENT '标准词/主词',
  `synonyms`      VARCHAR(500)    NOT NULL COMMENT '同义词，逗号分隔',
  `domain`        VARCHAR(40)     DEFAULT 'global' COMMENT 'global/subject/grade/type/news',
  `canonical`     VARCHAR(80)     DEFAULT NULL COMMENT '归一化目标词',
  `status`        TINYINT         DEFAULT 1,
  `update_time`   DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word_domain` (`word`, `domain`),
  KEY `idx_domain_status` (`domain`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索同义词词典';

CREATE TABLE IF NOT EXISTS `sys_search_intent_rule` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `pattern`         VARCHAR(200)    NOT NULL COMMENT '关键词或正则片段',
  `intent_type`     VARCHAR(40)     NOT NULL COMMENT 'stage/subject/grade/type/module/channel/news',
  `target_key`      VARCHAR(80)     DEFAULT NULL COMMENT '结构化 key，如 primary/chinese/courseware',
  `target_value`    VARCHAR(200)    DEFAULT NULL COMMENT '展示值或中文名',
  `target_payload`  JSON            DEFAULT NULL COMMENT '扩展 JSON',
  `priority`        INT             DEFAULT 0,
  `status`          TINYINT         DEFAULT 1,
  `update_time`     DATETIME        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pattern_type` (`pattern`, `intent_type`),
  KEY `idx_type_status` (`intent_type`, `status`),
  KEY `idx_priority` (`priority` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索意图识别规则';

-- 查询日志扩展（P2 归一化词 + 意图 JSON；表 P1 已建为 search_query_log）
SET @db := DATABASE();
SET @exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'search_query_log' AND COLUMN_NAME = 'normalized_keyword'
);
SET @sql := IF(@exists = 0,
  'ALTER TABLE search_query_log ADD COLUMN normalized_keyword VARCHAR(255) DEFAULT NULL COMMENT ''归一化搜索词'' AFTER keyword, ADD COLUMN intent_json TEXT DEFAULT NULL COMMENT ''解析意图 JSON'' AFTER normalized_keyword',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========== 同义词种子 ==========
INSERT INTO sys_search_synonym (word, synonyms, domain, canonical) VALUES
('语文', '中文,国文,汉语', 'subject', '语文'),
('数学', '奥数,算术', 'subject', '数学'),
('英语', '英文,外语', 'subject', '英语'),
('备课', '教案,教学设计,课件,学案', 'type', '备课'),
('课件', 'ppt,PPT,幻灯片,演示文稿', 'type', '课件'),
('教案', '教学设计,导学案', 'type', '教案'),
('资讯', '新闻,通知,政策,公告', 'news', '资讯'),
('通知', '公告,教育局通知', 'news', '通知'),
('初一', '七年级,初中一年级', 'grade', '七年级'),
('初二', '八年级,初中二年级', 'grade', '八年级'),
('初三', '九年级,初中三年级', 'grade', '九年级'),
('小一', '一年级,小学一年级', 'grade', '一年级'),
('小二', '二年级,小学二年级', 'grade', '二年级'),
('小三', '三年级,小学三年级', 'grade', '三年级'),
('小四', '四年级,小学四年级', 'grade', '四年级'),
('小五', '五年级,小学五年级', 'grade', '五年级'),
('小六', '六年级,小学六年级', 'grade', '六年级'),
('高一', '高中一年级,十年级', 'grade', '高一'),
('高二', '高中二年级,十一年级', 'grade', '高二'),
('高三', '高中三年级,十二年级', 'grade', '高三'),
('小学', 'primary', 'stage', '小学'),
('初中', 'junior,中学', 'stage', '初中'),
('高中', 'senior', 'stage', '高中'),
('同步备课', '同步,备课', 'module', '同步备课')
ON DUPLICATE KEY UPDATE synonyms=VALUES(synonyms), canonical=VALUES(canonical), status=1;

-- ========== 意图规则种子 ==========
INSERT INTO sys_search_intent_rule (pattern, intent_type, target_key, target_value, priority) VALUES
('小学', 'stage', 'primary', '小学', 100),
('初中', 'stage', 'junior', '初中', 100),
('高中', 'stage', 'senior', '高中', 100),
('语文', 'subject', 'chinese', '语文', 90),
('中文', 'subject', 'chinese', '语文', 85),
('国文', 'subject', 'chinese', '语文', 85),
('数学', 'subject', 'math', '数学', 90),
('英语', 'subject', 'english', '英语', 90),
('课件', 'type', 'courseware', '课件', 80),
('ppt', 'type', 'courseware', '课件', 75),
('教案', 'type', 'lesson_plan', '教案', 80),
('教学设计', 'type', 'lesson_plan', '教案', 78),
('试卷', 'type', 'exam_paper', '试卷', 80),
('同步备课', 'module', 'sync_prep', '同步备课', 70),
('主题班会', 'channel', 'class_meeting', '主题班会', 95),
('教育资讯', 'channel', 'news', '教育资讯', 90),
('资讯', 'channel', 'news', '教育资讯', 85),
('通知', 'channel', 'news', '教育局通知', 80),
('一年级', 'grade', 'grade_1', '一年级', 88),
('七年级', 'grade', 'grade_7', '七年级', 88),
('八年级', 'grade', 'grade_8', '八年级', 88),
('九年级', 'grade', 'grade_9', '九年级', 88),
('政策', 'news', 'news', '教育资讯', 85),
('教育局', 'news', 'news', '教育局通知', 84),
('初一', 'grade', 'grade_7', '七年级', 92),
('初二', 'grade', 'grade_8', '八年级', 92),
('初三', 'grade', 'grade_9', '九年级', 92)
ON DUPLICATE KEY UPDATE target_key=VALUES(target_key), target_value=VALUES(target_value), status=1;

SELECT 'synonym_count' AS k, COUNT(*) AS v FROM sys_search_synonym WHERE status=1
UNION ALL SELECT 'intent_rule_count', COUNT(*) FROM sys_search_intent_rule WHERE status=1;
