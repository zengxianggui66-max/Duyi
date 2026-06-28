import type { StageKey } from '@/config/subjectConfig'

export type NavTargetType = 'browse' | 'search' | 'route' | 'external' | 'scroll' | 'vip'

export interface NavTarget {
  type: NavTargetType
  routePath?: string
  stageKey?: StageKey | string
  subjectKey?: string
  versionKey?: string
  volumeName?: string
  keyword?: string
  scrollTarget?: string
  externalUrl?: string
  openInNewTab?: boolean
  query?: Record<string, string>
  searchEngine?: 'mysql' | 'auto'
  searchScope?: Record<string, unknown>
}

export interface BannerViewModel {
  id?: number
  title: string
  description: string
  cta: string
  icon: string
  color1: string
  color2: string
  navTarget: NavTarget
}

export interface QuickEntryViewModel {
  key: string
  title: string
  description: string
  icon: string
  color: string
  navTarget: NavTarget
}

export interface HotWordViewModel {
  label: string
  actionType: 'browse' | 'search'
  badge?: string
  navTarget: NavTarget
}
