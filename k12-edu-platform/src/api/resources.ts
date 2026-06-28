/**
 * M4/M5：主表资源写入 API（edu_resource + 双写宽表）
 */
import { request } from './request'
import type { ApiResult } from './request'
import type { PrimaryChineseItem } from './types'

export interface ResourceFilePayload {
  fileRole?: string
  originalFilename?: string
  fileExt?: string
  ossBucket?: string
  ossObjectKey?: string
  ossUrl?: string
  fileSizeBytes?: number
  allowPreview?: number
  sort?: number
}

/** 与后端 ResourceWriteDTO 对齐 */
export interface ResourceWritePayload {
  id?: number
  title?: string
  description?: string
  remark?: string
  stage?: string
  subject?: string
  module?: string
  type?: string
  gradeName?: string
  edition?: string
  unitName?: string
  lessonName?: string
  brandCode?: string
  catalogNodeId?: number
  catalogPath?: string
  subType?: string
  status?: number
  uploaderId?: number
  allowPreview?: number
  sort?: number
  isFree?: number
  lessonPlanJson?: string
  originalFilename?: string
  fileExt?: string
  ossBucket?: string
  ossObjectKey?: string
  ossUrl?: string
  fileSizeKb?: number
  files?: ResourceFilePayload[]
}

export const resourcesApi = {
  create(payload: ResourceWritePayload) {
    return request.post<ApiResult<PrimaryChineseItem>>('/resources', payload)
  },

  update(id: number, payload: ResourceWritePayload) {
    return request.put<ApiResult<PrimaryChineseItem>>(`/resources/${id}`, payload)
  },
}
