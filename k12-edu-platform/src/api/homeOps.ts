import { request, unwrapData, type ApiResult } from '@/api/request'
import type { NavTarget } from '@/types/homeOps'

export interface HomeBannerItem {
  id?: number
  slotCode?: string
  title: string
  subtitle?: string
  ctaText?: string
  icon?: string
  bgColorStart?: string
  bgColorEnd?: string
  imageUrl?: string
  navTarget: NavTarget
  sort?: number
}

export interface HomeQuickEntryItem {
  id?: number
  entryKey: string
  title: string
  description?: string
  icon?: string
  accentColor?: string
  navTarget: NavTarget
  sort?: number
}

export interface HomeHotWordItem {
  id?: number
  label: string
  actionType: 'browse' | 'search'
  badge?: string
  navTarget: NavTarget
  sort?: number
}

export interface HomeHeroPayload {
  banners: HomeBannerItem[]
  quickEntries: HomeQuickEntryItem[]
  hotWords: HomeHotWordItem[]
}

export async function fetchHomeBanners(stage?: string, slotCode = 'home_hero') {
  const params: Record<string, string> = { slotCode }
  if (stage) params.stage = stage
  const res = await request.get<ApiResult<HomeBannerItem[]>>('/home/banners', { params })
  return unwrapData(res) ?? []
}

export async function fetchHomeQuickEntries(stage?: string) {
  const params = stage ? { stage } : undefined
  const res = await request.get<ApiResult<HomeQuickEntryItem[]>>('/home/quick-entries', { params })
  return unwrapData(res) ?? []
}

export async function fetchHomeHotWords(stage?: string) {
  const params = stage ? { stage } : undefined
  const res = await request.get<ApiResult<HomeHotWordItem[]>>('/home/hot-words', { params })
  return unwrapData(res) ?? []
}

export async function fetchHomeHero(stage?: string, slotCode = 'home_hero'): Promise<HomeHeroPayload | null> {
  const params: Record<string, string> = { slotCode }
  if (stage) params.stage = stage
  const res = await request.get<ApiResult<HomeHeroPayload>>('/home/hero', { params })
  return unwrapData(res) ?? null
}

export interface HomeLatestColumnPayload {
  key: string
  title: string
  morePath: string
  dataSource: 'rule' | 'manual' | 'api'
  items: {
    id?: number
    title: string
    date?: string
    itemType?: string
    linkPath?: string
    articleId?: number
    resourceId?: number
  }[]
}

export async function fetchHomeLatestColumns(stageKey?: string) {
  const params = stageKey ? { stageKey } : undefined
  const res = await request.get<ApiResult<HomeLatestColumnPayload[]>>('/home/latest-columns', { params })
  return unwrapData(res) ?? []
}
