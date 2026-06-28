import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'
import type { NavTarget } from '@/types/homeOps'

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
  clicksByType?: Record<string, number>
  topQueries: SearchQueryRank[]
  topZeroQueries: SearchQueryRank[]
}

export interface SearchP3Readiness {
  days: number
  totalDocs: number
  docTypeDistribution?: Record<string, unknown>[]
  channelDistribution?: Record<string, unknown>[]
  scaleVerdict?: string
  scaleRecommendation?: string
  totalQueries: number
  avgCostMs?: number
  maxCostMs?: number
  p95CostMs?: number
  p99CostMs?: number
  slowestKeywords?: Record<string, unknown>[]
  latencyVerdict?: string
  zeroResultQueries: number
  zeroResultRate: number
  topZeroKeywords?: Record<string, unknown>[]
  zeroResultVerdict?: string
  overallCtr?: number
  ctrByKeyword?: Record<string, unknown>[]
  ctrVerdict?: string
  overallTop3ClickRate?: number
  overallAvgClickPosition?: number
  positionByKeyword?: Record<string, unknown>[]
  positionVerdict?: string
  overallRecommendation?: string
  p3Mode?: string
  notes?: string[]
}

export interface SearchEngineHealth {
  enabled?: boolean
  reachable?: boolean
  clusterName?: string
  indexName?: string
  docCount?: number
  lastSyncTime?: string
  error?: string
  [key: string]: unknown
}

export interface SearchSynonymItem {
  id?: number
  word: string
  synonyms: string
  domain?: string
  canonical?: string
  status?: number
  updateTime?: string
}

export interface SearchSynonymWrite {
  word: string
  synonyms: string
  domain?: string
  canonical?: string
  status?: number
}

export interface SearchRedirectItem {
  id?: number
  keyword: string
  title?: string
  routePath: string
  navTarget?: NavTarget
  priority?: number
  status?: number
  remark?: string
  updateTime?: string
}

export interface SearchRedirectWrite {
  keyword: string
  title?: string
  routePath?: string
  navTarget?: NavTarget
  priority?: number
  status?: number
  remark?: string
}

export interface SearchIntentRuleItem {
  id?: number
  pattern: string
  intentType: string
  targetKey?: string
  targetValue?: string
  targetPayload?: string
  priority?: number
  status?: number
  updateTime?: string
}

export interface SearchHotKeywordItem {
  id?: number
  keyword: string
  searchCount?: number
  boostScore?: number
  rank?: number
  status?: number
  updateTime?: string
}

export function getSearchStats(days = 7) {
  return request.get<ApiResult<SearchStats>>('/admin/search/stats', { params: { days } }).then(unwrapData)
}

export function getSearchP3Readiness(days = 7) {
  return request
    .get<ApiResult<SearchP3Readiness>>('/admin/search/p3-readiness', { params: { days } })
    .then(unwrapData)
}

export function getSearchEngineHealth() {
  return request.get<ApiResult<SearchEngineHealth>>('/admin/search/engine/health').then(unwrapData)
}

export function reindexSearch() {
  return request.post<ApiResult<number>>('/admin/search/reindex').then(unwrapData)
}

export function syncSearchEngine() {
  return request.post<ApiResult<number>>('/admin/search/engine/sync').then(unwrapData)
}

export function listSearchSynonyms(includeDisabled = false, domain?: string) {
  return request
    .get<ApiResult<SearchSynonymItem[]>>('/admin/search/synonyms', {
      params: { includeDisabled, domain },
    })
    .then(unwrapData)
}

export function createSearchSynonym(payload: SearchSynonymWrite) {
  return request.post<ApiResult<SearchSynonymItem>>('/admin/search/synonyms', payload).then(unwrapData)
}

export function createSearchSynonymDraft(keyword: string) {
  return request
    .post<ApiResult<SearchSynonymItem>>('/admin/search/synonyms/draft', null, {
      params: { keyword },
    })
    .then(unwrapData)
}

export function updateSearchSynonym(id: number, payload: SearchSynonymWrite) {
  return request.put<ApiResult<SearchSynonymItem>>(`/admin/search/synonyms/${id}`, payload).then(unwrapData)
}

export function setSearchSynonymStatus(id: number, status: number) {
  return request
    .put<ApiResult<null>>(`/admin/search/synonyms/${id}/status`, null, { params: { status } })
    .then(unwrapData)
}

export function deleteSearchSynonym(id: number) {
  return request.delete<ApiResult<null>>(`/admin/search/synonyms/${id}`).then(unwrapData)
}

export function listSearchRedirects(includeDisabled = false) {
  return request
    .get<ApiResult<SearchRedirectItem[]>>('/admin/search/redirects', { params: { includeDisabled } })
    .then(unwrapData)
}

export function createSearchRedirect(payload: SearchRedirectWrite) {
  return request.post<ApiResult<SearchRedirectItem>>('/admin/search/redirects', payload).then(unwrapData)
}

export function updateSearchRedirect(id: number, payload: SearchRedirectWrite) {
  return request.put<ApiResult<SearchRedirectItem>>(`/admin/search/redirects/${id}`, payload).then(unwrapData)
}

export function setSearchRedirectStatus(id: number, status: number) {
  return request
    .put<ApiResult<null>>(`/admin/search/redirects/${id}/status`, null, { params: { status } })
    .then(unwrapData)
}

export function deleteSearchRedirect(id: number) {
  return request.delete<ApiResult<null>>(`/admin/search/redirects/${id}`).then(unwrapData)
}

export function listSearchIntentRules(includeDisabled = false) {
  return request
    .get<ApiResult<SearchIntentRuleItem[]>>('/admin/search/intent-rules', {
      params: { includeDisabled },
    })
    .then(unwrapData)
}

export function listSearchHotKeywords(includeDisabled = false) {
  return request
    .get<ApiResult<SearchHotKeywordItem[]>>('/admin/search/hot-keywords', {
      params: { includeDisabled },
    })
    .then(unwrapData)
}

export function setSearchHotKeywordStatus(id: number, status: number) {
  return request
    .put<ApiResult<null>>(`/admin/search/hot-keywords/${id}/status`, null, { params: { status } })
    .then(unwrapData)
}

export function updateSearchHotKeywordBoost(id: number, boostScore: number) {
  return request
    .put<ApiResult<SearchHotKeywordItem>>(`/admin/search/hot-keywords/${id}/boost`, null, {
      params: { boostScore },
    })
    .then(unwrapData)
}

export function buildSearchPreviewUrl(keyword: string) {
  const q = encodeURIComponent(keyword.trim())
  return `/search/result?q=${q}`
}
