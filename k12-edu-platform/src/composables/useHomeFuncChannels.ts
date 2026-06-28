import { ref, computed, onMounted } from 'vue'
import { homePanelApi, type HomeFuncChannelDto } from '@/api/homePanel'
import { unwrapData } from '@/api/request'
import type { StageKey } from '@/config/subjectConfig'
import {
  HOME_FUNC_CHANNELS,
  HOME_FUNC_MENU_ITEMS,
  PROMOTION_EXAM_TYPE_TABS,
  PROMOTION_TOPICS_MAP,
  getHomeFuncChannel,
  type HomeFuncChannel,
  type HomeFuncKey,
} from '@/constants/homeFuncChannels'

function toChannel(dto: HomeFuncChannelDto): HomeFuncChannel {
  const fallback = getHomeFuncChannel(dto.key)
  return {
    key: dto.key as HomeFuncKey,
    name: dto.name,
    examType: dto.examType,
    defaultTopic: dto.defaultTopic,
    stageKey: (dto.stageKey || fallback.stageKey) as StageKey,
    paperTab: dto.paperTab,
    paperDefaultGrade: dto.paperDefaultGrade,
    scrollTarget: 'exam-module',
    browseModule: dto.browseModule || fallback.browseModule,
    browseStageKey: (dto.browseStageKey || fallback.browseStageKey) as StageKey,
    browseDefaultVolume: dto.browseDefaultVolume || fallback.browseDefaultVolume,
  }
}

export function useHomeFuncChannels() {
  const loaded = ref(false)
  const channels = ref<HomeFuncChannel[]>(Object.values(HOME_FUNC_CHANNELS))
  const promotionTopicsMap = ref<Record<string, string[]>>({ ...PROMOTION_TOPICS_MAP })
  const promotionExamTypeTabs = ref<{ key: string; label: string }[]>(
    PROMOTION_EXAM_TYPE_TABS.map((tab) => ({ key: tab.key, label: tab.label })),
  )

  const menuItems = computed(() =>
    channels.value.map((ch) => ({ key: ch.key, name: ch.name })),
  )

  const channelsByKey = computed(() => {
    const map: Record<string, HomeFuncChannel> = {}
    for (const ch of channels.value) {
      map[ch.key] = ch
    }
    return map
  })

  function getChannel(key: string): HomeFuncChannel {
    return channelsByKey.value[key] ?? getHomeFuncChannel(key)
  }

  async function load() {
    try {
      const res = await homePanelApi.getFuncChannels()
      const data = unwrapData(res)
      if (data?.channels?.length) {
        channels.value = data.channels.map(toChannel)
      }
      if (data?.promotionTopicsMap && Object.keys(data.promotionTopicsMap).length) {
        promotionTopicsMap.value = data.promotionTopicsMap
      }
      if (data?.promotionExamTabs?.length) {
        promotionExamTypeTabs.value = data.promotionExamTabs.map((tab) => ({
          key: String(tab.key),
          label: String(tab.label),
        }))
      }
    } catch {
      // 保留本地兜底配置
    } finally {
      loaded.value = true
    }
  }

  onMounted(load)

  return {
    loaded,
    channels,
    menuItems,
    promotionTopicsMap,
    promotionExamTypeTabs,
    getChannel,
    reload: load,
    fallbackMenuItems: HOME_FUNC_MENU_ITEMS,
  }
}
