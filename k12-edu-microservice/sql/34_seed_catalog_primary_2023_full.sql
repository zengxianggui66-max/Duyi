-- ============================================================
-- 小学 1-6 年级全科目录（2023 版：统编语文 + 人教数学 + 人教 PEP 英语）
-- 生成：python sql/tools/generate_primary_catalog.py
-- 执行：mysql -u root -p xinketang < sql/34_seed_catalog_primary_2023_full.sql
-- 依赖：28_catalog_scheme.sql、27_brand_baseline.sql
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

SET @scheme_id = (SELECT `id` FROM `edu_catalog_scheme` WHERE `code` = 'textbook_unit' LIMIT 1);

-- 清理旧的小学册别目录（仅含 volumeKey 的节点）
DELETE FROM `edu_catalog_node` WHERE `scheme_id` = @scheme_id AND JSON_UNQUOTE(JSON_EXTRACT(`meta`, '$.volumeKey')) IN ('y1s1', 'y1s2', 'y2s1', 'y2s2', 'y3s1', 'y3s2', 'y4s1', 'y4s2', 'y5s1', 'y5s2', 'y6s1', 'y6s2');

-- y1s1 语文（统编版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y1s1_语文_root', '语文（统编版）', '/语文（统编版）', 0, 'folder', 1, '{"volumeKey": "y1s1", "subject": "语文", "edition": "统编版", "defaultModule": "同步备课"}', 1);
SET @y1s1_语文_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_语文_root, 'y1s1_语文_u01', '我上学了', '/语文（统编版）/我上学了', 1, 'unit', 1, '{"volumeKey": "y1s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y1s1_语文_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_语文_u01, 'y1s1_语文_u01_l01', '我是中国人', '/语文（统编版）/我上学了/我是中国人', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u01, 'y1s1_语文_u01_l02', '我是小学生', '/语文（统编版）/我上学了/我是小学生', 2, 'lesson', 2, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u01, 'y1s1_语文_u01_l03', '我爱学语文', '/语文（统编版）/我上学了/我爱学语文', 2, 'lesson', 3, '{"volumeKey": "y1s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_语文_root, 'y1s1_语文_u02', '第一单元：识字', '/语文（统编版）/第一单元：识字', 1, 'unit', 2, '{"volumeKey": "y1s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y1s1_语文_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_语文_u02, 'y1s1_语文_u02_l01', '1 天地人', '/语文（统编版）/第一单元：识字/1 天地人', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u02, 'y1s1_语文_u02_l02', '2 金木水火土', '/语文（统编版）/第一单元：识字/2 金木水火土', 2, 'lesson', 2, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u02, 'y1s1_语文_u02_l03', '3 口耳目', '/语文（统编版）/第一单元：识字/3 口耳目', 2, 'lesson', 3, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u02, 'y1s1_语文_u02_l04', '4 日月水火', '/语文（统编版）/第一单元：识字/4 日月水火', 2, 'lesson', 4, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u02, 'y1s1_语文_u02_l05', '5 对韵歌', '/语文（统编版）/第一单元：识字/5 对韵歌', 2, 'lesson', 5, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u02, 'y1s1_语文_u02_l06', '口语交际：我说你做', '/语文（统编版）/第一单元：识字/口语交际：我说你做', 2, 'lesson', 6, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u02, 'y1s1_语文_u02_l07', '语文园地一', '/语文（统编版）/第一单元：识字/语文园地一', 2, 'lesson', 7, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u02, 'y1s1_语文_u02_l08', '快乐读书吧：读书真快乐', '/语文（统编版）/第一单元：识字/快乐读书吧：读书真快乐', 2, 'lesson', 8, '{"volumeKey": "y1s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_语文_root, 'y1s1_语文_u03', '汉语拼音', '/语文（统编版）/汉语拼音', 1, 'unit', 3, '{"volumeKey": "y1s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y1s1_语文_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_语文_u03, 'y1s1_语文_u03_l01', '1 a o e', '/语文（统编版）/汉语拼音/1 a o e', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u03, 'y1s1_语文_u03_l02', '2 i u ü y w', '/语文（统编版）/汉语拼音/2 i u ü y w', 2, 'lesson', 2, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u03, 'y1s1_语文_u03_l03', '3 b p m f', '/语文（统编版）/汉语拼音/3 b p m f', 2, 'lesson', 3, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u03, 'y1s1_语文_u03_l04', '4 d t n l', '/语文（统编版）/汉语拼音/4 d t n l', 2, 'lesson', 4, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u03, 'y1s1_语文_u03_l05', '5 g k h', '/语文（统编版）/汉语拼音/5 g k h', 2, 'lesson', 5, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u03, 'y1s1_语文_u03_l06', '6 j q x', '/语文（统编版）/汉语拼音/6 j q x', 2, 'lesson', 6, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u03, 'y1s1_语文_u03_l07', '7 z c s', '/语文（统编版）/汉语拼音/7 z c s', 2, 'lesson', 7, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u03, 'y1s1_语文_u03_l08', '8 zh ch sh r', '/语文（统编版）/汉语拼音/8 zh ch sh r', 2, 'lesson', 8, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u03, 'y1s1_语文_u03_l09', '语文园地二', '/语文（统编版）/汉语拼音/语文园地二', 2, 'lesson', 9, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u03, 'y1s1_语文_u03_l10', '9 ai ei ui', '/语文（统编版）/汉语拼音/9 ai ei ui', 2, 'lesson', 10, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u03, 'y1s1_语文_u03_l11', '10 ao ou iu', '/语文（统编版）/汉语拼音/10 ao ou iu', 2, 'lesson', 11, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u03, 'y1s1_语文_u03_l12', '11 ie üe er', '/语文（统编版）/汉语拼音/11 ie üe er', 2, 'lesson', 12, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u03, 'y1s1_语文_u03_l13', '12 an en in un ün', '/语文（统编版）/汉语拼音/12 an en in un ün', 2, 'lesson', 13, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u03, 'y1s1_语文_u03_l14', '13 ang eng ing ong', '/语文（统编版）/汉语拼音/13 ang eng ing ong', 2, 'lesson', 14, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u03, 'y1s1_语文_u03_l15', '语文园地三', '/语文（统编版）/汉语拼音/语文园地三', 2, 'lesson', 15, '{"volumeKey": "y1s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_语文_root, 'y1s1_语文_u04', '第四单元：课文', '/语文（统编版）/第四单元：课文', 1, 'unit', 4, '{"volumeKey": "y1s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y1s1_语文_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_语文_u04, 'y1s1_语文_u04_l01', '1 秋天', '/语文（统编版）/第四单元：课文/1 秋天', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u04, 'y1s1_语文_u04_l02', '2 小小的船', '/语文（统编版）/第四单元：课文/2 小小的船', 2, 'lesson', 2, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u04, 'y1s1_语文_u04_l03', '3 江南', '/语文（统编版）/第四单元：课文/3 江南', 2, 'lesson', 3, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u04, 'y1s1_语文_u04_l04', '4 四季', '/语文（统编版）/第四单元：课文/4 四季', 2, 'lesson', 4, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u04, 'y1s1_语文_u04_l05', '口语交际：我们做朋友', '/语文（统编版）/第四单元：课文/口语交际：我们做朋友', 2, 'lesson', 5, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u04, 'y1s1_语文_u04_l06', '语文园地四', '/语文（统编版）/第四单元：课文/语文园地四', 2, 'lesson', 6, '{"volumeKey": "y1s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_语文_root, 'y1s1_语文_u05', '第五单元：识字', '/语文（统编版）/第五单元：识字', 1, 'unit', 5, '{"volumeKey": "y1s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y1s1_语文_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_语文_u05, 'y1s1_语文_u05_l01', '6 画', '/语文（统编版）/第五单元：识字/6 画', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u05, 'y1s1_语文_u05_l02', '7 大小多少', '/语文（统编版）/第五单元：识字/7 大小多少', 2, 'lesson', 2, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u05, 'y1s1_语文_u05_l03', '8 小书包', '/语文（统编版）/第五单元：识字/8 小书包', 2, 'lesson', 3, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u05, 'y1s1_语文_u05_l04', '9 日月明', '/语文（统编版）/第五单元：识字/9 日月明', 2, 'lesson', 4, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u05, 'y1s1_语文_u05_l05', '10 升国旗', '/语文（统编版）/第五单元：识字/10 升国旗', 2, 'lesson', 5, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u05, 'y1s1_语文_u05_l06', '语文园地五', '/语文（统编版）/第五单元：识字/语文园地五', 2, 'lesson', 6, '{"volumeKey": "y1s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_语文_root, 'y1s1_语文_u06', '第六单元：课文', '/语文（统编版）/第六单元：课文', 1, 'unit', 6, '{"volumeKey": "y1s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y1s1_语文_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_语文_u06, 'y1s1_语文_u06_l01', '5 影子', '/语文（统编版）/第六单元：课文/5 影子', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u06, 'y1s1_语文_u06_l02', '6 比尾巴', '/语文（统编版）/第六单元：课文/6 比尾巴', 2, 'lesson', 2, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u06, 'y1s1_语文_u06_l03', '7 青蛙写诗', '/语文（统编版）/第六单元：课文/7 青蛙写诗', 2, 'lesson', 3, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u06, 'y1s1_语文_u06_l04', '8 雨点儿', '/语文（统编版）/第六单元：课文/8 雨点儿', 2, 'lesson', 4, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u06, 'y1s1_语文_u06_l05', '口语交际：用多大的声音', '/语文（统编版）/第六单元：课文/口语交际：用多大的声音', 2, 'lesson', 5, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u06, 'y1s1_语文_u06_l06', '语文园地六', '/语文（统编版）/第六单元：课文/语文园地六', 2, 'lesson', 6, '{"volumeKey": "y1s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_语文_root, 'y1s1_语文_u07', '第七单元：课文', '/语文（统编版）/第七单元：课文', 1, 'unit', 7, '{"volumeKey": "y1s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y1s1_语文_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_语文_u07, 'y1s1_语文_u07_l01', '9 明天要远足', '/语文（统编版）/第七单元：课文/9 明天要远足', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u07, 'y1s1_语文_u07_l02', '10 大还是小', '/语文（统编版）/第七单元：课文/10 大还是小', 2, 'lesson', 2, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u07, 'y1s1_语文_u07_l03', '11 项链', '/语文（统编版）/第七单元：课文/11 项链', 2, 'lesson', 3, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u07, 'y1s1_语文_u07_l04', '语文园地七', '/语文（统编版）/第七单元：课文/语文园地七', 2, 'lesson', 4, '{"volumeKey": "y1s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_语文_root, 'y1s1_语文_u08', '第八单元：课文', '/语文（统编版）/第八单元：课文', 1, 'unit', 8, '{"volumeKey": "y1s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y1s1_语文_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_语文_u08, 'y1s1_语文_u08_l01', '12 雪地里的小画家', '/语文（统编版）/第八单元：课文/12 雪地里的小画家', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u08, 'y1s1_语文_u08_l02', '13 乌鸦喝水', '/语文（统编版）/第八单元：课文/13 乌鸦喝水', 2, 'lesson', 2, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u08, 'y1s1_语文_u08_l03', '14 小蜗牛', '/语文（统编版）/第八单元：课文/14 小蜗牛', 2, 'lesson', 3, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u08, 'y1s1_语文_u08_l04', '口语交际：小兔运南瓜', '/语文（统编版）/第八单元：课文/口语交际：小兔运南瓜', 2, 'lesson', 4, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u08, 'y1s1_语文_u08_l05', '语文园地八', '/语文（统编版）/第八单元：课文/语文园地八', 2, 'lesson', 5, '{"volumeKey": "y1s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_语文_root, 'y1s1_语文_u09', '附录', '/语文（统编版）/附录', 1, 'unit', 9, '{"volumeKey": "y1s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y1s1_语文_u09 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_语文_u09, 'y1s1_语文_u09_l01', '识字表', '/语文（统编版）/附录/识字表', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u09, 'y1s1_语文_u09_l02', '写字表', '/语文（统编版）/附录/写字表', 2, 'lesson', 2, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u09, 'y1s1_语文_u09_l03', '常用笔画名称表', '/语文（统编版）/附录/常用笔画名称表', 2, 'lesson', 3, '{"volumeKey": "y1s1", "subject": "语文"}', 1),
(@scheme_id, @y1s1_语文_u09, 'y1s1_语文_u09_l04', '常用偏旁名称表', '/语文（统编版）/附录/常用偏旁名称表', 2, 'lesson', 4, '{"volumeKey": "y1s1", "subject": "语文"}', 1);

-- y1s1 数学（人教版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y1s1_数学_root', '数学（人教版）', '/数学（人教版）', 0, 'folder', 2, '{"volumeKey": "y1s1", "subject": "数学", "edition": "人教版", "defaultModule": "同步备课"}', 1);
SET @y1s1_数学_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_数学_root, 'y1s1_数学_u01', '1.准备课', '/数学（人教版）/1.准备课', 1, 'unit', 1, '{"volumeKey": "y1s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y1s1_数学_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_数学_u01, 'y1s1_数学_u01_l01', '数一数', '/数学（人教版）/1.准备课/数一数', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "数学"}', 1),
(@scheme_id, @y1s1_数学_u01, 'y1s1_数学_u01_l02', '比多少', '/数学（人教版）/1.准备课/比多少', 2, 'lesson', 2, '{"volumeKey": "y1s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_数学_root, 'y1s1_数学_u02', '2.位置', '/数学（人教版）/2.位置', 1, 'unit', 2, '{"volumeKey": "y1s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y1s1_数学_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_数学_u02, 'y1s1_数学_u02_l01', '上下前后', '/数学（人教版）/2.位置/上下前后', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "数学"}', 1),
(@scheme_id, @y1s1_数学_u02, 'y1s1_数学_u02_l02', '左右', '/数学（人教版）/2.位置/左右', 2, 'lesson', 2, '{"volumeKey": "y1s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_数学_root, 'y1s1_数学_u03', '3.1-5认识与加减法', '/数学（人教版）/3.1-5认识与加减法', 1, 'unit', 3, '{"volumeKey": "y1s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y1s1_数学_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_数学_u03, 'y1s1_数学_u03_l01', '认识数字', '/数学（人教版）/3.1-5认识与加减法/认识数字', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "数学"}', 1),
(@scheme_id, @y1s1_数学_u03, 'y1s1_数学_u03_l02', '比大小', '/数学（人教版）/3.1-5认识与加减法/比大小', 2, 'lesson', 2, '{"volumeKey": "y1s1", "subject": "数学"}', 1),
(@scheme_id, @y1s1_数学_u03, 'y1s1_数学_u03_l03', '分与合', '/数学（人教版）/3.1-5认识与加减法/分与合', 2, 'lesson', 3, '{"volumeKey": "y1s1", "subject": "数学"}', 1),
(@scheme_id, @y1s1_数学_u03, 'y1s1_数学_u03_l04', '加减运算', '/数学（人教版）/3.1-5认识与加减法/加减运算', 2, 'lesson', 4, '{"volumeKey": "y1s1", "subject": "数学"}', 1),
(@scheme_id, @y1s1_数学_u03, 'y1s1_数学_u03_l05', '0的运算', '/数学（人教版）/3.1-5认识与加减法/0的运算', 2, 'lesson', 5, '{"volumeKey": "y1s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_数学_root, 'y1s1_数学_u04', '4.认识图形', '/数学（人教版）/4.认识图形', 1, 'unit', 4, '{"volumeKey": "y1s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y1s1_数学_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_数学_u04, 'y1s1_数学_u04_l01', '长方体', '/数学（人教版）/4.认识图形/长方体', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "数学"}', 1),
(@scheme_id, @y1s1_数学_u04, 'y1s1_数学_u04_l02', '正方体', '/数学（人教版）/4.认识图形/正方体', 2, 'lesson', 2, '{"volumeKey": "y1s1", "subject": "数学"}', 1),
(@scheme_id, @y1s1_数学_u04, 'y1s1_数学_u04_l03', '圆柱', '/数学（人教版）/4.认识图形/圆柱', 2, 'lesson', 3, '{"volumeKey": "y1s1", "subject": "数学"}', 1),
(@scheme_id, @y1s1_数学_u04, 'y1s1_数学_u04_l04', '球', '/数学（人教版）/4.认识图形/球', 2, 'lesson', 4, '{"volumeKey": "y1s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_数学_root, 'y1s1_数学_u05', '5.6-10认识与加减法', '/数学（人教版）/5.6-10认识与加减法', 1, 'unit', 5, '{"volumeKey": "y1s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y1s1_数学_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_数学_u05, 'y1s1_数学_u05_l01', '6-10认识', '/数学（人教版）/5.6-10认识与加减法/6-10认识', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "数学"}', 1),
(@scheme_id, @y1s1_数学_u05, 'y1s1_数学_u05_l02', '连加连减', '/数学（人教版）/5.6-10认识与加减法/连加连减', 2, 'lesson', 2, '{"volumeKey": "y1s1", "subject": "数学"}', 1),
(@scheme_id, @y1s1_数学_u05, 'y1s1_数学_u05_l03', '加减混合', '/数学（人教版）/5.6-10认识与加减法/加减混合', 2, 'lesson', 3, '{"volumeKey": "y1s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_数学_root, 'y1s1_数学_u06', '6.11-20各数认识', '/数学（人教版）/6.11-20各数认识', 1, 'unit', 6, '{"volumeKey": "y1s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y1s1_数学_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_数学_u06, 'y1s1_数学_u06_l01', '数的认读', '/数学（人教版）/6.11-20各数认识/数的认读', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "数学"}', 1),
(@scheme_id, @y1s1_数学_u06, 'y1s1_数学_u06_l02', '十加几运算', '/数学（人教版）/6.11-20各数认识/十加几运算', 2, 'lesson', 2, '{"volumeKey": "y1s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_数学_root, 'y1s1_数学_u07', '7.认识钟表', '/数学（人教版）/7.认识钟表', 1, 'unit', 7, '{"volumeKey": "y1s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y1s1_数学_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_数学_u07, 'y1s1_数学_u07_l01', '认识整时', '/数学（人教版）/7.认识钟表/认识整时', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_数学_root, 'y1s1_数学_u08', '8.20以内进位加法', '/数学（人教版）/8.20以内进位加法', 1, 'unit', 8, '{"volumeKey": "y1s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y1s1_数学_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_数学_u08, 'y1s1_数学_u08_l01', '9加几', '/数学（人教版）/8.20以内进位加法/9加几', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "数学"}', 1),
(@scheme_id, @y1s1_数学_u08, 'y1s1_数学_u08_l02', '8、7、6加几', '/数学（人教版）/8.20以内进位加法/8、7、6加几', 2, 'lesson', 2, '{"volumeKey": "y1s1", "subject": "数学"}', 1),
(@scheme_id, @y1s1_数学_u08, 'y1s1_数学_u08_l03', '5、4、3、2加几', '/数学（人教版）/8.20以内进位加法/5、4、3、2加几', 2, 'lesson', 3, '{"volumeKey": "y1s1", "subject": "数学"}', 1);

-- y1s1 英语（人教PEP）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y1s1_英语_root', '英语（人教PEP）', '/英语（人教PEP）', 0, 'folder', 3, '{"volumeKey": "y1s1", "subject": "英语", "edition": "人教PEP", "defaultModule": "同步备课"}', 1);
SET @y1s1_英语_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_英语_root, 'y1s1_英语_u01', 'Unit1 School', '/英语（人教PEP）/Unit1 School', 1, 'unit', 1, '{"volumeKey": "y1s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y1s1_英语_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_英语_u01, 'y1s1_英语_u01_l01', '教室物品', '/英语（人教PEP）/Unit1 School/教室物品', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_英语_root, 'y1s1_英语_u02', 'Unit2 Face', '/英语（人教PEP）/Unit2 Face', 1, 'unit', 2, '{"volumeKey": "y1s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y1s1_英语_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_英语_u02, 'y1s1_英语_u02_l01', '身体五官', '/英语（人教PEP）/Unit2 Face/身体五官', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_英语_root, 'y1s1_英语_u03', 'Unit3 Animals', '/英语（人教PEP）/Unit3 Animals', 1, 'unit', 3, '{"volumeKey": "y1s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y1s1_英语_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_英语_u03, 'y1s1_英语_u03_l01', '小动物', '/英语（人教PEP）/Unit3 Animals/小动物', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_英语_root, 'y1s1_英语_u04', 'Unit4 Numbers', '/英语（人教PEP）/Unit4 Numbers', 1, 'unit', 4, '{"volumeKey": "y1s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y1s1_英语_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_英语_u04, 'y1s1_英语_u04_l01', '数字1-10', '/英语（人教PEP）/Unit4 Numbers/数字1-10', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_英语_root, 'y1s1_英语_u05', 'Unit5 Colours', '/英语（人教PEP）/Unit5 Colours', 1, 'unit', 5, '{"volumeKey": "y1s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y1s1_英语_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_英语_u05, 'y1s1_英语_u05_l01', '颜色', '/英语（人教PEP）/Unit5 Colours/颜色', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_英语_root, 'y1s1_英语_u06', 'Unit6 Fruit', '/英语（人教PEP）/Unit6 Fruit', 1, 'unit', 6, '{"volumeKey": "y1s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y1s1_英语_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s1_英语_u06, 'y1s1_英语_u06_l01', '水果', '/英语（人教PEP）/Unit6 Fruit/水果', 2, 'lesson', 1, '{"volumeKey": "y1s1", "subject": "英语"}', 1);

-- y1s2 语文（统编版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y1s2_语文_root', '语文（统编版）', '/语文（统编版）', 0, 'folder', 1, '{"volumeKey": "y1s2", "subject": "语文", "edition": "统编版", "defaultModule": "同步备课"}', 1);
SET @y1s2_语文_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_语文_root, 'y1s2_语文_u01', '第一单元：识字', '/语文（统编版）/第一单元：识字', 1, 'unit', 1, '{"volumeKey": "y1s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y1s2_语文_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_语文_u01, 'y1s2_语文_u01_l01', '1 春夏秋冬', '/语文（统编版）/第一单元：识字/1 春夏秋冬', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u01, 'y1s2_语文_u01_l02', '2 姓氏歌', '/语文（统编版）/第一单元：识字/2 姓氏歌', 2, 'lesson', 2, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u01, 'y1s2_语文_u01_l03', '3 小青蛙', '/语文（统编版）/第一单元：识字/3 小青蛙', 2, 'lesson', 3, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u01, 'y1s2_语文_u01_l04', '4 猜字谜', '/语文（统编版）/第一单元：识字/4 猜字谜', 2, 'lesson', 4, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u01, 'y1s2_语文_u01_l05', '口语交际：听故事，讲故事', '/语文（统编版）/第一单元：识字/口语交际：听故事，讲故事', 2, 'lesson', 5, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u01, 'y1s2_语文_u01_l06', '语文园地一', '/语文（统编版）/第一单元：识字/语文园地一', 2, 'lesson', 6, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u01, 'y1s2_语文_u01_l07', '快乐读书吧：读读童谣和儿歌', '/语文（统编版）/第一单元：识字/快乐读书吧：读读童谣和儿歌', 2, 'lesson', 7, '{"volumeKey": "y1s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_语文_root, 'y1s2_语文_u02', '第二单元：课文', '/语文（统编版）/第二单元：课文', 1, 'unit', 2, '{"volumeKey": "y1s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y1s2_语文_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_语文_u02, 'y1s2_语文_u02_l01', '1 吃水不忘挖井人', '/语文（统编版）/第二单元：课文/1 吃水不忘挖井人', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u02, 'y1s2_语文_u02_l02', '2 我多想去看看', '/语文（统编版）/第二单元：课文/2 我多想去看看', 2, 'lesson', 2, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u02, 'y1s2_语文_u02_l03', '3 一个接一个', '/语文（统编版）/第二单元：课文/3 一个接一个', 2, 'lesson', 3, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u02, 'y1s2_语文_u02_l04', '4 四个太阳', '/语文（统编版）/第二单元：课文/4 四个太阳', 2, 'lesson', 4, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u02, 'y1s2_语文_u02_l05', '语文园地二', '/语文（统编版）/第二单元：课文/语文园地二', 2, 'lesson', 5, '{"volumeKey": "y1s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_语文_root, 'y1s2_语文_u03', '第三单元：课文', '/语文（统编版）/第三单元：课文', 1, 'unit', 3, '{"volumeKey": "y1s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y1s2_语文_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_语文_u03, 'y1s2_语文_u03_l01', '5 小公鸡和小鸭子', '/语文（统编版）/第三单元：课文/5 小公鸡和小鸭子', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u03, 'y1s2_语文_u03_l02', '6 树和喜鹊', '/语文（统编版）/第三单元：课文/6 树和喜鹊', 2, 'lesson', 2, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u03, 'y1s2_语文_u03_l03', '7 怎么都快乐', '/语文（统编版）/第三单元：课文/7 怎么都快乐', 2, 'lesson', 3, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u03, 'y1s2_语文_u03_l04', '口语交际：请你帮个忙', '/语文（统编版）/第三单元：课文/口语交际：请你帮个忙', 2, 'lesson', 4, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u03, 'y1s2_语文_u03_l05', '语文园地三', '/语文（统编版）/第三单元：课文/语文园地三', 2, 'lesson', 5, '{"volumeKey": "y1s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_语文_root, 'y1s2_语文_u04', '第四单元：课文', '/语文（统编版）/第四单元：课文', 1, 'unit', 4, '{"volumeKey": "y1s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y1s2_语文_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_语文_u04, 'y1s2_语文_u04_l01', '8 静夜思', '/语文（统编版）/第四单元：课文/8 静夜思', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u04, 'y1s2_语文_u04_l02', '9 夜色', '/语文（统编版）/第四单元：课文/9 夜色', 2, 'lesson', 2, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u04, 'y1s2_语文_u04_l03', '10 端午粽', '/语文（统编版）/第四单元：课文/10 端午粽', 2, 'lesson', 3, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u04, 'y1s2_语文_u04_l04', '11 彩虹', '/语文（统编版）/第四单元：课文/11 彩虹', 2, 'lesson', 4, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u04, 'y1s2_语文_u04_l05', '语文园地四', '/语文（统编版）/第四单元：课文/语文园地四', 2, 'lesson', 5, '{"volumeKey": "y1s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_语文_root, 'y1s2_语文_u05', '第五单元：识字', '/语文（统编版）/第五单元：识字', 1, 'unit', 5, '{"volumeKey": "y1s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y1s2_语文_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_语文_u05, 'y1s2_语文_u05_l01', '5 动物儿歌', '/语文（统编版）/第五单元：识字/5 动物儿歌', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u05, 'y1s2_语文_u05_l02', '6 古对今', '/语文（统编版）/第五单元：识字/6 古对今', 2, 'lesson', 2, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u05, 'y1s2_语文_u05_l03', '7 操场上', '/语文（统编版）/第五单元：识字/7 操场上', 2, 'lesson', 3, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u05, 'y1s2_语文_u05_l04', '8 人之初', '/语文（统编版）/第五单元：识字/8 人之初', 2, 'lesson', 4, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u05, 'y1s2_语文_u05_l05', '口语交际：打电话', '/语文（统编版）/第五单元：识字/口语交际：打电话', 2, 'lesson', 5, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u05, 'y1s2_语文_u05_l06', '语文园地五', '/语文（统编版）/第五单元：识字/语文园地五', 2, 'lesson', 6, '{"volumeKey": "y1s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_语文_root, 'y1s2_语文_u06', '第六单元：课文', '/语文（统编版）/第六单元：课文', 1, 'unit', 6, '{"volumeKey": "y1s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y1s2_语文_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_语文_u06, 'y1s2_语文_u06_l01', '12 古诗二首（池上、小池）', '/语文（统编版）/第六单元：课文/12 古诗二首（池上、小池）', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u06, 'y1s2_语文_u06_l02', '13 荷叶圆圆', '/语文（统编版）/第六单元：课文/13 荷叶圆圆', 2, 'lesson', 2, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u06, 'y1s2_语文_u06_l03', '14 要下雨了', '/语文（统编版）/第六单元：课文/14 要下雨了', 2, 'lesson', 3, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u06, 'y1s2_语文_u06_l04', '语文园地六', '/语文（统编版）/第六单元：课文/语文园地六', 2, 'lesson', 4, '{"volumeKey": "y1s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_语文_root, 'y1s2_语文_u07', '第七单元：课文', '/语文（统编版）/第七单元：课文', 1, 'unit', 7, '{"volumeKey": "y1s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y1s2_语文_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_语文_u07, 'y1s2_语文_u07_l01', '15 文具的家', '/语文（统编版）/第七单元：课文/15 文具的家', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u07, 'y1s2_语文_u07_l02', '16 一分钟', '/语文（统编版）/第七单元：课文/16 一分钟', 2, 'lesson', 2, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u07, 'y1s2_语文_u07_l03', '17 动物王国开大会', '/语文（统编版）/第七单元：课文/17 动物王国开大会', 2, 'lesson', 3, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u07, 'y1s2_语文_u07_l04', '18 小猴子下山', '/语文（统编版）/第七单元：课文/18 小猴子下山', 2, 'lesson', 4, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u07, 'y1s2_语文_u07_l05', '口语交际：一起做游戏', '/语文（统编版）/第七单元：课文/口语交际：一起做游戏', 2, 'lesson', 5, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u07, 'y1s2_语文_u07_l06', '语文园地七', '/语文（统编版）/第七单元：课文/语文园地七', 2, 'lesson', 6, '{"volumeKey": "y1s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_语文_root, 'y1s2_语文_u08', '第八单元：课文', '/语文（统编版）/第八单元：课文', 1, 'unit', 8, '{"volumeKey": "y1s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y1s2_语文_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_语文_u08, 'y1s2_语文_u08_l01', '19 棉花姑娘', '/语文（统编版）/第八单元：课文/19 棉花姑娘', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u08, 'y1s2_语文_u08_l02', '20 咕咚', '/语文（统编版）/第八单元：课文/20 咕咚', 2, 'lesson', 2, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u08, 'y1s2_语文_u08_l03', '21 小壁虎借尾巴', '/语文（统编版）/第八单元：课文/21 小壁虎借尾巴', 2, 'lesson', 3, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u08, 'y1s2_语文_u08_l04', '语文园地八', '/语文（统编版）/第八单元：课文/语文园地八', 2, 'lesson', 4, '{"volumeKey": "y1s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_语文_root, 'y1s2_语文_u09', '附录', '/语文（统编版）/附录', 1, 'unit', 9, '{"volumeKey": "y1s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y1s2_语文_u09 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_语文_u09, 'y1s2_语文_u09_l01', '识字表', '/语文（统编版）/附录/识字表', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u09, 'y1s2_语文_u09_l02', '写字表', '/语文（统编版）/附录/写字表', 2, 'lesson', 2, '{"volumeKey": "y1s2", "subject": "语文"}', 1),
(@scheme_id, @y1s2_语文_u09, 'y1s2_语文_u09_l03', '常用偏旁名称表', '/语文（统编版）/附录/常用偏旁名称表', 2, 'lesson', 3, '{"volumeKey": "y1s2", "subject": "语文"}', 1);

-- y1s2 数学（人教版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y1s2_数学_root', '数学（人教版）', '/数学（人教版）', 0, 'folder', 2, '{"volumeKey": "y1s2", "subject": "数学", "edition": "人教版", "defaultModule": "同步备课"}', 1);
SET @y1s2_数学_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_数学_root, 'y1s2_数学_u01', '1.认识平面图形', '/数学（人教版）/1.认识平面图形', 1, 'unit', 1, '{"volumeKey": "y1s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y1s2_数学_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_数学_u01, 'y1s2_数学_u01_l01', '长方形', '/数学（人教版）/1.认识平面图形/长方形', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "数学"}', 1),
(@scheme_id, @y1s2_数学_u01, 'y1s2_数学_u01_l02', '正方形', '/数学（人教版）/1.认识平面图形/正方形', 2, 'lesson', 2, '{"volumeKey": "y1s2", "subject": "数学"}', 1),
(@scheme_id, @y1s2_数学_u01, 'y1s2_数学_u01_l03', '圆', '/数学（人教版）/1.认识平面图形/圆', 2, 'lesson', 3, '{"volumeKey": "y1s2", "subject": "数学"}', 1),
(@scheme_id, @y1s2_数学_u01, 'y1s2_数学_u01_l04', '平行四边形', '/数学（人教版）/1.认识平面图形/平行四边形', 2, 'lesson', 4, '{"volumeKey": "y1s2", "subject": "数学"}', 1),
(@scheme_id, @y1s2_数学_u01, 'y1s2_数学_u01_l05', '三角形', '/数学（人教版）/1.认识平面图形/三角形', 2, 'lesson', 5, '{"volumeKey": "y1s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_数学_root, 'y1s2_数学_u02', '2.20以内退位减法', '/数学（人教版）/2.20以内退位减法', 1, 'unit', 2, '{"volumeKey": "y1s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y1s2_数学_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_数学_u02, 'y1s2_数学_u02_l01', '十几减9', '/数学（人教版）/2.20以内退位减法/十几减9', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "数学"}', 1),
(@scheme_id, @y1s2_数学_u02, 'y1s2_数学_u02_l02', '十几减8、7、6', '/数学（人教版）/2.20以内退位减法/十几减8、7、6', 2, 'lesson', 2, '{"volumeKey": "y1s2", "subject": "数学"}', 1),
(@scheme_id, @y1s2_数学_u02, 'y1s2_数学_u02_l03', '十几减5、4、3、2', '/数学（人教版）/2.20以内退位减法/十几减5、4、3、2', 2, 'lesson', 3, '{"volumeKey": "y1s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_数学_root, 'y1s2_数学_u03', '3.分类与整理', '/数学（人教版）/3.分类与整理', 1, 'unit', 3, '{"volumeKey": "y1s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y1s2_数学_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_数学_u03, 'y1s2_数学_u03_l01', '分类与整理', '/数学（人教版）/3.分类与整理/分类与整理', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_数学_root, 'y1s2_数学_u04', '4.100以内数的认识', '/数学（人教版）/4.100以内数的认识', 1, 'unit', 4, '{"volumeKey": "y1s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y1s2_数学_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_数学_u04, 'y1s2_数学_u04_l01', '数数', '/数学（人教版）/4.100以内数的认识/数数', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "数学"}', 1),
(@scheme_id, @y1s2_数学_u04, 'y1s2_数学_u04_l02', '读写', '/数学（人教版）/4.100以内数的认识/读写', 2, 'lesson', 2, '{"volumeKey": "y1s2", "subject": "数学"}', 1),
(@scheme_id, @y1s2_数学_u04, 'y1s2_数学_u04_l03', '大小比较', '/数学（人教版）/4.100以内数的认识/大小比较', 2, 'lesson', 3, '{"volumeKey": "y1s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_数学_root, 'y1s2_数学_u05', '5.认识人民币', '/数学（人教版）/5.认识人民币', 1, 'unit', 5, '{"volumeKey": "y1s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y1s2_数学_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_数学_u05, 'y1s2_数学_u05_l01', '元角分换算', '/数学（人教版）/5.认识人民币/元角分换算', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "数学"}', 1),
(@scheme_id, @y1s2_数学_u05, 'y1s2_数学_u05_l02', '简单购物计算', '/数学（人教版）/5.认识人民币/简单购物计算', 2, 'lesson', 2, '{"volumeKey": "y1s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_数学_root, 'y1s2_数学_u06', '6.100以内加减法一', '/数学（人教版）/6.100以内加减法一', 1, 'unit', 6, '{"volumeKey": "y1s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y1s2_数学_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_数学_u06, 'y1s2_数学_u06_l01', '整十数加减', '/数学（人教版）/6.100以内加减法一/整十数加减', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "数学"}', 1),
(@scheme_id, @y1s2_数学_u06, 'y1s2_数学_u06_l02', '两位数加减', '/数学（人教版）/6.100以内加减法一/两位数加减', 2, 'lesson', 2, '{"volumeKey": "y1s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_数学_root, 'y1s2_数学_u07', '7.找规律', '/数学（人教版）/7.找规律', 1, 'unit', 7, '{"volumeKey": "y1s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y1s2_数学_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_数学_u07, 'y1s2_数学_u07_l01', '找规律', '/数学（人教版）/7.找规律/找规律', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "数学"}', 1);

-- y1s2 英语（人教PEP）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y1s2_英语_root', '英语（人教PEP）', '/英语（人教PEP）', 0, 'folder', 3, '{"volumeKey": "y1s2", "subject": "英语", "edition": "人教PEP", "defaultModule": "同步备课"}', 1);
SET @y1s2_英语_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_英语_root, 'y1s2_英语_u01', 'Unit1 Classroom', '/英语（人教PEP）/Unit1 Classroom', 1, 'unit', 1, '{"volumeKey": "y1s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y1s2_英语_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_英语_u01, 'y1s2_英语_u01_l01', '教室设施', '/英语（人教PEP）/Unit1 Classroom/教室设施', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_英语_root, 'y1s2_英语_u02', 'Unit2 Schoolbag', '/英语（人教PEP）/Unit2 Schoolbag', 1, 'unit', 2, '{"volumeKey": "y1s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y1s2_英语_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_英语_u02, 'y1s2_英语_u02_l01', '文具', '/英语（人教PEP）/Unit2 Schoolbag/文具', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_英语_root, 'y1s2_英语_u03', 'Unit3 Animals', '/英语（人教PEP）/Unit3 Animals', 1, 'unit', 3, '{"volumeKey": "y1s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y1s2_英语_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_英语_u03, 'y1s2_英语_u03_l01', '陆地动物', '/英语（人教PEP）/Unit3 Animals/陆地动物', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_英语_root, 'y1s2_英语_u04', 'Unit4 Food', '/英语（人教PEP）/Unit4 Food', 1, 'unit', 4, '{"volumeKey": "y1s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y1s2_英语_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_英语_u04, 'y1s2_英语_u04_l01', '主食零食', '/英语（人教PEP）/Unit4 Food/主食零食', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_英语_root, 'y1s2_英语_u05', 'Unit5 Drink', '/英语（人教PEP）/Unit5 Drink', 1, 'unit', 5, '{"volumeKey": "y1s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y1s2_英语_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_英语_u05, 'y1s2_英语_u05_l01', '饮品', '/英语（人教PEP）/Unit5 Drink/饮品', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_英语_root, 'y1s2_英语_u06', 'Unit6 Clothes', '/英语（人教PEP）/Unit6 Clothes', 1, 'unit', 6, '{"volumeKey": "y1s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y1s2_英语_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y1s2_英语_u06, 'y1s2_英语_u06_l01', '服饰', '/英语（人教PEP）/Unit6 Clothes/服饰', 2, 'lesson', 1, '{"volumeKey": "y1s2", "subject": "英语"}', 1);

-- y2s1 语文（统编版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y2s1_语文_root', '语文（统编版）', '/语文（统编版）', 0, 'folder', 1, '{"volumeKey": "y2s1", "subject": "语文", "edition": "统编版", "defaultModule": "同步备课"}', 1);
SET @y2s1_语文_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_语文_root, 'y2s1_语文_u01', '第一单元：课文', '/语文（统编版）/第一单元：课文', 1, 'unit', 1, '{"volumeKey": "y2s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y2s1_语文_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_语文_u01, 'y2s1_语文_u01_l01', '1 小蝌蚪找妈妈', '/语文（统编版）/第一单元：课文/1 小蝌蚪找妈妈', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u01, 'y2s1_语文_u01_l02', '2 我是什么', '/语文（统编版）/第一单元：课文/2 我是什么', 2, 'lesson', 2, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u01, 'y2s1_语文_u01_l03', '3 植物妈妈有办法', '/语文（统编版）/第一单元：课文/3 植物妈妈有办法', 2, 'lesson', 3, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u01, 'y2s1_语文_u01_l04', '口语交际：有趣的动物', '/语文（统编版）/第一单元：课文/口语交际：有趣的动物', 2, 'lesson', 4, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u01, 'y2s1_语文_u01_l05', '语文园地一', '/语文（统编版）/第一单元：课文/语文园地一', 2, 'lesson', 5, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u01, 'y2s1_语文_u01_l06', '快乐读书吧：读读儿童故事', '/语文（统编版）/第一单元：课文/快乐读书吧：读读儿童故事', 2, 'lesson', 6, '{"volumeKey": "y2s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_语文_root, 'y2s1_语文_u02', '第二单元：识字', '/语文（统编版）/第二单元：识字', 1, 'unit', 2, '{"volumeKey": "y2s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y2s1_语文_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_语文_u02, 'y2s1_语文_u02_l01', '1 场景歌', '/语文（统编版）/第二单元：识字/1 场景歌', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u02, 'y2s1_语文_u02_l02', '2 树之歌', '/语文（统编版）/第二单元：识字/2 树之歌', 2, 'lesson', 2, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u02, 'y2s1_语文_u02_l03', '3 拍手歌', '/语文（统编版）/第二单元：识字/3 拍手歌', 2, 'lesson', 3, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u02, 'y2s1_语文_u02_l04', '4 田家四季歌', '/语文（统编版）/第二单元：识字/4 田家四季歌', 2, 'lesson', 4, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u02, 'y2s1_语文_u02_l05', '语文园地二', '/语文（统编版）/第二单元：识字/语文园地二', 2, 'lesson', 5, '{"volumeKey": "y2s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_语文_root, 'y2s1_语文_u03', '第三单元：课文', '/语文（统编版）/第三单元：课文', 1, 'unit', 3, '{"volumeKey": "y2s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y2s1_语文_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_语文_u03, 'y2s1_语文_u03_l01', '4 曹冲称象', '/语文（统编版）/第三单元：课文/4 曹冲称象', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u03, 'y2s1_语文_u03_l02', '5 玲玲的画', '/语文（统编版）/第三单元：课文/5 玲玲的画', 2, 'lesson', 2, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u03, 'y2s1_语文_u03_l03', '6 一封信', '/语文（统编版）/第三单元：课文/6 一封信', 2, 'lesson', 3, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u03, 'y2s1_语文_u03_l04', '7 妈妈睡了', '/语文（统编版）/第三单元：课文/7 妈妈睡了', 2, 'lesson', 4, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u03, 'y2s1_语文_u03_l05', '口语交际：做手工', '/语文（统编版）/第三单元：课文/口语交际：做手工', 2, 'lesson', 5, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u03, 'y2s1_语文_u03_l06', '语文园地三', '/语文（统编版）/第三单元：课文/语文园地三', 2, 'lesson', 6, '{"volumeKey": "y2s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_语文_root, 'y2s1_语文_u04', '第四单元：课文', '/语文（统编版）/第四单元：课文', 1, 'unit', 4, '{"volumeKey": "y2s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y2s1_语文_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_语文_u04, 'y2s1_语文_u04_l01', '8 古诗二首（登鹳雀楼、望庐山瀑布）', '/语文（统编版）/第四单元：课文/8 古诗二首（登鹳雀楼、望庐山瀑布）', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u04, 'y2s1_语文_u04_l02', '9 黄山奇石', '/语文（统编版）/第四单元：课文/9 黄山奇石', 2, 'lesson', 2, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u04, 'y2s1_语文_u04_l03', '10 日月潭', '/语文（统编版）/第四单元：课文/10 日月潭', 2, 'lesson', 3, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u04, 'y2s1_语文_u04_l04', '11 葡萄沟', '/语文（统编版）/第四单元：课文/11 葡萄沟', 2, 'lesson', 4, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u04, 'y2s1_语文_u04_l05', '语文园地四', '/语文（统编版）/第四单元：课文/语文园地四', 2, 'lesson', 5, '{"volumeKey": "y2s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_语文_root, 'y2s1_语文_u05', '第五单元：课文', '/语文（统编版）/第五单元：课文', 1, 'unit', 5, '{"volumeKey": "y2s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y2s1_语文_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_语文_u05, 'y2s1_语文_u05_l01', '12 坐井观天', '/语文（统编版）/第五单元：课文/12 坐井观天', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u05, 'y2s1_语文_u05_l02', '13 寒号鸟', '/语文（统编版）/第五单元：课文/13 寒号鸟', 2, 'lesson', 2, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u05, 'y2s1_语文_u05_l03', '14 我要的是葫芦', '/语文（统编版）/第五单元：课文/14 我要的是葫芦', 2, 'lesson', 3, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u05, 'y2s1_语文_u05_l04', '口语交际：商量', '/语文（统编版）/第五单元：课文/口语交际：商量', 2, 'lesson', 4, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u05, 'y2s1_语文_u05_l05', '语文园地五', '/语文（统编版）/第五单元：课文/语文园地五', 2, 'lesson', 5, '{"volumeKey": "y2s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_语文_root, 'y2s1_语文_u06', '第六单元：课文', '/语文（统编版）/第六单元：课文', 1, 'unit', 6, '{"volumeKey": "y2s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y2s1_语文_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_语文_u06, 'y2s1_语文_u06_l01', '15 大禹治水', '/语文（统编版）/第六单元：课文/15 大禹治水', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u06, 'y2s1_语文_u06_l02', '16 朱德的扁担', '/语文（统编版）/第六单元：课文/16 朱德的扁担', 2, 'lesson', 2, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u06, 'y2s1_语文_u06_l03', '17 难忘的泼水节', '/语文（统编版）/第六单元：课文/17 难忘的泼水节', 2, 'lesson', 3, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u06, 'y2s1_语文_u06_l04', '口语交际：看图讲故事', '/语文（统编版）/第六单元：课文/口语交际：看图讲故事', 2, 'lesson', 4, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u06, 'y2s1_语文_u06_l05', '语文园地六', '/语文（统编版）/第六单元：课文/语文园地六', 2, 'lesson', 5, '{"volumeKey": "y2s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_语文_root, 'y2s1_语文_u07', '第七单元：课文', '/语文（统编版）/第七单元：课文', 1, 'unit', 7, '{"volumeKey": "y2s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y2s1_语文_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_语文_u07, 'y2s1_语文_u07_l01', '18 古诗二首（夜宿山寺、敕勒歌）', '/语文（统编版）/第七单元：课文/18 古诗二首（夜宿山寺、敕勒歌）', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u07, 'y2s1_语文_u07_l02', '19 雾在哪里', '/语文（统编版）/第七单元：课文/19 雾在哪里', 2, 'lesson', 2, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u07, 'y2s1_语文_u07_l03', '20 雪孩子', '/语文（统编版）/第七单元：课文/20 雪孩子', 2, 'lesson', 3, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u07, 'y2s1_语文_u07_l04', '语文园地七', '/语文（统编版）/第七单元：课文/语文园地七', 2, 'lesson', 4, '{"volumeKey": "y2s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_语文_root, 'y2s1_语文_u08', '第八单元：课文', '/语文（统编版）/第八单元：课文', 1, 'unit', 8, '{"volumeKey": "y2s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y2s1_语文_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_语文_u08, 'y2s1_语文_u08_l01', '21 狐假虎威', '/语文（统编版）/第八单元：课文/21 狐假虎威', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u08, 'y2s1_语文_u08_l02', '22 狐狸分奶酪', '/语文（统编版）/第八单元：课文/22 狐狸分奶酪', 2, 'lesson', 2, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u08, 'y2s1_语文_u08_l03', '23 纸船和风筝', '/语文（统编版）/第八单元：课文/23 纸船和风筝', 2, 'lesson', 3, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u08, 'y2s1_语文_u08_l04', '24 风娃娃', '/语文（统编版）/第八单元：课文/24 风娃娃', 2, 'lesson', 4, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u08, 'y2s1_语文_u08_l05', '语文园地八', '/语文（统编版）/第八单元：课文/语文园地八', 2, 'lesson', 5, '{"volumeKey": "y2s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_语文_root, 'y2s1_语文_u09', '附录', '/语文（统编版）/附录', 1, 'unit', 9, '{"volumeKey": "y2s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y2s1_语文_u09 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_语文_u09, 'y2s1_语文_u09_l01', '识字表', '/语文（统编版）/附录/识字表', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u09, 'y2s1_语文_u09_l02', '写字表', '/语文（统编版）/附录/写字表', 2, 'lesson', 2, '{"volumeKey": "y2s1", "subject": "语文"}', 1),
(@scheme_id, @y2s1_语文_u09, 'y2s1_语文_u09_l03', '词语表', '/语文（统编版）/附录/词语表', 2, 'lesson', 3, '{"volumeKey": "y2s1", "subject": "语文"}', 1);

-- y2s1 数学（人教版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y2s1_数学_root', '数学（人教版）', '/数学（人教版）', 0, 'folder', 2, '{"volumeKey": "y2s1", "subject": "数学", "edition": "人教版", "defaultModule": "同步备课"}', 1);
SET @y2s1_数学_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_数学_root, 'y2s1_数学_u01', '1.长度单位', '/数学（人教版）/1.长度单位', 1, 'unit', 1, '{"volumeKey": "y2s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y2s1_数学_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_数学_u01, 'y2s1_数学_u01_l01', '厘米', '/数学（人教版）/1.长度单位/厘米', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "数学"}', 1),
(@scheme_id, @y2s1_数学_u01, 'y2s1_数学_u01_l02', '米', '/数学（人教版）/1.长度单位/米', 2, 'lesson', 2, '{"volumeKey": "y2s1", "subject": "数学"}', 1),
(@scheme_id, @y2s1_数学_u01, 'y2s1_数学_u01_l03', '线段', '/数学（人教版）/1.长度单位/线段', 2, 'lesson', 3, '{"volumeKey": "y2s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_数学_root, 'y2s1_数学_u02', '2.100以内进退位加减法', '/数学（人教版）/2.100以内进退位加减法', 1, 'unit', 2, '{"volumeKey": "y2s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y2s1_数学_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_数学_u02, 'y2s1_数学_u02_l01', '100以内进退位加减法', '/数学（人教版）/2.100以内进退位加减法/100以内进退位加减法', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_数学_root, 'y2s1_数学_u03', '3.角的初步认识', '/数学（人教版）/3.角的初步认识', 1, 'unit', 3, '{"volumeKey": "y2s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y2s1_数学_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_数学_u03, 'y2s1_数学_u03_l01', '直角', '/数学（人教版）/3.角的初步认识/直角', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "数学"}', 1),
(@scheme_id, @y2s1_数学_u03, 'y2s1_数学_u03_l02', '锐角', '/数学（人教版）/3.角的初步认识/锐角', 2, 'lesson', 2, '{"volumeKey": "y2s1", "subject": "数学"}', 1),
(@scheme_id, @y2s1_数学_u03, 'y2s1_数学_u03_l03', '钝角', '/数学（人教版）/3.角的初步认识/钝角', 2, 'lesson', 3, '{"volumeKey": "y2s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_数学_root, 'y2s1_数学_u04', '4.表内乘法一', '/数学（人教版）/4.表内乘法一', 1, 'unit', 4, '{"volumeKey": "y2s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y2s1_数学_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_数学_u04, 'y2s1_数学_u04_l01', '1-6乘法口诀', '/数学（人教版）/4.表内乘法一/1-6乘法口诀', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_数学_root, 'y2s1_数学_u05', '5.观察物体一', '/数学（人教版）/5.观察物体一', 1, 'unit', 5, '{"volumeKey": "y2s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y2s1_数学_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_数学_u05, 'y2s1_数学_u05_l01', '观察物体', '/数学（人教版）/5.观察物体一/观察物体', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_数学_root, 'y2s1_数学_u06', '6.表内乘法二', '/数学（人教版）/6.表内乘法二', 1, 'unit', 6, '{"volumeKey": "y2s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y2s1_数学_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_数学_u06, 'y2s1_数学_u06_l01', '7-9乘法口诀', '/数学（人教版）/6.表内乘法二/7-9乘法口诀', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_数学_root, 'y2s1_数学_u07', '7.认识时间', '/数学（人教版）/7.认识时间', 1, 'unit', 7, '{"volumeKey": "y2s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y2s1_数学_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_数学_u07, 'y2s1_数学_u07_l01', '几时几分', '/数学（人教版）/7.认识时间/几时几分', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_数学_root, 'y2s1_数学_u08', '8.数学广角搭配', '/数学（人教版）/8.数学广角搭配', 1, 'unit', 8, '{"volumeKey": "y2s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y2s1_数学_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_数学_u08, 'y2s1_数学_u08_l01', '搭配问题', '/数学（人教版）/8.数学广角搭配/搭配问题', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "数学"}', 1);

-- y2s1 英语（人教PEP）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y2s1_英语_root', '英语（人教PEP）', '/英语（人教PEP）', 0, 'folder', 3, '{"volumeKey": "y2s1", "subject": "英语", "edition": "人教PEP", "defaultModule": "同步备课"}', 1);
SET @y2s1_英语_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_英语_root, 'y2s1_英语_u01', 'Unit1 My Family', '/英语（人教PEP）/Unit1 My Family', 1, 'unit', 1, '{"volumeKey": "y2s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y2s1_英语_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_英语_u01, 'y2s1_英语_u01_l01', '家人', '/英语（人教PEP）/Unit1 My Family/家人', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_英语_root, 'y2s1_英语_u02', 'Unit2 My Friends', '/英语（人教PEP）/Unit2 My Friends', 1, 'unit', 2, '{"volumeKey": "y2s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y2s1_英语_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_英语_u02, 'y2s1_英语_u02_l01', '朋友', '/英语（人教PEP）/Unit2 My Friends/朋友', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_英语_root, 'y2s1_英语_u03', 'Unit3 My Body', '/英语（人教PEP）/Unit3 My Body', 1, 'unit', 3, '{"volumeKey": "y2s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y2s1_英语_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_英语_u03, 'y2s1_英语_u03_l01', '身体部位', '/英语（人教PEP）/Unit3 My Body/身体部位', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_英语_root, 'y2s1_英语_u04', 'Unit4 Time', '/英语（人教PEP）/Unit4 Time', 1, 'unit', 4, '{"volumeKey": "y2s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y2s1_英语_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_英语_u04, 'y2s1_英语_u04_l01', '时间', '/英语（人教PEP）/Unit4 Time/时间', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_英语_root, 'y2s1_英语_u05', 'Unit5 My Clothes', '/英语（人教PEP）/Unit5 My Clothes', 1, 'unit', 5, '{"volumeKey": "y2s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y2s1_英语_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_英语_u05, 'y2s1_英语_u05_l01', '衣物', '/英语（人教PEP）/Unit5 My Clothes/衣物', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_英语_root, 'y2s1_英语_u06', 'Unit6 My Home', '/英语（人教PEP）/Unit6 My Home', 1, 'unit', 6, '{"volumeKey": "y2s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y2s1_英语_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s1_英语_u06, 'y2s1_英语_u06_l01', '家居', '/英语（人教PEP）/Unit6 My Home/家居', 2, 'lesson', 1, '{"volumeKey": "y2s1", "subject": "英语"}', 1);

-- y2s2 语文（统编版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y2s2_语文_root', '语文（统编版）', '/语文（统编版）', 0, 'folder', 1, '{"volumeKey": "y2s2", "subject": "语文", "edition": "统编版", "defaultModule": "同步备课"}', 1);
SET @y2s2_语文_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_语文_root, 'y2s2_语文_u01', '第一单元：课文', '/语文（统编版）/第一单元：课文', 1, 'unit', 1, '{"volumeKey": "y2s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y2s2_语文_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_语文_u01, 'y2s2_语文_u01_l01', '1 古诗二首（村居、咏柳）', '/语文（统编版）/第一单元：课文/1 古诗二首（村居、咏柳）', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u01, 'y2s2_语文_u01_l02', '2 找春天', '/语文（统编版）/第一单元：课文/2 找春天', 2, 'lesson', 2, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u01, 'y2s2_语文_u01_l03', '3 开满鲜花的小路', '/语文（统编版）/第一单元：课文/3 开满鲜花的小路', 2, 'lesson', 3, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u01, 'y2s2_语文_u01_l04', '4 邓小平爷爷植树', '/语文（统编版）/第一单元：课文/4 邓小平爷爷植树', 2, 'lesson', 4, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u01, 'y2s2_语文_u01_l05', '口语交际：注意说话的语气', '/语文（统编版）/第一单元：课文/口语交际：注意说话的语气', 2, 'lesson', 5, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u01, 'y2s2_语文_u01_l06', '语文园地一', '/语文（统编版）/第一单元：课文/语文园地一', 2, 'lesson', 6, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u01, 'y2s2_语文_u01_l07', '快乐读书吧：读读儿童故事', '/语文（统编版）/第一单元：课文/快乐读书吧：读读儿童故事', 2, 'lesson', 7, '{"volumeKey": "y2s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_语文_root, 'y2s2_语文_u02', '第二单元：课文', '/语文（统编版）/第二单元：课文', 1, 'unit', 2, '{"volumeKey": "y2s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y2s2_语文_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_语文_u02, 'y2s2_语文_u02_l01', '5 雷锋叔叔，你在哪里', '/语文（统编版）/第二单元：课文/5 雷锋叔叔，你在哪里', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u02, 'y2s2_语文_u02_l02', '6 千人糕', '/语文（统编版）/第二单元：课文/6 千人糕', 2, 'lesson', 2, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u02, 'y2s2_语文_u02_l03', '7 一匹出色的马', '/语文（统编版）/第二单元：课文/7 一匹出色的马', 2, 'lesson', 3, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u02, 'y2s2_语文_u02_l04', '语文园地二', '/语文（统编版）/第二单元：课文/语文园地二', 2, 'lesson', 4, '{"volumeKey": "y2s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_语文_root, 'y2s2_语文_u03', '第三单元：识字', '/语文（统编版）/第三单元：识字', 1, 'unit', 3, '{"volumeKey": "y2s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y2s2_语文_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_语文_u03, 'y2s2_语文_u03_l01', '1 神州谣', '/语文（统编版）/第三单元：识字/1 神州谣', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u03, 'y2s2_语文_u03_l02', '2 传统节日', '/语文（统编版）/第三单元：识字/2 传统节日', 2, 'lesson', 2, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u03, 'y2s2_语文_u03_l03', '3 “贝”的故事', '/语文（统编版）/第三单元：识字/3 “贝”的故事', 2, 'lesson', 3, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u03, 'y2s2_语文_u03_l04', '4 中国美食', '/语文（统编版）/第三单元：识字/4 中国美食', 2, 'lesson', 4, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u03, 'y2s2_语文_u03_l05', '口语交际：长大以后做什么', '/语文（统编版）/第三单元：识字/口语交际：长大以后做什么', 2, 'lesson', 5, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u03, 'y2s2_语文_u03_l06', '语文园地三', '/语文（统编版）/第三单元：识字/语文园地三', 2, 'lesson', 6, '{"volumeKey": "y2s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_语文_root, 'y2s2_语文_u04', '第四单元：课文', '/语文（统编版）/第四单元：课文', 1, 'unit', 4, '{"volumeKey": "y2s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y2s2_语文_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_语文_u04, 'y2s2_语文_u04_l01', '8 彩色的梦', '/语文（统编版）/第四单元：课文/8 彩色的梦', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u04, 'y2s2_语文_u04_l02', '9 枫树上的喜鹊', '/语文（统编版）/第四单元：课文/9 枫树上的喜鹊', 2, 'lesson', 2, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u04, 'y2s2_语文_u04_l03', '10 沙滩上的童话', '/语文（统编版）/第四单元：课文/10 沙滩上的童话', 2, 'lesson', 3, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u04, 'y2s2_语文_u04_l04', '11 我是一只小虫子', '/语文（统编版）/第四单元：课文/11 我是一只小虫子', 2, 'lesson', 4, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u04, 'y2s2_语文_u04_l05', '语文园地四', '/语文（统编版）/第四单元：课文/语文园地四', 2, 'lesson', 5, '{"volumeKey": "y2s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_语文_root, 'y2s2_语文_u05', '第五单元：课文', '/语文（统编版）/第五单元：课文', 1, 'unit', 5, '{"volumeKey": "y2s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y2s2_语文_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_语文_u05, 'y2s2_语文_u05_l01', '12 寓言二则（亡羊补牢、揠苗助长）', '/语文（统编版）/第五单元：课文/12 寓言二则（亡羊补牢、揠苗助长）', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u05, 'y2s2_语文_u05_l02', '13 画杨桃', '/语文（统编版）/第五单元：课文/13 画杨桃', 2, 'lesson', 2, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u05, 'y2s2_语文_u05_l03', '14 小马过河', '/语文（统编版）/第五单元：课文/14 小马过河', 2, 'lesson', 3, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u05, 'y2s2_语文_u05_l04', '口语交际：图书借阅公约', '/语文（统编版）/第五单元：课文/口语交际：图书借阅公约', 2, 'lesson', 4, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u05, 'y2s2_语文_u05_l05', '语文园地五', '/语文（统编版）/第五单元：课文/语文园地五', 2, 'lesson', 5, '{"volumeKey": "y2s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_语文_root, 'y2s2_语文_u06', '第六单元：课文', '/语文（统编版）/第六单元：课文', 1, 'unit', 6, '{"volumeKey": "y2s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y2s2_语文_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_语文_u06, 'y2s2_语文_u06_l01', '15 古诗二首（晓出净慈寺送林子方、绝句）', '/语文（统编版）/第六单元：课文/15 古诗二首（晓出净慈寺送林子方、绝句）', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u06, 'y2s2_语文_u06_l02', '16 雷雨', '/语文（统编版）/第六单元：课文/16 雷雨', 2, 'lesson', 2, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u06, 'y2s2_语文_u06_l03', '17 要是你在野外迷了路', '/语文（统编版）/第六单元：课文/17 要是你在野外迷了路', 2, 'lesson', 3, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u06, 'y2s2_语文_u06_l04', '18 太空生活趣事多', '/语文（统编版）/第六单元：课文/18 太空生活趣事多', 2, 'lesson', 4, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u06, 'y2s2_语文_u06_l05', '语文园地六', '/语文（统编版）/第六单元：课文/语文园地六', 2, 'lesson', 5, '{"volumeKey": "y2s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_语文_root, 'y2s2_语文_u07', '第七单元：课文', '/语文（统编版）/第七单元：课文', 1, 'unit', 7, '{"volumeKey": "y2s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y2s2_语文_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_语文_u07, 'y2s2_语文_u07_l01', '19 大象的耳朵', '/语文（统编版）/第七单元：课文/19 大象的耳朵', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u07, 'y2s2_语文_u07_l02', '20 蜘蛛开店', '/语文（统编版）/第七单元：课文/20 蜘蛛开店', 2, 'lesson', 2, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u07, 'y2s2_语文_u07_l03', '21 青蛙卖泥塘', '/语文（统编版）/第七单元：课文/21 青蛙卖泥塘', 2, 'lesson', 3, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u07, 'y2s2_语文_u07_l04', '22 小毛虫', '/语文（统编版）/第七单元：课文/22 小毛虫', 2, 'lesson', 4, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u07, 'y2s2_语文_u07_l05', '语文园地七', '/语文（统编版）/第七单元：课文/语文园地七', 2, 'lesson', 5, '{"volumeKey": "y2s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_语文_root, 'y2s2_语文_u08', '第八单元：课文', '/语文（统编版）/第八单元：课文', 1, 'unit', 8, '{"volumeKey": "y2s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y2s2_语文_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_语文_u08, 'y2s2_语文_u08_l01', '23 祖先的摇篮', '/语文（统编版）/第八单元：课文/23 祖先的摇篮', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u08, 'y2s2_语文_u08_l02', '24 当世界年纪还小的时候', '/语文（统编版）/第八单元：课文/24 当世界年纪还小的时候', 2, 'lesson', 2, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u08, 'y2s2_语文_u08_l03', '25 羿射九日', '/语文（统编版）/第八单元：课文/25 羿射九日', 2, 'lesson', 3, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u08, 'y2s2_语文_u08_l04', '口语交际：推荐一部动画片', '/语文（统编版）/第八单元：课文/口语交际：推荐一部动画片', 2, 'lesson', 4, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u08, 'y2s2_语文_u08_l05', '语文园地八', '/语文（统编版）/第八单元：课文/语文园地八', 2, 'lesson', 5, '{"volumeKey": "y2s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_语文_root, 'y2s2_语文_u09', '附录', '/语文（统编版）/附录', 1, 'unit', 9, '{"volumeKey": "y2s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y2s2_语文_u09 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_语文_u09, 'y2s2_语文_u09_l01', '识字表', '/语文（统编版）/附录/识字表', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u09, 'y2s2_语文_u09_l02', '写字表', '/语文（统编版）/附录/写字表', 2, 'lesson', 2, '{"volumeKey": "y2s2", "subject": "语文"}', 1),
(@scheme_id, @y2s2_语文_u09, 'y2s2_语文_u09_l03', '词语表', '/语文（统编版）/附录/词语表', 2, 'lesson', 3, '{"volumeKey": "y2s2", "subject": "语文"}', 1);

-- y2s2 数学（人教版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y2s2_数学_root', '数学（人教版）', '/数学（人教版）', 0, 'folder', 2, '{"volumeKey": "y2s2", "subject": "数学", "edition": "人教版", "defaultModule": "同步备课"}', 1);
SET @y2s2_数学_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_数学_root, 'y2s2_数学_u01', '1.数据收集整理', '/数学（人教版）/1.数据收集整理', 1, 'unit', 1, '{"volumeKey": "y2s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y2s2_数学_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_数学_u01, 'y2s2_数学_u01_l01', '数据收集整理', '/数学（人教版）/1.数据收集整理/数据收集整理', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_数学_root, 'y2s2_数学_u02', '2.表内除法一', '/数学（人教版）/2.表内除法一', 1, 'unit', 2, '{"volumeKey": "y2s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y2s2_数学_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_数学_u02, 'y2s2_数学_u02_l01', '平均分', '/数学（人教版）/2.表内除法一/平均分', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "数学"}', 1),
(@scheme_id, @y2s2_数学_u02, 'y2s2_数学_u02_l02', '除法算式', '/数学（人教版）/2.表内除法一/除法算式', 2, 'lesson', 2, '{"volumeKey": "y2s2", "subject": "数学"}', 1),
(@scheme_id, @y2s2_数学_u02, 'y2s2_数学_u02_l03', '乘法口诀求商', '/数学（人教版）/2.表内除法一/乘法口诀求商', 2, 'lesson', 3, '{"volumeKey": "y2s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_数学_root, 'y2s2_数学_u03', '3.图形运动', '/数学（人教版）/3.图形运动', 1, 'unit', 3, '{"volumeKey": "y2s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y2s2_数学_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_数学_u03, 'y2s2_数学_u03_l01', '轴对称', '/数学（人教版）/3.图形运动/轴对称', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "数学"}', 1),
(@scheme_id, @y2s2_数学_u03, 'y2s2_数学_u03_l02', '平移旋转', '/数学（人教版）/3.图形运动/平移旋转', 2, 'lesson', 2, '{"volumeKey": "y2s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_数学_root, 'y2s2_数学_u04', '4.表内除法二', '/数学（人教版）/4.表内除法二', 1, 'unit', 4, '{"volumeKey": "y2s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y2s2_数学_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_数学_u04, 'y2s2_数学_u04_l01', '7-9口诀求商', '/数学（人教版）/4.表内除法二/7-9口诀求商', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_数学_root, 'y2s2_数学_u05', '5.混合运算', '/数学（人教版）/5.混合运算', 1, 'unit', 5, '{"volumeKey": "y2s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y2s2_数学_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_数学_u05, 'y2s2_数学_u05_l01', '混合运算', '/数学（人教版）/5.混合运算/混合运算', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_数学_root, 'y2s2_数学_u06', '6.有余数的除法', '/数学（人教版）/6.有余数的除法', 1, 'unit', 6, '{"volumeKey": "y2s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y2s2_数学_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_数学_u06, 'y2s2_数学_u06_l01', '有余数的除法', '/数学（人教版）/6.有余数的除法/有余数的除法', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_数学_root, 'y2s2_数学_u07', '7.万以内数的认识', '/数学（人教版）/7.万以内数的认识', 1, 'unit', 7, '{"volumeKey": "y2s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y2s2_数学_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_数学_u07, 'y2s2_数学_u07_l01', '万以内数的认识', '/数学（人教版）/7.万以内数的认识/万以内数的认识', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_数学_root, 'y2s2_数学_u08', '8.克与千克', '/数学（人教版）/8.克与千克', 1, 'unit', 8, '{"volumeKey": "y2s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y2s2_数学_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_数学_u08, 'y2s2_数学_u08_l01', '克与千克', '/数学（人教版）/8.克与千克/克与千克', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_数学_root, 'y2s2_数学_u09', '9.推理', '/数学（人教版）/9.推理', 1, 'unit', 9, '{"volumeKey": "y2s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y2s2_数学_u09 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_数学_u09, 'y2s2_数学_u09_l01', '推理', '/数学（人教版）/9.推理/推理', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "数学"}', 1);

-- y2s2 英语（人教PEP）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y2s2_英语_root', '英语（人教PEP）', '/英语（人教PEP）', 0, 'folder', 3, '{"volumeKey": "y2s2", "subject": "英语", "edition": "人教PEP", "defaultModule": "同步备课"}', 1);
SET @y2s2_英语_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_英语_root, 'y2s2_英语_u01', 'Unit1 My School', '/英语（人教PEP）/Unit1 My School', 1, 'unit', 1, '{"volumeKey": "y2s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y2s2_英语_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_英语_u01, 'y2s2_英语_u01_l01', '校园', '/英语（人教PEP）/Unit1 My School/校园', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_英语_root, 'y2s2_英语_u02', 'Unit2 My Classroom', '/英语（人教PEP）/Unit2 My Classroom', 1, 'unit', 2, '{"volumeKey": "y2s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y2s2_英语_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_英语_u02, 'y2s2_英语_u02_l01', '教室', '/英语（人教PEP）/Unit2 My Classroom/教室', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_英语_root, 'y2s2_英语_u03', 'Unit3 My Day', '/英语（人教PEP）/Unit3 My Day', 1, 'unit', 3, '{"volumeKey": "y2s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y2s2_英语_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_英语_u03, 'y2s2_英语_u03_l01', '日常作息', '/英语（人教PEP）/Unit3 My Day/日常作息', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_英语_root, 'y2s2_英语_u04', 'Unit4 Weather', '/英语（人教PEP）/Unit4 Weather', 1, 'unit', 4, '{"volumeKey": "y2s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y2s2_英语_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_英语_u04, 'y2s2_英语_u04_l01', '天气', '/英语（人教PEP）/Unit4 Weather/天气', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_英语_root, 'y2s2_英语_u05', 'Unit5 Seasons', '/英语（人教PEP）/Unit5 Seasons', 1, 'unit', 5, '{"volumeKey": "y2s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y2s2_英语_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_英语_u05, 'y2s2_英语_u05_l01', '四季', '/英语（人教PEP）/Unit5 Seasons/四季', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_英语_root, 'y2s2_英语_u06', 'Unit6 Travel', '/英语（人教PEP）/Unit6 Travel', 1, 'unit', 6, '{"volumeKey": "y2s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y2s2_英语_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y2s2_英语_u06, 'y2s2_英语_u06_l01', '出行', '/英语（人教PEP）/Unit6 Travel/出行', 2, 'lesson', 1, '{"volumeKey": "y2s2", "subject": "英语"}', 1);

-- y3s1 语文（统编版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y3s1_语文_root', '语文（统编版）', '/语文（统编版）', 0, 'folder', 1, '{"volumeKey": "y3s1", "subject": "语文", "edition": "统编版", "defaultModule": "同步备课"}', 1);
SET @y3s1_语文_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_语文_root, 'y3s1_语文_u01', '第一单元', '/语文（统编版）/第一单元', 1, 'unit', 1, '{"volumeKey": "y3s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y3s1_语文_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_语文_u01, 'y3s1_语文_u01_l01', '1 大青树下的小学', '/语文（统编版）/第一单元/1 大青树下的小学', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u01, 'y3s1_语文_u01_l02', '2 花的学校', '/语文（统编版）/第一单元/2 花的学校', 2, 'lesson', 2, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u01, 'y3s1_语文_u01_l03', '3* 不懂就要问', '/语文（统编版）/第一单元/3* 不懂就要问', 2, 'lesson', 3, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u01, 'y3s1_语文_u01_l04', '口语交际：我的暑假生活', '/语文（统编版）/第一单元/口语交际：我的暑假生活', 2, 'lesson', 4, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u01, 'y3s1_语文_u01_l05', '习作：猜猜他是谁', '/语文（统编版）/第一单元/习作：猜猜他是谁', 2, 'lesson', 5, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u01, 'y3s1_语文_u01_l06', '语文园地', '/语文（统编版）/第一单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y3s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_语文_root, 'y3s1_语文_u02', '第二单元', '/语文（统编版）/第二单元', 1, 'unit', 2, '{"volumeKey": "y3s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y3s1_语文_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_语文_u02, 'y3s1_语文_u02_l01', '4 古诗三首（山行、赠刘景文、夜书所见）', '/语文（统编版）/第二单元/4 古诗三首（山行、赠刘景文、夜书所见）', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u02, 'y3s1_语文_u02_l02', '5 铺满金色巴掌的水泥道', '/语文（统编版）/第二单元/5 铺满金色巴掌的水泥道', 2, 'lesson', 2, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u02, 'y3s1_语文_u02_l03', '6 秋天的雨', '/语文（统编版）/第二单元/6 秋天的雨', 2, 'lesson', 3, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u02, 'y3s1_语文_u02_l04', '7* 听听，秋的声音', '/语文（统编版）/第二单元/7* 听听，秋的声音', 2, 'lesson', 4, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u02, 'y3s1_语文_u02_l05', '习作：写日记', '/语文（统编版）/第二单元/习作：写日记', 2, 'lesson', 5, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u02, 'y3s1_语文_u02_l06', '语文园地', '/语文（统编版）/第二单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y3s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_语文_root, 'y3s1_语文_u03', '第三单元', '/语文（统编版）/第三单元', 1, 'unit', 3, '{"volumeKey": "y3s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y3s1_语文_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_语文_u03, 'y3s1_语文_u03_l01', '8 卖火柴的小女孩', '/语文（统编版）/第三单元/8 卖火柴的小女孩', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u03, 'y3s1_语文_u03_l02', '9* 那一定会很好', '/语文（统编版）/第三单元/9* 那一定会很好', 2, 'lesson', 2, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u03, 'y3s1_语文_u03_l03', '10 在牛肚子里旅行', '/语文（统编版）/第三单元/10 在牛肚子里旅行', 2, 'lesson', 3, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u03, 'y3s1_语文_u03_l04', '11* 一块奶酪', '/语文（统编版）/第三单元/11* 一块奶酪', 2, 'lesson', 4, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u03, 'y3s1_语文_u03_l05', '习作：我来编童话', '/语文（统编版）/第三单元/习作：我来编童话', 2, 'lesson', 5, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u03, 'y3s1_语文_u03_l06', '语文园地', '/语文（统编版）/第三单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u03, 'y3s1_语文_u03_l07', '快乐读书吧：童话', '/语文（统编版）/第三单元/快乐读书吧：童话', 2, 'lesson', 7, '{"volumeKey": "y3s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_语文_root, 'y3s1_语文_u04', '第四单元', '/语文（统编版）/第四单元', 1, 'unit', 4, '{"volumeKey": "y3s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y3s1_语文_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_语文_u04, 'y3s1_语文_u04_l01', '12 总也倒不了的老屋', '/语文（统编版）/第四单元/12 总也倒不了的老屋', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u04, 'y3s1_语文_u04_l02', '13* 胡萝卜先生的长胡子', '/语文（统编版）/第四单元/13* 胡萝卜先生的长胡子', 2, 'lesson', 2, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u04, 'y3s1_语文_u04_l03', '14* 小狗学叫', '/语文（统编版）/第四单元/14* 小狗学叫', 2, 'lesson', 3, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u04, 'y3s1_语文_u04_l04', '口语交际：名字里的故事', '/语文（统编版）/第四单元/口语交际：名字里的故事', 2, 'lesson', 4, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u04, 'y3s1_语文_u04_l05', '习作：续写故事', '/语文（统编版）/第四单元/习作：续写故事', 2, 'lesson', 5, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u04, 'y3s1_语文_u04_l06', '语文园地', '/语文（统编版）/第四单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y3s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_语文_root, 'y3s1_语文_u05', '第五单元', '/语文（统编版）/第五单元', 1, 'unit', 5, '{"volumeKey": "y3s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y3s1_语文_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_语文_u05, 'y3s1_语文_u05_l01', '15 搭船的鸟', '/语文（统编版）/第五单元/15 搭船的鸟', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u05, 'y3s1_语文_u05_l02', '16 金色的草地', '/语文（统编版）/第五单元/16 金色的草地', 2, 'lesson', 2, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u05, 'y3s1_语文_u05_l03', '习作例文：我家的小狗、我爱故乡的杨梅', '/语文（统编版）/第五单元/习作例文：我家的小狗、我爱故乡的杨梅', 2, 'lesson', 3, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u05, 'y3s1_语文_u05_l04', '习作：我们眼中的缤纷世界', '/语文（统编版）/第五单元/习作：我们眼中的缤纷世界', 2, 'lesson', 4, '{"volumeKey": "y3s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_语文_root, 'y3s1_语文_u06', '第六单元', '/语文（统编版）/第六单元', 1, 'unit', 6, '{"volumeKey": "y3s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y3s1_语文_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_语文_u06, 'y3s1_语文_u06_l01', '17 古诗三首（望天门山、饮湖上初晴后雨、望洞庭）', '/语文（统编版）/第六单元/17 古诗三首（望天门山、饮湖上初晴后雨、望洞庭）', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u06, 'y3s1_语文_u06_l02', '18 富饶的西沙群岛', '/语文（统编版）/第六单元/18 富饶的西沙群岛', 2, 'lesson', 2, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u06, 'y3s1_语文_u06_l03', '19 海滨小城', '/语文（统编版）/第六单元/19 海滨小城', 2, 'lesson', 3, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u06, 'y3s1_语文_u06_l04', '20 美丽的小兴安岭', '/语文（统编版）/第六单元/20 美丽的小兴安岭', 2, 'lesson', 4, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u06, 'y3s1_语文_u06_l05', '习作：这儿真美', '/语文（统编版）/第六单元/习作：这儿真美', 2, 'lesson', 5, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u06, 'y3s1_语文_u06_l06', '语文园地', '/语文（统编版）/第六单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y3s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_语文_root, 'y3s1_语文_u07', '第七单元', '/语文（统编版）/第七单元', 1, 'unit', 7, '{"volumeKey": "y3s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y3s1_语文_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_语文_u07, 'y3s1_语文_u07_l01', '21 大自然的声音', '/语文（统编版）/第七单元/21 大自然的声音', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u07, 'y3s1_语文_u07_l02', '22 读不完的大书', '/语文（统编版）/第七单元/22 读不完的大书', 2, 'lesson', 2, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u07, 'y3s1_语文_u07_l03', '23 父亲、树林和鸟', '/语文（统编版）/第七单元/23 父亲、树林和鸟', 2, 'lesson', 3, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u07, 'y3s1_语文_u07_l04', '口语交际：身边的“小事”', '/语文（统编版）/第七单元/口语交际：身边的“小事”', 2, 'lesson', 4, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u07, 'y3s1_语文_u07_l05', '习作：我有一个想法', '/语文（统编版）/第七单元/习作：我有一个想法', 2, 'lesson', 5, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u07, 'y3s1_语文_u07_l06', '语文园地', '/语文（统编版）/第七单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y3s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_语文_root, 'y3s1_语文_u08', '第八单元', '/语文（统编版）/第八单元', 1, 'unit', 8, '{"volumeKey": "y3s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y3s1_语文_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_语文_u08, 'y3s1_语文_u08_l01', '24 司马光', '/语文（统编版）/第八单元/24 司马光', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u08, 'y3s1_语文_u08_l02', '25 灰雀', '/语文（统编版）/第八单元/25 灰雀', 2, 'lesson', 2, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u08, 'y3s1_语文_u08_l03', '26 手术台就是阵地', '/语文（统编版）/第八单元/26 手术台就是阵地', 2, 'lesson', 3, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u08, 'y3s1_语文_u08_l04', '27* 一个粗瓷大碗', '/语文（统编版）/第八单元/27* 一个粗瓷大碗', 2, 'lesson', 4, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u08, 'y3s1_语文_u08_l05', '口语交际：请教', '/语文（统编版）/第八单元/口语交际：请教', 2, 'lesson', 5, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u08, 'y3s1_语文_u08_l06', '习作：那次玩得真高兴', '/语文（统编版）/第八单元/习作：那次玩得真高兴', 2, 'lesson', 6, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u08, 'y3s1_语文_u08_l07', '语文园地', '/语文（统编版）/第八单元/语文园地', 2, 'lesson', 7, '{"volumeKey": "y3s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_语文_root, 'y3s1_语文_u09', '附录', '/语文（统编版）/附录', 1, 'unit', 9, '{"volumeKey": "y3s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y3s1_语文_u09 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_语文_u09, 'y3s1_语文_u09_l01', '识字表', '/语文（统编版）/附录/识字表', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u09, 'y3s1_语文_u09_l02', '写字表', '/语文（统编版）/附录/写字表', 2, 'lesson', 2, '{"volumeKey": "y3s1", "subject": "语文"}', 1),
(@scheme_id, @y3s1_语文_u09, 'y3s1_语文_u09_l03', '词语表', '/语文（统编版）/附录/词语表', 2, 'lesson', 3, '{"volumeKey": "y3s1", "subject": "语文"}', 1);

-- y3s1 数学（人教版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y3s1_数学_root', '数学（人教版）', '/数学（人教版）', 0, 'folder', 2, '{"volumeKey": "y3s1", "subject": "数学", "edition": "人教版", "defaultModule": "同步备课"}', 1);
SET @y3s1_数学_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_数学_root, 'y3s1_数学_u01', '1.时分秒', '/数学（人教版）/1.时分秒', 1, 'unit', 1, '{"volumeKey": "y3s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y3s1_数学_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_数学_u01, 'y3s1_数学_u01_l01', '时分秒', '/数学（人教版）/1.时分秒/时分秒', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_数学_root, 'y3s1_数学_u02', '2.万以内加减法一', '/数学（人教版）/2.万以内加减法一', 1, 'unit', 2, '{"volumeKey": "y3s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y3s1_数学_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_数学_u02, 'y3s1_数学_u02_l01', '万以内加减法一', '/数学（人教版）/2.万以内加减法一/万以内加减法一', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_数学_root, 'y3s1_数学_u03', '3.测量', '/数学（人教版）/3.测量', 1, 'unit', 3, '{"volumeKey": "y3s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y3s1_数学_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_数学_u03, 'y3s1_数学_u03_l01', '毫米分米千米', '/数学（人教版）/3.测量/毫米分米千米', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "数学"}', 1),
(@scheme_id, @y3s1_数学_u03, 'y3s1_数学_u03_l02', '吨', '/数学（人教版）/3.测量/吨', 2, 'lesson', 2, '{"volumeKey": "y3s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_数学_root, 'y3s1_数学_u04', '4.万以内加减法二', '/数学（人教版）/4.万以内加减法二', 1, 'unit', 4, '{"volumeKey": "y3s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y3s1_数学_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_数学_u04, 'y3s1_数学_u04_l01', '万以内加减法二', '/数学（人教版）/4.万以内加减法二/万以内加减法二', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_数学_root, 'y3s1_数学_u05', '5.倍的认识', '/数学（人教版）/5.倍的认识', 1, 'unit', 5, '{"volumeKey": "y3s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y3s1_数学_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_数学_u05, 'y3s1_数学_u05_l01', '倍的认识', '/数学（人教版）/5.倍的认识/倍的认识', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_数学_root, 'y3s1_数学_u06', '6.多位数乘一位数', '/数学（人教版）/6.多位数乘一位数', 1, 'unit', 6, '{"volumeKey": "y3s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y3s1_数学_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_数学_u06, 'y3s1_数学_u06_l01', '多位数乘一位数', '/数学（人教版）/6.多位数乘一位数/多位数乘一位数', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_数学_root, 'y3s1_数学_u07', '7.长方形正方形周长', '/数学（人教版）/7.长方形正方形周长', 1, 'unit', 7, '{"volumeKey": "y3s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y3s1_数学_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_数学_u07, 'y3s1_数学_u07_l01', '长方形正方形周长', '/数学（人教版）/7.长方形正方形周长/长方形正方形周长', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_数学_root, 'y3s1_数学_u08', '8.分数初步认识', '/数学（人教版）/8.分数初步认识', 1, 'unit', 8, '{"volumeKey": "y3s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y3s1_数学_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_数学_u08, 'y3s1_数学_u08_l01', '分数初步认识', '/数学（人教版）/8.分数初步认识/分数初步认识', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_数学_root, 'y3s1_数学_u09', '9.集合', '/数学（人教版）/9.集合', 1, 'unit', 9, '{"volumeKey": "y3s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y3s1_数学_u09 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_数学_u09, 'y3s1_数学_u09_l01', '集合', '/数学（人教版）/9.集合/集合', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "数学"}', 1);

-- y3s1 英语（人教PEP）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y3s1_英语_root', '英语（人教PEP）', '/英语（人教PEP）', 0, 'folder', 3, '{"volumeKey": "y3s1", "subject": "英语", "edition": "人教PEP", "defaultModule": "同步备课"}', 1);
SET @y3s1_英语_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_英语_root, 'y3s1_英语_u01', 'Unit1 Hello', '/英语（人教PEP）/Unit1 Hello', 1, 'unit', 1, '{"volumeKey": "y3s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y3s1_英语_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_英语_u01, 'y3s1_英语_u01_l01', '问候', '/英语（人教PEP）/Unit1 Hello/问候', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_英语_root, 'y3s1_英语_u02', 'Unit2 Colours', '/英语（人教PEP）/Unit2 Colours', 1, 'unit', 2, '{"volumeKey": "y3s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y3s1_英语_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_英语_u02, 'y3s1_英语_u02_l01', '颜色', '/英语（人教PEP）/Unit2 Colours/颜色', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_英语_root, 'y3s1_英语_u03', 'Unit3 My schoolbag', '/英语（人教PEP）/Unit3 My schoolbag', 1, 'unit', 3, '{"volumeKey": "y3s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y3s1_英语_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_英语_u03, 'y3s1_英语_u03_l01', '书包', '/英语（人教PEP）/Unit3 My schoolbag/书包', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_英语_root, 'y3s1_英语_u04', 'Unit4 My home', '/英语（人教PEP）/Unit4 My home', 1, 'unit', 4, '{"volumeKey": "y3s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y3s1_英语_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_英语_u04, 'y3s1_英语_u04_l01', '家', '/英语（人教PEP）/Unit4 My home/家', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_英语_root, 'y3s1_英语_u05', 'Unit5 Dinner ready', '/英语（人教PEP）/Unit5 Dinner ready', 1, 'unit', 5, '{"volumeKey": "y3s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y3s1_英语_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_英语_u05, 'y3s1_英语_u05_l01', '晚餐', '/英语（人教PEP）/Unit5 Dinner ready/晚餐', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_英语_root, 'y3s1_英语_u06', 'Unit6 How many', '/英语（人教PEP）/Unit6 How many', 1, 'unit', 6, '{"volumeKey": "y3s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y3s1_英语_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s1_英语_u06, 'y3s1_英语_u06_l01', '数量', '/英语（人教PEP）/Unit6 How many/数量', 2, 'lesson', 1, '{"volumeKey": "y3s1", "subject": "英语"}', 1);

-- y3s2 语文（统编版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y3s2_语文_root', '语文（统编版）', '/语文（统编版）', 0, 'folder', 1, '{"volumeKey": "y3s2", "subject": "语文", "edition": "统编版", "defaultModule": "同步备课"}', 1);
SET @y3s2_语文_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_语文_root, 'y3s2_语文_u01', '第一单元', '/语文（统编版）/第一单元', 1, 'unit', 1, '{"volumeKey": "y3s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y3s2_语文_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_语文_u01, 'y3s2_语文_u01_l01', '1 古诗三首（绝句、惠崇春江晚景、三衢道中）', '/语文（统编版）/第一单元/1 古诗三首（绝句、惠崇春江晚景、三衢道中）', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u01, 'y3s2_语文_u01_l02', '2 燕子', '/语文（统编版）/第一单元/2 燕子', 2, 'lesson', 2, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u01, 'y3s2_语文_u01_l03', '3 荷花', '/语文（统编版）/第一单元/3 荷花', 2, 'lesson', 3, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u01, 'y3s2_语文_u01_l04', '4* 昆虫备忘录', '/语文（统编版）/第一单元/4* 昆虫备忘录', 2, 'lesson', 4, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u01, 'y3s2_语文_u01_l05', '口语交际：春游去哪儿玩', '/语文（统编版）/第一单元/口语交际：春游去哪儿玩', 2, 'lesson', 5, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u01, 'y3s2_语文_u01_l06', '习作：我的植物朋友', '/语文（统编版）/第一单元/习作：我的植物朋友', 2, 'lesson', 6, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u01, 'y3s2_语文_u01_l07', '语文园地', '/语文（统编版）/第一单元/语文园地', 2, 'lesson', 7, '{"volumeKey": "y3s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_语文_root, 'y3s2_语文_u02', '第二单元', '/语文（统编版）/第二单元', 1, 'unit', 2, '{"volumeKey": "y3s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y3s2_语文_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_语文_u02, 'y3s2_语文_u02_l01', '5 守株待兔', '/语文（统编版）/第二单元/5 守株待兔', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u02, 'y3s2_语文_u02_l02', '6 陶罐和铁罐', '/语文（统编版）/第二单元/6 陶罐和铁罐', 2, 'lesson', 2, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u02, 'y3s2_语文_u02_l03', '7 鹿角和鹿腿', '/语文（统编版）/第二单元/7 鹿角和鹿腿', 2, 'lesson', 3, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u02, 'y3s2_语文_u02_l04', '8* 池子与河流', '/语文（统编版）/第二单元/8* 池子与河流', 2, 'lesson', 4, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u02, 'y3s2_语文_u02_l05', '口语交际：该不该实行班干部轮流制', '/语文（统编版）/第二单元/口语交际：该不该实行班干部轮流制', 2, 'lesson', 5, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u02, 'y3s2_语文_u02_l06', '习作：看图画，写一写', '/语文（统编版）/第二单元/习作：看图画，写一写', 2, 'lesson', 6, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u02, 'y3s2_语文_u02_l07', '语文园地', '/语文（统编版）/第二单元/语文园地', 2, 'lesson', 7, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u02, 'y3s2_语文_u02_l08', '快乐读书吧：小故事大道理', '/语文（统编版）/第二单元/快乐读书吧：小故事大道理', 2, 'lesson', 8, '{"volumeKey": "y3s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_语文_root, 'y3s2_语文_u03', '第三单元', '/语文（统编版）/第三单元', 1, 'unit', 3, '{"volumeKey": "y3s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y3s2_语文_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_语文_u03, 'y3s2_语文_u03_l01', '9 古诗三首（元日、清明、九月九日忆山东兄弟）', '/语文（统编版）/第三单元/9 古诗三首（元日、清明、九月九日忆山东兄弟）', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u03, 'y3s2_语文_u03_l02', '10 纸的发明', '/语文（统编版）/第三单元/10 纸的发明', 2, 'lesson', 2, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u03, 'y3s2_语文_u03_l03', '11 赵州桥', '/语文（统编版）/第三单元/11 赵州桥', 2, 'lesson', 3, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u03, 'y3s2_语文_u03_l04', '12* 一幅名扬中外的画', '/语文（统编版）/第三单元/12* 一幅名扬中外的画', 2, 'lesson', 4, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u03, 'y3s2_语文_u03_l05', '综合性学习：中华传统节日', '/语文（统编版）/第三单元/综合性学习：中华传统节日', 2, 'lesson', 5, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u03, 'y3s2_语文_u03_l06', '语文园地', '/语文（统编版）/第三单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y3s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_语文_root, 'y3s2_语文_u04', '第四单元', '/语文（统编版）/第四单元', 1, 'unit', 4, '{"volumeKey": "y3s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y3s2_语文_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_语文_u04, 'y3s2_语文_u04_l01', '13 花钟', '/语文（统编版）/第四单元/13 花钟', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u04, 'y3s2_语文_u04_l02', '14 蜜蜂', '/语文（统编版）/第四单元/14 蜜蜂', 2, 'lesson', 2, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u04, 'y3s2_语文_u04_l03', '15* 小虾', '/语文（统编版）/第四单元/15* 小虾', 2, 'lesson', 3, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u04, 'y3s2_语文_u04_l04', '习作：我做了一项小实验', '/语文（统编版）/第四单元/习作：我做了一项小实验', 2, 'lesson', 4, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u04, 'y3s2_语文_u04_l05', '语文园地', '/语文（统编版）/第四单元/语文园地', 2, 'lesson', 5, '{"volumeKey": "y3s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_语文_root, 'y3s2_语文_u05', '第五单元', '/语文（统编版）/第五单元', 1, 'unit', 5, '{"volumeKey": "y3s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y3s2_语文_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_语文_u05, 'y3s2_语文_u05_l01', '16 宇宙的另一边', '/语文（统编版）/第五单元/16 宇宙的另一边', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u05, 'y3s2_语文_u05_l02', '17 我变成了一棵树', '/语文（统编版）/第五单元/17 我变成了一棵树', 2, 'lesson', 2, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u05, 'y3s2_语文_u05_l03', '习作例文', '/语文（统编版）/第五单元/习作例文', 2, 'lesson', 3, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u05, 'y3s2_语文_u05_l04', '习作：奇妙的想象', '/语文（统编版）/第五单元/习作：奇妙的想象', 2, 'lesson', 4, '{"volumeKey": "y3s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_语文_root, 'y3s2_语文_u06', '第六单元', '/语文（统编版）/第六单元', 1, 'unit', 6, '{"volumeKey": "y3s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y3s2_语文_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_语文_u06, 'y3s2_语文_u06_l01', '18 童年的水墨画', '/语文（统编版）/第六单元/18 童年的水墨画', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u06, 'y3s2_语文_u06_l02', '19 剃头大师', '/语文（统编版）/第六单元/19 剃头大师', 2, 'lesson', 2, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u06, 'y3s2_语文_u06_l03', '20 肥皂泡', '/语文（统编版）/第六单元/20 肥皂泡', 2, 'lesson', 3, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u06, 'y3s2_语文_u06_l04', '21* 我不能失信', '/语文（统编版）/第六单元/21* 我不能失信', 2, 'lesson', 4, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u06, 'y3s2_语文_u06_l05', '习作：身边那些有特点的人', '/语文（统编版）/第六单元/习作：身边那些有特点的人', 2, 'lesson', 5, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u06, 'y3s2_语文_u06_l06', '语文园地', '/语文（统编版）/第六单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y3s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_语文_root, 'y3s2_语文_u07', '第七单元', '/语文（统编版）/第七单元', 1, 'unit', 7, '{"volumeKey": "y3s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y3s2_语文_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_语文_u07, 'y3s2_语文_u07_l01', '22 我们奇妙的世界', '/语文（统编版）/第七单元/22 我们奇妙的世界', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u07, 'y3s2_语文_u07_l02', '23 海底世界', '/语文（统编版）/第七单元/23 海底世界', 2, 'lesson', 2, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u07, 'y3s2_语文_u07_l03', '24 火烧云', '/语文（统编版）/第七单元/24 火烧云', 2, 'lesson', 3, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u07, 'y3s2_语文_u07_l04', '口语交际：劝告', '/语文（统编版）/第七单元/口语交际：劝告', 2, 'lesson', 4, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u07, 'y3s2_语文_u07_l05', '习作：国宝大熊猫', '/语文（统编版）/第七单元/习作：国宝大熊猫', 2, 'lesson', 5, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u07, 'y3s2_语文_u07_l06', '语文园地', '/语文（统编版）/第七单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y3s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_语文_root, 'y3s2_语文_u08', '第八单元', '/语文（统编版）/第八单元', 1, 'unit', 8, '{"volumeKey": "y3s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y3s2_语文_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_语文_u08, 'y3s2_语文_u08_l01', '25 慢性子裁缝和急性子顾客', '/语文（统编版）/第八单元/25 慢性子裁缝和急性子顾客', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u08, 'y3s2_语文_u08_l02', '26* 方帽子店', '/语文（统编版）/第八单元/26* 方帽子店', 2, 'lesson', 2, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u08, 'y3s2_语文_u08_l03', '27 漏', '/语文（统编版）/第八单元/27 漏', 2, 'lesson', 3, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u08, 'y3s2_语文_u08_l04', '28* 枣核', '/语文（统编版）/第八单元/28* 枣核', 2, 'lesson', 4, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u08, 'y3s2_语文_u08_l05', '口语交际：趣味故事会', '/语文（统编版）/第八单元/口语交际：趣味故事会', 2, 'lesson', 5, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u08, 'y3s2_语文_u08_l06', '习作：这样想象真有趣', '/语文（统编版）/第八单元/习作：这样想象真有趣', 2, 'lesson', 6, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u08, 'y3s2_语文_u08_l07', '语文园地', '/语文（统编版）/第八单元/语文园地', 2, 'lesson', 7, '{"volumeKey": "y3s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_语文_root, 'y3s2_语文_u09', '附录', '/语文（统编版）/附录', 1, 'unit', 9, '{"volumeKey": "y3s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y3s2_语文_u09 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_语文_u09, 'y3s2_语文_u09_l01', '识字表', '/语文（统编版）/附录/识字表', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u09, 'y3s2_语文_u09_l02', '写字表', '/语文（统编版）/附录/写字表', 2, 'lesson', 2, '{"volumeKey": "y3s2", "subject": "语文"}', 1),
(@scheme_id, @y3s2_语文_u09, 'y3s2_语文_u09_l03', '词语表', '/语文（统编版）/附录/词语表', 2, 'lesson', 3, '{"volumeKey": "y3s2", "subject": "语文"}', 1);

-- y3s2 数学（人教版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y3s2_数学_root', '数学（人教版）', '/数学（人教版）', 0, 'folder', 2, '{"volumeKey": "y3s2", "subject": "数学", "edition": "人教版", "defaultModule": "同步备课"}', 1);
SET @y3s2_数学_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_数学_root, 'y3s2_数学_u01', '1.位置与方向', '/数学（人教版）/1.位置与方向', 1, 'unit', 1, '{"volumeKey": "y3s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y3s2_数学_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_数学_u01, 'y3s2_数学_u01_l01', '位置与方向', '/数学（人教版）/1.位置与方向/位置与方向', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_数学_root, 'y3s2_数学_u02', '2.除数是一位数的除法', '/数学（人教版）/2.除数是一位数的除法', 1, 'unit', 2, '{"volumeKey": "y3s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y3s2_数学_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_数学_u02, 'y3s2_数学_u02_l01', '除数是一位数的除法', '/数学（人教版）/2.除数是一位数的除法/除数是一位数的除法', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_数学_root, 'y3s2_数学_u03', '3.复式统计表', '/数学（人教版）/3.复式统计表', 1, 'unit', 3, '{"volumeKey": "y3s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y3s2_数学_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_数学_u03, 'y3s2_数学_u03_l01', '复式统计表', '/数学（人教版）/3.复式统计表/复式统计表', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_数学_root, 'y3s2_数学_u04', '4.两位数乘两位数', '/数学（人教版）/4.两位数乘两位数', 1, 'unit', 4, '{"volumeKey": "y3s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y3s2_数学_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_数学_u04, 'y3s2_数学_u04_l01', '两位数乘两位数', '/数学（人教版）/4.两位数乘两位数/两位数乘两位数', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_数学_root, 'y3s2_数学_u05', '5.面积', '/数学（人教版）/5.面积', 1, 'unit', 5, '{"volumeKey": "y3s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y3s2_数学_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_数学_u05, 'y3s2_数学_u05_l01', '面积', '/数学（人教版）/5.面积/面积', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_数学_root, 'y3s2_数学_u06', '6.年月日', '/数学（人教版）/6.年月日', 1, 'unit', 6, '{"volumeKey": "y3s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y3s2_数学_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_数学_u06, 'y3s2_数学_u06_l01', '年月日', '/数学（人教版）/6.年月日/年月日', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_数学_root, 'y3s2_数学_u07', '7.小数初步认识', '/数学（人教版）/7.小数初步认识', 1, 'unit', 7, '{"volumeKey": "y3s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y3s2_数学_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_数学_u07, 'y3s2_数学_u07_l01', '小数初步认识', '/数学（人教版）/7.小数初步认识/小数初步认识', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_数学_root, 'y3s2_数学_u08', '8.搭配问题', '/数学（人教版）/8.搭配问题', 1, 'unit', 8, '{"volumeKey": "y3s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y3s2_数学_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_数学_u08, 'y3s2_数学_u08_l01', '搭配问题', '/数学（人教版）/8.搭配问题/搭配问题', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "数学"}', 1);

-- y3s2 英语（人教PEP）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y3s2_英语_root', '英语（人教PEP）', '/英语（人教PEP）', 0, 'folder', 3, '{"volumeKey": "y3s2", "subject": "英语", "edition": "人教PEP", "defaultModule": "同步备课"}', 1);
SET @y3s2_英语_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_英语_root, 'y3s2_英语_u01', 'Unit1 Welcome back', '/英语（人教PEP）/Unit1 Welcome back', 1, 'unit', 1, '{"volumeKey": "y3s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y3s2_英语_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_英语_u01, 'y3s2_英语_u01_l01', '返校', '/英语（人教PEP）/Unit1 Welcome back/返校', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_英语_root, 'y3s2_英语_u02', 'Unit2 My favourite season', '/英语（人教PEP）/Unit2 My favourite season', 1, 'unit', 2, '{"volumeKey": "y3s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y3s2_英语_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_英语_u02, 'y3s2_英语_u02_l01', '季节', '/英语（人教PEP）/Unit2 My favourite season/季节', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_英语_root, 'y3s2_英语_u03', 'Unit3 My school calendar', '/英语（人教PEP）/Unit3 My school calendar', 1, 'unit', 3, '{"volumeKey": "y3s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y3s2_英语_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_英语_u03, 'y3s2_英语_u03_l01', '校历', '/英语（人教PEP）/Unit3 My school calendar/校历', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_英语_root, 'y3s2_英语_u04', 'Unit4 When is', '/英语（人教PEP）/Unit4 When is', 1, 'unit', 4, '{"volumeKey": "y3s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y3s2_英语_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_英语_u04, 'y3s2_英语_u04_l01', '日期', '/英语（人教PEP）/Unit4 When is/日期', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_英语_root, 'y3s2_英语_u05', 'Unit5 Whose', '/英语（人教PEP）/Unit5 Whose', 1, 'unit', 5, '{"volumeKey": "y3s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y3s2_英语_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_英语_u05, 'y3s2_英语_u05_l01', '所属', '/英语（人教PEP）/Unit5 Whose/所属', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_英语_root, 'y3s2_英语_u06', 'Unit6 Work quietly', '/英语（人教PEP）/Unit6 Work quietly', 1, 'unit', 6, '{"volumeKey": "y3s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y3s2_英语_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y3s2_英语_u06, 'y3s2_英语_u06_l01', '行为规范', '/英语（人教PEP）/Unit6 Work quietly/行为规范', 2, 'lesson', 1, '{"volumeKey": "y3s2", "subject": "英语"}', 1);

-- y4s1 语文（统编版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y4s1_语文_root', '语文（统编版）', '/语文（统编版）', 0, 'folder', 1, '{"volumeKey": "y4s1", "subject": "语文", "edition": "统编版", "defaultModule": "同步备课"}', 1);
SET @y4s1_语文_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_语文_root, 'y4s1_语文_u01', '第一单元', '/语文（统编版）/第一单元', 1, 'unit', 1, '{"volumeKey": "y4s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y4s1_语文_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_语文_u01, 'y4s1_语文_u01_l01', '1 观潮', '/语文（统编版）/第一单元/1 观潮', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u01, 'y4s1_语文_u01_l02', '2 走月亮', '/语文（统编版）/第一单元/2 走月亮', 2, 'lesson', 2, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u01, 'y4s1_语文_u01_l03', '3 现代诗二首（秋晚的江上、花牛歌）', '/语文（统编版）/第一单元/3 现代诗二首（秋晚的江上、花牛歌）', 2, 'lesson', 3, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u01, 'y4s1_语文_u01_l04', '4* 繁星', '/语文（统编版）/第一单元/4* 繁星', 2, 'lesson', 4, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u01, 'y4s1_语文_u01_l05', '口语交际：我们与环境', '/语文（统编版）/第一单元/口语交际：我们与环境', 2, 'lesson', 5, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u01, 'y4s1_语文_u01_l06', '习作：推荐一个好地方', '/语文（统编版）/第一单元/习作：推荐一个好地方', 2, 'lesson', 6, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u01, 'y4s1_语文_u01_l07', '语文园地', '/语文（统编版）/第一单元/语文园地', 2, 'lesson', 7, '{"volumeKey": "y4s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_语文_root, 'y4s1_语文_u02', '第二单元', '/语文（统编版）/第二单元', 1, 'unit', 2, '{"volumeKey": "y4s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y4s1_语文_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_语文_u02, 'y4s1_语文_u02_l01', '5 一个豆荚里的五粒豆', '/语文（统编版）/第二单元/5 一个豆荚里的五粒豆', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u02, 'y4s1_语文_u02_l02', '6 蝙蝠和雷达', '/语文（统编版）/第二单元/6 蝙蝠和雷达', 2, 'lesson', 2, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u02, 'y4s1_语文_u02_l03', '7 呼风唤雨的世纪', '/语文（统编版）/第二单元/7 呼风唤雨的世纪', 2, 'lesson', 3, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u02, 'y4s1_语文_u02_l04', '8* 蝴蝶的家', '/语文（统编版）/第二单元/8* 蝴蝶的家', 2, 'lesson', 4, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u02, 'y4s1_语文_u02_l05', '习作：小小“动物园”', '/语文（统编版）/第二单元/习作：小小“动物园”', 2, 'lesson', 5, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u02, 'y4s1_语文_u02_l06', '语文园地', '/语文（统编版）/第二单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y4s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_语文_root, 'y4s1_语文_u03', '第三单元', '/语文（统编版）/第三单元', 1, 'unit', 3, '{"volumeKey": "y4s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y4s1_语文_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_语文_u03, 'y4s1_语文_u03_l01', '9 古诗三首（暮江吟、题西林壁、雪梅）', '/语文（统编版）/第三单元/9 古诗三首（暮江吟、题西林壁、雪梅）', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u03, 'y4s1_语文_u03_l02', '10 爬山虎的脚', '/语文（统编版）/第三单元/10 爬山虎的脚', 2, 'lesson', 2, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u03, 'y4s1_语文_u03_l03', '11 蟋蟀的住宅', '/语文（统编版）/第三单元/11 蟋蟀的住宅', 2, 'lesson', 3, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u03, 'y4s1_语文_u03_l04', '口语交际：爱护眼睛，保护视力', '/语文（统编版）/第三单元/口语交际：爱护眼睛，保护视力', 2, 'lesson', 4, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u03, 'y4s1_语文_u03_l05', '习作：写观察日记', '/语文（统编版）/第三单元/习作：写观察日记', 2, 'lesson', 5, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u03, 'y4s1_语文_u03_l06', '语文园地', '/语文（统编版）/第三单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y4s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_语文_root, 'y4s1_语文_u04', '第四单元', '/语文（统编版）/第四单元', 1, 'unit', 4, '{"volumeKey": "y4s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y4s1_语文_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_语文_u04, 'y4s1_语文_u04_l01', '12 盘古开天地', '/语文（统编版）/第四单元/12 盘古开天地', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u04, 'y4s1_语文_u04_l02', '13 精卫填海', '/语文（统编版）/第四单元/13 精卫填海', 2, 'lesson', 2, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u04, 'y4s1_语文_u04_l03', '14 普罗米修斯', '/语文（统编版）/第四单元/14 普罗米修斯', 2, 'lesson', 3, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u04, 'y4s1_语文_u04_l04', '15* 女娲补天', '/语文（统编版）/第四单元/15* 女娲补天', 2, 'lesson', 4, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u04, 'y4s1_语文_u04_l05', '习作：我和____过一天', '/语文（统编版）/第四单元/习作：我和____过一天', 2, 'lesson', 5, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u04, 'y4s1_语文_u04_l06', '语文园地', '/语文（统编版）/第四单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u04, 'y4s1_语文_u04_l07', '快乐读书吧：很久很久以前（神话故事）', '/语文（统编版）/第四单元/快乐读书吧：很久很久以前（神话故事）', 2, 'lesson', 7, '{"volumeKey": "y4s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_语文_root, 'y4s1_语文_u05', '第五单元', '/语文（统编版）/第五单元', 1, 'unit', 5, '{"volumeKey": "y4s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y4s1_语文_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_语文_u05, 'y4s1_语文_u05_l01', '16 麻雀', '/语文（统编版）/第五单元/16 麻雀', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u05, 'y4s1_语文_u05_l02', '17 爬天都峰', '/语文（统编版）/第五单元/17 爬天都峰', 2, 'lesson', 2, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u05, 'y4s1_语文_u05_l03', '习作例文：我家的杏熟了、小木船', '/语文（统编版）/第五单元/习作例文：我家的杏熟了、小木船', 2, 'lesson', 3, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u05, 'y4s1_语文_u05_l04', '习作：生活万花筒', '/语文（统编版）/第五单元/习作：生活万花筒', 2, 'lesson', 4, '{"volumeKey": "y4s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_语文_root, 'y4s1_语文_u06', '第六单元', '/语文（统编版）/第六单元', 1, 'unit', 6, '{"volumeKey": "y4s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y4s1_语文_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_语文_u06, 'y4s1_语文_u06_l01', '18 牛和鹅', '/语文（统编版）/第六单元/18 牛和鹅', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u06, 'y4s1_语文_u06_l02', '19 一只窝囊的大老虎', '/语文（统编版）/第六单元/19 一只窝囊的大老虎', 2, 'lesson', 2, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u06, 'y4s1_语文_u06_l03', '20 陀螺', '/语文（统编版）/第六单元/20 陀螺', 2, 'lesson', 3, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u06, 'y4s1_语文_u06_l04', '口语交际：安慰', '/语文（统编版）/第六单元/口语交际：安慰', 2, 'lesson', 4, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u06, 'y4s1_语文_u06_l05', '习作：记一次游戏', '/语文（统编版）/第六单元/习作：记一次游戏', 2, 'lesson', 5, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u06, 'y4s1_语文_u06_l06', '语文园地', '/语文（统编版）/第六单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y4s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_语文_root, 'y4s1_语文_u07', '第七单元', '/语文（统编版）/第七单元', 1, 'unit', 7, '{"volumeKey": "y4s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y4s1_语文_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_语文_u07, 'y4s1_语文_u07_l01', '21 古诗三首（出塞、凉州词、夏日绝句）', '/语文（统编版）/第七单元/21 古诗三首（出塞、凉州词、夏日绝句）', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u07, 'y4s1_语文_u07_l02', '22 为中华之崛起而读书', '/语文（统编版）/第七单元/22 为中华之崛起而读书', 2, 'lesson', 2, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u07, 'y4s1_语文_u07_l03', '23 梅兰芳蓄须', '/语文（统编版）/第七单元/23 梅兰芳蓄须', 2, 'lesson', 3, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u07, 'y4s1_语文_u07_l04', '24* 延安，我把你追寻', '/语文（统编版）/第七单元/24* 延安，我把你追寻', 2, 'lesson', 4, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u07, 'y4s1_语文_u07_l05', '习作：写信', '/语文（统编版）/第七单元/习作：写信', 2, 'lesson', 5, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u07, 'y4s1_语文_u07_l06', '语文园地', '/语文（统编版）/第七单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y4s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_语文_root, 'y4s1_语文_u08', '第八单元', '/语文（统编版）/第八单元', 1, 'unit', 8, '{"volumeKey": "y4s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y4s1_语文_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_语文_u08, 'y4s1_语文_u08_l01', '25 王戎不取道旁李', '/语文（统编版）/第八单元/25 王戎不取道旁李', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u08, 'y4s1_语文_u08_l02', '26 西门豹治邺', '/语文（统编版）/第八单元/26 西门豹治邺', 2, 'lesson', 2, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u08, 'y4s1_语文_u08_l03', '27* 故事二则（扁鹊治病、纪昌学射）', '/语文（统编版）/第八单元/27* 故事二则（扁鹊治病、纪昌学射）', 2, 'lesson', 3, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u08, 'y4s1_语文_u08_l04', '口语交际：讲历史人物故事', '/语文（统编版）/第八单元/口语交际：讲历史人物故事', 2, 'lesson', 4, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u08, 'y4s1_语文_u08_l05', '习作：我的心儿怦怦跳', '/语文（统编版）/第八单元/习作：我的心儿怦怦跳', 2, 'lesson', 5, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u08, 'y4s1_语文_u08_l06', '语文园地', '/语文（统编版）/第八单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y4s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_语文_root, 'y4s1_语文_u09', '附录', '/语文（统编版）/附录', 1, 'unit', 9, '{"volumeKey": "y4s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y4s1_语文_u09 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_语文_u09, 'y4s1_语文_u09_l01', '识字表', '/语文（统编版）/附录/识字表', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u09, 'y4s1_语文_u09_l02', '写字表', '/语文（统编版）/附录/写字表', 2, 'lesson', 2, '{"volumeKey": "y4s1", "subject": "语文"}', 1),
(@scheme_id, @y4s1_语文_u09, 'y4s1_语文_u09_l03', '词语表', '/语文（统编版）/附录/词语表', 2, 'lesson', 3, '{"volumeKey": "y4s1", "subject": "语文"}', 1);

-- y4s1 数学（人教版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y4s1_数学_root', '数学（人教版）', '/数学（人教版）', 0, 'folder', 2, '{"volumeKey": "y4s1", "subject": "数学", "edition": "人教版", "defaultModule": "同步备课"}', 1);
SET @y4s1_数学_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_数学_root, 'y4s1_数学_u01', '1.大数的认识', '/数学（人教版）/1.大数的认识', 1, 'unit', 1, '{"volumeKey": "y4s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y4s1_数学_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_数学_u01, 'y4s1_数学_u01_l01', '大数的认识', '/数学（人教版）/1.大数的认识/大数的认识', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_数学_root, 'y4s1_数学_u02', '2.公顷平方千米', '/数学（人教版）/2.公顷平方千米', 1, 'unit', 2, '{"volumeKey": "y4s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y4s1_数学_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_数学_u02, 'y4s1_数学_u02_l01', '公顷平方千米', '/数学（人教版）/2.公顷平方千米/公顷平方千米', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_数学_root, 'y4s1_数学_u03', '3.角的度量', '/数学（人教版）/3.角的度量', 1, 'unit', 3, '{"volumeKey": "y4s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y4s1_数学_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_数学_u03, 'y4s1_数学_u03_l01', '角的度量', '/数学（人教版）/3.角的度量/角的度量', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_数学_root, 'y4s1_数学_u04', '4.三位数乘两位数', '/数学（人教版）/4.三位数乘两位数', 1, 'unit', 4, '{"volumeKey": "y4s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y4s1_数学_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_数学_u04, 'y4s1_数学_u04_l01', '三位数乘两位数', '/数学（人教版）/4.三位数乘两位数/三位数乘两位数', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_数学_root, 'y4s1_数学_u05', '5.平行四边形梯形', '/数学（人教版）/5.平行四边形梯形', 1, 'unit', 5, '{"volumeKey": "y4s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y4s1_数学_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_数学_u05, 'y4s1_数学_u05_l01', '平行四边形梯形', '/数学（人教版）/5.平行四边形梯形/平行四边形梯形', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_数学_root, 'y4s1_数学_u06', '6.除数是两位数除法', '/数学（人教版）/6.除数是两位数除法', 1, 'unit', 6, '{"volumeKey": "y4s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y4s1_数学_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_数学_u06, 'y4s1_数学_u06_l01', '除数是两位数除法', '/数学（人教版）/6.除数是两位数除法/除数是两位数除法', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_数学_root, 'y4s1_数学_u07', '7.条形统计图', '/数学（人教版）/7.条形统计图', 1, 'unit', 7, '{"volumeKey": "y4s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y4s1_数学_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_数学_u07, 'y4s1_数学_u07_l01', '条形统计图', '/数学（人教版）/7.条形统计图/条形统计图', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_数学_root, 'y4s1_数学_u08', '8.优化', '/数学（人教版）/8.优化', 1, 'unit', 8, '{"volumeKey": "y4s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y4s1_数学_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_数学_u08, 'y4s1_数学_u08_l01', '优化', '/数学（人教版）/8.优化/优化', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "数学"}', 1);

-- y4s1 英语（人教PEP）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y4s1_英语_root', '英语（人教PEP）', '/英语（人教PEP）', 0, 'folder', 3, '{"volumeKey": "y4s1", "subject": "英语", "edition": "人教PEP", "defaultModule": "同步备课"}', 1);
SET @y4s1_英语_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_英语_root, 'y4s1_英语_u01', 'Unit1 Where is', '/英语（人教PEP）/Unit1 Where is', 1, 'unit', 1, '{"volumeKey": "y4s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y4s1_英语_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_英语_u01, 'y4s1_英语_u01_l01', '方位', '/英语（人教PEP）/Unit1 Where is/方位', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_英语_root, 'y4s1_英语_u02', 'Unit2 Ways to go school', '/英语（人教PEP）/Unit2 Ways to go school', 1, 'unit', 2, '{"volumeKey": "y4s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y4s1_英语_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_英语_u02, 'y4s1_英语_u02_l01', '交通方式', '/英语（人教PEP）/Unit2 Ways to go school/交通方式', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_英语_root, 'y4s1_英语_u03', 'Unit3 Weekend plan', '/英语（人教PEP）/Unit3 Weekend plan', 1, 'unit', 3, '{"volumeKey": "y4s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y4s1_英语_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_英语_u03, 'y4s1_英语_u03_l01', '周末计划', '/英语（人教PEP）/Unit3 Weekend plan/周末计划', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_英语_root, 'y4s1_英语_u04', 'Unit4 Pen pal', '/英语（人教PEP）/Unit4 Pen pal', 1, 'unit', 4, '{"volumeKey": "y4s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y4s1_英语_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_英语_u04, 'y4s1_英语_u04_l01', '笔友', '/英语（人教PEP）/Unit4 Pen pal/笔友', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_英语_root, 'y4s1_英语_u05', 'Unit5 Jobs', '/英语（人教PEP）/Unit5 Jobs', 1, 'unit', 5, '{"volumeKey": "y4s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y4s1_英语_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_英语_u05, 'y4s1_英语_u05_l01', '职业', '/英语（人教PEP）/Unit5 Jobs/职业', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_英语_root, 'y4s1_英语_u06', 'Unit6 Feelings', '/英语（人教PEP）/Unit6 Feelings', 1, 'unit', 6, '{"volumeKey": "y4s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y4s1_英语_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s1_英语_u06, 'y4s1_英语_u06_l01', '情绪', '/英语（人教PEP）/Unit6 Feelings/情绪', 2, 'lesson', 1, '{"volumeKey": "y4s1", "subject": "英语"}', 1);

-- y4s2 语文（统编版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y4s2_语文_root', '语文（统编版）', '/语文（统编版）', 0, 'folder', 1, '{"volumeKey": "y4s2", "subject": "语文", "edition": "统编版", "defaultModule": "同步备课"}', 1);
SET @y4s2_语文_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_语文_root, 'y4s2_语文_u01', '第一单元', '/语文（统编版）/第一单元', 1, 'unit', 1, '{"volumeKey": "y4s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y4s2_语文_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_语文_u01, 'y4s2_语文_u01_l01', '1 古诗词三首（四时田园杂兴·其二十五、宿新市徐公店、清平乐·村居）', '/语文（统编版）/第一单元/1 古诗词三首（四时田园杂兴·其二十五、宿新市徐公店、清平乐·村居）', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u01, 'y4s2_语文_u01_l02', '2 乡下人家', '/语文（统编版）/第一单元/2 乡下人家', 2, 'lesson', 2, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u01, 'y4s2_语文_u01_l03', '3 天窗', '/语文（统编版）/第一单元/3 天窗', 2, 'lesson', 3, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u01, 'y4s2_语文_u01_l04', '4* 三月桃花水', '/语文（统编版）/第一单元/4* 三月桃花水', 2, 'lesson', 4, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u01, 'y4s2_语文_u01_l05', '口语交际：转述', '/语文（统编版）/第一单元/口语交际：转述', 2, 'lesson', 5, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u01, 'y4s2_语文_u01_l06', '习作：我的乐园', '/语文（统编版）/第一单元/习作：我的乐园', 2, 'lesson', 6, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u01, 'y4s2_语文_u01_l07', '语文园地', '/语文（统编版）/第一单元/语文园地', 2, 'lesson', 7, '{"volumeKey": "y4s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_语文_root, 'y4s2_语文_u02', '第二单元', '/语文（统编版）/第二单元', 1, 'unit', 2, '{"volumeKey": "y4s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y4s2_语文_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_语文_u02, 'y4s2_语文_u02_l01', '5 琥珀', '/语文（统编版）/第二单元/5 琥珀', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u02, 'y4s2_语文_u02_l02', '6 飞向蓝天的恐龙', '/语文（统编版）/第二单元/6 飞向蓝天的恐龙', 2, 'lesson', 2, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u02, 'y4s2_语文_u02_l03', '7 纳米技术就在我们身边', '/语文（统编版）/第二单元/7 纳米技术就在我们身边', 2, 'lesson', 3, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u02, 'y4s2_语文_u02_l04', '8* 千年梦圆在今朝', '/语文（统编版）/第二单元/8* 千年梦圆在今朝', 2, 'lesson', 4, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u02, 'y4s2_语文_u02_l05', '口语交际：说新闻', '/语文（统编版）/第二单元/口语交际：说新闻', 2, 'lesson', 5, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u02, 'y4s2_语文_u02_l06', '习作：我的奇思妙想', '/语文（统编版）/第二单元/习作：我的奇思妙想', 2, 'lesson', 6, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u02, 'y4s2_语文_u02_l07', '语文园地', '/语文（统编版）/第二单元/语文园地', 2, 'lesson', 7, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u02, 'y4s2_语文_u02_l08', '快乐读书吧：十万个为什么（科普读物）', '/语文（统编版）/第二单元/快乐读书吧：十万个为什么（科普读物）', 2, 'lesson', 8, '{"volumeKey": "y4s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_语文_root, 'y4s2_语文_u03', '第三单元', '/语文（统编版）/第三单元', 1, 'unit', 3, '{"volumeKey": "y4s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y4s2_语文_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_语文_u03, 'y4s2_语文_u03_l01', '9 短诗三首（繁星·七一、繁星·一三一、繁星·一五九）', '/语文（统编版）/第三单元/9 短诗三首（繁星·七一、繁星·一三一、繁星·一五九）', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u03, 'y4s2_语文_u03_l02', '10 绿', '/语文（统编版）/第三单元/10 绿', 2, 'lesson', 2, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u03, 'y4s2_语文_u03_l03', '11 白桦', '/语文（统编版）/第三单元/11 白桦', 2, 'lesson', 3, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u03, 'y4s2_语文_u03_l04', '12* 在天晴了的时候', '/语文（统编版）/第三单元/12* 在天晴了的时候', 2, 'lesson', 4, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u03, 'y4s2_语文_u03_l05', '综合性学习：轻叩诗歌大门', '/语文（统编版）/第三单元/综合性学习：轻叩诗歌大门', 2, 'lesson', 5, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u03, 'y4s2_语文_u03_l06', '语文园地', '/语文（统编版）/第三单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y4s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_语文_root, 'y4s2_语文_u04', '第四单元', '/语文（统编版）/第四单元', 1, 'unit', 4, '{"volumeKey": "y4s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y4s2_语文_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_语文_u04, 'y4s2_语文_u04_l01', '13 猫', '/语文（统编版）/第四单元/13 猫', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u04, 'y4s2_语文_u04_l02', '14 母鸡', '/语文（统编版）/第四单元/14 母鸡', 2, 'lesson', 2, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u04, 'y4s2_语文_u04_l03', '15 白鹅', '/语文（统编版）/第四单元/15 白鹅', 2, 'lesson', 3, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u04, 'y4s2_语文_u04_l04', '习作：我的动物朋友', '/语文（统编版）/第四单元/习作：我的动物朋友', 2, 'lesson', 4, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u04, 'y4s2_语文_u04_l05', '语文园地', '/语文（统编版）/第四单元/语文园地', 2, 'lesson', 5, '{"volumeKey": "y4s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_语文_root, 'y4s2_语文_u05', '第五单元', '/语文（统编版）/第五单元', 1, 'unit', 5, '{"volumeKey": "y4s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y4s2_语文_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_语文_u05, 'y4s2_语文_u05_l01', '16 海上日出', '/语文（统编版）/第五单元/16 海上日出', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u05, 'y4s2_语文_u05_l02', '17 记金华的双龙洞', '/语文（统编版）/第五单元/17 记金华的双龙洞', 2, 'lesson', 2, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u05, 'y4s2_语文_u05_l03', '习作例文：颐和园、七月的天山', '/语文（统编版）/第五单元/习作例文：颐和园、七月的天山', 2, 'lesson', 3, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u05, 'y4s2_语文_u05_l04', '习作：游____', '/语文（统编版）/第五单元/习作：游____', 2, 'lesson', 4, '{"volumeKey": "y4s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_语文_root, 'y4s2_语文_u06', '第六单元', '/语文（统编版）/第六单元', 1, 'unit', 6, '{"volumeKey": "y4s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y4s2_语文_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_语文_u06, 'y4s2_语文_u06_l01', '18 小英雄雨来（节选）', '/语文（统编版）/第六单元/18 小英雄雨来（节选）', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u06, 'y4s2_语文_u06_l02', '19* 我们家的男子汉', '/语文（统编版）/第六单元/19* 我们家的男子汉', 2, 'lesson', 2, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u06, 'y4s2_语文_u06_l03', '20* 芦花鞋', '/语文（统编版）/第六单元/20* 芦花鞋', 2, 'lesson', 3, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u06, 'y4s2_语文_u06_l04', '口语交际：朋友相处的秘诀', '/语文（统编版）/第六单元/口语交际：朋友相处的秘诀', 2, 'lesson', 4, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u06, 'y4s2_语文_u06_l05', '习作：我学会了____', '/语文（统编版）/第六单元/习作：我学会了____', 2, 'lesson', 5, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u06, 'y4s2_语文_u06_l06', '语文园地', '/语文（统编版）/第六单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y4s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_语文_root, 'y4s2_语文_u07', '第七单元', '/语文（统编版）/第七单元', 1, 'unit', 7, '{"volumeKey": "y4s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y4s2_语文_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_语文_u07, 'y4s2_语文_u07_l01', '21 古诗三首（芙蓉楼送辛渐、塞下曲、墨梅）', '/语文（统编版）/第七单元/21 古诗三首（芙蓉楼送辛渐、塞下曲、墨梅）', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u07, 'y4s2_语文_u07_l02', '22 文言文二则（囊萤夜读、铁杵成针）', '/语文（统编版）/第七单元/22 文言文二则（囊萤夜读、铁杵成针）', 2, 'lesson', 2, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u07, 'y4s2_语文_u07_l03', '23 “诺曼底”号遇难记', '/语文（统编版）/第七单元/23 “诺曼底”号遇难记', 2, 'lesson', 3, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u07, 'y4s2_语文_u07_l04', '24* 黄继光', '/语文（统编版）/第七单元/24* 黄继光', 2, 'lesson', 4, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u07, 'y4s2_语文_u07_l05', '口语交际：自我介绍', '/语文（统编版）/第七单元/口语交际：自我介绍', 2, 'lesson', 5, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u07, 'y4s2_语文_u07_l06', '习作：我的“自画像”', '/语文（统编版）/第七单元/习作：我的“自画像”', 2, 'lesson', 6, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u07, 'y4s2_语文_u07_l07', '语文园地', '/语文（统编版）/第七单元/语文园地', 2, 'lesson', 7, '{"volumeKey": "y4s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_语文_root, 'y4s2_语文_u08', '第八单元', '/语文（统编版）/第八单元', 1, 'unit', 8, '{"volumeKey": "y4s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y4s2_语文_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_语文_u08, 'y4s2_语文_u08_l01', '25 宝葫芦的秘密（节选）', '/语文（统编版）/第八单元/25 宝葫芦的秘密（节选）', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u08, 'y4s2_语文_u08_l02', '26 巨人的花园', '/语文（统编版）/第八单元/26 巨人的花园', 2, 'lesson', 2, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u08, 'y4s2_语文_u08_l03', '27* 海的女儿', '/语文（统编版）/第八单元/27* 海的女儿', 2, 'lesson', 3, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u08, 'y4s2_语文_u08_l04', '习作：故事新编', '/语文（统编版）/第八单元/习作：故事新编', 2, 'lesson', 4, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u08, 'y4s2_语文_u08_l05', '语文园地', '/语文（统编版）/第八单元/语文园地', 2, 'lesson', 5, '{"volumeKey": "y4s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_语文_root, 'y4s2_语文_u09', '附录', '/语文（统编版）/附录', 1, 'unit', 9, '{"volumeKey": "y4s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y4s2_语文_u09 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_语文_u09, 'y4s2_语文_u09_l01', '识字表', '/语文（统编版）/附录/识字表', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u09, 'y4s2_语文_u09_l02', '写字表', '/语文（统编版）/附录/写字表', 2, 'lesson', 2, '{"volumeKey": "y4s2", "subject": "语文"}', 1),
(@scheme_id, @y4s2_语文_u09, 'y4s2_语文_u09_l03', '词语表', '/语文（统编版）/附录/词语表', 2, 'lesson', 3, '{"volumeKey": "y4s2", "subject": "语文"}', 1);

-- y4s2 数学（人教版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y4s2_数学_root', '数学（人教版）', '/数学（人教版）', 0, 'folder', 2, '{"volumeKey": "y4s2", "subject": "数学", "edition": "人教版", "defaultModule": "同步备课"}', 1);
SET @y4s2_数学_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_数学_root, 'y4s2_数学_u01', '1.四则运算', '/数学（人教版）/1.四则运算', 1, 'unit', 1, '{"volumeKey": "y4s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y4s2_数学_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_数学_u01, 'y4s2_数学_u01_l01', '四则运算', '/数学（人教版）/1.四则运算/四则运算', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_数学_root, 'y4s2_数学_u02', '2.观察物体二', '/数学（人教版）/2.观察物体二', 1, 'unit', 2, '{"volumeKey": "y4s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y4s2_数学_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_数学_u02, 'y4s2_数学_u02_l01', '观察物体二', '/数学（人教版）/2.观察物体二/观察物体二', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_数学_root, 'y4s2_数学_u03', '3.运算定律', '/数学（人教版）/3.运算定律', 1, 'unit', 3, '{"volumeKey": "y4s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y4s2_数学_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_数学_u03, 'y4s2_数学_u03_l01', '运算定律', '/数学（人教版）/3.运算定律/运算定律', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_数学_root, 'y4s2_数学_u04', '4.小数意义性质', '/数学（人教版）/4.小数意义性质', 1, 'unit', 4, '{"volumeKey": "y4s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y4s2_数学_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_数学_u04, 'y4s2_数学_u04_l01', '小数意义性质', '/数学（人教版）/4.小数意义性质/小数意义性质', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_数学_root, 'y4s2_数学_u05', '5.三角形', '/数学（人教版）/5.三角形', 1, 'unit', 5, '{"volumeKey": "y4s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y4s2_数学_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_数学_u05, 'y4s2_数学_u05_l01', '三角形', '/数学（人教版）/5.三角形/三角形', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_数学_root, 'y4s2_数学_u06', '6.小数加减法', '/数学（人教版）/6.小数加减法', 1, 'unit', 6, '{"volumeKey": "y4s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y4s2_数学_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_数学_u06, 'y4s2_数学_u06_l01', '小数加减法', '/数学（人教版）/6.小数加减法/小数加减法', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_数学_root, 'y4s2_数学_u07', '7.图形运动', '/数学（人教版）/7.图形运动', 1, 'unit', 7, '{"volumeKey": "y4s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y4s2_数学_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_数学_u07, 'y4s2_数学_u07_l01', '图形运动', '/数学（人教版）/7.图形运动/图形运动', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_数学_root, 'y4s2_数学_u08', '8.平均数鸡兔同笼', '/数学（人教版）/8.平均数鸡兔同笼', 1, 'unit', 8, '{"volumeKey": "y4s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y4s2_数学_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_数学_u08, 'y4s2_数学_u08_l01', '平均数', '/数学（人教版）/8.平均数鸡兔同笼/平均数', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "数学"}', 1),
(@scheme_id, @y4s2_数学_u08, 'y4s2_数学_u08_l02', '鸡兔同笼', '/数学（人教版）/8.平均数鸡兔同笼/鸡兔同笼', 2, 'lesson', 2, '{"volumeKey": "y4s2", "subject": "数学"}', 1);

-- y4s2 英语（人教PEP）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y4s2_英语_root', '英语（人教PEP）', '/英语（人教PEP）', 0, 'folder', 3, '{"volumeKey": "y4s2", "subject": "英语", "edition": "人教PEP", "defaultModule": "同步备课"}', 1);
SET @y4s2_英语_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_英语_root, 'y4s2_英语_u01', 'Unit1 Taller', '/英语（人教PEP）/Unit1 Taller', 1, 'unit', 1, '{"volumeKey": "y4s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y4s2_英语_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_英语_u01, 'y4s2_英语_u01_l01', '身高体型', '/英语（人教PEP）/Unit1 Taller/身高体型', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_英语_root, 'y4s2_英语_u02', 'Unit2 Last weekend', '/英语（人教PEP）/Unit2 Last weekend', 1, 'unit', 2, '{"volumeKey": "y4s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y4s2_英语_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_英语_u02, 'y4s2_英语_u02_l01', '过去周末', '/英语（人教PEP）/Unit2 Last weekend/过去周末', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_英语_root, 'y4s2_英语_u03', 'Unit3 Where did you go', '/英语（人教PEP）/Unit3 Where did you go', 1, 'unit', 3, '{"volumeKey": "y4s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y4s2_英语_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_英语_u03, 'y4s2_英语_u03_l01', '出行过去式', '/英语（人教PEP）/Unit3 Where did you go/出行过去式', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "英语"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_英语_root, 'y4s2_英语_u04', 'Unit4 Then and now', '/英语（人教PEP）/Unit4 Then and now', 1, 'unit', 4, '{"volumeKey": "y4s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y4s2_英语_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y4s2_英语_u04, 'y4s2_英语_u04_l01', '今昔对比', '/英语（人教PEP）/Unit4 Then and now/今昔对比', 2, 'lesson', 1, '{"volumeKey": "y4s2", "subject": "英语"}', 1);

-- y5s1 语文（统编版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y5s1_语文_root', '语文（统编版）', '/语文（统编版）', 0, 'folder', 1, '{"volumeKey": "y5s1", "subject": "语文", "edition": "统编版", "defaultModule": "同步备课"}', 1);
SET @y5s1_语文_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_语文_root, 'y5s1_语文_u01', '第一单元', '/语文（统编版）/第一单元', 1, 'unit', 1, '{"volumeKey": "y5s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y5s1_语文_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_语文_u01, 'y5s1_语文_u01_l01', '1 白鹭', '/语文（统编版）/第一单元/1 白鹭', 2, 'lesson', 1, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u01, 'y5s1_语文_u01_l02', '2 落花生', '/语文（统编版）/第一单元/2 落花生', 2, 'lesson', 2, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u01, 'y5s1_语文_u01_l03', '3 桂花雨', '/语文（统编版）/第一单元/3 桂花雨', 2, 'lesson', 3, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u01, 'y5s1_语文_u01_l04', '4* 珍珠鸟', '/语文（统编版）/第一单元/4* 珍珠鸟', 2, 'lesson', 4, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u01, 'y5s1_语文_u01_l05', '口语交际：制定班级公约', '/语文（统编版）/第一单元/口语交际：制定班级公约', 2, 'lesson', 5, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u01, 'y5s1_语文_u01_l06', '习作：我的心爱之物', '/语文（统编版）/第一单元/习作：我的心爱之物', 2, 'lesson', 6, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u01, 'y5s1_语文_u01_l07', '语文园地', '/语文（统编版）/第一单元/语文园地', 2, 'lesson', 7, '{"volumeKey": "y5s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_语文_root, 'y5s1_语文_u02', '第二单元', '/语文（统编版）/第二单元', 1, 'unit', 2, '{"volumeKey": "y5s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y5s1_语文_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_语文_u02, 'y5s1_语文_u02_l01', '5 搭石', '/语文（统编版）/第二单元/5 搭石', 2, 'lesson', 1, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u02, 'y5s1_语文_u02_l02', '6 将相和', '/语文（统编版）/第二单元/6 将相和', 2, 'lesson', 2, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u02, 'y5s1_语文_u02_l03', '7 什么比猎豹的速度更快', '/语文（统编版）/第二单元/7 什么比猎豹的速度更快', 2, 'lesson', 3, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u02, 'y5s1_语文_u02_l04', '8 冀中的地道战', '/语文（统编版）/第二单元/8 冀中的地道战', 2, 'lesson', 4, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u02, 'y5s1_语文_u02_l05', '习作：“漫画”老师', '/语文（统编版）/第二单元/习作：“漫画”老师', 2, 'lesson', 5, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u02, 'y5s1_语文_u02_l06', '语文园地', '/语文（统编版）/第二单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y5s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_语文_root, 'y5s1_语文_u03', '第三单元', '/语文（统编版）/第三单元', 1, 'unit', 3, '{"volumeKey": "y5s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y5s1_语文_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_语文_u03, 'y5s1_语文_u03_l01', '9 猎人海力布', '/语文（统编版）/第三单元/9 猎人海力布', 2, 'lesson', 1, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u03, 'y5s1_语文_u03_l02', '10 牛郎织女（一）', '/语文（统编版）/第三单元/10 牛郎织女（一）', 2, 'lesson', 2, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u03, 'y5s1_语文_u03_l03', '11* 牛郎织女（二）', '/语文（统编版）/第三单元/11* 牛郎织女（二）', 2, 'lesson', 3, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u03, 'y5s1_语文_u03_l04', '口语交际：讲民间故事', '/语文（统编版）/第三单元/口语交际：讲民间故事', 2, 'lesson', 4, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u03, 'y5s1_语文_u03_l05', '习作：缩写故事', '/语文（统编版）/第三单元/习作：缩写故事', 2, 'lesson', 5, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u03, 'y5s1_语文_u03_l06', '语文园地', '/语文（统编版）/第三单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u03, 'y5s1_语文_u03_l07', '快乐读书吧：从前有座山（民间故事）', '/语文（统编版）/第三单元/快乐读书吧：从前有座山（民间故事）', 2, 'lesson', 7, '{"volumeKey": "y5s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_语文_root, 'y5s1_语文_u04', '第四单元', '/语文（统编版）/第四单元', 1, 'unit', 4, '{"volumeKey": "y5s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y5s1_语文_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_语文_u04, 'y5s1_语文_u04_l01', '12 古诗三首（示儿、题临安邸、己亥杂诗）', '/语文（统编版）/第四单元/12 古诗三首（示儿、题临安邸、己亥杂诗）', 2, 'lesson', 1, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u04, 'y5s1_语文_u04_l02', '13 少年中国说（节选）', '/语文（统编版）/第四单元/13 少年中国说（节选）', 2, 'lesson', 2, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u04, 'y5s1_语文_u04_l03', '14 圆明园的毁灭', '/语文（统编版）/第四单元/14 圆明园的毁灭', 2, 'lesson', 3, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u04, 'y5s1_语文_u04_l04', '15* 小岛', '/语文（统编版）/第四单元/15* 小岛', 2, 'lesson', 4, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u04, 'y5s1_语文_u04_l05', '习作：二十年后的家乡', '/语文（统编版）/第四单元/习作：二十年后的家乡', 2, 'lesson', 5, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u04, 'y5s1_语文_u04_l06', '语文园地', '/语文（统编版）/第四单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y5s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_语文_root, 'y5s1_语文_u05', '第五单元', '/语文（统编版）/第五单元', 1, 'unit', 5, '{"volumeKey": "y5s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y5s1_语文_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_语文_u05, 'y5s1_语文_u05_l01', '16 太阳', '/语文（统编版）/第五单元/16 太阳', 2, 'lesson', 1, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u05, 'y5s1_语文_u05_l02', '17 松鼠', '/语文（统编版）/第五单元/17 松鼠', 2, 'lesson', 2, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u05, 'y5s1_语文_u05_l03', '习作例文：鲸、风向袋的制作', '/语文（统编版）/第五单元/习作例文：鲸、风向袋的制作', 2, 'lesson', 3, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u05, 'y5s1_语文_u05_l04', '习作：介绍一种事物', '/语文（统编版）/第五单元/习作：介绍一种事物', 2, 'lesson', 4, '{"volumeKey": "y5s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_语文_root, 'y5s1_语文_u06', '第六单元', '/语文（统编版）/第六单元', 1, 'unit', 6, '{"volumeKey": "y5s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y5s1_语文_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_语文_u06, 'y5s1_语文_u06_l01', '18 慈母情深', '/语文（统编版）/第六单元/18 慈母情深', 2, 'lesson', 1, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u06, 'y5s1_语文_u06_l02', '19 父爱之舟', '/语文（统编版）/第六单元/19 父爱之舟', 2, 'lesson', 2, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u06, 'y5s1_语文_u06_l03', '20* “精彩极了”和“糟糕透了”', '/语文（统编版）/第六单元/20* “精彩极了”和“糟糕透了”', 2, 'lesson', 3, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u06, 'y5s1_语文_u06_l04', '口语交际：父母之爱', '/语文（统编版）/第六单元/口语交际：父母之爱', 2, 'lesson', 4, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u06, 'y5s1_语文_u06_l05', '习作：我想对您说', '/语文（统编版）/第六单元/习作：我想对您说', 2, 'lesson', 5, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u06, 'y5s1_语文_u06_l06', '语文园地', '/语文（统编版）/第六单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y5s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_语文_root, 'y5s1_语文_u07', '第七单元', '/语文（统编版）/第七单元', 1, 'unit', 7, '{"volumeKey": "y5s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y5s1_语文_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_语文_u07, 'y5s1_语文_u07_l01', '21 古诗词三首（山居秋暝、枫桥夜泊、长相思）', '/语文（统编版）/第七单元/21 古诗词三首（山居秋暝、枫桥夜泊、长相思）', 2, 'lesson', 1, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u07, 'y5s1_语文_u07_l02', '22 四季之美', '/语文（统编版）/第七单元/22 四季之美', 2, 'lesson', 2, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u07, 'y5s1_语文_u07_l03', '23 鸟的天堂', '/语文（统编版）/第七单元/23 鸟的天堂', 2, 'lesson', 3, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u07, 'y5s1_语文_u07_l04', '24* 月迹', '/语文（统编版）/第七单元/24* 月迹', 2, 'lesson', 4, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u07, 'y5s1_语文_u07_l05', '习作：____即景', '/语文（统编版）/第七单元/习作：____即景', 2, 'lesson', 5, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u07, 'y5s1_语文_u07_l06', '语文园地', '/语文（统编版）/第七单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y5s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_语文_root, 'y5s1_语文_u08', '第八单元', '/语文（统编版）/第八单元', 1, 'unit', 8, '{"volumeKey": "y5s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y5s1_语文_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_语文_u08, 'y5s1_语文_u08_l01', '25 古人谈读书', '/语文（统编版）/第八单元/25 古人谈读书', 2, 'lesson', 1, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u08, 'y5s1_语文_u08_l02', '26 忆读书', '/语文（统编版）/第八单元/26 忆读书', 2, 'lesson', 2, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u08, 'y5s1_语文_u08_l03', '27* 我的“长生果”', '/语文（统编版）/第八单元/27* 我的“长生果”', 2, 'lesson', 3, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u08, 'y5s1_语文_u08_l04', '口语交际：我最喜欢的人物形象', '/语文（统编版）/第八单元/口语交际：我最喜欢的人物形象', 2, 'lesson', 4, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u08, 'y5s1_语文_u08_l05', '习作：推荐一本书', '/语文（统编版）/第八单元/习作：推荐一本书', 2, 'lesson', 5, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u08, 'y5s1_语文_u08_l06', '语文园地', '/语文（统编版）/第八单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y5s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_语文_root, 'y5s1_语文_u09', '附录', '/语文（统编版）/附录', 1, 'unit', 9, '{"volumeKey": "y5s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y5s1_语文_u09 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_语文_u09, 'y5s1_语文_u09_l01', '识字表', '/语文（统编版）/附录/识字表', 2, 'lesson', 1, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u09, 'y5s1_语文_u09_l02', '写字表', '/语文（统编版）/附录/写字表', 2, 'lesson', 2, '{"volumeKey": "y5s1", "subject": "语文"}', 1),
(@scheme_id, @y5s1_语文_u09, 'y5s1_语文_u09_l03', '词语表', '/语文（统编版）/附录/词语表', 2, 'lesson', 3, '{"volumeKey": "y5s1", "subject": "语文"}', 1);

-- y5s1 数学（人教版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y5s1_数学_root', '数学（人教版）', '/数学（人教版）', 0, 'folder', 2, '{"volumeKey": "y5s1", "subject": "数学", "edition": "人教版", "defaultModule": "同步备课"}', 1);
SET @y5s1_数学_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_数学_root, 'y5s1_数学_u01', '1.小数乘法', '/数学（人教版）/1.小数乘法', 1, 'unit', 1, '{"volumeKey": "y5s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y5s1_数学_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_数学_u01, 'y5s1_数学_u01_l01', '小数乘法', '/数学（人教版）/1.小数乘法/小数乘法', 2, 'lesson', 1, '{"volumeKey": "y5s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_数学_root, 'y5s1_数学_u02', '2.位置', '/数学（人教版）/2.位置', 1, 'unit', 2, '{"volumeKey": "y5s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y5s1_数学_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_数学_u02, 'y5s1_数学_u02_l01', '位置', '/数学（人教版）/2.位置/位置', 2, 'lesson', 1, '{"volumeKey": "y5s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_数学_root, 'y5s1_数学_u03', '3.小数除法', '/数学（人教版）/3.小数除法', 1, 'unit', 3, '{"volumeKey": "y5s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y5s1_数学_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_数学_u03, 'y5s1_数学_u03_l01', '小数除法', '/数学（人教版）/3.小数除法/小数除法', 2, 'lesson', 1, '{"volumeKey": "y5s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_数学_root, 'y5s1_数学_u04', '4.可能性', '/数学（人教版）/4.可能性', 1, 'unit', 4, '{"volumeKey": "y5s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y5s1_数学_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_数学_u04, 'y5s1_数学_u04_l01', '可能性', '/数学（人教版）/4.可能性/可能性', 2, 'lesson', 1, '{"volumeKey": "y5s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_数学_root, 'y5s1_数学_u05', '5.简易方程', '/数学（人教版）/5.简易方程', 1, 'unit', 5, '{"volumeKey": "y5s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y5s1_数学_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_数学_u05, 'y5s1_数学_u05_l01', '简易方程', '/数学（人教版）/5.简易方程/简易方程', 2, 'lesson', 1, '{"volumeKey": "y5s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_数学_root, 'y5s1_数学_u06', '6.多边形面积', '/数学（人教版）/6.多边形面积', 1, 'unit', 6, '{"volumeKey": "y5s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y5s1_数学_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_数学_u06, 'y5s1_数学_u06_l01', '多边形面积', '/数学（人教版）/6.多边形面积/多边形面积', 2, 'lesson', 1, '{"volumeKey": "y5s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_数学_root, 'y5s1_数学_u07', '7.植树问题', '/数学（人教版）/7.植树问题', 1, 'unit', 7, '{"volumeKey": "y5s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y5s1_数学_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_数学_u07, 'y5s1_数学_u07_l01', '植树问题', '/数学（人教版）/7.植树问题/植树问题', 2, 'lesson', 1, '{"volumeKey": "y5s1", "subject": "数学"}', 1);

-- y5s1 英语（人教PEP）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y5s1_英语_root', '英语（人教PEP）', '/英语（人教PEP）', 0, 'folder', 3, '{"volumeKey": "y5s1", "subject": "英语", "edition": "人教PEP", "defaultModule": "同步备课"}', 1);
SET @y5s1_英语_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_英语_root, 'y5s1_英语_u01', 'Unit1 复习重难点', '/英语（人教PEP）/Unit1 复习重难点', 1, 'unit', 1, '{"volumeKey": "y5s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y5s1_英语_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s1_英语_u01, 'y5s1_英语_u01_l01', '一般过去式', '/英语（人教PEP）/Unit1 复习重难点/一般过去式', 2, 'lesson', 1, '{"volumeKey": "y5s1", "subject": "英语"}', 1),
(@scheme_id, @y5s1_英语_u01, 'y5s1_英语_u01_l02', '比较级', '/英语（人教PEP）/Unit1 复习重难点/比较级', 2, 'lesson', 2, '{"volumeKey": "y5s1", "subject": "英语"}', 1);

-- y5s2 语文（统编版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y5s2_语文_root', '语文（统编版）', '/语文（统编版）', 0, 'folder', 1, '{"volumeKey": "y5s2", "subject": "语文", "edition": "统编版", "defaultModule": "同步备课"}', 1);
SET @y5s2_语文_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_语文_root, 'y5s2_语文_u01', '第一单元', '/语文（统编版）/第一单元', 1, 'unit', 1, '{"volumeKey": "y5s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y5s2_语文_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_语文_u01, 'y5s2_语文_u01_l01', '1 古诗三首（四时田园杂兴·其三十一、稚子弄冰、村晚）', '/语文（统编版）/第一单元/1 古诗三首（四时田园杂兴·其三十一、稚子弄冰、村晚）', 2, 'lesson', 1, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u01, 'y5s2_语文_u01_l02', '2 祖父的园子', '/语文（统编版）/第一单元/2 祖父的园子', 2, 'lesson', 2, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u01, 'y5s2_语文_u01_l03', '3* 月是故乡明', '/语文（统编版）/第一单元/3* 月是故乡明', 2, 'lesson', 3, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u01, 'y5s2_语文_u01_l04', '4* 梅花魂', '/语文（统编版）/第一单元/4* 梅花魂', 2, 'lesson', 4, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u01, 'y5s2_语文_u01_l05', '口语交际：走进他们的童年岁月', '/语文（统编版）/第一单元/口语交际：走进他们的童年岁月', 2, 'lesson', 5, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u01, 'y5s2_语文_u01_l06', '习作：那一刻，我长大了', '/语文（统编版）/第一单元/习作：那一刻，我长大了', 2, 'lesson', 6, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u01, 'y5s2_语文_u01_l07', '语文园地', '/语文（统编版）/第一单元/语文园地', 2, 'lesson', 7, '{"volumeKey": "y5s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_语文_root, 'y5s2_语文_u02', '第二单元', '/语文（统编版）/第二单元', 1, 'unit', 2, '{"volumeKey": "y5s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y5s2_语文_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_语文_u02, 'y5s2_语文_u02_l01', '5 草船借箭', '/语文（统编版）/第二单元/5 草船借箭', 2, 'lesson', 1, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u02, 'y5s2_语文_u02_l02', '6 景阳冈', '/语文（统编版）/第二单元/6 景阳冈', 2, 'lesson', 2, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u02, 'y5s2_语文_u02_l03', '7* 猴王出世', '/语文（统编版）/第二单元/7* 猴王出世', 2, 'lesson', 3, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u02, 'y5s2_语文_u02_l04', '8* 红楼春趣', '/语文（统编版）/第二单元/8* 红楼春趣', 2, 'lesson', 4, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u02, 'y5s2_语文_u02_l05', '口语交际：怎么表演课本剧', '/语文（统编版）/第二单元/口语交际：怎么表演课本剧', 2, 'lesson', 5, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u02, 'y5s2_语文_u02_l06', '习作：写读后感', '/语文（统编版）/第二单元/习作：写读后感', 2, 'lesson', 6, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u02, 'y5s2_语文_u02_l07', '语文园地', '/语文（统编版）/第二单元/语文园地', 2, 'lesson', 7, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u02, 'y5s2_语文_u02_l08', '快乐读书吧：读古典名著，品百味人生', '/语文（统编版）/第二单元/快乐读书吧：读古典名著，品百味人生', 2, 'lesson', 8, '{"volumeKey": "y5s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_语文_root, 'y5s2_语文_u03', '第三单元', '/语文（统编版）/第三单元', 1, 'unit', 3, '{"volumeKey": "y5s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y5s2_语文_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_语文_u03, 'y5s2_语文_u03_l01', '9 古诗三首（从军行、秋夜将晓出篱门迎凉有感、闻官军收河南河北）', '/语文（统编版）/第三单元/9 古诗三首（从军行、秋夜将晓出篱门迎凉有感、闻官军收河南河北）', 2, 'lesson', 1, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u03, 'y5s2_语文_u03_l02', '10 青山处处埋忠骨', '/语文（统编版）/第三单元/10 青山处处埋忠骨', 2, 'lesson', 2, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u03, 'y5s2_语文_u03_l03', '11 军神', '/语文（统编版）/第三单元/11 军神', 2, 'lesson', 3, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u03, 'y5s2_语文_u03_l04', '12* 清贫', '/语文（统编版）/第三单元/12* 清贫', 2, 'lesson', 4, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u03, 'y5s2_语文_u03_l05', '习作：他____了', '/语文（统编版）/第三单元/习作：他____了', 2, 'lesson', 5, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u03, 'y5s2_语文_u03_l06', '语文园地', '/语文（统编版）/第三单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y5s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_语文_root, 'y5s2_语文_u04', '第四单元', '/语文（统编版）/第四单元', 1, 'unit', 4, '{"volumeKey": "y5s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y5s2_语文_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_语文_u04, 'y5s2_语文_u04_l01', '13 人物描写一组（摔跤、他像一棵挺脱的树、两茎灯草）', '/语文（统编版）/第四单元/13 人物描写一组（摔跤、他像一棵挺脱的树、两茎灯草）', 2, 'lesson', 1, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u04, 'y5s2_语文_u04_l02', '14 刷子李', '/语文（统编版）/第四单元/14 刷子李', 2, 'lesson', 2, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u04, 'y5s2_语文_u04_l03', '习作例文：我的朋友容容、小守门员和他的观众们', '/语文（统编版）/第四单元/习作例文：我的朋友容容、小守门员和他的观众们', 2, 'lesson', 3, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u04, 'y5s2_语文_u04_l04', '习作：形形色色的人', '/语文（统编版）/第四单元/习作：形形色色的人', 2, 'lesson', 4, '{"volumeKey": "y5s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_语文_root, 'y5s2_语文_u05', '第五单元', '/语文（统编版）/第五单元', 1, 'unit', 5, '{"volumeKey": "y5s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y5s2_语文_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_语文_u05, 'y5s2_语文_u05_l01', '15 自相矛盾', '/语文（统编版）/第五单元/15 自相矛盾', 2, 'lesson', 1, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u05, 'y5s2_语文_u05_l02', '16 田忌赛马', '/语文（统编版）/第五单元/16 田忌赛马', 2, 'lesson', 2, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u05, 'y5s2_语文_u05_l03', '17 跳水', '/语文（统编版）/第五单元/17 跳水', 2, 'lesson', 3, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u05, 'y5s2_语文_u05_l04', '习作：神奇的探险之旅', '/语文（统编版）/第五单元/习作：神奇的探险之旅', 2, 'lesson', 4, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u05, 'y5s2_语文_u05_l05', '语文园地', '/语文（统编版）/第五单元/语文园地', 2, 'lesson', 5, '{"volumeKey": "y5s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_语文_root, 'y5s2_语文_u06', '第六单元', '/语文（统编版）/第六单元', 1, 'unit', 6, '{"volumeKey": "y5s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y5s2_语文_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_语文_u06, 'y5s2_语文_u06_l01', '18 威尼斯的小艇', '/语文（统编版）/第六单元/18 威尼斯的小艇', 2, 'lesson', 1, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u06, 'y5s2_语文_u06_l02', '19 牧场之国', '/语文（统编版）/第六单元/19 牧场之国', 2, 'lesson', 2, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u06, 'y5s2_语文_u06_l03', '20* 金字塔（金字塔夕照、不可思议的金字塔）', '/语文（统编版）/第六单元/20* 金字塔（金字塔夕照、不可思议的金字塔）', 2, 'lesson', 3, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u06, 'y5s2_语文_u06_l04', '口语交际：我是小小讲解员', '/语文（统编版）/第六单元/口语交际：我是小小讲解员', 2, 'lesson', 4, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u06, 'y5s2_语文_u06_l05', '习作：中国的世界文化遗产', '/语文（统编版）/第六单元/习作：中国的世界文化遗产', 2, 'lesson', 5, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u06, 'y5s2_语文_u06_l06', '语文园地', '/语文（统编版）/第六单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y5s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_语文_root, 'y5s2_语文_u07', '第七单元', '/语文（统编版）/第七单元', 1, 'unit', 7, '{"volumeKey": "y5s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y5s2_语文_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_语文_u07, 'y5s2_语文_u07_l01', '21 杨氏之子', '/语文（统编版）/第七单元/21 杨氏之子', 2, 'lesson', 1, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u07, 'y5s2_语文_u07_l02', '22 手指', '/语文（统编版）/第七单元/22 手指', 2, 'lesson', 2, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u07, 'y5s2_语文_u07_l03', '23* 童年的发现', '/语文（统编版）/第七单元/23* 童年的发现', 2, 'lesson', 3, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u07, 'y5s2_语文_u07_l04', '口语交际：我们都来讲笑话', '/语文（统编版）/第七单元/口语交际：我们都来讲笑话', 2, 'lesson', 4, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u07, 'y5s2_语文_u07_l05', '习作：漫画的启示', '/语文（统编版）/第七单元/习作：漫画的启示', 2, 'lesson', 5, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u07, 'y5s2_语文_u07_l06', '语文园地', '/语文（统编版）/第七单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y5s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_语文_root, 'y5s2_语文_u08', '第八单元', '/语文（统编版）/第八单元', 1, 'unit', 8, '{"volumeKey": "y5s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y5s2_语文_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_语文_u08, 'y5s2_语文_u08_l01', '综合性学习：难忘小学生活——回忆往事、依依惜别', '/语文（统编版）/第八单元/综合性学习：难忘小学生活——回忆往事、依依惜别', 2, 'lesson', 1, '{"volumeKey": "y5s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_语文_root, 'y5s2_语文_u09', '附录', '/语文（统编版）/附录', 1, 'unit', 9, '{"volumeKey": "y5s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y5s2_语文_u09 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_语文_u09, 'y5s2_语文_u09_l01', '识字表', '/语文（统编版）/附录/识字表', 2, 'lesson', 1, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u09, 'y5s2_语文_u09_l02', '写字表', '/语文（统编版）/附录/写字表', 2, 'lesson', 2, '{"volumeKey": "y5s2", "subject": "语文"}', 1),
(@scheme_id, @y5s2_语文_u09, 'y5s2_语文_u09_l03', '词语表', '/语文（统编版）/附录/词语表', 2, 'lesson', 3, '{"volumeKey": "y5s2", "subject": "语文"}', 1);

-- y5s2 数学（人教版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y5s2_数学_root', '数学（人教版）', '/数学（人教版）', 0, 'folder', 2, '{"volumeKey": "y5s2", "subject": "数学", "edition": "人教版", "defaultModule": "同步备课"}', 1);
SET @y5s2_数学_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_数学_root, 'y5s2_数学_u01', '1.观察物体三', '/数学（人教版）/1.观察物体三', 1, 'unit', 1, '{"volumeKey": "y5s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y5s2_数学_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_数学_u01, 'y5s2_数学_u01_l01', '观察物体三', '/数学（人教版）/1.观察物体三/观察物体三', 2, 'lesson', 1, '{"volumeKey": "y5s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_数学_root, 'y5s2_数学_u02', '2.因数与倍数', '/数学（人教版）/2.因数与倍数', 1, 'unit', 2, '{"volumeKey": "y5s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y5s2_数学_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_数学_u02, 'y5s2_数学_u02_l01', '因数与倍数', '/数学（人教版）/2.因数与倍数/因数与倍数', 2, 'lesson', 1, '{"volumeKey": "y5s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_数学_root, 'y5s2_数学_u03', '3.长方体正方体', '/数学（人教版）/3.长方体正方体', 1, 'unit', 3, '{"volumeKey": "y5s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y5s2_数学_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_数学_u03, 'y5s2_数学_u03_l01', '长方体正方体', '/数学（人教版）/3.长方体正方体/长方体正方体', 2, 'lesson', 1, '{"volumeKey": "y5s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_数学_root, 'y5s2_数学_u04', '4.分数意义性质', '/数学（人教版）/4.分数意义性质', 1, 'unit', 4, '{"volumeKey": "y5s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y5s2_数学_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_数学_u04, 'y5s2_数学_u04_l01', '分数意义性质', '/数学（人教版）/4.分数意义性质/分数意义性质', 2, 'lesson', 1, '{"volumeKey": "y5s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_数学_root, 'y5s2_数学_u05', '5.图形旋转', '/数学（人教版）/5.图形旋转', 1, 'unit', 5, '{"volumeKey": "y5s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y5s2_数学_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_数学_u05, 'y5s2_数学_u05_l01', '图形旋转', '/数学（人教版）/5.图形旋转/图形旋转', 2, 'lesson', 1, '{"volumeKey": "y5s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_数学_root, 'y5s2_数学_u06', '6.分数加减法', '/数学（人教版）/6.分数加减法', 1, 'unit', 6, '{"volumeKey": "y5s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y5s2_数学_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_数学_u06, 'y5s2_数学_u06_l01', '分数加减法', '/数学（人教版）/6.分数加减法/分数加减法', 2, 'lesson', 1, '{"volumeKey": "y5s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_数学_root, 'y5s2_数学_u07', '7.折线统计图', '/数学（人教版）/7.折线统计图', 1, 'unit', 7, '{"volumeKey": "y5s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y5s2_数学_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_数学_u07, 'y5s2_数学_u07_l01', '折线统计图', '/数学（人教版）/7.折线统计图/折线统计图', 2, 'lesson', 1, '{"volumeKey": "y5s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_数学_root, 'y5s2_数学_u08', '8.找次品', '/数学（人教版）/8.找次品', 1, 'unit', 8, '{"volumeKey": "y5s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y5s2_数学_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_数学_u08, 'y5s2_数学_u08_l01', '找次品', '/数学（人教版）/8.找次品/找次品', 2, 'lesson', 1, '{"volumeKey": "y5s2", "subject": "数学"}', 1);

-- y5s2 英语（人教PEP）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y5s2_英语_root', '英语（人教PEP）', '/英语（人教PEP）', 0, 'folder', 3, '{"volumeKey": "y5s2", "subject": "英语", "edition": "人教PEP", "defaultModule": "同步备课"}', 1);
SET @y5s2_英语_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_英语_root, 'y5s2_英语_u01', 'Unit1 复习重难点', '/英语（人教PEP）/Unit1 复习重难点', 1, 'unit', 1, '{"volumeKey": "y5s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y5s2_英语_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y5s2_英语_u01, 'y5s2_英语_u01_l01', '一般过去式', '/英语（人教PEP）/Unit1 复习重难点/一般过去式', 2, 'lesson', 1, '{"volumeKey": "y5s2", "subject": "英语"}', 1),
(@scheme_id, @y5s2_英语_u01, 'y5s2_英语_u01_l02', '比较级', '/英语（人教PEP）/Unit1 复习重难点/比较级', 2, 'lesson', 2, '{"volumeKey": "y5s2", "subject": "英语"}', 1);

-- y6s1 语文（统编版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y6s1_语文_root', '语文（统编版）', '/语文（统编版）', 0, 'folder', 1, '{"volumeKey": "y6s1", "subject": "语文", "edition": "统编版", "defaultModule": "同步备课"}', 1);
SET @y6s1_语文_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_语文_root, 'y6s1_语文_u01', '第一单元', '/语文（统编版）/第一单元', 1, 'unit', 1, '{"volumeKey": "y6s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y6s1_语文_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_语文_u01, 'y6s1_语文_u01_l01', '1 草原', '/语文（统编版）/第一单元/1 草原', 2, 'lesson', 1, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u01, 'y6s1_语文_u01_l02', '2 丁香结', '/语文（统编版）/第一单元/2 丁香结', 2, 'lesson', 2, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u01, 'y6s1_语文_u01_l03', '3 古诗词三首（宿建德江、六月二十七日望湖楼醉书、西江月·夜行黄沙道中）', '/语文（统编版）/第一单元/3 古诗词三首（宿建德江、六月二十七日望湖楼醉书、西江月·夜行黄沙道中）', 2, 'lesson', 3, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u01, 'y6s1_语文_u01_l04', '4* 花之歌', '/语文（统编版）/第一单元/4* 花之歌', 2, 'lesson', 4, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u01, 'y6s1_语文_u01_l05', '习作：变形记', '/语文（统编版）/第一单元/习作：变形记', 2, 'lesson', 5, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u01, 'y6s1_语文_u01_l06', '语文园地', '/语文（统编版）/第一单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y6s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_语文_root, 'y6s1_语文_u02', '第二单元', '/语文（统编版）/第二单元', 1, 'unit', 2, '{"volumeKey": "y6s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y6s1_语文_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_语文_u02, 'y6s1_语文_u02_l01', '5 七律·长征', '/语文（统编版）/第二单元/5 七律·长征', 2, 'lesson', 1, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u02, 'y6s1_语文_u02_l02', '6 狼牙山五壮士', '/语文（统编版）/第二单元/6 狼牙山五壮士', 2, 'lesson', 2, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u02, 'y6s1_语文_u02_l03', '7 开国大典', '/语文（统编版）/第二单元/7 开国大典', 2, 'lesson', 3, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u02, 'y6s1_语文_u02_l04', '8* 灯光', '/语文（统编版）/第二单元/8* 灯光', 2, 'lesson', 4, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u02, 'y6s1_语文_u02_l05', '9* 我的战友邱少云', '/语文（统编版）/第二单元/9* 我的战友邱少云', 2, 'lesson', 5, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u02, 'y6s1_语文_u02_l06', '口语交际：演讲', '/语文（统编版）/第二单元/口语交际：演讲', 2, 'lesson', 6, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u02, 'y6s1_语文_u02_l07', '习作：多彩的活动', '/语文（统编版）/第二单元/习作：多彩的活动', 2, 'lesson', 7, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u02, 'y6s1_语文_u02_l08', '语文园地', '/语文（统编版）/第二单元/语文园地', 2, 'lesson', 8, '{"volumeKey": "y6s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_语文_root, 'y6s1_语文_u03', '第三单元', '/语文（统编版）/第三单元', 1, 'unit', 3, '{"volumeKey": "y6s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y6s1_语文_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_语文_u03, 'y6s1_语文_u03_l01', '10 竹节人', '/语文（统编版）/第三单元/10 竹节人', 2, 'lesson', 1, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u03, 'y6s1_语文_u03_l02', '11 宇宙生命之谜', '/语文（统编版）/第三单元/11 宇宙生命之谜', 2, 'lesson', 2, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u03, 'y6s1_语文_u03_l03', '12* 故宫博物院', '/语文（统编版）/第三单元/12* 故宫博物院', 2, 'lesson', 3, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u03, 'y6s1_语文_u03_l04', '习作：____让生活更美好', '/语文（统编版）/第三单元/习作：____让生活更美好', 2, 'lesson', 4, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u03, 'y6s1_语文_u03_l05', '语文园地', '/语文（统编版）/第三单元/语文园地', 2, 'lesson', 5, '{"volumeKey": "y6s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_语文_root, 'y6s1_语文_u04', '第四单元', '/语文（统编版）/第四单元', 1, 'unit', 4, '{"volumeKey": "y6s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y6s1_语文_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_语文_u04, 'y6s1_语文_u04_l01', '13 桥', '/语文（统编版）/第四单元/13 桥', 2, 'lesson', 1, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u04, 'y6s1_语文_u04_l02', '14 穷人', '/语文（统编版）/第四单元/14 穷人', 2, 'lesson', 2, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u04, 'y6s1_语文_u04_l03', '15* 金色的鱼钩', '/语文（统编版）/第四单元/15* 金色的鱼钩', 2, 'lesson', 3, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u04, 'y6s1_语文_u04_l04', '口语交际：请你支持我', '/语文（统编版）/第四单元/口语交际：请你支持我', 2, 'lesson', 4, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u04, 'y6s1_语文_u04_l05', '习作：笔尖流出的故事', '/语文（统编版）/第四单元/习作：笔尖流出的故事', 2, 'lesson', 5, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u04, 'y6s1_语文_u04_l06', '语文园地', '/语文（统编版）/第四单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u04, 'y6s1_语文_u04_l07', '快乐读书吧：笑与泪，经历与成长（成长小说）', '/语文（统编版）/第四单元/快乐读书吧：笑与泪，经历与成长（成长小说）', 2, 'lesson', 7, '{"volumeKey": "y6s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_语文_root, 'y6s1_语文_u05', '第五单元', '/语文（统编版）/第五单元', 1, 'unit', 5, '{"volumeKey": "y6s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y6s1_语文_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_语文_u05, 'y6s1_语文_u05_l01', '16 夏天里的成长', '/语文（统编版）/第五单元/16 夏天里的成长', 2, 'lesson', 1, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u05, 'y6s1_语文_u05_l02', '17 盼', '/语文（统编版）/第五单元/17 盼', 2, 'lesson', 2, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u05, 'y6s1_语文_u05_l03', '习作例文：爸爸的计划、小站', '/语文（统编版）/第五单元/习作例文：爸爸的计划、小站', 2, 'lesson', 3, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u05, 'y6s1_语文_u05_l04', '习作：围绕中心意思写', '/语文（统编版）/第五单元/习作：围绕中心意思写', 2, 'lesson', 4, '{"volumeKey": "y6s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_语文_root, 'y6s1_语文_u06', '第六单元', '/语文（统编版）/第六单元', 1, 'unit', 6, '{"volumeKey": "y6s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y6s1_语文_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_语文_u06, 'y6s1_语文_u06_l01', '18 古诗三首（浪淘沙、江南春、书湖阴先生壁）', '/语文（统编版）/第六单元/18 古诗三首（浪淘沙、江南春、书湖阴先生壁）', 2, 'lesson', 1, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u06, 'y6s1_语文_u06_l02', '19 只有一个地球', '/语文（统编版）/第六单元/19 只有一个地球', 2, 'lesson', 2, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u06, 'y6s1_语文_u06_l03', '20* 青山不老', '/语文（统编版）/第六单元/20* 青山不老', 2, 'lesson', 3, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u06, 'y6s1_语文_u06_l04', '21* 三黑和土地', '/语文（统编版）/第六单元/21* 三黑和土地', 2, 'lesson', 4, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u06, 'y6s1_语文_u06_l05', '口语交际：意见不同怎么办', '/语文（统编版）/第六单元/口语交际：意见不同怎么办', 2, 'lesson', 5, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u06, 'y6s1_语文_u06_l06', '习作：学写倡议书', '/语文（统编版）/第六单元/习作：学写倡议书', 2, 'lesson', 6, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u06, 'y6s1_语文_u06_l07', '语文园地', '/语文（统编版）/第六单元/语文园地', 2, 'lesson', 7, '{"volumeKey": "y6s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_语文_root, 'y6s1_语文_u07', '第七单元', '/语文（统编版）/第七单元', 1, 'unit', 7, '{"volumeKey": "y6s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y6s1_语文_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_语文_u07, 'y6s1_语文_u07_l01', '22 文言文二则（伯牙鼓琴、书戴嵩画牛）', '/语文（统编版）/第七单元/22 文言文二则（伯牙鼓琴、书戴嵩画牛）', 2, 'lesson', 1, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u07, 'y6s1_语文_u07_l02', '23 月光曲', '/语文（统编版）/第七单元/23 月光曲', 2, 'lesson', 2, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u07, 'y6s1_语文_u07_l03', '24* 京剧趣谈', '/语文（统编版）/第七单元/24* 京剧趣谈', 2, 'lesson', 3, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u07, 'y6s1_语文_u07_l04', '口语交际：聊聊书法', '/语文（统编版）/第七单元/口语交际：聊聊书法', 2, 'lesson', 4, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u07, 'y6s1_语文_u07_l05', '习作：我的拿手好戏', '/语文（统编版）/第七单元/习作：我的拿手好戏', 2, 'lesson', 5, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u07, 'y6s1_语文_u07_l06', '语文园地', '/语文（统编版）/第七单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y6s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_语文_root, 'y6s1_语文_u08', '第八单元', '/语文（统编版）/第八单元', 1, 'unit', 8, '{"volumeKey": "y6s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y6s1_语文_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_语文_u08, 'y6s1_语文_u08_l01', '25 少年闰土', '/语文（统编版）/第八单元/25 少年闰土', 2, 'lesson', 1, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u08, 'y6s1_语文_u08_l02', '26 好的故事', '/语文（统编版）/第八单元/26 好的故事', 2, 'lesson', 2, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u08, 'y6s1_语文_u08_l03', '27* 我的伯父鲁迅先生', '/语文（统编版）/第八单元/27* 我的伯父鲁迅先生', 2, 'lesson', 3, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u08, 'y6s1_语文_u08_l04', '28 有的人——纪念鲁迅有感', '/语文（统编版）/第八单元/28 有的人——纪念鲁迅有感', 2, 'lesson', 4, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u08, 'y6s1_语文_u08_l05', '习作：有你，真好', '/语文（统编版）/第八单元/习作：有你，真好', 2, 'lesson', 5, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u08, 'y6s1_语文_u08_l06', '语文园地', '/语文（统编版）/第八单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y6s1", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_语文_root, 'y6s1_语文_u09', '附录', '/语文（统编版）/附录', 1, 'unit', 9, '{"volumeKey": "y6s1", "subject": "语文", "edition": "统编版"}', 1);
SET @y6s1_语文_u09 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_语文_u09, 'y6s1_语文_u09_l01', '写字表', '/语文（统编版）/附录/写字表', 2, 'lesson', 1, '{"volumeKey": "y6s1", "subject": "语文"}', 1),
(@scheme_id, @y6s1_语文_u09, 'y6s1_语文_u09_l02', '词语表', '/语文（统编版）/附录/词语表', 2, 'lesson', 2, '{"volumeKey": "y6s1", "subject": "语文"}', 1);

-- y6s1 数学（人教版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y6s1_数学_root', '数学（人教版）', '/数学（人教版）', 0, 'folder', 2, '{"volumeKey": "y6s1", "subject": "数学", "edition": "人教版", "defaultModule": "同步备课"}', 1);
SET @y6s1_数学_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_数学_root, 'y6s1_数学_u01', '1.分数乘法', '/数学（人教版）/1.分数乘法', 1, 'unit', 1, '{"volumeKey": "y6s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y6s1_数学_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_数学_u01, 'y6s1_数学_u01_l01', '分数乘法', '/数学（人教版）/1.分数乘法/分数乘法', 2, 'lesson', 1, '{"volumeKey": "y6s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_数学_root, 'y6s1_数学_u02', '2.位置与方向二', '/数学（人教版）/2.位置与方向二', 1, 'unit', 2, '{"volumeKey": "y6s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y6s1_数学_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_数学_u02, 'y6s1_数学_u02_l01', '位置与方向二', '/数学（人教版）/2.位置与方向二/位置与方向二', 2, 'lesson', 1, '{"volumeKey": "y6s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_数学_root, 'y6s1_数学_u03', '3.分数除法', '/数学（人教版）/3.分数除法', 1, 'unit', 3, '{"volumeKey": "y6s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y6s1_数学_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_数学_u03, 'y6s1_数学_u03_l01', '分数除法', '/数学（人教版）/3.分数除法/分数除法', 2, 'lesson', 1, '{"volumeKey": "y6s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_数学_root, 'y6s1_数学_u04', '4.比', '/数学（人教版）/4.比', 1, 'unit', 4, '{"volumeKey": "y6s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y6s1_数学_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_数学_u04, 'y6s1_数学_u04_l01', '比', '/数学（人教版）/4.比/比', 2, 'lesson', 1, '{"volumeKey": "y6s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_数学_root, 'y6s1_数学_u05', '5.圆', '/数学（人教版）/5.圆', 1, 'unit', 5, '{"volumeKey": "y6s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y6s1_数学_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_数学_u05, 'y6s1_数学_u05_l01', '圆', '/数学（人教版）/5.圆/圆', 2, 'lesson', 1, '{"volumeKey": "y6s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_数学_root, 'y6s1_数学_u06', '6.百分数一', '/数学（人教版）/6.百分数一', 1, 'unit', 6, '{"volumeKey": "y6s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y6s1_数学_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_数学_u06, 'y6s1_数学_u06_l01', '百分数一', '/数学（人教版）/6.百分数一/百分数一', 2, 'lesson', 1, '{"volumeKey": "y6s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_数学_root, 'y6s1_数学_u07', '7.扇形统计图', '/数学（人教版）/7.扇形统计图', 1, 'unit', 7, '{"volumeKey": "y6s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y6s1_数学_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_数学_u07, 'y6s1_数学_u07_l01', '扇形统计图', '/数学（人教版）/7.扇形统计图/扇形统计图', 2, 'lesson', 1, '{"volumeKey": "y6s1", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_数学_root, 'y6s1_数学_u08', '8.数与形', '/数学（人教版）/8.数与形', 1, 'unit', 8, '{"volumeKey": "y6s1", "subject": "数学", "edition": "人教版"}', 1);
SET @y6s1_数学_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_数学_u08, 'y6s1_数学_u08_l01', '数与形', '/数学（人教版）/8.数与形/数与形', 2, 'lesson', 1, '{"volumeKey": "y6s1", "subject": "数学"}', 1);

-- y6s1 英语（人教PEP）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y6s1_英语_root', '英语（人教PEP）', '/英语（人教PEP）', 0, 'folder', 3, '{"volumeKey": "y6s1", "subject": "英语", "edition": "人教PEP", "defaultModule": "同步备课"}', 1);
SET @y6s1_英语_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_英语_root, 'y6s1_英语_u01', 'Unit1 复习', '/英语（人教PEP）/Unit1 复习', 1, 'unit', 1, '{"volumeKey": "y6s1", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y6s1_英语_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s1_英语_u01, 'y6s1_英语_u01_l01', '小学英语综合', '/英语（人教PEP）/Unit1 复习/小学英语综合', 2, 'lesson', 1, '{"volumeKey": "y6s1", "subject": "英语"}', 1);

-- y6s2 语文（统编版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y6s2_语文_root', '语文（统编版）', '/语文（统编版）', 0, 'folder', 1, '{"volumeKey": "y6s2", "subject": "语文", "edition": "统编版", "defaultModule": "同步备课"}', 1);
SET @y6s2_语文_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_语文_root, 'y6s2_语文_u01', '第一单元', '/语文（统编版）/第一单元', 1, 'unit', 1, '{"volumeKey": "y6s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y6s2_语文_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_语文_u01, 'y6s2_语文_u01_l01', '1 北京的春节', '/语文（统编版）/第一单元/1 北京的春节', 2, 'lesson', 1, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u01, 'y6s2_语文_u01_l02', '2 腊八粥', '/语文（统编版）/第一单元/2 腊八粥', 2, 'lesson', 2, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u01, 'y6s2_语文_u01_l03', '3 古诗三首（寒食、迢迢牵牛星、十五夜望月）', '/语文（统编版）/第一单元/3 古诗三首（寒食、迢迢牵牛星、十五夜望月）', 2, 'lesson', 3, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u01, 'y6s2_语文_u01_l04', '4 藏戏', '/语文（统编版）/第一单元/4 藏戏', 2, 'lesson', 4, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u01, 'y6s2_语文_u01_l05', '口语交际：即兴发言', '/语文（统编版）/第一单元/口语交际：即兴发言', 2, 'lesson', 5, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u01, 'y6s2_语文_u01_l06', '习作：家乡的风俗', '/语文（统编版）/第一单元/习作：家乡的风俗', 2, 'lesson', 6, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u01, 'y6s2_语文_u01_l07', '语文园地', '/语文（统编版）/第一单元/语文园地', 2, 'lesson', 7, '{"volumeKey": "y6s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_语文_root, 'y6s2_语文_u02', '第二单元', '/语文（统编版）/第二单元', 1, 'unit', 2, '{"volumeKey": "y6s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y6s2_语文_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_语文_u02, 'y6s2_语文_u02_l01', '5 鲁滨逊漂流记（节选）', '/语文（统编版）/第二单元/5 鲁滨逊漂流记（节选）', 2, 'lesson', 1, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u02, 'y6s2_语文_u02_l02', '6* 骑鹅旅行记（节选）', '/语文（统编版）/第二单元/6* 骑鹅旅行记（节选）', 2, 'lesson', 2, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u02, 'y6s2_语文_u02_l03', '7* 汤姆·索亚历险记（节选）', '/语文（统编版）/第二单元/7* 汤姆·索亚历险记（节选）', 2, 'lesson', 3, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u02, 'y6s2_语文_u02_l04', '口语交际：同读一本书', '/语文（统编版）/第二单元/口语交际：同读一本书', 2, 'lesson', 4, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u02, 'y6s2_语文_u02_l05', '习作：写作品梗概', '/语文（统编版）/第二单元/习作：写作品梗概', 2, 'lesson', 5, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u02, 'y6s2_语文_u02_l06', '语文园地', '/语文（统编版）/第二单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u02, 'y6s2_语文_u02_l07', '快乐读书吧：漫步世界名著花园', '/语文（统编版）/第二单元/快乐读书吧：漫步世界名著花园', 2, 'lesson', 7, '{"volumeKey": "y6s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_语文_root, 'y6s2_语文_u03', '第三单元', '/语文（统编版）/第三单元', 1, 'unit', 3, '{"volumeKey": "y6s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y6s2_语文_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_语文_u03, 'y6s2_语文_u03_l01', '8 匆匆', '/语文（统编版）/第三单元/8 匆匆', 2, 'lesson', 1, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u03, 'y6s2_语文_u03_l02', '9 那个星期天', '/语文（统编版）/第三单元/9 那个星期天', 2, 'lesson', 2, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u03, 'y6s2_语文_u03_l03', '习作例文：别了，语文课、阳光的两种用法', '/语文（统编版）/第三单元/习作例文：别了，语文课、阳光的两种用法', 2, 'lesson', 3, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u03, 'y6s2_语文_u03_l04', '习作：让真情自然流露', '/语文（统编版）/第三单元/习作：让真情自然流露', 2, 'lesson', 4, '{"volumeKey": "y6s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_语文_root, 'y6s2_语文_u04', '第四单元', '/语文（统编版）/第四单元', 1, 'unit', 4, '{"volumeKey": "y6s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y6s2_语文_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_语文_u04, 'y6s2_语文_u04_l01', '10 古诗三首（马诗、石灰吟、竹石）', '/语文（统编版）/第四单元/10 古诗三首（马诗、石灰吟、竹石）', 2, 'lesson', 1, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u04, 'y6s2_语文_u04_l02', '11 十六年前的回忆', '/语文（统编版）/第四单元/11 十六年前的回忆', 2, 'lesson', 2, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u04, 'y6s2_语文_u04_l03', '12 为人民服务', '/语文（统编版）/第四单元/12 为人民服务', 2, 'lesson', 3, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u04, 'y6s2_语文_u04_l04', '13* 董存瑞舍身炸暗堡', '/语文（统编版）/第四单元/13* 董存瑞舍身炸暗堡', 2, 'lesson', 4, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u04, 'y6s2_语文_u04_l05', '综合性学习：奋斗的历程', '/语文（统编版）/第四单元/综合性学习：奋斗的历程', 2, 'lesson', 5, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u04, 'y6s2_语文_u04_l06', '语文园地', '/语文（统编版）/第四单元/语文园地', 2, 'lesson', 6, '{"volumeKey": "y6s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_语文_root, 'y6s2_语文_u05', '第五单元', '/语文（统编版）/第五单元', 1, 'unit', 5, '{"volumeKey": "y6s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y6s2_语文_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_语文_u05, 'y6s2_语文_u05_l01', '14 文言文二则（学弈、两小儿辩日）', '/语文（统编版）/第五单元/14 文言文二则（学弈、两小儿辩日）', 2, 'lesson', 1, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u05, 'y6s2_语文_u05_l02', '15 真理诞生于一百个问号之后', '/语文（统编版）/第五单元/15 真理诞生于一百个问号之后', 2, 'lesson', 2, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u05, 'y6s2_语文_u05_l03', '16 表里的生物', '/语文（统编版）/第五单元/16 表里的生物', 2, 'lesson', 3, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u05, 'y6s2_语文_u05_l04', '17* 他们那时候多有趣啊', '/语文（统编版）/第五单元/17* 他们那时候多有趣啊', 2, 'lesson', 4, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u05, 'y6s2_语文_u05_l05', '口语交际：辩论', '/语文（统编版）/第五单元/口语交际：辩论', 2, 'lesson', 5, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u05, 'y6s2_语文_u05_l06', '习作：插上科学的翅膀飞', '/语文（统编版）/第五单元/习作：插上科学的翅膀飞', 2, 'lesson', 6, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u05, 'y6s2_语文_u05_l07', '语文园地', '/语文（统编版）/第五单元/语文园地', 2, 'lesson', 7, '{"volumeKey": "y6s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_语文_root, 'y6s2_语文_u06', '第六单元', '/语文（统编版）/第六单元', 1, 'unit', 6, '{"volumeKey": "y6s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y6s2_语文_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_语文_u06, 'y6s2_语文_u06_l01', '综合性学习：难忘小学生活——回忆往事、依依惜别', '/语文（统编版）/第六单元/综合性学习：难忘小学生活——回忆往事、依依惜别', 2, 'lesson', 1, '{"volumeKey": "y6s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_语文_root, 'y6s2_语文_u07', '古诗词诵读（选读）', '/语文（统编版）/古诗词诵读（选读）', 1, 'unit', 7, '{"volumeKey": "y6s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y6s2_语文_u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_语文_u07, 'y6s2_语文_u07_l01', '1 采薇（节选）', '/语文（统编版）/古诗词诵读（选读）/1 采薇（节选）', 2, 'lesson', 1, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u07, 'y6s2_语文_u07_l02', '2 送元二使安西', '/语文（统编版）/古诗词诵读（选读）/2 送元二使安西', 2, 'lesson', 2, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u07, 'y6s2_语文_u07_l03', '3 春夜喜雨', '/语文（统编版）/古诗词诵读（选读）/3 春夜喜雨', 2, 'lesson', 3, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u07, 'y6s2_语文_u07_l04', '4 早春呈水部张十八员外', '/语文（统编版）/古诗词诵读（选读）/4 早春呈水部张十八员外', 2, 'lesson', 4, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u07, 'y6s2_语文_u07_l05', '5 江上渔者', '/语文（统编版）/古诗词诵读（选读）/5 江上渔者', 2, 'lesson', 5, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u07, 'y6s2_语文_u07_l06', '6 泊船瓜洲', '/语文（统编版）/古诗词诵读（选读）/6 泊船瓜洲', 2, 'lesson', 6, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u07, 'y6s2_语文_u07_l07', '7 游园不值', '/语文（统编版）/古诗词诵读（选读）/7 游园不值', 2, 'lesson', 7, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u07, 'y6s2_语文_u07_l08', '8 卜算子·送鲍浩然之浙东', '/语文（统编版）/古诗词诵读（选读）/8 卜算子·送鲍浩然之浙东', 2, 'lesson', 8, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u07, 'y6s2_语文_u07_l09', '9 浣溪沙', '/语文（统编版）/古诗词诵读（选读）/9 浣溪沙', 2, 'lesson', 9, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u07, 'y6s2_语文_u07_l10', '10 清平乐', '/语文（统编版）/古诗词诵读（选读）/10 清平乐', 2, 'lesson', 10, '{"volumeKey": "y6s2", "subject": "语文"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_语文_root, 'y6s2_语文_u08', '附录', '/语文（统编版）/附录', 1, 'unit', 8, '{"volumeKey": "y6s2", "subject": "语文", "edition": "统编版"}', 1);
SET @y6s2_语文_u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_语文_u08, 'y6s2_语文_u08_l01', '写字表', '/语文（统编版）/附录/写字表', 2, 'lesson', 1, '{"volumeKey": "y6s2", "subject": "语文"}', 1),
(@scheme_id, @y6s2_语文_u08, 'y6s2_语文_u08_l02', '词语表', '/语文（统编版）/附录/词语表', 2, 'lesson', 2, '{"volumeKey": "y6s2", "subject": "语文"}', 1);

-- y6s2 数学（人教版）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y6s2_数学_root', '数学（人教版）', '/数学（人教版）', 0, 'folder', 2, '{"volumeKey": "y6s2", "subject": "数学", "edition": "人教版", "defaultModule": "同步备课"}', 1);
SET @y6s2_数学_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_数学_root, 'y6s2_数学_u01', '1.负数', '/数学（人教版）/1.负数', 1, 'unit', 1, '{"volumeKey": "y6s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y6s2_数学_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_数学_u01, 'y6s2_数学_u01_l01', '负数', '/数学（人教版）/1.负数/负数', 2, 'lesson', 1, '{"volumeKey": "y6s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_数学_root, 'y6s2_数学_u02', '2.百分数二', '/数学（人教版）/2.百分数二', 1, 'unit', 2, '{"volumeKey": "y6s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y6s2_数学_u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_数学_u02, 'y6s2_数学_u02_l01', '百分数二', '/数学（人教版）/2.百分数二/百分数二', 2, 'lesson', 1, '{"volumeKey": "y6s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_数学_root, 'y6s2_数学_u03', '3.圆柱圆锥', '/数学（人教版）/3.圆柱圆锥', 1, 'unit', 3, '{"volumeKey": "y6s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y6s2_数学_u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_数学_u03, 'y6s2_数学_u03_l01', '圆柱圆锥', '/数学（人教版）/3.圆柱圆锥/圆柱圆锥', 2, 'lesson', 1, '{"volumeKey": "y6s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_数学_root, 'y6s2_数学_u04', '4.比例', '/数学（人教版）/4.比例', 1, 'unit', 4, '{"volumeKey": "y6s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y6s2_数学_u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_数学_u04, 'y6s2_数学_u04_l01', '比例', '/数学（人教版）/4.比例/比例', 2, 'lesson', 1, '{"volumeKey": "y6s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_数学_root, 'y6s2_数学_u05', '5.鸽巢问题', '/数学（人教版）/5.鸽巢问题', 1, 'unit', 5, '{"volumeKey": "y6s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y6s2_数学_u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_数学_u05, 'y6s2_数学_u05_l01', '鸽巢问题', '/数学（人教版）/5.鸽巢问题/鸽巢问题', 2, 'lesson', 1, '{"volumeKey": "y6s2", "subject": "数学"}', 1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_数学_root, 'y6s2_数学_u06', '6.小学总复习', '/数学（人教版）/6.小学总复习', 1, 'unit', 6, '{"volumeKey": "y6s2", "subject": "数学", "edition": "人教版"}', 1);
SET @y6s2_数学_u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_数学_u06, 'y6s2_数学_u06_l01', '小学总复习', '/数学（人教版）/6.小学总复习/小学总复习', 2, 'lesson', 1, '{"volumeKey": "y6s2", "subject": "数学"}', 1);

-- y6s2 英语（人教PEP）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, 0, 'y6s2_英语_root', '英语（人教PEP）', '/英语（人教PEP）', 0, 'folder', 3, '{"volumeKey": "y6s2", "subject": "英语", "edition": "人教PEP", "defaultModule": "同步备课"}', 1);
SET @y6s2_英语_root = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_英语_root, 'y6s2_英语_u01', 'Unit1 毕业复习', '/英语（人教PEP）/Unit1 毕业复习', 1, 'unit', 1, '{"volumeKey": "y6s2", "subject": "英语", "edition": "人教PEP"}', 1);
SET @y6s2_英语_u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id, @y6s2_英语_u01, 'y6s2_英语_u01_l01', '小学英语综合', '/英语（人教PEP）/Unit1 毕业复习/小学英语综合', 2, 'lesson', 1, '{"volumeKey": "y6s2", "subject": "英语"}', 1);

-- 完成