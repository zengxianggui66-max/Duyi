<template>
  <div class="oauth-callback">
    <template v-if="status === 'loading'">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>{{ message }}</p>
    </template>
    <template v-else>
      <el-icon :size="40" class="is-error"><WarningFilled /></el-icon>
      <p class="error-title">授权处理失败</p>
      <p class="error-desc">{{ message }}</p>
      <div class="error-actions">
        <el-button @click="goBack">返回上一页</el-button>
        <el-button type="primary" @click="goLogin">去登录页</el-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading, WarningFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/store'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const message = ref('正在处理授权…')
const status = ref<'loading' | 'error'>('loading')

import {
  navigateAfterLogin,
  resolveRedirectPath,
  type LoginIntent,
} from '@/composables/useAuthLoginFlow'

function resolveOAuthCallbackError(error: unknown, fallback = '授权失败，请重试') {
  const err = error as {
    message?: string
    response?: {
      data?: {
        code?: number | string
        message?: string
        msg?: string
      }
    }
  }
  const code = String(err?.response?.data?.code ?? '')
  const backendMsg = err?.response?.data?.message || err?.response?.data?.msg || err?.message || ''
  const text = `${backendMsg} ${code}`.toLowerCase()

  if (text.includes('state') && (text.includes('失效') || text.includes('expire') || text.includes('invalid'))) {
    return '授权状态已失效，请重新发起授权'
  }
  if (text.includes('access_denied') || text.includes('授权取消') || text.includes('cancel')) {
    return '你已取消授权，本次未完成登录/绑定'
  }
  if (text.includes('已被其他用户绑定') || text.includes('conflict') || text.includes('duplicate')) {
    return '该第三方账号已被其他用户绑定，请更换账号后重试'
  }
  if (text.includes('参数') && text.includes('缺失')) {
    return '授权回调参数不完整，请重新发起授权'
  }
  return backendMsg || fallback
}

function fail(msg: string) {
  status.value = 'error'
  message.value = msg
  ElMessage.error(msg)
}

function goBack() {
  const mode = sessionStorage.getItem('oauth_mode') || 'login'
  const fallback = mode === 'bind' ? '/profile?tab=security' : '/login'
  router.replace(fallback)
}

function goLogin() {
  router.replace('/login')
}

onMounted(async () => {
  const code = route.query.code as string | undefined
  const state = route.query.state as string | undefined
  const savedState = sessionStorage.getItem('oauth_state')
  const type = sessionStorage.getItem('oauth_type')
  const mode = sessionStorage.getItem('oauth_mode') || 'login'
  const redirect = resolveRedirectPath(
    sessionStorage.getItem('oauth_redirect') || (route.query.redirect as string),
  )
  const oauthIntent = (sessionStorage.getItem('oauth_intent') || 'front') as LoginIntent

  if (!code || !state || !type) {
    fail('授权回调参数缺失，请重新发起授权')
    return
  }
  if (state !== savedState) {
    fail('授权状态校验失败，请重新授权')
    return
  }

  sessionStorage.removeItem('oauth_state')
  sessionStorage.removeItem('oauth_type')
  sessionStorage.removeItem('oauth_mode')
  sessionStorage.removeItem('oauth_redirect')
  sessionStorage.removeItem('oauth_intent')

  try {
    const data = await userStore.oauthLogin(code, type, state)

    if ('bindSuccess' in data && data.bindSuccess) {
      ElMessage.success('绑定成功')
      await userStore.fetchCurrentUser()
      const bindBack = mode === 'bind' ? resolveRedirectPath(redirect, '/profile?tab=security') : redirect
      router.replace(bindBack)
      return
    }

    if ('needsRoleSelection' in data && data.needsRoleSelection) {
      message.value = '正在完成账号注册…'
      const completed = await userStore.completeOAuthRegister({
        type: data.oauthType!,
        oauthId: data.oauthId!,
        nickname: data.nickname,
        avatar: data.avatar,
      })
      sessionStorage.removeItem('oauth_pending')
      ElMessage.success('注册成功')
      if (completed?.needBindPhone || completed?.userInfo?.needBindPhone) {
        const q: Record<string, string> = { redirect }
        if (oauthIntent === 'admin') q.intent = 'admin'
        router.replace({ path: '/bind-phone', query: q })
      } else {
        await navigateAfterLogin(router, { redirect, intent: oauthIntent })
      }
      return
    }

    ElMessage.success('登录成功')
    const loginResult = data as { needBindPhone?: boolean; userInfo?: { needBindPhone?: boolean } }
    if (loginResult.needBindPhone || loginResult.userInfo?.needBindPhone) {
      const q: Record<string, string> = { redirect }
      if (oauthIntent === 'admin') q.intent = 'admin'
      router.replace({ path: '/bind-phone', query: q })
    } else {
      await navigateAfterLogin(router, { redirect, intent: oauthIntent })
    }
  } catch (e: unknown) {
    fail(resolveOAuthCallbackError(e))
  }
})
</script>

<style scoped>
.oauth-callback {
  min-height: 240px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: var(--text-secondary);
}
.is-error {
  color: var(--el-color-danger);
}
.error-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}
.error-desc {
  margin: 0;
}
.error-actions {
  display: flex;
  gap: 10px;
}
</style>
