import { defineStore } from 'pinia'

import { ref, computed } from 'vue'

import { getAdminMe, getAdminMenus, getAdminPermissions, type AdminMe, type AdminMenu } from '@/admin/api/auth'

import { useUserStore } from '@/store/user'



export const useAdminAuthStore = defineStore('adminAuth', () => {

  const user = ref<AdminMe | null>(null)

  const menus = ref<AdminMenu[]>([])

  const permissions = ref<string[]>([])

  const loaded = ref(false)



  /** staff 入口：user.role=admin 且已分配 sys_role */

  const isStaff = computed(

    () => user.value?.role === 'admin' && (user.value?.roles?.length ?? 0) > 0

  )



  const isSuperAdmin = computed(() => user.value?.roles?.includes('super_admin') ?? false)



  /** @deprecated 使用 isStaff */

  const isAdmin = isStaff



  async function refreshMenus() {
    menus.value = await getAdminMenus()
  }

  async function bootstrap() {

    const me = await getAdminMe()

    user.value = me

    const [menuTree, permList] = await Promise.all([getAdminMenus(), getAdminPermissions()])

    menus.value = menuTree

    permissions.value = permList

    loaded.value = true

    return me

  }



  function hasPermission(code: string) {
    if (isSuperAdmin.value) return true
    return permissions.value.includes(code)
  }

  /** 数据分析：新权限或旧控制台权限均可访问 */
  function hasAnalyticsView() {
    return hasPermission('admin:analytics:view') || hasPermission('admin:dashboard:view')
  }



  function reset() {

    user.value = null

    menus.value = []

    permissions.value = []

    loaded.value = false

  }



  async function logout() {

    const userStore = useUserStore()

    await userStore.logout()

    reset()

  }



  return {

    user,

    menus,

    permissions,

    loaded,

    isStaff,

    isSuperAdmin,

    isAdmin,

    bootstrap,

    refreshMenus,

    hasPermission,

    hasAnalyticsView,

    reset,

    logout,

  }

})

