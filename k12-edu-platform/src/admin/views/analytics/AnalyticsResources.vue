<template>
  <AdminPageShell title="资源分析" desc="资源增长、分布与热门排行（受数据范围约束）">
    <div class="toolbar">
      <el-radio-group v-model="days" size="small" @change="load">
        <el-radio-button :value="7">近 7 天</el-radio-button>
        <el-radio-button :value="30">近 30 天</el-radio-button>
      </el-radio-group>
      <el-button size="small" :loading="loading" @click="load">刷新</el-button>
    </div>
    <p v-if="data?.scoped && data.scopeHint" class="hint">{{ data.scopeHint }}</p>

    <el-row v-loading="loading" :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header>资源上传趋势</template>
          <TrendBars :points="data?.resourceUploadTrend ?? []" show-cumulative />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header>审核通过率（近 {{ days }} 天）</template>
          <div class="audit-block">
            <div class="audit-rate">{{ auditPassRateText }}</div>
            <div class="audit-detail">
              {{ data?.audit?.approved ?? 0 }} 通过 / {{ data?.audit?.rejected ?? 0 }} 驳回
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="section">
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header>学段分布</template>
          <AnalyticsMiniBar :items="stageItems" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header>学科分布</template>
          <AnalyticsMiniBar :items="subjectItems" />
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="section">
      <template #header>
        <div class="top-header">
          <span>热门资源 Top 10</span>
          <el-radio-group v-model="topTab" size="small">
            <el-radio-button value="download">下载</el-radio-button>
            <el-radio-button value="view">浏览</el-radio-button>
            <el-radio-button value="collect">收藏</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <el-table :data="currentTop" size="small" stripe>
        <el-table-column type="index" width="48" label="#" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="stage" label="学段" width="80" />
        <el-table-column prop="subject" label="学科" width="80" />
        <el-table-column prop="downloadCount" label="下载" width="72" />
        <el-table-column prop="viewCount" label="浏览" width="72" />
        <el-table-column prop="collectCount" label="收藏" width="72" />
        <el-table-column label="操作" width="88" fixed="right">
          <template #default="{ row }">
            <el-button v-if="hasResourceView" link type="primary" @click="goResource(row.id)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AdminPageShell from '@/admin/components/AdminPageShell.vue'
import AnalyticsMiniBar from '@/admin/components/AnalyticsMiniBar.vue'
import TrendBars from '@/admin/components/TrendBars.vue'
import { getAnalyticsDashboard, type AnalyticsDashboard, type AnalyticsTopResource } from '@/admin/api/analytics'
import { useAdminAuthStore } from '@/admin/store/adminAuth'

const loading = ref(false)
const days = ref(7)
const topTab = ref<'download' | 'view' | 'collect'>('download')
const data = ref<AnalyticsDashboard | null>(null)
const router = useRouter()
const adminStore = useAdminAuthStore()
const hasResourceView = computed(() => adminStore.hasPermission('admin:resource:view'))

const auditPassRateText = computed(() => {
  const rate = data.value?.audit?.passRate
  return rate == null ? '-' : `${rate}%`
})

const stageItems = computed(() =>
  (data.value?.stageDistribution ?? []).map((x) => ({ label: x.name || '未分类', value: x.count })),
)
const subjectItems = computed(() =>
  (data.value?.subjectDistribution ?? []).map((x) => ({ label: x.name || '未分类', value: x.count })),
)

const currentTop = computed<AnalyticsTopResource[]>(() => {
  if (!data.value) return []
  if (topTab.value === 'view') return data.value.topByView
  if (topTab.value === 'collect') return data.value.topByCollect
  return data.value.topByDownload
})

function goResource(id: number) {
  router.push(`/admin/resource-main/${id}`)
}

async function load() {
  loading.value = true
  try {
    data.value = await getAnalyticsDashboard(days.value)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.hint {
  margin: 0 0 12px;
  font-size: 13px;
  color: #909399;
}
.section {
  margin-top: 16px;
}
.audit-block {
  padding: 24px 8px;
  text-align: center;
}
.audit-rate {
  font-size: 36px;
  font-weight: 700;
  color: #303133;
}
.audit-detail {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}
.top-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
