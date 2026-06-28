/** 竞赛专区：学段、学科、资源形态映射（方案1 主站 resource API） */

export const COMPETITION_CHANNEL = 'jingsai'

export const COMPETITION_BASE_TAG = '学科竞赛'

export const COMPETITION_CATEGORY_NAMES: Record<string, string> = {
  all: '全部',
  olympiad: '学科奥赛',
  primary_math: '小学奥数',
  math: '数学竞赛',
  physics: '物理竞赛',
  chemistry: '化学竞赛',
  biology: '生物竞赛',
  info: '信息学竞赛',
  writing: '作文大赛',
  english: '英语竞赛',
  cert: '考级证书',
  exam_prep: '考前辅导',
  elite: '精品专辑',
}

export const COMPETITION_GRADE_NAMES: Record<string, string> = {
  all: '全学段',
  primary: '小学',
  junior: '初中',
  senior: '高中',
}

export const COMPETITION_FORM_NAMES: Record<string, string> = {
  exam: '真题试卷',
  mock: '模拟卷',
  lecture: '讲义',
  lesson_plan: '教案',
  ppt: '课件PPT',
  video: '视频',
  doc: '文档',
  exercise: '练习题',
}

export const COMPETITION_GRADE_STAGES = [
  { key: 'all', name: '不限' },
  { key: 'primary', name: '小学' },
  { key: 'junior', name: '初中' },
  { key: 'senior', name: '高中' },
] as const

export const COMPETITION_SUBJECTS = [
  { key: 'all', name: '不限' },
  { key: 'math', name: '数学' },
  { key: 'physics', name: '物理' },
  { key: 'chemistry', name: '化学' },
  { key: 'biology', name: '生物' },
  { key: 'info', name: '信息学' },
  { key: 'chinese', name: '语文/作文' },
  { key: 'english', name: '英语' },
] as const

/** 侧边栏学科 key → 后端 subject 字段（info 走关键词，不设 subject） */
export const COMPETITION_SUBJECT_API: Record<string, string | undefined> = {
  math: 'math',
  physics: 'physics',
  chemistry: 'chemistry',
  biology: 'biology',
  chinese: 'chinese',
  english: 'english',
}

export const COMPETITION_FORMATS = [
  { key: 'all', name: '不限' },
  { key: 'ppt', name: 'PPT课件', resourceType: 'courseware' },
  { key: 'word', name: 'Word/教案', resourceType: 'lesson_plan' },
  { key: 'pdf', name: 'PDF/讲义', mediaType: 'document' },
  { key: 'exam', name: '试卷/真题', resourceType: 'exam' },
  { key: 'video', name: '视频', resourceType: 'video', mediaType: 'video' },
] as const

export const COMPETITION_LEVELS = [
  { key: 'all', name: '不限' },
  { key: 'free', name: '免费', isFree: 1 },
  { key: 'paid', name: '会员精品', isFree: 0 },
] as const

export function buildCompetitionListParams(opts: {
  tabKey: string
  tabKeywords: Record<string, string>
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
  if (opts.tabKey !== 'all' && opts.tabKeywords[opts.tabKey]) {
    parts.push(opts.tabKeywords[opts.tabKey])
  }
  if (opts.subject === 'info') {
    parts.push('信息学 NOIP CSP 编程')
  }
  if (opts.keyword?.trim()) {
    parts.push(opts.keyword.trim())
  }

  const formatItem = COMPETITION_FORMATS.find((f) => f.key === opts.format)
  const levelItem = COMPETITION_LEVELS.find((l) => l.key === opts.level)

  const params: Record<string, unknown> = {
    channelType: COMPETITION_CHANNEL,
    current: opts.current ?? 1,
    size: opts.size ?? 12,
    sortField: opts.sortField ?? 'createTime',
    sortOrder: opts.sortOrder ?? 'desc',
  }

  if (opts.grade !== 'all') params.gradeLevel = opts.grade
  const apiSubject = COMPETITION_SUBJECT_API[opts.subject]
  if (apiSubject) params.subject = apiSubject
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
  if (parts.length) params.keyword = parts.join(',')

  return params
}
