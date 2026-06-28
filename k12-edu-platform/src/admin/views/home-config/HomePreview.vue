<template>
  <div>
    <div class="home-ops__toolbar">
      <el-select v-model="stageKey" style="width: 140px" @change="load">
        <el-option label="小学 primary" value="primary" />
        <el-option label="初中 junior" value="junior" />
        <el-option label="高中 senior" value="senior" />
      </el-select>
      <el-button @click="load">刷新预览</el-button>
      <el-button v-if="canEdit" @click="invalidateCache">清缓存</el-button>
      <el-button v-if="canEdit" @click="runSchedule">跑定时任务</el-button>
    </div>

    <el-row v-loading="loading" :gutter="16">
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>首屏 Hero</template>
          <p>Banner {{ preview?.hero?.banners?.length ?? 0 }} · 快捷 {{ preview?.hero?.quickEntries?.length ?? 0 }} · 热词 {{ preview?.hero?.hotWords?.length ?? 0 }}</p>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>最新内容</template>
          <p>{{ preview?.latestColumns?.length ?? 0 }} 列</p>
          <ul v-if="preview?.latestColumns?.length" class="preview-list">
            <li v-for="col in preview.latestColumns" :key="col.key">{{ col.title }}（{{ col.items?.length ?? 0 }} 条）</li>
          </ul>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>升学 / 频道</template>
          <p>升学入口 {{ preview?.funcChannels?.channels?.length ?? 0 }}</p>
          <p>特色频道 {{ preview?.opsChannelCount ?? 0 }}</p>
        </el-card>
      </el-col>
    </el-row>

    <el-card v-if="cacheStats" class="cache-card" shadow="never">
      <template #header>首页读缓存</template>
      <pre>{{ JSON.stringify(cacheStats, null, 2) }}</pre>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useAdminAuthStore } from '@/admin/store/adminAuth'
import { request, unwrapData, type ApiResult } from '@/api/request'

interface HomePreviewPayload {
  hero?: { banners?: unknown[]; quickEntries?: unknown[]; hotWords?: unknown[] }
  latestColumns?: { key: string; title: string; items?: unknown[] }[]
  funcChannels?: { channels?: unknown[] }
  opsChannelCount?: number
}

const adminStore = useAdminAuthStore()
const canEdit = computed(() => adminStore.hasPermission('admin:home:edit'))
const loading = ref(false)
const stageKey = ref('primary')
const preview = ref<HomePreviewPayload | null>(null)
const cacheStats = ref<Record<string, unknown> | null>(null)

async function load() {
  loading.value = true
  try {
    preview.value = await request
      .get<ApiResult<HomePreviewPayload>>('/admin/home/preview', { params: { stageKey: stageKey.value } })
      .then(unwrapData)
    cacheStats.value = await request
      .get<ApiResult<Record<string, unknown>>>('/admin/home/cache/stats')
      .then(unwrapData)
  } catch (e: unknown) {
    ElMessage.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

async function invalidateCache() {
  await request.post<ApiResult<void>>('/admin/home/cache/invalidate')
  ElMessage.success('缓存已清空')
  await load()
}

async function runSchedule() {
  const res = await request.post<ApiResult<{ changed: number }>>('/admin/home/schedule/run')
  ElMessage.success(`定时扫描完成，变更 ${unwrapData(res)?.changed ?? 0} 条`)
  await load()
}

onMounted(load)
</script>

<style scoped>
.home-ops__toolbar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.preview-list { margin: 8px 0 0; padding-left: 18px; font-size: 13px; color: #666; }
.cache-card { margin-top: 16px; }
pre { margin: 0; font-size: 12px; }
</style>
