import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'

// ============================================================
// Phase 8：质量治理 API
// ============================================================

// ---------- 敏感词 ----------

export interface SensitiveWord {
  id: number
  word: string
  category: number       // 0-通用 1-政治 2-色情 3-广告 4-暴力 5-其他
  level: number          // 1-警告 2-阻断
  status: number         // 1-启用 0-禁用
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface SensitiveWordDTO {
  word: string
  category?: number
  level?: number
  remark?: string
}

export interface SensitiveWordPage {
  records: SensitiveWord[]
  total: number
}

/** 分页查询敏感词 */
export function listSensitiveWords(params: {
  pageNum?: number
  pageSize?: number
  keyword?: string
  category?: number
  level?: number
  status?: number
}) {
  return request
    .get<ApiResult<SensitiveWordPage>>('/admin/quality/sensitive-words', { params })
    .then(unwrapData)
}

/** 新增敏感词 */
export function createSensitiveWord(payload: SensitiveWordDTO) {
  return request
    .post<ApiResult<SensitiveWord>>('/admin/quality/sensitive-words', payload)
    .then(unwrapData)
}

/** 编辑敏感词 */
export function updateSensitiveWord(id: number, payload: SensitiveWordDTO) {
  return request
    .put<ApiResult<SensitiveWord>>(`/admin/quality/sensitive-words/${id}`, payload)
    .then(unwrapData)
}

/** 删除敏感词 */
export function deleteSensitiveWord(id: number) {
  return request
    .delete<ApiResult<null>>(`/admin/quality/sensitive-words/${id}`)
    .then(unwrapData)
}

/** 批量启用/禁用 */
export function batchSetSensitiveWordStatus(ids: number[], status: number) {
  return request
    .put<ApiResult<null>>('/admin/quality/sensitive-words/batch-status', { ids, status })
    .then(unwrapData)
}

/** 敏感词分类统计 */
export function getSensitiveWordStats() {
  return request
    .get<ApiResult<Record<string, number>[]>>('/admin/quality/sensitive-words/stats')
    .then(unwrapData)
}

// ---------- 预览失败队列 ----------

export interface PreviewFailItem {
  id: number
  sourceType: string
  sourceId: number
  globalId?: number
  title: string
  failReason?: string
  failCount: number
  lastFailTime: string
  status: number          // 0-待处理 1-已处理 2-已忽略
  handlerId?: number
  handlerName?: string
  handlerNote?: string
  handlerTime?: string
  createTime?: string
}

export interface PreviewFailPage {
  records: PreviewFailItem[]
  total: number
}

/** 分页查询预览失败队列 */
export function listPreviewFails(params: {
  pageNum?: number
  pageSize?: number
  status?: number
  sourceType?: string
  keyword?: string
}) {
  return request
    .get<ApiResult<PreviewFailPage>>('/admin/quality/preview-fails', { params })
    .then(unwrapData)
}

/** 标记已处理 */
export function markPreviewFailHandled(id: number, note?: string) {
  return request
    .post<ApiResult<null>>(`/admin/quality/preview-fails/${id}/handle`, { note })
    .then(unwrapData)
}

/** 标记已忽略 */
export function markPreviewFailIgnored(id: number, note?: string) {
  return request
    .post<ApiResult<null>>(`/admin/quality/preview-fails/${id}/ignore`, { note })
    .then(unwrapData)
}

/** 预览失败统计 */
export function getPreviewFailStats() {
  return request
    .get<ApiResult<Record<string, number>[]>>('/admin/quality/preview-fails/stats')
    .then(unwrapData)
}

// ---------- 审核 SLA ----------

export interface AuditSla {
  avgAuditDurationSec: number
  avgAuditDurationFormatted: string
  overtime24hCount: number
  overtime48hCount: number
  overtime72hCount: number
  totalPendingCount: number
  todayCompletedCount: number
  todayRejectedCount: number
}

/** 审核 SLA */
export function getAuditSla(days = 30) {
  return request
    .get<ApiResult<AuditSla>>('/admin/quality/analytics/sla', { params: { days } })
    .then(unwrapData)
}

// ---------- 审核员工作量 ----------

export interface AuditWorkload {
  statDate?: string
  auditorId: number
  auditorName: string
  approveCount: number
  rejectCount: number
  totalCount: number
  avgDurationSec: number
  avgDurationFormatted?: string
  maxDurationSec: number
  overtimeCount: number
}

/** 审核员工作量 */
export function getAuditorWorkload(days = 30, limit = 10) {
  return request
    .get<ApiResult<AuditWorkload[]>>('/admin/quality/analytics/workload', { params: { days, limit } })
    .then(unwrapData)
}

// ---------- 驳回原因统计 ----------

export interface RejectStats {
  category: number
  categoryName: string
  reason: string
  rejectCount: number
  percentage: string
}

/** 驳回原因统计 */
export function getRejectStats(days = 30) {
  return request
    .get<ApiResult<RejectStats[]>>('/admin/quality/analytics/reject-stats', { params: { days } })
    .then(unwrapData)
}

// ---------- 资源增长趋势 ----------

export interface DailyStatsPoint {
  statDate?: string
  newCount: number
  approvedCount: number
  rejectedCount: number
  offlineCount: number
  downloadCount: number
  viewCount: number
}

export interface GrowthTrend {
  totalCount: number
  publishedCount: number
  avgDailyNew: number
  avgDailyApproved: number
  avgDailyRejected: number
  approvalRate: string
  rejectionRate: string
  offlineRate: string
  dailyPoints: DailyStatsPoint[]
}

/** 资源增长趋势 */
export function getGrowthTrend(days = 30) {
  return request
    .get<ApiResult<GrowthTrend>>('/admin/quality/analytics/growth', { params: { days } })
    .then(unwrapData)
}

// ---------- 低访问资源 ----------

export interface LowAccessResource {
  globalId: number
  sourceType: string
  sourceId: number
  title: string
  stage: string
  subject: string
  uploaderId: number
  uploadTime: string
  downloadCount: number
  viewCount: number
  publishStatus: number
  auditStatus: number
  daysSinceUpload: number
  accessLevel: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

/** 低访问资源分页查询 */
export function getLowAccessResources(params: {
  pageNum?: number
  pageSize?: number
  stage?: string
  subject?: string
  accessLevel?: string
}) {
  return request
    .get<ApiResult<PageResult<LowAccessResource>>>('/admin/quality/analytics/low-access', { params })
    .then(unwrapData)
}

// ---------- 质量大盘 ----------

export interface QualityDashboard {
  sla: AuditSla
  rejectStats: RejectStats[]
  auditorWorkload: AuditWorkload[]
  growthTrend: GrowthTrend
  fileSafetyRiskCount: number
  previewFailPendingCount: number
  lowAccessCount: number
  sensitiveWordCount: number
}

/** 质量大盘 */
export function getQualityDashboard(days = 30) {
  return request
    .get<ApiResult<QualityDashboard>>('/admin/quality/analytics/dashboard', { params: { days } })
    .then(unwrapData)
}
