-- ============================================================
-- 备课中心：资料篮 + 试题库（支持选题组卷）
-- mysql -u root -p xinketang < sql/19_prep_center.sql
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `prep_basket` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(50) DEFAULT '默认资料篮' COMMENT '篮子名称',
  `is_default` TINYINT DEFAULT 1 COMMENT '是否默认篮',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='备课资料篮';

CREATE TABLE IF NOT EXISTS `prep_basket_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `basket_id` BIGINT NOT NULL,
  `item_type` VARCHAR(20) NOT NULL COMMENT 'resource|question|paper|album',
  `ref_id` BIGINT NOT NULL COMMENT '关联ID',
  `title` VARCHAR(255) NOT NULL DEFAULT '',
  `subtitle` VARCHAR(255) DEFAULT NULL COMMENT '副标题/题型/来源',
  `cover_url` VARCHAR(500) DEFAULT NULL,
  `meta_json` JSON DEFAULT NULL COMMENT '扩展：学科、难度、分值等',
  `sort_order` INT DEFAULT 0,
  `score` DECIMAL(6,1) DEFAULT NULL COMMENT '试题分值',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_basket_item` (`basket_id`, `item_type`, `ref_id`),
  KEY `idx_basket_id` (`basket_id`),
  KEY `idx_item_type` (`item_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资料篮条目';

CREATE TABLE IF NOT EXISTS `question_bank` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `stem` TEXT NOT NULL COMMENT '题干HTML',
  `question_type` VARCHAR(30) NOT NULL DEFAULT 'choice' COMMENT 'choice|blank|answer|composite',
  `options_json` JSON DEFAULT NULL COMMENT '选择题选项',
  `answer` TEXT COMMENT '答案',
  `analysis` TEXT COMMENT '解析',
  `grade_level` VARCHAR(20) NOT NULL DEFAULT 'junior',
  `subject` VARCHAR(30) NOT NULL DEFAULT 'math',
  `difficulty` TINYINT DEFAULT 3 COMMENT '1-5',
  `score` DECIMAL(6,1) DEFAULT 5.0,
  `knowledge_points` VARCHAR(500) DEFAULT NULL COMMENT '知识点，逗号分隔',
  `source_type` VARCHAR(30) DEFAULT 'real' COMMENT 'real|mock|sync|special',
  `source_name` VARCHAR(100) DEFAULT NULL COMMENT '来源卷名称',
  `region` VARCHAR(50) DEFAULT 'national',
  `year` INT DEFAULT NULL,
  `usage_count` INT DEFAULT 0,
  `status` TINYINT DEFAULT 1,
  `deleted` TINYINT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_subject` (`grade_level`, `subject`),
  KEY `idx_difficulty` (`difficulty`),
  KEY `idx_source` (`source_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试题库';

-- 种子试题（支持选题组卷演示）
INSERT INTO `question_bank` (
  `id`, `stem`, `question_type`, `options_json`, `answer`, `analysis`,
  `grade_level`, `subject`, `difficulty`, `score`, `knowledge_points`,
  `source_type`, `source_name`, `region`, `year`, `usage_count`
) VALUES
(2001, '<p>下列方程中，是一元一次方程的是（　）</p>', 'choice',
 '["A. x²=4","B. 2x+1=0","C. xy=2","D. 1/x=1"]', 'B', '<p>一元一次方程只含一个未知数且次数为1。</p>',
 'junior', 'math', 2, 3.0, '一元一次方程', 'sync', '七年级同步练习', 'national', 2026, 120),

(2002, '<p>化简：3a²b - 2ab² + ab² - 5a²b</p>', 'answer', NULL, '-2a²b - ab²',
 '<p>合并同类项，注意符号。</p>', 'junior', 'math', 3, 4.0, '整式加减', 'sync', '七年级上册', 'national', 2026, 88),

(2003, '<p>成都中考：如图，在△ABC中，DE∥BC，若AD:DB=2:3，AE=4，则EC的长为（　）</p>', 'choice',
 '["A. 6","B. 8","C. 10","D. 12"]', 'A', '<p>平行线分线段成比例。</p>',
 'junior', 'math', 4, 4.0, '相似三角形', 'real', '2025成都中考数学', 'chengdu', 2025, 560),

(2004, '<p>阅读《岳阳楼记》选段，回答问题：文中体现作者政治抱负的句子是____。</p>', 'blank', NULL,
 '先天下之忧而忧，后天下之乐而乐', '<p>理解主旨与名句默写。</p>',
 'junior', 'chinese', 3, 5.0, '文言文阅读', 'real', '2025绵阳中考语文', 'mianyang', 2025, 320),

(2005, '<p>Choose the correct answer: —Must I finish it today? —No, you ____.</p>', 'choice',
 '["A. mustn''t","B. needn''t","C. can''t","D. shouldn''t"]', 'B', '<p>情态动词 must 的否定回答用 needn''t。</p>',
 'junior', 'english', 2, 2.0, '情态动词', 'sync', '八年级英语Unit3', 'national', 2026, 210),

(2006, '<p>下列物质中，属于纯净物的是（　）</p>', 'choice',
 '["A. 空气","B. 蒸馏水","C. 矿泉水","D. 生铁"]', 'B', '<p>纯净物由一种物质组成。</p>',
 'junior', 'chemistry', 2, 3.0, '物质分类', 'sync', '九年级化学', 'national', 2026, 175),

(2007, '<p>关于牛顿第一定律，下列说法正确的是（　）</p>', 'choice',
 '["A. 物体不受力一定静止","B. 惯性是物体保持运动状态不变的性质","C. 力是维持运动的原因","D. 物体受力一定运动"]', 'B',
 '<p>惯性概念与力不是维持运动的原因。</p>', 'junior', 'physics', 3, 4.0, '牛顿第一定律', 'real', '2024成都中考物理', 'chengdu', 2024, 430),

(2008, '<p>细胞是生命活动的基本单位，植物细胞特有的结构是（　）</p>', 'choice',
 '["A. 细胞膜","B. 细胞壁","C. 线粒体","D. 细胞核"]', 'B', '<p>植物细胞有细胞壁、叶绿体等。</p>',
 'junior', 'biology', 1, 2.0, '细胞结构', 'sync', '七年级生物', 'national', 2026, 98),

(2009, '<p>解不等式组：{ 2x-1 &gt; 3, x+4 ≤ 10 }</p>', 'answer', NULL, '2 &lt; x ≤ 6',
 '<p>分别求解后取交集。</p>', 'junior', 'math', 3, 5.0, '一元一次不等式组', 'special', '期中复习专项', 'sichuan', 2026, 145),

(2010, '<p>高考数学：已知函数 f(x)=ln x - ax，若 f(x) ≤ 0 恒成立，求实数 a 的取值范围。</p>', 'answer', NULL, 'a ≥ 1/e',
 '<p>分离参数或构造函数求最值。</p>', 'senior', 'math', 5, 12.0, '导数应用', 'real', '2025全国乙卷', 'national', 2025, 890),

(2011, '<p>下列成语使用正确的一项是（　）</p>', 'choice',
 '["A. 他做事总是画龙点睛","B. 这篇文章妙语连珠","C. 同学们济济一堂地散去","D. 这个问题不刊之论"]', 'B',
 '<p>成语辨析。</p>', 'senior', 'chinese', 3, 3.0, '成语运用', 'mock', '高考模拟卷一', 'national', 2026, 220),

(2012, '<p>完形填空：Learning is a lifelong journey. We should never stop ____ new things.</p>', 'choice',
 '["A. learn","B. learning","C. learned","D. to learning"]', 'B', '<p>stop doing 表示停止正在做的事；此处为不断学习。</p>',
 'senior', 'english', 2, 2.5, '非谓语动词', 'mock', '高考英语模拟', 'national', 2026, 156),

(2013, '<p>有机化学：苯与浓硝酸在浓硫酸催化下反应的主要产物是____。</p>', 'blank', NULL, '硝基苯',
 '<p>硝化反应。</p>', 'senior', 'chemistry', 4, 4.0, '芳香烃', 'sync', '选修有机化学', 'national', 2026, 67),

(2014, '<p>电磁感应：闭合线圈在匀强磁场中转动，产生正弦交流电，其最大值由哪些因素决定？</p>', 'answer', NULL,
 '匝数、磁感应强度、面积、角速度', '<p>Em=NBSω。</p>', 'senior', 'physics', 4, 6.0, '电磁感应', 'real', '2025四川高考物理', 'sichuan', 2025, 310),

(2015, '<p>遗传学：豌豆杂交实验中，子二代性状分离比接近（　）</p>', 'choice',
 '["A. 1:1","B. 3:1","C. 9:3:3:1","D. 1:2:1"]', 'B', '<p>孟德尔分离定律。</p>',
 'senior', 'biology', 2, 3.0, '遗传规律', 'sync', '必修二', 'national', 2026, 124),

(2016, '<p>小学数学：一个长方形长8cm、宽5cm，它的周长是____厘米。</p>', 'blank', NULL, '26',
 '<p>周长=(8+5)×2。</p>', 'primary', 'math', 1, 2.0, '长方形周长', 'sync', '三年级数学', 'national', 2026, 45),

(2017, '<p>「双减」背景下，下列作业设计符合要求的是（　）</p>', 'choice',
 '["A. 机械抄写生字10遍","B. 分层弹性作业","C. 统一超纲竞赛题","D. 家长代做"]', 'B',
 '<p>政策导向减量提质、分层设计。</p>', 'primary', 'chinese', 1, 2.0, '作业设计', 'special', '教研动态', 'national', 2026, 38),

(2018, '<p>绵阳中考：二次函数 y=ax²+bx+c 图像过点(1,0)，对称轴 x=2，则 b=____。</p>', 'blank', NULL, '-4a',
 '<p>对称轴公式与代入。</p>', 'junior', 'math', 4, 5.0, '二次函数', 'real', '2025绵阳中考', 'mianyang', 2025, 278),

(2019, '<p>历史：辛亥革命的历史意义包括（多选概念理解）</p>', 'choice',
 '["A. 推翻君主专制","B. 建立共和观念","C. 完成反帝任务","D. 促进思想解放"]', 'ABD',
 '<p>C 未完成反帝反封建彻底任务。</p>', 'junior', 'history', 3, 4.0, '辛亥革命', 'sync', '八年级历史', 'national', 2026, 92),

(2020, '<p>地理：成都平原农业发达的主要自然原因是（　）</p>', 'choice',
 '["A. 沙漠广布","B. 水热条件较好","C. 终年寒冷","D. 干旱少雨"]', 'B',
 '<p>亚热带季风气候。</p>', 'junior', 'geography', 2, 3.0, '农业区位', 'sync', '八年级地理', 'chengdu', 2026, 76)
ON DUPLICATE KEY UPDATE
  `stem` = VALUES(`stem`),
  `answer` = VALUES(`answer`),
  `usage_count` = VALUES(`usage_count`);
