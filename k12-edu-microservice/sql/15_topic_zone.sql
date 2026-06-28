-- ============================================================
-- 专题资源专区（方案 B 独立表）
-- 地域 × 专题场景 × 学段 × 学科 × 资源形态
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `topic_resource` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `summary` VARCHAR(500) DEFAULT '' COMMENT '简介',
    `category` VARCHAR(30) NOT NULL COMMENT 'holiday_hw|term_open|midterm_final|promotion|news|cross|project',
    `region` VARCHAR(20) NOT NULL DEFAULT 'all' COMMENT 'chengdu|mianyang|sichuan|all',
    `grade_stage` VARCHAR(20) NOT NULL DEFAULT 'all' COMMENT 'primary|junior|senior|all',
    `subject` VARCHAR(30) DEFAULT '' COMMENT 'chinese|math|...',
    `resource_form` VARCHAR(20) NOT NULL DEFAULT 'material' COMMENT 'exam|lesson_plan|ppt|video|doc|material|exercise',
    `topic_label` VARCHAR(100) DEFAULT '' COMMENT '专题副标题/适用说明',
    `school_year` VARCHAR(20) DEFAULT '' COMMENT '2025-2026',
    `file_format` VARCHAR(20) DEFAULT 'PDF',
    `file_url` VARCHAR(1000) DEFAULT '',
    `cover_url` VARCHAR(500) DEFAULT '',
    `icon` VARCHAR(10) DEFAULT '📚',
    `tags` VARCHAR(200) DEFAULT '',
    `download_count` INT NOT NULL DEFAULT 0,
    `view_count` INT NOT NULL DEFAULT 0,
    `is_free` TINYINT NOT NULL DEFAULT 1,
    `is_elite` TINYINT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `sort` INT NOT NULL DEFAULT 0,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_region` (`region`),
    KEY `idx_grade` (`grade_stage`),
    KEY `idx_subject` (`subject`),
    KEY `idx_form` (`resource_form`),
    KEY `idx_status` (`status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专题资源';

CREATE TABLE IF NOT EXISTS `topic_album` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `summary` VARCHAR(500) DEFAULT '',
    `category` VARCHAR(30) NOT NULL DEFAULT 'elite',
    `region` VARCHAR(20) NOT NULL DEFAULT 'all',
    `grade_stage` VARCHAR(20) NOT NULL DEFAULT 'all',
    `cover_url` VARCHAR(500) DEFAULT '',
    `icon` VARCHAR(10) DEFAULT '🏅',
    `tags` VARCHAR(200) DEFAULT '',
    `resource_count` INT NOT NULL DEFAULT 0,
    `download_count` INT NOT NULL DEFAULT 0,
    `is_free` TINYINT NOT NULL DEFAULT 1,
    `is_elite` TINYINT NOT NULL DEFAULT 1,
    `status` TINYINT NOT NULL DEFAULT 1,
    `sort` INT NOT NULL DEFAULT 0,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_region` (`region`),
    KEY `idx_grade` (`grade_stage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专题精品专辑';

CREATE TABLE IF NOT EXISTS `topic_album_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `album_id` BIGINT NOT NULL,
    `resource_id` BIGINT NOT NULL,
    `sort` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_album_res` (`album_id`, `resource_id`),
    KEY `idx_album` (`album_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专题专辑-资源关联';

DELETE FROM `topic_album_item` WHERE `album_id` BETWEEN 5001 AND 5004;
DELETE FROM `topic_album` WHERE `id` BETWEEN 5001 AND 5004;
DELETE FROM `topic_resource` WHERE `id` BETWEEN 5101 AND 5120;

INSERT INTO `topic_album` (`id`,`title`,`summary`,`category`,`region`,`grade_stage`,`icon`,`tags`,`resource_count`,`download_count`,`is_elite`,`sort`) VALUES
(5001,'成都片区·期末冲刺精选','成都各区期末模拟与复习讲义','midterm_final','chengdu','all','🐼','成都,期末冲刺',36,22100,1,100),
(5002,'绵阳片区·中考升学包','绵阳中考三轮复习与真题','promotion','mianyang','junior','🏔️','绵阳,中考,升学',28,15600,1,90),
(5003,'2026寒暑假作业全科合集','小学至高中暑假寒假作业精选','holiday_hw','all','all','🏖️','寒暑假,作业',42,38900,1,80),
(5004,'跨学科PBL项目方案库','STEAM与项目式学习教学设计','project','all','all','🔗','PBL,跨学科',24,9870,1,70);

INSERT INTO `topic_resource` (`id`,`title`,`summary`,`category`,`region`,`grade_stage`,`subject`,`resource_form`,`topic_label`,`file_format`,`icon`,`tags`,`download_count`,`view_count`,`is_free`,`is_elite`,`sort`) VALUES
(5101,'成都七年级数学期末冲刺模拟卷（6套）','贴合成都期末考情，含压轴题专项','midterm_final','chengdu','junior','math','exam','期末模拟','PDF','📝','专题资源,成都,期末冲刺',19800,65400,1,1,100),
(5102,'成都初三中考物理二轮专题突破','力学与电学综合训练','promotion','chengdu','junior','physics','material','中考二轮','PDF','⚡','专题资源,成都,中考',22300,71200,0,1,98),
(5103,'绵阳八年级英语寒假作业精选','词汇阅读完形专项','holiday_hw','mianyang','junior','english','material','寒假作业','PDF','📘','专题资源,绵阳,寒假作业',15600,48900,1,0,95),
(5104,'绵阳中考数学三轮冲刺讲义','压轴题与选填技巧','promotion','mianyang','junior','math','material','中考冲刺','PDF','📐','专题资源,绵阳,中考',26700,84500,0,1,93),
(5105,'2026小学三年级暑假作业全科合集','语数英暑假作业含答案','holiday_hw','all','primary','chinese','material','暑假作业','PDF','🏖️','专题资源,寒暑假',32100,98700,1,0,90),
(5106,'开学收心班会课件（三至六年级）','帮助学生快速进入学习状态','term_open','chengdu','primary','politics','ppt','开学备考','PPT','🎒','专题资源,开学备考',8760,31500,1,0,88),
(5107,'高一数学期中复习全套资料','知识点梳理与模拟卷','midterm_final','all','senior','math','material','期中复习','PDF','📊','专题资源,期中复习',19500,61200,1,0,85),
(5108,'八年级物理期末冲刺模拟卷（5套）','紧贴期末题型','midterm_final','all','junior','physics','exam','期末冲刺','PDF','🔬','专题资源,期末冲刺',21300,72400,0,0,82),
(5109,'时事热点·全国两会进课堂','初中道法时政学案','news','all','junior','politics','lesson_plan','时事热点','PDF','📰','专题资源,时事热点',7650,28900,1,0,80),
(5110,'跨学科PBL·成都博物馆研学方案','语文历史美术融合','cross','chengdu','primary','chinese','lesson_plan','PBL研学','PDF','🔗','专题资源,跨学科,PBL',5430,19800,1,0,78),
(5111,'绵阳科技城STEM创客项目手册','科学+信息实践','project','mianyang','primary','science','material','STEM','PDF','🔬','专题资源,STEM,PBL',6780,23400,1,0,75),
(5112,'高考英语一轮词汇专题（成都）','3500词分类记忆','promotion','chengdu','senior','english','material','高考一轮','PDF','📚','专题资源,高考,成都',20100,68900,0,1,72),
(5113,'小学六年级数学期末总复习课件','小学阶段知识点梳理','midterm_final','all','primary','math','ppt','期末总复习','PPT','📋','专题资源,期末冲刺',16700,56300,1,0,70),
(5114,'九年级寒假作业（中考方向）','语数英物化融入真题','holiday_hw','all','junior','math','material','寒假作业','PDF','❄️','专题资源,寒假作业,中考',24500,81200,1,0,68),
(5115,'成都小升初数学衔接暑假作业','小升高预习衔接','promotion','chengdu','primary','math','material','小升初','PDF','🎯','专题资源,小升初,成都',18900,56700,1,0,65),
(5116,'高二英语期末语法专项复习','高中语法考点训练','midterm_final','all','senior','english','material','期末语法','PDF','✍️','专题资源,期末冲刺',11200,38900,0,0,62),
(5117,'小学四年级暑假口算天天练','60天打卡含答案','holiday_hw','all','primary','math','exercise','暑假练习','PDF','✏️','专题资源,暑假作业',13400,41200,1,0,60),
(5118,'绵阳高一化学期中复习清单','必修知识点思维导图','midterm_final','mianyang','senior','chemistry','material','期中复习','PDF','🧪','专题资源,绵阳,期中',11200,37800,1,0,58),
(5119,'成都片区开学摸底测验卷（初中）','开学第一周诊断性测试','term_open','chengdu','junior','math','exam','开学备考','PDF','📄','专题资源,成都,开学',9800,32100,1,0,55),
(5120,'项目式学习·水资源保护跨学科方案','初中地理生物道法融合','project','all','junior','geography','lesson_plan','PBL项目','PDF','💧','专题资源,跨学科,项目式学习',4320,15600,1,0,50);

INSERT INTO `topic_album_item` (`album_id`,`resource_id`,`sort`) VALUES
(5001,5101,1),(5001,5107,2),(5001,5108,3),(5001,5113,4),
(5002,5104,1),(5002,5103,2),(5002,5114,3),
(5003,5105,1),(5003,5114,2),(5003,5117,3),
(5004,5110,1),(5004,5111,2),(5004,5120,3);
