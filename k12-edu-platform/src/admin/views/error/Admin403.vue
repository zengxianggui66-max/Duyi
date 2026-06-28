<template>
  <div class="admin-403">
    <el-result icon="warning" title="403" sub-title="您没有访问管理端的权限">
      <template #extra>
        <el-button type="primary" @click="goLogin">重新登录</el-button>
        <el-button @click="router.replace('/')">返回前台</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { buildLoginRoute } from '@/composables/useAuthLoginFlow'

const router = useRouter()

function goLogin() {
  router.replace(buildLoginRoute({ intent: 'admin' }))
}
</script>

<style scoped>
.admin-403 {
  padding: 48px 24px;
}
</style>
