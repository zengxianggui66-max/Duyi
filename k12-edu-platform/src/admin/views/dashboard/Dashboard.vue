<template>
  <AdminPageShell title="内容运营驾驶舱" desc="MySQL 日维度聚合 · 主资源库 oss_primary_chinese_resource">
    <div class="dash-toolbar">
      <el-radio-group v-model="days" size="small" @change="load">
        <el-radio-button :value="7">近 7 天</el-radio-button>
        <el-radio-button :value="30">近 30 天</el-radio-button>
      </el-radio-group>
      <el-button size="small" :loading="loading" @click="load">刷新</el-button>
    </div>

    <p v-if="dashboard?.scoped && dashboard.scopeHint" class="dash-hint">{{ dashboard.scopeHint }}</p>

    <el-row v-loading="loading" :gutter="16" class="dash-cards">
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <el-card shadow="hover" class="dash-card">
          <div class="dash-card__label">用户总数</div>
          <div class="dash-card__value">{{ userStats?.totalUsers ?? '-' }}</div>
          <div class="dash-card__sub">近 {{ userDays }} 天新增 {{ userStats?.newUsersInPeriod ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <el-card shadow="hover" class="dash-card">
          <div class="dash-card__label">资源总数</div>
          <div class="dash-card__value">{{ summary.totalResources ?? '-' }}</div>
          <div class="dash-card__sub">近 {{ days }} 天 +{{ summary.newResourcesInPeriod ?? '-' }}</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <el-card shadow="hover" class="dash-card">
          <div class="dash-card__label">待审核</div>
          <div class="dash-card__value dash-card__value--warn">{{ summary.pendingResources ?? '-' }}</div>
          <div class="dash-card__sub dash-card__sub--placeholder">&nbsp;</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <el-card shadow="hover" class="dash-card">
          <div class="dash-card__label">已发布</div>
          <div class="dash-card__value">{{ summary.publishedResources ?? '-' }}</div>
          <div class="dash-card__sub dash-card__sub--placeholder">&nbsp;</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <el-card shadow="hover" class="dash-card">
          <div class="dash-card__label">总下载</div>
          <div class="dash-card__value">{{ summary.totalDownloads ?? '-' }}</div>
          <div class="dash-card__sub dash-card__sub--placeholder">&nbsp;</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <el-card shadow="hover" class="dash-card">
          <div class="dash-card__label">总浏览</div>
          <div class="dash-card__value">{{ summary.totalViews ?? '-' }}</div>
          <div class="dash-card__sub dash-card__sub--placeholder">&nbsp;</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <el-card shadow="hover" class="dash-card">
          <div class="dash-card__label">总收藏</div>
          <div class="dash-card__value">{{ summary.totalCollects ?? '-' }}</div>
          <div class="dash-card__sub dash-card__sub--placeholder">&nbsp;</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :md="6" :lg="4">
        <el-card shadow="hover" class="dash-card">
          <div class="dash-card__label">审核通过率</div>
          <div class="dash-card__value">{{ auditPassRateText }}</div>
          <div class="dash-card__sub">近 {{ days }} 天 {{ audit.approved ?? 0 }} 通过 / {{ audit.rejected ?? 0 }} 驳回</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Phase 8: 质量风险快报卡片行 -->
    <h3 class="dash-section-title">质量风险快报</h3>
    <el-row v-loading="qualityLoading" :gutter="16" class="dash-cards">
      <el-col :xs="12" :sm="6" :lg="4">
        <el-card shadow="hover" class="dash-card dash-card--risk">
          <div class="dash-card__label">文件安全风险</div>
          <div class="dash-card__value" :class="fileRiskClass">
            {{ qualityCards.fileSafetyRisk ?? '-' }}
          </div>
          <div class="dash-card__sub">高风险文件</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6" :lg="4">
        <el-card shadow="hover" class="dash-card dash-card--warn">
          <div class="dash-card__label">预览失败</div>
          <div class="dash-card__value dash-card__value--warn">
            {{ qualityCards.previewFailPending ?? '-' }}
          </div>
          <div class="dash-card__sub">待处理队列</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6" :lg="4">
        <el-card shadow="hover" class="dash-card dash-card--warn">
          <div class="dash-card__label">超时待审 &gt;24h</div>
          <div class="dash-card__value dash-card__value--warn">
            {{ qualityCards.overtime24h ?? '-' }}
          </div>
          <div class="dash-card__sub">需优先处理</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6" :lg="4">
        <el-card shadow="hover" class="dash-card">
          <div class="dash-card__label">低访问资源</div>
          <div class="dash-card__value">{{ qualityCards.lowAccess ?? '-' }}</div>
          <div class="dash-card__sub">>30天 低浏览/下载</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6" :lg="4">
        <el-card shadow="hover" class="dash-card">
          <div class="dash-card__label">敏感词库</div>
          <div class="dash-card__value">{{ qualityCards.sensitiveWords ?? '-' }}</div>
          <div class="dash-card__sub">已启用词数</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6" :lg="4">
        <el-card shadow="hover" class="dash-card">
          <div class="dash-card__label">驳回率</div>
          <div class="dash-card__value dash-card__value--warn">
            {{ qualityCards.rejectionRate ?? '-' }}
          </div>
          <div class="dash-card__sub">近30天</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="dash-section">
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header>学段分布</template>
          <AnalyticsMiniBar :items="stageBarItems" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="never">
          <template #header>学科分布</template>
          <AnalyticsMiniBar :items="subjectBarItems" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="dash-section">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never">
          <template #header>资源上传趋势</template>
          <TrendBars :points="dashboard?.resourceUploadTrend ?? []" show-cumulative />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="never">
          <template #header>互动趋势</template>
          <el-table :data="dashboard?.actionTrend ?? []" size="small" stripe max-height="280">
            <el-table-column prop="date" label="日期" width="120" />
            <el-table-column label="浏览" min-width="140">
              <template #default="{ row }">
                <span class="trend-cell">{{ row.views }}</span>
                <span class="trend-bar" :style="actionBarStyle(row.views, maxActionViews)" />
              </template>
            </el-table-column>
            <el-table-column label="下载" min-width="140">
              <template #default="{ row }">
                <span class="trend-cell">{{ row.downloads }}</span>
                <span class="trend-bar trend-bar--green" :style="actionBarStyle(row.downloads, maxActionDownloads)" />
              </template>
            </el-table-column>
            <el-table-column label="收藏" min-width="140">
              <template #default="{ row }">
                <span class="trend-cell">{{ row.collects }}</span>
                <span class="trend-bar trend-bar--orange" :style="actionBarStyle(row.collects, maxActionCollects)" />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="dash-section">
      <el-col :span="24">
        <el-card shadow="never">
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
          <el-table :data="currentTopList" size="small" stripe>
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
      </el-col>
    </el-row>

    <el-row v-if="topUploaders.length" :gutter="16" class="dash-section">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header>上传量排行（近 {{ days }} 天 · 按上传者）</template>
          <el-table :data="topUploaders" size="small" stripe>
            <el-table-column type="index" width="48" label="#" />
            <el-table-column prop="uploaderId" label="上传者 ID" width="120" />
            <el-table-column prop="uploadCount" label="上传数" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <div class="dash-actions">
      <el-button v-if="hasAuditView" type="primary" @click="$router.push('/admin/audit/resources')">
        前往审核中心（{{ summary.pendingResources ?? 0 }} 待审）
      </el-button>
      <el-button v-if="hasResourceView" @click="$router.push('/admin/resource-main')">资源管理</el-button>
      <el-button v-if="hasSearchView" @click="$router.push('/admin/search/overview')">搜索运营</el-button>
    </div>
  </AdminPageShell>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import AdminPageShell from '@/admin/components/AdminPageShell.vue'
import AnalyticsMiniBar from '@/admin/components/AnalyticsMiniBar.vue'
import TrendBars from '@/admin/components/TrendBars.vue'
import {
  getAnalyticsDashboard,
  getAnalyticsUsers,
  type AnalyticsDashboard,
  type AnalyticsTopResource,
  type AnalyticsUser,
} from '@/admin/api/analytics'
import { getQualityDashboard, getGrowthTrend } from '@/admin/api/quality'
import { useAdminAuthStore } from '@/admin/store/adminAuth'

const router = useRouter()
const loading = ref(false)
const qualityLoading = ref(false)
const days = ref(7)
const userDays = 30
const topTab = ref<'download' | 'view' | 'collect'>('download')
const dashboard = ref<AnalyticsDashboard | null>(null)
const userStats = ref<AnalyticsUser | null>(null)
const adminStore = useAdminAuthStore()

const hasAuditView = computed(() => adminStore.hasPermission('admin:audit:view'))
const hasResourceView = computed(() => adminStore.hasPermission('admin:resource:view'))
const hasSearchView = computed(() => adminStore.hasPermission('admin:search:view'))
const hasQualityView = computed(() => adminStore.hasPermission('admin:quality:sensitive_view'))

const summary = computed(() => dashboard.value?.summary ?? ({} as AnalyticsDashboard['summary']))
const audit = computed(() => dashboard.value?.audit ?? { approved: 0, rejected: 0 })
const topUploaders = computed(() => dashboard.value?.topUploaders ?? [])

// Phase 8: 质量风险快报数据
const qualityCards = reactive({
  fileSafetyRisk: 0 as number,
  previewFailPending: 0 as number,
  overtime24h: 0 as number,
  lowAccess: 0 as number,
  sensitiveWords: 0 as number,
  rejectionRate: '0%' as string,
})

const fileRiskClass = computed(() => {
  const v = qualityCards.fileSafetyRisk
  if (v > 5) return 'dash-card__value--danger'
  if (v > 0) return 'dash-card__value--warn'
  return 'dash-card__value--success'
})

const auditPassRateText = computed(() => {
  const rate = dashboard.value?.audit?.passRate
  if (rate == null) return '-'
  return `${rate}%`
})

const stageBarItems = computed(() =>
  (dashboard.value?.stageDistribution ?? []).map((x) => ({ label: x.name || '未分类', value: x.count })),
)
const subjectBarItems = computed(() =>
  (dashboard.value?.subjectDistribution ?? []).map((x) => ({ label: x.name || '未分类', value: x.count })),
)

const currentTopList = computed<AnalyticsTopResource[]>(() => {
  if (!dashboard.value) return []
  if (topTab.value === 'view') return dashboard.value.topByView
  if (topTab.value === 'collect') return dashboard.value.topByCollect
  return dashboard.value.topByDownload
})

const maxActionViews = computed(() =>
  Math.max(...(dashboard.value?.actionTrend ?? []).map((r) => r.views), 1),
)
const maxActionDownloads = computed(() =>
  Math.max(...(dashboard.value?.actionTrend ?? []).map((r) => r.downloads), 1),
)
const maxActionCollects = computed(() =>
  Math.max(...(dashboard.value?.actionTrend ?? []).map((r) => r.collects), 1),
)

function actionBarStyle(value: number, max: number) {
  const pct = Math.round((value / max) * 100)
  return { width: `${Math.max(pct, value > 0 ? 6 : 0)}%` }
}

function goResource(id: number) {
  router.push(`/admin/resource-main/${id}`)
}

async function load() {
  if (!adminStore.hasPermission('admin:dashboard:view')) return
  loading.value = true
  qualityLoading.value = true
  try {
    const [dash, users] = await Promise.all([
      getAnalyticsDashboard(days.value),
      getAnalyticsUsers(userDays),
    ])
    dashboard.value = dash
    userStats.value = users
  } finally {
    loading.value = false
  }

  // Phase 8: 并行加载质量快报（独立加载，失败不影响主面板）
  if (hasQualityView.value) {
    try {
      const [quality, growth] = await Promise.all([
        getQualityDashboard(30),
        getGrowthTrend(30),
      ])
      qualityCards.fileSafetyRisk = quality?.fileSafetyRiskCount ?? 0
      qualityCards.previewFailPending = quality?.previewFailPendingCount ?? 0
      qualityCards.overtime24h = quality?.sla?.overtime24hCount ?? 0
      qualityCards.lowAccess = quality?.lowAccessCount ?? 0
      qualityCards.sensitiveWords = quality?.sensitiveWordCount ?? 0
      qualityCards.rejectionRate = growth?.rejectionRate ?? '0%'
    } catch {
      // 质量数据加载失败，静默处理
    }
  }
  qualityLoading.value = false
}

onMounted(load)
</script>

<style scoped>
.dash-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.dash-hint {
  margin: 0 0 12px;
  font-size: 13px;
  color: #909399;
}
.dash-cards {
  margin-bottom: 16px;
}
.dash-card {
  margin-bottom: 12px;
}
.dash-card__label {
  font-size: 13px;
  color: #8e8ea0;
  margin-bottom: 4px;
}
.dash-card__value {
  font-size: 24px;
  font-weight: 700;
  color: #1a1a2e;
  line-height: 1.2;
}
.dash-card__value--warn {
  color: #e6a23c;
}
.dash-card__sub {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}
.dash-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin: 20px 0 12px;
}
.dash-section {
  margin-bottom: 16px;
}
.top-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
.trend-cell {
  display: inline-block;
  min-width: 36px;
  margin-right: 8px;
  font-variant-numeric: tabular-nums;
}
.trend-bar {
  display: inline-block;
  height: 8px;
  max-width: 120px;
  background: #409eff;
  border-radius: 3px;
  vertical-align: middle;
}
.trend-bar--green {
  background: #67c23a;
}
.trend-bar--orange {
  background: #e6a23c;
}
.dash-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 8px;
}
</style>
