import axios, { AxiosError } from 'axios'
import type { AxiosInstance, AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

/** 设置为 true 时不弹出全局错误提示，由调用方自行处理。 */
export type SilentRequestConfig = AxiosRequestConfig & { silentError?: boolean }

declare module 'axios' {
  interface AxiosRequestConfig {
    silentError?: boolean
  }
  interface InternalAxiosRequestConfig {
    silentError?: boolean
  }
}

let lastErrorMessage = ''
let lastErrorAt = 0

function showGlobalError(message: string) {
  const now = Date.now()
  if (message === lastErrorMessage && now - lastErrorAt < 1500) return
  lastErrorMessage = message
  lastErrorAt = now
  ElMessage.error(message)
}

export interface ApiResult<T = any> {
  code: number
  message: string
  data: T
}

export interface PageData<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export function unwrapData<T>(response: { data: ApiResult<T> }): T {
  return response.data.data
}

class RequestManager {
  private serialMap = new Map<string, number>()
  private abortMap = new Map<string, AbortController>()

  makeKey(method = 'get', url: string, params?: Record<string, unknown>): string {
    const sorted = params ? this.sortParams(params) : {}
    return `${method}:${url}:${JSON.stringify(sorted)}`
  }

  private sortParams(params: Record<string, unknown>): Record<string, unknown> {
    const sorted: Record<string, unknown> = {}
    Object.keys(params)
      .sort()
      .forEach((key) => {
        const value = params[key]
        if (value !== undefined && value !== null) {
          sorted[key] = value
        }
      })
    return sorted
  }

  abort(key: string): boolean {
    const controller = this.abortMap.get(key)
    if (!controller) return false
    controller.abort()
    this.abortMap.delete(key)
    return true
  }

  createSignal(method: string, url: string, params?: Record<string, unknown>) {
    const key = this.makeKey(method, url, params)
    this.abort(key)
    const controller = new AbortController()
    this.abortMap.set(key, controller)
    return { signal: controller.signal, key }
  }

  cleanup(key: string): void {
    this.abortMap.delete(key)
  }

  nextSerial(composableId: string): number {
    const next = (this.serialMap.get(composableId) || 0) + 1
    this.serialMap.set(composableId, next)
    return next
  }

  isLatest(composableId: string, serial: number): boolean {
    return this.serialMap.get(composableId) === serial
  }
}

export const requestManager = new RequestManager()

interface CacheEntry {
  data: any
  timestamp: number
}

class LRURequestCache {
  private cache = new Map<string, CacheEntry>()
  private maxSize: number
  private ttl: number

  constructor(maxSize = 30, ttl = 5 * 60 * 1000) {
    this.maxSize = maxSize
    this.ttl = ttl
  }

  get<T>(key: string): T | null {
    const entry = this.cache.get(key)
    if (!entry) return null
    if (Date.now() - entry.timestamp > this.ttl) {
      this.cache.delete(key)
      return null
    }
    this.cache.delete(key)
    this.cache.set(key, entry)
    return entry.data as T
  }

  set(key: string, data: unknown): void {
    if (this.cache.has(key)) {
      this.cache.delete(key)
    } else if (this.cache.size >= this.maxSize) {
      const oldestKey = this.cache.keys().next().value
      if (oldestKey !== undefined) this.cache.delete(oldestKey)
    }
    this.cache.set(key, { data, timestamp: Date.now() })
  }

  invalidatePrefix(prefix: string): void {
    for (const key of this.cache.keys()) {
      if (key.startsWith(prefix)) this.cache.delete(key)
    }
  }

  clear(): void {
    this.cache.clear()
  }
}

export const requestCache = new LRURequestCache()

function resolveErrorMessage(error: AxiosError): string {
  if (error.response) {
    const res = error.response.data as Partial<ApiResult> | undefined
    switch (error.response.status) {
      case 401:
        return res?.message || '登录已过期，请重新登录'
      case 403:
        return res?.message || '没有权限访问该资源'
      case 404:
        return res?.message || '请求的资源不存在'
      case 500:
        return res?.message || '服务器内部错误，请联系管理员'
      default:
        return res?.message || '网络请求失败，请稍后重试'
    }
  }

  if (error.request) {
    return '网络连接失败，请检查网络设置'
  }

  return error.message || '网络请求失败，请稍后重试'
}

const createRequestInstance = (): AxiosInstance => {
  const instance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE || '/api',
    timeout: 15000,
    headers: {
      'Content-Type': 'application/json',
    },
    paramsSerializer: (params) => {
      const parts: string[] = []
      for (const [key, value] of Object.entries(params)) {
        if (value !== undefined && value !== null) {
          parts.push(`${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
        }
      }
      return parts.join('&')
    },
  })

  instance.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem('token')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
      return config
    },
    (error) => Promise.reject(error),
  )

  instance.interceptors.response.use(
    (response) => {
      const res = response.data as ApiResult
      if (res.code !== 200) {
        const silent = (response.config as SilentRequestConfig)?.silentError
        const message = res.message || '请求失败'
        if (!silent) {
          showGlobalError(message)
        }
        return Promise.reject(new Error(message))
      }
      return response
    },
    async (error: AxiosError) => {
      if (axios.isCancel(error) || error.name === 'CanceledError' || (error as any)?.__CANCEL__) {
        return Promise.reject(error)
      }

      const errorMessage = resolveErrorMessage(error)

      if (error.response?.status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        ElMessage.error(errorMessage)
        setTimeout(() => {
          const loginPath = window.location.pathname.startsWith('/admin')
            ? `/login?intent=admin&redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`
            : '/login'
          window.location.href = loginPath
        }, 1500)
        return Promise.reject(error)
      }

      const silent = (error.config as SilentRequestConfig | undefined)?.silentError
      if (!silent) {
        showGlobalError(errorMessage)
      }
      return Promise.reject(error)
    },
  )

  return instance
}

export const request = createRequestInstance()
