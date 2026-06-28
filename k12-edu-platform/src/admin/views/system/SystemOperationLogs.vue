<template>
  <div class="op-log">
    <div class="op-log__toolbar">
      <el-input v-model="username" placeholder="用户名" clearable style="width: 140px" />
      <el-input v-model="module" placeholder="模块" clearable style="width: 120px" />
      <el-input v-model="action" placeholder="动作" clearable style="width: 120px" />
      <el-button size="small" @click="filterModule('resource')">资源</el-button>
      <el-button size="small" @click="filterModule('system')">系统</el-button>
      <el-button size="small" @click="filterModule('')">全部</el-button>
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <el-table v-loading="loading" :data="records" border stripe size="small">
      <el-table-column prop="createTime" label="时间" width="170" />
      <el-table-column prop="username" label="用户" width="100" />
      <el-table-column prop="module" label="模块" width="90" />
      <el-table-column prop="action" label="动作" width="120" />
      <el-table-column prop="requestUri" label="URI" min-width="200" show-overflow-tooltip />
      <el-table-column label="结果" width="70">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
    <div class="op-log__pager">
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
import { listSystemLogs } from '@/admin/api/system'
import type { OperationLog } from '@/admin/api/operationLogs'

const loading = ref(false)
const records = ref<OperationLog[]>([])
const username = ref('')
const module = ref('')
const action = ref('')
const current = ref(1)
const size = ref(20)
const total = ref(0)

function filterModule(value: string) {
  module.value = value
  current.value = 1
  load()
}

async function load() {
  loading.value = true
  try {
    const page = await listSystemLogs({
      current: current.value,
      size: size.value,
      username: username.value || undefined,
      module: module.value || undefined,
      action: action.value || undefined,
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
.op-log__toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}
.op-log__pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
