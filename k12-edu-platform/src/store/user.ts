/**
 * 用户状态管理
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'
import type { UserInfo, LoginResult } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const storedToken = localStorage.getItem('token')
  const user = ref<UserInfo | null>(null)
  const token = ref<string>(storedToken && storedToken !== 'test-token-dev' ? storedToken : '')

  const isLoggedIn = computed(() => !!token.value)
  const isPremium = computed(() => user.value?.memberLevel === 'premium' || user.value?.memberLevel === 1)
  const nickname = computed(() => user.value?.nickname || user.value?.username || '未登录')

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('token', t)
  }

  function setUser(u: UserInfo | null) {
    user.value = u
  }

  function applyLoginResult(data: LoginResult) {
    const t = data.token || data.accessToken
    if (t) {
      setToken(t)
    }
    if (data.userInfo) {
      setUser(data.userInfo)
    }
    import('@/store/prepBasket').then(({ usePrepBasketStore }) => {
      usePrepBasketStore().mergeLocalOnLogin()
    })
    return data
  }

  async function logout() {
    try {
      if (token.value) {
        await authApi.logout()
      }
    } catch {
      // 忽略登出接口错误，仍清除本地状态
    } finally {
      token.value = ''
      user.value = null
      localStorage.removeItem('token')
    }
  }

  async function login(username: string, password: string, role?: string) {
    const res = await authApi.login({ username, password, role })
    return applyLoginResult(res.data.data)
  }

  async function smsLogin(phone: string, code: string, role?: string) {
    const res = await authApi.smsLogin({ phone, code, role })
    const data = applyLoginResult(res.data.data)
    return { ...data, isNewUser: res.data.data?.isNewUser }
  }

  async function sendSmsCode(
    phone: string,
    captchaKey: string,
    captchaCode: string,
    type = 'login'
  ) {
    await authApi.sendSmsCode({ phone, type, captchaKey, captchaCode })
  }

  async function oauthLogin(code: string, type: string, state: string) {
    const res = await authApi.oauthLogin({ code, type, state })
    const data = res.data.data as LoginResult
    if (data.bindSuccess) {
      return { bindSuccess: true as const }
    }
    if (data.needsRoleSelection) {
      return {
        needsRoleSelection: true as const,
        oauthType: data.oauthType!,
        oauthId: data.oauthId!,
        nickname: data.nickname,
        avatar: data.avatar,
      }
    }
    return applyLoginResult(data)
  }

  async function completeOAuthRegister(data: {
    type: string
    oauthId: string
    nickname?: string
    avatar?: string
    role?: string
  }) {
    const res = await authApi.completeOAuthRegister(data)
    return applyLoginResult(res.data.data)
  }

  async function listOAuthBinds() {
    const res = await authApi.listOAuthBinds()
    return res.data.data || []
  }

  async function unbindOAuth(type: string) {
    await authApi.unbindOAuth(type)
    if (user.value?.oauthBinds) {
      user.value = {
        ...user.value,
        oauthBinds: user.value.oauthBinds.filter((t) => t !== type),
      }
    }
  }

  async function bindPhone(phone: string, code: string) {
    await authApi.bindPhone({ phone, code })
    if (user.value) {
      user.value = { ...user.value, phone, needBindPhone: false }
    }
  }

  async function register(data: {
    username: string
    password: string
    email?: string
    nickname?: string
    agreedToTerms: string
  }) {
    const res = await authApi.register(data)
    return applyLoginResult(res.data.data)
  }

  async function smsRegister(data: {
    phone: string
    code: string
    nickname?: string
    agreedToTerms: string
  }) {
    const res = await authApi.smsRegister(data)
    return applyLoginResult(res.data.data)
  }

  async function fetchCurrentUser() {
    if (!token.value) return
    try {
      const res = await authApi.getCurrentUser()
      setUser(res.data.data as UserInfo)
    } catch {
      await logout()
    }
  }

  if (token.value) {
    fetchCurrentUser()
  }

  return {
    user,
    token,
    isLoggedIn,
    isPremium,
    nickname,
    setToken,
    setUser,
    applyLoginResult,
    logout,
    login,
    smsLogin,
    sendSmsCode,
    oauthLogin,
    completeOAuthRegister,
    listOAuthBinds,
    unbindOAuth,
    bindPhone,
    register,
    smsRegister,
    fetchCurrentUser,
  }
})
