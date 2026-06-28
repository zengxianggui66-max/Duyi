/**
 * 备考类型配置（按学段分组）
 */
export interface ExamTypeItem {
  key: string
  name: string
  icon?: string
}

export const examTypes: Record<string, ExamTypeItem[]> = {
  preschool: [
    { key: 'kindergarten_bridge', name: '幼小衔接', icon: '🏫' },
    { key: 'school_ready', name: '入学准备', icon: '🎒' },
    { key: 'habit_ready', name: '习惯养成', icon: '🌟' },
    { key: 'summer_bridge', name: '暑假衔接', icon: '☀️' },
  ],
  primary: [
    { key: 'sync_unit', name: '单元/月考', icon: '📋' },
    { key: 'sync_term', name: '期中/期末', icon: '📑' },
    { key: 'sync_vacation', name: '寒暑假衔接', icon: '🌞' },
    { key: 'entrance_primary', name: '小升初（民办校/重点校择校）', icon: '🏫' },
    { key: 'cert_ket', name: 'KET', icon: '📜' },
    { key: 'cert_pet', name: 'PET', icon: '📜' },
    { key: 'cert_math_olympiad', name: '数学竞赛（奥数/华杯赛）', icon: '🥇' },
    { key: 'cert_coding_scratch', name: 'Scratch/机器人考级', icon: '🤖' },
    { key: 'cert_art_level', name: '美术考级', icon: '🎨' },
    { key: 'cert_calligraphy_level', name: '书法考级', icon: '✒️' },
  ],
  junior: [
    { key: 'sync_unit_j', name: '单元/月考', icon: '📋' },
    { key: 'sync_term_j', name: '期中/期末', icon: '📑' },
    { key: 'sync_vacation_j', name: '寒暑假衔接', icon: '🌞' },
    { key: 'entrance_zhongkao', name: '中考（全科冲刺/专项提分）', icon: '🏅' },
    { key: 'cert_pet_j', name: 'PET/FCE', icon: '📜' },
    { key: 'cert_math_olympiad_j', name: '数学竞赛', icon: '🥇' },
    { key: 'cert_physics_olympiad', name: '物理竞赛', icon: '⚡' },
    { key: 'cert_chemistry_olympiad', name: '化学竞赛', icon: '🧪' },
    { key: 'cert_coding_python', name: '编程考级（Python）', icon: '💻' },
    { key: 'cert_english_competition', name: '英语能力竞赛', icon: '🌍' },
  ],
  senior: [
    { key: 'sync_unit_s', name: '单元/月考', icon: '📋' },
    { key: 'sync_term_s', name: '期中/期末', icon: '📑' },
    { key: 'sync_vacation_s', name: '寒暑假衔接', icon: '🌞' },
    { key: 'entrance_gaokao', name: '高考（全科冲刺/专项提分）', icon: '🏆' },
    { key: 'entrance_art_physical', name: '艺体考（文化课）', icon: '🎭' },
    { key: 'cert_fce_s', name: 'FCE/雅思/托福', icon: '📜' },
    { key: 'cert_math_olympiad_s', name: '数学竞赛', icon: '🥇' },
    { key: 'cert_physics_olympiad_s', name: '物理竞赛', icon: '⚡' },
    { key: 'cert_chemistry_olympiad_s', name: '化学竞赛', icon: '🧪' },
    { key: 'cert_biology_olympiad', name: '生物竞赛', icon: '🧬' },
    { key: 'cert_coding_algorithm', name: '编程考级（Python/算法）', icon: '💻' },
  ],
  art: [
    { key: 'art_level_1_10', name: '美术考级（1–10级）', icon: '🏅' },
    { key: 'art_children', name: '儿童画考级', icon: '👶' },
    { key: 'art_sketch_level', name: '素描考级', icon: '✏️' },
    { key: 'art_color_level', name: '色彩考级', icon: '🌈' },
    { key: 'art_chinese_painting_level', name: '国画考级', icon: '🖼️' },
    { key: 'art_cartoon_level', name: '动漫考级', icon: '✎' },
    { key: 'art_calligraphy_level', name: '书法考级（硬笔/软笔）', icon: '✒️' },
    { key: 'art_talent', name: '艺术特长生备考', icon: '⭐' },
    { key: 'art_quality_test', name: '中小学生艺术素养测评', icon: '📊' },
    { key: 'art_exam_prep', name: '美术类艺考备考', icon: '🎓' },
  ],
  dance: [
    { key: 'chinese_dance_level', name: '中国舞考级（1–10级）', icon: '🏅' },
    { key: 'folk_dance_level', name: '民族舞考级', icon: '💃' },
    { key: 'ballet_level', name: '芭蕾舞考级', icon: '🩰' },
    { key: 'latin_level', name: '拉丁舞考级', icon: '🔥' },
    { key: 'street_level', name: '流行舞/街舞考级', icon: '🕺' },
    { key: 'dance_talent', name: '艺术特长生备考', icon: '⭐' },
    { key: 'dance_exam_basics', name: '舞蹈艺考基本功训练', icon: '💪' },
    { key: 'dance_competition', name: '校园艺术节/比赛备赛', icon: '🏆' },
  ],
}

/**
 * 根据学段获取备考类型列表
 */
export function getExamTypesByGrade(level: string): ExamTypeItem[] {
  return examTypes[level] || []
}

/**
 * 根据 key 获取备考类型名称
 */
export function getExamTypeName(key: string, level?: string): string {
  const list = level ? examTypes[level] : Object.values(examTypes).flat()
  return list.find(e => e.key === key)?.name || key
}
