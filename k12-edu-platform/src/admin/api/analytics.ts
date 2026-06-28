import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'

export interface AnalyticsDailyPoint {
  date: string
  count: number
  cumulative?: number
}

export interface AnalyticsActionDaily {
  date: string
  views: number
  downloads: number
  collects: number
}

export interface AnalyticsDistributionItem {
  name: string
  count: number
}

export interface AnalyticsAudit {
  approved: number
  rejected: number
  passRate?: number | null
}

export interface AnalyticsTopResource {
  id: number
  title: string
  stage?: string
  subject?: string
  downloadCount: number
  viewCount: number
  collectCount: number
}

export interface AnalyticsSummary {
  totalResources: number
  pendingResources: number
  publishedResources: number
  totalDownloads: number
  totalViews: number
  totalCollects: number
  newResourcesInPeriod: number
}

export interface AnalyticsUploaderStat {
  uploaderId: number
  uploadCount: number
}

export interface AnalyticsDashboard {
  days: number
  scoped: boolean
  scopeHint?: string | null
  summary: AnalyticsSummary
  audit: AnalyticsAudit
  resourceUploadTrend: AnalyticsDailyPoint[]
  actionTrend: AnalyticsActionDaily[]
  stageDistribution: AnalyticsDistributionItem[]
  subjectDistribution: AnalyticsDistributionItem[]
  topByDownload: AnalyticsTopResource[]
  topByView: AnalyticsTopResource[]
  topByCollect: AnalyticsTopResource[]
  topUploaders: AnalyticsUploaderStat[]
}

export interface AnalyticsUser {
  days: number
  totalUsers: number
  newUsersInPeriod: number
  registrationTrend: AnalyticsDailyPoint[]
}

export function getAnalyticsDashboard(days = 7) {
  return request
    .get<ApiResult<AnalyticsDashboard>>('/admin/analytics/dashboard', { params: { days } })
    .then(unwrapData)
}

export function getAnalyticsUsers(days = 30) {
  return request
    .get<ApiResult<AnalyticsUser>>('/admin/analytics/users', { params: { days } })
    .then(unwrapData)
}
