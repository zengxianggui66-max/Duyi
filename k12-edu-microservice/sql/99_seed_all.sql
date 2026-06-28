-- ============================================================
-- 新课堂教育 — 99 全量种子数据
-- 依赖：00 → 02 → 03 → 04 → 05 已执行
-- 含：6学段、全学科、9版本、栏目、资源类型树、场景、目录样例
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==================== 1. 学段 ====================
INSERT INTO `edu_stage` (`id`, `code`, `name`, `icon`, `sort`, `status`) VALUES
(1, 'primary', '小学', '🏫', 1, 1),
(2, 'junior',  '初中', '📚', 2, 1),
(3, 'senior',  '高中', '🎓', 3, 1),
(4, 'art',     '美术', '🎨', 4, 1),
(5, 'dance',   '舞蹈', '💃', 5, 1),
(6, 'preschool', '幼儿', '🌱', 6, 1);

-- ==================== 2. 学期 / 册别 ====================
INSERT INTO `edu_semester` (`id`, `code`, `name`, `sort`) VALUES
(1, 'first',  '上学期', 1),
(2, 'second', '下学期', 2);

INSERT INTO `edu_volume` (`id`, `code`, `name`, `sort`) VALUES
(1, 'up',   '上册', 1),
(2, 'down', '下册', 2),
(3, 'full', '全册', 3);

-- ==================== 3. 教材版本（9个，与 subjectConfig 一致） ====================
INSERT INTO `edu_edition` (`id`, `code`, `name`, `short_name`, `publisher`, `year_label`, `sort`, `status`) VALUES
(1, 'tongbian2024', '统编版(2024)', '统编',   '人民教育出版社', '2024', 1, 1),
(2, 'renjiao',      '人教版',         '人教',   '人民教育出版社', NULL,   2, 1),
(3, 'beishida',     '北师大版',       '北师大', '北京师范大学出版社', NULL, 3, 1),
(4, 'sujiao',       '苏教版',         '苏教',   '江苏教育出版社', NULL,   4, 1),
(5, 'hujiao',       '沪教版',         '沪教',   '上海教育出版社', NULL,   5, 1),
(6, 'xishida',      '西师大版',       '西师大', '西南师范大学出版社', NULL, 6, 1),
(7, 'yuwen',        '语文版',         '语文版', NULL, NULL, 7, 1),
(8, 'jijiao',       '冀教版',         '冀教',   '河北教育出版社', NULL,   8, 1),
(9, 'tongbian2016', '统编版(2016)',   '统编16', '人民教育出版社', '2016', 9, 1);

-- ==================== 4. 学科 ====================
INSERT INTO `edu_subject` (`id`, `stage_id`, `code`, `name`, `icon`, `sort`, `status`) VALUES
(1,  1, 'chinese',  '语文',       '📖', 1, 1),
(2,  1, 'math',     '数学',       '🔢', 2, 1),
(3,  1, 'english',  '英语',       '🌍', 3, 1),
(4,  1, 'science',  '科学',       '🔬', 4, 1),
(5,  1, 'politics', '道德与法治', '🏛️', 5, 1),
(6,  1, 'music',    '音乐',       '🎵', 6, 1),
(7,  1, 'art',      '美术',       '🎨', 7, 1),
(8,  1, 'pe',       '体育',       '⚽', 8, 1),
-- 初中 (stage_id=2)
(9,  2, 'chinese',  '语文',       '📖', 1, 1),
(10, 2, 'math',     '数学',       '🔢', 2, 1),
(11, 2, 'english',  '英语',       '🌍', 3, 1),
(12, 2, 'physics',  '物理',       '⚡', 4, 1),
(13, 2, 'chemistry','化学',       '🧪', 5, 1),
(14, 2, 'biology',  '生物',       '🌱', 6, 1),
(15, 2, 'history',  '历史',       '📜', 7, 1),
(16, 2, 'geography','地理',       '🗺️', 8, 1),
(17, 2, 'politics', '政治',       '🏛️', 9, 1),
-- 高中 (stage_id=3)
(18, 3, 'chinese',  '语文',       '📖', 1, 1),
(19, 3, 'math',     '数学',       '🔢', 2, 1),
(20, 3, 'english',  '英语',       '🌍', 3, 1),
(21, 3, 'physics',  '物理',       '⚡', 4, 1),
(22, 3, 'chemistry','化学',       '🧪', 5, 1),
(23, 3, 'biology',  '生物',       '🌱', 6, 1),
(24, 3, 'history',  '历史',       '📜', 7, 1),
(25, 3, 'geography','地理',       '🗺️', 8, 1),
(26, 3, 'politics', '政治',       '🏛️', 9, 1),
-- 美术学段 / 舞蹈学段
(27, 4, 'art',      '美术',       '🎨', 1, 1),
(28, 5, 'dance',    '舞蹈',       '💃', 1, 1),
-- 幼儿阶段
(29, 6, 'chinese',  '拼音识字',   '🔤', 1, 1),
(30, 6, 'math',     '数学启蒙',   '🔢', 2, 1),
(31, 6, 'habit',    '习惯养成',   '🌟', 3, 1),
(32, 6, 'activity', '综合活动',   '🧩', 4, 1);

-- ==================== 5. 年级 ====================
INSERT INTO `edu_grade` (`id`, `stage_id`, `code`, `name`, `sort`, `status`) VALUES
(1,  1, 'grade1', '一年级', 1, 1),
(2,  1, 'grade2', '二年级', 2, 1),
(3,  1, 'grade3', '三年级', 3, 1),
(4,  1, 'grade4', '四年级', 4, 1),
(5,  1, 'grade5', '五年级', 5, 1),
(6,  1, 'grade6', '六年级', 6, 1),
-- 初中 7-9
(7,  2, 'grade7',  '初一', 1, 1),
(8,  2, 'grade8',  '初二', 2, 1),
(9,  2, 'grade9',  '初三', 3, 1),
-- 高中 10-12
(10, 3, 'grade10', '高一', 1, 1),
(11, 3, 'grade11', '高二', 2, 1),
(12, 3, 'grade12', '高三', 3, 1),
-- 美术学段（沿用小学年级命名）
(13, 4, 'grade1', '一年级', 1, 1),
(14, 4, 'grade2', '二年级', 2, 1),
(15, 4, 'grade3', '三年级', 3, 1),
(16, 4, 'grade4', '四年级', 4, 1),
(17, 4, 'grade5', '五年级', 5, 1),
(18, 4, 'grade6', '六年级', 6, 1),
-- 舞蹈学段
(19, 5, 'grade1', '一年级', 1, 1),
(20, 5, 'grade2', '二年级', 2, 1),
(21, 5, 'grade3', '三年级', 3, 1),
(22, 5, 'grade4', '四年级', 4, 1),
(23, 5, 'grade5', '五年级', 5, 1),
(24, 5, 'grade6', '六年级', 6, 1),
-- 幼儿阶段
(25, 6, 'k2', '中班', 1, 1),
(26, 6, 'k3', '大班', 2, 1),
(27, 6, 'bridge', '幼小衔接', 3, 1);

-- ==================== 6. 学科-版本关联（subjectVersionsMap） ====================
-- 使用 INSERT IGNORE，避免重复执行种子脚本时 uk_subject_edition 冲突
-- 语文：全部 9 版本
INSERT IGNORE INTO `edu_subject_edition` (`subject_id`, `edition_id`, `sort`)
SELECT s.id, e.id, e.sort
FROM `edu_subject` s
CROSS JOIN `edu_edition` e
WHERE s.code = 'chinese';

-- 数学/英语：除语文版(7)外
INSERT IGNORE INTO `edu_subject_edition` (`subject_id`, `edition_id`, `sort`)
SELECT s.id, e.id, e.sort
FROM `edu_subject` s
CROSS JOIN `edu_edition` e
WHERE s.code IN ('math', 'english') AND e.id <> 7;

-- 科学：主流 5 版本（仅小学）
INSERT IGNORE INTO `edu_subject_edition` (`subject_id`, `edition_id`, `sort`)
SELECT s.id, e.id, e.sort
FROM `edu_subject` s
JOIN `edu_edition` e ON e.code IN ('tongbian2024','renjiao','beishida','sujiao','hujiao')
WHERE s.code = 'science';

-- 政治/道德与法治：统编为主
INSERT IGNORE INTO `edu_subject_edition` (`subject_id`, `edition_id`, `sort`)
SELECT s.id, e.id, e.sort
FROM `edu_subject` s
JOIN `edu_edition` e ON e.code IN ('tongbian2024','tongbian2016','renjiao')
WHERE s.code = 'politics';

-- 理化生/地理：主流 5 版本
INSERT IGNORE INTO `edu_subject_edition` (`subject_id`, `edition_id`, `sort`)
SELECT s.id, e.id, e.sort
FROM `edu_subject` s
JOIN `edu_edition` e ON e.code IN ('tongbian2024','renjiao','beishida','sujiao','hujiao')
WHERE s.code IN ('physics','chemistry','biology','geography');

-- 历史：统编+北师大+人教
INSERT IGNORE INTO `edu_subject_edition` (`subject_id`, `edition_id`, `sort`)
SELECT s.id, e.id, e.sort
FROM `edu_subject` s
JOIN `edu_edition` e ON e.code IN ('tongbian2024','tongbian2016','renjiao','beishida')
WHERE s.code = 'history';

-- 音乐/美术/体育：主流 4 版本（仅小学 stage_id=1，避免与美术学段 code=art 的 id=27 重复）
INSERT IGNORE INTO `edu_subject_edition` (`subject_id`, `edition_id`, `sort`)
SELECT s.id, e.id, e.sort
FROM `edu_subject` s
JOIN `edu_edition` e ON e.code IN ('tongbian2024','renjiao','beishida','sujiao')
WHERE s.code IN ('music','art','pe') AND s.stage_id = 1;

-- 美术学段 / 舞蹈学段：全 9 版本
INSERT IGNORE INTO `edu_subject_edition` (`subject_id`, `edition_id`, `sort`)
SELECT s.id, e.id, e.sort
FROM `edu_subject` s
CROSS JOIN `edu_edition` e
WHERE s.stage_id IN (4, 5);

-- 幼儿阶段习惯/活动：启蒙与机构课程以通用主流版本为主
INSERT IGNORE INTO `edu_subject_edition` (`subject_id`, `edition_id`, `sort`)
SELECT s.id, e.id, e.sort
FROM `edu_subject` s
JOIN `edu_edition` e ON e.code IN ('tongbian2024','renjiao','beishida')
WHERE s.stage_id = 6 AND s.code IN ('habit','activity');

-- ==================== 7. 资源类型树 ====================
-- 一级分组 parent_id=0
INSERT INTO `edu_resource_type` (`id`, `parent_id`, `code`, `name`, `icon`, `group_code`, `group_name`, `default_exts`, `allow_preview`, `sort`, `status`) VALUES
(1,  0, 'grp_teach',    '备课授课', NULL, 'teach',    '备课授课', NULL, 0, 1, 1),
(2,  0, 'grp_practice', '练习测评', NULL, 'practice', '练习测评', NULL, 0, 2, 1),
(3,  0, 'grp_review',   '复习备考', NULL, 'review',   '复习备考', NULL, 0, 3, 1),
(4,  0, 'grp_textbook', '教材教辅', NULL, 'textbook', '教材教辅', NULL, 0, 4, 1),
(5,  0, 'grp_media',    '音视频',   NULL, 'media',    '音视频',   NULL, 0, 5, 1),
(6,  0, 'grp_reflect',  '反思总结', NULL, 'reflect',  '反思总结', NULL, 0, 6, 1),
(7,  0, 'grp_material', '素材其他', NULL, 'material', '素材其他', NULL, 0, 7, 1);

-- 二级：备课授课
INSERT INTO `edu_resource_type` (`id`, `parent_id`, `code`, `name`, `icon`, `group_code`, `group_name`, `default_exts`, `allow_preview`, `sort`, `status`) VALUES
(11, 1, 'lesson_plan',      '教案',     '📖', 'teach', '备课授课', '["doc","docx"]', 1, 1, 1),
(12, 1, 'study_guide',      '学案',     '📘', 'teach', '备课授课', '["doc","docx"]', 1, 2, 1),
(13, 1, 'courseware',       '课件',     '📊', 'teach', '备课授课', '["ppt","pptx"]', 1, 3, 1),
(14, 1, 'handout',          '讲义',     '📄', 'teach', '备课授课', '["doc","docx","pdf"]', 1, 4, 1),
(15, 1, 'lecture_script',   '说课稿',   '🎤', 'teach', '备课授课', '["doc","docx"]', 1, 5, 1),
(16, 1, 'teaching_plan',    '教学计划', '📋', 'teach', '备课授课', '["doc","docx"]', 1, 6, 1),
(17, 1, 'preview_sheet',    '预习单',   '📝', 'teach', '备课授课', '["doc","docx"]', 1, 7, 1),
(18, 1, 'classroom_record', '课堂实录', '🎬', 'teach', '备课授课', '["mp4"]', 1, 8, 1),
(19, 1, 'open_class',       '公开课',   '🏫', 'teach', '备课授课', '["ppt","pptx","mp4"]', 1, 9, 1),
(20, 1, 'verbatim',         '逐字稿',   '📃', 'teach', '备课授课', '["doc","docx"]', 1, 10, 1);

-- 二级：练习测评
INSERT INTO `edu_resource_type` (`id`, `parent_id`, `code`, `name`, `icon`, `group_code`, `group_name`, `default_exts`, `allow_preview`, `sort`, `status`) VALUES
(21, 2, 'class_exercise',   '课时练习', '📝', 'practice', '练习测评', '["doc","docx","pdf"]', 1, 1, 1),
(22, 2, 'unit_test',        '单元测试', '📋', 'practice', '练习测评', '["doc","docx","pdf"]', 1, 2, 1),
(23, 2, 'exam_paper',       '试卷',     '📄', 'practice', '练习测评', '["doc","docx","pdf"]', 1, 3, 1),
(24, 2, 'answer_analysis',  '答案解析', '✅', 'practice', '练习测评', '["doc","docx","pdf"]', 1, 4, 1),
(25, 2, 'practice',         '练习',     '✏️', 'practice', '练习测评', '["doc","docx","pdf"]', 1, 5, 1);

-- 二级：复习备考
INSERT INTO `edu_resource_type` (`id`, `parent_id`, `code`, `name`, `icon`, `group_code`, `group_name`, `default_exts`, `allow_preview`, `sort`, `status`) VALUES
(31, 3, 'review_outline',   '复习提纲', '🔍', 'review', '复习备考', '["doc","docx"]', 1, 1, 1),
(32, 3, 'topic_lecture',    '专题讲义', '📚', 'review', '复习备考', '["doc","docx","pdf"]', 1, 2, 1),
(33, 3, 'mistake_collection','错题汇编','📒', 'review', '复习备考', '["doc","docx","pdf"]', 1, 3, 1);

-- 二级：教材教辅
INSERT INTO `edu_resource_type` (`id`, `parent_id`, `code`, `name`, `icon`, `group_code`, `group_name`, `default_exts`, `allow_preview`, `sort`, `status`) VALUES
(41, 4, 'e_textbook',       '电子课本', '📕', 'textbook', '教材教辅', '["pdf"]', 1, 1, 1),
(42, 4, 'teacher_book',     '教师用书', '📚', 'textbook', '教材教辅', '["pdf"]', 1, 2, 1);

-- 二级：音视频
INSERT INTO `edu_resource_type` (`id`, `parent_id`, `code`, `name`, `icon`, `group_code`, `group_name`, `default_exts`, `allow_preview`, `sort`, `status`) VALUES
(51, 5, 'audio_read',       '音频朗读', '🎧', 'media', '音视频', '["mp3","wav"]', 1, 1, 1),
(52, 5, 'micro_lesson',     '微课',     '🎥', 'media', '音视频', '["mp4"]', 1, 2, 1),
(53, 5, 'knowledge_video',  '知识点视频','📺','media', '音视频', '["mp4"]', 1, 3, 1),
(54, 5, 'demo_lesson',      '示范课',   '🎓', 'media', '音视频', '["mp4"]', 1, 4, 1),
(55, 5, 'video',            '视频',     '🎬', 'media', '音视频', '["mp4"]', 1, 5, 1);

-- 二级：反思总结
INSERT INTO `edu_resource_type` (`id`, `parent_id`, `code`, `name`, `icon`, `group_code`, `group_name`, `default_exts`, `allow_preview`, `sort`, `status`) VALUES
(61, 6, 'teaching_reflect', '教学反思', '💭', 'reflect', '反思总结', '["doc","docx"]', 1, 1, 1),
(62, 6, 'teaching_summary', '教学总结', '📋', 'reflect', '反思总结', '["doc","docx"]', 1, 2, 1);

-- 二级：素材与其他（含历史兼容类型）
INSERT INTO `edu_resource_type` (`id`, `parent_id`, `code`, `name`, `icon`, `group_code`, `group_name`, `default_exts`, `allow_preview`, `sort`, `status`) VALUES
(71, 7, 'image_material',   '图片素材', '🖼️', 'material', '素材其他', '["jpg","png"]', 1, 1, 1),
(72, 7, 'ppt_template',     '课件模板', '📊', 'material', '素材其他', '["ppt","pptx"]', 1, 2, 1),
(73, 7, 'mind_map',         '思维导图', '🗺️', 'material', '素材其他', '["xmind","png"]', 1, 3, 1),
(74, 7, 'material',         '素材',     '🎨', 'material', '素材其他', '["zip","rar"]', 0, 4, 1),
(75, 7, 'prep_bundle',      '备课综合', '📦', 'material', '素材其他', '["zip"]', 0, 5, 1),
(76, 7, 'unit_bundle',      '单元整合', '📦', 'material', '素材其他', '["zip"]', 0, 6, 1),
(77, 7, 'topic_explain',    '专题讲解', '🔍', 'material', '素材其他', '["doc","ppt","pdf"]', 1, 7, 1),
(78, 7, 'lab_report',       '实验报告', '🔬', 'material', '素材其他', '["doc","docx"]', 1, 8, 1),
(79, 7, 'knowledge_point',  '知识点',   '💡', 'material', '素材其他', '["doc","pdf"]', 1, 9, 1),
(80, 7, 'other',            '其他',     '📄', 'material', '素材其他', NULL, 1, 99, 1);

-- ==================== 8. 栏目/专区（columnConfig + 设计文档） ====================
INSERT INTO `edu_module` (`id`, `code`, `name`, `icon`, `module_category`, `applicable_stages`, `description`, `sort`, `status`) VALUES
(1,  'sync_prep',       '同步备课',     '📚', 'sync',         NULL, '日常同步教学资源', 1, 1),
(2,  'school_open',     '开学专区',     '🏫', 'transition',   NULL, '开学季专题', 2, 1),
(3,  'monthly',         '月考',         '📝', 'monthly',      NULL, '月考专区', 3, 1),
(4,  'midterm',         '期中',         '📋', 'term',         NULL, '期中考试', 4, 1),
(5,  'final',           '期末',         '📋', 'term',         NULL, '期末考试', 5, 1),
(6,  'unit_test_mod',   '单元测试',     '📝', 'sync',         '["primary","junior","senior"]', '单元检测', 6, 1),
(7,  'xsc_real',        '小升初真题',   '🎯', 'exam',         '["primary"]', '小升初真题', 7, 1),
(8,  'xsc_mock',        '小升初模拟',   '🎯', 'exam',         '["primary"]', '小升初模拟', 8, 1),
(9,  'xsc_zone',        '小升初专区',   '🎯', 'transition',   '["primary"]', '小升初备考', 9, 1),
(10, 'topic_review',    '专题复习',     '🔍', 'topic',        NULL, '专题复习', 10, 1),
(11, 'real_collection', '真题汇编',     '📜', 'exam',         NULL, '历年真题汇编', 11, 1),
(12, 'summer',          '暑假',         '☀️', 'holiday',      NULL, '暑假作业与复习', 12, 1),
(13, 'winter',          '寒假',         '❄️', 'holiday',      NULL, '寒假作业与复习', 13, 1),
(14, 'composition',     '作文',         '✍️', 'composition',  NULL, '作文指导与范文', 14, 1),
(15, 'reading',         '阅读',         '📖', 'reading',      NULL, '阅读理解', 15, 1),
(16, 'competition',     '竞赛',         '🏆', 'competition',  NULL, '学科竞赛', 16, 1),
(17, 'knowledge_mod',   '知识点',       '💡', 'sync',         NULL, '知识点梳理', 17, 1),
(18, 'academic_level',  '学业水平',     '📊', 'exam',         '["junior","senior"]', '学业水平考试', 18, 1),
(19, 'round1',          '一轮复习',     '1️⃣', 'review',       '["junior","senior"]', '一轮复习', 19, 1),
(20, 'round2',          '二轮专题',     '2️⃣', 'review',       '["junior","senior"]', '二轮专题', 20, 1),
(21, 'round3',          '三轮冲刺',     '3️⃣', 'review',       '["junior","senior"]', '三轮冲刺', 21, 1),
(22, 'zk_mock',         '中考模拟',     '📝', 'exam',         '["junior"]', '中考模拟卷', 22, 1),
(23, 'zk_real',         '中考真题',     '📜', 'exam',         '["junior"]', '中考真题', 23, 1),
(24, 'gk_mock',         '高考模拟',     '📝', 'exam',         '["senior"]', '高考模拟卷', 24, 1),
(25, 'gk_real',         '高考真题',     '📜', 'exam',         '["senior"]', '高考真题', 25, 1),
(26, 'pure_material',   '纯素材',       '🎨', 'material',     '["primary","junior","senior"]', '纯素材下载', 26, 1),
-- 兼容旧栏目名称（别名入口，与前端历史数据对齐）
(27, 'midterm_zone',    '期中专区',     '📋', 'term',         NULL, '期中专区（别名）', 27, 1),
(28, 'final_zone',      '期末专区',     '📋', 'term',         NULL, '期末专区（别名）', 28, 1),
(29, 'summer_zone',     '暑假专区',     '☀️', 'holiday',      NULL, '暑假专区（别名）', 29, 1),
(30, 'winter_zone',     '寒假专区',     '❄️', 'holiday',      NULL, '寒假专区（别名）', 30, 1),
(31, 'composition_zone','作文专区',     '✍️', 'composition',  NULL, '作文专区（别名）', 31, 1),
(32, 'reading_comp',    '阅读理解',     '📖', 'reading',      NULL, '阅读理解（别名）', 32, 1),
(33, 'competition_zone','竞赛专区',     '🏆', 'competition',  NULL, '竞赛专区（别名）', 33, 1),
-- 幼儿阶段 / 幼小衔接栏目
(34, 'preschool_pinyin', '拼音识字',     '🔤', 'transition',   '["preschool"]', '声母韵母、整体认读、识字卡片与拼音练习', 34, 1),
(35, 'preschool_math',   '数学启蒙',     '🔢', 'sync',         '["preschool"]', '数感、10以内加减与趣味数学启蒙', 35, 1),
(36, 'preschool_teach',  '教学启蒙',     '🌱', 'sync',         '["preschool"]', '幼儿入学认知、课堂启蒙与综合教学活动', 36, 1),
(37, 'preschool_habit',  '习惯养成',     '🌟', 'transition',   '["preschool"]', '课堂规则、专注力、时间管理与入学适应', 37, 1),
(38, 'preschool_summer', '暑假衔接',     '☀️', 'holiday',      '["preschool"]', '大班升小学暑假综合训练', 38, 1),
(39, 'preschool_bridge', '幼小衔接',     '🏫', 'transition',   '["preschool"]', '幼小衔接综合资料', 39, 1),
(40, 'picture_reading',  '绘本阅读',     '📖', 'reading',      '["preschool"]', '绘本阅读、亲子共读与表达训练', 40, 1),
(41, 'oral_reading',     '表达与阅读',   '💬', 'reading',      '["preschool"]', '看图说话、口语表达与早期阅读', 41, 1),
(42, 'preschool_activity','综合活动',    '🧩', 'topic',        '["preschool"]', '主题活动、游戏化学习与综合实践', 42, 1),
(43, 'home_coeducation', '家园共育',     '👨‍👩‍👧', 'material','["preschool"]', '家长沟通、入学准备清单与家庭指导', 43, 1);

-- 栏目-学段关联（applicable_stages 为空=全学段；否则仅匹配列出的学段 code）
INSERT INTO `edu_module_stage` (`module_id`, `stage_id`, `sort`)
SELECT m.id, s.id, m.sort
FROM `edu_module` m
CROSS JOIN `edu_stage` s
WHERE m.applicable_stages IS NULL
  AND s.code <> 'preschool';

INSERT INTO `edu_module_stage` (`module_id`, `stage_id`, `sort`)
SELECT m.id, s.id, m.sort
FROM `edu_module` m
JOIN `edu_stage` s ON JSON_CONTAINS(m.applicable_stages, CONCAT('"', s.code, '"'))
WHERE m.applicable_stages IS NOT NULL;

-- ==================== 9. 考试场景 / 教学场景 ====================
INSERT INTO `edu_exam_scene` (`id`, `code`, `name`, `exam_level`, `sort`, `status`) VALUES
(1, 'unit',       '单元测验', 'unit',       1, 1),
(2, 'monthly',    '月考',     'monthly',    2, 1),
(3, 'midterm',    '期中考试', 'midterm',    3, 1),
(4, 'final',      '期末考试', 'final',      4, 1),
(5, 'xsc',        '小升初',   'xsc',        5, 1),
(6, 'zk',         '中考',     'zk',         6, 1),
(7, 'gk',         '高考',     'gk',         7, 1),
(8, 'mock',       '模拟考试', 'mock',       8, 1),
(9, 'real_paper', '真题',     'real_paper', 9, 1),
(10,'competition','竞赛',     'competition',10, 1),
(11,'academic',   '学业水平', 'academic',   11, 1),
(12,'topic',      '专题',     'topic',      12, 1);

INSERT INTO `edu_teaching_scene` (`id`, `code`, `name`, `sort`, `status`) VALUES
(1, 'preview',    '预习',   1, 1),
(2, 'new_lesson', '新课',   2, 1),
(3, 'practice',   '练习',   3, 1),
(4, 'review',     '复习',   4, 1),
(5, 'open_class', '公开课', 5, 1),
(6, 'exam_prep',  '备考',   6, 1),
(7, 'after_class','课后',   7, 1);

-- ==================== 10. 文件格式 / 频道 / 地区样例 ====================
INSERT INTO `edu_file_format` (`id`, `code`, `name`, `extensions`, `mime_types`, `preview_type`, `sort`, `status`) VALUES
(1, 'word',    'Word文档', 'doc,docx', 'application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'office', 1, 1),
(2, 'ppt',     'PPT演示',  'ppt,pptx', 'application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation', 'office', 2, 1),
(3, 'pdf',     'PDF',      'pdf',      'application/pdf', 'pdf', 3, 1),
(4, 'audio',   '音频',     'mp3,wav',  'audio/mpeg,audio/wav', 'audio', 4, 1),
(5, 'video',   '视频',     'mp4',      'video/mp4', 'video', 5, 1),
(6, 'archive', '压缩包',   'zip,rar',  'application/zip,application/x-rar-compressed', 'download_only', 6, 1),
(7, 'image',   '图片',     'jpg,png,gif', 'image/jpeg,image/png,image/gif', 'image', 7, 1);

INSERT INTO `edu_channel` (`id`, `code`, `name`, `description`, `sort`, `status`) VALUES
(1, 'main',      '主站',     'K12 主站资源', 1, 1),
(2, 'banhui',    '班会育人', '班会/德育频道', 2, 1),
(3, 'patriotic', '爱国教育', '爱国主义教育', 3, 1);

INSERT INTO `feature_category` (`id`, `channel_id`, `parent_id`, `code`, `name`, `sort`, `status`) VALUES
(1, 2, 0, 'banhui_root', '班会育人', 1, 1),
(2, 2, 1, 'theme_edu',   '主题教育', 1, 1),
(3, 2, 1, 'class_meet',  '班会活动', 2, 1),
(4, 3, 0, 'patriotic_root', '爱国教育', 1, 1);

INSERT INTO `edu_region` (`id`, `parent_id`, `code`, `name`, `level`, `sort`, `status`) VALUES
(1, 0, 'CN',     '全国',   0, 0, 1),
(2, 1, '110000', '北京市', 1, 1, 1),
(3, 1, '310000', '上海市', 1, 2, 1),
(4, 1, '440000', '广东省', 1, 3, 1),
(5, 1, '330000', '浙江省', 1, 4, 1),
(6, 1, '320000', '江苏省', 1, 5, 1);

-- ==================== 11. 单元 / 课文（小学语文统编2024 样例） ====================
INSERT INTO `edu_unit` (`id`, `subject_id`, `grade_id`, `edition_id`, `volume_id`, `semester_id`, `code`, `name`, `sort`, `status`) VALUES
-- 一年级上册 subject_id=1, grade_id=1, edition_id=1, volume_id=1
(1,  1, 1, 1, 1, 1, 'u1', '第一单元 识字（一）',  1, 1),
(2,  1, 1, 1, 1, 1, 'u2', '第二单元 识字（二）',  2, 1),
(3,  1, 1, 1, 1, 1, 'u3', '第三单元 拼音',        3, 1),
(4,  1, 1, 1, 1, 1, 'u4', '第四单元 阅读（一）',  4, 1),
(5,  1, 1, 1, 1, 1, 'u5', '第五单元 阅读（二）',  5, 1),
(6,  1, 1, 1, 1, 1, 'u6', '第六单元 口语交际',    6, 1),
-- 一年级下册 volume_id=2
(7,  1, 1, 1, 2, 2, 'u1', '第一单元 识字',        1, 1),
(8,  1, 1, 1, 2, 2, 'u2', '第二单元 课文（一）',  2, 1),
(9,  1, 1, 1, 2, 2, 'u3', '第三单元 课文（二）',  3, 1),
(10, 1, 1, 1, 2, 2, 'u4', '第四单元 课文（三）',  4, 1),
(11, 1, 1, 1, 2, 2, 'u5', '第五单元 课文（四）',  5, 1),
(12, 1, 1, 1, 2, 2, 'u6', '第六单元 课文（五）',  6, 1),
-- 二年级上册 grade_id=2
(13, 1, 2, 1, 1, 1, 'u1', '第一单元', 1, 1),
(14, 1, 2, 1, 1, 1, 'u2', '第二单元', 2, 1),
(15, 1, 2, 1, 1, 1, 'u3', '第三单元', 3, 1),
(16, 1, 2, 1, 1, 1, 'u4', '第四单元', 4, 1),
(17, 1, 2, 1, 1, 1, 'u5', '第五单元', 5, 1),
(18, 1, 2, 1, 1, 1, 'u6', '第六单元', 6, 1),
(19, 1, 2, 1, 1, 1, 'u7', '第七单元', 7, 1),
(20, 1, 2, 1, 1, 1, 'u8', '第八单元', 8, 1);

-- 课文：一年级上册第一单元
INSERT INTO `edu_lesson` (`id`, `unit_id`, `code`, `name`, `lesson_no`, `sort`, `status`) VALUES
(1,  1, 'l1', '天地人',     1, 1, 1),
(2,  1, 'l2', '金木水火土', 2, 2, 1),
(3,  1, 'l3', '口耳目手足', 3, 3, 1),
(4,  2, 'l1', 'a o e',      1, 1, 1),
(5,  2, 'l2', 'i u ü',     2, 2, 1),
(6,  3, 'l1', 'b p m f',    1, 1, 1),
(7,  4, 'l1', '秋天',       1, 1, 1),
(8,  4, 'l2', '小小的船',   2, 2, 1);

-- 知识点样例
INSERT INTO `edu_knowledge_point` (`id`, `subject_id`, `lesson_id`, `parent_id`, `code`, `name`, `sort`, `status`) VALUES
(1, 1, 1, 0, 'kp1', '识字与写字', 1, 1),
(2, 1, 1, 1, 'kp1-1', '认识常用汉字', 1, 1),
(3, 1, 7, 0, 'kp2', '朗读与理解', 1, 1);

-- ==================== 12. 班会育人 dict 扩展（原 k12_banhui_category.sql） ====================
INSERT IGNORE INTO `dict` (`type`, `code`, `name`, `label`, `icon`, `grade_levels`, `sort`, `group_key`, `group_name`, `description`) VALUES
('resource_type', 'banhui_meeting', '主题班会', '主题班会', '📝', 'primary,junior,senior', 1, 'banhui', '班会育人', '各类主题班会PPT、视频、教案'),
('resource_type', 'banhui_communist', '班会-团日活动', '团日活动', '🏅', 'junior,senior', 2, 'banhui', '班会育人', '共青团、团日活动资源'),
('resource_type', 'banhui_party', '班会-党史教育', '党史教育', '🇨🇳', 'junior,senior', 3, 'banhui', '班会育人', '党史学习教育资料'),
('resource_type', 'banhui_parent_general', '家长会-综合性', '综合性家长会', '👨‍👩‍👧', 'primary,junior,senior', 10, 'banhui', '班会育人', '综合性家长会PPT'),
('resource_type', 'banhui_parent_progress', '家长会-学期汇报', '学期汇报', '📊', 'primary,junior,senior', 11, 'banhui', '班会育人', '学生学期表现汇报'),
('resource_type', 'banhui_parent_exam', '家长会-考试分析', '考试分析会', '📈', 'junior,senior', 12, 'banhui', '班会育人', '期中期末考试分析'),
('resource_type', 'banhui_parent_zhongkao', '家长会-中考/高考', '升学指导会', '🎓', 'junior,senior', 13, 'banhui', '班会育人', '中考/高考备考指导'),
('resource_type', 'banhui_safety_campus', '安全教育-校园安全', '校园安全', '🏫', 'primary,junior,senior', 20, 'banhui', '班会育人', '校园安全教育资料'),
('resource_type', 'banhui_safety_traffic', '安全教育-交通安全', '交通安全', '🚗', 'primary,junior,senior', 21, 'banhui', '班会育人', '交通安全教育资料'),
('resource_type', 'banhui_safety_fire', '安全教育-消防安全', '消防安全', '🔥', 'primary,junior,senior', 22, 'banhui', '班会育人', '消防安全教育资料'),
('resource_type', 'banhui_safety_electrical', '安全教育-用电安全', '用电安全', '⚡', 'primary,junior,senior', 23, 'banhui', '班会育人', '用电安全教育资料'),
('resource_type', 'banhui_safety_water', '安全教育-防溺水', '防溺水', '🏊', 'primary,junior,senior', 24, 'banhui', '班会育人', '防溺水安全教育'),
('resource_type', 'banhui_safety_cyber', '安全教育-网络安全', '网络安全', '💻', 'junior,senior', 25, 'banhui', '班会育人', '网络安全教育资料'),
('resource_type', 'banhui_safety_anti_fraud', '安全教育-防诈骗', '防诈骗', '🛡️', 'junior,senior', 26, 'banhui', '班会育人', '防诈骗安全教育'),
('resource_type', 'banhui_safety_drug', '安全教育-禁毒教育', '禁毒教育', '⚠️', 'junior,senior', 27, 'banhui', '班会育人', '禁毒宣传教育资料'),
('resource_type', 'banhui_mental_growth', '心理健康-心理成长', '心理成长', '🌱', 'primary,junior,senior', 30, 'banhui', '班会育人', '青少年心理成长辅导'),
('resource_type', 'banhui_mental_exam', '心理健康-考试减压', '考试减压', '😌', 'junior,senior', 31, 'banhui', '班会育人', '考试焦虑心理辅导'),
('resource_type', 'banhui_mental_relationship', '心理健康-人际关系', '人际关系', '🤝', 'junior,senior', 32, 'banhui', '班会育人', '人际交往心理辅导'),
('resource_type', 'banhui_mental_family', '心理健康-亲子沟通', '亲子沟通', '👨‍👩‍👧‍👦', 'primary,junior,senior', 33, 'banhui', '班会育人', '亲子关系沟通指导'),
('resource_type', 'banhui_mental_bullying', '心理健康-校园欺凌', '校园欺凌', '✋', 'primary,junior,senior', 34, 'banhui', '班会育人', '校园欺凌预防与应对'),
('resource_type', 'banhui_mental_depression', '心理健康-情绪管理', '情绪管理', '😊', 'primary,junior,senior', 35, 'banhui', '班会育人', '情绪管理与调节'),
('resource_type', 'banhui_moral_patriotism', '品德培养-爱国主义', '爱国主义', '🇨🇳', 'primary,junior,senior', 40, 'banhui', '班会育人', '爱国主义教育资料'),
('resource_type', 'banhui_moral_honor', '品德培养-荣誉责任', '荣誉责任', '🏆', 'primary,junior,senior', 41, 'banhui', '班会育人', '荣誉感与责任感培养'),
('resource_type', 'banhui_moral_integrity', '品德培养-诚信教育', '诚信教育', '✅', 'primary,junior,senior', 42, 'banhui', '班会育人', '诚信品质培养'),
('resource_type', 'banhui_moral_diligence', '品德培养-勤奋学习', '勤奋学习', '📚', 'primary,junior,senior', 43, 'banhui', '班会育人', '勤奋学习品质培养'),
('resource_type', 'banhui_moral_respect', '品德培养-尊重感恩', '尊重感恩', '🙏', 'primary,junior,senior', 44, 'banhui', '班会育人', '尊重他人、感恩教育'),
('resource_type', 'banhui_moral_law', '品德培养-法治教育', '法治教育', '⚖️', 'junior,senior', 45, 'banhui', '班会育人', '法治观念培养'),
('resource_type', 'banhui_moral_env', '品德培养-环保意识', '环保意识', '🌍', 'primary,junior,senior', 46, 'banhui', '班会育人', '生态环境保护教育');

INSERT IGNORE INTO `dict` (`type`, `code`, `name`, `label`, `icon`, `grade_levels`, `sort`, `group_key`, `group_name`, `description`) VALUES
('scenario', 'banhui_meeting', '主题班会', '主题班会', '📝', 'primary,junior,senior', 1, 'banhui', '班会育人', '主题班会适用'),
('scenario', 'banhui_parent', '家长会', '家长会', '👨‍👩‍👧', 'primary,junior,senior', 2, 'banhui', '班会育人', '家长会适用'),
('scenario', 'banhui_safety', '安全教育', '安全教育', '🛡️', 'primary,junior,senior', 3, 'banhui', '班会育人', '安全教育适用'),
('scenario', 'banhui_mental', '心理健康', '心理健康', '💚', 'primary,junior,senior', 4, 'banhui', '班会育人', '心理健康辅导适用'),
('scenario', 'banhui_moral', '品德培养', '品德培养', '🌟', 'primary,junior,senior', 5, 'banhui', '班会育人', '品德培养适用'),
('media_type', 'video', '视频形式', '视频', '🎬', 'primary,junior,senior', 1, 'media', '资源形式', 'MP4等视频'),
('media_type', 'audio', '音频形式', '音频', '🎵', 'primary,junior,senior', 2, 'media', '资源形式', 'MP3等音频'),
('media_type', 'document', '文档形式', '文档', '📄', 'primary,junior,senior', 3, 'media', '资源形式', 'Word/PDF/PPT'),
('media_type', 'image', '图片形式', '图片', '🖼️', 'primary,junior,senior', 4, 'media', '资源形式', 'JPG/PNG');

SET FOREIGN_KEY_CHECKS = 1;

SELECT '99_seed_all.sql 执行完成' AS message,
  (SELECT COUNT(*) FROM edu_stage) AS stages,
  (SELECT COUNT(*) FROM edu_subject) AS subjects,
  (SELECT COUNT(*) FROM edu_edition) AS editions,
  (SELECT COUNT(*) FROM edu_module) AS modules,
  (SELECT COUNT(*) FROM edu_resource_type WHERE parent_id > 0) AS resource_types,
  (SELECT COUNT(*) FROM edu_unit) AS units,
  (SELECT COUNT(*) FROM edu_lesson) AS lessons;
