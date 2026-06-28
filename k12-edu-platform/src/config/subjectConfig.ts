/**
 * 学段/学科配置
 * 集中管理学段定义、学科数据、名称映射等静态配置
 */

// ===== 学段配置 =====
export interface Stage {
  key: string
  name: string
}

export const stages: Stage[] = [
  { key: 'preschool', name: '幼儿' },
  { key: 'primary', name: '小学' },
  { key: 'junior', name: '初中' },
  { key: 'senior', name: '高中' },
  { key: 'art', name: '美术' },
  { key: 'dance', name: '舞蹈' },
]

export type StageKey = 'preschool' | 'primary' | 'junior' | 'senior' | 'art' | 'dance'

// ===== 学段名称映射 =====
export const stageNames: Record<StageKey, string> = {
  preschool: '幼儿',
  primary: '小学',
  junior: '初中',
  senior: '高中',
  art: '美术',
  dance: '舞蹈',
}

// ===== 学科数据（按学段分组） =====
export interface SubjectItem {
  key: string
  name: string
  isNew?: boolean
}

export const subjectDataMap: Record<StageKey, SubjectItem[]> = {
  preschool: [
    { key: 'chinese', name: '拼音识字', isNew: true },
    { key: 'math', name: '数学启蒙', isNew: true },
    { key: 'habit', name: '习惯养成', isNew: true },
    { key: 'activity', name: '综合活动', isNew: false },
  ],
  primary: [
    { key: 'chinese', name: '语文', isNew: false },
    { key: 'math', name: '数学', isNew: false },
    { key: 'english', name: '英语', isNew: true },
    { key: 'science', name: '科学', isNew: false },
    { key: 'politics', name: '道德与法治', isNew: false },
    { key: 'music', name: '音乐', isNew: false },
    { key: 'art', name: '美术', isNew: false },
    { key: 'pe', name: '体育', isNew: false },
  ],
  junior: [
    { key: 'chinese', name: '语文', isNew: false },
    { key: 'math', name: '数学', isNew: false },
    { key: 'english', name: '英语', isNew: false },
    { key: 'physics', name: '物理', isNew: false },
    { key: 'chemistry', name: '化学', isNew: false },
    { key: 'biology', name: '生物', isNew: false },
    { key: 'history', name: '历史', isNew: false },
    { key: 'geography', name: '地理', isNew: false },
    { key: 'politics', name: '政治', isNew: false },
  ],
  senior: [
    { key: 'chinese', name: '语文', isNew: false },
    { key: 'math', name: '数学', isNew: false },
    { key: 'english', name: '英语', isNew: false },
    { key: 'physics', name: '物理', isNew: false },
    { key: 'chemistry', name: '化学', isNew: false },
    { key: 'biology', name: '生物', isNew: false },
    { key: 'history', name: '历史', isNew: false },
    { key: 'geography', name: '地理', isNew: false },
    { key: 'politics', name: '政治', isNew: false },
  ],
  art: [
    { key: 'art', name: '美术', isNew: false },
  ],
  dance: [
    { key: 'dance', name: '舞蹈', isNew: false },
  ],
}

// ===== 版本映射 =====
// 与数据库 edu_edition 表保持一致（id, name, short_name, sort）
export interface VersionItem {
  id: number
  key: string
  name: string
  isNew?: boolean
}

// 数据库中的9个版本：统编版(2024)/人教版/北师大版/苏教版/沪教版/西师大版/语文版/冀教版/统编版(2016)
const allVersions: VersionItem[] = [
  { id: 1, key: 'tongbian2024', name: '统编版(2024)', isNew: true },
  { id: 2, key: 'renjiao', name: '人教版', isNew: true },
  { id: 3, key: 'beishida', name: '北师大版', isNew: false },
  { id: 4, key: 'sujiao', name: '苏教版', isNew: false },
  { id: 5, key: 'hujiao', name: '沪教版', isNew: false },
  { id: 6, key: 'xishida', name: '西师大版', isNew: false },
  { id: 7, key: 'yuwen', name: '语文版', isNew: false },
  { id: 8, key: 'jijiao', name: '冀教版', isNew: false },
  { id: 9, key: 'tongbian2016', name: '统编版(2016)', isNew: false },
]

export const subjectVersionsMap: Record<string, VersionItem[]> = {
  // 幼儿启蒙：以园本/机构课程和幼小衔接专项为主
  habit: allVersions.filter(v => ['tongbian2024', 'renjiao'].includes(v.key)),
  activity: allVersions.filter(v => ['tongbian2024', 'renjiao', 'beishida'].includes(v.key)),
  // 语文：所有版本都适用
  chinese: allVersions,
  // 数学：除语文版外都适用
  math: allVersions.filter(v => v.key !== 'yuwen'),
  // 英语：除语文版外都适用
  english: allVersions.filter(v => v.key !== 'yuwen'),
  // 科学：主流版本
  science: allVersions.filter(v => ['tongbian2024', 'renjiao', 'beishida', 'sujiao', 'hujiao'].includes(v.key)),
  // 道德与法治/政治：统编版为主
  politics: allVersions.filter(v => ['tongbian2024', 'tongbian2016', 'renjiao'].includes(v.key)),
  // 物理：主流版本
  physics: allVersions.filter(v => ['tongbian2024', 'renjiao', 'beishida', 'sujiao', 'hujiao'].includes(v.key)),
  // 化学：主流版本
  chemistry: allVersions.filter(v => ['tongbian2024', 'renjiao', 'beishida', 'sujiao', 'hujiao'].includes(v.key)),
  // 生物：主流版本
  biology: allVersions.filter(v => ['tongbian2024', 'renjiao', 'beishida', 'sujiao', 'hujiao'].includes(v.key)),
  // 历史：统编版为主
  history: allVersions.filter(v => ['tongbian2024', 'tongbian2016', 'renjiao', 'beishida'].includes(v.key)),
  // 地理：主流版本
  geography: allVersions.filter(v => ['tongbian2024', 'renjiao', 'beishida', 'sujiao', 'hujiao'].includes(v.key)),
  // 音乐：主流版本
  music: allVersions.filter(v => ['tongbian2024', 'renjiao', 'beishida', 'sujiao'].includes(v.key)),
  // 美术：主流版本
  art: allVersions.filter(v => ['tongbian2024', 'renjiao', 'beishida', 'sujiao'].includes(v.key)),
  // 体育：主流版本
  pe: allVersions.filter(v => ['tongbian2024', 'renjiao', 'beishida', 'sujiao'].includes(v.key)),
  // 默认：返回全部
  default: allVersions,
}

/** 七彩/小学：顶栏优先展示的栏目（其余栏目保留，排在后面） */
export const COLUMN_PRIORITY_PRIMARY: string[] = [
  '同步备课',
  '国学阅读',
  '期中复习',
  '期末复习',
]

/** 按品牌调整栏目顺序（不删项，仅排序；key 为空表示「全部系列」） */
export const columnOrderByBrand: Record<string, string[] | undefined> = {
  '': COLUMN_PRIORITY_PRIMARY,
  qicai: COLUMN_PRIORITY_PRIMARY,
  platform: COLUMN_PRIORITY_PRIMARY,
}

// ===== 栏目配置（按学段，顺序与产品一致） =====
export const columnConfig: Record<StageKey, string[]> = {
  preschool: [
    '拼音识字', '数学启蒙', '教学启蒙', '习惯养成',
    '暑假衔接', '幼小衔接', '绘本阅读', '表达与阅读', '综合活动', '家园共育',
  ],
  primary: [
    '同步备课', '国学阅读', '期中复习', '期末复习',
    '开学专区', '月考', '期中', '期末', '小升初真题', '小升初模拟',
    '专题复习', '真题汇编', '暑假', '寒假', '作文', '阅读', '竞赛',
    '教师工作包',
  ],
  junior: [
    '同步备课', '开学专区', '寒假', '作文', '阅读', '月考', '竞赛', '期中', '期末',
    '学业水平', '一轮复习', '二轮专题', '三轮冲刺', '中考模拟', '中考真题', '真题汇编', '暑假',
  ],
  senior: [
    '同步备课', '开学专区', '暑假', '作文', '阅读', '月考', '竞赛', '期中', '期末',
    '学业水平', '一轮复习', '二轮专题', '三轮冲刺', '高考模拟', '高考真题', '真题汇编', '寒假',
  ],
  art: [
    '同步备课', '开学专区', '月考', '期中', '期末',
    '专题复习', '暑假', '寒假', '竞赛',
  ],
  dance: [
    '同步备课', '开学专区', '月考', '期中', '期末',
    '专题复习', '寒假', '暑假', '竞赛',
  ],
}

/** 栏目图标（与 edu_module 种子数据对齐） */
export const columnIcons: Record<string, string> = {
  '同步备课': '📚',
  '拼音识字': '🔤',
  '数学启蒙': '🔢',
  '教学启蒙': '🌱',
  '习惯养成': '🌟',
  '暑假衔接': '☀️',
  '幼小衔接': '🏫',
  '绘本阅读': '📖',
  '表达与阅读': '💬',
  '综合活动': '🧩',
  '家园共育': '👨‍👩‍👧',
  '开学专区': '🏫',
  '月考': '📝',
  '期中': '📋',
  '期末': '📋',
  '小升初真题': '🎯',
  '小升初模拟': '🎯',
  '专题复习': '🔍',
  '真题汇编': '📜',
  '暑假': '☀️',
  '寒假': '❄️',
  '作文': '✍️',
  '阅读': '📖',
  '国学阅读': '📜',
  '教师工作包': '🧰',
  '期中复习': '📋',
  '期末复习': '📚',
  '竞赛': '🏆',
  '学业水平': '📊',
  '一轮复习': '1️⃣',
  '二轮专题': '2️⃣',
  '三轮冲刺': '3️⃣',
  '中考模拟': '📝',
  '中考真题': '📜',
  '高考模拟': '📝',
  '高考真题': '📜',
}

/** 按品牌重排栏目名（不删项） */
export function sortColumnsForBrand(names: string[], brandCode: string): string[] {
  const priority =
    columnOrderByBrand[brandCode] ?? columnOrderByBrand[''] ?? COLUMN_PRIORITY_PRIMARY
  const head = priority.filter((n) => names.includes(n))
  const tail = names.filter((n) => !priority.includes(n))
  return [...head, ...tail]
}

/** 小学顶栏优先栏目（用于高亮样式） */
export function isPriorityColumn(
  name: string,
  brandCode: string,
  stageKey: StageKey = 'primary',
): boolean {
  if (stageKey === 'preschool') {
    return ['拼音识字', '数学启蒙', '习惯养成', '暑假衔接'].includes(name)
  }
  if (stageKey !== 'primary') return name === '同步备课'
  const priority =
    columnOrderByBrand[brandCode] ?? columnOrderByBrand[''] ?? COLUMN_PRIORITY_PRIMARY
  return priority.includes(name)
}

export interface ColumnGroupDefinition {
  key: string
  label: string
  columns: string[]
}

/** 顶部直出栏目（其余通过“更多栏目”分组展示） */
export const topVisibleColumnsByStage: Record<StageKey, string[]> = {
  preschool: ['拼音识字', '数学启蒙', '教学启蒙', '习惯养成', '暑假衔接', '绘本阅读'],
  primary: ['同步备课', '国学阅读', '期中复习', '期末复习', '开学专区', '月考'],
  junior: ['同步备课', '开学专区', '月考', '期中', '期末', '中考真题', '真题汇编'],
  senior: ['同步备课', '开学专区', '月考', '期中', '期末', '高考真题'],
  art: ['同步备课', '开学专区', '月考', '期中', '期末'],
  dance: ['同步备课', '开学专区', '月考', '期中', '期末'],
}

/** 小学栏目分组（用于“更多栏目”面板） */
const primaryColumnGroups: ColumnGroupDefinition[] = [
  {
    key: 'stage_exam',
    label: '阶段检测',
    columns: ['期中', '期末'],
  },
  {
    key: 'promotion',
    label: '升学冲刺',
    columns: ['小升初真题', '小升初模拟', '真题汇编'],
  },
  {
    key: 'topic_expand',
    label: '专题拓展',
    columns: ['专题复习', '暑假', '寒假', '竞赛'],
  },
  {
    key: 'reading_writing',
    label: '阅读写作',
    columns: ['作文', '阅读'],
  },
  {
    key: 'teacher_support',
    label: '教师支持',
    columns: ['教师工作包'],
  },
]

/** 幼儿阶段栏目分组：围绕幼小衔接、启蒙认知、习惯与家园共育 */
const preschoolColumnGroups: ColumnGroupDefinition[] = [
  {
    key: 'bridge',
    label: '幼小衔接',
    columns: ['拼音识字', '数学启蒙', '教学启蒙', '暑假衔接', '幼小衔接'],
  },
  {
    key: 'habit',
    label: '习惯养成',
    columns: ['习惯养成', '综合活动'],
  },
  {
    key: 'literacy',
    label: '阅读表达',
    columns: ['绘本阅读', '表达与阅读'],
  },
  {
    key: 'home_school',
    label: '家园共育',
    columns: ['家园共育'],
  },
]

/** 初中栏目分组（参考小学「更多栏目」结构） */
const juniorColumnGroups: ColumnGroupDefinition[] = [
  {
    key: 'exam',
    label: '阶段检测',
    columns: ['开学专区', '月考', '期中', '期末'],
  },
  {
    key: 'review',
    label: '复习冲刺',
    columns: ['学业水平', '一轮复习', '二轮专题', '三轮冲刺'],
  },
  {
    key: 'exam_real',
    label: '真题与模拟',
    columns: ['中考模拟', '中考真题', '真题汇编'],
  },
  {
    key: 'topic_expand',
    label: '专题拓展',
    columns: ['暑假', '寒假', '竞赛'],
  },
  {
    key: 'reading_writing',
    label: '阅读写作',
    columns: ['作文', '阅读'],
  },
]

/** 其他学段的通用分组策略（按现有栏目语义收纳） */
const commonColumnGroups: ColumnGroupDefinition[] = [
  {
    key: 'exam',
    label: '阶段检测',
    columns: ['开学专区', '月考', '期中', '期末'],
  },
  {
    key: 'review',
    label: '复习冲刺',
    columns: ['学业水平', '一轮复习', '二轮专题', '三轮冲刺'],
  },
  {
    key: 'exam_real',
    label: '真题与模拟',
    columns: ['中考模拟', '中考真题', '高考模拟', '高考真题', '真题汇编'],
  },
  {
    key: 'topic',
    label: '专题拓展',
    columns: ['专题复习', '暑假', '寒假', '竞赛', '作文', '阅读'],
  },
]

const columnGroupsByStage: Record<StageKey, ColumnGroupDefinition[]> = {
  preschool: preschoolColumnGroups,
  primary: primaryColumnGroups,
  junior: juniorColumnGroups,
  senior: commonColumnGroups,
  art: commonColumnGroups,
  dance: commonColumnGroups,
}

export function getTopVisibleColumnsForStage(
  stageKey: StageKey,
  columns: string[],
): string[] {
  const preferred = topVisibleColumnsByStage[stageKey] || topVisibleColumnsByStage.primary
  const head = preferred.filter((name) => columns.includes(name))
  return head
}

export function groupColumnsForStage(
  stageKey: StageKey,
  columns: string[],
): ColumnGroupDefinition[] {
  const groupDefs = columnGroupsByStage[stageKey] || columnGroupsByStage.primary
  const used = new Set<string>()
  const groups: ColumnGroupDefinition[] = []

  for (const g of groupDefs) {
    const hit = g.columns.filter((name) => columns.includes(name))
    if (!hit.length) continue
    hit.forEach((name) => used.add(name))
    groups.push({
      key: g.key,
      label: g.label,
      columns: hit,
    })
  }

  const rest = columns.filter((name) => !used.has(name))
  if (rest.length) {
    groups.push({
      key: 'others',
      label: '其他栏目',
      columns: rest,
    })
  }
  return groups
}
