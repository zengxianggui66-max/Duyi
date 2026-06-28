-- ============================================================
-- 26 首页专区演示数据补丁（增量，可重复执行）
-- 依赖：已执行 23、24、25
-- 用途：补齐默认筛选下的空列表（小学六年级期中、高中历史同步备课、升学专题等）
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==================== 1. 试卷专区：小学六年级下册期中（默认 Tab） ====================
DELETE FROM `oss_primary_chinese_resource` WHERE `id` IN (10108, 10109);

INSERT INTO `oss_primary_chinese_resource` (
  `id`, `stage`, `subject`, `module`, `type`, `grade_name`, `edition`, `unit_name`,
  `title`, `original_filename`, `file_ext`, `oss_bucket`, `oss_object_key`, `oss_url`,
  `file_size_kb`, `download_count`, `view_count`, `status`, `uploader_id`, `upload_time`, `is_deleted`, `sort`
) VALUES
(10108,'小学','数学','期中','试卷','六年级下册','人教版','综合','2025-2026学年六年级下册数学期中试卷（含答案）','期中数学.doc','doc','qier-duuyi','demo/paper/p9.doc','https://example.com/p9.doc',480,11,6,1,1,'2026-05-18 08:00:00',0,11),
(10109,'小学','语文','期中','试卷','六年级下册','统编版(2024)','综合','2025-2026学年六年级下册语文期中试卷（含答案）','期中语文6.doc','doc','qier-duuyi','demo/paper/p10.doc','https://example.com/p10.doc',480,10,5,1,1,'2026-05-18 07:30:00',0,10);

-- ==================== 2. 同步备课：高中历史 OSS 演示 ====================
DELETE FROM `oss_primary_chinese_resource` WHERE `id` BETWEEN 10130 AND 10139;

INSERT INTO `oss_primary_chinese_resource` (
  `id`, `stage`, `subject`, `module`, `type`, `grade_name`, `edition`, `unit_name`,
  `title`, `original_filename`, `file_ext`, `oss_bucket`, `oss_object_key`, `oss_url`,
  `file_size_kb`, `download_count`, `view_count`, `status`, `uploader_id`, `upload_time`, `is_deleted`, `sort`
) VALUES
(10130,'高中','历史','同步备课','课件','高二下册','统编版(2024)','第一单元','中外历史纲要下册第1课【课件】','his1.pptx','pptx','qier-duuyi','demo/sync/his1.pptx','https://example.com/his1.pptx',2048,30,15,1,1,'2026-05-18 09:00:00',0,10),
(10131,'高中','历史','同步备课','教案','高二下册','统编版(2024)','第一单元','中外历史纲要下册第1课【教案】','his1.doc','doc','qier-duuyi','demo/sync/his1.doc','https://example.com/his1.doc',1309,22,10,1,1,'2026-05-18 08:30:00',0,9),
(10132,'高中','历史','同步备课','练习','高二下册','统编版(2024)','第二单元','近代史专题【课时练习】','his2.doc','doc','qier-duuyi','demo/sync/his2.doc','https://example.com/his2.doc',512,18,8,1,1,'2026-05-17 17:00:00',0,8),
(10133,'高中','历史','同步备课','学案','高二下册','统编版(2024)','第三单元','世界史通史【学案】','his3.doc','doc','qier-duuyi','demo/sync/his3.doc','https://example.com/his3.doc',600,16,7,1,1,'2026-05-17 16:30:00',0,7),
(10134,'高中','历史','知识点','知识点','高二下册','统编版(2024)','综合','辛亥革命知识点梳理','his_kp.doc','doc','qier-duuyi','demo/sync/his_kp.doc','https://example.com/his_kp.doc',400,20,9,1,1,'2026-05-17 15:00:00',0,6);

-- ==================== 3. 升学专区：修正旧标题 + 补充专题数据 ====================
UPDATE `oss_primary_chinese_resource` SET
  `title` = '2026武汉初三中考数学真题及答案',
  `module` = '中考真题'
WHERE `id` = 10200;

UPDATE `oss_primary_chinese_resource` SET
  `title` = '2026江门初三中考语文真题及答案',
  `module` = '中考真题'
WHERE `id` = 10201;

DELETE FROM `oss_primary_chinese_resource` WHERE `id` BETWEEN 10209 AND 10219;

INSERT INTO `oss_primary_chinese_resource` (
  `id`, `stage`, `subject`, `module`, `type`, `grade_name`, `edition`, `unit_name`,
  `title`, `original_filename`, `file_ext`, `oss_bucket`, `oss_object_key`, `oss_url`,
  `file_size_kb`, `download_count`, `view_count`, `status`, `uploader_id`, `upload_time`, `is_deleted`, `sort`
) VALUES
(10209,'初中','数学','中考模拟','试卷','九年级下册','人教版','综合','2026武汉初三二模试卷及答案（九科全）','zk_2m.doc','doc','qier-duuyi','demo/promo/z6.doc','https://example.com/z6.doc',800,45,18,1,1,'2026-05-16 10:15:00',0,11),
(10210,'初中','物理','二轮专题','试卷','九年级下册','人教版','综合','2026中考物理二轮专题突破讲义','round2.doc','doc','qier-duuyi','demo/promo/z7.doc','https://example.com/z7.doc',650,28,12,1,1,'2026-05-15 16:30:00',0,5),
(10211,'初中','化学','三轮冲刺','试卷','九年级下册','人教版','综合','2026中考化学三轮冲刺模拟卷','round3.doc','doc','qier-duuyi','demo/promo/z8.doc','https://example.com/z8.doc',620,26,11,1,1,'2026-05-15 16:00:00',0,4),
(10212,'初中','数学','真题汇编','试卷','九年级下册','人教版','综合','2019-2026中考数学真题汇编','zk_comp.doc','doc','qier-duuyi','demo/promo/z9.doc','https://example.com/z9.doc',900,42,20,1,1,'2026-05-15 15:30:00',0,3);

-- ==================== 4. 置顶位补丁 ====================
DELETE FROM `home_panel_featured` WHERE `id` IN (5, 6);

INSERT INTO `home_panel_featured`
  (`id`, `panel_code`, `tab_key`, `filter_key`, `stage_key`, `subject_name`, `grade_name`, `resource_id`, `resource_source`, `sort`, `status`) VALUES
(5, 'paper_zone', 'midterm', NULL, 'primary', NULL, '六年级下册', 10108, 'oss_primary_chinese', 100, 1),
(6, 'sync_prep', 'courseware', NULL, 'senior', '历史', NULL, 10130, 'oss_primary_chinese', 95, 1);

SET FOREIGN_KEY_CHECKS = 1;

SELECT '26_home_panel_demo_patch.sql 执行完成' AS message,
  (SELECT COUNT(*) FROM oss_primary_chinese_resource WHERE id IN (10108, 10109)) AS paper_midterm_rows,
  (SELECT COUNT(*) FROM oss_primary_chinese_resource WHERE id BETWEEN 10130 AND 10134) AS sync_history_rows,
  (SELECT COUNT(*) FROM oss_primary_chinese_resource WHERE id BETWEEN 10209 AND 10212) AS promotion_extra_rows,
  (SELECT COUNT(*) FROM home_panel_featured WHERE id IN (5, 6)) AS featured_patch_rows;
