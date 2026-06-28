/**
 * 顶栏热门词 / 搜索框跳转配置
 * 展示文案与跳转行为分离：导航型不传 keyword，搜索型才传 keyword
 */
import type { StageKey } from '@/config/subjectConfig'

export type HotWordActionType = 'browse' | 'search'

/** 学科浏览页 query（与 ResourceBrowseQuery 对齐的子集） */
export interface HotWordBrowseQuery {
  brand?: string
  module?: string
  volume?: string
  keyword?: string
  type?: string
  node?: string
}

interface HotWordActionBase {
  /** 顶栏展示文案，也是搜索框精确匹配的 key */
  label: string
  stage: StageKey
  subjectKey?: string
  versionKey?: string
  /** 册别展示名，如「一年级上册」；解析为 volumeDataMap.id */
  volumeName?: string
}

/** 导航型：进入学科浏览页，按栏目/册别/系列筛选，不用标题 keyword */
export interface HotWordBrowseAction extends HotWordActionBase {
  type: 'browse'
  query?: HotWordBrowseQuery
}

/** 搜索型：在指定范围内用 keyword 搜标题 */
export interface HotWordSearchAction extends HotWordActionBase {
  type: 'search'
  module?: string
  /** 传给 API 的 keyword；默认用 label */
  keyword?: string
}

export type HotWordAction = HotWordBrowseAction | HotWordSearchAction

/** 首页顶栏热门词（顺序即展示顺序） */
export const HOT_WORD_ACTIONS: HotWordAction[] = [
  {
    type: 'browse',
    label: '一年级语文',
    stage: 'primary',
    subjectKey: 'chinese',
    versionKey: 'tongbian2024',
    volumeName: '一年级上册',
    query: { module: '同步备课' },
  },
  {
    type: 'browse',
    label: '期中试卷',
    stage: 'primary',
    subjectKey: 'chinese',
    versionKey: 'tongbian2024',
    volumeName: '一年级上册',
    query: { module: '期中' },
  },
  {
    type: 'search',
    label: '教案模板',
    stage: 'primary',
    subjectKey: 'chinese',
    versionKey: 'tongbian2024',
    volumeName: '一年级上册',
    module: '同步备课',
    keyword: '教案',
  },
  {
    type: 'browse',
    label: '中考复习',
    stage: 'junior',
    subjectKey: 'chinese',
    versionKey: 'tongbian2024',
    volumeName: '九年级下册',
    query: { module: '中考真题' },
  },
  {
    type: 'browse',
    label: '高考真题',
    stage: 'senior',
    subjectKey: 'chinese',
    versionKey: 'tongbian2024',
    volumeName: '选择性必修二',
    query: { module: '高考真题' },
  },
]

const hotWordMap = new Map<string, HotWordAction>(
  HOT_WORD_ACTIONS.map((item) => [item.label, item]),
)

/** 顶栏热门行展示文案列表 */
export const HOT_WORD_LABELS = HOT_WORD_ACTIONS.map((item) => item.label)

export function findHotWordAction(input: string): HotWordAction | undefined {
  const key = input.trim()
  if (!key) return undefined
  return hotWordMap.get(key)
}
