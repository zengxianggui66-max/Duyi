/**
 * 首页学科详情 — 版本列表
 * 与学科详情页 CourseCatalog 版本弹窗保持一致：以 subjectConfig 全量为准，
 * API 仅用于补充/校正「新」标识，不缩减列表（edu filter-options 仅有资源的版本会漏项）
 */
import { ref } from 'vue'
import { primaryChineseApi } from '@/api'
import { unwrapData } from '@/api/request'
import { stageNames, subjectVersionsMap, type StageKey } from '@/config/subjectConfig'
import {
  mapEditionNameToOption,
  mapEditionStrings,
  type EditionVersionOption,
} from '@/utils/editionAdapter'

const cache = new Map<string, EditionVersionOption[]>()

function cacheKey(stage: StageKey, subjectKey: string) {
  return `${stage}:${subjectKey}`
}

/** 与 useVersionVolume / CourseCatalog 相同数据源 */
function staticVersions(subjectKey: string): EditionVersionOption[] {
  const items = subjectVersionsMap[subjectKey] || subjectVersionsMap.default || []
  return items.map((v) => ({
    key: v.key,
    name: v.name,
    editionName: v.name,
    isNew: v.isNew,
    id: v.id,
  }))
}

/** 将 API 返回的版本名合并进列表（追加缺失项，不删除静态项） */
function mergeApiEditionNames(
  base: EditionVersionOption[],
  apiNames: string[],
): EditionVersionOption[] {
  if (!apiNames.length) return base

  const byKey = new Map(base.map((v) => [v.key, { ...v }]))
  const byEdition = new Map(base.map((v) => [v.editionName, v.key]))

  for (const raw of apiNames) {
    const name = String(raw ?? '').trim()
    if (!name) continue
    const opt = mapEditionNameToOption(name)
    const existingKey = byEdition.get(name) || byEdition.get(opt.editionName)
    if (existingKey && byKey.has(existingKey)) {
      const cur = byKey.get(existingKey)!
      if (opt.isNew) cur.isNew = true
      continue
    }
    if (!byKey.has(opt.key)) {
      byKey.set(opt.key, opt)
      byEdition.set(opt.editionName, opt.key)
    }
  }

  const order = base.map((v) => v.key)
  const merged = Array.from(byKey.values())
  merged.sort((a, b) => {
    const ia = order.indexOf(a.key)
    const ib = order.indexOf(b.key)
    if (ia === -1 && ib === -1) return a.name.localeCompare(b.name, 'zh-CN')
    if (ia === -1) return 1
    if (ib === -1) return -1
    return ia - ib
  })
  return merged
}

async function fetchPrimaryChineseEditionNames(): Promise<string[]> {
  try {
    const res = await primaryChineseApi.getFilterOptions()
    const data = unwrapData(res)
    return data?.editions ?? []
  } catch {
    return []
  }
}

export function useHomeSubjectVersions() {
  const versions = ref<EditionVersionOption[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function loadVersions(stage: StageKey, subjectKey: string, _subjectName: string) {
    const key = cacheKey(stage, subjectKey)
    if (cache.has(key)) {
      versions.value = cache.get(key)!
      return
    }

    loading.value = true
    error.value = null
    try {
      let list = staticVersions(subjectKey)

      if (subjectKey === 'chinese' && stage === 'primary') {
        const apiNames = await fetchPrimaryChineseEditionNames()
        list = mergeApiEditionNames(list, apiNames)
      }

      cache.set(key, list)
      versions.value = list
    } catch (e) {
      error.value = e instanceof Error ? e.message : '版本加载失败'
      versions.value = staticVersions(subjectKey)
    } finally {
      loading.value = false
    }
  }

  function clearVersions() {
    versions.value = []
    error.value = null
  }

  return {
    versions,
    loading,
    error,
    loadVersions,
    clearVersions,
  }
}
