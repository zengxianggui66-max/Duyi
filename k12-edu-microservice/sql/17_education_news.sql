-- ============================================================
-- 教育资讯扩展 + 咨询线索 + 种子数据
-- mysql -u root -p xinketang < sql/17_education_news.sql
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

-- article 表扩展（列已存在则自动跳过，可重复执行）
DROP PROCEDURE IF EXISTS `sp_add_column_if_missing`;
DELIMITER $$
CREATE PROCEDURE `sp_add_column_if_missing`(
  IN p_table VARCHAR(64),
  IN p_column VARCHAR(64),
  IN p_definition TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = p_table
      AND COLUMN_NAME = p_column
  ) THEN
    SET @ddl = CONCAT(
      'ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_definition
    );
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;

CALL sp_add_column_if_missing('article', 'sub_category',
  'VARCHAR(50) DEFAULT NULL COMMENT ''二级分类'' AFTER `category`');
CALL sp_add_column_if_missing('article', 'grade_levels',
  'VARCHAR(80) DEFAULT ''all'' COMMENT ''适用学段，逗号分隔'' AFTER `tags`');
CALL sp_add_column_if_missing('article', 'regions',
  'VARCHAR(80) DEFAULT ''national'' COMMENT ''地区'' AFTER `grade_levels`');
CALL sp_add_column_if_missing('article', 'source_name',
  'VARCHAR(100) DEFAULT NULL COMMENT ''来源名称'' AFTER `regions`');
CALL sp_add_column_if_missing('article', 'source_url',
  'VARCHAR(500) DEFAULT NULL COMMENT ''原文链接'' AFTER `source_name`');
CALL sp_add_column_if_missing('article', 'is_top',
  'TINYINT DEFAULT 0 COMMENT ''是否头条'' AFTER `source_url`');
CALL sp_add_column_if_missing('article', 'top_order',
  'INT DEFAULT 0 COMMENT ''头条排序'' AFTER `is_top`');
CALL sp_add_column_if_missing('article', 'is_featured',
  'TINYINT DEFAULT 0 COMMENT ''精选'' AFTER `top_order`');
CALL sp_add_column_if_missing('article', 'publish_time',
  'DATETIME DEFAULT NULL COMMENT ''发布时间'' AFTER `is_featured`');
CALL sp_add_column_if_missing('article', 'policy_points',
  'TEXT DEFAULT NULL COMMENT ''政策要点JSON数组'' AFTER `publish_time`');
CALL sp_add_column_if_missing('article', 'related_topic_keywords',
  'VARCHAR(200) DEFAULT NULL COMMENT ''关联专题关键词'' AFTER `policy_points`');
CALL sp_add_column_if_missing('article', 'consult_enabled',
  'TINYINT DEFAULT 1 COMMENT ''是否展示咨询'' AFTER `related_topic_keywords`');
CALL sp_add_column_if_missing('article', 'content_type',
  'VARCHAR(20) DEFAULT ''article'' COMMENT ''article|video'' AFTER `consult_enabled`');

DROP PROCEDURE IF EXISTS `sp_add_column_if_missing`;

CREATE TABLE IF NOT EXISTS `article_collect` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `article_id` BIGINT NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
  KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资讯收藏';

CREATE TABLE IF NOT EXISTS `consult_lead` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `article_id` BIGINT DEFAULT NULL COMMENT '来源文章',
  `user_id` BIGINT DEFAULT NULL COMMENT '登录用户',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `phone` VARCHAR(20) NOT NULL COMMENT '手机',
  `grade` VARCHAR(30) DEFAULT NULL COMMENT '年级/学段',
  `intent_type` VARCHAR(30) DEFAULT 'general' COMMENT 'trial|material|promotion|general',
  `remark` VARCHAR(500) DEFAULT NULL,
  `status` TINYINT DEFAULT 0 COMMENT '0待跟进 1已联系 2已转化',
  `source_page` VARCHAR(100) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_phone` (`phone`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教培咨询线索';

-- 清空旧种子（可选，仅开发环境）
-- DELETE FROM article WHERE id BETWEEN 1001 AND 1030;

INSERT INTO `article` (
  `id`, `title`, `summary`, `content`, `cover_url`, `category`, `category_name`,
  `author`, `view_count`, `comment_count`, `like_count`, `tags`,
  `status`, `sub_category`, `grade_levels`, `regions`, `source_name`,
  `is_top`, `top_order`, `is_featured`, `publish_time`, `policy_points`,
  `related_topic_keywords`, `consult_enabled`, `content_type`
) VALUES
(1001, '四川省2026年义务教育阶段招生入学工作实施意见解读',
 '明确免试就近入学、公办民办同步招生、保障随迁子女等核心要求，成都绵阳同步细化执行。',
 '<p>省教育厅近日印发2026年义务教育招生入学实施意见，强调坚持免试就近入学，规范民办义务教育学校招生行为。</p><p>成都、绵阳等地将结合本地学位情况制定实施细则，家长需关注户籍与房产一致性要求。</p>',
 '', 'policy', '教育政策', '政策研究室', 12800, 0, 156,
 '招生,义务教育,四川', 1, 'enrollment', 'all', 'sichuan', '四川省教育厅',
 1, 1, 1, '2026-05-10 09:00:00',
 '["坚持免试就近入学，严禁以各类考试选拔学生","公办民办同步招生，报名人数超计划实行电脑摇号","保障随迁子女入学，以居住证为主要依据","成都绵阳将发布本地实施细则"]',
 '招生入学', 1, 'article'),

(1002, '教育部：深化基础教育课程教学改革试点工作',
 '聚焦核心素养导向、跨学科主题学习、数字化赋能教学等方向，推动课堂变革。',
 '<p>教育部基础教育司表示，将继续扩大课程教学改革试点，鼓励学校开展项目式学习与大单元教学设计。</p>',
 '', 'policy', '教育政策', '教育部资讯组', 8650, 0, 98,
 '新课标,教学改革', 1, 'curriculum', 'all', 'national', '教育部',
 1, 2, 1, '2026-05-08 10:00:00',
 '["扩大课程教学改革试点范围","强化核心素养与跨学科主题学习","推进数字化教学场景应用","校本教研与教师培训同步加强"]',
 '跨学科,PBL', 1, 'article'),

(1003, '成都市2026年中考体育考试政策调整说明',
 '考试项目、分值构成与免考缓考政策有所优化，考生需提前规划训练。',
 '<p>成都市教育局发布2026年中考体育考试方案，总分仍为70分，项目设置兼顾力量、速度与技能。</p>',
 '', 'policy', '教育政策', '成都招考', 15200, 0, 210,
 '成都,中考,体育', 1, 'exam_policy', 'middle', 'chengdu', '成都市教育局',
 1, 3, 1, '2026-05-06 14:00:00',
 '["总分70分，分必考与选考项目","免考缓考须按规定提交材料","初二初三需提早训练规划","与升学录取政策配套实施"]',
 '成都中考,升学冲刺', 1, 'article'),

(1004, '「双减」背景下课后服务提质增效指导意见',
 '鼓励开设体育艺术、科技创新、阅读劳动等课程，严禁将课后服务变相为集体补课。',
 '<p>文件要求课后服务应遵循学生自愿原则，丰富课程供给，加强师资与经费保障。</p>',
 '', 'policy', '教育政策', '政策解读组', 6200, 0, 76,
 '双减,课后服务', 1, 'after_school', 'all', 'national', '教育部',
 0, 0, 1, '2026-05-04 11:00:00', NULL, '开学备考', 1, 'article'),

(1005, '新高考改革：选科组合与专业规划衔接要点',
 '物化生、史政地等组合与高校专业限科对应关系梳理，助力高一选科。',
 '<p>专家提示，选科应结合兴趣、优势学科与目标专业要求，避免盲目跟风。</p>',
 '', 'reform', '教学改革', '生涯教研组', 9800, 0, 132,
 '新高考,选科', 1, 'gaokao_reform', 'high', 'national', '教学网',
 0, 0, 1, '2026-05-12 08:00:00', NULL, '高考选科,生涯规划', 1, 'article'),

(1006, '大单元教学设计与实施：初中语文实践案例',
 '以「思辨性阅读」为主题，展示目标—任务—评价一体化设计路径。',
 '<p>教研员分享单元整体教学设计框架，强调真实情境任务与形成性评价。</p>',
 '', 'reform', '教学改革', '王敏名师工作室', 5400, 0, 88,
 '大单元,语文', 1, 'unit_design', 'middle', 'sichuan', '教研室',
 0, 0, 1, '2026-05-11 15:00:00', NULL, '期中复习', 1, 'article'),

(1007, '小学数学「数感培养」课堂观察与改进',
 '通过课堂观察量表，提炼低段数学游戏化、情境化教学策略。',
 '<p>本期教研聚焦一年级加减法启蒙，探讨操作活动与口头表达的融合。</p>',
 '', 'research', '教研动态', '成都市小数专委会', 4100, 0, 45,
 '数感,小学数学', 1, 'math', 'primary', 'chengdu', '成都教研',
 0, 0, 1, '2026-05-09 16:00:00', NULL, '小学数学', 1, 'article'),

(1008, '2026年绵阳市初中英语听说能力教研活动纪要',
 '聚焦中考英语听说测试题型，分享听说课型设计与分层训练方案。',
 '<p>活动汇集全市骨干英语教师，展示听说课例与测评数据反馈。</p>',
 '', 'research', '教研动态', '绵阳教科所', 3800, 0, 52,
 '英语,听说,绵阳', 1, 'english', 'middle', 'mianyang', '绵阳教科所',
 0, 0, 1, '2026-05-07 13:00:00', NULL, '绵阳中考', 1, 'article'),

(1009, '省级课题申报：数字化赋能乡村学校教研',
 '课题指南发布，鼓励城乡结对、在线联合教研与资源共建共享。',
 '<p>申报截止时间为6月底，重点支持乡村小规模学校教研组织创新。</p>',
 '', 'research', '教研动态', '省教科院', 2900, 0, 31,
 '课题,数字化', 1, 'project', 'all', 'sichuan', '省教科院',
 0, 0, 0, '2026-05-05 10:00:00', NULL, NULL, 0, 'article'),

(1010, '特级教师李华：中考语文阅读满分策略讲座',
 '三场线下巡讲覆盖成都锦江、青羊、高新，分享记叙文与议论文阅读技巧。',
 '<p>讲座结合近三年成都中考真题，拆解审题、定位、概括、赏析四类核心能力。</p>',
 '', 'teacher', '名师讲堂', '李华名师工作室', 11200, 0, 198,
 '名师,语文,中考', 1, 'lecture', 'middle', 'chengdu', '名师讲堂',
 0, 0, 1, '2026-05-13 09:00:00', NULL, '成都中考,期中复习', 1, 'article'),

(1011, '全国优秀教师张伟：高中物理竞赛入门指导',
 '从兴趣激发到实验探究，帮助学有余力学生规划竞赛学习路径。',
 '<p>视频课程已上线平台名师专栏，含力学入门专题与实验设计案例。</p>',
 '', 'teacher', '名师讲堂', '张伟', 7600, 0, 145,
 '物理,竞赛,名师', 1, 'olympiad', 'high', 'national', '学科网',
 0, 0, 1, '2026-05-10 18:00:00', NULL, '物理竞赛', 1, 'video'),

(1012, '名师面对面：小学班主任班级管理沙龙',
 '分享家校沟通、习惯养成、情绪引导等实用策略，适合1-6年级班主任。',
 '<p>沙龙采用案例研讨形式，提供可复制的班级公约与激励机制模板。</p>',
 '', 'teacher', '名师讲堂', '班主任联盟', 4500, 0, 67,
 '班主任,班级管理', 1, 'class_mgmt', 'primary', 'chengdu', '班主任联盟',
 0, 0, 0, '2026-05-08 19:00:00', NULL, '主题班会', 1, 'article'),

(1013, '2026成都中考倒计时60天：三轮复习规划建议',
 '一轮夯实基础、二轮专题突破、三轮模拟冲刺，附周计划表模板。',
 '<p>教研团队建议合理分配语文、数学、英语、物理、化学复习时间，重视错题复盘。</p>',
 '', 'exam', '升学备考', '成都备考研究中心', 18600, 0, 320,
 '成都中考,冲刺', 1, 'zhongkao', 'middle', 'chengdu', '备考中心',
 1, 4, 1, '2026-05-14 08:00:00', NULL, '升学冲刺,成都', 1, 'article'),

(1014, '绵阳中考政策与志愿填报指南（2026版）',
 '批次设置、划线规则、指标到校与平行志愿填报注意事项一文读懂。',
 '<p>建议考生家长结合一模二模成绩与位次，合理拉开志愿梯度。</p>',
 '', 'exam', '升学备考', '绵阳招考', 12400, 0, 186,
 '绵阳,中考,志愿', 1, 'volunteer', 'middle', 'mianyang', '绵阳招考',
 0, 0, 1, '2026-05-12 14:00:00', NULL, '绵阳中考,升学冲刺', 1, 'article'),

(1015, '高考冲刺最后30天：心理调适与复习节奏',
 '心理老师支招缓解焦虑，建议保持规律作息与适度运动。',
 '<p>家长应避免过度施压，关注孩子情绪变化，必要时寻求专业辅导。</p>',
 '', 'exam', '升学备考', '心理健康中心', 9800, 0, 124,
 '高考,心理', 1, 'gaokao', 'high', 'national', '教学网',
 0, 0, 1, '2026-05-11 10:00:00', NULL, '高考冲刺', 1, 'article'),

(1016, '小升初衔接：暑假如何平稳过渡初中学习',
 '学科差异、学习方法与时间管理三方面给出可操作建议。',
 '<p>建议暑假完成小初衔接专题练习，培养预习与笔记习惯。</p>',
 '', 'exam', '升学备考', '升学指导组', 6700, 0, 89,
 '小升初,暑假', 1, 'xsc', 'primary', 'chengdu', '升学指导',
 0, 0, 1, '2026-05-09 11:00:00', NULL, '暑假作业,开学备考', 1, 'article'),

(1017, '期中考后诊断：如何制定个性化补弱计划',
 '依据试卷分析找准知识漏洞，区分知识性错误与习惯性失误。',
 '<p>教研员建议建立错题本分类标签，每周固定时间专项突破。</p>',
 '', 'exam', '升学备考', '教研员专栏', 5200, 0, 61,
 '期中,复习', 1, 'midterm', 'all', 'sichuan', '教研室',
 0, 0, 0, '2026-05-06 09:00:00', NULL, '期中复习', 1, 'article'),

(1018, '四川省职业教育贯通培养政策问答',
 '中职与本科贯通、高职扩招等热点问题权威解答。',
 '<p>适合关注职业教育路径的家长与学生阅读参考。</p>',
 '', 'policy', '教育政策', '省招考院', 3400, 0, 28,
 '职业教育', 1, 'vocational', 'high', 'sichuan', '省招考院',
 0, 0, 0, '2026-05-03 15:00:00',
 '["贯通培养需关注对口高校专业","中职升高职渠道进一步拓宽","技能证书与学业评价并重"]',
 NULL, 0, 'article'),

(1019, '项目式学习（PBL）优秀案例：校园节水行动',
 '跨学科融合科学、数学、语文与劳动，形成可展示的成果报告。',
 '<p>案例来自成都某小学五年级，获市级教研成果一等奖。</p>',
 '', 'research', '教研动态', 'PBL联盟', 3600, 0, 42,
 'PBL,跨学科', 1, 'pbl', 'primary', 'chengdu', '教研成果',
 0, 0, 1, '2026-05-04 16:00:00', NULL, '项目式学习', 1, 'article'),

(1020, '名师讲堂回放：化学实验安全与中考必考实验',
 '涵盖气体制备、金属活动性、酸碱盐等高频实验操作规范。',
 '<p>适合初三学生与化学教师观看，含实验视频与评分要点。</p>',
 '', 'teacher', '名师讲堂', '陈老师化学', 5800, 0, 95,
 '化学,实验,中考', 1, 'chemistry', 'middle', 'national', '名师讲堂',
 0, 0, 0, '2026-05-07 20:00:00', NULL, '化学竞赛,期中复习', 1, 'video')
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `summary` = VALUES(`summary`),
  `content` = VALUES(`content`),
  `category` = VALUES(`category`),
  `category_name` = VALUES(`category_name`),
  `publish_time` = VALUES(`publish_time`),
  `policy_points` = VALUES(`policy_points`),
  `is_top` = VALUES(`is_top`),
  `top_order` = VALUES(`top_order`);
