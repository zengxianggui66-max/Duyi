/**
 * 资源系列（品牌）配置 — M1 本地兜底，与 GET /api/brands 对齐
 */
export type DisplayMode = 'lesson_hub' | 'category_list' | 'unit_matrix'

export interface ResourceSeriesItem {
  /** 空字符串表示「全部系列」，API 不传 brandCode */
  code: string
  name: string
  publisher?: string
  defaultSchemeCode?: string
  displayMode?: DisplayMode
}

/** 默认选中系列 */
export const DEFAULT_BRAND_CODE = 'qicai'

/** Tab 展示顺序（含「全部」） */
export const RESOURCE_SERIES_TABS: ResourceSeriesItem[] = [
  { code: '', name: '全部系列', displayMode: 'lesson_hub' },
  {
    code: 'qicai',
    name: '七彩课堂',
    publisher: '时代华语出版社',
    defaultSchemeCode: 'textbook_unit',
    displayMode: 'lesson_hub',
  },
  {
    code: 'zhuangyuan',
    name: '状元版',
    publisher: '状元教育',
    defaultSchemeCode: 'zy_taxonomy',
    displayMode: 'category_list',
  },
  {
    code: 'platform',
    name: '平台自建',
    publisher: '平台',
    displayMode: 'lesson_hub',
  },
]

export function findSeriesByCode(code: string): ResourceSeriesItem | undefined {
  if (!code) return RESOURCE_SERIES_TABS.find((s) => s.code === '')
  return RESOURCE_SERIES_TABS.find((s) => s.code === code)
}

export function seriesNameByCode(code: string): string {
  return findSeriesByCode(code)?.name || ''
}

/** 传给列表 API：空/全部 → undefined */
export function brandCodeForApi(code: string): string | undefined {
  return code?.trim() ? code.trim() : undefined
}
