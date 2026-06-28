/**
 * 上传持久化工具函数
 * 从 useResourceUploadForm 中提取
 */
import { primaryChineseApi, resourcesApi } from '@/api'
import type { PrimaryChineseItem } from '@/api/types'
import { unwrapData } from '@/api/request'
import { USE_MASTER_WRITE } from '@/config/featureFlags'
import { toResourceWritePayload, type SyncUploadPayload } from '@/utils/resourceWriteMapper'
import { RESOURCE_STATUS_DRAFT } from '@/constants/uploadDefaults'
import { fileApi, type UploadResult } from '@/api'

/** 构建同步备课提交载荷 */
export function buildSyncPayload(
  formData: any,
  browseSnapshot: any,
  draftId: number | null,
  tempUploadResult: UploadResult | null,
  uploadFile: File | null,
  gradeLevelLabel: string,
  subjectLabel: string,
  fileUrl: string,
  fileSizeKb: number,
  ext: string,
  status: number,
  options?: { file?: File; title?: string },
): SyncUploadPayload {
  const snap = browseSnapshot
  const file = options?.file ?? uploadFile
  return {
    id: draftId ?? undefined,
    stage: gradeLevelLabel || '小学',
    subject: subjectLabel || snap.subjectKey,
    module: formData.module || '同步备课',
    type: formData.teachingType,
    gradeName: formData.gradeName,
    edition: formData.editionName,
    brandCode: snap.brandCode || undefined,
    catalogNodeId:
      snap.catalogNodeId && snap.catalogNodeId > 0 ? snap.catalogNodeId : undefined,
    subType: formData.subType || undefined,
    unitName: snap.unitNameForApi || formData.unitName || undefined,
    lessonName: snap.lessonName || formData.lessonName || undefined,
    title: options?.title || formData.title,
    originalFilename: file?.name,
    fileExt: ext,
    ossUrl: fileUrl || undefined,
    ossBucket: tempUploadResult?.ossBucket,
    ossObjectKey: tempUploadResult?.ossObjectKey || tempUploadResult?.filePath,
    fileSizeKb: fileUrl ? fileSizeKb : undefined,
    status,
    description: formData.description,
    allowPreview: formData.allowPreview,
    sort: formData.sortWeight,
    lessonPlanJson: formData.lessonPlanJson,
  }
}

/** 持久化草稿 */
export async function persistDraftRecord(
  payload: SyncUploadPayload,
): Promise<PrimaryChineseItem | undefined> {
  if (USE_MASTER_WRITE) {
    const writePayload = toResourceWritePayload(payload, { draft: true })
    if (payload.id) {
      const res = await resourcesApi.update(payload.id, writePayload)
      return unwrapData(res)
    }
    const res = await resourcesApi.create(writePayload)
    return unwrapData(res)
  }
  const res = await primaryChineseApi.saveDraft(payload)
  return unwrapData(res)
}

/** 持久化已发布的资源 */
export async function persistPublishedRecord(
  payload: SyncUploadPayload,
): Promise<PrimaryChineseItem | undefined> {
  if (USE_MASTER_WRITE) {
    const writePayload = toResourceWritePayload(payload)
    if (payload.id) {
      const res = await resourcesApi.update(payload.id, writePayload)
      return unwrapData(res)
    }
    const res = await resourcesApi.create(writePayload)
    return unwrapData(res)
  }
  const res = await primaryChineseApi.save(payload)
  return unwrapData(res)
}

/** 草稿提交审核 */
export async function finalizeDraftSubmit(
  payload: SyncUploadPayload,
  id: number,
  _status: number,
): Promise<PrimaryChineseItem | undefined> {
  if (USE_MASTER_WRITE) {
    const writePayload = toResourceWritePayload({ ...payload, status: RESOURCE_STATUS_DRAFT }, { draft: true })
    await resourcesApi.update(id, writePayload)
  } else {
    await primaryChineseApi.saveDraft(payload)
  }
  const res = await primaryChineseApi.submitDraft(id)
  return unwrapData(res)
}

/** 确保文件已上传 */
export async function ensureFileUploaded(
  file: File,
  tempUploadResult: UploadResult | null,
): Promise<UploadResult> {
  const matchesExisting =
    tempUploadResult &&
    tempUploadResult.fileName === file.name &&
    tempUploadResult.fileSize === file.size

  if (matchesExisting && tempUploadResult?.fileUrl) {
    return tempUploadResult
  }

  const fd = new FormData()
  fd.append('file', file)
  const uploadRes = await fileApi.upload(fd)
  return unwrapData(uploadRes)
}
