import { request, unwrapData } from '@/api/request'
import type { ApiResult } from '@/api/request'
import type { OperationLog, PageResult } from '@/admin/api/operationLogs'

export interface SystemLoginLog {
  id: number
  userId?: number
  username?: string
  loginType?: string
  success?: number
  failReason?: string
  ip?: string
  userAgent?: string
  createTime?: string
}

export interface ConfigField {
  key: string
  valueType?: string
  description?: string
  requiresRestart?: boolean
  configured?: boolean
}

export interface SystemConfigGroup {
  groupCode: string
  values: Record<string, unknown>
  fields?: ConfigField[]
}

export interface StorageStatus {
  provider?: string
  configured?: boolean
  reachable?: boolean
  bucket?: string
  latencyMs?: number
  localFallback?: { path?: string; writable?: boolean }
}

export interface PreviewStatus {
  enabled?: boolean
  libreoffice?: { configured?: boolean; reachable?: boolean; path?: string }
  poiFallback?: boolean
  asyncEnabled?: boolean
  sampleProbe?: string
}

export interface FeatureFlagItem {
  key: string
  label?: string
  enabled?: boolean
  scope?: 'runtime' | 'buildTime'
  description?: string
}

export function listSystemLogs(params: {
  current?: number
  size?: number
  module?: string
  username?: string
  action?: string
  status?: number
}) {
  return request
    .get<ApiResult<PageResult<OperationLog>>>('/admin/system/logs', { params })
    .then(unwrapData)
}

export function listSystemLoginLogs(params: {
  current?: number
  size?: number
  username?: string
  loginType?: string
  success?: number
  staffOnly?: boolean
}) {
  return request
    .get<ApiResult<PageResult<SystemLoginLog>>>('/admin/system/login-logs', { params })
    .then(unwrapData)
}

export function getSystemConfig(group: string) {
  return request
    .get<ApiResult<SystemConfigGroup>>('/admin/system/config', { params: { group } })
    .then(unwrapData)
}

export function updateSystemConfig(group: string, values: Record<string, unknown>) {
  return request
    .put<ApiResult<SystemConfigGroup>>('/admin/system/config', values, { params: { group } })
    .then(unwrapData)
}

export function getStorageStatus() {
  return request.get<ApiResult<StorageStatus>>('/admin/system/storage/status').then(unwrapData)
}

export function getPreviewStatus() {
  return request.get<ApiResult<PreviewStatus>>('/admin/system/preview/status').then(unwrapData)
}

export function getFeatureFlags() {
  return request.get<ApiResult<{ flags: FeatureFlagItem[] }>>('/admin/system/feature-flags').then(unwrapData)
}

export function getPublicFeatureFlags() {
  return request.get<ApiResult<Record<string, boolean>>>('/public/feature-flags').then(unwrapData)
}
