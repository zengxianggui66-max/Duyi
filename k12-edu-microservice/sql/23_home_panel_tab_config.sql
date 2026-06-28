-- ============================================================
-- 23 首页三大专区 Tab 查询配置（可重复执行）
-- 依赖：05_resource、03_dimension_taxonomy、99_seed_all
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `home_panel_tab_config` (
  `id`                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `panel_code`           VARCHAR(32)  NOT NULL COMMENT 'sync_prep|paper_zone|promotion',
  `tab_key`              VARCHAR(64)  NOT NULL COMMENT '顶栏Tab key 或升学 examType',
  `filter_key`           VARCHAR(64)  DEFAULT NULL COMMENT '升学侧栏专题；其他面板为空',
  `tab_label`            VARCHAR(50)  DEFAULT NULL,
  `module_names`         JSON         DEFAULT NULL COMMENT 'edu_module.name / oss.module 匹配',
  `exclude_module_names` JSON         DEFAULT NULL,
  `resource_type_names`  JSON         DEFAULT NULL COMMENT '资源类型名',
  `title_keyword`        VARCHAR(100) DEFAULT NULL COMMENT '标题关键词补充',
  `query_mode`           VARCHAR(20)  NOT NULL DEFAULT 'resource' COMMENT 'resource|suite',
  `sort`                 SMALLINT     DEFAULT 0,
  `status`               TINYINT      DEFAULT 1,
  `create_time`          DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`          DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_panel_tab_filter` (`panel_code`, `tab_key`, `filter_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页专区Tab查询映射';

-- 清理本脚本管理的配置（可重复执行）
DELETE FROM `home_panel_tab_config` WHERE `panel_code` IN ('sync_prep', 'paper_zone', 'promotion');

-- ==================== 同步备课 ====================
INSERT INTO `home_panel_tab_config`
  (`panel_code`, `tab_key`, `filter_key`, `tab_label`, `module_names`, `resource_type_names`, `query_mode`, `sort`) VALUES
('sync_prep', 'courseware',   NULL, '课件',     '["同步备课"]', '["课件"]', 'resource', 1),
('sync_prep', 'lesson_plan',  NULL, '教案',     '["同步备课"]', '["教案"]', 'resource', 2),
('sync_prep', 'exercise',     NULL, '同步练习', '["同步备课"]', '["练习","同步练习","课时练习","习题"]', 'resource', 3),
('sync_prep', 'study_guide',  NULL, '学案',     '["同步备课"]', '["学案"]', 'resource', 4),
('sync_prep', 'knowledge',    NULL, '知识点',   '["同步备课","知识点"]', '["知识点"]', 'resource', 5),
('sync_prep', 'collection',   NULL, '合集',     '["同步备课"]', NULL, 'suite', 6);

-- ==================== 试卷专区 ====================
INSERT INTO `home_panel_tab_config`
  (`panel_code`, `tab_key`, `filter_key`, `tab_label`, `module_names`, `exclude_module_names`, `query_mode`, `sort`) VALUES
('paper_zone', 'midterm',  NULL, '期中', '["期中","期中专区"]', '["同步备课"]', 'resource', 1),
('paper_zone', 'final',    NULL, '期末', '["期末","期末专区"]', '["同步备课"]', 'resource', 2),
('paper_zone', 'opening',  NULL, '开学考', '["开学专区"]', '["同步备课"]', 'resource', 3),
('paper_zone', 'monthly',  NULL, '月考', '["月考"]', '["同步备课"]', 'resource', 4),
('paper_zone', 'winter',   NULL, '寒假', '["寒假","寒假专区"]', '["同步备课"]', 'resource', 5),
('paper_zone', 'summer',   NULL, '暑假', '["暑假","暑假专区"]', '["同步备课"]', 'resource', 6),
('paper_zone', 'entrance', NULL, '升学', '["小升初真题","小升初模拟","小升初专区","学业水平"]', '["同步备课"]', 'resource', 7);

-- ==================== 升学专区（tab_key=examType, filter_key=侧栏专题） ====================
-- 小升初
INSERT INTO `home_panel_tab_config`
  (`panel_code`, `tab_key`, `filter_key`, `tab_label`, `module_names`, `title_keyword`, `query_mode`, `sort`) VALUES
('promotion', 'primary_promo', '真题',       '真题',       '["小升初真题"]', NULL, 'resource', 1),
('promotion', 'primary_promo', '模拟试卷',   '模拟试卷',   '["小升初模拟"]', NULL, 'resource', 2),
('promotion', 'primary_promo', '名校招生',   '名校招生',   '["小升初专区"]', '招生', 'resource', 3),
('promotion', 'primary_promo', '专项训练',   '专项训练',   '["专题复习"]', NULL, 'resource', 4),
('promotion', 'primary_promo', '真题汇编',   '真题汇编',   '["真题汇编"]', NULL, 'resource', 5),
('promotion', 'primary_promo', '重点校卷',   '重点校卷',   '["小升初真题","小升初模拟"]', '重点校', 'resource', 6),
('promotion', 'primary_promo', '分班考试',   '分班考试',   '["小升初模拟","小升初专区"]', '分班', 'resource', 7),
('promotion', 'primary_promo', '暑假衔接',   '暑假衔接',   '["暑假专区","暑假"]', '衔接', 'resource', 8),
-- 中考
('promotion', 'middle', '一模',       '一模',       '["中考模拟"]', '一模', 'resource', 1),
('promotion', 'middle', '二模',       '二模',       '["中考模拟"]', '二模', 'resource', 2),
('promotion', 'middle', '真题',       '真题',       '["中考真题"]', NULL, 'resource', 3),
('promotion', 'middle', '一轮复习',   '一轮复习',   '["一轮复习"]', NULL, 'resource', 4),
('promotion', 'middle', '二轮专题',   '二轮专题',   '["二轮专题"]', NULL, 'resource', 5),
('promotion', 'middle', '三轮冲刺',   '三轮冲刺',   '["三轮冲刺"]', NULL, 'resource', 6),
('promotion', 'middle', '模拟试卷',   '模拟试卷',   '["中考模拟"]', NULL, 'resource', 7),
('promotion', 'middle', '真题汇编',   '真题汇编',   '["真题汇编"]', NULL, 'resource', 8),
('promotion', 'middle', '中考作文',   '中考作文',   '["作文","作文专区"]', '中考', 'resource', 9),
-- 高考
('promotion', 'high', '一模',       '一模',       '["高考模拟"]', '一模', 'resource', 1),
('promotion', 'high', '二模',       '二模',       '["高考模拟"]', '二模', 'resource', 2),
('promotion', 'high', '三模',       '三模',       '["高考模拟"]', '三模', 'resource', 3),
('promotion', 'high', '真题',       '真题',       '["高考真题"]', NULL, 'resource', 4),
('promotion', 'high', '一轮复习',   '一轮复习',   '["一轮复习"]', NULL, 'resource', 5),
('promotion', 'high', '二轮专题',   '二轮专题',   '["二轮专题"]', NULL, 'resource', 6),
('promotion', 'high', '三轮冲刺',   '三轮冲刺',   '["三轮冲刺"]', NULL, 'resource', 7),
('promotion', 'high', '模拟试卷',   '模拟试卷',   '["高考模拟"]', NULL, 'resource', 8),
('promotion', 'high', '真题汇编',   '真题汇编',   '["真题汇编"]', NULL, 'resource', 9),
('promotion', 'high', '高考作文',   '高考作文',   '["作文","作文专区"]', '高考', 'resource', 10),
-- 幼小衔接
('promotion', 'kindergarten_bridge', '拼音识字', '拼音识字', '["拼音识字"]', '拼音', 'resource', 1),
('promotion', 'kindergarten_bridge', '数学启蒙', '数学启蒙', '["数学启蒙"]', '数学启蒙', 'resource', 2),
('promotion', 'kindergarten_bridge', '习惯养成', '习惯养成', '["习惯养成"]', '习惯', 'resource', 3),
('promotion', 'kindergarten_bridge', '暑假衔接', '暑假衔接', '["暑假衔接"]', '衔接', 'resource', 4),
-- 对口升学
('promotion', 'vocational_promo', '政策解读', '政策解读', '["专题复习"]', '对口', 'resource', 1),
('promotion', 'vocational_promo', '真题资料', '真题资料', '["学业水平"]', NULL, 'resource', 2),
('promotion', 'vocational_promo', '专业对口', '专业对口', '["专题复习"]', '专业', 'resource', 3),
('promotion', 'vocational_promo', '模拟试卷', '模拟试卷', '["中考模拟","学业水平"]', '对口', 'resource', 4);
