<template>
  <div class="login-log">
    <div class="login-log__toolbar">
      <el-input v-model="username" placeholder="用户名" clearable style="width: 140px" />
      <el-select v-model="loginType" placeholder="登录方式" clearable style="width: 120px">
        <el-option label="后台" value="admin" />
        <el-option label="密码" value="password" />
        <el-option label="短信" value="sms" />
        <el-option label="OAuth" value="oauth" />
      </el-select>
      <el-select v-model="success" placeholder="结果" clearable style="width: 100px">
        <el-option label="成功" :value="1" />
        <el-option label="失败" :value="0" />
      </el-select>
      <el-checkbox v-model="staffOnly">仅 staff</el-checkbox>
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <el-table v-loading="loading" :data="records" border stripe size="small">
      <el-table-column prop="createTime" label="时间" width="170" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="loginType" label="方式" width="80" />
      <el-table-column label="结果" width="70">
        <template #default="{ row }">
          <el-tag :type="row.success === 1 ? 'success' : 'danger'" size="small">
            {{ row.success === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="failReason" label="失败原因" min-width="140" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP" width="120" />
    </el-table>
    <div class="login-log__pager">
      <el-pagination
        v-model:current-page="current"
        v-model:page-size="size"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listSystemLoginLogs, type SystemLoginLog } from '@/admin/api/system'

const loading = ref(false)
const records = ref<SystemLoginLog[]>([])
const username = ref('')
const loginType = ref<string>()
const success = ref<number>()
const staffOnly = ref(false)
const current = ref(1)
const size = ref(20)
const total = ref(0)

async function load() {
  loading.value = true
  try {
    const page = await listSystemLoginLogs({
      current: current.value,
      size: size.value,
      username: username.value || undefined,
      loginType: loginType.value,
      success: success.value,
      staffOnly: staffOnly.value || undefined,
    })
    records.value = page.records
    total.value = page.total
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.login-log__toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
  align-items: center;
}
.login-log__pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
