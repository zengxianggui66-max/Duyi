-- ============================================================
-- 10 学科详情页演示数据
-- 场景：小学 / 语文 / 一年级下册 / 统编版(2024) / 同步备课
--       第一单元·识字 / 春夏秋冬 / 课件
-- 依赖：00~09、99 已执行
-- 说明：前端列表走 oss_primary_chinese_resource；目录树走静态 JSON + DB 课文合并
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==================== 1. 规范目录：单元名与课文（edu_unit / edu_lesson） ====================
-- 与前端 unit-catalog.json、unitData.ts 命名对齐（· 分隔）
UPDATE `edu_unit` SET `name` = '第一单元·识字' WHERE `id` = 7;
UPDATE `edu_unit` SET `name` = '第二单元·课文' WHERE `id` = 8;
UPDATE `edu_unit` SET `name` = '第三单元·课文' WHERE `id` = 9;
UPDATE `edu_unit` SET `name` = '第四单元·课文' WHERE `id` = 10;
UPDATE `edu_unit` SET `name` = '第五单元·识字' WHERE `id` = 11;
UPDATE `edu_unit` SET `name` = '第六单元·课文' WHERE `id` = 12;

-- 一年级下册课文（unit_id 7~12）
INSERT INTO `edu_lesson` (`id`, `unit_id`, `code`, `name`, `lesson_no`, `sort`, `status`) VALUES
(9,  7, 'l1', '春夏秋冬',           1, 1, 1),
(10, 7, 'l2', '姓氏歌',             2, 2, 1),
(11, 8, 'l1', '吃水不忘挖井人',     1, 1, 1),
(12, 8, 'l2', '我多想去看看',       2, 2, 1),
(13, 9, 'l1', '小公鸡和小鸭子',     1, 1, 1),
(14, 9, 'l2', '树和喜鹊',           2, 2, 1),
(15, 10, 'l1', '静夜思',            1, 1, 1),
(16, 10, 'l2', '夜色',              2, 2, 1),
(17, 11, 'l1', '动物儿歌',          1, 1, 1),
(18, 11, 'l2', '古对今',            2, 2, 1),
(19, 12, 'l1', '古诗二首',          1, 1, 1),
(20, 12, 'l2', '荷叶圆圆',          2, 2, 1)
ON DUPLICATE KEY UPDATE
  `unit_id` = VALUES(`unit_id`),
  `name` = VALUES(`name`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);

-- ==================== 2. 宽表资源（页面实际查询表） ====================
-- 清理本脚本写入的演示 ID 段，可重复执行
DELETE FROM `oss_primary_chinese_resource` WHERE `id` BETWEEN 10001 AND 10099;

INSERT INTO `oss_primary_chinese_resource` (
  `id`, `stage`, `subject`, `module`, `type`,
  `grade_name`, `edition`, `unit_name`, `lesson_name`,
  `title`, `original_filename`, `file_ext`,
  `oss_bucket`, `oss_object_key`, `oss_url`,
  `file_size_kb`, `download_count`, `view_count`,
  `status`, `uploader_id`, `upload_time`, `is_deleted`, `sort`, `remark`
) VALUES
-- ---------- 第一单元·识字 / 春夏秋冬 ----------
(10001, '小学', '语文', '同步备课', '课件', '一年级下册', '统编版(2024)', '第一单元·识字', '春夏秋冬',
 '识字1 春夏秋冬【课件】', '识字1 春夏秋冬【课件】.pptx', 'pptx',
 'qier-duuyi', 'demo/y1s2/u1/识字1 春夏秋冬【课件】.pptx',
 'https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/demo/y1s2/%E8%AF%86%E5%AD%971%20%E6%98%A5%E5%A4%8F%E7%A7%8B%E5%86%AC%E3%80%90%E8%AF%BE%E4%BB%B6%E3%80%91.pptx',
 2048, 326, 89, 1, 1, '2026-05-10 10:00:00', 0, 10, '演示数据-课件'),

(10002, '小学', '语文', '同步备课', '教案', '一年级下册', '统编版(2024)', '第一单元·识字', '春夏秋冬',
 '识字1 春夏秋冬【教案】', '识字1 春夏秋冬【教案】.doc', 'doc',
 'qier-duuyi', '1.第一单元/识字1 春夏秋冬【教案】.doc',
 'https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/1.%E7%AC%AC%E4%B8%80%E5%8D%95%E5%85%83/%E8%AF%86%E5%AD%971%20%E6%98%A5%E5%A4%8F%E7%A7%8B%E5%86%AC%E3%80%90%E6%95%99%E6%A1%88%E3%80%91.doc',
 1309, 88, 45, 1, 1, '2026-05-10 12:41:00', 0, 9, '演示数据-教案'),

(10003, '小学', '语文', '同步备课', '练习', '一年级下册', '统编版(2024)', '第一单元·识字', '春夏秋冬',
 '识字1 春夏秋冬【课时练习】', '识字1 春夏秋冬【课时练习】.doc', 'doc',
 'qier-duuyi', 'demo/y1s2/u1/识字1 春夏秋冬【课时练习】.doc',
 'https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/demo/y1s2/%E8%AF%86%E5%AD%971%20%E6%98%A5%E5%A4%8F%E7%A7%8B%E5%86%AC%E3%80%90%E8%AF%BE%E6%97%B6%E7%BB%83%E4%B9%A0%E3%80%91.doc',
 512, 156, 32, 1, 1, '2026-05-11 09:20:00', 0, 8, '演示数据-练习'),

(10004, '小学', '语文', '同步备课', '学案', '一年级下册', '统编版(2024)', '第一单元·识字', '春夏秋冬',
 '识字1 春夏秋冬【学案】', '识字1 春夏秋冬【学案】.doc', 'doc',
 'qier-duuyi', 'demo/y1s2/u1/识字1 春夏秋冬【学案】.doc',
 'https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/demo/y1s2/%E8%AF%86%E5%AD%971%20%E6%98%A5%E5%A4%8F%E7%A7%8B%E5%86%AC%E3%80%90%E5%AD%A6%E6%A1%88%E3%80%91.doc',
 768, 94, 21, 1, 1, '2026-05-11 14:00:00', 0, 7, '演示数据-学案'),

(10005, '小学', '语文', '同步备课', '课件', '一年级下册', '统编版(2024)', '第一单元·识字', '春夏秋冬',
 '识字1 春夏秋冬【识字游戏课件】', '识字1 春夏秋冬【识字游戏课件】.pptx', 'pptx',
 'qier-duuyi', 'demo/y1s2/u1/识字1 春夏秋冬【识字游戏课件】.pptx',
 'https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/demo/y1s2/%E8%AF%86%E5%AD%971%20%E6%98%A5%E5%A4%8F%E7%A7%8B%E5%86%AC%E3%80%90%E8%AF%86%E5%AD%97%E6%B8%B8%E6%88%8F%E8%AF%BE%E4%BB%B6%E3%80%91.pptx',
 1536, 201, 67, 1, 1, '2026-05-12 08:30:00', 0, 6, '演示数据-课件2'),

(10006, '小学', '语文', '同步备课', '音频/朗读', '一年级下册', '统编版(2024)', '第一单元·识字', '春夏秋冬',
 '识字1 春夏秋冬【课文朗读】', '识字1 春夏秋冬【课文朗读】.mp3', 'mp3',
 'qier-duuyi', 'demo/y1s2/u1/识字1 春夏秋冬【课文朗读】.mp3',
 'https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/demo/y1s2/%E8%AF%86%E5%AD%971%20%E6%98%A5%E5%A4%8F%E7%A7%8B%E5%86%AC%E3%80%90%E8%AF%BE%E6%96%87%E6%9C%97%E8%AF%BB%E3%80%91.mp3',
 3200, 78, 120, 1, 1, '2026-05-12 11:00:00', 0, 5, '演示数据-音频'),

-- ---------- 第一单元·识字 / 姓氏歌 ----------
(10007, '小学', '语文', '同步备课', '课件', '一年级下册', '统编版(2024)', '第一单元·识字', '姓氏歌',
 '识字2 姓氏歌【课件】', '识字2 姓氏歌【课件】.pptx', 'pptx',
 'qier-duuyi', 'demo/y1s2/u1/识字2 姓氏歌【课件】.pptx',
 'https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/demo/y1s2/%E8%AF%86%E5%AD%972%20%E5%A7%93%E6%B0%8F%E6%AD%8C%E3%80%90%E8%AF%BE%E4%BB%B6%E3%80%91.pptx',
 1920, 145, 38, 1, 1, '2026-05-10 15:00:00', 0, 4, NULL),

(10008, '小学', '语文', '同步备课', '教案', '一年级下册', '统编版(2024)', '第一单元·识字', '姓氏歌',
 '识字2 姓氏歌【教案】', '识字2 姓氏歌【教案】.doc', 'doc',
 'qier-duuyi', '1.第一单元/识字2 姓氏歌【教案】.doc',
 'https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/1.%E7%AC%AC%E4%B8%80%E5%8D%95%E5%85%83/%E8%AF%86%E5%AD%972%20%E5%A7%93%E6%B0%8F%E6%AD%8C%E3%80%90%E6%95%99%E6%A1%88%E3%80%91.doc',
 308, 72, 28, 1, 1, '2026-05-10 12:41:00', 0, 3, NULL),

-- ---------- 单元级（选中「第一单元·识字」父节点时） ----------
(10009, '小学', '语文', '同步备课', '课件', '一年级下册', '统编版(2024)', '第一单元·识字', NULL,
 '第一单元·识字【单元导读课件】', '第一单元·识字【单元导读课件】.pptx', 'pptx',
 'qier-duuyi', 'demo/y1s2/u1/第一单元·识字【单元导读课件】.pptx',
 'https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/demo/y1s2/%E7%AC%AC%E4%B8%80%E5%8D%95%E5%85%83%E3%80%90%E5%8D%95%E5%85%83%E5%AF%BC%E8%AF%BB%E8%AF%BE%E4%BB%B6%E3%80%91.pptx',
 2560, 412, 95, 1, 1, '2026-05-08 16:00:00', 0, 2, NULL),

(10010, '小学', '语文', '同步备课', '教案', '一年级下册', '统编版(2024)', '第一单元·识字', NULL,
 '口语交际：听故事，讲故事【教案】', '口语交际：听故事，讲故事【教案】.doc', 'doc',
 'qier-duuyi', '1.第一单元/口语交际：听故事，讲故事【教案】.doc',
 'https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/1.%E7%AC%AC%E4%B8%80%E5%8D%95%E5%85%83/%E5%8F%A3%E8%AF%AD%E4%BA%A4%E9%99%85%EF%BC%9A%E5%90%AC%E6%95%85%E4%BA%8B%EF%BC%8C%E8%AE%B2%E6%95%85%E4%BA%8B%E3%80%90%E6%95%99%E6%A1%88%E3%80%91.doc',
 277, 65, 18, 1, 1, '2026-05-10 12:41:00', 0, 1, NULL),

-- ---------- 第二单元样例（吃水不忘挖井人） ----------
(10011, '小学', '语文', '同步备课', '课件', '一年级下册', '统编版(2024)', '第二单元·课文', '吃水不忘挖井人',
 '1 吃水不忘挖井人【课件】', '1 吃水不忘挖井人【课件】.pptx', 'pptx',
 'qier-duuyi', 'demo/y1s2/u2/1 吃水不忘挖井人【课件】.pptx',
 'https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/demo/y1s2/u2/%E5%90%83%E6%B0%B4%E4%B8%8D%E5%BF%98%E6%8C%96%E4%BA%95%E4%BA%BA%E3%80%90%E8%AF%BE%E4%BB%B6%E3%80%91.pptx',
 1800, 98, 41, 1, 1, '2026-05-13 10:00:00', 0, 0, NULL);

-- ==================== 3. 规范资源主表（edu_resource + 维度，可选新接口） ====================
DELETE FROM `edu_resource_dimension` WHERE `resource_id` BETWEEN 20001 AND 20020;
DELETE FROM `edu_resource_module` WHERE `resource_id` BETWEEN 20001 AND 20020;
DELETE FROM `edu_resource` WHERE `id` BETWEEN 20001 AND 20020;

INSERT INTO `edu_resource` (
  `id`, `title`, `description`, `original_filename`, `file_ext`,
  `oss_bucket`, `oss_object_key`, `oss_url`, `file_size_kb`,
  `status`, `is_free`, `download_count`, `view_count`, `publish_time`, `upload_time`, `is_deleted`, `sort`
) VALUES
(20001, '识字1 春夏秋冬【课件】', '一年级下册统编版·同步备课·课件演示', '识字1 春夏秋冬【课件】.pptx', 'pptx',
 'qier-duuyi', 'demo/y1s2/u1/识字1 春夏秋冬【课件】.pptx',
 'https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/demo/y1s2/%E8%AF%86%E5%AD%971%20%E6%98%A5%E5%A4%8F%E7%A7%8B%E5%86%AC%E3%80%90%E8%AF%BE%E4%BB%B6%E3%80%91.pptx',
 2048, 2, 1, 326, 89, '2026-05-10 10:00:00', '2026-05-10 10:00:00', 0, 10),
(20002, '识字1 春夏秋冬【教案】', '一年级下册统编版·同步备课·教案演示', '识字1 春夏秋冬【教案】.doc', 'doc',
 'qier-duuyi', '1.第一单元/识字1 春夏秋冬【教案】.doc',
 'https://qier-duuyi.oss-cn-chengdu.aliyuncs.com/1.%E7%AC%AC%E4%B8%80%E5%8D%95%E5%85%83/%E8%AF%86%E5%AD%971%20%E6%98%A5%E5%A4%8F%E7%A7%8B%E5%86%AC%E3%80%90%E6%95%99%E6%A1%88%E3%80%91.doc',
 1309, 2, 1, 88, 45, '2026-05-10 12:41:00', '2026-05-10 12:41:00', 0, 9);

INSERT INTO `edu_resource_dimension` (
  `resource_id`, `stage_id`, `subject_id`, `edition_id`, `grade_id`,
  `semester_id`, `volume_id`, `module_id`, `resource_type_id`, `unit_id`, `lesson_id`
) VALUES
(20001, 1, 1, 1, 1, 2, 2, 1, 13, 7, 9),
(20002, 1, 1, 1, 1, 2, 2, 1, 11, 7, 9);

INSERT INTO `edu_resource_module` (`resource_id`, `module_id`) VALUES
(20001, 1), (20002, 1);

SET FOREIGN_KEY_CHECKS = 1;

SELECT '10_seed_y1s2_chinese_demo.sql 执行完成' AS message,
  (SELECT COUNT(*) FROM edu_lesson WHERE unit_id BETWEEN 7 AND 12) AS y1s2_lessons,
  (SELECT COUNT(*) FROM oss_primary_chinese_resource WHERE id BETWEEN 10001 AND 10099) AS oss_demo_rows,
  (SELECT COUNT(*) FROM edu_resource WHERE id BETWEEN 20001 AND 20020) AS edu_resource_demo_rows;
