import type { RouteLocationRaw, Router } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { useAdminAuthStore } from '@/admin/store/adminAuth'

export type LoginIntent = 'front' | 'admin'

export const DEFAULT_ADMIN_HOME = '/admin/analytics/overview'
export const DEFAULT_FRONT_HOME = '/'

/** 解析站内 redirect，拒绝开放重定向 */
export function resolveRedirectPath(raw: unknown, fallback = DEFAULT_FRONT_HOME): string {
  if (typeof raw !== 'string' || !raw.startsWith('/') || raw.startsWith('//')) {
    return fallback
  }
  return raw
}

/** 是否应进入管理端（intent=admin 或 redirect 指向 /admin） */
export function isAdminLoginTarget(redirect: string, intent?: unknown): boolean {
  if (intent === 'admin') return true
  return redirect.startsWith('/admin') && redirect !== '/admin/403'
}

export function resolveLoginIntent(query: Record<string, unknown>): LoginIntent {
  const redirect = resolveRedirectPath(query.redirect, DEFAULT_FRONT_HOME)
  return isAdminLoginTarget(redirect, query.intent) ? 'admin' : 'front'
}

/** 构建统一登录页路由（前台 / 管理端共用） */
export function buildLoginRoute(options?: {
  intent?: LoginIntent
  redirect?: string
}): RouteLocationRaw {
  const query: Record<string, string> = {}
  if (options?.intent === 'admin') {
    query.intent = 'admin'
  }
  if (options?.redirect && options.redirect.startsWith('/')) {
    query.redirect = options.redirect
  }
  return { path: '/login', query }
}

export interface PostLoginOptions {
  redirect?: string
  intent?: LoginIntent
}

/**
 * 登录成功后按能力分流：
 * - 管理端目标：bootstrap RBAC，无 staff 权限则登出并提示
 * - 前台目标：直接进入 redirect 或首页
 */
export async function navigateAfterLogin(
  router: Router,
  options: PostLoginOptions = {},
): Promise<boolean> {
  const userStore = useUserStore()
  const adminStore = useAdminAuthStore()

  const redirect = resolveRedirectPath(options.redirect, DEFAULT_FRONT_HOME)
  const adminTarget = isAdminLoginTarget(redirect, options.intent)

  if (adminTarget) {
    adminStore.reset()
    try {
      await adminStore.bootstrap()
    } catch {
      adminStore.reset()
      await userStore.logout()
      ElMessage.error('管理端权限校验失败，请重新登录')
      return false
    }
    if (!adminStore.isStaff) {
      await userStore.logout()
      adminStore.reset()
      ElMessage.error('该账号无管理端权限')
      return false
    }
    const dest = redirect.startsWith('/admin') ? redirect : DEFAULT_ADMIN_HOME
    await router.replace(dest)
    return true
  }

  await router.replace(redirect)
  return true
}

/** 401 / 未登录访问管理端时使用的登录 URL */
export function adminLoginUrl(redirectPath?: string): string {
  const params = new URLSearchParams({ intent: 'admin' })
  if (redirectPath && redirectPath.startsWith('/')) {
    params.set('redirect', redirectPath)
  }
  return `/login?${params.toString()}`
}
