<template>
  <div class="legacy-api-usage">
    <div class="legacy-api-usage__toolbar">
      <el-radio-group v-model="days" size="small" @change="load">
        <el-radio-button :value="7">近 7 天</el-radio-button>
        <el-radio-button :value="14">近 14 天</el-radio-button>
        <el-radio-button :value="30">近 30 天</el-radio-button>
      </el-radio-group>
      <el-button size="small" :loading="loading" @click="load">刷新</el-button>
    </div>

    <el-alert
      v-if="offlineReady"
      type="success"
      :closable="false"
      show-icon
      title="近 7 天核心旧读接口调用量均为 0，可进入下线评审（Phase 3I-D）"
      class="legacy-api-usage__alert"
    />
    <el-alert
      v-else
      type="warning"
      :closable="false"
      show-icon
      title="旧读接口仍有调用，暂不可物理删除；请继续推进 unified 读与 fallback 清理"
      class="legacy-api-usage__alert"
    />

    <el-row :gutter="16" class="legacy-api-usage__cards">
      <el-col :xs="12" :sm="8">
        <el-card shadow="hover">
          <div class="card-label">窗口总调用</div>
          <div class="card-value">{{ summary.totalHits }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8">
        <el-card shadow="hover">
          <div class="card-label">活跃接口数</div>
          <div class="card-value">{{ summary.activePaths }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8">
        <el-card shadow="hover">
          <div class="card-label">7 天零调用核心 path</div>
          <div class="card-value">{{ summary.zeroCorePaths }}/{{ CORE_LEGACY_PATHS.length }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="aggregatedRows" stripe class="legacy-api-usage__table">
      <el-table-column prop="apiPath" label="接口路径" min-width="280" show-overflow-tooltip />
      <el-table-column prop="hitCount" label="调用次数" width="120" sortable />
      <el-table-column prop="lastHitTime" label="最后调用" width="180" />
      <el-table-column prop="sampleQuery" label="样例 Query" min-width="200" show-overflow-tooltip />
      <el-table-column label="7 天趋势" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.hitCount === 0" type="success" size="small">可下线候选</el-tag>
          <el-tag v-else type="danger" size="small">仍活跃</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getLegacyApiUsage, type LegacyApiUsageRow } from '@/admin/api/resourceMain'

const CORE_LEGACY_PATHS = [
  '/api/primary-chinese/page',
  '/api/resources/browse',
  '/api/topic/resources/page',
  '/api/culture/resources/page',
  '/api/competition/resources/page',
]

const days = ref(7)
const loading = ref(false)
const rows = ref<LegacyApiUsageRow[]>([])

const summary = reactive({
  totalHits: 0,
  activePaths: 0,
  zeroCorePaths: 0,
})

const aggregatedRows = computed(() => {
  const map = new Map<string, LegacyApiUsageRow & { hitCount: number }>()
  for (const row of rows.value) {
    const key = row.apiPath
    const existing = map.get(key)
    if (!existing) {
      map.set(key, {
        ...row,
        hitCount: Number(row.hitCount || 0),
      })
      continue
    }
    existing.hitCount += Number(row.hitCount || 0)
    if (row.lastHitTime && (!existing.lastHitTime || row.lastHitTime > existing.lastHitTime)) {
      existing.lastHitTime = row.lastHitTime
      existing.sampleQuery = row.sampleQuery
    }
  }
  return [...map.values()].sort((a, b) => b.hitCount - a.hitCount)
})

const offlineReady = computed(() => {
  if (days.value !== 7) return false
  const byPath = new Map(aggregatedRows.value.map((r) => [r.apiPath, r.hitCount]))
  return CORE_LEGACY_PATHS.every((p) => (byPath.get(p) || 0) === 0)
})

async function load() {
  loading.value = true
  try {
    rows.value = await getLegacyApiUsage(days.value)
    summary.totalHits = aggregatedRows.value.reduce((sum, r) => sum + r.hitCount, 0)
    summary.activePaths = aggregatedRows.value.filter((r) => r.hitCount > 0).length
    const byPath = new Map(aggregatedRows.value.map((r) => [r.apiPath, r.hitCount]))
    summary.zeroCorePaths = CORE_LEGACY_PATHS.filter((p) => (byPath.get(p) || 0) === 0).length
  } catch {
    ElMessage.error('加载旧接口调用统计失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.legacy-api-usage__toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.legacy-api-usage__alert {
  margin-bottom: 16px;
}

.legacy-api-usage__cards {
  margin-bottom: 16px;
}

.card-label {
  font-size: 13px;
  color: #8e8ea0;
}

.card-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
  color: #1a1a2e;
}

.legacy-api-usage__table {
  width: 100%;
}
</style>
