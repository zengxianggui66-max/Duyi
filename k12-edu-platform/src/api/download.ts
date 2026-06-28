/**
 * 下载记录相关 API
 */
import { request } from './request'
import type { ApiResult, PageData } from './request'

export interface DownloadItem {
  id: number
  userId: number
  resourceId: number
  resourceTitle: string
  resourceType?: string
  fileExt?: string
  fileSize?: number
  subject?: string
  gradeName?: string
  teachingType?: string
  stageKey?: string
  createTime: string
}

export interface DownloadListParams {
  current?: number
  size?: number
  keyword?: string
  stageKey?: string
  subject?: string
  teachingType?: string
}

export interface DownloadStats {
  total: number
  weekCount: number
  todayCount: number
}

export interface AddDownloadPayload {
  resourceId: number
  resourceTitle?: string
  resourceType?: string
}

export interface RecordDownloadPayload extends AddDownloadPayload {
  subject?: string
  stageKey?: string
  gradeName?: string
  teachingType?: string
  fileExt?: string
}

export const downloadApi = {
  /** 分页查询下载记录 */
  getPage(params?: DownloadListParams) {
    return request.get<ApiResult<PageData<DownloadItem>>>('/resource/download/page', { params })
  },

  /** 获取下载统计 */
  getStats() {
    return request.get<ApiResult<DownloadStats>>('/resource/download/stats', {
      silentError: true,
    })
  },

  /** 新增下载记录 */
  add(payload: AddDownloadPayload) {
    return request.post<ApiResult<null>>('/resource/download', payload)
  },

  /** 记录用户下载（资源详情页下载时调用） */
  record(payload: RecordDownloadPayload) {
    return request.post<ApiResult<null>>('/resource/download/record', payload, {
      silentError: true,
    })
  },

  /** 删除下载记录 */
  remove(id: number) {
    return request.delete<ApiResult<null>>(`/resource/download/${id}`)
  },

  /** 批量删除下载记录 */
  batchRemove(ids: number[]) {
    return request.delete<ApiResult<null>>('/resource/download/batch', { data: { ids } })
  },
}
