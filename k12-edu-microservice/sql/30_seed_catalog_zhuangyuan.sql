-- ============================================================
-- M2：状元版 — 四层产品分类树种子 (scheme=zy_taxonomy)
-- 执行：mysql -u root -p xinketang < sql/30_seed_catalog_zhuangyuan.sql
-- 依赖：28_catalog_scheme.sql
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

SET @scheme_id = (SELECT `id` FROM `edu_catalog_scheme` WHERE `code` = 'zy_taxonomy' LIMIT 1);

DELETE FROM `edu_catalog_node` WHERE `scheme_id` = @scheme_id;

-- ========== 第一层 ==========
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,0,'class_ppt','一、上课课件','/一、上课课件',0,'folder',1,JSON_OBJECT('defaultModule','同步备课','defaultType','课件'),1);
SET @l1_ppt = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,0,'other_res','二、其他资源','/二、其他资源',0,'folder',2,JSON_OBJECT('defaultModule','纯素材'),1);
SET @l1_other = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,0,'lesson_plan','三、教案','/三、教案',0,'folder',3,JSON_OBJECT('defaultModule','同步备课','defaultType','教案','displayMode','unit_matrix'),1);
SET @l1_plan = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,0,'homework_ppt','四、作业课件','/四、作业课件',0,'folder',4,JSON_OBJECT('defaultModule','同步备课','defaultType','课件'),1);
SET @l1_hw = LAST_INSERT_ID();

-- ========== 一、上课课件 — 第二层 ==========
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,@l1_ppt,'ppt_lesson','教案版','/一、上课课件/教案版',1,'leaf',1,JSON_OBJECT('defaultType','课件','subType','教案版'),1),
(@scheme_id,@l1_ppt,'ppt_shuangjian','双减作业设计精华版','/一、上课课件/双减作业设计精华版',1,'leaf',2,JSON_OBJECT('defaultType','课件','subType','双减精华'),1),
(@scheme_id,@l1_ppt,'ppt_interact','交互吧','/一、上课课件/交互吧',1,'leaf',3,JSON_OBJECT('defaultType','课件','subType','交互吧'),1),
(@scheme_id,@l1_ppt,'ppt_read','课文朗读和听写视频','/一、上课课件/课文朗读和听写视频',1,'leaf',4,JSON_OBJECT('defaultType','视频'),1),
(@scheme_id,@l1_ppt,'ppt_stroke','交互笔顺','/一、上课课件/交互笔顺',1,'leaf',5,JSON_OBJECT('defaultType','课件','subType','交互笔顺'),1);

-- ========== 二、其他资源 — 第二层 ==========
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,@l1_other,'prep_res','备课资源','/二、其他资源/备课资源',1,'folder',1,JSON_OBJECT('defaultModule','纯素材'),1);
SET @l2_prep = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,@l1_other,'install_soft','安装软件','/二、其他资源/安装软件',1,'leaf',2,JSON_OBJECT('defaultType','其他'),1);

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,@l1_other,'plan_summary','计划加总结','/二、其他资源/计划加总结',1,'folder',3,JSON_OBJECT('defaultModule','纯素材'),1);
SET @l2_plan = LAST_INSERT_ID();

-- 备课资源 — 第三层（8 子项）
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,@l2_prep,'prep_riyue','日月积累','/二、其他资源/备课资源/日月积累',2,'leaf',1,JSON_OBJECT('defaultType','知识点'),1),
(@scheme_id,@l2_prep,'prep_zhuanxiang','专项学习','/二、其他资源/备课资源/专项学习',2,'leaf',2,JSON_OBJECT('defaultType','学案'),1),
(@scheme_id,@l2_prep,'prep_fansi','教学反思','/二、其他资源/备课资源/教学反思',2,'leaf',3,JSON_OBJECT('defaultType','教学反思'),1),
(@scheme_id,@l2_prep,'prep_qimo','期末总复习','/二、其他资源/备课资源/期末总复习',2,'leaf',4,JSON_OBJECT('defaultModule','期末'),1),
(@scheme_id,@l2_prep,'prep_zi','生字卡片','/二、其他资源/备课资源/生字卡片',2,'leaf',5,JSON_OBJECT('defaultType','课件'),1),
(@scheme_id,@l2_prep,'prep_audio','相关音频','/二、其他资源/备课资源/相关音频',2,'leaf',6,JSON_OBJECT('defaultType','音频/朗读'),1),
(@scheme_id,@l2_prep,'prep_shuoke','说课稿','/二、其他资源/备课资源/说课稿',2,'leaf',7,JSON_OBJECT('defaultType','教案'),1),
(@scheme_id,@l2_prep,'prep_shilu','课堂实录','/二、其他资源/备课资源/课堂实录',2,'leaf',8,JSON_OBJECT('defaultType','视频'),1);

-- 计划加总结 — 第三层
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,@l2_plan,'sum_work','工作总结','/二、其他资源/计划加总结/工作总结',2,'leaf',1,JSON_OBJECT('defaultType','教案'),1),
(@scheme_id,@l2_plan,'sum_plan','工作计划','/二、其他资源/计划加总结/工作计划',2,'leaf',2,JSON_OBJECT('defaultType','教案'),1),
(@scheme_id,@l2_plan,'sum_banzhuren','班主任计划加总结','/二、其他资源/计划加总结/班主任计划加总结',2,'leaf',3,JSON_OBJECT('defaultType','教案'),1);

-- ========== 三、教案 — 各单元完整教案（单元矩阵入口） ==========
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,@l1_plan,'plan_units','各单元完整教案','/三、教案/各单元完整教案',1,'folder',1,JSON_OBJECT('displayMode','unit_matrix','defaultType','教案'),1);
SET @l2_units = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,@l2_units,'zy_u01','我上学了','/三、教案/各单元完整教案/我上学了',2,'unit',1,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@l2_units,'zy_u02','第一单元·识字','/三、教案/各单元完整教案/第一单元·识字',2,'unit',2,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@l2_units,'zy_u03','第二单元·拼音','/三、教案/各单元完整教案/第二单元·拼音',2,'unit',3,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@l2_units,'zy_u04','第三单元·课文','/三、教案/各单元完整教案/第三单元·课文',2,'unit',4,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@l2_units,'zy_u05','第四单元·识字','/三、教案/各单元完整教案/第四单元·识字',2,'unit',5,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@l2_units,'zy_u06','第五单元·课文','/三、教案/各单元完整教案/第五单元·课文',2,'unit',6,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@l2_units,'zy_u07','第六单元·识字','/三、教案/各单元完整教案/第六单元·识字',2,'unit',7,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@l2_units,'zy_u08','第七单元·课文','/三、教案/各单元完整教案/第七单元·课文',2,'unit',8,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@l2_units,'zy_u09','第八单元·课文','/三、教案/各单元完整教案/第八单元·课文',2,'unit',9,JSON_OBJECT('volumeKey','y1s1'),1);

-- ========== 四、作业课件 ==========
INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,@l1_hw,'hw_units','各单元作业课件','/四、作业课件/各单元作业课件',1,'folder',1,JSON_OBJECT('displayMode','unit_matrix','defaultType','课件'),1);
SET @l2_hw = LAST_INSERT_ID();

INSERT INTO `edu_catalog_node` (`scheme_id`,`parent_id`,`code`,`name`,`name_path`,`depth`,`node_type`,`sort`,`meta`,`status`) VALUES
(@scheme_id,@l2_hw,'hw_u01','我上学了','/四、作业课件/各单元作业课件/我上学了',2,'unit',1,JSON_OBJECT('volumeKey','y1s1','fileRole','main'),1),
(@scheme_id,@l2_hw,'hw_u02','第一单元·识字','/四、作业课件/各单元作业课件/第一单元·识字',2,'unit',2,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@l2_hw,'hw_u03','第二单元·拼音','/四、作业课件/各单元作业课件/第二单元·拼音',2,'unit',3,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@l2_hw,'hw_u04','第三单元·课文','/四、作业课件/各单元作业课件/第三单元·课文',2,'unit',4,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@l2_hw,'hw_u05','第四单元·识字','/四、作业课件/各单元作业课件/第四单元·识字',2,'unit',5,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@l2_hw,'hw_u06','第五单元·课文','/四、作业课件/各单元作业课件/第五单元·课文',2,'unit',6,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@l2_hw,'hw_u07','第六单元·识字','/四、作业课件/各单元作业课件/第六单元·识字',2,'unit',7,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@l2_hw,'hw_u08','第七单元·课文','/四、作业课件/各单元作业课件/第七单元·课文',2,'unit',8,JSON_OBJECT('volumeKey','y1s1'),1),
(@scheme_id,@l2_hw,'hw_u09','第八单元·课文','/四、作业课件/各单元作业课件/第八单元·课文',2,'unit',9,JSON_OBJECT('volumeKey','y1s1'),1);
