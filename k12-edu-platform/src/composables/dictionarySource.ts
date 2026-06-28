/**
 * Phase 5-E：字典/标签 API 统一读源（带缓存 + 常量兜底）
 */
import { dictionaryApi, type DictionaryItem } from '@/api/dictionary'
import {
  getUploadResourceTagOptions,
  RESOURCE_TAG_LABELS,
  type ResourceTagOption,
} from '@/constants/resourceBrowseTags'
import { examTypesByGrade } from '@/constants/uploadOptions'
import { EXAM_TYPE_LABELS } from '@/constants/uploadLabels'
import { USE_DICTIONARY_API } from '@/config/featureFlags'

const CACHE_TTL_MS = 5 * 60 * 1000

type CacheEntry<T> = { at: number; data: T }

let browseTagCache: CacheEntry<ResourceTagOption[]> | null = null
const browseTagKeyCache = new Map<string, CacheEntry<ResourceTagOption[]>>()
let examSceneCache: CacheEntry<{ label: string; value: string }[]> | null = null

function isFresh<T>(entry: CacheEntry<T> | null | undefined): entry is CacheEntry<T> {
  return !!entry && Date.now() - entry.at < CACHE_TTL_MS
}

function mapBrowseTags(list: DictionaryItem[]): ResourceTagOption[] {
  return list.map((d) => ({ value: d.code, label: d.name }))
}

/** 上传/浏览资源属性标签 */
export async function loadBrowseTagOptions(
  stageKey: string,
  module: string,
): Promise<ResourceTagOption[]> {
  const cacheKey = `${stageKey}|${module || ''}`
  const keyed = browseTagKeyCache.get(cacheKey)
  if (isFresh(keyed)) return keyed!.data

  if (USE_DICTIONARY_API) {
    try {
      const list = await dictionaryApi.listBrowseTags(stageKey || undefined, module || undefined)
      if (list.length) {
        const mapped = mapBrowseTags(list)
        browseTagKeyCache.set(cacheKey, { at: Date.now(), data: mapped })
        mergeTagLabels(mapped)
        return mapped
      }
    } catch {
      /* fallback */
    }
  }
  return getUploadResourceTagOptions(stageKey, module)
}

/** 全部浏览标签（不区分学段栏目，用于标签名解析） */
export async function loadAllBrowseTagLabels(): Promise<Record<string, string>> {
  if (isFresh(browseTagCache)) {
    return Object.fromEntries(browseTagCache!.data.map((t) => [t.value, t.label]))
  }
  if (USE_DICTIONARY_API) {
    try {
      const list = await dictionaryApi.listBrowseTags()
      if (list.length) {
        const mapped = mapBrowseTags(list)
        browseTagCache = { at: Date.now(), data: mapped }
        mergeTagLabels(mapped)
        return Object.fromEntries(mapped.map((t) => [t.value, t.label]))
      }
    } catch {
      /* fallback */
    }
  }
  return { ...RESOURCE_TAG_LABELS }
}

/** 考试/备考场景（上传页 examTypes） */
export async function loadExamTypeOptions(
  stageKey: string,
): Promise<{ label: string; value: string }[]> {
  if (isFresh(examSceneCache)) return examSceneCache!.data

  if (USE_DICTIONARY_API) {
    try {
      const list = await dictionaryApi.listExamScenes()
      if (list.length) {
        const mapped = list.map((d) => ({
          label: d.name,
          value: d.code,
        }))
        examSceneCache = { at: Date.now(), data: mapped }
        return mapped
      }
    } catch {
      /* fallback */
    }
  }

  const fallback =
    examTypesByGrade[stageKey as keyof typeof examTypesByGrade] ||
    examTypesByGrade.primary ||
    []
  examSceneCache = { at: Date.now(), data: fallback }
  return fallback
}

export function resolveExamTypeLabel(code: string, apiLabels?: Record<string, string>): string {
  if (apiLabels?.[code]) return apiLabels[code]
  return EXAM_TYPE_LABELS[code] || code
}

function mergeTagLabels(tags: ResourceTagOption[]) {
  for (const t of tags) {
    RESOURCE_TAG_LABELS[t.value] = t.label
  }
}

export function invalidateDictionaryCache() {
  browseTagCache = null
  browseTagKeyCache.clear()
  examSceneCache = null
}
