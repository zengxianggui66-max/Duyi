<template>
  <div>
    <el-alert
      type="warning"
      :closable="false"
      show-icon
      class="hint"
      title="只读列表"
      description="意图规则误配影响大，本期仅支持查看；编辑将在后续阶段开放。"
    />

    <div class="toolbar">
      <el-checkbox v-model="showDisabled" @change="load">显示已禁用</el-checkbox>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-table v-loading="loading" :data="records" border stripe empty-text="暂无数据">
      <el-table-column prop="pattern" label="匹配模式" min-width="120" show-overflow-tooltip />
      <el-table-column prop="intentType" label="意图类型" width="110" />
      <el-table-column prop="targetKey" label="targetKey" width="110" show-overflow-tooltip />
      <el-table-column prop="targetValue" label="targetValue" min-width="120" show-overflow-tooltip />
      <el-table-column prop="priority" label="优先级" width="80" align="center" />
      <el-table-column label="状态" width="88" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listSearchIntentRules, type SearchIntentRuleItem } from '@/admin/api/search'

const loading = ref(false)
const showDisabled = ref(false)
const records = ref<SearchIntentRuleItem[]>([])

async function load() {
  loading.value = true
  try {
    records.value = await listSearchIntentRules(showDisabled.value)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.hint {
  margin-bottom: 16px;
}
</style>
