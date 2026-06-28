-- ============================================================
-- 传统文化 · 巴蜀研学（方案 B+C）
-- 主题 + 地域（成都/巴蜀）+ 研学时长，不分学段
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `culture_resource` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL COMMENT '标题',
    `summary` VARCHAR(500) DEFAULT '' COMMENT '简介',
    `category` VARCHAR(30) NOT NULL COMMENT 'guoxue|shici|calligraphy|festival|story|customs|bashu|yanxue',
    `region` VARCHAR(30) NOT NULL DEFAULT 'bashu' COMMENT 'chengdu|bashu|sichuan|all',
    `duration_type` VARCHAR(30) NOT NULL DEFAULT 'one_day' COMMENT 'half_day|one_day|two_three_day|camp|flexible',
    `duration_label` VARCHAR(50) DEFAULT '' COMMENT '展示用时长文案',
    `suitable_audience` VARCHAR(100) DEFAULT '研学机构' COMMENT '适用对象',
    `location` VARCHAR(100) DEFAULT '' COMMENT '地点/线路',
    `resource_kind` VARCHAR(20) NOT NULL DEFAULT 'platform' COMMENT 'platform自有|external外链',
    `file_format` VARCHAR(20) DEFAULT 'PDF' COMMENT 'PPT|PDF|视频|方案',
    `file_url` VARCHAR(1000) DEFAULT '' COMMENT '平台资源下载/预览地址',
    `cover_url` VARCHAR(500) DEFAULT '',
    `icon` VARCHAR(10) DEFAULT '📜',
    `source_name` VARCHAR(100) DEFAULT '' COMMENT '外链来源名称',
    `external_url` VARCHAR(1000) DEFAULT '' COMMENT '外链地址',
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
    KEY `idx_duration` (`duration_type`),
    KEY `idx_kind` (`resource_kind`),
    KEY `idx_status` (`status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='传统文化研学资源';

CREATE TABLE IF NOT EXISTS `culture_study_package` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `summary` VARCHAR(500) DEFAULT '',
    `region` VARCHAR(30) NOT NULL DEFAULT 'chengdu',
    `duration_type` VARCHAR(30) NOT NULL DEFAULT 'one_day',
    `duration_label` VARCHAR(50) DEFAULT '',
    `suitable_audience` VARCHAR(100) DEFAULT '小学研学/亲子',
    `location` VARCHAR(100) DEFAULT '',
    `cover_url` VARCHAR(500) DEFAULT '',
    `icon` VARCHAR(10) DEFAULT '🏮',
    `tags` VARCHAR(200) DEFAULT '',
    `resource_count` INT NOT NULL DEFAULT 0,
    `download_count` INT NOT NULL DEFAULT 0,
    `is_free` TINYINT NOT NULL DEFAULT 1,
    `is_elite` TINYINT NOT NULL DEFAULT 0,
    `status` TINYINT NOT NULL DEFAULT 1,
    `sort` INT NOT NULL DEFAULT 0,
    `is_deleted` TINYINT NOT NULL DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_region` (`region`),
    KEY `idx_duration` (`duration_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='传统文化研学线路包';

CREATE TABLE IF NOT EXISTS `culture_package_item` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `package_id` BIGINT NOT NULL,
    `resource_id` BIGINT NOT NULL,
    `sort` INT NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_pkg_res` (`package_id`, `resource_id`),
    KEY `idx_package` (`package_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='研学包-资源关联';

-- ==================== 种子数据（可重复执行：先清后插） ====================
DELETE FROM `culture_package_item` WHERE `package_id` BETWEEN 3001 AND 3006;
DELETE FROM `culture_study_package` WHERE `id` BETWEEN 3001 AND 3006;
DELETE FROM `culture_resource` WHERE `id` BETWEEN 1001 AND 1010 OR `id` BETWEEN 2001 AND 2006;

-- 平台自有资源
INSERT INTO `culture_resource` (
    `id`, `title`, `summary`, `category`, `region`, `duration_type`, `duration_label`,
    `suitable_audience`, `location`, `resource_kind`, `file_format`, `icon`, `tags`, `download_count`, `is_elite`, `sort`
) VALUES
(1001, '《论语》节选诵读与讲解（研学版）', '适合半日营地诵读活动，含拼音与释义', 'guoxue', 'bashu', 'half_day', '半日', '小学研学', '营地课堂', 'platform', 'PDF', '📜', '国学,论语,诵读', 1280, 1, 10),
(1002, '杜甫草堂诗词研学手册', '杜甫在蜀诗歌选读+任务卡+讲解词', 'shici', 'chengdu', 'one_day', '1日', '小学-初中研学', '杜甫草堂', 'platform', 'PDF', '🖌️', '诗词,杜甫,草堂', 3420, 1, 20),
(1003, '楷书入门：研学书法体验课', '基本笔画+临帖页+教师示范说明', 'calligraphy', 'bashu', 'half_day', '半日', '全年龄体验', '教培教室', 'platform', 'PDF', '✒️', '书法,楷书', 2100, 0, 30),
(1004, '成都端午民俗：抢鸭子与游百病', '巴蜀端午习俗图文+互动问答', 'festival', 'chengdu', 'half_day', '半日', '亲子研学', '成都市区', 'platform', 'PPT', '🏮', '端午,民俗,成都', 1890, 0, 40),
(1005, '都江堰与李冰治水故事', '民间传说+水利科普+研学任务单', 'story', 'chengdu', 'one_day', '1日', '小学研学', '都江堰景区', 'platform', 'PDF', '📖', '都江堰,民间故事,水利', 4560, 1, 50),
(1006, '川剧变脸非遗科普讲义', '非遗知识+观摩要点+安全提示', 'customs', 'chengdu', 'half_day', '半日', '研学机构', '蜀风雅韵/剧院', 'platform', 'PPT', '🎭', '川剧,变脸,非遗', 2780, 0, 60),
(1007, '金沙遗址：古蜀文明探秘', '太阳神鸟+祭祀文化+展厅研学题', 'bashu', 'chengdu', 'one_day', '1日', '初中研学', '金沙遗址博物馆', 'platform', 'PDF', '🏛️', '金沙,古蜀,博物馆', 3890, 1, 70),
(1008, '蜀绣体验：非遗手工研学方案', '针法简介+体验流程+物料清单', 'customs', 'chengdu', 'half_day', '半日', '亲子/小学', '蜀绣工坊', 'platform', 'PDF', '🧵', '蜀绣,非遗,手工', 1560, 0, 80),
(1009, '李白与蜀道：诗歌文化行', '诗歌选读+路线讲解+打卡任务', 'shici', 'bashu', 'one_day', '1日', '初中研学', '江油/峨眉山', 'platform', 'PDF', '🖌️', '李白,蜀道,诗词', 2340, 0, 90),
(1010, '研学带队教师安全须知（成都版）', '交通、饮食、应急、保险 checklist', 'yanxue', 'chengdu', 'flexible', '通用', '教培机构', '成都出发线路', 'platform', 'PDF', '📋', '研学,安全,教师', 5120, 1, 100);

-- 外链延展（方案 C）
INSERT INTO `culture_resource` (
    `id`, `title`, `summary`, `category`, `region`, `duration_type`, `duration_label`,
    `resource_kind`, `source_name`, `external_url`, `icon`, `tags`, `view_count`, `sort`
) VALUES
(2001, '中国非物质文化遗产网 · 巴蜀专区', '国家级非遗名录检索与介绍', 'customs', 'bashu', 'flexible', '在线', 'external', '中国非遗网', 'https://www.ihchina.cn/', '🌐', '非遗,官方', 890, 200),
(2002, '四川省文化和旅游厅', '文旅政策、非遗资讯、四川文旅活动', 'bashu', 'sichuan', 'flexible', '在线', 'external', '四川省文旅厅', 'https://wlt.sc.gov.cn/', '🌐', '文旅,四川', 760, 201),
(2003, '国家中小学智慧教育平台 · 传统文化', '教育部精品传统文化课程资源', 'guoxue', 'all', 'flexible', '在线', 'external', '智慧教育平台', 'https://www.zxx.edu.cn/', '🌐', '国学,官方课程', 2340, 202),
(2004, '成都武侯祠博物馆官网', '三国文化展陈与研学预约参考', 'bashu', 'chengdu', 'flexible', '在线', 'external', '武侯祠博物馆', 'https://www.wuhouci.net.cn/', '🌐', '三国,武侯祠', 1120, 203),
(2005, '杜甫草堂博物馆', '草堂导览、活动公告', 'shici', 'chengdu', 'flexible', '在线', 'external', '杜甫草堂', 'https://www.cddfct.com/', '🌐', '杜甫,诗歌', 980, 204),
(2006, '金沙遗址博物馆', '展览资讯与教育服务', 'bashu', 'chengdu', 'flexible', '在线', 'external', '金沙遗址博物馆', 'https://www.jinshasitemuseum.com/', '🌐', '金沙,古蜀', 1340, 205);

-- 研学线路包
INSERT INTO `culture_study_package` (
    `id`, `title`, `summary`, `region`, `duration_type`, `duration_label`,
    `suitable_audience`, `location`, `icon`, `tags`, `resource_count`, `download_count`, `is_elite`, `sort`
) VALUES
(3001, '杜甫草堂诗词文化一日研学', '诗歌鉴赏+草堂导览+任务卡+评价表全套', 'chengdu', 'one_day', '1日', '小学高年级/初中', '青羊区·杜甫草堂', '🖌️', '诗词,草堂,一日', 5, 2890, 1, 10),
(3002, '都江堰水利与民间传说研学', '李冰治水+民间故事+科学探究', 'chengdu', 'one_day', '1日', '小学研学', '都江堰市', '💧', '都江堰,水利,一日', 4, 3560, 1, 20),
(3003, '金沙遗址古蜀文明半日营', '展厅研学+太阳神鸟主题任务', 'chengdu', 'half_day', '半日', '小学-初中', '金沙遗址博物馆', '🏛️', '金沙,古蜀,半日', 3, 1980, 0, 30),
(3004, '川剧非遗+蜀绣手工一日体验', '上午观演科普+下午手工体验', 'chengdu', 'one_day', '1日', '亲子/小学', '市区非遗体验点', '🎭', '川剧,蜀绣,非遗', 4, 2450, 1, 40),
(3005, '三国文化武侯祠深度研学', '诸葛亮与蜀汉文化专题', 'chengdu', 'one_day', '1日', '初中研学', '武侯祠', '⚔️', '三国,武侯祠', 3, 3120, 1, 50),
(3006, '巴蜀传统文化两日营（营地版）', '国学诵读+民俗+安全须知组合包', 'bashu', 'two_three_day', '2日', '教培营地', '成都近郊营地', '🏮', '综合,两日,营地', 8, 1780, 1, 60);

INSERT INTO `culture_package_item` (`package_id`, `resource_id`, `sort`) VALUES
(3001, 1002, 1), (3001, 1010, 2),
(3002, 1005, 1), (3002, 1010, 2),
(3003, 1007, 1), (3003, 1010, 2),
(3004, 1006, 1), (3004, 1008, 2), (3004, 1010, 3),
(3005, 1007, 1),
(3006, 1001, 1), (3006, 1004, 2), (3006, 1006, 3), (3006, 1010, 4);
