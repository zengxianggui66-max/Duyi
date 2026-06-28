-- ============================================================
-- 26 首页升学专区 P2：幼小衔接 / 对口升学
-- 依赖：23_home_panel_tab_config.sql、24_home_panel_p1.sql
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==================== 升学专区 Tab 配置 ====================
DELETE FROM `home_panel_tab_config`
WHERE `panel_code` = 'promotion'
  AND `tab_key` IN ('kindergarten_bridge', 'vocational_promo');

INSERT INTO `home_panel_tab_config`
  (`panel_code`, `tab_key`, `filter_key`, `tab_label`, `module_names`, `title_keyword`, `query_mode`, `sort`) VALUES
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

-- ==================== OSS 演示数据（10220-10227） ====================
DELETE FROM `oss_primary_chinese_resource` WHERE `id` BETWEEN 10220 AND 10227;

INSERT INTO `oss_primary_chinese_resource` (
  `id`, `stage`, `subject`, `module`, `type`, `grade_name`, `edition`, `unit_name`,
  `title`, `original_filename`, `file_ext`, `oss_bucket`, `oss_object_key`, `oss_url`,
  `file_size_kb`, `download_count`, `view_count`, `status`, `uploader_id`, `upload_time`, `is_deleted`, `sort`
) VALUES
(10220,'幼儿','拼音识字','拼音识字','练习','大班下学期','统编版(2024)','综合','幼小衔接语文拼音识字训练册（声母韵母）','yx_pinyin.doc','doc','qier-duuyi','demo/promo/yx1.doc','https://example.com/yx1.doc',420,28,14,1,1,'2026-05-16 10:00:00',0,10),
(10221,'幼儿','数学启蒙','数学启蒙','练习','大班下学期','人教版','综合','幼小衔接数学启蒙：10以内加减法练习','yx_math.doc','doc','qier-duuyi','demo/promo/yx2.doc','https://example.com/yx2.doc',380,24,12,1,1,'2026-05-16 09:30:00',0,9),
(10222,'幼儿','习惯养成','习惯养成','练习','大班下学期','统编版(2024)','综合','幼小衔接习惯养成：课堂规则与时间管理','yx_habit.doc','doc','qier-duuyi','demo/promo/yx3.doc','https://example.com/yx3.doc',360,20,10,1,1,'2026-05-16 09:00:00',0,8),
(10223,'幼儿','拼音识字','暑假衔接','练习','暑假衔接','统编版(2024)','综合','幼小衔接暑假衔接综合训练（语文+数学）','yx_bridge.doc','doc','qier-duuyi','demo/promo/yx4.doc','https://example.com/yx4.doc',400,22,11,1,1,'2026-05-15 18:00:00',0,7),
(10224,'初中','语文','专题复习','学案','九年级下册','统编版(2024)','综合','四川省对口升学政策解读与备考指引','dk_policy.doc','doc','qier-duuyi','demo/promo/dk1.doc','https://example.com/dk1.doc',520,32,16,1,1,'2026-05-16 11:00:00',0,10),
(10225,'初中','数学','学业水平','试卷','九年级下册','人教版','综合','对口升学数学真题精选（四川版）','dk_exam.doc','doc','qier-duuyi','demo/promo/dk2.doc','https://example.com/dk2.doc',600,30,15,1,1,'2026-05-16 10:30:00',0,9),
(10226,'初中','语文','专题复习','学案','九年级下册','统编版(2024)','综合','贯通培养专业对口选科指导手册','dk_major.doc','doc','qier-duuyi','demo/promo/dk3.doc','https://example.com/dk3.doc',480,26,13,1,1,'2026-05-16 10:00:00',0,8),
(10227,'初中','数学','中考模拟','试卷','九年级下册','人教版','综合','对口升学模拟试卷（数学综合卷）','dk_mock.doc','doc','qier-duuyi','demo/promo/dk4.doc','https://example.com/dk4.doc',550,28,14,1,1,'2026-05-15 17:00:00',0,7);

SET FOREIGN_KEY_CHECKS = 1;
