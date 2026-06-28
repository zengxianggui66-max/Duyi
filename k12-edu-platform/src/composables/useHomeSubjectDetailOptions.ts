/**
 * 首页学科详情浮层 — Phase 5-F 聚合 API
 * GET /api/home/subject-nav
 */
import { ref, computed } from 'vue'
import { homeSubjectNavApi } from '@/api/homeSubjectNav'
import type { StageKey } from '@/config/subjectConfig'
import {
  editionNameToKey,
  type EditionVersionOption,
} from '@/utils/editionAdapter'
import { DEFAULT_RESOURCE_TYPE_TABS } from '@/constants/syncPrepColumnFilters'

function mapEditions(
  items: { id?: number; code?: string; name: string; isNew?: boolean }[] | undefined,
): EditionVersionOption[] {
  if (!items?.length) return []
  return items.map((item) => {
    const editionName = item.name.trim()
    const key = item.code?.trim() || editionNameToKey(editionName)
    return {
      id: item.id,
      key,
      name: editionName,
      editionName,
      isNew: item.isNew,
    }
  })
}

export function useHomeSubjectDetailOptions() {
  const versions = ref<EditionVersionOption[]>([])
  const resourceTypes = ref<string[]>([])
  const reviewModules = ref<string[]>([])
  const promotionModules = ref<string[]>([])
  const optionsLoading = ref(false)
  const optionsError = ref<string | null>(null)
  const promotionSectionTitle = ref('升学备考')

  async function loadOptions(stage: StageKey, subjectKey: string, _subjectName?: string) {
    optionsLoading.value = true
    optionsError.value = null
    promotionSectionTitle.value = '升学备考'

    try {
      const data = await homeSubjectNavApi.getNav(stage, subjectKey)
      if (!data?.subject?.code) {
        throw new Error('学科导航数据为空')
      }

      versions.value = mapEditions(data.syncPrep?.editions)
      const types = (data.syncPrep?.resourceTypes || [])
        .map((t) => String(t.name ?? '').trim())
        .filter(Boolean)
      resourceTypes.value = types.length ? types : [...DEFAULT_RESOURCE_TYPE_TABS]

      reviewModules.value = (data.reviewPrep?.modules || [])
        .map((m) => String(m.name ?? '').trim())
        .filter(Boolean)
      promotionModules.value = (data.promotionPrep?.modules || [])
        .map((m) => String(m.name ?? '').trim())
        .filter(Boolean)

      promotionSectionTitle.value =
        data.promotionPrep?.title || data.promotionPrep?.label || '升学备考'
    } catch (e) {
      optionsError.value = e instanceof Error ? e.message : '筛选项加载失败'
      versions.value = []
      resourceTypes.value = [...DEFAULT_RESOURCE_TYPE_TABS]
      reviewModules.value = []
      promotionModules.value = []
    } finally {
      optionsLoading.value = false
    }
  }

  function clearOptions() {
    versions.value = []
    resourceTypes.value = []
    reviewModules.value = []
    promotionModules.value = []
    optionsError.value = null
  }

  const loading = computed(() => optionsLoading.value)

  return {
    versions,
    resourceTypes,
    reviewModules,
    promotionModules,
    promotionSectionTitle,
    loading,
    versionsLoading: optionsLoading,
    optionsLoading,
    optionsError,
    loadOptions,
    clearOptions,
  }
}
