-- ============================================================
-- 24 首页专区 P1：分学段升学卷配置 + 演示数据
-- 依赖：23_home_panel_tab_config.sql
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 试卷专区「升学」按学段拆分
DELETE FROM `home_panel_tab_config`
WHERE `panel_code` = 'paper_zone' AND `tab_key` = 'entrance';

INSERT INTO `home_panel_tab_config`
  (`panel_code`, `tab_key`, `filter_key`, `tab_label`, `module_names`, `exclude_module_names`, `query_mode`, `sort`) VALUES
('paper_zone', 'entrance', 'primary', '升学', '["小升初真题","小升初模拟","小升初专区"]', '["同步备课"]', 'resource', 7),
('paper_zone', 'entrance', 'junior',  '升学', '["学业水平","中考模拟","中考真题"]', '["同步备课"]', 'resource', 7),
('paper_zone', 'entrance', 'senior',  '升学', '["学业水平","高考模拟","高考真题"]', '["同步备课"]', 'resource', 7);

-- ==================== OSS 宽表：试卷专区演示（10100-10159） ====================
DELETE FROM `oss_primary_chinese_resource` WHERE `id` BETWEEN 10100 AND 10159;

INSERT INTO `oss_primary_chinese_resource` (
  `id`, `stage`, `subject`, `module`, `type`, `grade_name`, `edition`, `unit_name`,
  `title`, `original_filename`, `file_ext`, `oss_bucket`, `oss_object_key`, `oss_url`,
  `file_size_kb`, `download_count`, `view_count`, `status`, `uploader_id`, `upload_time`, `is_deleted`, `sort`
) VALUES
-- 小学 期末/月考/开学/寒暑假/升学（试卷无单元，unit_name 填「综合」）
(10100,'小学','数学','期末','试卷','六年级下册','人教版','综合','2025-2026学年六年级下册数学期末试卷（含答案）','期末数学.doc','doc','qier-duuyi','demo/paper/p1.doc','https://example.com/p1.doc',512,10,5,1,1,'2026-05-18 10:00:00',0,10),
(10101,'小学','语文','期末','试卷','六年级下册','统编版(2024)','综合','2025-2026学年六年级下册语文期末试卷（含答案）','期末语文.doc','doc','qier-duuyi','demo/paper/p2.doc','https://example.com/p2.doc',512,12,6,1,1,'2026-05-18 09:00:00',0,9),
(10102,'小学','数学','月考','试卷','五年级下册','人教版','综合','2025-2026学年五年级下册数学月考试卷','月考数学.doc','doc','qier-duuyi','demo/paper/p3.doc','https://example.com/p3.doc',400,8,4,1,1,'2026-05-17 14:00:00',0,8),
(10103,'小学','语文','期中','试卷','四年级下册','统编版(2024)','综合','2025-2026学年四年级下册语文期中试卷','期中语文.doc','doc','qier-duuyi','demo/paper/p4.doc','https://example.com/p4.doc',400,7,3,1,1,'2026-05-17 12:00:00',0,7),
(10104,'小学','数学','开学专区','试卷','三年级上册','人教版','综合','2025-2026学年三年级上册数学开学考试卷','开学数学.doc','doc','qier-duuyi','demo/paper/p5.doc','https://example.com/p5.doc',380,6,2,1,1,'2026-05-16 11:00:00',0,6),
(10105,'小学','语文','寒假','试卷','五年级下册','统编版(2024)','综合','2026寒假五年级语文复习试卷','寒假语文.doc','doc','qier-duuyi','demo/paper/p6.doc','https://example.com/p6.doc',350,5,2,1,1,'2026-05-16 10:00:00',0,5),
(10106,'小学','数学','暑假','试卷','六年级下册','人教版','综合','2026暑假六年级数学衔接试卷','暑假数学.doc','doc','qier-duuyi','demo/paper/p7.doc','https://example.com/p7.doc',350,5,2,1,1,'2026-05-15 16:00:00',0,4),
(10107,'小学','语文','小升初真题','试卷','六年级下册','统编版(2024)','综合','2025年某某区小升初语文真题及答案','小升初语文.doc','doc','qier-duuyi','demo/paper/p8.doc','https://example.com/p8.doc',600,20,10,1,1,'2026-05-15 15:00:00',0,3),
(10108,'小学','数学','期中','试卷','六年级下册','人教版','综合','2025-2026学年六年级下册数学期中试卷（含答案）','期中数学.doc','doc','qier-duuyi','demo/paper/p9.doc','https://example.com/p9.doc',480,11,6,1,1,'2026-05-18 08:00:00',0,11),
(10109,'小学','语文','期中','试卷','六年级下册','统编版(2024)','综合','2025-2026学年六年级下册语文期中试卷（含答案）','期中语文6.doc','doc','qier-duuyi','demo/paper/p10.doc','https://example.com/p10.doc',480,10,5,1,1,'2026-05-18 07:30:00',0,10),
-- 初中
(10110,'初中','数学','期末','试卷','九年级下册','人教版','综合','2025-2026学年九年级下册数学期末试卷（含答案）','期末数学.doc','doc','qier-duuyi','demo/paper/j1.doc','https://example.com/j1.doc',512,15,8,1,1,'2026-05-18 11:00:00',0,10),
(10111,'初中','语文','期末','试卷','九年级下册','统编版(2024)','综合','2025-2026学年九年级下册语文期末试卷（含答案）','期末语文.doc','doc','qier-duuyi','demo/paper/j2.doc','https://example.com/j2.doc',512,14,7,1,1,'2026-05-18 10:30:00',0,9),
(10112,'初中','数学','月考','试卷','八年级下册','人教版','综合','2025-2026学年八年级下册数学月考试卷','月考数学.doc','doc','qier-duuyi','demo/paper/j3.doc','https://example.com/j3.doc',400,9,4,1,1,'2026-05-17 15:00:00',0,8),
(10113,'初中','英语','期中','试卷','七年级下册','人教版','综合','2025-2026学年七年级下册英语期中试卷','期中英语.doc','doc','qier-duuyi','demo/paper/j4.doc','https://example.com/j4.doc',400,8,4,1,1,'2026-05-17 13:00:00',0,7),
(10114,'初中','数学','开学专区','试卷','七年级上册','人教版','综合','2025-2026学年七年级上册数学开学考试卷','开学数学.doc','doc','qier-duuyi','demo/paper/j5.doc','https://example.com/j5.doc',380,7,3,1,1,'2026-05-16 12:00:00',0,6),
(10115,'初中','语文','寒假','试卷','八年级下册','统编版(2024)','综合','2026寒假八年级语文复习试卷','寒假语文.doc','doc','qier-duuyi','demo/paper/j6.doc','https://example.com/j6.doc',350,6,2,1,1,'2026-05-16 11:00:00',0,5),
(10116,'初中','数学','暑假','试卷','九年级下册','人教版','综合','2026暑假九年级数学衔接试卷','暑假数学.doc','doc','qier-duuyi','demo/paper/j7.doc','https://example.com/j7.doc',350,6,2,1,1,'2026-05-15 17:00:00',0,4),
(10117,'初中','数学','中考模拟','试卷','九年级下册','人教版','综合','2026年中考数学模拟试卷（一）','中考模拟.doc','doc','qier-duuyi','demo/paper/j8.doc','https://example.com/j8.doc',600,25,12,1,1,'2026-05-15 16:00:00',0,3),
-- 高中
(10120,'高中','数学','期末','试卷','高三下册','人教版','综合','2025-2026学年高三下册数学期末试卷（含答案）','期末数学.doc','doc','qier-duuyi','demo/paper/s1.doc','https://example.com/s1.doc',512,18,9,1,1,'2026-05-18 12:00:00',0,10),
(10121,'高中','语文','期末','试卷','高二下册','统编版(2024)','综合','2025-2026学年高二下册语文期末试卷（含答案）','期末语文.doc','doc','qier-duuyi','demo/paper/s2.doc','https://example.com/s2.doc',512,16,8,1,1,'2026-05-18 11:30:00',0,9),
(10122,'高中','物理','月考','试卷','高一上册','人教版','综合','2025-2026学年高一上册物理月考试卷','月考物理.doc','doc','qier-duuyi','demo/paper/s3.doc','https://example.com/s3.doc',400,10,5,1,1,'2026-05-17 16:00:00',0,8),
(10123,'高中','历史','期中','试卷','高二上册','统编版(2024)','综合','2025-2026学年高二上册历史期中试卷','期中历史.doc','doc','qier-duuyi','demo/paper/s4.doc','https://example.com/s4.doc',400,9,4,1,1,'2026-05-17 14:00:00',0,7),
(10124,'高中','数学','开学专区','试卷','高一上册','人教版','综合','2025-2026学年高一上册数学开学考试卷','开学数学.doc','doc','qier-duuyi','demo/paper/s5.doc','https://example.com/s5.doc',380,8,3,1,1,'2026-05-16 13:00:00',0,6),
(10125,'高中','英语','寒假','试卷','高二下册','人教版','综合','2026寒假高二英语复习试卷','寒假英语.doc','doc','qier-duuyi','demo/paper/s6.doc','https://example.com/s6.doc',350,7,3,1,1,'2026-05-16 12:00:00',0,5),
(10126,'高中','数学','暑假','试卷','高三下册','人教版','综合','2026暑假高三数学衔接试卷','暑假数学.doc','doc','qier-duuyi','demo/paper/s7.doc','https://example.com/s7.doc',350,7,3,1,1,'2026-05-15 18:00:00',0,4),
(10127,'高中','数学','高考模拟','试卷','高三下册','人教版','综合','2026年高考数学模拟试卷（一）','高考模拟.doc','doc','qier-duuyi','demo/paper/s8.doc','https://example.com/s8.doc',600,30,15,1,1,'2026-05-15 17:00:00',0,3);

-- ==================== OSS：升学专区演示（10200-10229） ====================
DELETE FROM `oss_primary_chinese_resource` WHERE `id` BETWEEN 10200 AND 10229;

INSERT INTO `oss_primary_chinese_resource` (
  `id`, `stage`, `subject`, `module`, `type`, `grade_name`, `edition`, `unit_name`,
  `title`, `original_filename`, `file_ext`, `oss_bucket`, `oss_object_key`, `oss_url`,
  `file_size_kb`, `download_count`, `view_count`, `status`, `uploader_id`, `upload_time`, `is_deleted`, `sort`
) VALUES
(10200,'初中','数学','中考真题','试卷','九年级下册','人教版','综合','2026武汉初三中考数学真题及答案','zk.doc','doc','qier-duuyi','demo/promo/z1.doc','https://example.com/z1.doc',800,50,20,1,1,'2026-05-16 10:00:00',0,10),
(10201,'初中','语文','中考真题','试卷','九年级下册','统编版(2024)','综合','2026江门初三中考语文真题及答案','zk.doc','doc','qier-duuyi','demo/promo/z2.doc','https://example.com/z2.doc',800,48,19,1,1,'2026-05-16 09:30:00',0,9),
(10202,'初中','数学','中考模拟','试卷','九年级下册','人教版','综合','2026合肥初三一模数学试卷及答案','zk_mock.doc','doc','qier-duuyi','demo/promo/z3.doc','https://example.com/z3.doc',700,40,18,1,1,'2026-05-16 09:00:00',0,8),
(10203,'初中','英语','中考模拟','试卷','九年级下册','人教版','综合','2026烟台初三二模英语试卷及答案','zk_mock2.doc','doc','qier-duuyi','demo/promo/z4.doc','https://example.com/z4.doc',700,38,17,1,1,'2026-05-15 18:00:00',0,7),
(10204,'初中','语文','一轮复习','试卷','九年级下册','统编版(2024)','综合','2026中考语文一轮复习专题讲义','round1.doc','doc','qier-duuyi','demo/promo/z5.doc','https://example.com/z5.doc',600,30,15,1,1,'2026-05-15 17:00:00',0,6),
(10205,'高中','数学','高考真题','试卷','高三下册','人教版','综合','2026年全国高考数学真题（新课标I卷）','gk.doc','doc','qier-duuyi','demo/promo/g1.doc','https://example.com/g1.doc',800,55,25,1,1,'2026-05-16 11:00:00',0,10),
(10206,'高中','语文','高考模拟','试卷','高三下册','统编版(2024)','综合','2026某某市高三一模语文试卷及答案','gk_mock.doc','doc','qier-duuyi','demo/promo/g2.doc','https://example.com/g2.doc',750,45,20,1,1,'2026-05-16 10:30:00',0,9),
(10207,'小学','语文','小升初真题','试卷','六年级下册','统编版(2024)','综合','2026年某某市小升初语文真题汇编','xsc.doc','doc','qier-duuyi','demo/promo/x1.doc','https://example.com/x1.doc',650,35,16,1,1,'2026-05-16 10:00:00',0,8),
(10208,'小学','数学','小升初模拟','试卷','六年级下册','人教版','综合','2026年小升初数学模拟试卷（三）','xsc_mock.doc','doc','qier-duuyi','demo/promo/x2.doc','https://example.com/x2.doc',600,32,14,1,1,'2026-05-15 16:00:00',0,7),
(10209,'初中','数学','中考模拟','试卷','九年级下册','人教版','综合','2026武汉初三二模试卷及答案（九科全）','zk_2m.doc','doc','qier-duuyi','demo/promo/z6.doc','https://example.com/z6.doc',800,45,18,1,1,'2026-05-16 10:15:00',0,11),
(10210,'初中','物理','二轮专题','试卷','九年级下册','人教版','综合','2026中考物理二轮专题突破讲义','round2.doc','doc','qier-duuyi','demo/promo/z7.doc','https://example.com/z7.doc',650,28,12,1,1,'2026-05-15 16:30:00',0,5),
(10211,'初中','化学','三轮冲刺','试卷','九年级下册','人教版','综合','2026中考化学三轮冲刺模拟卷','round3.doc','doc','qier-duuyi','demo/promo/z8.doc','https://example.com/z8.doc',620,26,11,1,1,'2026-05-15 16:00:00',0,4),
(10212,'初中','数学','真题汇编','试卷','九年级下册','人教版','综合','2019-2026中考数学真题汇编','zk_comp.doc','doc','qier-duuyi','demo/promo/z9.doc','https://example.com/z9.doc',900,42,20,1,1,'2026-05-15 15:30:00',0,3);

-- ==================== OSS：同步备课演示（高中历史等，10130-10139） ====================
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

-- ==================== edu_resource：初中/高中/美术/舞蹈（20100-20124） ====================
DELETE FROM `edu_resource_dimension` WHERE `resource_id` BETWEEN 20100 AND 20124;
DELETE FROM `edu_resource_module` WHERE `resource_id` BETWEEN 20100 AND 20124;
DELETE FROM `edu_resource` WHERE `id` BETWEEN 20100 AND 20124;

INSERT INTO `edu_resource` (
  `id`, `title`, `description`, `original_filename`, `file_ext`,
  `oss_bucket`, `oss_object_key`, `oss_url`, `file_size_kb`,
  `status`, `is_free`, `download_count`, `view_count`, `publish_time`, `upload_time`, `is_deleted`, `sort`
) VALUES
(20100,'人教版九年级数学期末复习课件','初中数学期末','课件.pptx','pptx','qier-duuyi','demo/edu/20100.pptx','https://example.com/20100.pptx',2048,1,1,20,10,'2026-05-18 10:00:00','2026-05-18 10:00:00',0,10),
(20101,'人教版九年级数学期末测试卷','初中数学期末试卷','试卷.pdf','pdf','qier-duuyi','demo/edu/20101.pdf','https://example.com/20101.pdf',1024,1,1,30,15,'2026-05-18 09:00:00','2026-05-18 09:00:00',0,9),
(20102,'统编版高二历史选择性必修课件','高中历史同步备课','课件.pptx','pptx','qier-duuyi','demo/edu/20102.pptx','https://example.com/20102.pptx',2048,1,1,25,12,'2026-05-18 08:00:00','2026-05-18 08:00:00',0,10),
(20103,'2026高考历史一轮复习学案','高中历史一轮复习','学案.doc','doc','qier-duuyi','demo/edu/20103.doc','https://example.com/20103.doc',800,1,1,40,18,'2026-05-17 16:00:00','2026-05-17 16:00:00',0,9),
(20104,'2026中考物理二模试卷','初中物理中考模拟','试卷.pdf','pdf','qier-duuyi','demo/edu/20104.pdf','https://example.com/20104.pdf',900,1,1,35,16,'2026-05-17 15:00:00','2026-05-17 15:00:00',0,8),
(20105,'美术学段素描基础课件','美术同步备课','课件.pptx','pptx','qier-duuyi','demo/edu/20105.pptx','https://example.com/20105.pptx',1500,1,1,15,8,'2026-05-17 14:00:00','2026-05-17 14:00:00',0,10),
(20106,'舞蹈学段中国舞基本功教案','舞蹈同步备课','教案.doc','doc','qier-duuyi','demo/edu/20106.doc','https://example.com/20106.doc',600,1,1,12,6,'2026-05-17 13:00:00','2026-05-17 13:00:00',0,9);

INSERT INTO `edu_resource_dimension` (
  `resource_id`, `stage_id`, `subject_id`, `edition_id`, `grade_id`, `volume_id`,
  `module_id`, `resource_type_id`, `exam_scene_id`
) VALUES
(20100, 2, 10, 2, 9, 2, 1, 13, NULL),
(20101, 2, 10, 2, 9, 2, 5, 23, 4),
(20102, 3, 24, 1, 11, 1, 1, 13, NULL),
(20103, 3, 24, 1, 12, 2, 19, 12, 7),
(20104, 2, 12, 2, 9, 2, 22, 23, 6),
(20105, 4, 27, 1, 6, 2, 1, 13, NULL),
(20106, 5, 28, 1, 6, 2, 1, 11, NULL);

INSERT INTO `edu_resource_module` (`resource_id`, `module_id`) VALUES
(20100, 1), (20101, 5), (20101, 4),
(20102, 1), (20103, 19), (20103, 23),
(20104, 22), (20104, 23),
(20105, 1), (20106, 1);

-- 成套资源演示
DELETE FROM `edu_resource_suite` WHERE `id` BETWEEN 3001 AND 3010;
INSERT INTO `edu_resource_suite` (`id`, `title`, `description`, `module_id`, `status`, `sort`, `update_time`) VALUES
(3001, '一年级下册语文同步备课成套资源', '含课件教案练习', 1, 1, 10, '2026-05-18 10:00:00'),
(3002, '九年级数学期末备考成套试卷', '期末+模拟', 5, 1, 9, '2026-05-18 09:00:00'),
(3003, '高三历史高考复习成套资料', '一轮+真题', 19, 1, 8, '2026-05-17 16:00:00');

SET FOREIGN_KEY_CHECKS = 1;

SELECT '24_home_panel_p1.sql 执行完成' AS message;
