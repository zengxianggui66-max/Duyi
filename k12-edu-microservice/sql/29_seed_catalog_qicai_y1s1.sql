-- ============================================================
-- M2：七彩课堂 — 一年级上册 (y1s1) 目录种子
-- 来源：k12-edu-platform/src/config/unitData.ts
-- 执行：mysql -u root -p xinketang < sql/29_seed_catalog_qicai_y1s1.sql
-- 依赖：28_catalog_scheme.sql
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

SET @scheme_id = (SELECT `id` FROM `edu_catalog_scheme` WHERE `code` = 'textbook_unit' LIMIT 1);

DELETE FROM `edu_catalog_node`
WHERE `scheme_id` = @scheme_id
  AND JSON_UNQUOTE(JSON_EXTRACT(`meta`, '$.volumeKey')) = 'y1s1';

-- 我上学了
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`)
VALUES (@scheme_id,0,'y1s1_u01','我上学了','/我上学了',0,'unit',1,JSON_OBJECT('volumeKey','y1s1','defaultModule','同步备课'),1);
SET @u01 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,@u01,'y1s1_u01_l01','我是中国人','/我上学了/我是中国人',1,'lesson',1,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@u01,'y1s1_u01_l02','我爱我们的祖国','/我上学了/我爱我们的祖国',1,'lesson',2,JSON_OBJECT('volumeKey','y1s1'),1);

-- 第一单元·识字
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`)
VALUES (@scheme_id,0,'y1s1_u02','第一单元·识字','/第一单元·识字',0,'unit',2,JSON_OBJECT('volumeKey','y1s1','defaultModule','同步备课'),1);
SET @u02 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,@u02,'y1s1_u02_l01','1 天地人','/第一单元·识字/1 天地人',1,'lesson',1,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@u02,'y1s1_u02_l02','2 金木水火土','/第一单元·识字/2 金木水火土',1,'lesson',2,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@u02,'y1s1_u02_l03','3 口耳目','/第一单元·识字/3 口耳目',1,'lesson',3,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@u02,'y1s1_u02_l04','4 日月水火','/第一单元·识字/4 日月水火',1,'lesson',4,JSON_OBJECT('volumeKey','y1s1'),1);

-- 第二单元·拼音
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`)
VALUES (@scheme_id,0,'y1s1_u03','第二单元·拼音','/第二单元·拼音',0,'unit',3,JSON_OBJECT('volumeKey','y1s1','defaultModule','同步备课'),1);
SET @u03 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,@u03,'y1s1_u03_l01','a o e','/第二单元·拼音/a o e',1,'lesson',1,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@u03,'y1s1_u03_l02','i u ü','/第二单元·拼音/i u ü',1,'lesson',2,JSON_OBJECT('volumeKey','y1s1'),1);

-- 第三单元·课文
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`)
VALUES (@scheme_id,0,'y1s1_u04','第三单元·课文','/第三单元·课文',0,'unit',4,JSON_OBJECT('volumeKey','y1s1','defaultModule','同步备课'),1);
SET @u04 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,@u04,'y1s1_u04_l01','秋天','/第三单元·课文/秋天',1,'lesson',1,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@u04,'y1s1_u04_l02','小小的船','/第三单元·课文/小小的船',1,'lesson',2,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@u04,'y1s1_u04_l03','江南','/第三单元·课文/江南',1,'lesson',3,JSON_OBJECT('volumeKey','y1s1'),1);

-- 第四单元·识字
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`)
VALUES (@scheme_id,0,'y1s1_u05','第四单元·识字','/第四单元·识字',0,'unit',5,JSON_OBJECT('volumeKey','y1s1','defaultModule','同步备课'),1);
SET @u05 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,@u05,'y1s1_u05_l01','画','/第四单元·识字/画',1,'lesson',1,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@u05,'y1s1_u05_l02','大小多少','/第四单元·识字/大小多少',1,'lesson',2,JSON_OBJECT('volumeKey','y1s1'),1);

-- 第五单元·课文
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`)
VALUES (@scheme_id,0,'y1s1_u06','第五单元·课文','/第五单元·课文',0,'unit',6,JSON_OBJECT('volumeKey','y1s1','defaultModule','同步备课'),1);
SET @u06 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,@u06,'y1s1_u06_l01','青蛙写诗','/第五单元·课文/青蛙写诗',1,'lesson',1,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@u06,'y1s1_u06_l02','雨点儿','/第五单元·课文/雨点儿',1,'lesson',2,JSON_OBJECT('volumeKey','y1s1'),1);

-- 第六单元·识字
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`)
VALUES (@scheme_id,0,'y1s1_u07','第六单元·识字','/第六单元·识字',0,'unit',7,JSON_OBJECT('volumeKey','y1s1','defaultModule','同步备课'),1);
SET @u07 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,@u07,'y1s1_u07_l01','日月明','/第六单元·识字/日月明',1,'lesson',1,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@u07,'y1s1_u07_l02','小书包','/第六单元·识字/小书包',1,'lesson',2,JSON_OBJECT('volumeKey','y1s1'),1);

-- 第七单元·课文
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`)
VALUES (@scheme_id,0,'y1s1_u08','第七单元·课文','/第七单元·课文',0,'unit',8,JSON_OBJECT('volumeKey','y1s1','defaultModule','同步备课'),1);
SET @u08 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,@u08,'y1s1_u08_l01','明天要远足','/第七单元·课文/明天要远足',1,'lesson',1,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@u08,'y1s1_u08_l02','大还是小','/第七单元·课文/大还是小',1,'lesson',2,JSON_OBJECT('volumeKey','y1s1'),1);

-- 第八单元·课文
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`)
VALUES (@scheme_id,0,'y1s1_u09','第八单元·课文','/第八单元·课文',0,'unit',9,JSON_OBJECT('volumeKey','y1s1','defaultModule','同步备课'),1);
SET @u09 = LAST_INSERT_ID();
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,@u09,'y1s1_u09_l01','雪地里的小画家','/第八单元·课文/雪地里的小画家',1,'lesson',1,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@u09,'y1s1_u09_l02','乌鸦喝水','/第八单元·课文/乌鸦喝水',1,'lesson',2,JSON_OBJECT('volumeKey','y1s1'),1);
