/**
 * Phase 5-E / Phase 2：分类维度 API 统一读源（带内存缓存 + 常量兜底）
 *
 * 约定：API 优先 → 空则 fallback → 仍空则返回 []（调用方禁用控件并提示）
 * C 端默认 includeDisabled=false；禁用项由后端提交时再校验。
 */
import { ref } from 'vue'
import {
  taxonomyApi,
  type TaxonomyEdition,
  type TaxonomyModule,
  type TaxonomyResourceType,
  type TaxonomyStage,
  type TaxonomySubject,
  type TaxonomyVolume,
} from '@/api/taxonomy'
import {
  gradesByGrade,
  textbookVersionsByGrade,
  type VersionGroup,
} from '@/constants/uploadOptions'
import { LESSON_TYPE_ORDER } from '@/composables/useLessonHub'
import {
  columnConfig,
  stages,
  subjectDataMap,
  subjectVersionsMap,
  type Stage,
  type StageKey,
  type SubjectItem,
} from '@/config/subjectConfig'
import { volumeDataMap, type VolumeItem } from '@/config/volumeData'
import { USE_TAXONOMY_API } from '@/config/featureFlags'

/** 本轮加载是否触发了离线兜底（API 异常或空响应后回退常量） */
export const taxonomyUsingOfflineFallback = ref(false)

export function resetTaxonomyOfflineFallback() {
  taxonomyUsingOfflineFallback.value = false
}

function markTaxonomyOfflineFallback() {
  taxonomyUsingOfflineFallback.value = true
}

const CACHE_TTL_MS = 5 * 60 * 1000

type CacheEntry<T> = { at: number; data: T }

export interface TaxonomyCodeName {
  code: string
  name: string
}

export interface TaxonomyEditionItem extends TaxonomyCodeName {
  publisher?: string
  shortName?: string
}

export interface TaxonomyEditionGroup {
  publisher: string
  editions: TaxonomyEditionItem[]
}

let stageCache: CacheEntry<Stage[]> | null = null
const subjectCache = new Map<string, CacheEntry<SubjectItem[]>>()
const gradeCache = new Map<string, CacheEntry<TaxonomyCodeName[]>>()
const volumeCache = new Map<string, CacheEntry<VolumeItem[]>>()
const editionCache = new Map<string, CacheEntry<TaxonomyEditionItem[]>>()
const moduleCache = new Map<string, CacheEntry<TaxonomyCodeName[]>>()
const resourceTypeCache = new Map<string, CacheEntry<TaxonomyCodeName[]>>()

function isFresh<T>(entry: CacheEntry<T> | undefined | null): entry is CacheEntry<T> {
  return !!entry && Date.now() - entry.at < CACHE_TTL_MS
}

function cacheKey(...parts: (string | undefined)[]): string {
  return parts.map((p) => (p ?? '').trim()).join('|')
}

function fallbackStages(): Stage[] {
  return stages.map((s) => ({ key: s.key, name: s.name }))
}

function fallbackSubjects(stageKey: string): SubjectItem[] {
  return subjectDataMap[stageKey as StageKey] || []
}

function fallbackVolumes(stageKey: string): VolumeItem[] {
  return volumeDataMap[stageKey as StageKey] || volumeDataMap.primary
}

function fallbackGrades(stageKey: string): TaxonomyCodeName[] {
  const fromUpload = gradesByGrade[stageKey as keyof typeof gradesByGrade]
  if (fromUpload?.length) {
    return fromUpload.map((g) => ({ code: g.value, name: g.label }))
  }
  return []
}

/** 教材版本分组离线兜底（仅 taxonomySource catch 分支使用） */
export function fallbackTextbookVersionGroups(stageKey: string): VersionGroup[] {
  const list = textbookVersionsByGrade[stageKey as keyof typeof textbookVersionsByGrade]
  return list?.length ? [...list] : []
}

function fallbackModules(stageKey: string): TaxonomyCodeName[] {
  const names = columnConfig[stageKey as StageKey] || columnConfig.primary
  return names.map((name) => ({ code: name, name }))
}

function fallbackEditions(subjectCode: string): TaxonomyEditionItem[] {
  const list =
    subjectVersionsMap[subjectCode] ||
    subjectVersionsMap.chinese ||
    subjectVersionsMap.default ||
    []
  return list.map((v) => ({ code: v.key, name: v.name }))
}

function fallbackResourceTypes(): TaxonomyCodeName[] {
  return LESSON_TYPE_ORDER.filter((t) => t !== '其他').map((name) => ({
    code: name,
    name,
  }))
}

function mapEdition(row: TaxonomyEdition): TaxonomyEditionItem {
  return {
    code: row.code || row.name,
    name: row.name,
    publisher: row.publisher,
    shortName: row.shortName,
  }
}

function mapModule(row: TaxonomyModule): TaxonomyCodeName {
  return {
    code: row.code || row.name,
    name: row.name,
  }
}

function mapResourceType(row: TaxonomyResourceType): TaxonomyCodeName {
  return {
    code: row.code || row.name,
    name: row.name,
  }
}

/** 学段列表（上传页 / 浏览页共用） */
export async function loadStages(): Promise<Stage[]> {
  if (!USE_TAXONOMY_API) return fallbackStages()
  if (isFresh(stageCache) && stageCache.data.length) return stageCache.data
  try {
    const list = await taxonomyApi.getStages()
    if (list.length) {
      const mapped = list.map((s: TaxonomyStage) => ({ key: s.code, name: s.name }))
      stageCache = { at: Date.now(), data: mapped }
      return mapped
    }
    markTaxonomyOfflineFallback()
  } catch {
    markTaxonomyOfflineFallback()
  }
  const fallback = fallbackStages()
  stageCache = { at: Date.now(), data: fallback }
  return fallback
}

/** 学科列表 */
export async function loadSubjects(stageKey: string): Promise<SubjectItem[]> {
  if (!stageKey) return []
  if (!USE_TAXONOMY_API) return fallbackSubjects(stageKey)
  const cached = subjectCache.get(stageKey)
  if (isFresh(cached)) return cached!.data
  try {
    const list = await taxonomyApi.getSubjects(stageKey)
    if (list.length) {
      const mapped = list.map((s: TaxonomySubject) => ({
        key: s.code,
        name: s.name,
      }))
      subjectCache.set(stageKey, { at: Date.now(), data: mapped })
      return mapped
    }
    markTaxonomyOfflineFallback()
  } catch {
    markTaxonomyOfflineFallback()
  }
  const fallback = fallbackSubjects(stageKey)
  subjectCache.set(stageKey, { at: Date.now(), data: fallback })
  return fallback
}

/** 年级列表（code 与 edu_grade.code 对齐，如 grade1） */
export async function loadGrades(stageKey: string): Promise<TaxonomyCodeName[]> {
  if (!stageKey) return []
  const key = cacheKey(stageKey)
  const cached = gradeCache.get(key)
  if (isFresh(cached)) return cached!.data
  if (USE_TAXONOMY_API) {
    try {
      const list = await taxonomyApi.getGrades(stageKey)
      if (list.length) {
        const mapped = list.map((g) => ({ code: g.code, name: g.name }))
        gradeCache.set(key, { at: Date.now(), data: mapped })
        return mapped
      }
      markTaxonomyOfflineFallback()
    } catch {
      markTaxonomyOfflineFallback()
    }
  }
  const fallback = fallbackGrades(stageKey)
  if (fallback.length) {
    gradeCache.set(key, { at: Date.now(), data: fallback })
  }
  return fallback
}

/** 教材版本（stage + subject code 或中文名） */
export async function loadEditions(
  stageKey: string,
  subjectCode: string,
): Promise<TaxonomyEditionItem[]> {
  if (!stageKey || !subjectCode) return []
  const key = cacheKey(stageKey, subjectCode)
  const cached = editionCache.get(key)
  if (isFresh(cached)) return cached!.data
  if (USE_TAXONOMY_API) {
    try {
      const list = await taxonomyApi.getEditions(stageKey, subjectCode)
      if (list.length) {
        const mapped = list.map(mapEdition)
        editionCache.set(key, { at: Date.now(), data: mapped })
        return mapped
      }
      markTaxonomyOfflineFallback()
    } catch {
      markTaxonomyOfflineFallback()
    }
  }
  const fallback = fallbackEditions(subjectCode)
  if (fallback.length) {
    editionCache.set(key, { at: Date.now(), data: fallback })
  }
  return fallback
}

/** 版本按出版社分组（基于 loadEditions 结果） */
export async function loadEditionGroups(
  stageKey: string,
  subjectCode: string,
): Promise<TaxonomyEditionGroup[]> {
  const editions = await loadEditions(stageKey, subjectCode)
  const groups = new Map<string, TaxonomyEditionItem[]>()
  for (const edition of editions) {
    const publisher = edition.publisher?.trim() || '其他'
    const bucket = groups.get(publisher)
    if (bucket) {
      bucket.push(edition)
    } else {
      groups.set(publisher, [edition])
    }
  }
  return Array.from(groups.entries()).map(([publisher, items]) => ({
    publisher,
    editions: items,
  }))
}

/** 册别列表（name 与 volumeDataMap 对齐） */
export async function loadVolumes(stageKey: string): Promise<VolumeItem[]> {
  if (!stageKey) return []
  const cached = volumeCache.get(stageKey)
  if (isFresh(cached)) return cached!.data
  if (USE_TAXONOMY_API) {
    try {
      const list = await taxonomyApi.getVolumes(stageKey)
      if (list.length) {
        const mapped = list.map((v: TaxonomyVolume, i: number) => ({
          id: v.code || `vol_${i + 1}`,
          name: v.name,
          isNew: false,
        }))
        volumeCache.set(stageKey, { at: Date.now(), data: mapped })
        return mapped
      }
      markTaxonomyOfflineFallback()
    } catch {
      markTaxonomyOfflineFallback()
    }
  }
  const fallback = fallbackVolumes(stageKey)
  volumeCache.set(stageKey, { at: Date.now(), data: fallback })
  return fallback
}

/** 册别名称列表 */
export async function loadVolumeNames(stageKey: string): Promise<string[]> {
  const volumes = await loadVolumes(stageKey)
  return volumes.map((v) => v.name)
}

/** 栏目列表（含 code） */
export async function loadModules(stageKey: string): Promise<TaxonomyCodeName[]> {
  if (!stageKey) return []
  const cached = moduleCache.get(stageKey)
  if (isFresh(cached)) return cached!.data
  if (USE_TAXONOMY_API) {
    try {
      const list = await taxonomyApi.getModules(stageKey)
      if (list.length) {
        const mapped = list.map(mapModule).filter((m) => m.name)
        moduleCache.set(stageKey, { at: Date.now(), data: mapped })
        return mapped
      }
      markTaxonomyOfflineFallback()
    } catch {
      markTaxonomyOfflineFallback()
    }
  }
  const fallback = fallbackModules(stageKey)
  moduleCache.set(stageKey, { at: Date.now(), data: fallback })
  return fallback
}

/** 栏目名称列表（兼容旧调用） */
export async function loadModuleNames(stageKey: string): Promise<string[]> {
  const modules = await loadModules(stageKey)
  return modules.map((m) => m.name).filter(Boolean)
}

/** 资源类型列表（含 code；subject/module 支持 code 或中文名） */
export async function loadResourceTypes(
  stageKey: string,
  subjectCodeOrName?: string,
  moduleCodeOrName?: string,
): Promise<TaxonomyCodeName[]> {
  if (!stageKey) return []
  const key = cacheKey(stageKey, subjectCodeOrName, moduleCodeOrName)
  const cached = resourceTypeCache.get(key)
  if (isFresh(cached)) return cached!.data
  if (USE_TAXONOMY_API) {
    try {
      const list = await taxonomyApi.getResourceTypes(
        stageKey,
        subjectCodeOrName,
        moduleCodeOrName,
      )
      if (list.length) {
        const mapped = list.map(mapResourceType).filter((t) => t.name)
        resourceTypeCache.set(key, { at: Date.now(), data: mapped })
        return mapped
      }
      markTaxonomyOfflineFallback()
    } catch {
      markTaxonomyOfflineFallback()
    }
  }
  const fallback = fallbackResourceTypes()
  resourceTypeCache.set(key, { at: Date.now(), data: fallback })
  return fallback
}

/** 资源类型名称列表（兼容旧调用） */
export async function loadResourceTypeNames(
  stageKey: string,
  subjectCodeOrName?: string,
  moduleCodeOrName?: string,
): Promise<string[]> {
  const types = await loadResourceTypes(stageKey, subjectCodeOrName, moduleCodeOrName)
  return types.map((t) => t.name).filter(Boolean)
}

/** 管理端保存 taxonomy 后可调用；否则依赖 5min TTL */
export function invalidateTaxonomyCache() {
  stageCache = null
  subjectCache.clear()
  gradeCache.clear()
  volumeCache.clear()
  editionCache.clear()
  moduleCache.clear()
  resourceTypeCache.clear()
}
