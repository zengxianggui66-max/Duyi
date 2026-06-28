import { ref, watch, type Ref } from 'vue'
import { homePanelApi, HOME_LIMIT, type HomePanelItem, type HomePanelListResult } from '@/api/homePanel'
import { requestManager, unwrapData } from '@/api/request'
import type { ResourceListItem } from '@/components/shared'

function toListItems(items: HomePanelItem[]): ResourceListItem[] {
  return items.map((it) => ({
    id: it.id,
    title: it.title,
    date: it.date || '',
    detailPath: it.detailPath,
  }))
}

type PanelApiMethod = (params: any) => Promise<any>

interface UsePanelOptions {
  apiMethod: PanelApiMethod
  watchSources: Ref[]
  paramBuilder: () => Record<string, any>
}

function usePanel({ apiMethod, watchSources, paramBuilder }: UsePanelOptions) {
  const items = ref<ResourceListItem[]>([])
  const loading = ref(false)
  const loaded = ref(false)
  const panelId = `home-panel:${Math.random().toString(36).slice(2)}`

  async function load() {
    const serial = requestManager.nextSerial(panelId)
    loading.value = true
    try {
      const res = await apiMethod({ ...paramBuilder(), limit: HOME_LIMIT })
      if (!requestManager.isLatest(panelId, serial)) return
      const data = unwrapData<HomePanelListResult>(res)
      items.value = toListItems(data?.items ?? [])
    } catch {
      if (!requestManager.isLatest(panelId, serial)) return
      items.value = []
    } finally {
      if (requestManager.isLatest(panelId, serial)) {
        loading.value = false
        loaded.value = true
      }
    }
  }

  watch(watchSources, load, { immediate: true })
  return { items, loading, loaded, reload: load }
}

export function useSyncPrepPanel(
  stageKey: Ref<string>,
  subjectName: Ref<string>,
  tabKey: Ref<string>
) {
  return usePanel({
    apiMethod: homePanelApi.getSyncPrep,
    watchSources: [stageKey, subjectName, tabKey],
    paramBuilder: () => ({ stageKey: stageKey.value, subjectName: subjectName.value, tabKey: tabKey.value }),
  })
}

export function usePaperZonePanel(
  stageKey: Ref<string>,
  gradeName: Ref<string>,
  tabKey: Ref<string>
) {
  return usePanel({
    apiMethod: homePanelApi.getPaperZone,
    watchSources: [stageKey, gradeName, tabKey],
    paramBuilder: () => ({ stageKey: stageKey.value, gradeName: gradeName.value, tabKey: tabKey.value }),
  })
}

export function usePromotionPanel(examType: Ref<string>, topic: Ref<string>) {
  return usePanel({
    apiMethod: homePanelApi.getPromotion,
    watchSources: [examType, topic],
    paramBuilder: () => ({ examType: examType.value, topic: topic.value }),
  })
}
