<template>
  <el-drawer
    :model-value="visible"
    :title="detail ? `审核预览 · ${detail.title}` : '审核预览'"
    size="640px"
    destroy-on-close
    @close="emit('close')"
  >
    <div v-loading="loading" class="audit-preview">
      <template v-if="detail">
        <div v-if="insights" class="insight-alerts">
          <el-alert
            v-if="insights.duplicateHints.length"
            type="warning"
            :closable="false"
            show-icon
            title="疑似重复资源"
          >
            <ul class="hint-list">
              <li v-for="item in insights.duplicateHints" :key="item.id">
                #{{ item.id }} {{ item.title }}
                <el-tag size="small" type="info">{{ matchTypeLabel(item.matchType) }}</el-tag>
                <el-tag size="small">{{ item.statusLabel || '未知' }}</el-tag>
              </li>
            </ul>
          </el-alert>
          <el-alert
            v-if="insights.sensitiveWords.length"
            type="error"
            :closable="false"
            show-icon
            :title="`敏感词提示：${insights.sensitiveWords.join('、')}`"
          />
          <el-alert
            :type="safetyAlertType"
            :closable="false"
            show-icon
            :title="`文件安全：${safetyLabel}`"
            :description="insights.fileSafetyMessage"
          />
        </div>

        <el-descriptions :column="2" border size="small" class="meta-block">
          <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ detail.type || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学段">{{ detail.stage || '-' }}</el-descriptions-item>
          <el-descriptions-item label="学科">{{ detail.subject || '-' }}</el-descriptions-item>
          <el-descriptions-item label="年级">{{ detail.gradeName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="栏目">{{ detail.module || '-' }}</el-descriptions-item>
          <el-descriptions-item label="版本">{{ detail.edition || '-' }}</el-descriptions-item>
          <el-descriptions-item label="单元">{{ detail.unitName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="课文">{{ detail.lessonName || '-' }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.catalogPath" label="目录路径" :span="2">
            {{ detail.catalogPath }}
          </el-descriptions-item>
          <el-descriptions-item label="文件" :span="2">
            {{ detail.originalFilename || detail.fileExt || '-' }}
            <span v-if="detail.fileSizeKb">（{{ formatSize(detail.fileSizeKb) }}）</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.remark" label="简介" :span="2">
            {{ detail.remark }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="preview-block">
          <div class="preview-title">文件预览</div>
          <iframe
            v-if="previewMode === 'pdf'"
            :src="fileUrl"
            class="preview-frame"
            title="PDF 预览"
          />
          <img
            v-else-if="previewMode === 'image'"
            :src="fileUrl"
            class="preview-image"
            alt="资源预览"
          />
          <div v-else class="preview-fallback">
            <p>当前格式暂不支持在线预览</p>
            <el-button type="primary" link @click="openFile">下载/打开文件</el-button>
          </div>
        </div>

        <div class="preview-actions">
          <el-button
            v-permission="'admin:audit:approve'"
            type="success"
            :loading="acting"
            @click="emit('approve', detail.globalId)"
          >
            通过
          </el-button>
          <el-button
            v-permission="'admin:audit:reject'"
            type="danger"
            :loading="acting"
            @click="emit('reject', detail.globalId)"
          >
            驳回
          </el-button>
          <el-button @click="emit('close')">关闭</el-button>
        </div>
      </template>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { buildPrimaryChineseFileUrl } from '@/api/primaryChinese'
import {
  getAuditInsights,
  getPrimaryResource,
  type AdminPrimaryResourceDetail,
  type AuditInsights,
} from '@/admin/api/resources'

const props = defineProps<{
  visible: boolean
  resourceId: number | null
  acting?: boolean
}>()

const emit = defineEmits<{
  close: []
  approve: [id: number]
  reject: [id: number]
}>()

const loading = ref(false)
const detail = ref<AdminPrimaryResourceDetail | null>(null)
const insights = ref<AuditInsights | null>(null)

const fileResourceId = computed(() => detail.value?.sourceId || props.resourceId)

const fileUrl = computed(() =>
  fileResourceId.value ? buildPrimaryChineseFileUrl(fileResourceId.value, 'inline') : '',
)

const previewMode = computed(() => {
  const ext = (detail.value?.fileExt || '').toLowerCase().replace(/^\./, '')
  if (ext === 'pdf') return 'pdf'
  if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext)) return 'image'
  return 'other'
})

const safetyLabel = computed(() => {
  const map: Record<string, string> = {
    safe: '安全',
    pending: '待核查',
    risk: '高风险',
    unknown: '未知',
  }
  return map[insights.value?.fileSafetyStatus || 'unknown'] || '未知'
})

const safetyAlertType = computed(() => {
  const status = insights.value?.fileSafetyStatus
  if (status === 'risk') return 'error'
  if (status === 'pending') return 'warning'
  if (status === 'safe') return 'success'
  return 'info'
})

watch(
  () => [props.visible, props.resourceId] as const,
  ([visible, id]) => {
    if (visible && id) {
      void load(id)
    } else {
      detail.value = null
      insights.value = null
    }
  },
  { immediate: true },
)

async function load(id: number) {
  loading.value = true
  try {
    const [resource, auditInsights] = await Promise.all([
      getPrimaryResource(id),
      getAuditInsights(id).catch(() => null),
    ])
    detail.value = resource
    insights.value = auditInsights
  } finally {
    loading.value = false
  }
}

function formatSize(kb?: number) {
  if (!kb) return '-'
  if (kb < 1024) return `${kb} KB`
  return `${(kb / 1024).toFixed(1)} MB`
}

function matchTypeLabel(type?: string) {
  if (type === 'title') return '标题相同'
  if (type === 'filename') return '文件名相同'
  if (type === 'oss_key') return 'OSS路径相同'
  return type || '相似'
}

function openFile() {
  if (fileResourceId.value) {
    window.open(buildPrimaryChineseFileUrl(fileResourceId.value, 'attachment'), '_blank')
  }
}
</script>

<style scoped>
.audit-preview {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 200px;
}
.insight-alerts {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.hint-list {
  margin: 8px 0 0;
  padding-left: 18px;
  line-height: 1.8;
}
.meta-block {
  margin-top: 4px;
}
.preview-block {
  border: 1px solid var(--el-border-color-light);
  border-radius: 6px;
  overflow: hidden;
}
.preview-title {
  padding: 8px 12px;
  font-size: 13px;
  font-weight: 600;
  background: var(--el-fill-color-light);
  border-bottom: 1px solid var(--el-border-color-light);
}
.preview-frame {
  width: 100%;
  height: 420px;
  border: none;
  display: block;
}
.preview-image {
  display: block;
  max-width: 100%;
  max-height: 420px;
  margin: 0 auto;
}
.preview-fallback {
  padding: 48px 16px;
  text-align: center;
  color: var(--el-text-color-secondary);
}
.preview-actions {
  display: flex;
  gap: 8px;
  padding-top: 8px;
}
</style>
