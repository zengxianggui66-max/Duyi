<template>
  <div v-loading="loading">
    <div class="toolbar">
      <el-radio-group v-model="days" @change="load">
        <el-radio-button :value="7">近 7 天</el-radio-button>
        <el-radio-button :value="30">近 30 天</el-radio-button>
      </el-radio-group>
      <el-button @click="load">刷新</el-button>
    </div>

    <el-row v-if="stats" :gutter="16" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-label">搜索次数</div>
          <div class="stat-value">{{ stats.totalQueries }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-label">无结果次数</div>
          <div class="stat-value stat-value--warn">{{ stats.zeroResultQueries }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-label">无结果率</div>
          <div class="stat-value">{{ formatRate(stats.zeroResultRate) }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-label">点击率 (CTR)</div>
          <div class="stat-value">{{ formatRate(stats.clickThroughRate) }}</div>
          <div class="stat-sub">点击 {{ stats.totalClicks }} 次</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row v-if="stats" :gutter="16" class="tables-row">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>Top 搜索词</span>
              <el-button link type="primary" @click="exportCsv('top')">导出 CSV</el-button>
            </div>
          </template>
          <el-table :data="stats.topQueries || []" border stripe size="small" empty-text="暂无数据">
            <el-table-column type="index" label="#" width="48" />
            <el-table-column prop="keyword" label="关键词" min-width="140" show-overflow-tooltip />
            <el-table-column prop="queryCount" label="搜索次数" width="100" align="right" />
            <el-table-column label="平均命中" width="100" align="right">
              <template #default="{ row }">{{ formatHits(row.avgHits) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>
            <div class="card-header">
              <span>Top 无结果词</span>
              <el-button link type="primary" @click="exportCsv('zero')">导出 CSV</el-button>
            </div>
          </template>
          <el-table :data="stats.topZeroQueries || []" border stripe size="small" empty-text="暂无数据">
            <el-table-column type="index" label="#" width="48" />
            <el-table-column prop="keyword" label="关键词" min-width="120" show-overflow-tooltip />
            <el-table-column prop="zeroCount" label="无结果次数" width="100" align="right" />
            <el-table-column prop="queryCount" label="总搜索次数" width="100" align="right" />
            <el-table-column v-if="canEdit" label="治理" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="goSynonymDraft(row.keyword)">同义词草稿</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getSearchStats, type SearchStats } from '@/admin/api/search'
import { useAdminAuthStore } from '@/admin/store/adminAuth'

const router = useRouter()
const adminStore = useAdminAuthStore()
const canEdit = computed(() => adminStore.hasPermission('admin:search:edit'))

const loading = ref(false)
const days = ref(7)
const stats = ref<SearchStats | null>(null)

function formatRate(rate: number | undefined) {
  if (rate == null || Number.isNaN(rate)) return '-'
  return `${(rate * 100).toFixed(2)}%`
}

function formatHits(hits: number | undefined) {
  if (hits == null || Number.isNaN(hits)) return '-'
  return hits.toFixed(1)
}

async function load() {
  loading.value = true
  try {
    stats.value = await getSearchStats(days.value)
  } catch {
    ElMessage.error('加载搜索统计失败')
  } finally {
    loading.value = false
  }
}

function goSynonymDraft(keyword: string) {
  router.push({ path: '/admin/search/synonyms', query: { draft: keyword } })
}

function exportCsv(kind: 'top' | 'zero') {
  if (!stats.value) return
  const rows = kind === 'top' ? stats.value.topQueries : stats.value.topZeroQueries
  if (!rows?.length) {
    ElMessage.warning('暂无数据可导出')
    return
  }
  const header =
    kind === 'top'
      ? ['关键词', '搜索次数', '平均命中']
      : ['关键词', '无结果次数', '总搜索次数']
  const lines = [header.join(',')]
  for (const row of rows) {
    if (kind === 'top') {
      lines.push([csvCell(row.keyword), row.queryCount, formatHits(row.avgHits)].join(','))
    } else {
      lines.push([csvCell(row.keyword), row.zeroCount ?? 0, row.queryCount].join(','))
    }
  }
  const blob = new Blob(['\uFEFF' + lines.join('\n')], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = kind === 'top' ? `search-top-queries-${days.value}d.csv` : `search-zero-queries-${days.value}d.csv`
  a.click()
  URL.revokeObjectURL(url)
}

function csvCell(text: string) {
  const s = String(text ?? '')
  if (/[",\n]/.test(s)) return `"${s.replace(/"/g, '""')}"`
  return s
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
.stat-cards {
  margin-bottom: 20px;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a2e;
}
.stat-value--warn {
  color: #e6a23c;
}
.stat-sub {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}
.tables-row {
  margin-top: 4px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
</style>