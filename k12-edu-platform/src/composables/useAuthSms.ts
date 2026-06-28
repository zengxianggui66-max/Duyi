/**
 * 短信验证码 / 图形验证 共用逻辑（登录、注册页）
 */
import { ref, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/store'
import type { FormInstance } from 'element-plus'

export type AuthRole = 'teacher' | 'student' | 'parent'

export const ROLE_OPTIONS: { value: AuthRole; label: string; icon: string; desc: string }[] = [
  { value: 'teacher', label: '教师', icon: '👩‍🏫', desc: '备课授课、上传资源' },
  { value: 'student', label: '学生', icon: '🎓', desc: '学习资料、在线练习' },
  { value: 'parent', label: '家长', icon: '👨‍👩‍👧', desc: '关注学情、家校沟通' },
]

export type SmsCodeType = 'login' | 'register' | 'bind' | (string & {})

export function useAuthSms(smsType: SmsCodeType = 'login') {
  const userStore = useUserStore()
  const sendingCode = ref(false)
  const countdown = ref(0)
  const captchaKey = ref('')
  const captchaQuestion = ref('')
  const captchaType = ref('arithmetic')
  let countdownTimer: ReturnType<typeof setInterval> | null = null

  const smsForm = ref({
    phone: '',
    code: '',
    captchaCode: '',
    role: 'teacher' as AuthRole,
    nickname: '',
    agreed: false,
  })

  const smsRules = {
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
    ],
    captchaCode: [{ required: true, message: '请完成图形验证', trigger: 'blur' }],
    code: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
    role: [{ required: true, message: '请选择身份', trigger: 'change' }],
    nickname: [{ max: 20, message: '昵称不超过20字', trigger: 'blur' }],
  }

  onUnmounted(() => {
    if (countdownTimer) clearInterval(countdownTimer)
  })

  async function loadCaptcha(silent = false) {
    try {
      const res = await authApi.getCaptcha(silent)
      const data = res.data.data
      captchaKey.value = data.captchaKey
      captchaQuestion.value = data.question
      captchaType.value = data.captchaType || 'arithmetic'
    } catch {
      if (!silent) {
        ElMessage.warning('无法加载图形验证，请稍后点击「获取题目」重试')
      }
    }
  }

  function startCountdown(seconds = 60) {
    if (countdownTimer) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
    countdown.value = seconds
    countdownTimer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0 && countdownTimer) {
        clearInterval(countdownTimer)
        countdownTimer = null
      }
    }, 1000)
  }

  async function dispatchSendCode(captchaCode: string) {
    sendingCode.value = true
    try {
      await userStore.sendSmsCode(
        smsForm.value.phone,
        captchaKey.value,
        captchaCode,
        smsType,
      )
      ElMessage.success('验证码已发送')
      startCountdown()
      await loadCaptcha()
      return true
    } catch (e: unknown) {
      const err = e as { message?: string }
      ElMessage.error(err.message || '发送失败')
      await loadCaptcha()
      return false
    } finally {
      sendingCode.value = false
    }
  }

  async function handleSendCode(formRef: FormInstance | undefined) {
    if (!formRef) return
    await formRef.validateField(['phone', 'captchaCode'], async (valid) => {
      if (!valid) return
      await dispatchSendCode(smsForm.value.captchaCode)
    })
  }

  /** 弹窗图形验证通过后发码（仅校验手机号） */
  async function sendCodeWithCaptcha(
    formRef: FormInstance | undefined,
    captchaCode: string,
  ): Promise<boolean> {
    if (!formRef) return false
    if (!captchaCode.trim()) {
      ElMessage.warning('请输入图形验证结果')
      return false
    }
    let ok = false
    await formRef.validateField(['phone'], async (valid) => {
      if (!valid) return
      ok = await dispatchSendCode(captchaCode.trim())
    })
    return ok
  }

  /** 登录页：图形验证在弹窗内完成，主表单不含 captchaCode / role 校验 */
  const loginSmsRules = {
    phone: smsRules.phone,
    code: smsRules.code,
  }

  return {
    smsForm,
    smsRules,
    loginSmsRules,
    sendingCode,
    countdown,
    captchaKey,
    captchaQuestion,
    captchaType,
    loadCaptcha,
    handleSendCode,
    sendCodeWithCaptcha,
  }
}
