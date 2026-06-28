<template>
  <div class="oauth-select-role">
    <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    <p>正在完成账号注册…</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { useUserStore } from '@/store'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

interface PendingOAuth {
  type: string
  oauthId: string
  nickname?: string
  avatar?: string
}

function getRedirect(): string {
  const r = route.query.redirect
  return typeof r === 'string' && r.startsWith('/') ? r : '/'
}

onMounted(async () => {
  const raw = sessionStorage.getItem('oauth_pending')
  if (!raw) {
    router.replace('/login')
    return
  }

  let pending: PendingOAuth
  try {
    pending = JSON.parse(raw) as PendingOAuth
  } catch {
    router.replace('/login')
    return
  }

  try {
    const data = await userStore.completeOAuthRegister({
      type: pending.type,
      oauthId: pending.oauthId,
      nickname: pending.nickname,
      avatar: pending.avatar,
    })
    sessionStorage.removeItem('oauth_pending')
    ElMessage.success('注册成功')
    if (data?.needBindPhone || data?.userInfo?.needBindPhone) {
      router.replace({ path: '/bind-phone', query: { redirect: getRedirect() } })
    } else {
      router.replace(getRedirect())
    }
  } catch (e: unknown) {
    const err = e as { message?: string }
    ElMessage.error(err.message || '注册失败')
    router.replace('/login')
  }
})
</script>

<style scoped>
.oauth-select-role {
  min-height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--text-secondary);
}
</style>
