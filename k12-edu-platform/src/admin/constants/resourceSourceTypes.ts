/** 统一资源主域 sourceType（与 v_admin_resource_main / resource_main 一致） */
export interface ResourceSourceTypeOption {
  label: string
  value: string
  tagType?: 'success' | 'primary' | 'warning' | 'info' | 'danger'
}

export const RESOURCE_SOURCE_TYPE_OPTIONS: ResourceSourceTypeOption[] = [
  { label: '学科资源', value: 'primary_chinese' },
  { label: '专题资源', value: 'topic_resource', tagType: 'primary' },
  { label: '传统文化', value: 'culture_resource', tagType: 'warning' },
  { label: '竞赛资源', value: 'competition_resource', tagType: 'success' },
  { label: '通用资源', value: 'edu_resource', tagType: 'info' },
]

const labelMap = Object.fromEntries(
  RESOURCE_SOURCE_TYPE_OPTIONS.map((o) => [o.value, o.label]),
) as Record<string, string>

const tagMap = Object.fromEntries(
  RESOURCE_SOURCE_TYPE_OPTIONS.filter((o) => o.tagType).map((o) => [o.value, o.tagType!]),
) as Record<string, 'success' | 'primary' | 'warning' | 'info' | 'danger'>

export function resourceSourceTypeLabel(sourceType?: string): string {
  if (!sourceType) return '-'
  return labelMap[sourceType] || sourceType
}

export function resourceSourceTypeTagType(
  sourceType?: string,
): 'success' | 'primary' | 'warning' | 'info' | 'danger' | undefined {
  if (!sourceType) return 'info'
  return tagMap[sourceType] ?? 'info'
}
