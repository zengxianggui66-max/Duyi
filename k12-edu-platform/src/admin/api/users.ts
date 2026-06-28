import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'
import type { CollectItem } from '@/api/collect'

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

export interface AdminUserRow {
  id: number
  username: string
  nickname?: string
  avatar?: string
  phoneMasked?: string
  portalRole: string
  portalRoleName?: string
  status: number
  registerFrom?: string
  createTime?: string
  lastLoginTime?: string
  staff?: boolean
  adminRoles?: string[]
  adminRoleLabel?: string
}

export interface AdminOAuthBind {
  id: number
  oauthType: string
  oauthTypeName?: string
  oauthId?: string
  nickname?: string
  avatar?: string
  bindTime?: string
}

export interface AdminUserDetail extends AdminUserRow {
  email?: string
  gender?: number
  birthday?: string
  updateTime?: string
  oauthBinds?: AdminOAuthBind[]
  uploadCount?: number
  collectionCount?: number
}

export interface AdminUserUpload {
  id: number
  title: string
  stage?: string
  subject?: string
  module?: string
  type?: string
  status?: number
  statusLabel?: string
  uploadTime?: string
}

export interface AdminUserStats {
  uploadCount: number
  collectionCount: number
  viewCount?: number
  downloadCount?: number
  searchCount?: number
  loginCount?: number
}

export interface AdminUserAction {
  id: number
  actionType: string
  resourceId?: number
  resourceType?: string
  title?: string
  keyword?: string
  hitCount?: number
  ip?: string
  sourceApi?: string
  createTime?: string
}

export interface AdminUserLoginLog {
  id: number
  loginType?: string
  success?: number
  failReason?: string
  ip?: string
  createTime?: string
}

export interface AdminUserQuery {
  current?: number
  size?: number
  keyword?: string
  portalRole?: string
  status?: number
  staffOnly?: boolean
}

export function listAdminUsers(params: AdminUserQuery) {
  return request
    .get<ApiResult<PageResult<AdminUserRow>>>('/admin/users', { params })
    .then(unwrapData)
}

export function getAdminUserDetail(id: number) {
  return request.get<ApiResult<AdminUserDetail>>(`/admin/users/${id}`).then(unwrapData)
}

export function getAdminUserStats(id: number) {
  return request.get<ApiResult<AdminUserStats>>(`/admin/users/${id}/stats`).then(unwrapData)
}

export function updateAdminUser(id: number, data: Record<string, unknown>) {
  return request.put<ApiResult<void>>(`/admin/users/${id}`, data)
}

export function disableAdminUser(id: number) {
  return request.post<ApiResult<void>>(`/admin/users/${id}/disable`)
}

export function enableAdminUser(id: number) {
  return request.post<ApiResult<void>>(`/admin/users/${id}/enable`)
}

export function resetUserPassword(id: number) {
  return request.post<ApiResult<string>>(`/admin/users/${id}/reset-password`).then(unwrapData)
}

export function listUserUploads(id: number, current = 1, size = 10) {
  return request
    .get<ApiResult<PageResult<AdminUserUpload>>>(`/admin/users/${id}/resources`, {
      params: { current, size },
    })
    .then(unwrapData)
}

export function listUserCollections(id: number, current = 1, size = 10) {
  return request
    .get<ApiResult<PageResult<CollectItem>>>(`/admin/users/${id}/collections`, {
      params: { current, size },
    })
    .then(unwrapData)
}

export function listUserActions(
  id: number,
  actionType: 'view' | 'download' | 'search',
  current = 1,
  size = 10,
) {
  return request
    .get<ApiResult<PageResult<AdminUserAction>>>(`/admin/users/${id}/actions`, {
      params: { actionType, current, size },
    })
    .then(unwrapData)
}

export function listUserLoginLogs(id: number, current = 1, size = 10) {
  return request
    .get<ApiResult<PageResult<AdminUserLoginLog>>>(`/admin/users/${id}/login-logs`, {
      params: { current, size },
    })
    .then(unwrapData)
}

export interface AdminUserRemark {
  id: number
  userId: number
  adminUserId?: number
  adminUsername?: string
  content: string
  createTime?: string
}

export interface AdminUserOperationLog {
  id: number
  userId?: number
  username?: string
  action?: string
  permission?: string
  requestUri?: string
  status?: number
  createTime?: string
}

export function listUserRemarks(id: number, current = 1, size = 10) {
  return request
    .get<ApiResult<PageResult<AdminUserRemark>>>(`/admin/users/${id}/remarks`, {
      params: { current, size },
    })
    .then(unwrapData)
}

export function addUserRemark(id: number, content: string) {
  return request
    .post<ApiResult<AdminUserRemark>>(`/admin/users/${id}/remarks`, { content })
    .then(unwrapData)
}

export function listUserOperationLogs(id: number, current = 1, size = 10) {
  return request
    .get<ApiResult<PageResult<AdminUserOperationLog>>>(`/admin/users/${id}/operation-logs`, {
      params: { current, size },
    })
    .then(unwrapData)
}

export function batchUpdateUserStatus(ids: number[], status: 0 | 1) {
  return request
    .post<ApiResult<number>>('/admin/users/batch-status', { ids, status })
    .then(unwrapData)
}

export async function exportAdminUsers(params: AdminUserQuery) {
  const res = await request.get('/admin/users/export', {
    params: { ...params, staffOnly: params.staffOnly ?? false },
    responseType: 'blob',
  })
  const blob = res.data as Blob
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `users-export-${Date.now()}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

export const PORTAL_ROLE_OPTIONS = [
  { value: 'teacher', label: '教师' },
  { value: 'student', label: '学生' },
  { value: 'parent', label: '家长' },
  { value: 'pending', label: '待分配' },
]

export const REGISTER_FROM_LABEL: Record<string, string> = {
  account: '账号注册',
  phone: '手机号',
  oauth: '第三方',
}
