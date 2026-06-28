<template>
  <div v-loading="loading">
    <div class="toolbar">
      <el-radio-group v-model="days" size="small" @change="load">
        <el-radio-button :value="7">近 7 天</el-radio-button>
        <el-radio-button :value="30">近 30 天</el-radio-button>
      </el-radio-group>
      <el-button size="small" @click="load">刷新</el-button>
    </div>

    <el-row v-if="stats" :gutter="16" class="stat-cards">
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover">
          <div class="stat-label">无结果次数</div>
          <div class="stat-value stat-value--warn">{{ stats.zeroResultQueries }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover">
          <div class="stat-label">无结果率</div>
          <div class="stat-value">{{ formatRate(stats.zeroResultRate) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card shadow="hover">
          <div class="stat-label">总搜索次数</div>
          <div class="stat-value">{{ stats.totalQueries }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card v-if="stats" shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span>Top 无结果词</span>
          <el-button link type="primary" @click="exportCsv">导出 CSV</el-button>
        </div>
      </template>
      <el-table :data="stats.topZeroQueries || []" border stripe size="small" empty-text="暂无数据">
        <el-table-column type="index" label="#" width="48" />
        <el-table-column prop="keyword" label="关键词" min-width="140" show-overflow-tooltip />
        <el-table-column prop="zeroCount" label="无结果次数" width="110" align="right" />
        <el-table-column prop="queryCount" label="总搜索次数" width="110" align="right" />
        <el-table-column v-if="canEdit" label="治理" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goSynonymDraft(row.keyword)">同义词草稿</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getSearchStats, type SearchStats } from '@/admin/api/search'
import { useAdminAuthStore } from '@/admin/store/adminAuth'

const loading = ref(false)
const days = ref(7)
const stats = ref<SearchStats | null>(null)
const router = useRouter()
const adminStore = useAdminAuthStore()
const canEdit = computed(() => adminStore.hasPermission('admin:search:edit'))

function formatRate(rate?: number) {
  if (rate == null) return '-'
  return `${(rate * 100).toFixed(1)}%`
}

async function load() {
  loading.value = true
  try {
    stats.value = await getSearchStats(days.value)
  } finally {
    loading.value = false
  }
}

function goSynonymDraft(keyword: string) {
  router.push({ path: '/admin/search/synonyms', query: { draft: keyword } })
}

function exportCsv() {
  const rows = stats.value?.topZeroQueries || []
  const header = 'keyword,zeroCount,queryCount\n'
  const body = rows.map((r) => `${r.keyword},${r.zeroCount},${r.queryCount}`).join('\n')
  const blob = new Blob([header + body], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `zero-result-keywords-${days.value}d.csv`
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}
.stat-cards {
  margin-bottom: 16px;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 6px;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
}
.stat-value--warn {
  color: #e6a23c;
}
.table-card {
  margin-top: 8px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>
