/**
 * 资源类型配置（按学段分组）
 */
export interface ResourceTypeItem {
  key: string
  name: string
  icon: string
}

export const resourceTypes: Record<string, ResourceTypeItem[]> = {
  preschool: [
    { key: 'preschool_handout', name: '启蒙练习册', icon: '📄' },
    { key: 'preschool_literacy_card', name: '拼音识字卡', icon: '🔤' },
    { key: 'preschool_math_game', name: '数学游戏单', icon: '🔢' },
    { key: 'preschool_picture_book', name: '绘本阅读', icon: '📖' },
    { key: 'preschool_courseware', name: '教案/课件', icon: '📊' },
    { key: 'preschool_activity', name: '课堂活动包', icon: '🧩' },
    { key: 'preschool_habit', name: '习惯养成表', icon: '🌟' },
    { key: 'preschool_parent', name: '家园沟通手册', icon: '👨‍👩‍👧' },
    { key: 'preschool_assessment', name: '入学准备测评', icon: '📝' },
  ],
  primary: [
    { key: 'paper_handout', name: '讲义/练习册', icon: '📄' },
    { key: 'paper_sync', name: '同步教辅', icon: '📚' },
    { key: 'paper_exam', name: '单元/期末试卷', icon: '📋' },
    { key: 'paper_guide', name: '教材配套学案', icon: '📝' },
    { key: 'paper_errorbook', name: '错题本', icon: '❌' },
    { key: 'media_micro', name: '微课/短视频', icon: '🎬' },
    { key: 'media_animation', name: '动画教学', icon: '🎞️' },
    { key: 'media_audio', name: '听力素材', icon: '🎧' },
    { key: 'media_record', name: '课堂实录', icon: '🎥' },
    { key: 'media_cartoon', name: '启蒙动画', icon: '🌟' },
    { key: 'doc_courseware', name: '教案/课件', icon: '📊' },
    { key: 'doc_outline', name: '教学大纲', icon: '📑' },
    { key: 'doc_analysis', name: '学情分析报告', icon: '📈' },
    { key: 'doc_parent', name: '家长沟通手册', icon: '👨‍👩‍👧' },
    { key: 'digital_bank', name: '题库', icon: '💻' },
    { key: 'digital_ai', name: 'AI组卷', icon: '🤖' },
    { key: 'digital_etextbook', name: '电子教材', icon: '📖' },
    { key: 'digital_template', name: '课件模板', icon: '📐' },
    { key: 'digital_platform', name: '学习平台', icon: '🌐' },
    { key: 'ext_assessment', name: '入学分班测', icon: '📝' },
    { key: 'ext_practical_art', name: '美术/书法教具', icon: '🖌️' },
    { key: 'ext_practical_code', name: '编程教具', icon: '🤖' },
    { key: 'ext_merchandise', name: '学习手账/成长手册', icon: '🎁' },
  ],
  junior: [
    { key: 'paper_handout_j', name: '同步讲义/练习册', icon: '📄' },
    { key: 'paper_zhongkao', name: '中考真题汇编', icon: '🏅' },
    { key: 'paper_exam_j', name: '单元/期中/期末试卷', icon: '📋' },
    { key: 'paper_topic', name: '专题学案', icon: '📝' },
    { key: 'paper_errorbook_j', name: '错题本', icon: '❌' },
    { key: 'media_micro_j', name: '微课/专题精讲', icon: '🎬' },
    { key: 'media_record_j', name: '课堂实录', icon: '🎥' },
    { key: 'media_course_j', name: '名师录播课', icon: '📺' },
    { key: 'media_audio_j', name: '听力素材', icon: '🎧' },
    { key: 'media_experiment', name: '实验教学视频', icon: '🧪' },
    { key: 'doc_courseware_j', name: '教案/课件', icon: '📊' },
    { key: 'doc_outline_j', name: '教学大纲', icon: '📑' },
    { key: 'doc_analysis_j', name: '学情分析报告', icon: '📈' },
    { key: 'doc_zhongkao', name: '中考考点手册', icon: '🎯' },
    { key: 'doc_parent_j', name: '家长沟通手册', icon: '👨‍👩‍👧' },
    { key: 'digital_bank_j', name: '题库', icon: '💻' },
    { key: 'digital_ai_j', name: 'AI组卷', icon: '🤖' },
    { key: 'digital_etextbook_j', name: '电子教材', icon: '📖' },
    { key: 'digital_template_j', name: '课件模板', icon: '📐' },
    { key: 'digital_platform_j', name: '学习平台', icon: '🌐' },
    { key: 'ext_assessment_j', name: '入学测/阶段测/中考模拟测', icon: '📝' },
    { key: 'ext_practical_science', name: '理化生实验器材', icon: '🔬' },
    { key: 'ext_merchandise_j', name: '错题本/知识点手账', icon: '📒' },
  ],
  senior: [
    { key: 'paper_review', name: '高考总复习讲义', icon: '📄' },
    { key: 'paper_gaokao', name: '真题汇编', icon: '🏆' },
    { key: 'paper_topic_s', name: '专题学案', icon: '📝' },
    { key: 'paper_mock', name: '模拟试卷', icon: '📋' },
    { key: 'paper_errorbook_s', name: '错题本', icon: '❌' },
    { key: 'paper_gaokao_point', name: '高考考点清单', icon: '🎯' },
    { key: 'media_topic_s', name: '专题精讲微课', icon: '🎬' },
    { key: 'media_course_s', name: '名师录播课', icon: '📺' },
    { key: 'media_record_s', name: '课堂实录', icon: '🎥' },
    { key: 'media_experiment_s', name: '实验教学视频', icon: '🧪' },
    { key: 'media_audio_s', name: '听力素材', icon: '🎧' },
    { key: 'doc_courseware_s', name: '教案/课件', icon: '📊' },
    { key: 'doc_outline_s', name: '教学大纲', icon: '📑' },
    { key: 'doc_analysis_s', name: '学情分析报告', icon: '📈' },
    { key: 'doc_policy', name: '高考政策解读', icon: '📜' },
    { key: 'doc_parent_s', name: '家长沟通手册', icon: '👨‍👩‍👧' },
    { key: 'digital_bank_s', name: '题库', icon: '💻' },
    { key: 'digital_ai_s', name: 'AI组卷', icon: '🤖' },
    { key: 'digital_etextbook_s', name: '电子教材', icon: '📖' },
    { key: 'digital_template_s', name: '课件模板', icon: '📐' },
    { key: 'digital_platform_s', name: '学习平台', icon: '🌐' },
    { key: 'ext_assessment_s', name: '入学测/月考/模考/高考模拟测', icon: '📝' },
    { key: 'ext_practical_s', name: '理化生实验器材', icon: '🔬' },
    { key: 'ext_merchandise_s', name: '高考错题本/冲刺手册', icon: '📒' },
  ],
  art: [
    { key: 'art_textbook', name: '考级专用教材', icon: '📖' },
    { key: 'art_copybook', name: '临摹画册', icon: '🖼️' },
    { key: 'art_template', name: '范画集', icon: '📋' },
    { key: 'art_exam_paper', name: '考级真题/作品集', icon: '📝' },
    { key: 'art_workbook', name: '练习册/线稿本', icon: '📓' },
    { key: 'art_form', name: '考级报名表/证书模板', icon: '📄' },
    { key: 'art_video_demo', name: '考级步骤示范视频', icon: '🎬' },
    { key: 'art_video_tech', name: '绘画技法微课', icon: '🎥' },
    { key: 'art_video_review', name: '作品点评实录', icon: '📺' },
    { key: 'art_video_guide', name: '考级流程讲解', icon: '🎞️' },
    { key: 'art_doc_plan', name: '考级教案/课件', icon: '📊' },
    { key: 'art_doc_outline', name: '考级大纲与考点', icon: '📑' },
    { key: 'art_doc_template', name: '学员作品集模板', icon: '📂' },
    { key: 'art_doc_analysis', name: '考级通过率分析', icon: '📈' },
    { key: 'art_doc_notice', name: '家长须知/考级通知', icon: '👨‍👩‍👧' },
    { key: 'art_digital_gallery', name: '电子范画库', icon: '💻' },
    { key: 'art_digital_bank', name: '考级题库', icon: '🤖' },
    { key: 'art_digital_archive', name: '作品电子档案', icon: '📁' },
    { key: 'art_digital_mock', name: '线上考级模拟系统', icon: '🌐' },
    { key: 'art_ext_material', name: '画材套装（铅笔/颜料/宣纸）', icon: '🛍️' },
    { key: 'art_ext_paper', name: '考级专用画纸/宣纸', icon: '📃' },
    { key: 'art_ext_mount', name: '装裱材料', icon: '🖼️' },
    { key: 'art_ext_merch', name: '文创周边（画板/围裙/作品集）', icon: '🎁' },
  ],
  dance: [
    { key: 'dance_paper_manual', name: '舞蹈考级教材', icon: '📖' },
    { key: 'dance_paper_combo', name: '考级组合手册', icon: '📋' },
    { key: 'dance_paper_basic', name: '基本功训练手册', icon: '💪' },
    { key: 'dance_paper_plan', name: '舞蹈教案/节拍谱', icon: '📝' },
    { key: 'dance_paper_score', name: '考级评分标准', icon: '📊' },
    { key: 'dance_video_demo', name: '考级组合示范视频', icon: '🎬' },
    { key: 'dance_video_tech', name: '基本功教学微课', icon: '🎥' },
    { key: 'dance_video_choreo', name: '成品舞教学', icon: '📺' },
    { key: 'dance_video_formation', name: '考级队形/镜面教学', icon: '🎞️' },
    { key: 'dance_video_stage', name: '舞台表演实录', icon: '🌟' },
    { key: 'dance_video_music', name: '考级音乐库', icon: '🎵' },
    { key: 'dance_doc_courseware', name: '考级教学课件', icon: '📊' },
    { key: 'dance_doc_outline', name: '考级大纲与流程', icon: '📑' },
    { key: 'dance_doc_schedule', name: '学员训练计划', icon: '📅' },
    { key: 'dance_doc_makeup', name: '舞台妆容/服装方案', icon: '💄' },
    { key: 'dance_doc_safety', name: '考级安全须知', icon: '⚠️' },
    { key: 'dance_digital_metronome', name: '节拍器/训练计时器', icon: '⏱️' },
    { key: 'dance_digital_motion', name: '舞蹈动作分解库', icon: '📖' },
    { key: 'dance_digital_mock', name: '线上考级模拟', icon: '🌐' },
    { key: 'dance_ext_costume', name: '舞蹈服、舞鞋、练功服', icon: '👗' },
    { key: 'dance_ext_prop', name: '考级专用道具（扇子/手绢/伞）', icon: '🪭' },
    { key: 'dance_ext_equip', name: '把杆、瑜伽垫、练功辅助器材', icon: '🧘' },
    { key: 'dance_ext_makeup', name: '舞台妆容用品', icon: '💅' },
  ],
}

/**
 * 根据学段获取资源类型列表
 */
export function getResourceTypesByGrade(level: string): ResourceTypeItem[] {
  return resourceTypes[level] || []
}

/**
 * 根据 key 获取资源类型名称
 */
export function getResourceTypeName(key: string, level?: string): string {
  const list = level ? resourceTypes[level] : Object.values(resourceTypes).flat()
  return list.find(r => r.key === key)?.name || key
}

// ==================== 兼容性别名 ====================

/** resourceTypeConfig 是 resourceTypes 的别名（兼容旧代码） */
export const resourceTypeConfig = resourceTypes
