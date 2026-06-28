/**
 * browse/stats → ResourceTypeBar
 * - 可挂载两套：课本级（排序） / 当前上下文（角标实时更新）
 */
import { ref, watch, onUnmounted, type Ref, type ComputedRef } from 'vue'
import { resourceGateway } from '@/api/resourceGateway'
import type { BrowseStatsResult } from '@/api/browse'
import type { PrimaryChineseParams } from '@/api/types'
import {
  hasBrowseStatsScope,
  stripBrowseStatsParams,
} from '@/utils/sortResourceTypes'
import { statsRowDisplayType } from '@/utils/resourceDisplayType'

export function useBrowseTypeStats(
  buildParams: () => PrimaryChineseParams,
  enabled?: Ref<boolean> | ComputedRef<boolean>,
) {
  let currentSerial = 0
  let debounceTimer: ReturnType<typeof setTimeout> | null = null
  let lastKey = ''

  const statsCache = new Map<string, Record<string, number>>()

  const typeCounts = ref<Record<string, number>>({})
  const statsLoading = ref(false)
  const statsReady = ref(false)

  async function fetchTypeStats(options: { force?: boolean } = {}) {
    if (enabled?.value === false) {
      typeCounts.value = {}
      statsReady.value = false
      return
    }

    const params = stripBrowseStatsParams(buildParams() as unknown as Record<string, unknown>) as PrimaryChineseParams
    const cacheKey = JSON.stringify(params)
    if (!hasBrowseStatsScope(params as unknown as Record<string, unknown>)) {
      // 无 scope 时不清空已有数据，保留上次可用结果
      statsReady.value = false
      return
    }

    if (!options.force && cacheKey === lastKey && statsReady.value) {
      return
    }

    const cached = statsCache.get(cacheKey)
    if (!options.force && cached) {
      lastKey = cacheKey
      typeCounts.value = cached
      statsReady.value = true
      statsLoading.value = false
      return
    }

    const serial = ++currentSerial
    lastKey = cacheKey
    statsLoading.value = true
    try {
      const { stats } = await resourceGateway.getPrimaryChineseStats(params, { silentError: true })
      if (serial !== currentSerial) return
      const payload = stats as BrowseStatsResult | BrowseStatsResult['types'] | null
      const list = Array.isArray(payload)
        ? payload
        : (payload?.types ?? [])
      const map: Record<string, number> = {}
      let sum = 0
      for (const row of list) {
        const key = statsRowDisplayType(row)
        map[key] = (map[key] || 0) + (row.count || 0)
        sum += row.count || 0
      }
      const apiTotal =
        !Array.isArray(payload) && payload?.total != null
          ? Number(payload.total)
          : sum
      const apiTypeSum =
        !Array.isArray(payload) && payload?.typeSum != null
          ? Number(payload.typeSum)
          : sum
      map['全部'] = apiTotal
      statsCache.set(cacheKey, map)
      typeCounts.value = map
      statsReady.value = true
      if (import.meta.env.DEV && apiTypeSum !== apiTotal) {
        console.warn(
          `[browse stats] API typeSum(${apiTypeSum}) ≠ total(${apiTotal})`,
          payload,
        )
      }
    } catch {
      if (serial === currentSerial) {
        typeCounts.value = {}
        statsReady.value = false
      }
    } finally {
      if (serial === currentSerial) statsLoading.value = false
    }
  }

  function scheduleFetchTypeStats(options: { force?: boolean } = {}) {
    if (debounceTimer) clearTimeout(debounceTimer)
    debounceTimer = setTimeout(() => {
      debounceTimer = null
      fetchTypeStats(options)
    }, 150)
  }

  function countForType(typeName: string): number {
    if (!typeName || typeName === '全部') {
      return typeCounts.value['全部'] ?? 0
    }
    return typeCounts.value[typeName] ?? 0
  }

  watch(
    () => {
      if (enabled?.value === false) return null
      return JSON.stringify(stripBrowseStatsParams(buildParams() as unknown as Record<string, unknown>))
    },
    () => {
      scheduleFetchTypeStats()
    },
    { immediate: true },
  )

  onUnmounted(() => {
    currentSerial++
    if (debounceTimer) {
      clearTimeout(debounceTimer)
      debounceTimer = null
    }
  })

  return {
    typeCounts,
    statsLoading,
    statsReady,
    fetchTypeStats,
    scheduleFetchTypeStats,
    countForType,
  }
}

/** 将列表 API 参数转为 stats 参数（去掉类型筛选与分页） */
export function statsParamsFromBrowse(
  params: Record<string, unknown>,
): PrimaryChineseParams {
  return stripBrowseStatsParams(params as Record<string, unknown>) as PrimaryChineseParams
}


