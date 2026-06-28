/**
 * 考试类栏目（开学专区 / 月考 / 期中 / 期末等）类型 Tab 与地区配置
 */

export const EXAM_COLUMNS = [
  '开学专区',
  '月考',
  '期中',
  '期末',
  '小升初真题',
  '小升初模拟',
] as const

export type ExamColumnName = (typeof EXAM_COLUMNS)[number]

/** 专题/真题类布局：与「真题汇编」同款（扩展筛选 + 列表区，无左侧教材目录） */
export const TOPIC_LAYOUT_COLUMNS = [
  '专题复习',
  '真题汇编',
  '暑假',
  '寒假',
  '阅读',
  '竞赛',
  '学业水平',
  '一轮复习',
  '二轮专题',
  '三轮冲刺',
  '中考模拟',
  '中考真题',
  '高考模拟',
  '高考真题',
] as const

export type TopicLayoutColumnName = (typeof TOPIC_LAYOUT_COLUMNS)[number]

/** 各栏目「类型」Tab 文案（首项均为「全部」） */
export const EXAM_TYPE_BY_COLUMN: Record<string, string[]> = {
  开学专区: ['全部', '开学考', '模拟卷', '开学第一课', '教学计划'],
  月考: ['全部', '真题', '真题汇编', '单元测试', '知识点', '专项练习', '模拟卷', '培优拔尖'],
  期中: [
    '全部',
    '模拟卷',
    '真题',
    '真题汇编',
    '考点精练',
    '培优拔尖',
    '强化突破',
    '易错题分',
    '重难点',
    '必刷卷',
    '巩固练习',
  ],
  期末: [
    '全部',
    '模拟卷',
    '真题',
    '考点精练',
    '真题汇编',
    '培优拔尖',
    '强化突破',
    '易错',
    '题分',
    '重难点',
    '必刷卷',
    '巩固练习',
  ],
  小升初真题: ['全部', '真题', '真题汇编', '模拟卷', '专项练习', '培优拔尖'],
  小升初模拟: ['全部', '真题', '真题汇编', '模拟卷', '专项练习', '培优拔尖'],
  学业水平: ['全部', '真题', '模拟卷', '专项练习', '考点精练'],
  一轮复习: ['全部', '知识点', '专项练习', '模拟卷', '真题汇编'],
  二轮专题: ['全部', '专题突破', '易错题', '重难点', '模拟卷'],
  三轮冲刺: ['全部', '模拟卷', '真题', '押题卷', '查漏补缺'],
  中考模拟: ['全部', '模拟卷', '真题汇编', '专项练习', '培优拔尖'],
  中考真题: ['全部', '真题', '真题汇编', '模拟卷', '专项练习'],
  高考模拟: ['全部', '模拟卷', '真题汇编', '专项练习', '培优拔尖'],
  高考真题: ['全部', '真题', '真题汇编', '模拟卷', '专项练习'],
  真题汇编: ['全部', '真题汇编', '真题', '模拟卷', '专项练习'],
}

const DEFAULT_EXAM_TYPES = [
  '全部',
  '真题',
  '真题汇编',
  '单元测试',
  '知识点',
  '专项练习',
  '模拟卷',
  '培优拔尖',
]

/** 考试类栏目统一：四川片区（以成都为中心） */
export const CHENGDU_AREA_REGIONS = ['全部', '四川省', '成都市', '绵阳市', '德阳市', '彭州市']

/** @deprecated 使用 CHENGDU_AREA_REGIONS */
export const SCHOOL_OPENING_REGIONS = CHENGDU_AREA_REGIONS

/** 专题类栏目：全国省份（展示行 + 更多） */
export const NATIONAL_REGIONS_ALL = [
  '全部',
  '北京市',
  '天津市',
  '河北省',
  '山西省',
  '内蒙古自治区',
  '辽宁省',
  '吉林省',
  '黑龙江省',
  '上海市',
  '江苏省',
  '浙江省',
  '安徽省',
  '福建省',
  '江西省',
  '山东省',
  '四川省',
  '广东省',
]

export const NATIONAL_REGIONS_DISPLAY = NATIONAL_REGIONS_ALL.slice(0, 14)

/** 考试类栏目默认选中成都片区 */
export const CHENGDU_AREA_DEFAULT_REGION = '成都市'

/** @deprecated 使用 CHENGDU_AREA_DEFAULT_REGION */
export const SCHOOL_OPENING_DEFAULT_REGION = CHENGDU_AREA_DEFAULT_REGION

export interface ExamBrowseQuery {
  type?: string
  subType?: string
  keyword?: string
  edition?: string
}

/** Tab 文案 → 主类型（少数项与 sub_type 区分） */
const TYPE_AS_MAIN: Record<string, string> = {
  知识点: '知识点',
  课件: '课件',
  教案: '教案',
  练习: '练习',
  试卷: '试卷',
  学案: '学案',
  视频: '视频',
}

/**
 * 将考试布局「类型」Tab 转为 API 的 type / subType
 * 细项（真题、培优拔尖等）优先走 subType，与入库 sub_type 一致
 */
export function mapExamTypeToQuery(selectedType: string): Pick<ExamBrowseQuery, 'type' | 'subType'> {
  if (!selectedType || selectedType === '全部') {
    return {}
  }
  if (selectedType === '知识点') {
    return { type: '知识点' }
  }
  if (selectedType === '开学第一课') {
    return { type: '课件', subType: '开学第一课' }
  }
  if (selectedType === '教学计划') {
    return { type: '教案', subType: '教学计划' }
  }
  if (TYPE_AS_MAIN[selectedType]) {
    return { type: TYPE_AS_MAIN[selectedType] }
  }
  return { subType: selectedType }
}

export function getExamTypeList(column: string): string[] {
  return EXAM_TYPE_BY_COLUMN[column] ?? DEFAULT_EXAM_TYPES
}

/** 月考 / 期中 / 期末 / 开学专区 / 小升初等考试栏目均使用成都片区 */
export function usesSichuanRegions(column: string): boolean {
  return (EXAM_COLUMNS as readonly string[]).includes(column)
}

/** 专题/真题类栏目使用全国省份筛选 */
export function usesTopicLayout(column: string): boolean {
  return (TOPIC_LAYOUT_COLUMNS as readonly string[]).includes(column)
}

export function getRegionLists(column: string): {
  display: string[]
  more: string[]
  defaultRegion: string
} {
  if (usesSichuanRegions(column)) {
    return {
      display: CHENGDU_AREA_REGIONS,
      more: [],
      defaultRegion: CHENGDU_AREA_DEFAULT_REGION,
    }
  }
  return {
    display: NATIONAL_REGIONS_DISPLAY,
    more: NATIONAL_REGIONS_ALL.filter((r) => !NATIONAL_REGIONS_DISPLAY.includes(r)),
    defaultRegion: '全部',
  }
}

/**
 * 合并考试/专题布局筛选为 browse 查询参数
 */
export function resolveExamBrowseParams(opts: {
  column: string
  selectedType: string
  selectedRegion: string
  selectedVersion: string
  searchKeyword?: string
  layoutUsesExamFilters: boolean
}): ExamBrowseQuery | null {
  if (!opts.layoutUsesExamFilters) {
    return null
  }

  const { type, subType } = mapExamTypeToQuery(opts.selectedType)
  const parts: string[] = []
  const kw = opts.searchKeyword?.trim()
  if (kw) parts.push(kw)
  if (opts.selectedRegion && opts.selectedRegion !== '全部') {
    parts.push(opts.selectedRegion)
  }

  const edition =
    opts.selectedVersion && opts.selectedVersion !== '全部'
      ? opts.selectedVersion
      : undefined

  return {
    type,
    subType,
    keyword: parts.length ? parts.join(' ') : undefined,
    edition,
  }
}
