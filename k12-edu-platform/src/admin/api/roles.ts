import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'

export interface AdminRole {
  id: number
  code: string
  name: string
  description?: string
  status: number
  isBuiltin: number
  permissionIds: number[]
}

export interface AdminPermissionNode {
  id: number
  code: string
  name: string
  type: string
  module?: string
  parentId?: number
  sort?: number
  children?: AdminPermissionNode[]
}

export function listRoles() {
  return request.get<ApiResult<AdminRole[]>>('/admin/roles').then(unwrapData)
}

export function createRole(data: { code: string; name: string; description?: string; status?: number }) {
  return request.post<ApiResult<AdminRole>>('/admin/roles', data).then(unwrapData)
}

export function updateRole(id: number, data: { name?: string; description?: string; status?: number }) {
  return request.put<ApiResult<AdminRole>>(`/admin/roles/${id}`, data).then(unwrapData)
}

export function getPermissionTree() {
  return request.get<ApiResult<AdminPermissionNode[]>>('/admin/permissions/tree').then(unwrapData)
}

export function assignRolePermissions(roleId: number, permissionIds: number[]) {
  return request.put<ApiResult<void>>(`/admin/roles/${roleId}/permissions`, { permissionIds })
}

export function getUserRoleIds(userId: number) {
  return request.get<ApiResult<number[]>>(`/admin/users/${userId}/roles`).then(unwrapData)
}

export function assignUserRoles(userId: number, roleIds: number[]) {
  return request.put<ApiResult<void>>(`/admin/users/${userId}/roles`, { roleIds })
}
