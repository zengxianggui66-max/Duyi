<template>
  <motion.div class="topic-detail-page" v-loading="loading" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }">
    <motion.div class="container" :initial="{ opacity: 0, y: 8 }" :animate="{ opacity: 1, y: 0 }">
      <nav class="breadcrumb">
        <router-link to="/">首页</router-link>
        <span class="sep">/</span>
        <router-link to="/topic-zone">专题资源</router-link>
        <span class="sep">/</span>
        <span class="current">资源详情</span>
      </nav>

      <el-empty v-if="!loading && !resource" description="资源不存在或已下架">
        <el-button type="primary" @click="router.push('/topic-zone')">返回专题资源</el-button>
      </el-empty>

      <template v-else-if="resource">
        <motion.header class="detail-header card" :initial="{ opacity: 0, y: 10 }" :animate="{ opacity: 1, y: 0 }">
          <div class="title-row">
            <span class="res-icon">{{ resource.icon || '📚' }}</span>
            <motion.div class="title-block" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.08 }">
              <h1>{{ resource.title }}</h1>
              <div class="tag-row">
                <el-tag type="warning">{{ categoryName(resource.category) }}</el-tag>
                <el-tag>{{ regionName(resource.region) }}</el-tag>
                <el-tag>{{ gradeName(resource.gradeStage) }}</el-tag>
                <el-tag type="info">{{ formName(resource.resourceForm) }}</el-tag>
                <el-tag v-if="resource.topicLabel" type="success">{{ resource.topicLabel }}</el-tag>
                <el-tag v-if="resource.isFree === 1" type="success" effect="plain">免费</el-tag>
                <el-tag v-else type="danger" effect="plain">精品</el-tag>
              </div>
            </motion.div>
          </div>
          <p class="summary">{{ resource.summary || '暂无简介' }}</p>
          <div class="stats-row">
            <span>👁 {{ resource.viewCount ?? 0 }} 浏览</span>
            <span>⬇ {{ formatCount(resource.downloadCount) }} 下载</span>
            <span v-if="resource.fileFormat">📎 {{ resource.fileFormat }}</span>
          </div>
          <motion.div class="actions" :initial="{ opacity: 0 }" :animate="{ opacity: 1 }" :transition="{ delay: 0.15 }">
            <el-button type="primary" size="large" :disabled="!resource.fileUrl" @click="handleDownload">
              下载资源
            </el-button>
            <el-button v-if="resource.fileUrl" size="large" @click="openPreview">在线预览</el-button>
            <el-button size="large" @click="router.push('/topic-zone')">返回列表</el-button>
          </motion.div>
        </motion.header>

        <motion.section v-if="resource.tags" class="tags-section card" :initial="{ opacity: 0, y: 8 }" :animate="{ opacity: 1, y: 0 }" :transition="{ delay: 0.12 }">
          <h3>标签</h3>
          <div class="tags-wrap">
            <el-tag v-for="t in tagList" :key="t" size="small" class="tag-item">{{ t }}</el-tag>
          </div>
        </motion.section>

        <motion.section v-if="related.length" class="related-section card" :initial="{ opacity: 0, y: 8 }" :animate="{ opacity: 1, y: 0 }" :transition="{ delay: 0.18 }">
          <h3>相关推荐</h3>
          <ul class="related-list">
            <li v-for="item in related" :key="item.id" @click="goDetail(item.id)">
              {{ item.icon || '📚' }} {{ item.title }}
              <span class="rel-meta">↓ {{ formatCount(item.downloadCount) }}</span>
            </li>
          </ul>
        </motion.section>
      </template>
    </motion.div>
  </motion.div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { motion } from 'motion-v'
import { resourceGateway, type TopicResourceItem } from '@/api/resourceGateway'
import { resourceApi } from '@/api/resource'
import {
  TOPIC_CATEGORY_NAMES,
  TOPIC_REGION_NAMES,
  TOPIC_GRADE_NAMES,
  TOPIC_FORM_NAMES,
} from '@/constants/topicZone'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const resource = ref<TopicResourceItem | null>(null)
const related = ref<TopicResourceItem[]>([])
const useDedicated = ref(true)
const currentGlobalId = ref<number | null>(null)

const tagList = computed(() =>
  (resource.value?.tags || '')
    .split(/[,，]/)
    .map((t) => t.trim())
    .filter(Boolean)
)

function categoryName(key: string) {
  return TOPIC_CATEGORY_NAMES[key] || key
}
function regionName(key: string) {
  return TOPIC_REGION_NAMES[key] || key
}
function gradeName(key: string) {
  return TOPIC_GRADE_NAMES[key] || key
}
function formName(key: string) {
  return TOPIC_FORM_NAMES[key] || key
}
function formatCount(n?: number) {
  if (!n) return '0'
  return n >= 10000 ? `${(n / 10000).toFixed(1)}万` : String(n)
}

async function loadDedicated(id: number) {
  const { item, globalId } = await resourceGateway.getTopicResourceDetail(id)
  currentGlobalId.value = globalId
  resource.value = item
  await resourceGateway.recordViewBySource('topic_resource', id).catch(() => {})
  const { page } = await resourceGateway.listTopicResources({
    category: item.category,
    size: 6,
    current: 1,
  })
  related.value = (page?.records || []).filter((r) => r.id !== id).slice(0, 5)
}

async function loadFallback(id: number) {
  const res = await resourceApi.getDetail(id)
  const r = res.data.data
  if (!r) {
    resource.value = null
    return
  }
  resource.value = {
    id: r.id,
    title: r.title,
    summary: r.description,
    category: 'holiday_hw',
    region: 'all',
    gradeStage: r.gradeLevel || 'all',
    subject: r.subject,
    resourceForm: r.resourceType || 'material',
    fileFormat: r.fileFormat,
    fileUrl: r.fileUrl,
    icon: '📚',
    tags: Array.isArray(r.tags) ? r.tags.join(',') : String(r.tags || ''),
    downloadCount: r.downloadCount,
    viewCount: r.viewCount,
    isFree: 1,
  }
  related.value = []
}

async function load() {
  const id = Number(route.params.id)
  if (!Number.isFinite(id) || id <= 0) {
    resource.value = null
    loading.value = false
    return
  }
  loading.value = true
  try {
    if (useDedicated.value) {
      await loadDedicated(id)
    } else {
      await loadFallback(id)
    }
  } catch {
    if (useDedicated.value) {
      useDedicated.value = false
      try {
        await loadFallback(id)
      } catch {
        resource.value = null
        ElMessage.error('加载资源失败')
      }
    } else {
      resource.value = null
      ElMessage.error('加载资源失败')
    }
  } finally {
    loading.value = false
  }
}

async function handleDownload() {
  if (!resource.value) return
  try {
    const d = await resourceGateway.recordDownloadBySource('topic_resource', resource.value.id)
    if (d?.downloadUrl) {
      window.open(d.downloadUrl, '_blank')
      return
    }
  } catch {
    // 记录失败不阻断打开文件
  }
  if (resource.value.fileUrl) {
    window.open(resource.value.fileUrl, '_blank')
  } else {
    ElMessage.warning('暂无下载地址')
  }
}

function openPreview() {
  if (resource.value?.fileUrl) {
    window.open(resource.value.fileUrl, '_blank')
  }
}

function goDetail(id: number) {
  router.push(`/topic-zone/resource/${id}`)
}

watch(() => route.params.id, () => load())

onMounted(() => load())
</script>

<style scoped>
.topic-detail-page {
  min-height: calc(100vh - 60px);
  background: var(--bg-page, #f5f6f8);
  padding: 24px 0 48px;
}
.container { max-width: 900px; margin: 0 auto; padding: 0 24px; }
.breadcrumb { font-size: 13px; margin-bottom: 20px; color: var(--text-secondary); }
.breadcrumb a { color: #7c3aed; text-decoration: none; }
.sep { margin: 0 8px; }
.current { color: var(--text-primary); }

.detail-header { padding: 28px 32px; margin-bottom: 20px; }
.title-row { display: flex; gap: 16px; align-items: flex-start; margin-bottom: 16px; }
.res-icon { font-size: 48px; flex-shrink: 0; }
.title-block h1 { font-size: 24px; font-weight: 700; margin: 0 0 12px; line-height: 1.35; }
.tag-row { display: flex; flex-wrap: wrap; gap: 8px; }
.summary { color: var(--text-secondary); line-height: 1.7; margin-bottom: 16px; font-size: 15px; }
.stats-row { display: flex; gap: 20px; font-size: 14px; color: var(--text-secondary); margin-bottom: 20px; }
.actions { display: flex; flex-wrap: wrap; gap: 12px; }

.tags-section, .related-section { padding: 20px 24px; margin-bottom: 16px; }
.tags-section h3, .related-section h3 { font-size: 16px; margin-bottom: 12px; }
.tags-wrap { display: flex; flex-wrap: wrap; gap: 8px; }
.related-list { list-style: none; padding: 0; margin: 0; }
.related-list li {
  padding: 12px 14px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
}
.related-list li:hover { background: #f5f3ff; }
.rel-meta { font-size: 12px; color: var(--text-secondary); }
</style>
