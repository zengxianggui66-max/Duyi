export const AUDIT_STATUS = {
  DRAFT: -1,
  PENDING: 0,
  APPROVED: 1,
  REJECTED: 2,
  RECHECKING: 3,
} as const

export const PUBLISH_STATUS = {
  UNPUBLISHED: 0,
  PUBLISHED: 1,
  OFFLINE: 2,
  SCHEDULED: 3,
  RECYCLED: 4,
} as const

export const LEGACY_STATUS = {
  DRAFT: -1,
  PENDING: 0,
  PUBLISHED: 1,
  REJECTED: 2,
  OFFLINE: 3,
  DELETED: 4,
} as const

export interface ResourceStatusLike {
  legacyStatus?: number
  status?: number
  auditStatus?: number
  publishStatus?: number
  auditStatusLabel?: string
  shelfStatusLabel?: string
}

export function resolveAuditStatus(row: ResourceStatusLike): number {
  if (typeof row.auditStatus === 'number') return row.auditStatus

  const legacy = typeof row.legacyStatus === 'number' ? row.legacyStatus : row.status
  switch (legacy) {
    case LEGACY_STATUS.DRAFT:
      return AUDIT_STATUS.DRAFT
    case LEGACY_STATUS.PENDING:
      return AUDIT_STATUS.PENDING
    case LEGACY_STATUS.REJECTED:
      return AUDIT_STATUS.REJECTED
    case LEGACY_STATUS.PUBLISHED:
    case LEGACY_STATUS.OFFLINE:
    case LEGACY_STATUS.DELETED:
      return AUDIT_STATUS.APPROVED
    default:
      return AUDIT_STATUS.PENDING
  }
}

export function resolvePublishStatus(row: ResourceStatusLike): number {
  if (typeof row.publishStatus === 'number') return row.publishStatus

  const legacy = typeof row.legacyStatus === 'number' ? row.legacyStatus : row.status
  switch (legacy) {
    case LEGACY_STATUS.PUBLISHED:
      return PUBLISH_STATUS.PUBLISHED
    case LEGACY_STATUS.OFFLINE:
      return PUBLISH_STATUS.OFFLINE
    case LEGACY_STATUS.DELETED:
      return PUBLISH_STATUS.RECYCLED
    default:
      return PUBLISH_STATUS.UNPUBLISHED
  }
}

export function getAuditStatusLabel(row: ResourceStatusLike): string {
  if (row.auditStatusLabel) return row.auditStatusLabel
  switch (resolveAuditStatus(row)) {
    case AUDIT_STATUS.DRAFT:
      return '草稿'
    case AUDIT_STATUS.PENDING:
      return '待审核'
    case AUDIT_STATUS.APPROVED:
      return '审核通过'
    case AUDIT_STATUS.REJECTED:
      return '已驳回'
    case AUDIT_STATUS.RECHECKING:
      return '复审中'
    default:
      return '未知'
  }
}

export function getPublishStatusLabel(row: ResourceStatusLike): string {
  if (row.shelfStatusLabel) return row.shelfStatusLabel
  switch (resolvePublishStatus(row)) {
    case PUBLISH_STATUS.PUBLISHED:
      return '已上架'
    case PUBLISH_STATUS.OFFLINE:
      return '已下架'
    case PUBLISH_STATUS.SCHEDULED:
      return '定时上架'
    case PUBLISH_STATUS.RECYCLED:
      return '回收站'
    default:
      return '未上架'
  }
}

export function getAuditStatusTagType(
  row: ResourceStatusLike,
): 'success' | 'warning' | 'danger' | 'info' {
  switch (resolveAuditStatus(row)) {
    case AUDIT_STATUS.APPROVED:
      return 'success'
    case AUDIT_STATUS.PENDING:
    case AUDIT_STATUS.RECHECKING:
      return 'warning'
    case AUDIT_STATUS.REJECTED:
      return 'danger'
    default:
      return 'info'
  }
}

export function getPublishStatusTagType(
  row: ResourceStatusLike,
): 'success' | 'warning' | 'danger' | 'info' {
  switch (resolvePublishStatus(row)) {
    case PUBLISH_STATUS.PUBLISHED:
      return 'success'
    case PUBLISH_STATUS.SCHEDULED:
      return 'warning'
    case PUBLISH_STATUS.RECYCLED:
      return 'danger'
    default:
      return 'info'
  }
}

export function canPublish(row: ResourceStatusLike): boolean {
  return (
    resolveAuditStatus(row) === AUDIT_STATUS.APPROVED &&
    resolvePublishStatus(row) === PUBLISH_STATUS.UNPUBLISHED
  )
}

export function canOffline(row: ResourceStatusLike): boolean {
  return resolvePublishStatus(row) === PUBLISH_STATUS.PUBLISHED
}

export function canRecycle(row: ResourceStatusLike): boolean {
  const publish = resolvePublishStatus(row)
  return publish !== PUBLISH_STATUS.PUBLISHED && publish !== PUBLISH_STATUS.RECYCLED
}
