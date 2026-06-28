-- ============================================================
-- 【单独执行】首页顶栏功能入口 P3 增量脚本
-- 适用：已执行过 23/24/25/26，仅需补 func-channels 元数据表
-- 可重复执行（先删后插，幂等）
-- 执行：mysql -u root -p xinketang < 27_home_func_channel_only.sql
-- ============================================================

USE `xinketang`;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `home_func_channel` (
  `id`                   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `func_key`             VARCHAR(32)  NOT NULL COMMENT '顶栏入口 key：youxiao/xiaoshengchu/...',
  `name`                 VARCHAR(50)  NOT NULL COMMENT '显示名称',
  `exam_type`            VARCHAR(64)  NOT NULL COMMENT '升学专区 examType，对应 home_panel_tab_config.tab_key',
  `default_topic`        VARCHAR(64)  NOT NULL COMMENT '默认侧栏专题 filter_key',
  `stage_key`            VARCHAR(20)  NOT NULL COMMENT 'preschool|primary|junior|senior|art|dance',
  `paper_tab`            VARCHAR(32)  NOT NULL COMMENT '试卷专区 tab_key',
  `paper_default_grade`  VARCHAR(30)  NOT NULL COMMENT '试卷专区默认年级册别',
  `scroll_target`        VARCHAR(32)  NOT NULL DEFAULT 'exam-module',
  `exam_tab_label`       VARCHAR(50)  DEFAULT NULL COMMENT '升学专区 Tab 显示名，默认同 name',
  `browse_module`        VARCHAR(64)  DEFAULT NULL COMMENT '资源浏览栏目 module',
  `browse_stage_key`     VARCHAR(20)  DEFAULT NULL COMMENT '资源浏览学段',
  `browse_default_volume` VARCHAR(30) DEFAULT NULL COMMENT '资源浏览默认册别名',
  `sort`                 SMALLINT     DEFAULT 0,
  `status`               TINYINT      DEFAULT 1,
  `create_time`          DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`          DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_func_key` (`func_key`),
  KEY `idx_exam_type` (`exam_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页顶栏功能入口配置';

DELETE FROM `home_func_channel`
WHERE `func_key` IN ('youxiao', 'xiaoshengchu', 'zhongkao', 'gaokao', 'duikou');

INSERT INTO `home_func_channel`
  (`func_key`, `name`, `exam_type`, `default_topic`, `stage_key`, `paper_tab`, `paper_default_grade`, `scroll_target`, `exam_tab_label`, `browse_module`, `browse_stage_key`, `browse_default_volume`, `sort`, `status`)
VALUES
('youxiao',       '幼小衔接', 'kindergarten_bridge', '拼音识字', 'preschool', 'opening',  '大班下学期', 'exam-module', '幼小衔接', '拼音识字',   'preschool', '大班下学期',   1, 1),
('xiaoshengchu',  '小升初',   'primary_promo',       '真题',     'primary', 'entrance', '六年级下册', 'exam-module', '小升初',   '小升初真题', 'primary', '六年级下册',   2, 1),
('zhongkao',      '中考',     'middle',              '真题',     'junior',  'entrance', '九年级下册', 'exam-module', '中考',     '中考真题',   'junior',  '九年级下册',   3, 1),
('gaokao',        '高考',     'high',                '真题',     'senior',  'entrance', '高三下册',   'exam-module', '高考',     '高考真题',   'senior',  '选择性必修二', 4, 1),
('duikou',        '对口升学', 'vocational_promo',    '政策解读', 'junior',  'entrance', '九年级下册', 'exam-module', '对口升学', '学业水平',   'junior',  '九年级下册',   5, 1);

-- ---------- 执行后验收（应返回 5 条） ----------
SELECT func_key, name, exam_type, default_topic, stage_key, paper_tab, sort
FROM home_func_channel
WHERE status = 1
ORDER BY sort;
