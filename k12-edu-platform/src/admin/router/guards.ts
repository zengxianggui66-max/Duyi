import type { Router } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAdminAuthStore } from '@/admin/store/adminAuth'
import { buildLoginRoute } from '@/composables/useAuthLoginFlow'

export function setupAdminGuards(router: Router) {
  router.beforeEach(async (to, _from, next) => {
    if (!to.path.startsWith('/admin')) {
      next()
      return
    }

    const userStore = useUserStore()
    const adminStore = useAdminAuthStore()

    if (to.meta.public) {
      next()
      return
    }

    if (!userStore.token) {
      next(buildLoginRoute({ intent: 'admin', redirect: to.fullPath }))
      return
    }

    try {
      if (!adminStore.loaded) {
        await adminStore.bootstrap()
      }
    } catch {
      adminStore.reset()
      if (userStore.isLoggedIn && to.path !== '/admin/403') {
        next('/admin/403')
        return
      }
      next(buildLoginRoute({ intent: 'admin', redirect: to.fullPath }))
      return
    }

    if (!adminStore.isStaff) {
      next('/admin/403')
      return
    }

    const permission = to.meta.permission as string | undefined
    if (permission === 'admin:analytics:view') {
      if (!adminStore.hasAnalyticsView()) {
        next('/admin/403')
        return
      }
    } else if (permission && !adminStore.hasPermission(permission)) {
      next('/admin/403')
      return
    }

    next()
  })
}
