-- ============================================================
-- 新课堂教育 — 国学阅读·作文深度融合（DDL + 种子数据）
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------
-- 1. sinology_reading 国学阅读素材表（纯内容，无关联外键）
--    所有维度关联通过 edu_resource + edu_resource_dimension 实现
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `sinology_reading`;

CREATE TABLE `sinology_reading` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `resource_id`     BIGINT UNSIGNED DEFAULT NULL COMMENT '关联 edu_resource.id，将国学素材映射为资源',
  `title`           VARCHAR(200) NOT NULL COMMENT '篇目标题',
  `dynasty`         VARCHAR(50)  DEFAULT NULL COMMENT '朝代：先秦/两汉/魏晋/唐/宋/元/明/清',
  `author`          VARCHAR(100) DEFAULT NULL COMMENT '作者/出处',
  `source_book`     VARCHAR(200) DEFAULT NULL COMMENT '出处典籍',
  `genre`           VARCHAR(50)  DEFAULT NULL COMMENT '体裁：诗词/文言文/蒙学/经典/成语/寓言',
  `content`         TEXT         NOT NULL COMMENT '原文内容',
  `translation`     TEXT         DEFAULT NULL COMMENT '白话译文',
  `appreciation`    TEXT         DEFAULT NULL COMMENT '赏析/写作手法分析',
  `composition_hint` TEXT        DEFAULT NULL COMMENT '作文启发：该篇可关联的写作训练方向',
  `difficulty`      TINYINT      DEFAULT 1 COMMENT '难度 1-5',
  `key_phrases`     VARCHAR(500) DEFAULT NULL COMMENT '名句/成语 逗号分隔',
  `audio_url`       VARCHAR(500) DEFAULT NULL COMMENT '朗读音频URL',
  `video_url`       VARCHAR(500) DEFAULT NULL COMMENT '讲解视频URL',
  `word_count`      INT          DEFAULT NULL COMMENT '正文字数',
  `status`          TINYINT      DEFAULT 1 COMMENT '0草稿 1已发布',
  `sort`            INT          DEFAULT 0,
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_dynasty` (`dynasty`),
  KEY `idx_genre` (`genre`),
  KEY `idx_title` (`title`),
  FULLTEXT KEY `ft_content` (`content`),
  FULLTEXT KEY `ft_translation` (`translation`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='国学阅读素材表';

-- ----------------------------------------------------------
-- 2. edu_school 学校信息表（纯信息，关联 edu_region）
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `edu_school`;

CREATE TABLE `edu_school` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`            VARCHAR(100) NOT NULL COMMENT '学校全称',
  `short_name`      VARCHAR(50)  DEFAULT NULL COMMENT '简称',
  `region_id`       INT UNSIGNED DEFAULT NULL COMMENT '关联 edu_region.id',
  `region_path`     VARCHAR(200) DEFAULT NULL COMMENT '地区路径：四川省/成都市/武侯区',
  `school_type`     VARCHAR(20)  DEFAULT NULL COMMENT '学校类型：public/private/key/foreign_lang',
  `school_level`    VARCHAR(20)  DEFAULT NULL COMMENT '学段：primary/junior/senior/k12',
  `tags`            VARCHAR(200) DEFAULT NULL COMMENT '标签：外语特色/竞赛强校/国学特色',
  `address`         VARCHAR(300) DEFAULT NULL,
  `contact`         VARCHAR(100) DEFAULT NULL,
  `status`          TINYINT      DEFAULT 1,
  `sort`            INT          DEFAULT 0,
  `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_region_id` (`region_id`),
  KEY `idx_school_type` (`school_type`),
  KEY `idx_name` (`name`),
  CONSTRAINT `fk_school_region` FOREIGN KEY (`region_id`) REFERENCES `edu_region` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学校信息表';

-- ----------------------------------------------------------
-- 3. 种子数据：edu_module 新增「国学阅读」栏目 (id=34)
-- ----------------------------------------------------------
INSERT IGNORE INTO `edu_module` (`id`, `name`, `icon`, `sort`, `status`) VALUES
(34, '国学阅读', '📜', 34, 1);

-- ----------------------------------------------------------
-- 4. 种子数据：四川地区 edu_region（如果不存在）
--    id 范围预留 510000-519999 作为四川专用
-- ----------------------------------------------------------
INSERT IGNORE INTO `edu_region` (`id`, `parent_id`, `name`, `level`, `code`, `sort`, `status`) VALUES
(510000, 0,    '四川省',     1, '510000', 0, 1),
(510100, 510000, '成都市',   2, '510100', 1, 1),
(510104, 510100, '锦江区',   3, '510104', 1, 1),
(510107, 510100, '武侯区',   3, '510107', 2, 1),
(510106, 510100, '金牛区',   3, '510106', 3, 1),
(510108, 510100, '成华区',   3, '510108', 4, 1),
(510182, 510100, '彭州市',   3, '510182', 5, 1),
(510700, 510000, '绵阳市',   2, '510700', 2, 1),
(510703, 510700, '涪城区',   3, '510703', 1, 1),
(511800, 510000, '都江堰市', 2, '511800', 3, 1);

-- ----------------------------------------------------------
-- 5. 种子数据：四川地区标杆学校
-- ----------------------------------------------------------
INSERT IGNORE INTO `edu_school` (`id`, `name`, `short_name`, `region_id`, `region_path`, `school_type`, `school_level`, `tags`, `sort`) VALUES
(1, '成都外国语学校',           '成外',    510104, '四川省/成都市/锦江区', 'foreign_lang', 'k12',     '外语特色,竞赛强校,国学特色',   1),
(2, '四川省成都市石室中学',     '石室中学', 510107, '四川省/成都市/武侯区', 'key',         'senior',  '千年名校,国学特色,文科强校',   2),
(3, '成都市第七中学',           '七中',    510106, '四川省/成都市/金牛区', 'key',         'senior',  '理科强校,竞赛强校',            3),
(4, '成都市第四中学',           '四中',    510108, '四川省/成都市/成华区', 'key',         'senior',  '文科强校',                     4),
(5, '四川省绵阳中学',           '绵中',    510703, '四川省/绵阳市/涪城区', 'key',         'senior',  '川北名校,升学率高',            5),
(6, '彭州市延秀小学',           '延秀小学', 510182, '四川省/成都市/彭州市', 'public',      'primary', '国学特色校',                   6),
(7, '都江堰市北街小学',         '北街小学', 511800, '四川省/都江堰市',     'public',      'primary', '传统文化教育示范校',          7);

-- ----------------------------------------------------------
-- 6. 示例种子数据：国学阅读素材（4篇示例）
-- ----------------------------------------------------------
INSERT INTO `sinology_reading` (`id`, `resource_id`, `title`, `dynasty`, `author`, `source_book`, `genre`, `content`, `translation`, `appreciation`, `composition_hint`, `difficulty`, `key_phrases`, `word_count`) VALUES
(1, NULL, '陋室铭', '唐', '刘禹锡', '全唐文', '文言文',
 '山不在高，有仙则名。水不在深，有龙则灵。斯是陋室，惟吾德馨。苔痕上阶绿，草色入帘青。谈笑有鸿儒，往来无白丁。可以调素琴，阅金经。无丝竹之乱耳，无案牍之劳形。南阳诸葛庐，西蜀子云亭。孔子云：何陋之有？',
 '山不一定要高，有仙人居住就会出名。水不一定要深，有蛟龙潜藏就有灵气。这虽是简陋的屋子，只要我的品德高尚就不觉得简陋。青苔长上台阶，一片碧绿；草色映入窗帘，满目青葱。在这里谈笑的都是学问渊博的人，往来的没有不学无术之徒。可以弹奏朴素的琴，阅读佛经。没有嘈杂的乐声扰乱双耳，没有官府的公文劳累身心。好比南阳诸葛亮的茅庐，西蜀扬子云的玄亭。孔子说：有什么简陋的呢？',
 '本文运用托物言志的手法，借陋室表达作者高洁傲岸的节操和安贫乐道的情趣。全文81字，结构严谨，层次分明。开篇以山水起兴，引出"陋室"与"德馨"的对比关系，是比兴手法的典范。中段从环境、交往、生活三方面铺陈渲染，句式骈散结合，韵律和谐。结尾以诸葛庐、子云亭自比，用孔子之言作结，以反问收束，余韵悠长。写作借鉴：①托物言志手法的运用；②排比句式增强气势；③以典故结尾提升立意。',
 '写作训练方向：\n1. 托物言志：选择身边一件普通物品（旧书桌、一支笔、一棵树），通过描写它来寄托自己的品格或志向。\n2. 环境描写：仿写"苔痕上阶绿，草色入帘青"，用白描手法写一个印象深刻的场景。\n3. 议论文立意：以"物质与精神"为话题，结合本文观点写议论文。\n4. 文白互译：将原文改写为现代散文，体会文言文的精炼之美。',
 2, '山不厌高，水不厌深|谈笑有鸿儒，往来无白丁|何陋之有', 81),

(2, NULL, '观书有感', '宋', '朱熹', '朱文公文集', '诗词',
 '半亩方塘一鉴开，天光云影共徘徊。\n问渠那得清如许？为有源头活水来。',
 '半亩大的方形池塘像一面镜子一样打开，天光和云影在水面上闪耀浮动。要问池塘里的水为何这样清澈？是因为有永不枯竭的源头源源不断地为它输送活水。',
 '此诗为哲理诗经典，借景喻理，以池塘活水比喻读书学习要不断汲取新知。前两句写景，以"方塘"喻书，"天光云影"喻书中广阔天地；后两句问答，揭示哲理——人要保持思想澄明、才思不断，必须持续学习，像源头活水一样日日更新。全诗28字，设问句式引人深思，比喻贴切而不露痕迹。写作借鉴：①以物喻理的写法——从具体事物引出抽象道理；②问答句式增强表达效果；③以小见大的构思方式。',
 '写作训练方向：\n1. 哲理感悟：以生活中一个常见现象（泉水、河流、树苗）写一段话，引出对"持续学习/成长"的感悟。\n2. 设问写法：用"问渠那得清如许"的自问自答结构写一篇习作。\n3. 议论文素材：以"源头活水"为比喻，论述创新的重要性。\n4. 诗歌鉴赏：比较阅读《观书有感（其二）》，分析两首诗的异同。',
 2, '问渠那得清如许|为有源头活水来', 28),

(3, NULL, '孙权劝学', '宋', '司马光', '资治通鉴', '文言文',
 '初，权谓吕蒙曰："卿今当涂掌事，不可不学！"蒙辞以军中多务。权曰："孤岂欲卿治经为博士邪！但当涉猎，见往事耳。卿言多务，孰若孤？孤常读书，自以为大有所益。"蒙乃始就学。及鲁肃过寻阳，与蒙论议，大惊曰："卿今者才略，非复吴下阿蒙！"蒙曰："士别三日，即更刮目相待，大兄何见事之晚乎！"肃遂拜蒙母，结友而别。',
 '当初，孙权对吕蒙说："你现在当权掌管事务，不可以不学习！"吕蒙用军中事务繁多为理由来推辞。孙权说："我难道想要你研究儒家经典成为博士吗？只是应当粗略地阅读，了解历史罢了。你说事务繁多，谁比得上我呢？我经常读书，自以为大有好处。"吕蒙于是开始学习。等到鲁肃经过寻阳，与吕蒙议论，十分惊奇地说："你现在的才干谋略，不再是过去吴地的那个阿蒙了！"吕蒙说："读书人分别几天，就要用新的眼光来看待，兄长为什么领悟事情这么晚呢！"鲁肃于是拜见吕蒙的母亲，与吕蒙结为好友后分别。',
 '本文以对话推动情节，人物形象鲜明。孙权作为君主，循循善诱，现身说法说服吕蒙学习；吕蒙从"辞以多务"到"乃始就学"，态度转变可信；鲁肃"大惊"衬托吕蒙进步之大。"吴下阿蒙""刮目相待"成为千古成语。全文119字，干净利落，无一句废话，是对话描写的典范。写作借鉴：①通过对话展现人物性格的变化；②侧面烘托——以鲁肃之惊写吕蒙之变；③简短故事蕴含深刻道理。',
 '写作训练方向：\n1. 人物对话：写一段通过对话展现人物成长的片断。\n2. 论证"学无止境"：以吕蒙的故事为论据，写一篇议论文。\n3. 缩写与扩写：将本文缩写为50字短文，或扩写吕蒙学习的具体过程。\n4. 续写：写鲁肃离开后与友人谈起吕蒙的对话场景。',
 2, '士别三日，即更刮目相待|吴下阿蒙|但当涉猎，见往事耳', 119),

(4, NULL, '学弈', '先秦', '孟子', '孟子·告子上', '文言文',
 '弈秋，通国之善弈者也。使弈秋诲二人弈，其一人专心致志，惟弈秋之为听；一人虽听之，一心以为有鸿鹄将至，思援弓缴而射之。虽与之俱学，弗若之矣。为是其智弗若与？曰：非然也。',
 '弈秋是全国最擅长下棋的人。让弈秋教导两个人下棋，其中一人专心致志，只听弈秋的教导；另一人虽然也在听，却一心以为有天鹅将要飞来，想要拉弓搭箭去射它。虽然他和前一个人一起学习，却不如人家。是因为他的智力不如人家吗？回答说：不是这样的。',
 '本文运用对比手法，以学弈为喻揭示"专心致志"的重要性。两人同师而学，结果迥异，原因不在智力而在态度。结构上先叙事再议论，末句问答点明主旨：学习效果取决于专注程度而非天赋。语言精炼，68字道尽学习真谛，是极佳的议论文论据素材。写作借鉴：①对比手法——通过两个学生的对比突出主题；②设问结尾，引人深思；③以小见大——从学棋小故事引出"态度决定一切"的大道理。',
 '写作训练方向：\n1. 对比描写：写一个场景中的两个人物，通过对比突出不同品质。\n2. 议论文：以"专心致志"为题，用本文和其他事例写一篇议论文。\n3. 寓言创作：仿照本文，自编一个200字以内的寓言故事说明某个道理。\n4. 续写：写"专心致志"的学生后来成为国手的经过。',
 1, '专心致志|一心以为有鸿鹄将至|思援弓缴而射之', 68);

SET FOREIGN_KEY_CHECKS = 1;
