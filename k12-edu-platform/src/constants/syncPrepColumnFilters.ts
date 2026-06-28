import { mapDisplayTypeToQuery, displayTypeStatsFallbackKey } from '@/utils/resourceDisplayType'

/** 默认布局资源类型 Tab（非考试/专题栏目共用） */
export const DEFAULT_RESOURCE_TYPE_TABS = [
  '全部',
  '课件',
  '教案',
  '练习',
  '学案',
  '电子课本',
  '教学反思',
  '音频/朗读',
  '视频',
  '图片素材',
] as const

/** 仅「同步备课」栏目额外展示的类型 Tab */
export const SYNC_PREP_EXTRA_TYPE_TABS = ['精彩片段', '课文相关图片'] as const

export const SYNC_PREP_COLUMN = '同步备课'

/** 类型栏「更多」区展示的小众资源类型（与 resourceConstants.TEACHER_BOOK_TYPES 对齐） */
export const MORE_TYPE_TABS = [
  '课堂实录',
  '讲义',
  '说课稿',
  '教学设计',
  '预习',
  '教学总结',
  '逐字稿',
  '公开课',
] as const

/** 按栏目返回类型 Tab 列表 */
export function resourceTypeTabsForColumn(column: string): string[] {
  const base = [...DEFAULT_RESOURCE_TYPE_TABS]
  if (column !== SYNC_PREP_COLUMN) return base

  const idx = base.indexOf('视频')
  const insertAt = idx >= 0 ? idx + 1 : base.length - 1
  return [
    ...base.slice(0, insertAt),
    ...SYNC_PREP_EXTRA_TYPE_TABS,
    ...base.slice(insertAt),
  ]
}

/** 将同步备课「类型」Tab 转为 browse API 的 type / subType（与后端 displayType 对齐） */
export function mapSyncPrepTypeToQuery(
  selectedType: string,
): { type?: string; subType?: string } {
  return mapDisplayTypeToQuery(selectedType)
}

export function isSyncPrepOnlyTab(tab: string): boolean {
  return (SYNC_PREP_EXTRA_TYPE_TABS as readonly string[]).includes(tab)
}

/** 统计接口 displayType 聚合时，为 Tab 提供角标兜底 key */
export function syncPrepTabCountFallbackKey(tab: string): string | undefined {
  return displayTypeStatsFallbackKey(tab)
}

/** 上传页：同步备课额外 Tab → 入库 type / subType / 表单 resourceType */
export function resolveSyncPrepUploadFields(selectedType: string): {
  apiType: string
  subType?: string
  resourceType?: string
} | null {
  const q = mapSyncPrepTypeToQuery(selectedType)
  if (!q.type || q.type === selectedType) return null
  if (selectedType === '精彩片段') {
    return { apiType: q.type, subType: q.subType, resourceType: 'video' }
  }
  if (selectedType === '课文相关图片') {
    return { apiType: q.type, subType: q.subType, resourceType: 'document' }
  }
  return null
}

export function teachingTypeForUpload(
  selectedType: string,
  module: string,
): string {
  if (!selectedType || selectedType === '全部') return '课件'
  if (module === SYNC_PREP_COLUMN) {
    const extra = resolveSyncPrepUploadFields(selectedType)
    if (extra) return extra.apiType
  }
  return selectedType
}
