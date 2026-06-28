/**
 * 学科页资源系列（品牌）筛选
 */
import { ref, computed, watch, onMounted, type Ref } from 'vue'
import type { RouteLocationNormalizedLoaded } from 'vue-router'
import { brandApi } from '../api/brand'
import { unwrapData } from '../api/request'
import {
  DEFAULT_BRAND_CODE,
  RESOURCE_SERIES_TABS,
  brandCodeForApi,
  findSeriesByCode,
  type ResourceSeriesItem,
} from '../config/resourceSeriesConfig'

export interface UseResourceSeriesOptions {
  route: RouteLocationNormalizedLoaded
  currentPublisher: Ref<string>
}

export function useResourceSeries(opts: UseResourceSeriesOptions) {
  const { route, currentPublisher } = opts

  const seriesTabs = ref<ResourceSeriesItem[]>([...RESOURCE_SERIES_TABS])
  const selectedBrandCode = ref(DEFAULT_BRAND_CODE)
  const brandsLoading = ref(false)

  const selectedSeries = computed(() => findSeriesByCode(selectedBrandCode.value))
  const selectedSeriesName = computed(() => selectedSeries.value?.name || '')
  const brandCodeParam = computed(() => brandCodeForApi(selectedBrandCode.value))
  const displayMode = computed(
    () => selectedSeries.value?.displayMode || 'lesson_hub',
  )

  function applyPublisherFromSeries() {
    const pub = selectedSeries.value?.publisher
    if (pub) {
      currentPublisher.value = pub
    }
  }

  function initBrandFromRoute() {
    const raw = route.query.brand
    if (typeof raw === 'string') {
      selectedBrandCode.value = raw
      return
    }
    if (!selectedBrandCode.value) {
      selectedBrandCode.value = DEFAULT_BRAND_CODE
    }
  }

  async function loadBrandsFromApi() {
    brandsLoading.value = true
    try {
      const res = await brandApi.list()
      const list = unwrapData(res)
      if (list?.length) {
        const fromApi: ResourceSeriesItem[] = list.map((b) => ({
          code: b.code,
          name: b.name,
          publisher: b.publisher,
        }))
        const allTab = RESOURCE_SERIES_TABS.find((s) => s.code === '')
        seriesTabs.value = allTab ? [allTab, ...fromApi] : fromApi
      }
    } catch {
      seriesTabs.value = [...RESOURCE_SERIES_TABS]
    } finally {
      brandsLoading.value = false
      applyPublisherFromSeries()
    }
  }

  function selectBrand(code: string) {
    selectedBrandCode.value = code
    applyPublisherFromSeries()
  }

  watch(selectedBrandCode, () => applyPublisherFromSeries())

  onMounted(() => {
    initBrandFromRoute()
    loadBrandsFromApi()
  })

  return {
    seriesTabs,
    selectedBrandCode,
    selectedSeries,
    selectedSeriesName,
    brandCodeParam,
    displayMode,
    brandsLoading,
    initBrandFromRoute,
    selectBrand,
    loadBrandsFromApi,
  }
}
