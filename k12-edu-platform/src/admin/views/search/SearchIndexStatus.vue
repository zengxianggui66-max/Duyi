<template>
  <div v-loading="loading">
    <div class="toolbar">
      <el-radio-group v-model="days" @change="loadReadiness">
        <el-radio-button :value="7">近 7 天</el-radio-button>
        <el-radio-button :value="30">近 30 天</el-radio-button>
      </el-radio-group>
      <el-button @click="loadAll">刷新</el-button>
      <template v-if="canReindex">
        <el-button type="warning" :loading="reindexing" @click="onReindex">全量重建索引</el-button>
        <el-button type="primary" :loading="syncing" @click="onEngineSync">同步 OpenSearch</el-button>
      </template>
    </div>

    <el-alert
      v-if="!canReindex"
      type="info"
      :closable="false"
      show-icon
      class="hint"
      title="只读模式"
      description="索引重建与 OpenSearch 同步需 content_admin 或 super_admin 权限。"
    />

    <el-row :gutter="16">
      <el-col :span="12">
        <el-card shadow="never" header="搜索引擎健康">
          <el-descriptions v-if="health" :column="1" border size="small">
            <el-descriptions-item label="已启用">
              <el-tag :type="health.enabled ? 'success' : 'info'" size="small">
                {{ health.enabled ? '是' : '否' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="可达">
              <el-tag :type="health.reachable ? 'success' : 'danger'" size="small">
                {{ health.reachable ? '正常' : '不可达' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="集群">{{ health.clusterName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="索引">{{ health.indexName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="文档数">{{ health.docCount ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="最近同步">{{ health.lastSyncTime || '-' }}</el-descriptions-item>
            <el-descriptions-item v-if="health.error" label="错误">
              <span class="error-text">{{ health.error }}</span>
            </el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="暂无健康数据" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" header="P3 就绪评估">
          <template v-if="readiness">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="索引文档数">{{ readiness.totalDocs }}</el-descriptions-item>
              <el-descriptions-item label="规模评估">{{ readiness.scaleVerdict || '-' }}</el-descriptions-item>
              <el-descriptions-item label="耗时评估">{{ readiness.latencyVerdict || '-' }}</el-descriptions-item>
              <el-descriptions-item label="无结果评估">{{ readiness.zeroResultVerdict || '-' }}</el-descriptions-item>
              <el-descriptions-item label="CTR 评估">{{ readiness.ctrVerdict || '-' }}</el-descriptions-item>
              <el-descriptions-item label="综合建议">{{ readiness.overallRecommendation || '-' }}</el-descriptions-item>
              <el-descriptions-item label="P3 模式">{{ readiness.p3Mode || '-' }}</el-descriptions-item>
            </el-descriptions>
            <ul v-if="readiness.notes?.length" class="notes">
              <li v-for="(note, i) in readiness.notes" :key="i">{{ note }}</li>
            </ul>
          </template>
          <el-empty v-else description="暂无评估数据" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getSearchEngineHealth,
  getSearchP3Readiness,
  reindexSearch,
  syncSearchEngine,
  type SearchEngineHealth,
  type SearchP3Readiness,
} from '@/admin/api/search'
import { useAdminAuthStore } from '@/admin/store/adminAuth'

const adminStore = useAdminAuthStore()
const canReindex = computed(() => adminStore.hasPermission('admin:search:reindex'))

const loading = ref(false)
const reindexing = ref(false)
const syncing = ref(false)
const days = ref(7)
const health = ref<SearchEngineHealth | null>(null)
const readiness = ref<SearchP3Readiness | null>(null)

async function loadHealth() {
  health.value = await getSearchEngineHealth()
}

async function loadReadiness() {
  readiness.value = await getSearchP3Readiness(days.value)
}

async function loadAll() {
  loading.value = true
  try {
    await Promise.all([loadHealth(), loadReadiness()])
  } catch {
    ElMessage.error('加载索引状态失败')
  } finally {
    loading.value = false
  }
}

async function onReindex() {
  try {
    await ElMessageBox.confirm('将重建 sys_search_document 与 resource_search_index，可能耗时数分钟，是否继续？', '全量重建索引', {
      type: 'warning',
      confirmButtonText: '开始重建',
    })
  } catch {
    return
  }
  reindexing.value = true
  try {
    const count = await reindexSearch()
    ElMessage.success(`索引重建完成，共 ${count} 条`)
    await loadAll()
  } catch {
    ElMessage.error('索引重建失败')
  } finally {
    reindexing.value = false
  }
}

async function onEngineSync() {
  try {
    await ElMessageBox.confirm('将把 MySQL 索引全量同步到 OpenSearch，是否继续？', '同步 OpenSearch', {
      type: 'warning',
      confirmButtonText: '开始同步',
    })
  } catch {
    return
  }
  syncing.value = true
  try {
    const count = await syncSearchEngine()
    ElMessage.success(`OpenSearch 同步完成，共 ${count} 条`)
    await loadHealth()
  } catch {
    ElMessage.error('OpenSearch 同步失败')
  } finally {
    syncing.value = false
  }
}

onMounted(loadAll)
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
.notes {
  margin: 12px 0 0;
  padding-left: 20px;
  font-size: 13px;
  color: #606266;
}
.error-text {
  color: #f56c6c;
  word-break: break-all;
}
</style>
