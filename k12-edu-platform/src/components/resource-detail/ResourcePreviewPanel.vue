<template>
  <el-card class="preview-card" :class="{ 'preview-card--fullscreen': isFullPreview }">
    <template #header>
      <div class="card-header">
        <span class="header-title">📖 资源预览</span>
        <el-button-group>
          <el-button size="small" @click="$emit('toggle-fullscreen')">
            {{ isFullPreview ? '退出全屏' : '全屏预览' }}
          </el-button>
        </el-button-group>
      </div>
    </template>

    <div class="preview-body">
      <PreviewPptViewer
        v-if="previewType === 'ppt'"
        :title="resource.title"
        :preview-info="filePreviewInfo ?? null"
        :loading="previewLoading"
        :processing="previewProcessing"
        :error="previewError"
        :original-url="resource.fileUrl"
        :is-full-preview="false"
        :current-slide="currentSlide"
        @prev-slide="$emit('prev-slide')"
        @next-slide="$emit('next-slide')"
        @goto-slide="$emit('goto-slide', $event)"
        @slide-count="$emit('slide-count', $event)"
      />

      <PreviewMediaPlayer
        v-else-if="(previewType === 'video' || previewType === 'audio') && mediaUrl"
        :preview-type="previewType"
        :file-url="mediaUrl"
        :cover-url="resource.coverUrl"
        :title="resource.title"
        :is-playing="isPlaying"
        :audio-progress="audioProgress"
        :current-time="currentTime"
        :audio-duration="audioDuration"
        ref="mediaPlayerRef"
        @toggle-audio="$emit('toggle-audio')"
        @audio-loaded="$emit('audio-loaded')"
        @audio-timeupdate="$emit('audio-timeupdate')"
        @seek="$emit('seek-audio', $event)"
      />

      <PreviewFallback
        v-else-if="previewType === 'video' || previewType === 'audio'"
        message="当前媒体暂无可用播放地址"
      />

      <div
        v-else-if="previewType === 'document' && (previewLoading || previewProcessing)"
        class="document-processing"
      >
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>{{ documentProcessingMessage }}</span>
      </div>

      <PreviewDocument
        v-else-if="previewType === 'document' && documentPreviewUrl && documentPreviewMode === 'pdf'"
        :pdf-url="documentPreviewUrl"
        preview-mode="pdf"
        :fallback-message="documentFallbackMessage"
        :fullscreen="isFullPreview"
      />

      <PreviewFallback
        v-else-if="previewType === 'document'"
        :message="documentFallbackMessage"
      />

      <div v-else-if="previewType === 'image' && imageUrl" class="preview-image-wrap">
        <img :src="imageUrl" :alt="resource.title" loading="lazy" />
      </div>

      <PreviewFallback
        v-else-if="previewType === 'image'"
        message="当前图片暂无可用预览地址"
      />

      <PreviewFallback v-else />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import type { FilePreviewInfo } from '@/api/file'
import PreviewPptViewer from './preview/PreviewPptViewer.vue'
import PreviewMediaPlayer from './preview/PreviewMediaPlayer.vue'
import PreviewDocument from './preview/PreviewDocument.vue'
import PreviewFallback from './preview/PreviewFallback.vue'

const props = defineProps<{
  resource: Record<string, any>
  previewType: string
  isFullPreview: boolean
  currentSlide: number
  slideCount: number
  isPlaying: boolean
  audioProgress: number
  currentTime: string
  audioDuration: string
  pdfUrl: string
  playbackUrl?: string
  documentPreviewUrl?: string
  documentPreviewMode?: string
  filePreviewInfo?: FilePreviewInfo | null
  previewLoading?: boolean
  previewProcessing?: boolean
  previewError?: string
}>()

const emit = defineEmits<{
  'toggle-fullscreen': []
  'prev-slide': []
  'next-slide': []
  'goto-slide': [index: number]
  'slide-count': [count: number]
  'toggle-audio': []
  'audio-loaded': []
  'audio-timeupdate': []
  'seek-audio': [event: MouseEvent]
}>()

const mediaPlayerRef = ref<InstanceType<typeof PreviewMediaPlayer> | null>(null)


const mediaUrl = computed(
  () => props.playbackUrl || props.filePreviewInfo?.previewUrl || props.resource?.fileUrl || '',
)

const imageUrl = computed(
  () => props.playbackUrl || props.documentPreviewUrl || props.filePreviewInfo?.previewUrl || '',
)

const documentProcessingMessage = computed(
  () =>
    props.filePreviewInfo?.message ||
    '正在将文档转为 PDF，请稍候…（首次预览约需 1–2 分钟）',
)

const documentFallbackMessage = computed(() => {
  if (props.previewError) return props.previewError
  if (props.filePreviewInfo?.previewMode === 'none') {
    return props.filePreviewInfo.message || '当前文档暂无法在线预览，请下载原文件查看'
  }
  return '当前文档暂无法在线预览，请点击下载后查看'
})

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape' && props.isFullPreview) {
    emit('toggle-fullscreen')
  }
}

watch(
  () => props.isFullPreview,
  (full) => {
    document.body.style.overflow = full ? 'hidden' : ''
    if (full) {
      window.addEventListener('keydown', onKeydown)
    } else {
      window.removeEventListener('keydown', onKeydown)
    }
  },
)

onUnmounted(() => {
  document.body.style.overflow = ''
  window.removeEventListener('keydown', onKeydown)
})

defineExpose({
  get audioPlayer() {
    return mediaPlayerRef.value?.audioRef
  },
})
</script>

<style scoped>
.preview-card {
  border-radius: 12px;
}

.preview-card--fullscreen {
  position: fixed;
  inset: 0;
  z-index: 9999;
  margin: 0;
  border-radius: 0;
  display: flex;
  flex-direction: column;
  height: 100vh;
  width: 100vw;
}

.preview-card--fullscreen :deep(.el-card__body) {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  padding: 12px 16px 16px;
}

.preview-body {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.preview-card--fullscreen .preview-body {
  height: 100%;
}

.preview-card--fullscreen :deep(.preview-ppt-viewer),
.preview-card--fullscreen :deep(.preview-document),
.preview-card--fullscreen :deep(.preview-pdf-viewer),
.preview-card--fullscreen :deep(.preview-pdf-iframe),
.preview-card--fullscreen :deep(.preview-slides),
.preview-card--fullscreen :deep(.preview-media-player) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.preview-card--fullscreen :deep(.slide-container) {
  aspect-ratio: auto;
  flex: 1;
  min-height: 0;
}

.preview-card--fullscreen :deep(.preview-office) {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.preview-card--fullscreen :deep(.office-iframe),
.preview-card--fullscreen :deep(.doc-iframe),
.preview-card--fullscreen :deep(.pdf-iframe) {
  flex: 1;
  min-height: 0;
  height: 100% !important;
}

.preview-card--fullscreen :deep(.office-fallback) {
  flex-shrink: 0;
}

.preview-card--fullscreen :deep(.preview-video) {
  flex: 1;
  min-height: 0;
  display: flex;
  align-items: center;
  background: #000;
}

.preview-card--fullscreen :deep(.preview-video video) {
  width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.preview-card--fullscreen .preview-image-wrap {
  flex: 1;
  min-height: 0;
}

.preview-card--fullscreen .preview-image-wrap img {
  max-height: 100%;
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-title {
  font-size: 16px;
  font-weight: 600;
}
.preview-image-wrap {
  display: flex;
  justify-content: center;
  padding: 16px;
  background: #fafbfc;
}
.preview-image-wrap img {
  max-width: 100%;
  max-height: 560px;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.document-processing {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 400px;
  padding: 48px;
  color: var(--el-text-color-secondary);
  background: #525659;
  border-radius: 8px;
}
</style>




