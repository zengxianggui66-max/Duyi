/** 专题资源专区：方案 P0 主站 resource API + 标签筛选 */

export const TOPIC_CHANNEL = 'zhuanti'

/** 当前学年（9 月起算新学年） */
export function resolveSchoolYear(date = new Date()): string {
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  if (month >= 9) return `${year}-${year + 1}`
  return `${year - 1}-${year}`
}

export const TOPIC_BASE_TAG = '专题资源'

export const TOPIC_REGIONS = [
  { key: 'all', name: '不限' },
  { key: 'chengdu', name: '成都' },
  { key: 'mianyang', name: '绵阳' },
  { key: 'sichuan', name: '四川其他' },
] as const

export const TOPIC_REGION_KEYWORDS: Record<string, string> = {
  chengdu: '成都',
  mianyang: '绵阳',
  sichuan: '四川',
}

export const TOPIC_GRADE_STAGES = [
  { key: 'all', name: '不限' },
  { key: 'preschool', name: '幼儿' },
  { key: 'primary', name: '小学' },
  { key: 'junior', name: '初中' },
  { key: 'senior', name: '高中' },
] as const

export const TOPIC_SUBJECTS = [
  { key: 'all', name: '不限' },
  { key: 'chinese', name: '语文' },
  { key: 'math', name: '数学' },
  { key: 'english', name: '英语' },
  { key: 'physics', name: '物理' },
  { key: 'chemistry', name: '化学' },
  { key: 'biology', name: '生物' },
  { key: 'politics', name: '道法' },
  { key: 'history', name: '历史' },
  { key: 'geography', name: '地理' },
] as const

export const TOPIC_FORMATS = [
  { key: 'all', name: '不限' },
  { key: 'ppt', name: 'PPT课件', resourceType: 'courseware' },
  { key: 'word', name: 'Word/教案', resourceType: 'lesson_plan' },
  { key: 'pdf', name: 'PDF/讲义', mediaType: 'document' },
  { key: 'exam', name: '试卷/真题', resourceType: 'exam' },
  { key: 'video', name: '视频', resourceType: 'video', mediaType: 'video' },
  { key: 'material', name: '练习/作业', resourceType: 'material' },
] as const

export const TOPIC_LEVELS = [
  { key: 'all', name: '不限' },
  { key: 'free', name: '免费', isFree: 1 },
  { key: 'paid', name: '会员精品', isFree: 0 },
] as const

export const TOPIC_CATEGORY_NAMES: Record<string, string> = {
  all: '全部',
  holiday_hw: '寒暑假作业',
  term_open: '开学备考',
  midterm_final: '期中期末',
  promotion: '升学冲刺',
  news: '时事热点',
  cross: '跨学科',
  project: '项目式学习',
}

export const TOPIC_REGION_NAMES: Record<string, string> = {
  all: '不限',
  chengdu: '成都',
  mianyang: '绵阳',
  sichuan: '四川其他',
}

export const TOPIC_GRADE_NAMES: Record<string, string> = {
  all: '全学段',
  preschool: '幼儿',
  primary: '小学',
  junior: '初中',
  senior: '高中',
}

export const TOPIC_FORM_NAMES: Record<string, string> = {
  exam: '试卷/真题',
  material: '讲义/作业',
  lesson_plan: '教案',
  ppt: '课件PPT',
  video: '视频',
  doc: 'PDF文档',
  exercise: '练习题',
  courseware: '课件',
  document: '文档',
}

/** 前端筛选 key → 专库 resourceForm */
export const TOPIC_FORMAT_API_MAP: Record<string, string> = {
  ppt: 'ppt',
  word: 'lesson_plan',
  pdf: 'doc',
  exam: 'exam',
  video: 'video',
  material: 'material',
}

export const TOPIC_TAB_KEYWORDS: Record<string, string> = {
  holiday_hw: '寒暑假 暑假作业 寒假作业',
  term_open: '开学备考 开学 收心',
  midterm_final: '期中期末 期中复习 期末冲刺',
  promotion: '升学冲刺 小升初 中考 高考',
  news: '时事热点',
  cross: '跨学科 融合',
  project: '项目式学习 PBL',
  stem: 'STEM',
  ai: '人工智能',
}

export function buildTopicListParams(opts: {
  tabKey: string
  tabKeywords: Record<string, string>
  region: string
  grade: string
  subject: string
  format: string
  level: string
  keyword?: string
  current?: number
  size?: number
  sortField?: string
  sortOrder?: 'desc' | 'asc'
}) {
  const parts: string[] = []
  if (opts.tabKey !== 'all' && opts.tabKey !== 'elite' && opts.tabKeywords[opts.tabKey]) {
    parts.push(opts.tabKeywords[opts.tabKey])
  }
  if (opts.region !== 'all' && TOPIC_REGION_KEYWORDS[opts.region]) {
    parts.push(TOPIC_REGION_KEYWORDS[opts.region])
  }
  if (opts.keyword?.trim()) {
    parts.push(opts.keyword.trim())
  }

  const formatItem = TOPIC_FORMATS.find((f) => f.key === opts.format)
  const levelItem = TOPIC_LEVELS.find((l) => l.key === opts.level)

  const params: Record<string, unknown> = {
    channelType: TOPIC_CHANNEL,
    current: opts.current ?? 1,
    size: opts.size ?? 12,
    sortField: opts.sortField ?? 'downloadCount',
    sortOrder: opts.sortOrder ?? 'desc',
  }

  if (opts.grade !== 'all') params.gradeLevel = opts.grade
  if (opts.subject !== 'all') params.subject = opts.subject
  if (formatItem && formatItem.key !== 'all') {
    if ('resourceType' in formatItem && formatItem.resourceType) {
      params.resourceType = formatItem.resourceType
    }
    if ('mediaType' in formatItem && formatItem.mediaType) {
      params.mediaType = formatItem.mediaType
    }
  }
  if (levelItem && 'isFree' in levelItem) {
    params.isFree = levelItem.isFree
  }
  if (parts.length) params.keyword = parts.join(' ')

  return params
}
