import type { App } from 'vue'
import type { Directive } from 'vue'
import { useAdminAuthStore } from '@/admin/store/adminAuth'

export function hasPermission(code: string): boolean {
  const store = useAdminAuthStore()
  return store.hasPermission(code)
}

export function hasAnyPermission(codes: string[]): boolean {
  return codes.some((code) => hasPermission(code))
}

export const vPermission: Directive<HTMLElement, string | string[]> = {
  mounted(el, binding) {
    const codes = Array.isArray(binding.value) ? binding.value : [binding.value]
    if (!hasAnyPermission(codes)) {
      el.style.display = 'none'
    }
  },
  updated(el, binding) {
    const codes = Array.isArray(binding.value) ? binding.value : [binding.value]
    el.style.display = hasAnyPermission(codes) ? '' : 'none'
  },
}

export function registerAdminPermissions(app: App) {
  app.directive('permission', vPermission)
}
