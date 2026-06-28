<template>
  <div class="quality-dashboard">
    <!-- 工具栏 -->
    <div class="dash-toolbar">
      <el-radio-group v-model="days" size="small" @change="load">
        <el-radio-button :value="7">近 7 天</el-radio-button>
        <el-radio-button :value="30">近 30 天</el-radio-button>
      </el-radio-group>
      <el-button size="small" :loading="loading" @click="load">刷新</el-button>
    </div>

    <div v-loading="loading">
      <!-- 质量风险卡片行 -->
      <el-row :gutter="16" class="dash-cards">
        <el-col :xs="12" :sm="8" :md="6">
          <el-card shadow="hover" class="dash-card">
            <div class="dash-card__label">文件安全风险</div>
            <div class="dash-card__value" :class="riskColorClass(dashboard?.fileSafetyRiskCount ?? 0)">
              {{ dashboard?.fileSafetyRiskCount ?? '-' }}
            </div>
            <div class="dash-card__sub">高风险文件</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <el-card shadow="hover" class="dash-card">
            <div class="dash-card__label">预览失败</div>
            <div class="dash-card__value dash-card__value--warn">
              {{ dashboard?.previewFailPendingCount ?? '-' }}
            </div>
            <div class="dash-card__sub">待处理队列</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <el-card shadow="hover" class="dash-card">
            <div class="dash-card__label">低访问资源</div>
            <div class="dash-card__value dash-card__value--info">
              {{ dashboard?.lowAccessCount ?? '-' }}
            </div>
            <div class="dash-card__sub">上架>30天 低浏览</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <el-card shadow="hover" class="dash-card">
            <div class="dash-card__label">敏感词库</div>
            <div class="dash-card__value">{{ dashboard?.sensitiveWordCount ?? '-' }}</div>
            <div class="dash-card__sub">已启用词数</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- SLA 卡片行 -->
      <el-row :gutter="16" class="dash-cards" style="margin-top:16px">
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="dash-card">
            <div class="dash-card__label">平均审核时长</div>
            <div class="dash-card__value dash-card__value--primary">
              {{ dashboard?.sla?.avgAuditDurationFormatted ?? '-' }}
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="dash-card">
            <div class="dash-card__label">超时待审 &gt;24h</div>
            <div class="dash-card__value dash-card__value--warn">
              {{ dashboard?.sla?.overtime24hCount ?? '-' }}
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="dash-card">
            <div class="dash-card__label">超时待审 &gt;48h</div>
            <div class="dash-card__value dash-card__value--danger">
              {{ dashboard?.sla?.overtime48hCount ?? '-' }}
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="dash-card">
            <div class="dash-card__label">当日审核完成</div>
            <div class="dash-card__value">{{ dashboard?.sla?.todayCompletedCount ?? '-' }}</div>
            <div class="dash-card__sub">驳回 {{ dashboard?.sla?.todayRejectedCount ?? 0 }}</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 增长趋势卡片 -->
      <el-row :gutter="16" class="dash-cards" style="margin-top:16px">
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="dash-card">
            <div class="dash-card__label">资源总数</div>
            <div class="dash-card__value">{{ dashboard?.growthTrend?.totalCount ?? '-' }}</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="dash-card">
            <div class="dash-card__label">审核通过率</div>
            <div class="dash-card__value dash-card__value--success">
              {{ dashboard?.growthTrend?.approvalRate ?? '-' }}
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="dash-card">
            <div class="dash-card__label">驳回率</div>
            <div class="dash-card__value dash-card__value--warn">
              {{ dashboard?.growthTrend?.rejectionRate ?? '-' }}
            </div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="hover" class="dash-card">
            <div class="dash-card__label">下架率</div>
            <div class="dash-card__value dash-card__value--info">
              {{ dashboard?.growthTrend?.offlineRate ?? '-' }}
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 驳回原因 & 审核员工作量双栏 -->
      <el-row :gutter="16" style="margin-top:16px">
        <el-col :span="12">
          <el-card shadow="never" header="驳回原因分布（近30天）">
            <el-table :data="dashboard?.rejectStats ?? []" size="small" max-height="360">
              <el-table-column prop="categoryName" label="分类" width="100" />
              <el-table-column prop="reason" label="原因" show-overflow-tooltip />
              <el-table-column prop="rejectCount" label="次数" width="80" align="right" />
              <el-table-column prop="percentage" label="占比" width="80" align="right">
                <template #default="{ row }">
                  {{ row.percentage }}%
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="never" header="审核员工作量 Top10（近30天）">
            <el-table :data="dashboard?.auditorWorkload ?? []" size="small" max-height="360">
              <el-table-column prop="auditorName" label="审核员" width="100" />
              <el-table-column prop="approveCount" label="通过" width="70" align="right" />
              <el-table-column prop="rejectCount" label="驳回" width="70" align="right" />
              <el-table-column prop="totalCount" label="合计" width="70" align="right" />
              <el-table-column prop="avgDurationSec" label="平均耗时" width="100" align="right">
                <template #default="{ row }">
                  {{ formatSecs(row.avgDurationSec) }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getQualityDashboard, type QualityDashboard } from '@/admin/api/quality'

const loading = ref(false)
const days = ref(30)
const dashboard = ref<QualityDashboard | null>(null)

async function load() {
  loading.value = true
  try {
    dashboard.value = await getQualityDashboard(days.value)
  } catch (e) {
    console.error('加载质量大盘失败', e)
  } finally {
    loading.value = false
  }
}

function formatSecs(secs: number): string {
  if (!secs || secs <= 0) return '0m'
  const h = Math.floor(secs / 3600)
  const m = Math.floor((secs % 3600) / 60)
  return h > 0 ? `${h}h${m}m` : `${m}m`
}

function riskColorClass(count: number): string {
  if (count > 5) return 'dash-card__value--danger'
  if (count > 0) return 'dash-card__value--warn'
  return 'dash-card__value--success'
}

onMounted(load)
</script>

<style scoped>
.quality-dashboard {
  padding: 0;
}

.dash-toolbar {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 16px;
}

.dash-cards {
  margin-bottom: 0;
}

.dash-card {
  height: 100%;
}

.dash-card__label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.dash-card__value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.dash-card__value--warn {
  color: #e6a23c;
}

.dash-card__value--danger {
  color: #f56c6c;
}

.dash-card__value--success {
  color: #67c23a;
}

.dash-card__value--info {
  color: #409eff;
}

.dash-card__value--primary {
  color: #409eff;
}

.dash-card__sub {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 4px;
}
</style>
