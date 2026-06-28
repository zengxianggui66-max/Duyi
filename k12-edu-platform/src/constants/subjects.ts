/**
 * 学科配置（按学段细分）
 */
export interface SubjectInfo {
  key: string
  name: string
  icon: string
  color: string
  gradeLevels: string[]
}

export const subjects: SubjectInfo[] = [
  // ========== 幼儿 ==========
  { key: 'preschool_literacy', name: '拼音识字', icon: '🔤', color: '#52C41A', gradeLevels: ['preschool'] },
  { key: 'preschool_math', name: '数学启蒙', icon: '🔢', color: '#3498DB', gradeLevels: ['preschool'] },
  { key: 'preschool_habit', name: '习惯养成', icon: '🌟', color: '#F59E0B', gradeLevels: ['preschool'] },
  { key: 'preschool_activity', name: '综合活动', icon: '🧩', color: '#8B5CF6', gradeLevels: ['preschool'] },
  // ========== 小学 ==========
  { key: 'chinese_p', name: '语文', icon: '📖', color: '#E74C3C', gradeLevels: ['primary'] },
  { key: 'math_p', name: '数学', icon: '🔢', color: '#3498DB', gradeLevels: ['primary'] },
  { key: 'english_p', name: '英语', icon: '🌍', color: '#2ECC71', gradeLevels: ['primary'] },
  { key: 'science', name: '科学', icon: '🔬', color: '#16A085', gradeLevels: ['primary'] },
  { key: 'steam', name: 'STEAM', icon: '🚀', color: '#9B59B6', gradeLevels: ['primary'] },
  { key: 'programming', name: '编程', icon: '💻', color: '#34495E', gradeLevels: ['primary'] },
  { key: 'calligraphy', name: '书法', icon: '✒️', color: '#795548', gradeLevels: ['primary'] },
  { key: 'art_p', name: '美术', icon: '🎨', color: '#D35400', gradeLevels: ['primary'] },
  { key: 'pe', name: '体育', icon: '⚽', color: '#E67E22', gradeLevels: ['primary'] },
  { key: 'music', name: '音乐', icon: '🎵', color: '#E91E63', gradeLevels: ['primary'] },
  { key: 'morality', name: '道德与法治', icon: '⚖️', color: '#E74C3C', gradeLevels: ['primary'] },
  // ========== 初中 ==========
  { key: 'chinese_j', name: '语文', icon: '📖', color: '#E74C3C', gradeLevels: ['junior'] },
  { key: 'math_j', name: '数学', icon: '🔢', color: '#3498DB', gradeLevels: ['junior'] },
  { key: 'english_j', name: '英语', icon: '🌍', color: '#2ECC71', gradeLevels: ['junior'] },
  { key: 'physics', name: '物理', icon: '⚡', color: '#9B59B6', gradeLevels: ['junior'] },
  { key: 'chemistry', name: '化学', icon: '🧪', color: '#F39C12', gradeLevels: ['junior'] },
  { key: 'biology', name: '生物', icon: '🧬', color: '#1ABC9C', gradeLevels: ['junior'] },
  { key: 'history_j', name: '历史', icon: '📜', color: '#8E44AD', gradeLevels: ['junior'] },
  { key: 'geography_j', name: '地理', icon: '🗺️', color: '#27AE60', gradeLevels: ['junior'] },
  { key: 'politics_j', name: '道法', icon: '⚖️', color: '#E74C3C', gradeLevels: ['junior'] },
  // ========== 高中 ==========
  { key: 'chinese_s', name: '语文', icon: '📖', color: '#E74C3C', gradeLevels: ['senior'] },
  { key: 'math_s', name: '数学', icon: '🔢', color: '#3498DB', gradeLevels: ['senior'] },
  { key: 'english_s', name: '英语', icon: '🌍', color: '#2ECC71', gradeLevels: ['senior'] },
  { key: 'physics_s', name: '物理', icon: '⚡', color: '#9B59B6', gradeLevels: ['senior'] },
  { key: 'chemistry_s', name: '化学', icon: '🧪', color: '#F39C12', gradeLevels: ['senior'] },
  { key: 'biology_s', name: '生物', icon: '🧬', color: '#1ABC9C', gradeLevels: ['senior'] },
  { key: 'politics_s', name: '政治', icon: '🏛️', color: '#E67E22', gradeLevels: ['senior'] },
  { key: 'history_s', name: '历史', icon: '📜', color: '#8E44AD', gradeLevels: ['senior'] },
  { key: 'geography_s', name: '地理', icon: '🗺️', color: '#27AE60', gradeLevels: ['senior'] },
  // ========== 美术 ==========
  { key: 'creative_art', name: '儿童创意美术', icon: '🎨', color: '#FF69B4', gradeLevels: ['art'] },
  { key: 'art_basics', name: '少儿美术基础', icon: '🖌️', color: '#795548', gradeLevels: ['art'] },
  { key: 'sketch', name: '素描', icon: '✏️', color: '#607D8B', gradeLevels: ['art'] },
  { key: 'color', name: '色彩（水粉/水彩/彩铅）', icon: '🌈', color: '#E91E63', gradeLevels: ['art'] },
  { key: 'cartoon', name: '动漫/卡通', icon: '✎', color: '#FF5722', gradeLevels: ['art'] },
  { key: 'chinese_painting', name: '国画', icon: '🖼️', color: '#8BC34A', gradeLevels: ['art'] },
  { key: 'calligraphy_art', name: '书法（硬笔/软笔）', icon: '✒️', color: '#9C27B0', gradeLevels: ['art'] },
  { key: 'handcraft', name: '手工/黏土/综合材料', icon: '🪆', color: '#00BCD4', gradeLevels: ['art'] },
  { key: 'art_exam', name: '艺考美术（素描/色彩/速写）', icon: '🎯', color: '#F44336', gradeLevels: ['art'] },
  // ========== 舞蹈 ==========
  { key: 'chinese_dance', name: '中国舞', icon: '🏮', color: '#D32F2F', gradeLevels: ['dance'] },
  { key: 'folk_dance', name: '少儿民族舞', icon: '💃', color: '#FF9800', gradeLevels: ['dance'] },
  { key: 'classical_dance', name: '古典舞', icon: '👑', color: '#9C27B0', gradeLevels: ['dance'] },
  { key: 'latin', name: '拉丁舞', icon: '🔥', color: '#E91E63', gradeLevels: ['dance'] },
  { key: 'ballroom', name: '摩登舞', icon: '🤵', color: '#3F51B5', gradeLevels: ['dance'] },
  { key: 'street_dance', name: '街舞/流行舞', icon: '🎧', color: '#00BCD4', gradeLevels: ['dance'] },
  { key: 'ballet_basics', name: '芭蕾基训', icon: '🩰', color: '#F8BBD9', gradeLevels: ['dance'] },
  { key: 'dance_exam', name: '艺考舞蹈', icon: '🎯', color: '#F44336', gradeLevels: ['dance'] },
]

/**
 * 根据学段获取学科列表
 */
export function getSubjectsByGrade(level: string): SubjectInfo[] {
  return subjects.filter(s => s.gradeLevels.includes(level as any))
}

/**
 * 根据 key 获取学科名称
 */
export function getSubjectName(key: string): string {
  return subjects.find(s => s.key === key)?.name || key
}

/**
 * 根据 key 获取学科信息
 */
export function getSubjectInfo(key: string): SubjectInfo | undefined {
  return subjects.find(s => s.key === key)
}

// ==================== 兼容性别名 ====================

/** subjectConfig 是 subjects 的别名（兼容旧代码） */
export const subjectConfig = subjects
