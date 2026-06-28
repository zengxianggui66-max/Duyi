/**
 * 资源展示工具函数
 * 统一管理资源图标、类型名称、封面颜色等映射
 */

/** 资源类型 → 图标 emoji */
export function getResourceIcon(type?: string): string {
  const map: Record<string, string> = {
    paper_handout: '📄', paper_exam: '📋', paper_guide: '📝',
    media_micro: '🎬', media_record: '🎥', media_course: '📺', media_animation: '🎞️', media_audio: '🎧',
    doc_courseware: '📊', doc_outline: '📑', doc_analysis: '📈', doc_parent: '👨‍👩‍👧',
    digital_bank: '💻', digital_ai: '🤖', digital_etextbook: '📖', digital_template: '📐', digital_platform: '🌐',
    ext_assessment: '📝', ext_practical: '🛠️', ext_merchandise: '🎁',
    courseware: '📊', lesson_plan: '📝', study_guide: '📋', exam_paper: '📋', material: '📂',
  }
  return map[type || ''] || '📄'
}

/** 教学资源类型 → 中文名称 */
export function getTypeName(type?: string): string {
  const map: Record<string, string> = {
    courseware: '课件', lesson_plan: '教案', study_guide: '导学案',
    exam_paper: '试卷', material: '素材',
  }
  return map[type || ''] || type || '资源'
}

/** 学科 key → 中文名称 */
export function getSubjectName(subject?: string): string {
  const map: Record<string, string> = {
    chinese: '语文', math: '数学', english: '英语', physics: '物理',
    chemistry: '化学', biology: '生物', politics: '政治', history: '历史',
    geography: '地理',
  }
  return map[subject || ''] || subject || '综合'
}

/** 教材版本 key → 显示名称 */
export function getVersionName(v?: string): string {
  const map: Record<string, string> = {
    pep_primary: '人教版', bsd_primary: '北师大版', suke: '苏教版',
    waiyan_primary: '外研版', hujiao: '沪教版', xiangjiao: '湘教版', lujiao: '鲁教版',
    oxford_discover: 'Oxford Discover', power_up: 'Power Up', think: 'Think',
    unlock: 'Unlock', get_smart: 'Get Smart', treasures: 'Treasures',
    other: '通用',
  }
  return map[v || ''] || v || '通用'
}

/** 考试类型 key → 显示名称 */
export function getExamTypeName(e?: string): string {
  const map: Record<string, string> = {
    sync_monthly: '单元/月考', sync_term: '期中/期末', sync_vacation: '寒暑假衔接',
    entrance_primary: '小升初', entrance_zhongkao: '中考', entrance_gaokao: '高考', entrance_arts: '艺体考',
    cert_cambridge: 'KET/PET/FCE', cert_ielts: '雅思/托福', cert_math: '数学竞赛',
    cert_science: '科学竞赛', cert_coding: '编程考级', cert_art: '美术/书法考级',
  }
  return map[e || ''] || ''
}

/** 封面渐变色（循环取色） */
const COVER_COLORS = [
  'linear-gradient(135deg, #667eea, #764ba2)',
  'linear-gradient(135deg, #f093fb, #f5576c)',
  'linear-gradient(135deg, #4facfe, #00f2fe)',
  'linear-gradient(135deg, #43e97b, #38f9d7)',
  'linear-gradient(135deg, #fa709a, #fee140)',
  'linear-gradient(135deg, #a18cd1, #fbc2eb)',
]

export function getCoverColor(id: number): string {
  return COVER_COLORS[id % COVER_COLORS.length]
}
