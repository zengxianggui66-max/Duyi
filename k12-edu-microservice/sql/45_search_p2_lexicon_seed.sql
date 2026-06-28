-- P2 搜索词典扩展种子：同义词 / 意图规则 / 频道栏目类型映射 / 备课入口索引
-- 依赖：44_search_p2_lexicon.sql 已执行
-- mysql -u root -p xinketang < sql/45_search_p2_lexicon_seed.sql
USE xinketang;
SET NAMES utf8mb4;

-- ============================================================
-- 1. 同义词扩展（domain: stage/subject/grade/type/module/channel/news）
-- ============================================================
INSERT INTO sys_search_synonym (word, synonyms, domain, canonical) VALUES
-- 学段
('幼儿', '学前,preschool,幼儿园', 'stage', '幼儿'),
('美术', 'art,美术学段', 'stage', '美术'),
('舞蹈', 'dance,舞蹈学段', 'stage', '舞蹈'),
-- 学科
('物理', '物理学', 'subject', '物理'),
('化学', '化学科', 'subject', '化学'),
('生物', '生物学,生命科学', 'subject', '生物'),
('历史', '历史学', 'subject', '历史'),
('地理', '地理学', 'subject', '地理'),
('科学', '自然科学', 'subject', '科学'),
('音乐', '音乐课', 'subject', '音乐'),
('体育', '体育课,体能', 'subject', '体育'),
('道德与法治', '道法,品德,思政,道德法治', 'subject', '道德与法治'),
('政治', '道法,思想政治,道德与法治,思政', 'subject', '政治'),
('拼音识字', '拼音,识字,幼儿语文', 'subject', '拼音识字'),
-- 年级/册别
('二年级', '小二,小学二年级', 'grade', '二年级'),
('三年级', '小三,小学三年级', 'grade', '三年级'),
('四年级', '小四,小学四年级', 'grade', '四年级'),
('五年级', '小五,小学五年级', 'grade', '五年级'),
('六年级', '小六,小学六年级', 'grade', '六年级'),
('七年级', '初一,初中一年级', 'grade', '七年级'),
('八年级', '初二,初中二年级', 'grade', '八年级'),
('九年级', '初三,初中三年级', 'grade', '九年级'),
('必修一', '高一上,高中一年级上', 'grade', '必修一'),
('必修二', '高一下,高一下', 'grade', '必修二'),
('选择性必修一', '高二上,选择性必修上', 'grade', '选择性必修一'),
('选择性必修二', '高二下,选择性必修下', 'grade', '选择性必修二'),
-- 资源类型
('试卷', '试题,测试卷,练习卷,真题,模拟卷,卷子', 'type', '试卷'),
('练习', '习题,课时练习,课后练习', 'type', '练习'),
('学案', '导学案,学习方案', 'type', '学案'),
('素材', '图片素材,备课包,教学素材', 'type', '素材'),
('视频', '微课,课堂实录,示范课,录像', 'type', '视频'),
('音频', '音频朗读,朗读,mp3', 'type', '音频'),
('电子课本', '电子教材,电子书', 'type', '电子课本'),
('说课稿', '说课', 'type', '说课稿'),
('公开课', '优质课,示范课', 'type', '公开课'),
('教学反思', '反思', 'type', '教学反思'),
('教学总结', '总结', 'type', '教学总结'),
('讲义', '授课讲义', 'type', '讲义'),
('组卷', '出题,智能组卷', 'type', '组卷'),
-- 栏目
('期中', '期中复习,期中专区,期中试卷,期中考试', 'module', '期中'),
('期末', '期末复习,期末专区,期末试卷,期末考试', 'module', '期末'),
('月考', '月考试卷,阶段检测', 'module', '月考'),
('开学专区', '开学,开学季,开学备考', 'module', '开学专区'),
('暑假', '暑假专区,暑假作业,暑期', 'module', '暑假'),
('寒假', '寒假专区,寒假作业', 'module', '寒假'),
('作文', '作文专区,习作,写作', 'module', '作文'),
('阅读', '阅读理解,课外阅读', 'module', '阅读'),
('专题复习', '专题,复习专题', 'module', '专题复习'),
('中考', '中考真题,中考模拟,中考复习,中考卷', 'module', '中考真题'),
('高考', '高考真题,高考模拟,高考复习,高考卷', 'module', '高考真题'),
('小升初', '小升初真题,小升初模拟,升学', 'module', '小升初真题'),
('真题汇编', '真题,历年真题', 'module', '真题汇编'),
('竞赛', '学科竞赛,奥赛', 'module', '竞赛'),
('国学阅读', '国学,经典诵读', 'module', '国学阅读'),
('期中复习', '期中,期中专区', 'module', '期中复习'),
('期末复习', '期末,期末专区', 'module', '期末复习'),
-- 特色频道
('主题班会', '班会,德育,班主任,班会课', 'channel', '主题班会'),
('生涯规划', '职业规划,选科,志愿填报,升学指导,选课指导', 'channel', '生涯规划'),
('传统文化', '国学,诗词,书法,民俗,巴蜀研学', 'channel', '传统文化'),
('竞赛专区', '奥赛,学科竞赛,比赛,华杯赛,希望杯', 'channel', '竞赛专区'),
('专题资源', '专题,备考,冲刺,专题页', 'channel', '专题资源'),
('爱国教育', '爱国主义,革命传统,勿忘国耻,学雷锋', 'channel', '爱国教育'),
('安全教育', '校园安全,交通安全,防溺水,网络安全,消防', 'channel', '安全教育'),
('心理健康', '心理成长,情绪管理,考试减压,心理课', 'channel', '心理健康'),
('家长会', '家长沟通,学期汇报,考试分析会,家长学校', 'channel', '家长会'),
('备课中心', '备课,备课专区,同步资源', 'channel', '备课中心'),
('智能备课', 'AI备课,AI辅助备课', 'channel', '智能备课'),
('资料篮', '资源篮,收藏篮', 'channel', '资料篮'),
-- 资讯
('教育政策', '政策文件,政策解读,招生政策', 'news', '教育政策'),
('教研动态', '教研活动,课题,教研', 'news', '教研动态'),
('升学备考', '考试政策,升学资讯,中高考政策', 'news', '升学备考'),
('名师讲堂', '名师,课例', 'news', '名师讲堂'),
('教学改革', '新课标,课堂改革', 'news', '教学改革'),
('区域动态', '地方动态,片区,成都,绵阳', 'news', '区域动态'),
('学校通知', '校内通知,校务', 'news', '学校通知'),
('教育局通知', '教育局,公告,政务通知', 'news', '教育局通知')
ON DUPLICATE KEY UPDATE synonyms=VALUES(synonyms), canonical=VALUES(canonical), status=1;

-- ============================================================
-- 2. 意图规则扩展（pattern + intent_type 唯一）
-- ============================================================
INSERT INTO sys_search_intent_rule (pattern, intent_type, target_key, target_value, target_payload, priority) VALUES
-- 学段
('幼儿', 'stage', 'preschool', '幼儿', NULL, 100),
('学前', 'stage', 'preschool', '幼儿', NULL, 98),
('美术', 'stage', 'art', '美术', JSON_OBJECT('route_path', '/subject/art/art/tongbian2024'), 95),
('舞蹈', 'stage', 'dance', '舞蹈', JSON_OBJECT('route_path', '/subject/dance/dance/tongbian2024'), 95),
-- 学科
('物理', 'subject', 'physics', '物理', NULL, 88),
('化学', 'subject', 'chemistry', '化学', NULL, 88),
('生物', 'subject', 'biology', '生物', NULL, 88),
('历史', 'subject', 'history', '历史', NULL, 88),
('地理', 'subject', 'geography', '地理', NULL, 88),
('科学', 'subject', 'science', '科学', NULL, 88),
('音乐', 'subject', 'music', '音乐', NULL, 86),
('体育', 'subject', 'pe', '体育', NULL, 86),
('道德与法治', 'subject', 'politics', '道德与法治', NULL, 90),
('道法', 'subject', 'politics', '政治', NULL, 92),
('政治', 'subject', 'politics', '政治', NULL, 88),
('奥数', 'subject', 'math', '数学', NULL, 82),
('拼音', 'subject', 'chinese', '拼音识字', JSON_OBJECT('stage', 'preschool'), 80),
-- 年级
('二年级', 'grade', 'grade_2', '二年级', NULL, 88),
('三年级', 'grade', 'grade_3', '三年级', NULL, 88),
('四年级', 'grade', 'grade_4', '四年级', NULL, 88),
('五年级', 'grade', 'grade_5', '五年级', NULL, 88),
('六年级', 'grade', 'grade_6', '六年级', NULL, 88),
('高一', 'grade', 'grade_10', '高一', NULL, 90),
('高二', 'grade', 'grade_11', '高二', NULL, 90),
('高三', 'grade', 'grade_12', '高三', NULL, 90),
('必修一', 'grade', 'grade_10', '必修一', JSON_OBJECT('volume_id', 's10s1'), 85),
('必修二', 'grade', 'grade_10', '必修二', JSON_OBJECT('volume_id', 's10s2'), 85),
('选择性必修一', 'grade', 'grade_11', '选择性必修一', JSON_OBJECT('volume_id', 's11s1'), 85),
('选择性必修二', 'grade', 'grade_11', '选择性必修二', JSON_OBJECT('volume_id', 's11s2'), 85),
-- 资源类型
('学案', 'type', 'study_guide', '学案', NULL, 78),
('练习', 'type', 'practice', '练习', NULL, 78),
('素材', 'type', 'material', '素材', NULL, 76),
('视频', 'type', 'video', '视频', NULL, 78),
('音频', 'type', 'audio_read', '音频朗读', NULL, 76),
('电子课本', 'type', 'e_textbook', '电子课本', NULL, 78),
('说课稿', 'type', 'lecture_script', '说课稿', NULL, 76),
('公开课', 'type', 'open_class', '公开课', NULL, 76),
('教学反思', 'type', 'teaching_reflect', '教学反思', NULL, 74),
('教学总结', 'type', 'teaching_summary', '教学总结', NULL, 74),
('讲义', 'type', 'handout', '讲义', NULL, 74),
('试题', 'type', 'exam_paper', '试卷', NULL, 79),
('真题', 'type', 'exam_paper', '试卷', NULL, 77),
('模拟卷', 'type', 'exam_paper', '试卷', NULL, 75),
('PPT', 'type', 'courseware', '课件', NULL, 76),
('幻灯片', 'type', 'courseware', '课件', NULL, 74),
-- 栏目 module
('期中', 'module', 'midterm', '期中', JSON_OBJECT('module_name', '期中'), 72),
('期中复习', 'module', 'mid_review_pack', '期中复习', JSON_OBJECT('module_name', '期中复习', 'stage', 'primary'), 73),
('期末', 'module', 'final', '期末', JSON_OBJECT('module_name', '期末'), 72),
('期末复习', 'module', 'final_review_pack', '期末复习', JSON_OBJECT('module_name', '期末复习', 'stage', 'primary'), 73),
('月考', 'module', 'monthly', '月考', JSON_OBJECT('module_name', '月考'), 70),
('开学', 'module', 'school_open', '开学专区', JSON_OBJECT('module_name', '开学专区'), 68),
('暑假', 'module', 'summer', '暑假', JSON_OBJECT('module_name', '暑假'), 68),
('寒假', 'module', 'winter', '寒假', JSON_OBJECT('module_name', '寒假'), 68),
('作文', 'module', 'composition', '作文', JSON_OBJECT('module_name', '作文'), 68),
('阅读', 'module', 'reading', '阅读', JSON_OBJECT('module_name', '阅读'), 68),
('专题复习', 'module', 'topic_review', '专题复习', JSON_OBJECT('module_name', '专题复习'), 70),
('中考真题', 'module', 'zk_real', '中考真题', JSON_OBJECT('module_name', '中考真题', 'stage', 'junior'), 75),
('中考复习', 'module', 'zk_real', '中考真题', JSON_OBJECT('module_name', '中考真题', 'stage', 'junior'), 74),
('中考模拟', 'module', 'zk_mock', '中考模拟', JSON_OBJECT('module_name', '中考模拟'), 73),
('高考真题', 'module', 'gk_real', '高考真题', JSON_OBJECT('module_name', '高考真题', 'stage', 'senior'), 75),
('高考复习', 'module', 'gk_real', '高考真题', JSON_OBJECT('module_name', '高考真题', 'stage', 'senior'), 74),
('高考模拟', 'module', 'gk_mock', '高考模拟', JSON_OBJECT('module_name', '高考模拟'), 73),
('小升初', 'module', 'xsc_real', '小升初真题', JSON_OBJECT('module_name', '小升初真题', 'stage', 'primary'), 75),
('小升初真题', 'module', 'xsc_real', '小升初真题', JSON_OBJECT('module_name', '小升初真题'), 76),
('真题汇编', 'module', 'real_collection', '真题汇编', JSON_OBJECT('module_name', '真题汇编'), 72),
('竞赛', 'module', 'competition', '竞赛', JSON_OBJECT('module_name', '竞赛'), 70),
('国学阅读', 'module', 'guoxue_reading', '国学阅读', JSON_OBJECT('module_name', '国学阅读', 'stage', 'primary'), 70),
('一轮复习', 'module', 'round1', '一轮复习', NULL, 68),
('二轮专题', 'module', 'round2', '二轮专题', NULL, 68),
('三轮冲刺', 'module', 'round3', '三轮冲刺', NULL, 68),
-- 特色频道 channel
('传统文化', 'channel', 'culture', '传统文化', JSON_OBJECT('route_path', '/culture', 'contentDomain', 'feature'), 94),
('国学', 'channel', 'culture', '传统文化', JSON_OBJECT('route_path', '/culture'), 88),
('竞赛专区', 'channel', 'competition', '竞赛专区', JSON_OBJECT('route_path', '/competition', 'contentDomain', 'feature'), 94),
('奥赛', 'channel', 'competition', '竞赛专区', JSON_OBJECT('route_path', '/competition'), 86),
('专题资源', 'channel', 'topic', '专题资源', JSON_OBJECT('route_path', '/topic', 'contentDomain', 'feature'), 92),
('生涯规划', 'channel', 'career', '生涯规划', JSON_OBJECT('route_path', '/topic', 'contentDomain', 'feature'), 93),
('职业规划', 'channel', 'career', '生涯规划', JSON_OBJECT('route_path', '/topic'), 88),
('选科', 'channel', 'career', '生涯规划', JSON_OBJECT('route_path', '/topic'), 85),
('志愿填报', 'channel', 'career', '生涯规划', JSON_OBJECT('route_path', '/topic'), 85),
('爱国教育', 'channel', 'patriotic', '爱国教育', JSON_OBJECT('route_path', '/theme-class-meeting', 'category', '爱国教育'), 88),
('爱国主义', 'channel', 'patriotic', '爱国教育', JSON_OBJECT('route_path', '/theme-class-meeting'), 86),
('安全教育', 'channel', 'safety', '安全教育', JSON_OBJECT('route_path', '/theme-class-meeting', 'category', '安全教育'), 88),
('心理健康', 'channel', 'mental', '心理健康', JSON_OBJECT('route_path', '/theme-class-meeting', 'category', '心理健康'), 88),
('家长会', 'channel', 'parent_meeting', '家长会', JSON_OBJECT('route_path', '/theme-class-meeting', 'category', '更多主题'), 86),
('班会', 'channel', 'class_meeting', '主题班会', JSON_OBJECT('route_path', '/theme-class-meeting'), 90),
('德育', 'channel', 'class_meeting', '主题班会', JSON_OBJECT('route_path', '/theme-class-meeting'), 85),
-- 备课专区 channel/prep
('备课', 'channel', 'prep', '备课中心', JSON_OBJECT('route_path', '/lesson', 'contentDomain', 'prep'), 88),
('备课中心', 'channel', 'prep', '备课中心', JSON_OBJECT('route_path', '/lesson'), 90),
('智能备课', 'channel', 'prep', '智能备课', JSON_OBJECT('route_path', '/lesson/smart', 'contentDomain', 'prep'), 88),
('组卷', 'channel', 'prep', '组卷', JSON_OBJECT('route_path', '/lesson/assemble', 'contentDomain', 'prep'), 86),
('资料篮', 'channel', 'prep', '资料篮', JSON_OBJECT('route_path', '/lesson/basket', 'contentDomain', 'prep'), 84),
('试卷列表', 'channel', 'prep', '试卷列表', JSON_OBJECT('route_path', '/lesson/papers', 'contentDomain', 'prep'), 82),
-- 教育资讯 news
('教育政策', 'news', 'policy', '教育政策', JSON_OBJECT('route_path', '/news/channel/policy'), 86),
('政策文件', 'news', 'policy', '教育政策', JSON_OBJECT('route_path', '/news/channel/policy'), 84),
('教研动态', 'news', 'research', '教研动态', JSON_OBJECT('route_path', '/news/channel/research'), 84),
('教研活动', 'news', 'research', '教研动态', JSON_OBJECT('route_path', '/news/channel/research'), 83),
('名师讲堂', 'news', 'teacher', '名师讲堂', JSON_OBJECT('route_path', '/news/channel/teacher'), 82),
('升学备考', 'news', 'exam', '升学备考', JSON_OBJECT('route_path', '/news/channel/exam'), 84),
('考试政策', 'news', 'exam', '升学备考', JSON_OBJECT('route_path', '/news/channel/exam'), 83),
('教学改革', 'news', 'reform', '教学改革', JSON_OBJECT('route_path', '/news/channel/reform'), 82),
('区域动态', 'news', 'region', '区域动态', JSON_OBJECT('route_path', '/news/list?keyword=区域'), 80),
('学校通知', 'news', 'school_notice', '学校通知', JSON_OBJECT('route_path', '/news/list?keyword=学校通知'), 80),
('新闻', 'news', 'news', '教育资讯', JSON_OBJECT('route_path', '/news'), 78),
('公告', 'news', 'notice', '教育局通知', JSON_OBJECT('route_path', '/news/list?keyword=通知'), 79),
('帮助中心', 'channel', 'help', '帮助中心', JSON_OBJECT('route_path', '/help'), 70)
ON DUPLICATE KEY UPDATE
  target_key=VALUES(target_key),
  target_value=VALUES(target_value),
  target_payload=VALUES(target_payload),
  priority=VALUES(priority),
  status=1;

-- ============================================================
-- 3. 补全搜索索引：备课专区缺失入口
-- ============================================================
INSERT INTO sys_search_document (
  doc_id, doc_type, biz_id, title, subtitle, summary, content_text, keyword_text,
  channel_key, channel_name, route_path, status, is_deleted, quality_score, hot_score, publish_time
) VALUES
('prep:papers', 'prep', 'papers', '试卷列表', '备课专区 / 试卷管理', '备课专区试卷列表入口', '备课专区试卷列表入口', '试卷,组卷,试卷列表,备课', 'prep', '备课专区', '/lesson/papers', 1, 0, 2, 9, NOW()),
('prep:basket', 'prep', 'basket', '资料篮', '备课专区 / 资源篮', '备课资料篮入口', '备课资料篮入口', '资料篮,资源篮,收藏,备课', 'prep', '备课专区', '/lesson/basket', 1, 0, 2, 8, NOW()),
('news:region', 'page', 'region', '区域动态', '教育资讯 / 区域', '区域教育动态栏目', '区域教育动态栏目', '区域,成都,绵阳,地方动态', 'news', '教育资讯', '/news/list?keyword=区域', 1, 0, 2, 7, NOW()),
('news:school_notice', 'page', 'school_notice', '学校通知', '教育资讯 / 学校', '学校通知栏目', '学校通知栏目', '学校,校内,校务,通知', 'news', '教育资讯', '/news/list?keyword=学校通知', 1, 0, 2, 7, NOW()),
('channel:patriotic', 'channel', 'patriotic', '爱国教育', '特色频道 / 爱国', '爱国教育主题班会入口', '爱国教育主题班会入口', '爱国,爱国主义,革命传统', 'class_meeting', '特色频道', '/theme-class-meeting', 1, 0, 2, 8, NOW()),
('channel:safety', 'channel', 'safety', '安全教育', '特色频道 / 安全', '安全教育主题班会入口', '安全教育主题班会入口', '安全,校园安全,交通安全,防溺水', 'class_meeting', '特色频道', '/theme-class-meeting', 1, 0, 2, 8, NOW()),
('channel:mental', 'channel', 'mental', '心理健康', '特色频道 / 心理', '心理健康主题班会入口', '心理健康主题班会入口', '心理,情绪,减压,成长', 'class_meeting', '特色频道', '/theme-class-meeting', 1, 0, 2, 8, NOW()),
('channel:parent_meeting', 'channel', 'parent_meeting', '家长会', '特色频道 / 家长', '家长会资源入口', '家长会资源入口', '家长会,家长沟通,学期汇报', 'class_meeting', '特色频道', '/theme-class-meeting', 1, 0, 2, 7, NOW())
ON DUPLICATE KEY UPDATE
  title=VALUES(title), subtitle=VALUES(subtitle), summary=VALUES(summary),
  content_text=VALUES(content_text), keyword_text=VALUES(keyword_text),
  route_path=VALUES(route_path), channel_key=VALUES(channel_key), channel_name=VALUES(channel_name),
  hot_score=VALUES(hot_score), status=1, is_deleted=0;

-- ============================================================
-- 4. 运营热词补充（search_hot_keyword）
-- ============================================================
INSERT INTO search_hot_keyword (keyword, search_count, status, deleted) VALUES
('期中试卷', 520, 1, 0),
('一年级语文', 480, 1, 0),
('中考复习', 450, 1, 0),
('高考真题', 420, 1, 0),
('安全教育', 380, 1, 0),
('教案模板', 360, 1, 0)
ON DUPLICATE KEY UPDATE search_count=GREATEST(search_count, VALUES(search_count)), status=1, deleted=0;

-- ============================================================
-- 5. 执行结果
-- ============================================================
SELECT 'synonym_count' AS k, COUNT(*) AS v FROM sys_search_synonym WHERE status=1
UNION ALL SELECT 'intent_rule_count', COUNT(*) FROM sys_search_intent_rule WHERE status=1
UNION ALL SELECT 'search_doc_prep_channel', COUNT(*) FROM sys_search_document WHERE doc_type IN ('prep','channel','page') AND status=1 AND is_deleted=0
UNION ALL SELECT 'hot_keyword_count', COUNT(*) FROM search_hot_keyword WHERE status=1 AND deleted=0;
