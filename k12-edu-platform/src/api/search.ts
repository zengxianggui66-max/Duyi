/**
 * 搜索相关 API
 */
import type { RouteLocationRaw } from 'vue-router'
import { request } from './request'
import type { ApiResult } from './request'
import { resolveNavTargetRoute } from '@/utils/resolveNavTarget'

// ==================== 类型定义 ====================

export interface HotKeyword {
  keyword: string
  searchCount: number
  rank: number
}

export interface SearchFacetBucket {
  key: string
  name: string
  count: number
}

/** 内容域：全部 / 学段资源 / 特色频道 / 备课专区 / 教育资讯 */
export type SearchContentDomain =
  | ''
  | 'stage_resource'
  | 'feature'
  | 'prep'
  | 'news'

export interface SearchFacets {
  stages: SearchFacetBucket[]
  channels: SearchFacetBucket[]
  types: SearchFacetBucket[]
  domains?: SearchFacetBucket[]
}

export interface SearchResultItem {
  docId: string
  resourceId: number
  resourceType: string
  title: string
  summary?: string
  titleHighlight?: string
  summaryHighlight?: string
  stageKey?: string
  stageName?: string
  channelKey?: string
  channelName?: string
  subject?: string
  gradeName?: string
  teachingType?: string
  fileExt?: string
  downloadCount?: number
  viewCount?: number
  uploadTime?: string
  score?: number
  detailRoute: string
  coverUrl?: string
  badges?: string[]
  /** resource / news / channel / lesson / prep / feature / page */
  docType?: string
  bizId?: string
  subtitle?: string
  matchedFields?: string[]
  routeQuery?: Record<string, string>
  contentDomain?: SearchContentDomain
}

/** P2 搜索意图解析结果 */
export interface SearchParsedIntent {
  stage?: string
  subject?: string
  grade?: string
  resourceType?: string
  module?: string
  channel?: string
  contentDomain?: string
  normalizedQuery?: string
}

export interface SearchAllResult {
  records: SearchResultItem[]
  total: number
  page: number
  size: number
  pages: number
  facets?: SearchFacets
  costMs?: number
  queryHint?: string
  recommendations?: SearchSuggestItem[]
  /** 顶部「你可能要找」意图建议 */
  intents?: SearchSuggestItem[]
  /** P2 查询意图解析结果 */
  parsedIntent?: SearchParsedIntent
}

export interface SearchAllParams {
  q: string
  page?: number
  size?: number
  stage?: string
  channel?: string
  type?: string
  domain?: SearchContentDomain
  sort?: 'score' | 'newest' | 'download'
  /** mysql | auto — auto 在 OpenSearch hosts 配置时优先走引擎 */
  searchEngine?: 'mysql' | 'auto'
}

export type SearchSuggestKind =
  | 'keyword'
  | 'history'
  | 'hot'
  | 'title'
  | 'channel'
  | 'subject'
  | 'stage'
  | 'resourceType'
  | 'feature'
  | 'prep'
  | 'news'
  | 'page'

import type { NavTarget } from '@/types/homeOps'

export interface SearchSuggestItem {
  text: string
  kind: SearchSuggestKind
  resourceType?: string
  resourceId?: number
  detailRoute?: string
  hitCount?: number
  highlight?: string
  subtitle?: string
  routeQuery?: Record<string, string>
  contentDomain?: SearchContentDomain
  /** Phase 7-B: ops hot word nav (browse/search) */
  navTarget?: NavTarget
}

export interface SearchRedirectResult {
  directHit: boolean
  confidence: number
  reason?: string
  target: SearchResultItem | null
}

export interface SearchClickPayload {
  keyword: string
  docId?: string
  resourceId?: number
  resourceType?: string
  clickType?: 'result' | 'redirect' | 'recommend'
  position?: number
  detailRoute?: string
}

export interface SearchQueryRank {
  keyword: string
  queryCount: number
  zeroCount?: number
  avgHits?: number
}

export interface SearchStats {
  days: number
  totalQueries: number
  zeroResultQueries: number
  zeroResultRate: number
  totalClicks: number
  clickThroughRate: number
  clicksByType: Record<string, number>
  topQueries: SearchQueryRank[]
  topZeroQueries: SearchQueryRank[]
}

/** 内容域筛选选项（结果页） */
export const SEARCH_CONTENT_DOMAIN_OPTIONS: Array<{ key: SearchContentDomain; label: string }> = [
  { key: '', label: '全部' },
  { key: 'stage_resource', label: '学段资源' },
  { key: 'feature', label: '特色频道' },
  { key: 'prep', label: '备课专区' },
  { key: 'news', label: '教育资讯' },
]

/** 将 suggest kind 映射到 UI 分组 key */
export function suggestGroupKey(kind: SearchSuggestKind): string {
  if (kind === 'subject' || kind === 'stage') return 'subject_stage'
  if (kind === 'title' || kind === 'resourceType') return 'resource'
  if (kind === 'channel' || kind === 'feature') return 'feature'
  if (kind === 'prep') return 'prep'
  if (kind === 'news') return 'news'
  if (kind === 'history') return 'history'
  if (kind === 'hot') return 'hot'
  if (kind === 'page') return 'page'
  return 'keyword'
}

export function suggestGroupLabel(groupKey: string): string {
  const map: Record<string, string> = {
    subject_stage: '学段学科',
    resource: '资源直达',
    feature: '特色频道',
    prep: '备课专区',
    news: '教育资讯',
    history: '历史搜索',
    hot: '热门搜索',
    page: '系统入口',
    keyword: '关键词',
  }
  return map[groupKey] || '其他'
}

/** 结果卡片 docType 标签文案 */
export function docTypeLabel(docType?: string, fallback?: string): string {
  const map: Record<string, string> = {
    resource: '资源',
    news: '资讯',
    channel: '频道',
    lesson: '备课',
    prep: '备课',
    feature: '特色',
    page: '入口',
  }
  if (docType && map[docType]) return map[docType]
  return fallback || '资源'
}

/** 内容域标签文案 */
export function contentDomainLabel(domain?: SearchContentDomain): string {
  const found = SEARCH_CONTENT_DOMAIN_OPTIONS.find((item) => item.key === domain)
  return found?.label || '全站'
}

/** 将 suggest 项解析为可跳转路由 */
export function resolveSuggestionRoute(item: SearchSuggestItem): RouteLocationRaw | null {
  if (item.navTarget) {
    const fromNav = resolveNavTargetRoute(item.navTarget)
    if (fromNav) return fromNav
  }
  if (item.detailRoute) {
    const qIdx = item.detailRoute.indexOf('?')
    if (qIdx >= 0) {
      const path = item.detailRoute.slice(0, qIdx)
      const query = Object.fromEntries(new URLSearchParams(item.detailRoute.slice(qIdx + 1)))
      return { path, query: { ...query, ...item.routeQuery } }
    }
    if (item.routeQuery) {
      return { path: item.detailRoute, query: item.routeQuery }
    }
    return item.detailRoute
  }
  if (item.routeQuery) {
    if (item.routeQuery.path) {
      const { path, ...query } = item.routeQuery
      return { path, query }
    }
    return {
      name: 'SearchResult',
      query: { q: item.text, page: '1', sort: 'score', ...item.routeQuery },
    }
  }
  return null
}

/** 将搜索结果项解析为可跳转路由 */
export function resolveResultRoute(item: SearchResultItem): RouteLocationRaw | null {
  if (item.detailRoute) {
    const qIdx = item.detailRoute.indexOf('?')
    if (qIdx >= 0) {
      const path = item.detailRoute.slice(0, qIdx)
      const query = Object.fromEntries(new URLSearchParams(item.detailRoute.slice(qIdx + 1)))
      return { path, query: { ...query, ...item.routeQuery } }
    }
    if (item.routeQuery) {
      return { path: item.detailRoute, query: item.routeQuery }
    }
    return item.detailRoute
  }
  if (item.routeQuery) {
    if (item.routeQuery.path) {
      const { path, ...query } = item.routeQuery
      return { path, query }
    }
    return {
      name: 'SearchResult',
      query: { q: item.title, page: '1', sort: 'score', ...item.routeQuery },
    }
  }
  if (item.resourceId) {
    return { name: 'ResourceDetail', params: { id: String(item.resourceId) } }
  }
  return null
}

// ==================== API 方法 ====================

export const searchApi = {
  /**
   * 获取热搜关键词
   */
  getHotKeywords(limit?: number) {
    return request.get<ApiResult<HotKeyword[]>>('/search/hot-keywords', {
      params: { limit: limit || 10 },
    })
  },

  /**
   * 记录搜索关键词
   */
  recordKeyword(keyword: string, userId?: number) {
    return request.post<ApiResult>('/search/keyword', null, {
      params: { keyword, userId },
    })
  },

  /**
   * 获取搜索历史
   */
  getSearchHistory(userId: number, limit?: number) {
    return request.get<ApiResult<string[]>>('/search/history', {
      params: { userId, limit: limit || 20 },
    })
  },

  /**
   * 清空搜索历史
   */
  clearHistory(userId: number) {
    return request.delete<ApiResult>('/search/history', {
      params: { userId },
    })
  },

  /**
   * 全站搜索（分页 + 筛选 + 排序）
   */
  searchAll(params: SearchAllParams) {
    return request.get<ApiResult<SearchAllResult>>('/search/all', {
      params,
      silentError: true,
    })
  },

  /**
   * 搜索建议词
   */
  suggest(q: string, limit = 10, userId?: number) {
    return request.get<ApiResult<SearchSuggestItem[]>>('/search/suggest', {
      params: { q, limit, userId },
      silentError: true,
    })
  },

  /**
   * 直跳判定（命中唯一高置信结果）
   */
  redirect(q: string) {
    return request.get<ApiResult<SearchRedirectResult>>('/search/redirect', {
      params: { q },
      silentError: true,
    })
  },

  recordClick(payload: SearchClickPayload) {
    return request.post<ApiResult>('/search/click', payload, {
      silentError: true,
    })
  },

  searchStats(days = 7) {
    return request.get<ApiResult<SearchStats>>('/search/admin/stats', {
      params: { days },
      silentError: true,
    })
  },
}
