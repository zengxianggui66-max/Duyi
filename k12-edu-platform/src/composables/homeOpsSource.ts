import { USE_HOME_OPS_API } from '@/config/featureFlags'
import {
  STATIC_BANNERS,
  STATIC_HOT_WORDS,
  STATIC_QUICK_ENTRIES,
} from '@/config/homeOpsStatic'
import {
  fetchHomeBanners,
  fetchHomeHero,
  fetchHomeHotWords,
  fetchHomeQuickEntries,
  type HomeBannerItem,
  type HomeHotWordItem,
  type HomeQuickEntryItem,
} from '@/api/homeOps'
import type { BannerViewModel, HotWordViewModel, QuickEntryViewModel } from '@/types/homeOps'

function mapBanner(item: HomeBannerItem): BannerViewModel {
  return {
    id: item.id,
    title: item.title,
    description: item.subtitle ?? '',
    cta: item.ctaText ?? '立即查看',
    icon: item.icon ?? '📚',
    color1: item.bgColorStart ?? '#667EEA',
    color2: item.bgColorEnd ?? '#764BA2',
    navTarget: item.navTarget,
  }
}

function mapQuickEntry(item: HomeQuickEntryItem): QuickEntryViewModel {
  return {
    key: item.entryKey,
    title: item.title,
    description: item.description ?? '',
    icon: item.icon ?? '📚',
    color: item.accentColor ?? '#4facfe',
    navTarget: item.navTarget,
  }
}

function mapHotWord(item: HomeHotWordItem): HotWordViewModel {
  return {
    label: item.label,
    actionType: item.actionType,
    badge: item.badge,
    navTarget: item.navTarget,
  }
}

export async function loadHomeBanners(stage?: string): Promise<BannerViewModel[]> {
  if (!USE_HOME_OPS_API) return STATIC_BANNERS
  try {
    const list = await fetchHomeBanners(stage)
    if (!list.length) return STATIC_BANNERS
    return list.map(mapBanner)
  } catch {
    return STATIC_BANNERS
  }
}

export async function loadHomeQuickEntries(stage?: string): Promise<QuickEntryViewModel[]> {
  if (!USE_HOME_OPS_API) return STATIC_QUICK_ENTRIES
  try {
    const list = await fetchHomeQuickEntries(stage)
    if (!list.length) return STATIC_QUICK_ENTRIES
    return list.map(mapQuickEntry)
  } catch {
    return STATIC_QUICK_ENTRIES
  }
}

export async function loadHomeHotWords(stage?: string): Promise<HotWordViewModel[]> {
  if (!USE_HOME_OPS_API) return STATIC_HOT_WORDS
  try {
    const list = await fetchHomeHotWords(stage)
    if (!list.length) return STATIC_HOT_WORDS
    return list.map(mapHotWord)
  } catch {
    return STATIC_HOT_WORDS
  }
}

export async function loadHomeHero(stage?: string) {
  if (!USE_HOME_OPS_API) {
    return {
      banners: STATIC_BANNERS,
      quickEntries: STATIC_QUICK_ENTRIES,
      hotWords: STATIC_HOT_WORDS,
    }
  }
  try {
    const hero = await fetchHomeHero(stage)
    if (!hero) {
      return {
        banners: STATIC_BANNERS,
        quickEntries: STATIC_QUICK_ENTRIES,
        hotWords: STATIC_HOT_WORDS,
      }
    }
    return {
      banners: hero.banners?.length ? hero.banners.map(mapBanner) : STATIC_BANNERS,
      quickEntries: hero.quickEntries?.length ? hero.quickEntries.map(mapQuickEntry) : STATIC_QUICK_ENTRIES,
      hotWords: hero.hotWords?.length ? hero.hotWords.map(mapHotWord) : STATIC_HOT_WORDS,
    }
  } catch {
    return {
      banners: STATIC_BANNERS,
      quickEntries: STATIC_QUICK_ENTRIES,
      hotWords: STATIC_HOT_WORDS,
    }
  }
}
