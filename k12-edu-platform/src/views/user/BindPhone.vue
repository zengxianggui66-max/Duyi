<template>
  <div class="auth-form bind-form">
    <h2 class="auth-form__title">绑定手机号</h2>
    <p class="auth-form__subtitle">绑定后可使用短信验证码登录，并用于账号安全验证</p>

    <el-form :model="smsForm" :rules="bindRules" ref="formRef" label-position="top">
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="smsForm.phone" placeholder="请输入手机号" maxlength="11" size="large" />
      </el-form-item>
      <el-form-item label="短信验证码" prop="code">
        <div class="auth-sms-row">
          <el-input v-model="smsForm.code" placeholder="验证码" maxlength="6" size="large" />
          <el-button
            plain
            size="large"
            class="auth-send-code-btn"
            :disabled="countdown > 0"
            :loading="sendingCode"
            @click="openCaptchaDialog"
          >
            {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
          </el-button>
        </div>
      </el-form-item>
      <el-button type="primary" size="large" round class="auth-submit-btn" :loading="loading" @click="submit">
        确认绑定
      </el-button>
    </el-form>

    <CaptchaDialog
      v-model="captchaDialogVisible"
      v-model:code="dialogCaptchaCode"
      :question="captchaQuestion"
      :captcha-type="captchaType"
      :loading="sendingCode"
      hint="请完成图形验证后获取绑定验证码"
      confirm-label="确认并发送"
      @confirm="confirmCaptchaAndSend"
      @refresh="() => loadCaptcha()"
      @closed="resetCaptchaDialog"
      @opened="() => trackAuthEvent('captcha_dialog_open', { page: 'bind-phone' })"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { useUserStore } from '@/store'
import { useAuthSms } from '@/composables/useAuthSms'
import {
  navigateAfterLogin,
  resolveLoginIntent,
  resolveRedirectPath,
  buildLoginRoute,
} from '@/composables/useAuthLoginFlow'
import { trackAuthEvent } from '@/utils/authAnalytics'
import CaptchaDialog from '@/components/user/CaptchaDialog.vue'
import '@/assets/styles/auth-page.css'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const captchaDialogVisible = ref(false)
const dialogCaptchaCode = ref('')
const formRef = ref<FormInstance>()

const {
  smsForm,
  loginSmsRules,
  sendingCode,
  countdown,
  captchaQuestion,
  captchaType,
  loadCaptcha,
  sendCodeWithCaptcha,
} = useAuthSms('bind')

const bindRules = {
  phone: loginSmsRules.phone,
  code: loginSmsRules.code,
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.replace(buildLoginRoute({
      redirect: resolveRedirectPath(route.query.redirect),
      intent: resolveLoginIntent(route.query as Record<string, unknown>),
    }))
  }
})

async function openCaptchaDialog() {
  if (!formRef.value) return
  await formRef.value.validateField(['phone'], (valid) => {
    if (!valid) return
    dialogCaptchaCode.value = ''
    captchaDialogVisible.value = true
    loadCaptcha()
  })
}

async function confirmCaptchaAndSend() {
  trackAuthEvent('captcha_dialog_confirm', { page: 'bind-phone' })
  const ok = await sendCodeWithCaptcha(formRef.value, dialogCaptchaCode.value)
  if (ok) {
    trackAuthEvent('sms_code_send_success', { page: 'bind-phone' })
    captchaDialogVisible.value = false
  } else {
    trackAuthEvent('sms_code_send_fail', { page: 'bind-phone' })
  }
}

function resetCaptchaDialog() {
  dialogCaptchaCode.value = ''
}

async function submit() {
  if (!formRef.value) return
  await formRef.value.validate(async (ok) => {
    if (!ok) return
    loading.value = true
    try {
      await userStore.bindPhone(smsForm.value.phone, smsForm.value.code)
      ElMessage.success('绑定成功')
      await navigateAfterLogin(router, {
        redirect: resolveRedirectPath(route.query.redirect),
        intent: resolveLoginIntent(route.query as Record<string, unknown>),
      })
    } catch (e: unknown) {
      const err = e as { message?: string }
      ElMessage.error(err.message || '绑定失败')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.auth-form__title {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
}

.auth-form__subtitle {
  margin: 0 0 24px;
  font-size: 13px;
  color: var(--text-secondary);
}
</style>
