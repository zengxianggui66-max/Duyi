<template>
  <div class="auth-form register-form">
    <el-tabs v-model="activeTab" class="auth-tabs auth-tabs--register register-tabs" @tab-change="onTabChange">
      <el-tab-pane label="手机号注册" name="phone">
        <el-form :model="smsForm" :rules="loginSmsRules" ref="smsRef" class="auth-form__fields">
          <el-form-item prop="phone">
            <el-input v-model="smsForm.phone" placeholder="请输入手机号" prefix-icon="Iphone" size="large" maxlength="11" />
          </el-form-item>

          <el-form-item prop="code">
            <div class="auth-sms-row">
              <el-input v-model="smsForm.code" placeholder="请输入验证码" prefix-icon="Message" size="large" maxlength="6" />
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

          <el-form-item prop="nickname">
            <el-input v-model="smsForm.nickname" placeholder="昵称（选填）" size="large" maxlength="20" />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="账号密码" name="account">
        <el-form :model="accountForm" :rules="accountRules" ref="accountRef" class="auth-form__fields">
          <el-form-item prop="username">
            <el-input v-model="accountForm.username" placeholder="用户名 3-20 个字符" prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="accountForm.password" type="password" placeholder="密码至少 6 位" prefix-icon="Lock" size="large" show-password />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="accountForm.confirmPassword" type="password" placeholder="再次输入密码" prefix-icon="Lock" size="large" show-password />
          </el-form-item>
          <el-form-item prop="email">
            <el-input v-model="accountForm.email" placeholder="邮箱（选填，用于找回密码）" prefix-icon="Message" size="large" />
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
        注册并登录
      </el-button>
    </div>

    <div class="auth-form__footer auth-form__footer--register">
      <router-link :to="loginLink" class="auth-form__register-link">立即登录 ›</router-link>
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
import { useAuthSms } from '@/composables/useAuthSms'
import { requireAuthAgreement } from '@/composables/useAuthAgreement'
import { trackAuthEvent } from '@/utils/authAnalytics'
import AuthAgreementCheckbox from '@/components/user/AuthAgreementCheckbox.vue'
import CaptchaDialog from '@/components/user/CaptchaDialog.vue'
import type { FormInstance } from 'element-plus'
import '@/assets/styles/auth-page.css'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const activeTab = ref('phone')
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
} = useAuthSms('register')

const smsRef = ref<FormInstance>()
const accountRef = ref<FormInstance>()

const accountForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  email: '',
})

const accountRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名为 3-20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_: unknown, value: string, callback: (e?: Error) => void) => {
        if (value !== accountForm.value.password) callback(new Error('两次密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

const loginLink = computed(() => {
  const q: Record<string, string> = {}
  const redirect = route.query.redirect
  if (typeof redirect === 'string' && redirect.startsWith('/')) q.redirect = redirect
  return { path: '/login', query: q }
})

onMounted(() => {
  if (userStore.isLoggedIn) {
    router.replace(getRedirect())
  }
})

function getRedirect(): string {
  const r = route.query.redirect
  return typeof r === 'string' && r.startsWith('/') ? r : '/'
}

function onTabChange(name: string | number) {
  trackAuthEvent('register_tab_switch', { tab: String(name) })
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
  trackAuthEvent('captcha_dialog_open', { page: 'register' })
}

async function confirmCaptchaAndSend() {
  trackAuthEvent('captcha_dialog_confirm', { page: 'register' })
  const ok = await sendCodeWithCaptcha(smsRef.value, dialogCaptchaCode.value)
  if (ok) {
    trackAuthEvent('sms_code_send_success', { page: 'register' })
    captchaDialogVisible.value = false
  } else {
    trackAuthEvent('sms_code_send_fail', { page: 'register' })
  }
}

function resetCaptchaDialog() {
  dialogCaptchaCode.value = ''
}

async function handleSubmit() {
  if (!requireAuthAgreement(agreed.value)) return
  if (activeTab.value === 'phone') {
    await handleSmsRegister()
  } else {
    await handleAccountRegister()
  }
}

async function handleSmsRegister() {
  if (!smsRef.value) return
  await smsRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.smsRegister({
        phone: smsForm.value.phone,
        code: smsForm.value.code,
        nickname: smsForm.value.nickname || undefined,
        agreedToTerms: 'true',
      })
      trackAuthEvent('register_submit_sms')
      ElMessage.success('注册成功，欢迎加入新课堂教育')
      router.push(getRedirect())
    } catch (e: unknown) {
      const err = e as { message?: string }
      ElMessage.error(err.message || '注册失败')
    } finally {
      loading.value = false
    }
  })
}

async function handleAccountRegister() {
  if (!accountRef.value) return
  await accountRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await userStore.register({
        username: accountForm.value.username,
        password: accountForm.value.password,
        email: accountForm.value.email || undefined,
        agreedToTerms: 'true',
      })
      trackAuthEvent('register_submit_account')
      ElMessage.success('注册成功')
      router.push(getRedirect())
    } catch (e: unknown) {
      const err = e as { message?: string }
      ElMessage.error(err.message || '注册失败')
    } finally {
      loading.value = false
    }
  })
}
</script>
