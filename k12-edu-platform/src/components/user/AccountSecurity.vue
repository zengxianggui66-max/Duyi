<template>
  <div class="account-security">
    <h3>🔐 账号安全</h3>
    <p class="section-desc">管理登录手机、密码与第三方账号，保障账号安全</p>

    <section class="security-section">
      <div class="section-head">
        <div>
          <h4>登录手机</h4>
          <p class="section-hint">用于验证码登录与找回账号；更换需验证原手机与新手机</p>
        </div>
        <el-button type="primary" plain size="small" @click="openPhoneDialog">
          {{ boundPhone ? '更换手机号' : '绑定手机号' }}
        </el-button>
      </div>
      <div class="info-row">
        <span class="info-label">当前手机</span>
        <span class="info-value">{{ maskedPhone }}</span>
      </div>
    </section>

    <section class="security-section">
      <div class="section-head">
        <div>
          <h4>登录密码</h4>
          <p class="section-hint">用户名密码登录时使用；第三方/短信注册用户可在此设置</p>
        </div>
        <el-button type="primary" plain size="small" @click="openPasswordDialog">
          {{ boundPhone ? '修改密码' : '设置密码' }}
        </el-button>
      </div>
      <div class="info-row">
        <span class="info-label">密码状态</span>
        <span class="info-value">已加密存储，无法查看明文</span>
      </div>
      <p v-if="!boundPhone" class="warn-hint">未绑定手机时，仅可通过「原密码」修改；建议先绑定手机以便短信设置密码</p>
    </section>

    <section class="security-section">
      <h4>第三方账号</h4>
      <p class="section-hint">绑定后可使用微信、QQ 或企业微信快速登录</p>
      <div v-loading="loading" class="bind-list">
        <div v-for="item in binds" :key="item.type" class="bind-item">
          <div class="bind-icon" :class="item.type">{{ typeIcon(item.type) }}</div>
          <div class="bind-info">
            <div class="bind-name">{{ item.typeName }}</div>
            <div class="bind-status">
              <template v-if="item.bound">
                <el-tag type="success" size="small">已绑定</el-tag>
                <span v-if="item.nickname" class="bind-nickname">{{ item.nickname }}</span>
              </template>
              <el-tag v-else type="info" size="small">未绑定</el-tag>
            </div>
            <div v-if="item.bound" class="bind-meta">
              <span v-if="item.bindTime">绑定时间：{{ formatDateTime(item.bindTime) }}</span>
              <span v-if="item.lastLoginAt">最近登录：{{ formatDateTime(item.lastLoginAt) }}</span>
            </div>
          </div>
          <div class="bind-actions">
            <el-tooltip
              v-if="item.bound && item.canUnbind === false && item.unbindReason"
              :content="item.unbindReason"
              placement="top"
            >
              <el-button size="small" type="danger" plain disabled>解绑</el-button>
            </el-tooltip>
            <el-button
              v-else-if="item.bound"
              size="small"
              type="danger"
              plain
              :loading="isUnbindingType(item.type)"
              :disabled="item.canUnbind === false || isBindingType(item.type)"
              @click="handleUnbind(item)"
            >
              解绑
            </el-button>
            <el-button
              v-else
              size="small"
              type="primary"
              :loading="isBindingType(item.type)"
              :disabled="isUnbindingType(item.type)"
              @click="handleBind(item.type)"
            >
              绑定
            </el-button>
          </div>
        </div>
      </div>
    </section>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="解绑提示"
      description="若仅使用第三方登录，解绑前请先绑定手机号或设置密码，否则可能无法登录。"
    />

    <section class="security-section">
      <div class="section-head">
        <div>
          <h4>最近操作记录</h4>
          <p class="section-hint">展示近期第三方绑定/解绑结果，便于账号安全核对</p>
        </div>
      </div>
      <div v-loading="logsLoading" class="logs-panel">
        <template v-if="bindLogs.length">
          <div v-for="(log, idx) in bindLogs" :key="log.id || `${log.action}-${idx}`" class="log-row">
            <div class="log-main">
              <span class="log-action">{{ formatLogAction(log.action) }}</span>
              <span class="log-provider">{{ formatProvider(log.provider, log.providerName) }}</span>
              <el-tag :type="log.result === 'success' ? 'success' : 'danger'" size="small">
                {{ log.result === 'success' ? '成功' : '失败' }}
              </el-tag>
            </div>
            <div class="log-sub">
              <span v-if="log.reasonMessage">{{ log.reasonMessage }}</span>
              <span v-else-if="log.reasonCode">原因码：{{ log.reasonCode }}</span>
              <span v-if="log.ip">IP：{{ log.ip }}</span>
              <span v-if="log.createdAt">{{ formatDateTime(log.createdAt) }}</span>
            </div>
          </div>
        </template>
        <el-empty v-else description="暂无操作记录" :image-size="62" />
      </div>
    </section>

    <el-dialog v-model="bindPhoneDialogVisible" title="绑定手机号" width="440px" destroy-on-close @closed="resetBindPhoneForm">
      <el-form ref="bindPhoneFormRef" :model="bindPhoneForm" :rules="bindPhoneRules" label-position="top">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="bindPhoneForm.phone" placeholder="请输入11位手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="图形验证" prop="captchaCode">
          <div class="inline-row">
            <el-input v-model="bindPhoneForm.captchaCode" placeholder="计算结果" />
            <el-button @click="loadBindCaptcha">{{ bindCaptchaQuestion || '获取验证' }}</el-button>
          </div>
        </el-form-item>
        <el-form-item label="短信验证码" prop="code">
          <div class="inline-row">
            <el-input v-model="bindPhoneForm.code" placeholder="6位验证码" maxlength="6" />
            <el-button type="primary" :disabled="bindCountdown > 0" :loading="bindSending" @click="sendBindCode">
              {{ bindCountdown > 0 ? `${bindCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindPhoneDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="bindSubmitting" @click="submitBindPhone">确认绑定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="changePhoneDialogVisible" title="更换手机号" width="480px" destroy-on-close @closed="resetChangePhoneForm">
      <el-steps :active="changePhoneStep" finish-status="success" align-center class="change-steps">
        <el-step title="验证原手机" />
        <el-step title="绑定新手机" />
      </el-steps>
      <div v-show="changePhoneStep === 0" class="step-body">
        <p class="step-hint">向当前手机 {{ maskedPhone }} 发送验证码</p>
        <el-form ref="oldPhoneFormRef" :model="oldPhoneForm" :rules="oldPhoneRules" label-position="top">
          <el-form-item label="图形验证" prop="captchaCode">
            <div class="inline-row">
              <el-input v-model="oldPhoneForm.captchaCode" placeholder="计算结果" />
              <el-button @click="loadOldCaptcha">{{ oldCaptchaQuestion || '获取验证' }}</el-button>
            </div>
          </el-form-item>
          <el-form-item label="原手机验证码" prop="oldCode">
            <div class="inline-row">
              <el-input v-model="oldPhoneForm.oldCode" placeholder="6位验证码" maxlength="6" />
              <el-button type="primary" :disabled="oldCountdown > 0" :loading="oldSending" @click="sendOldPhoneCode">
                {{ oldCountdown > 0 ? `${oldCountdown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
      <div v-show="changePhoneStep === 1" class="step-body">
        <el-form ref="newPhoneFormRef" :model="newPhoneForm" :rules="newPhoneRules" label-position="top">
          <el-form-item label="新手机号" prop="newPhone">
            <el-input v-model="newPhoneForm.newPhone" placeholder="请输入新手机号" maxlength="11" />
          </el-form-item>
          <el-form-item label="图形验证" prop="captchaCode">
            <div class="inline-row">
              <el-input v-model="newPhoneForm.captchaCode" placeholder="计算结果" />
              <el-button @click="loadNewCaptcha">{{ newCaptchaQuestion || '获取验证' }}</el-button>
            </div>
          </el-form-item>
          <el-form-item label="新手机验证码" prop="newCode">
            <div class="inline-row">
              <el-input v-model="newPhoneForm.newCode" placeholder="6位验证码" maxlength="6" />
              <el-button type="primary" :disabled="newCountdown > 0" :loading="newSending" @click="sendNewPhoneCode">
                {{ newCountdown > 0 ? `${newCountdown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="changePhoneDialogVisible = false">取消</el-button>
        <el-button v-if="changePhoneStep > 0" @click="changePhoneStep = 0">上一步</el-button>
        <el-button v-if="changePhoneStep === 0" type="primary" @click="goChangePhoneStep2">下一步</el-button>
        <el-button v-else type="primary" :loading="changeSubmitting" @click="submitChangePhone">确认更换</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="passwordDialogVisible" title="修改登录密码" width="460px" destroy-on-close @closed="resetPasswordForm">
      <el-tabs v-model="passwordTab">
        <el-tab-pane label="原密码修改" name="old">
          <el-form ref="pwdOldFormRef" :model="pwdOldForm" :rules="pwdOldRules" label-position="top">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="pwdOldForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdOldForm.newPassword" type="password" show-password placeholder="6-32位" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="pwdOldForm.confirmPassword" type="password" show-password placeholder="再次输入" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="短信验证设置" name="sms" :disabled="!boundPhone">
          <p v-if="!boundPhone" class="tab-disabled-hint">请先绑定手机号</p>
          <el-form v-else ref="pwdSmsFormRef" :model="pwdSmsForm" :rules="pwdSmsRules" label-position="top">
            <el-form-item label="已绑定手机">
              <el-input :model-value="maskedPhone" disabled />
            </el-form-item>
            <el-form-item label="图形验证" prop="captchaCode">
              <div class="inline-row">
                <el-input v-model="pwdSmsForm.captchaCode" placeholder="计算结果" />
                <el-button @click="loadPwdCaptcha">{{ pwdCaptchaQuestion || '获取验证' }}</el-button>
              </div>
            </el-form-item>
            <el-form-item label="短信验证码" prop="code">
              <div class="inline-row">
                <el-input v-model="pwdSmsForm.code" placeholder="6位验证码" maxlength="6" />
                <el-button type="primary" :disabled="pwdCountdown > 0" :loading="pwdSending" @click="sendPwdCode">
                  {{ pwdCountdown > 0 ? `${pwdCountdown}s` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="pwdSmsForm.newPassword" type="password" show-password placeholder="6-32位" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="pwdSmsForm.confirmPassword" type="password" show-password placeholder="再次输入" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordSubmitting" @click="submitPassword">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { authApi } from '@/api/auth'
import { useUserStore } from '@/store'
import type { OAuthBindItem, OAuthBindLogItem } from '@/api/auth'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(false)
const binds = ref<OAuthBindItem[]>([])
const logsLoading = ref(false)
const bindLogs = ref<OAuthBindLogItem[]>([])
const bindingTypeLoading = ref<string>('')
const unbindingTypeLoading = ref<string>('')

const boundPhone = computed(() => userStore.user?.phone?.trim() || '')
const maskedPhone = computed(() => {
  const p = boundPhone.value
  if (!p) return '未绑定'
  if (p.length >= 11) return `${p.slice(0, 3)}****${p.slice(-4)}`
  return p
})

function typeIcon(type: string) {
  return { wechat: '微', qq: 'Q', wework: '企' }[type] || '?'
}

function isBindingType(type: string) {
  return bindingTypeLoading.value === type
}

function isUnbindingType(type: string) {
  return unbindingTypeLoading.value === type
}

function formatDateTime(v?: string) {
  if (!v) return '-'
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return v
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

function startCountdown(counter: { value: number }, timerRef: { current: ReturnType<typeof setInterval> | null }) {
  counter.value = 60
  if (timerRef.current) clearInterval(timerRef.current)
  timerRef.current = setInterval(() => {
    counter.value--
    if (counter.value <= 0 && timerRef.current) {
      clearInterval(timerRef.current)
      timerRef.current = null
    }
  }, 1000)
}

async function loadBinds() {
  loading.value = true
  try {
    const res = await authApi.listOAuthBinds()
    binds.value = res.data.data || []
  } catch {
    // request 拦截器已展示错误提示，此处仅降级为默认列表
    binds.value = []
  } finally {
    loading.value = false
  }
}

async function loadBindLogs() {
  logsLoading.value = true
  try {
    const res = await authApi.listOAuthBindLogs({ size: 5 })
    bindLogs.value = res.data.data || []
  } catch {
    // 后端日志接口未接入时优雅降级
    bindLogs.value = []
  } finally {
    logsLoading.value = false
  }
}

async function handleBind(type: string) {
  if (bindingTypeLoading.value || unbindingTypeLoading.value) return
  bindingTypeLoading.value = type
  try {
    const redirectUri = `${window.location.origin}/login/oauth/callback`
    const redirect = route.fullPath
    const res = await authApi.getOAuthBindUrl(type, redirectUri)
    const { authorizationUrl, state } = res.data.data
    sessionStorage.setItem('oauth_state', state)
    sessionStorage.setItem('oauth_type', type)
    sessionStorage.setItem('oauth_mode', 'bind')
    sessionStorage.setItem('oauth_redirect', redirect)
    window.location.href = authorizationUrl
  } catch (e: unknown) {
    ElMessage.error(resolveOAuthErrorMessage(e))
  } finally {
    bindingTypeLoading.value = ''
  }
}

async function handleUnbind(item: OAuthBindItem) {
  if (bindingTypeLoading.value || unbindingTypeLoading.value) return
  unbindingTypeLoading.value = item.type
  try {
    await ElMessageBox.confirm(
      `确定解绑${item.typeName}吗？解绑后将无法使用该方式登录。`,
      '解绑确认',
      { type: 'warning', confirmButtonText: '解绑', cancelButtonText: '取消' },
    )
    await authApi.unbindOAuth(item.type)
    ElMessage.success('解绑成功')
    await loadBinds()
    await loadBindLogs()
  } catch (e: unknown) {
    if (e === 'cancel') return
    ElMessage.error(resolveOAuthErrorMessage(e, '解绑失败，请稍后重试'))
  } finally {
    unbindingTypeLoading.value = ''
  }
}

function resolveOAuthErrorMessage(error: unknown, defaultMsg = '绑定失败，请稍后重试') {
  const err = error as {
    message?: string
    response?: {
      data?: {
        message?: string
        msg?: string
        code?: number | string
      }
    }
  }
  const backendMsg = err?.response?.data?.message || err?.response?.data?.msg || err?.message || ''
  const code = String(err?.response?.data?.code ?? '')
  const text = `${backendMsg} ${code}`.toLowerCase()

  if (text.includes('state') && (text.includes('invalid') || text.includes('expire') || text.includes('失效'))) {
    return '授权状态已过期，请重新发起绑定'
  }
  if (text.includes('已被其他用户绑定') || text.includes('conflict') || text.includes('duplicate')) {
    return '该第三方账号已绑定其他账户，请更换账号后重试'
  }
  if (text.includes('access_denied') || text.includes('授权取消') || text.includes('cancel')) {
    return '已取消授权，未完成绑定'
  }
  return backendMsg || defaultMsg
}

function formatLogAction(action?: string) {
  return {
    bind: '绑定',
    unbind: '解绑',
    login: '登录',
  }[action || ''] || '操作'
}

function formatProvider(provider?: string, providerName?: string) {
  if (providerName) return providerName
  return {
    wechat: '微信',
    qq: 'QQ',
    wework: '企业微信',
  }[provider || ''] || '第三方账号'
}

// ---------- 绑定手机 ----------
const bindPhoneDialogVisible = ref(false)
const bindPhoneFormRef = ref<FormInstance>()
const bindPhoneForm = ref({ phone: '', code: '', captchaCode: '' })
const bindCaptchaKey = ref('')
const bindCaptchaQuestion = ref('')
const bindSending = ref(false)
const bindSubmitting = ref(false)
const bindCountdown = ref(0)
const bindTimer = { current: null as ReturnType<typeof setInterval> | null }

const bindPhoneRules: FormRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  captchaCode: [{ required: true, message: '请完成图形验证', trigger: 'blur' }],
  code: [{ required: true, message: '请输入短信验证码', trigger: 'blur' }],
}

function openPhoneDialog() {
  if (boundPhone.value) {
    changePhoneDialogVisible.value = true
    changePhoneStep.value = 0
    void loadOldCaptcha()
  } else {
    bindPhoneDialogVisible.value = true
    void loadBindCaptcha()
  }
}

function resetBindPhoneForm() {
  bindPhoneForm.value = { phone: '', code: '', captchaCode: '' }
  bindPhoneFormRef.value?.resetFields()
}

async function loadBindCaptcha() {
  const res = await authApi.getCaptcha(true)
  bindCaptchaKey.value = res.data.data.captchaKey
  bindCaptchaQuestion.value = res.data.data.question
}

async function sendBindCode() {
  if (!bindPhoneFormRef.value) return
  const ok = await bindPhoneFormRef.value.validateField(['phone', 'captchaCode']).catch(() => false)
  if (!ok) return
  bindSending.value = true
  try {
    await userStore.sendSmsCode(bindPhoneForm.value.phone, bindCaptchaKey.value, bindPhoneForm.value.captchaCode, 'bind')
    ElMessage.success('验证码已发送')
    startCountdown(bindCountdown, bindTimer)
    await loadBindCaptcha()
  } catch {
    await loadBindCaptcha()
  } finally {
    bindSending.value = false
  }
}

async function submitBindPhone() {
  if (!bindPhoneFormRef.value) return
  const valid = await bindPhoneFormRef.value.validate().catch(() => false)
  if (!valid) return
  bindSubmitting.value = true
  try {
    await userStore.bindPhone(bindPhoneForm.value.phone, bindPhoneForm.value.code)
    ElMessage.success('绑定成功')
    bindPhoneDialogVisible.value = false
    await userStore.fetchCurrentUser()
  } finally {
    bindSubmitting.value = false
  }
}

// ---------- 更换手机（双验证） ----------
const changePhoneDialogVisible = ref(false)
const changePhoneStep = ref(0)
const oldPhoneFormRef = ref<FormInstance>()
const newPhoneFormRef = ref<FormInstance>()
const oldPhoneForm = ref({ captchaCode: '', oldCode: '' })
const newPhoneForm = ref({ newPhone: '', captchaCode: '', newCode: '' })
const oldCaptchaKey = ref('')
const oldCaptchaQuestion = ref('')
const newCaptchaKey = ref('')
const newCaptchaQuestion = ref('')
const oldSending = ref(false)
const newSending = ref(false)
const changeSubmitting = ref(false)
const oldCountdown = ref(0)
const newCountdown = ref(0)
const oldTimer = { current: null as ReturnType<typeof setInterval> | null }
const newTimer = { current: null as ReturnType<typeof setInterval> | null }

const oldPhoneRules: FormRules = {
  captchaCode: [{ required: true, message: '请完成图形验证', trigger: 'blur' }],
  oldCode: [{ required: true, message: '请输入原手机验证码', trigger: 'blur' }],
}

const newPhoneRules: FormRules = {
  newPhone: [
    { required: true, message: '请输入新手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' },
  ],
  captchaCode: [{ required: true, message: '请完成图形验证', trigger: 'blur' }],
  newCode: [{ required: true, message: '请输入新手机验证码', trigger: 'blur' }],
}

function resetChangePhoneForm() {
  changePhoneStep.value = 0
  oldPhoneForm.value = { captchaCode: '', oldCode: '' }
  newPhoneForm.value = { newPhone: '', captchaCode: '', newCode: '' }
  oldPhoneFormRef.value?.resetFields()
  newPhoneFormRef.value?.resetFields()
}

async function loadOldCaptcha() {
  const res = await authApi.getCaptcha(true)
  oldCaptchaKey.value = res.data.data.captchaKey
  oldCaptchaQuestion.value = res.data.data.question
}

async function loadNewCaptcha() {
  const res = await authApi.getCaptcha(true)
  newCaptchaKey.value = res.data.data.captchaKey
  newCaptchaQuestion.value = res.data.data.question
}

async function sendOldPhoneCode() {
  if (!boundPhone.value || !oldPhoneFormRef.value) return
  const ok = await oldPhoneFormRef.value.validateField(['captchaCode']).catch(() => false)
  if (!ok) return
  oldSending.value = true
  try {
    await userStore.sendSmsCode(boundPhone.value, oldCaptchaKey.value, oldPhoneForm.value.captchaCode, 'change_phone_old')
    ElMessage.success('验证码已发送至当前手机')
    startCountdown(oldCountdown, oldTimer)
    await loadOldCaptcha()
  } catch {
    await loadOldCaptcha()
  } finally {
    oldSending.value = false
  }
}

async function sendNewPhoneCode() {
  if (!newPhoneFormRef.value) return
  const ok = await newPhoneFormRef.value.validateField(['newPhone', 'captchaCode']).catch(() => false)
  if (!ok) return
  newSending.value = true
  try {
    await userStore.sendSmsCode(
      newPhoneForm.value.newPhone,
      newCaptchaKey.value,
      newPhoneForm.value.captchaCode,
      'change_phone_new',
    )
    ElMessage.success('验证码已发送至新手机')
    startCountdown(newCountdown, newTimer)
    await loadNewCaptcha()
  } catch {
    await loadNewCaptcha()
  } finally {
    newSending.value = false
  }
}

async function goChangePhoneStep2() {
  if (!oldPhoneFormRef.value) return
  const valid = await oldPhoneFormRef.value.validate().catch(() => false)
  if (!valid) return
  if (!/^\d{6}$/.test(oldPhoneForm.value.oldCode)) {
    ElMessage.warning('请先获取并填写6位原手机验证码')
    return
  }
  changePhoneStep.value = 1
  void loadNewCaptcha()
}

async function submitChangePhone() {
  if (!newPhoneFormRef.value) return
  const valid = await newPhoneFormRef.value.validate().catch(() => false)
  if (!valid) return
  changeSubmitting.value = true
  try {
    await authApi.changePhone({
      newPhone: newPhoneForm.value.newPhone,
      oldCode: oldPhoneForm.value.oldCode,
      newCode: newPhoneForm.value.newCode,
    })
    ElMessage.success('手机号已更换')
    changePhoneDialogVisible.value = false
    await userStore.fetchCurrentUser()
  } finally {
    changeSubmitting.value = false
  }
}

// ---------- 密码 ----------
const passwordDialogVisible = ref(false)
const passwordTab = ref<'old' | 'sms'>('sms')
const passwordSubmitting = ref(false)

const pwdOldFormRef = ref<FormInstance>()
const pwdOldForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdSmsFormRef = ref<FormInstance>()
const pwdSmsForm = ref({ captchaCode: '', code: '', newPassword: '', confirmPassword: '' })
const pwdCaptchaKey = ref('')
const pwdCaptchaQuestion = ref('')
const pwdSending = ref(false)
const pwdCountdown = ref(0)
const pwdTimer = { current: null as ReturnType<typeof setInterval> | null }

const confirmPwdValidator = (
  _rule: unknown,
  value: string,
  callback: (e?: Error) => void,
  source: { newPassword?: string },
) => {
  if (value !== source.newPassword) callback(new Error('两次密码不一致'))
  else callback()
}

const pwdOldRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为6-32位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: (_r, v, cb) => confirmPwdValidator(_r, v, cb, pwdOldForm.value), trigger: 'blur' },
  ],
}

const pwdSmsRules: FormRules = {
  captchaCode: [{ required: true, message: '请完成图形验证', trigger: 'blur' }],
  code: [{ required: true, message: '请输入短信验证码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为6-32位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: (_r, v, cb) => confirmPwdValidator(_r, v, cb, pwdSmsForm.value), trigger: 'blur' },
  ],
}

function openPasswordDialog() {
  passwordTab.value = boundPhone.value ? 'sms' : 'old'
  passwordDialogVisible.value = true
  if (boundPhone.value) void loadPwdCaptcha()
}

function resetPasswordForm() {
  pwdOldForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  pwdSmsForm.value = { captchaCode: '', code: '', newPassword: '', confirmPassword: '' }
  pwdOldFormRef.value?.resetFields()
  pwdSmsFormRef.value?.resetFields()
}

async function loadPwdCaptcha() {
  const res = await authApi.getCaptcha(true)
  pwdCaptchaKey.value = res.data.data.captchaKey
  pwdCaptchaQuestion.value = res.data.data.question
}

async function sendPwdCode() {
  if (!boundPhone.value || !pwdSmsFormRef.value) return
  const ok = await pwdSmsFormRef.value.validateField(['captchaCode']).catch(() => false)
  if (!ok) return
  pwdSending.value = true
  try {
    await userStore.sendSmsCode(boundPhone.value, pwdCaptchaKey.value, pwdSmsForm.value.captchaCode, 'set_password')
    ElMessage.success('验证码已发送')
    startCountdown(pwdCountdown, pwdTimer)
    await loadPwdCaptcha()
  } catch {
    await loadPwdCaptcha()
  } finally {
    pwdSending.value = false
  }
}

async function submitPassword() {
  passwordSubmitting.value = true
  try {
    if (passwordTab.value === 'old') {
      if (!pwdOldFormRef.value) return
      const valid = await pwdOldFormRef.value.validate().catch(() => false)
      if (!valid) return
      await authApi.changePassword({
        oldPassword: pwdOldForm.value.oldPassword,
        newPassword: pwdOldForm.value.newPassword,
      })
    } else {
      if (!pwdSmsFormRef.value || !boundPhone.value) return
      const valid = await pwdSmsFormRef.value.validate().catch(() => false)
      if (!valid) return
      await authApi.changePassword({
        phone: boundPhone.value,
        code: pwdSmsForm.value.code,
        newPassword: pwdSmsForm.value.newPassword,
      })
    }
    ElMessage.success('密码已更新')
    passwordDialogVisible.value = false
  } finally {
    passwordSubmitting.value = false
  }
}

onMounted(() => {
  void loadBinds()
  void loadBindLogs()
  void userStore.fetchCurrentUser()
})

onUnmounted(() => {
  if (bindTimer.current) clearInterval(bindTimer.current)
  if (oldTimer.current) clearInterval(oldTimer.current)
  if (newTimer.current) clearInterval(newTimer.current)
  if (pwdTimer.current) clearInterval(pwdTimer.current)
})
</script>

<style scoped>
.account-security h3 {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 4px;
}
.section-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 24px;
}
.security-section {
  margin-bottom: 28px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border-light);
}
.security-section:last-of-type {
  border-bottom: none;
}
.section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}
.security-section h4 {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 4px;
}
.section-hint {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0;
}
.info-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--bg-body);
  border-radius: var(--radius-sm);
  font-size: 14px;
}
.info-label {
  color: var(--text-secondary);
  min-width: 72px;
}
.info-value {
  font-weight: 500;
}
.warn-hint {
  font-size: 12px;
  color: var(--el-color-warning);
  margin: 8px 0 0;
}
.change-steps {
  margin-bottom: 20px;
}
.step-body {
  min-height: 120px;
}
.step-hint {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 16px;
}
.bind-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
}
.bind-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg-body);
  border-radius: var(--radius-sm);
}
.bind-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
  flex-shrink: 0;
}
.bind-icon.wechat {
  background: #e8f5e9;
  color: #09b83e;
}
.bind-icon.qq {
  background: #e3f2fd;
  color: #12b7f5;
}
.bind-icon.wework {
  background: #e8eaf6;
  color: #1d6fe8;
}
.bind-info {
  flex: 1;
  min-width: 0;
}
.bind-name {
  font-weight: 600;
  margin-bottom: 6px;
}
.bind-status {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.bind-meta {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-secondary);
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.bind-nickname {
  font-size: 12px;
  color: var(--text-secondary);
}
.bind-actions {
  flex-shrink: 0;
}
.inline-row {
  display: flex;
  gap: 8px;
  width: 100%;
}
.tab-disabled-hint {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

.logs-panel {
  margin-top: 10px;
  background: var(--bg-body);
  border-radius: var(--radius-sm);
  padding: 10px 12px;
}

.log-row {
  padding: 10px 0;
  border-bottom: 1px dashed var(--border-light);
}

.log-row:last-child {
  border-bottom: none;
}

.log-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.log-action {
  font-weight: 600;
  color: var(--text-primary);
}

.log-provider {
  color: var(--text-secondary);
}

.log-sub {
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-secondary);
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>
