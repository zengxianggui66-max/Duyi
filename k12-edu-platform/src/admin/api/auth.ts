/**
 * 管理端 API
 */
import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'

export interface AdminMe {
  id: number
  username: string
  nickname: string
  avatar?: string
  role: string
  roles: string[]
  permissions: string[]
}

export interface AdminMenu {
  id: number
  parentId: number
  title: string
  path: string
  name: string
  icon?: string
  component?: string
  permissionCode?: string
  sort?: number
  children?: AdminMenu[]
}

export function getAdminMe() {
  return request.get<ApiResult<AdminMe>>('/admin/auth/me').then(unwrapData)
}

export function getAdminMenus() {
  return request.get<ApiResult<AdminMenu[]>>('/admin/menus').then(unwrapData)
}

export function getAdminPermissions() {
  return request.get<ApiResult<string[]>>('/admin/permissions').then(unwrapData)
}
