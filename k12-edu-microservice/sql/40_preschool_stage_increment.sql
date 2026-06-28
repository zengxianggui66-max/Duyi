-- ============================================================
-- 40 幼儿阶段 / 幼小衔接独立增量脚本（可重复执行）
-- 适用：已执行过原始全量建表与种子数据，仅补本次新增的幼儿阶段资源体系
-- 执行：mysql -u root -p xinketang < 40_preschool_stage_increment.sql
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==================== 1. 学段：幼儿 ====================
INSERT INTO `edu_stage` (`code`, `name`, `icon`, `sort`, `status`)
VALUES ('preschool', '幼儿', '🌱', 6, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `icon` = VALUES(`icon`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);

SET @preschool_stage_id := (SELECT `id` FROM `edu_stage` WHERE `code` = 'preschool' LIMIT 1);

-- ==================== 2. 幼儿学科 ====================
INSERT INTO `edu_subject` (`stage_id`, `code`, `name`, `icon`, `sort`, `status`)
VALUES
(@preschool_stage_id, 'chinese',  '拼音识字', '🔤', 1, 1),
(@preschool_stage_id, 'math',     '数学启蒙', '🔢', 2, 1),
(@preschool_stage_id, 'habit',    '习惯养成', '🌟', 3, 1),
(@preschool_stage_id, 'activity', '综合活动', '🧩', 4, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `icon` = VALUES(`icon`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);

-- ==================== 3. 幼儿年级 ====================
INSERT INTO `edu_grade` (`stage_id`, `code`, `name`, `sort`, `status`)
VALUES
(@preschool_stage_id, 'k2',     '中班',     1, 1),
(@preschool_stage_id, 'k3',     '大班',     2, 1),
(@preschool_stage_id, 'bridge', '幼小衔接', 3, 1)
ON DUPLICATE KEY UPDATE
  `code` = VALUES(`code`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);

-- ==================== 4. 幼儿学科-版本关联 ====================
INSERT IGNORE INTO `edu_subject_edition` (`subject_id`, `edition_id`, `sort`)
SELECT s.id, e.id, e.sort
FROM `edu_subject` s
JOIN `edu_stage` st ON st.id = s.stage_id AND st.code = 'preschool'
JOIN `edu_edition` e ON e.code IN ('tongbian2024', 'renjiao')
WHERE s.code IN ('chinese', 'math');

INSERT IGNORE INTO `edu_subject_edition` (`subject_id`, `edition_id`, `sort`)
SELECT s.id, e.id, e.sort
FROM `edu_subject` s
JOIN `edu_stage` st ON st.id = s.stage_id AND st.code = 'preschool'
JOIN `edu_edition` e ON e.code IN ('tongbian2024', 'renjiao', 'beishida')
WHERE s.code IN ('habit', 'activity');

-- ==================== 5. 幼儿阶段栏目 ====================
INSERT INTO `edu_module`
  (`code`, `name`, `icon`, `module_category`, `applicable_stages`, `description`, `sort`, `status`)
VALUES
('preschool_pinyin',   '拼音识字',   '🔤', 'transition', '["preschool"]', '声母韵母、整体认读、识字卡片与拼音练习', 34, 1),
('preschool_math',     '数学启蒙',   '🔢', 'sync',       '["preschool"]', '数感、10以内加减与趣味数学启蒙', 35, 1),
('preschool_teach',    '教学启蒙',   '🌱', 'sync',       '["preschool"]', '幼儿入学认知、课堂启蒙与综合教学活动', 36, 1),
('preschool_habit',    '习惯养成',   '🌟', 'transition', '["preschool"]', '课堂规则、专注力、时间管理与入学适应', 37, 1),
('preschool_summer',   '暑假衔接',   '☀️', 'holiday',    '["preschool"]', '大班升小学暑假综合训练', 38, 1),
('preschool_bridge',   '幼小衔接',   '🏫', 'transition', '["preschool"]', '幼小衔接综合资料', 39, 1),
('picture_reading',    '绘本阅读',   '📖', 'reading',    '["preschool"]', '绘本阅读、亲子共读与表达训练', 40, 1),
('oral_reading',       '表达与阅读', '💬', 'reading',    '["preschool"]', '看图说话、口语表达与早期阅读', 41, 1),
('preschool_activity', '综合活动',   '🧩', 'topic',      '["preschool"]', '主题活动、游戏化学习与综合实践', 42, 1),
('home_coeducation',   '家园共育',   '👨‍👩‍👧', 'material', '["preschool"]', '家长沟通、入学准备清单与家庭指导', 43, 1)
ON DUPLICATE KEY UPDATE
  `icon` = VALUES(`icon`),
  `module_category` = VALUES(`module_category`),
  `applicable_stages` = VALUES(`applicable_stages`),
  `description` = VALUES(`description`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);

-- 栏目-幼儿学段关联：先清本脚本维护的幼儿关联，再重建，避免重复
DELETE ms
FROM `edu_module_stage` ms
JOIN `edu_stage` st ON st.id = ms.stage_id AND st.code = 'preschool'
JOIN `edu_module` m ON m.id = ms.module_id
WHERE m.code IN (
  'preschool_pinyin', 'preschool_math', 'preschool_teach', 'preschool_habit',
  'preschool_summer', 'preschool_bridge', 'picture_reading', 'oral_reading',
  'preschool_activity', 'home_coeducation'
);

INSERT INTO `edu_module_stage` (`module_id`, `stage_id`, `sort`)
SELECT m.id, @preschool_stage_id, m.sort
FROM `edu_module` m
WHERE m.code IN (
  'preschool_pinyin', 'preschool_math', 'preschool_teach', 'preschool_habit',
  'preschool_summer', 'preschool_bridge', 'picture_reading', 'oral_reading',
  'preschool_activity', 'home_coeducation'
);

-- ==================== 6. 首页升学专区：幼小衔接专题映射 ====================
DELETE FROM `home_panel_tab_config`
WHERE `panel_code` = 'promotion'
  AND `tab_key` = 'kindergarten_bridge';

INSERT INTO `home_panel_tab_config`
  (`panel_code`, `tab_key`, `filter_key`, `tab_label`, `module_names`, `title_keyword`, `query_mode`, `sort`, `status`)
VALUES
('promotion', 'kindergarten_bridge', '拼音识字', '拼音识字', '["拼音识字"]', '拼音',     'resource', 1, 1),
('promotion', 'kindergarten_bridge', '数学启蒙', '数学启蒙', '["数学启蒙"]', '数学启蒙', 'resource', 2, 1),
('promotion', 'kindergarten_bridge', '习惯养成', '习惯养成', '["习惯养成"]', '习惯',     'resource', 3, 1),
('promotion', 'kindergarten_bridge', '暑假衔接', '暑假衔接', '["暑假衔接"]', '衔接',     'resource', 4, 1);

-- ==================== 7. 首页顶栏功能入口：幼小衔接落到幼儿阶段 ====================
CREATE TABLE IF NOT EXISTS `home_func_channel` (
  `id`                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `func_key`              VARCHAR(32)  NOT NULL COMMENT '顶栏入口 key：youxiao/xiaoshengchu/...',
  `name`                  VARCHAR(50)  NOT NULL COMMENT '显示名称',
  `exam_type`             VARCHAR(64)  NOT NULL COMMENT '升学专区 examType，对应 home_panel_tab_config.tab_key',
  `default_topic`         VARCHAR(64)  NOT NULL COMMENT '默认侧栏专题 filter_key',
  `stage_key`             VARCHAR(20)  NOT NULL COMMENT 'preschool|primary|junior|senior|art|dance',
  `paper_tab`             VARCHAR(32)  NOT NULL COMMENT '试卷专区 tab_key',
  `paper_default_grade`   VARCHAR(30)  NOT NULL COMMENT '试卷专区默认年级册别',
  `scroll_target`         VARCHAR(32)  NOT NULL DEFAULT 'exam-module',
  `exam_tab_label`        VARCHAR(50)  DEFAULT NULL COMMENT '升学专区 Tab 显示名，默认同 name',
  `browse_module`         VARCHAR(64)  DEFAULT NULL COMMENT '资源浏览栏目 module',
  `browse_stage_key`      VARCHAR(20)  DEFAULT NULL COMMENT '资源浏览学段',
  `browse_default_volume` VARCHAR(30)  DEFAULT NULL COMMENT '资源浏览默认册别名',
  `sort`                  SMALLINT     DEFAULT 0,
  `status`                TINYINT      DEFAULT 1,
  `create_time`           DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`           DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_func_key` (`func_key`),
  KEY `idx_exam_type` (`exam_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页顶栏功能入口配置';

SET @sql_add_exam_tab_label := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'home_func_channel' AND COLUMN_NAME = 'exam_tab_label') = 0,
  'ALTER TABLE `home_func_channel` ADD COLUMN `exam_tab_label` VARCHAR(50) DEFAULT NULL COMMENT ''升学专区 Tab 显示名，默认同 name'' AFTER `scroll_target`',
  'SELECT 1'
);
PREPARE stmt FROM @sql_add_exam_tab_label;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_add_browse_module := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'home_func_channel' AND COLUMN_NAME = 'browse_module') = 0,
  'ALTER TABLE `home_func_channel` ADD COLUMN `browse_module` VARCHAR(64) DEFAULT NULL COMMENT ''资源浏览栏目 module'' AFTER `exam_tab_label`',
  'SELECT 1'
);
PREPARE stmt FROM @sql_add_browse_module;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_add_browse_stage_key := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'home_func_channel' AND COLUMN_NAME = 'browse_stage_key') = 0,
  'ALTER TABLE `home_func_channel` ADD COLUMN `browse_stage_key` VARCHAR(20) DEFAULT NULL COMMENT ''资源浏览学段'' AFTER `browse_module`',
  'SELECT 1'
);
PREPARE stmt FROM @sql_add_browse_stage_key;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql_add_browse_default_volume := IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'home_func_channel' AND COLUMN_NAME = 'browse_default_volume') = 0,
  'ALTER TABLE `home_func_channel` ADD COLUMN `browse_default_volume` VARCHAR(30) DEFAULT NULL COMMENT ''资源浏览默认册别名'' AFTER `browse_stage_key`',
  'SELECT 1'
);
PREPARE stmt FROM @sql_add_browse_default_volume;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO `home_func_channel`
  (`func_key`, `name`, `exam_type`, `default_topic`, `stage_key`, `paper_tab`, `paper_default_grade`,
   `scroll_target`, `exam_tab_label`, `browse_module`, `browse_stage_key`, `browse_default_volume`, `sort`, `status`)
VALUES
('youxiao', '幼小衔接', 'kindergarten_bridge', '拼音识字', 'preschool', 'opening', '大班下学期',
 'exam-module', '幼小衔接', '拼音识字', 'preschool', '大班下学期', 1, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `exam_type` = VALUES(`exam_type`),
  `default_topic` = VALUES(`default_topic`),
  `stage_key` = VALUES(`stage_key`),
  `paper_tab` = VALUES(`paper_tab`),
  `paper_default_grade` = VALUES(`paper_default_grade`),
  `scroll_target` = VALUES(`scroll_target`),
  `exam_tab_label` = VALUES(`exam_tab_label`),
  `browse_module` = VALUES(`browse_module`),
  `browse_stage_key` = VALUES(`browse_stage_key`),
  `browse_default_volume` = VALUES(`browse_default_volume`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);

-- ==================== 8. OSS 演示资源：幼小衔接四条 ====================
DELETE FROM `oss_primary_chinese_resource` WHERE `id` BETWEEN 10220 AND 10223;

INSERT INTO `oss_primary_chinese_resource` (
  `id`, `stage`, `subject`, `module`, `type`, `grade_name`, `edition`, `unit_name`,
  `title`, `original_filename`, `file_ext`, `oss_bucket`, `oss_object_key`, `oss_url`,
  `file_size_kb`, `download_count`, `view_count`, `status`, `uploader_id`, `upload_time`, `is_deleted`, `sort`
) VALUES
(10220, '幼儿', '拼音识字', '拼音识字', '练习', '大班下学期', '统编版(2024)', '综合',
 '幼小衔接语文拼音识字训练册（声母韵母）', 'yx_pinyin.doc', 'doc',
 'qier-duuyi', 'demo/promo/yx1.doc', 'https://example.com/yx1.doc',
 420, 28, 14, 1, 1, '2026-05-16 10:00:00', 0, 10),
(10221, '幼儿', '数学启蒙', '数学启蒙', '练习', '大班下学期', '人教版', '综合',
 '幼小衔接数学启蒙：10以内加减法练习', 'yx_math.doc', 'doc',
 'qier-duuyi', 'demo/promo/yx2.doc', 'https://example.com/yx2.doc',
 380, 24, 12, 1, 1, '2026-05-16 09:30:00', 0, 9),
(10222, '幼儿', '习惯养成', '习惯养成', '练习', '大班下学期', '统编版(2024)', '综合',
 '幼小衔接习惯养成：课堂规则与时间管理', 'yx_habit.doc', 'doc',
 'qier-duuyi', 'demo/promo/yx3.doc', 'https://example.com/yx3.doc',
 360, 20, 10, 1, 1, '2026-05-16 09:00:00', 0, 8),
(10223, '幼儿', '拼音识字', '暑假衔接', '练习', '暑假衔接', '统编版(2024)', '综合',
 '幼小衔接暑假衔接综合训练（语文+数学）', 'yx_bridge.doc', 'doc',
 'qier-duuyi', 'demo/promo/yx4.doc', 'https://example.com/yx4.doc',
 400, 22, 11, 1, 1, '2026-05-15 18:00:00', 0, 7);

-- ==================== 9. 兼容旧 dict：幼儿学段 ====================
-- 不同初始化脚本中的 dict 字段略有差异，这里按字段存在情况动态写入。
SET @dict_has_code := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'dict' AND COLUMN_NAME = 'code'
);

SET @sql_dict_preschool := IF(
  @dict_has_code > 0,
  'INSERT INTO `dict` (`type`, `code`, `name`, `label`, `value`, `sort`, `status`) VALUES (''grade_level'', ''preschool'', ''幼儿'', ''幼儿'', ''preschool'', 0, 1) ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `label` = VALUES(`label`), `value` = VALUES(`value`), `sort` = VALUES(`sort`), `status` = VALUES(`status`)',
  'INSERT INTO `dict` (`type`, `label`, `value`, `sort`, `status`) SELECT ''grade_level'', ''幼儿'', ''preschool'', 0, 1 WHERE NOT EXISTS (SELECT 1 FROM `dict` WHERE `type` = ''grade_level'' AND `value` = ''preschool'')'
);
PREPARE stmt FROM @sql_dict_preschool;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET FOREIGN_KEY_CHECKS = 1;

-- ==================== 执行后验收 ====================
SELECT `code`, `name`, `sort`, `status`
FROM `edu_stage`
WHERE `code` = 'preschool';

SELECT s.`code`, s.`name`, s.`sort`
FROM `edu_subject` s
JOIN `edu_stage` st ON st.id = s.stage_id
WHERE st.code = 'preschool'
ORDER BY s.sort;

SELECT `tab_key`, `filter_key`, `module_names`, `title_keyword`
FROM `home_panel_tab_config`
WHERE `panel_code` = 'promotion'
  AND `tab_key` = 'kindergarten_bridge'
ORDER BY `sort`;

SELECT `func_key`, `stage_key`, `browse_module`, `browse_stage_key`, `browse_default_volume`
FROM `home_func_channel`
WHERE `func_key` = 'youxiao';
