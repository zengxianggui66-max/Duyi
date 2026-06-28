<template>
  <div class="profile-settings" v-loading="loading">
    <h3>⚙️ 个人设置</h3>
    <p class="section-desc">完善头像、昵称与基本信息；身份由管理员分配</p>

    <section class="settings-section">
      <h4>头像与昵称</h4>
      <div class="avatar-row">
        <div class="avatar-preview">
          <img v-if="form.avatar" :src="form.avatar" alt="头像" class="avatar-img" />
          <span v-else class="avatar-fallback">{{ avatarFallback }}</span>
        </div>
        <div class="avatar-actions">
          <el-upload
            :show-file-list="false"
            accept="image/jpeg,image/png,image/gif,image/webp"
            :before-upload="beforeAvatarUpload"
            :http-request="handleAvatarUpload"
          >
            <el-button type="primary" plain :loading="avatarUploading">更换头像</el-button>
          </el-upload>
          <p class="hint">支持 JPG/PNG/GIF/WebP，不超过 2MB</p>
        </div>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px" class="settings-form">
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" maxlength="50" show-word-limit placeholder="对外展示名称" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input :model-value="userStore.user?.username" disabled />
          <span class="field-hint">登录账号，暂不支持修改</span>
        </el-form-item>
      </el-form>
    </section>

    <section class="settings-section">
      <h4>基本信息</h4>
      <el-form :model="form" :rules="rules" label-width="88px" class="settings-form">
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :value="0">保密</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="生日" prop="birthday">
          <el-date-picker
            v-model="form.birthday"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            style="width: 100%"
            clearable
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" maxlength="100" placeholder="选填" clearable />
        </el-form-item>
        <el-form-item label="手机">
          <el-input :model-value="maskedPhone" disabled />
          <router-link class="link-security" :to="{ path: '/profile', query: { tab: 'security' } }">
            前往账号安全修改
          </router-link>
        </el-form-item>
      </el-form>
    </section>

    <section class="settings-section">
      <h4>身份信息</h4>
      <el-form label-width="88px" class="settings-form">
        <el-form-item label="当前身份">
          <span class="role-display">{{ roleDisplayName }}</span>
          <p class="field-hint">身份由管理员在后台分配，如需变更请联系管理员</p>
        </el-form-item>
      </el-form>
    </section>

    <div class="form-actions">
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      <el-button @click="loadFormFromStore">取消</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules, type UploadRequestOptions } from 'element-plus'
import { useUserStore } from '@/store'
import { authApi, fileApi } from '@/api'
import { unwrapData } from '@/api/request'
import type { UserInfo } from '@/api/auth'

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const avatarUploading = ref(false)
const formRef = ref<FormInstance>()

const form = ref({
  nickname: '',
  avatar: '',
  gender: 0 as 0 | 1 | 2,
  birthday: '' as string | undefined,
  email: '',
})

const roleDisplayName = computed(() => {
  const u = userStore.user
  if (u?.roleName) return u.roleName
  const r = u?.role
  if (r === 'admin') return '管理员'
  if (r === 'teacher') return '教师'
  if (r === 'student') return '学生'
  if (r === 'parent') return '家长'
  if (r === 'pending') return '待分配'
  return '待分配'
})
const rules: FormRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 1, max: 50, message: '昵称长度为1-50个字符', trigger: 'blur' },
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

const avatarFallback = computed(() => {
  const r = userStore.user?.role
  if (r === 'admin') return '🛡️'
  if (r === 'teacher') return '👨‍🏫'
  if (r === 'student') return '🎓'
  if (r === 'parent') return '👨‍👧'
  if (r === 'pending') return '⏳'
  return '👤'
})

const maskedPhone = computed(() => {
  const p = userStore.user?.phone
  if (!p) return '未绑定'
  if (p.length >= 11) return `${p.slice(0, 3)}****${p.slice(-4)}`
  return p
})

function fillForm(u: UserInfo) {
  form.value = {
    nickname: u.nickname || u.username || '',
    avatar: u.avatar || '',
    gender: (u.gender ?? 0) as 0 | 1 | 2,
    birthday: u.birthday || undefined,
    email: u.email || '',
  }
}

function loadFormFromStore() {
  if (userStore.user) fillForm(userStore.user)
}

async function loadProfile() {
  if (!userStore.isLoggedIn) return
  loading.value = true
  try {
    const res = await authApi.getCurrentUser()
    const data = unwrapData(res) as UserInfo
    userStore.setUser(data)
    fillForm(data)
  } catch {
  } finally {
    loading.value = false
  }
}

function beforeAvatarUpload(file: File) {
  const okType = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  if (!okType) {
    ElMessage.warning('仅支持 JPG、PNG、GIF、WebP 图片')
    return false
  }
  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('头像不能超过 2MB')
    return false
  }
  return true
}

async function handleAvatarUpload(options: UploadRequestOptions) {
  const file = options.file as File
  avatarUploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    const res = await fileApi.upload(fd)
    const data = unwrapData(res)
    form.value.avatar = data.fileUrl
    ElMessage.success('头像上传成功，请点击保存生效')
    options.onSuccess?.(data)
  } catch {
    ElMessage.error('头像上传失败')
  } finally {
    avatarUploading.value = false
  }
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const res = await authApi.updateProfile({
      nickname: form.value.nickname.trim(),
      avatar: form.value.avatar,
      gender: form.value.gender,
      birthday: form.value.birthday || '',
      email: form.value.email.trim(),
    })
    const data = unwrapData(res) as {
      userInfo: UserInfo
      token?: string
      accessToken?: string
    }
    if (data.token || data.accessToken) {
      userStore.setToken(data.token || data.accessToken!)
    }
    if (data.userInfo) {
      userStore.setUser(data.userInfo)
      fillForm(data.userInfo)
    }
    ElMessage.success('保存成功')
  } catch {
    /* 全局拦截已提示 */
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  if (userStore.user) fillForm(userStore.user)
  void loadProfile()
})
</script>

<style scoped>
.profile-settings h3 {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 4px;
}
.section-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 20px;
}
.settings-section {
  margin-bottom: 28px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border-light);
}
.settings-section:last-of-type {
  border-bottom: none;
}
.settings-section h4 {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 16px;
  color: var(--text-primary);
}
.avatar-row {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}
.avatar-preview {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  background: var(--color-primary-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-fallback {
  font-size: 40px;
  line-height: 1;
}
.avatar-actions .hint {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 8px 0 0;
}
.settings-form {
  max-width: 480px;
}
.field-hint {
  display: block;
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
}
.link-security {
  display: inline-block;
  font-size: 13px;
  color: var(--color-primary);
  margin-top: 4px;
}
.form-actions {
  display: flex;
  gap: 12px;
  padding-top: 8px;
}
.role-display {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}
</style>
