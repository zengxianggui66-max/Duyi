import type { ResourceWritePayload } from '@/api/resources'
import {
  RESOURCE_STATUS_DRAFT,
  SYNC_UPLOAD_DEFAULT_STATUS,
} from '@/constants/uploadDefaults'

/** 同步备课上传表单 → 宽表/主表写入字段（与 buildSyncPayload 一致） */
export interface SyncUploadPayload {
  id?: number
  stage?: string
  subject?: string
  module?: string
  type?: string
  gradeName?: string
  edition?: string
  brandCode?: string
  catalogNodeId?: number
  catalogPath?: string
  subType?: string
  unitName?: string
  lessonName?: string
  title?: string
  description?: string
  remark?: string
  originalFilename?: string
  fileExt?: string
  ossUrl?: string
  ossBucket?: string
  ossObjectKey?: string
  fileSizeKb?: number
  status?: number
  allowPreview?: number
  previewStatus?: number
  fileSafetyStatus?: number
  isFree?: number
  sort?: number
  lessonPlanJson?: string
}

export function toResourceWritePayload(
  payload: SyncUploadPayload,
  options?: { draft?: boolean },
): ResourceWritePayload {
  const draft = options?.draft === true
  const status = draft
    ? RESOURCE_STATUS_DRAFT
    : payload.status ?? SYNC_UPLOAD_DEFAULT_STATUS

  let title = (payload.title || '').trim()
  if (!title) {
    title = draft ? '未命名草稿' : ''
  }

  const write: ResourceWritePayload = {
    title,
    description: payload.description,
    remark: payload.remark,
    stage: payload.stage,
    subject: payload.subject,
    module: payload.module,
    type: payload.type,
    gradeName: payload.gradeName,
    edition: payload.edition,
    unitName: payload.unitName,
    lessonName: payload.lessonName,
    brandCode: payload.brandCode,
    catalogNodeId: payload.catalogNodeId,
    catalogPath: payload.catalogPath,
    subType: payload.subType,
    status,
    allowPreview: payload.allowPreview,
    isFree: payload.isFree,
    sort: payload.sort,
    lessonPlanJson: payload.lessonPlanJson,
    originalFilename: payload.originalFilename,
    fileExt: payload.fileExt,
    ossBucket: payload.ossBucket,
    ossObjectKey: payload.ossObjectKey,
    ossUrl: payload.ossUrl,
    fileSizeKb: payload.fileSizeKb,
  }

  if (payload.ossUrl || payload.ossObjectKey) {
    const bytes =
      payload.fileSizeKb != null && payload.fileSizeKb > 0
        ? payload.fileSizeKb * 1024
        : undefined
    write.files = [
      {
        fileRole: 'main',
        originalFilename: payload.originalFilename,
        fileExt: payload.fileExt,
        ossBucket: payload.ossBucket,
        ossObjectKey: payload.ossObjectKey,
        ossUrl: payload.ossUrl,
        fileSizeBytes: bytes,
        allowPreview: payload.allowPreview ?? 1,
        sort: 0,
      },
    ]
  }

  return write
}
