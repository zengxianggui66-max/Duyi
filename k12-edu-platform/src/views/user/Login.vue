<template>
  <div class="auth-form login-form">
    <p v-if="isAdminIntent" class="login-form__intent-hint">管理端登录 · 登录后按账号权限进入后台</p>

    <el-tabs v-model="activeTab" class="auth-tabs auth-tabs--login login-tabs" @tab-change="onTabChange">
      <el-tab-pane label="短信登录" name="sms">
        <el-form :model="smsForm" :rules="loginSmsRules" ref="smsRef" class="auth-form__fields">
          <el-form-item prop="phone">
            <el-input
              v-model="smsForm.phone"
              placeholder="请输入手机号"
              prefix-icon="Iphone"
              size="large"
              maxlength="11"
            />
          </el-form-item>
          <el-form-item prop="code">
            <div class="auth-sms-row">
              <el-input
                v-model="smsForm.code"
                placeholder="请输入验证码"
                prefix-icon="Message"
                size="large"
                maxlength="6"
              />
              <el-button
                plain
                size="large"
                :disabled="countdown > 0"
                :loading="sendingCode"
                class="auth-send-code-btn"
                @click="openCaptchaDialog"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="账号密码" name="account">
        <el-form :model="loginForm" :rules="loginRules" ref="loginRef" class="auth-form__fields">
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              prefix-icon="User"
              size="large"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <div class="auth-actions">
      <AuthAgreementCheckbox v-model="agreed" />
      <el-button
        type="primary"
        size="large"
        class="auth-submit-btn"
        :disabled="!agreed"
        :loading="loading"
        @click="handleSubmit"
      >
        登录
      </el-button>
      <div class="auth-forgot-row">
        <router-link
          v-show="activeTab === 'account'"
          :to="AUTH_FORGOT_PASSWORD_PATH"
          class="auth-forgot-link"
        >
          忘记密码？
        </router-link>
      </div>
    </div>

    <div class="auth-form__footer">
      <div class="auth-form__oauth">
        <button type="button" class="auth-oauth-link" title="微信登录" @click="handleOAuthLogin('wechat')">
          微信
        </button>
        <button type="button" class="auth-oauth-link auth-oauth-link--qq" title="QQ登录" @click="handleOAuthLogin('qq')">
          QQ
        </button>
        <button type="button" class="auth-oauth-link auth-oauth-link--wework" title="企业微信登录" @click="handleOAuthLogin('wework')">
          企微
        </button>
      </div>
      <router-link v-if="!isAdminIntent" :to="registerLink" class="auth-form__register-link">
        免费注册 ›
      </router-link>
    </div>

    <CaptchaDialog
      v-model="captchaDialogVisible"
      v-model:code="dialogCaptchaCode"
      :question="captchaQuestion"
      :captcha-type="captchaType"
      :loading="sendingCode"
      @confirm="confirmCaptchaAndSend"
      @refresh="() => loadCaptcha()"
      @closed="resetCaptchaDialog"
      @opened="onCaptchaDialogOpened"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store'
import { authApi } from '@/api/auth'
import { useAuthSms } from '@/composables/useAuthSms'
import { requireAuthAgreement } from '@/composables/useAuthAgreement'
import {
  navigateAfterLogin,
  resolveLoginIntent,
  resolveRedirectPath,
  type LoginIntent,
} from '@/composables/useAuthLoginFlow'
import { AUTH_FORGOT_PASSWORD_PATH } from '@/constants/authLegal'
import { trackAuthEvent } from '@/utils/authAnalytics'
import AuthAgreementCheckbox from '@/components/user/AuthAgreementCheckbox.vue'
import CaptchaDialog from '@/components/user/CaptchaDialog.vue'
import type { FormInstance } from 'element-plus'
import '@/assets/styles/auth-page.css'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const activeTab = ref<'sms' | 'account'>('sms')
const agreed = ref(false)
const captchaDialogVisible = ref(false)
const dialogCaptchaCode = ref('')

const {
  smsForm,
  loginSmsRules,
  sendingCode,
  countdown,
  captchaQuestion,
  captchaType,
  loadCaptcha,
  sendCodeWithCaptcha,
} = useAuthSms('login')

const loginForm = ref({
  username: '',
  password: '',
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const smsRef = ref<FormInstance>()
const loginRef = ref<FormInstance>()

const loginIntent = computed<LoginIntent>(() =>
  resolveLoginIntent(route.query as Record<string, unknown>),
)

const isAdminIntent = computed(() => loginIntent.value === 'admin')

const registerLink = computed(() => {
  const q: Record<string, string> = {}
  const redirect = route.query.redirect
  if (typeof redirect === 'string' && redirect.startsWith('/')) q.redirect = redirect
  return { path: '/register', query: q }
})

function postLoginOptions() {
  return {
    redirect: resolveRedirectPath(route.query.redirect),
    intent: loginIntent.value,
  }
}

async function finishLogin(needBindPhone?: boolean) {
  if (needBindPhone) {
    const q: Record<string, string> = {
      redirect: resolveRedirectPath(route.query.redirect),
    }
    if (isAdminIntent.value) q.intent = 'admin'
    await router.replace({ path: '/bind-phone', query: q })
    return
  }
  await navigateAfterLogin(router, postLoginOptions())
}

onMounted(async () => {
  if (!userStore.isLoggedIn) return
  await navigateAfterLogin(router, postLoginOptions())
})

function onTabChange(name: string | number) {
  trackAuthEvent('login_tab_switch', { tab: String(name) })
}

async function openCaptchaDialog() {
  if (!smsRef.value) return
  await smsRef.value.validateField(['phone'], (valid) => {
    if (!valid) return
    dialogCaptchaCode.value = ''
    captchaDialogVisible.value = true
    loadCaptcha()
  })
}

function onCaptchaDialogOpened() {
  trackAuthEvent('captcha_dialog_open', { page: 'login' })
}

async function confirmCaptchaAndSend() {
  trackAuthEvent('captcha_dialog_confirm', { page: 'login' })
  const ok = await sendCodeWithCaptcha(smsRef.value, dialogCaptchaCode.value)
  if (ok) {
    trackAuthEvent('sms_code_send_success', { page: 'login' })
    captchaDialogVisible.value = false
  } else {
    trackAuthEvent('sms_code_send_fail', { page: 'login' })
  }
}

function resetCaptchaDialog() {
  dialogCaptchaCode.value = ''
}

async function handleSubmit() {
  if (!requireAuthAgreement(agreed.value)) return
  if (activeTab.value === 'sms') {
    await handleSmsLogin()
  } else {
    await handlePasswordLogin()
  }
}

async function handleSmsLogin() {
  if (!smsRef.value) return
  await smsRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const result = await userStore.smsLogin(
        smsForm.value.phone,
        smsForm.value.code,
      )
      trackAuthEvent('login_submit_sms', { isNewUser: !!result?.isNewUser })
      ElMessage.success(result?.isNewUser ? '注册并登录成功' : '登录成功')
      await finishLogin(result?.needBindPhone || result?.userInfo?.needBindPhone)
    } catch (e: unknown) {
      const err = e as { message?: string }
      ElMessage.error(err.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}

async function handlePasswordLogin() {
  if (!loginRef.value) return
  await loginRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.login(loginForm.value.username, loginForm.value.password)
      trackAuthEvent('login_submit_password')
      ElMessage.success('登录成功')
      await finishLogin()
    } catch (e: unknown) {
      const err = e as { message?: string }
      ElMessage.error(err.message || '登录失败')
    } finally {
      loading.value = false
    }
  })
}

async function handleOAuthLogin(type: string) {
  if (!requireAuthAgreement(agreed.value)) return
  trackAuthEvent('login_oauth_start', { type })
  try {
    const redirectUri = `${window.location.origin}/login/oauth/callback`
    const result = await authApi.getOAuthUrl(type, redirectUri)
    const { authorizationUrl, state } = result.data.data
    sessionStorage.setItem('oauth_state', state)
    sessionStorage.setItem('oauth_type', type)
    sessionStorage.setItem('oauth_mode', 'login')
    sessionStorage.setItem('oauth_redirect', resolveRedirectPath(route.query.redirect))
    sessionStorage.setItem('oauth_intent', loginIntent.value)
    window.location.href = authorizationUrl
  } catch {
    // request 拦截器已展示错误提示
  }
}
</script>

<style scoped>
.login-form__intent-hint {
  margin: 0 0 12px;
  padding: 8px 12px;
  font-size: 13px;
  line-height: 1.5;
  color: #4361ee;
  background: #edf3ff;
  border-radius: 8px;
  text-align: center;
}
</style>
