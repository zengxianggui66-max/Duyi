-- ============================================================
-- 竞赛专区（方案 B）
-- 学段 × 竞赛类型 × 资源形态
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `competition_resource` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `summary` VARCHAR(500) DEFAULT '' COMMENT '简介',
    `category` VARCHAR(30) NOT NULL COMMENT 'olympiad|primary_math|math|physics|chemistry|biology|info|writing|english|cert|exam_prep',
    `grade_stage` VARCHAR(20) NOT NULL DEFAULT 'all' COMMENT 'primary|junior|senior|all',
    `subject` VARCHAR(30) DEFAULT '' COMMENT 'math|physics|...',
    `resource_form` VARCHAR(20) NOT NULL DEFAULT 'document' COMMENT 'exam|mock|lecture|lesson_plan|ppt|video|doc|exercise',
    `competition_name` VARCHAR(100) DEFAULT '' COMMENT '赛事/考级名称',
    `file_format` VARCHAR(20) DEFAULT 'PDF',
    `file_url` VARCHAR(1000) DEFAULT '',
    `cover_url` VARCHAR(500) DEFAULT '',
    `icon` VARCHAR(10) DEFAULT '🏆',
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
    KEY `idx_grade` (`grade_stage`),
    KEY `idx_subject` (`subject`),
    KEY `idx_form` (`resource_form`),
    KEY `idx_status` (`status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛专区资源';

CREATE TABLE IF NOT EXISTS `competition_training_pack` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `summary` VARCHAR(500) DEFAULT '',
    `category` VARCHAR(30) NOT NULL DEFAULT 'elite',
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
    KEY `idx_grade` (`grade_stage`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛备考精品包';

CREATE TABLE IF NOT EXISTS `competition_pack_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `package_id` BIGINT NOT NULL,
    `resource_id` BIGINT NOT NULL,
    `sort` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pkg_res` (`package_id`, `resource_id`),
    KEY `idx_package` (`package_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞赛包-资源关联';

DELETE FROM `competition_pack_item` WHERE `package_id` BETWEEN 4001 AND 4004;
DELETE FROM `competition_training_pack` WHERE `id` BETWEEN 4001 AND 4004;
DELETE FROM `competition_resource` WHERE `id` BETWEEN 4101 AND 4112;

INSERT INTO `competition_training_pack` (`id`,`title`,`summary`,`category`,`grade_stage`,`icon`,`tags`,`resource_count`,`download_count`,`is_elite`,`sort`) VALUES
(4001,'奥数培优全套课程','小学至初中奥数体系化讲义与真题','primary_math','all','📐','奥数,数学竞赛',50,15600,1,100),
(4002,'信息学CSP备考包','C++算法入门到提高，含模拟题','info','junior','💻','CSP,NOIP,信息学',35,9870,1,90),
(4003,'全国作文大赛指导','范文、技法与评委点评','writing','all','✍️','作文大赛',25,7650,1,80),
(4004,'物理竞赛冲刺资料','力学电磁学专题突破','physics','senior','⚡','物理竞赛',40,6540,1,70);

INSERT INTO `competition_resource` (`id`,`title`,`summary`,`category`,`grade_stage`,`subject`,`resource_form`,`competition_name`,`file_format`,`icon`,`tags`,`download_count`,`view_count`,`is_free`,`is_elite`,`sort`) VALUES
(4101,'全国初中数学竞赛真题汇编（近5年）','含2022-2026年联赛真题及解析','math','junior','math','exam','初中数学联赛','PDF','📝','学科竞赛,数学竞赛,真题',25600,89200,0,1,100),
(4102,'高中物理竞赛力学专题讲义','牛顿力学、动量能量核心考点','physics','senior','physics','lecture','物理竞赛','PDF','⚡','学科竞赛,物理竞赛',8900,32100,0,1,95),
(4103,'化学竞赛无机化学专题训练','元素化合物与实验设计','chemistry','senior','chemistry','exercise','化学竞赛','PDF','🧪','学科竞赛,化学竞赛',6750,24500,0,0,90),
(4104,'NOIP信息学竞赛C++入门','基础语法、算法与数据结构','info','senior','','video','NOIP','MP4','💻','学科竞赛,信息学,NOIP',12300,45600,0,1,88),
(4105,'全国高中数学联赛模拟卷（10套）','一试二试模拟环境','math','senior','math','mock','高中数学联赛','PDF','📐','学科竞赛,数学竞赛,模拟',17800,62300,0,1,85),
(4106,'生物竞赛细胞生物学专题','细胞结构、代谢与分裂','biology','senior','biology','lecture','生物竞赛','PDF','🧬','学科竞赛,生物竞赛',3450,12800,0,0,80),
(4107,'小学奥数思维训练（三年级）','华杯赛/希望杯入门','primary_math','primary','math','exercise','华杯赛','PDF','🔢','学科竞赛,奥数,小学',18900,52100,1,0,78),
(4108,'小学奥数培优讲义（四年级）','应用题与计数原理','primary_math','primary','math','lesson_plan','希望杯','PDF','📋','学科竞赛,奥数',14200,43800,1,0,75),
(4109,'全国中学生英语能力竞赛真题','NEPCS 真题与听力指导','english','junior','english','exam','NEPCS','PDF','📝','学科竞赛,英语竞赛',9800,31200,0,0,72),
(4110,'CSP-J 入门算法视频课','排序、搜索、贪心入门','info','junior','','video','CSP-J','MP4','💻','学科竞赛,CSP,编程',22100,67800,0,1,70),
(4111,'美术考级1-3级备考PPT','儿童画与色彩考级要点','cert','primary','art','ppt','美术考级','PPT','🎨','学科竞赛,考级,美术',5430,18900,1,0,65),
(4112,'高中竞赛考前冲刺名校加分指南','强基计划与证书运用','exam_prep','senior','math','doc','强基计划','PDF','🎯','学科竞赛,考前辅导,名校',11200,38900,0,1,60);

INSERT INTO `competition_pack_item` (`package_id`,`resource_id`,`sort`) VALUES
(4001,4107,1),(4001,4108,2),(4001,4101,3),
(4002,4104,1),(4002,4110,2),
(4003,4109,1),
(4004,4102,1),(4004,4103,2);
