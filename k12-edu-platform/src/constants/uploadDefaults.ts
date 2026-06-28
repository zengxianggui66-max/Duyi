/** 宽表草稿状态（与后端 ResourceStatusConstants.DRAFT 一致） */
export const RESOURCE_STATUS_DRAFT = -1

/** 待审核 */
export const RESOURCE_STATUS_PENDING = 0

/**
 * 同步备课提交时的默认资源状态
 * - 0：待审核（默认，用户上传须走审核）
 * - 1：已发布（仅本地特殊联调时可设 VITE_SYNC_UPLOAD_DEFAULT_STATUS=1）
 */
export const SYNC_UPLOAD_DEFAULT_STATUS = Number(
  import.meta.env.VITE_SYNC_UPLOAD_DEFAULT_STATUS ?? '0',
)

export function isSyncUploadAutoPublished(): boolean {
  return SYNC_UPLOAD_DEFAULT_STATUS === 1
}
