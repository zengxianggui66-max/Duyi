/**
 * 学科详情「栏目」列表区与上传页共用的资源属性标签
 * 与 AdvancedFilterBar / ExamColumnLayout / TopicColumnLayout 勾选一致
 */
import type { StageKey } from '@/config/subjectConfig'

export interface ResourceTagOption {
  value: string
  label: string
}

/** 同步备课等栏目列表页通用标签 */
export const CORE_BROWSE_RESOURCE_TAGS: ResourceTagOption[] = [
  { value: 'sync', label: '同步' },
  { value: 'quality', label: '精品' },
  { value: 'free', label: '免费' },
  { value: 'has_answer', label: '有答案' },
  { value: 'text_version', label: '文字版' },
]

/** 列表区 AdvancedFilterBar 勾选项（不含「同步」） */
export const BROWSE_LIST_FILTER_TAGS: ResourceTagOption[] = CORE_BROWSE_RESOURCE_TAGS.filter(
  (t) => t.value !== 'sync',
)

const STAGE_EXTRA_TAGS: Partial<Record<StageKey, ResourceTagOption[]>> = {
  art: [
    { value: 'exam_level', label: '考级' },
    { value: 'art_exam', label: '艺考' },
  ],
  dance: [
    { value: 'exam_level', label: '考级' },
    { value: 'art_exam', label: '艺考' },
  ],
}

const MODULE_EXTRA_TAGS: Record<string, ResourceTagOption[]> = {
  竞赛: [{ value: 'competition', label: '竞赛' }],
  专题复习: [{ value: 'review', label: '复习' }],
}

const ALL_TAG_OPTIONS: ResourceTagOption[] = [
  ...CORE_BROWSE_RESOURCE_TAGS,
  ...(STAGE_EXTRA_TAGS.art ?? []),
  ...(STAGE_EXTRA_TAGS.dance ?? []),
  ...Object.values(MODULE_EXTRA_TAGS).flat(),
]

export const RESOURCE_TAG_LABELS: Record<string, string> = Object.fromEntries(
  ALL_TAG_OPTIONS.map((t) => [t.value, t.label]),
)

export function getUploadResourceTagOptions(
  stageKey: string,
  module: string,
): ResourceTagOption[] {
  const extras: ResourceTagOption[] = []
  const stage = stageKey as StageKey
  if (stage && STAGE_EXTRA_TAGS[stage]) {
    extras.push(...(STAGE_EXTRA_TAGS[stage] ?? []))
  }
  if (module && MODULE_EXTRA_TAGS[module]) {
    extras.push(...MODULE_EXTRA_TAGS[module])
  }

  const seen = new Set<string>()
  const list: ResourceTagOption[] = []
  for (const tag of [...CORE_BROWSE_RESOURCE_TAGS, ...extras]) {
    if (seen.has(tag.value)) continue
    seen.add(tag.value)
    list.push(tag)
  }
  return list
}
