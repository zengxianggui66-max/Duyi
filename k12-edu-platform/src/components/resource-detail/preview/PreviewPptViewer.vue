<template>
  <div class="preview-ppt-viewer">
    <div v-if="loading || isProcessing" class="viewer-loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>{{ processingMessage }}</span>
    </div>

    <el-alert
      v-else-if="error"
      type="warning"
      :closable="false"
      show-icon
      :title="error"
    />

    <el-alert
      v-else-if="hint"
      type="info"
      :closable="false"
      show-icon
      class="viewer-hint"
      :title="hint"
    />

    <PreviewPptImageSlides
      v-if="mode === 'slides' && slideUrls.length"
      :title="title"
      :slide-urls="slideUrls"
      :current-slide="currentSlide"
      :is-full-preview="isFullPreview"
      @prev="prevSlide"
      @next="nextSlide"
      @goto="gotoSlide"
    />

    <PreviewPptPdfSlides
      v-else-if="mode === 'pdf' && pdfUrl"
      :pdf-url="pdfUrl"
      :current-slide="currentSlide"
      :is-full-preview="isFullPreview"
      @prev="prevSlide"
      @next="nextSlide"
      @goto="gotoSlide"
      @pages-loaded="onPagesLoaded"
    />

    <PreviewPptHtmlSlides
      v-else-if="mode === 'html' && htmlUrl"
      :title="title"
      :html-url="htmlUrl"
      :current-slide="currentSlide"
      :is-full-preview="isFullPreview"
      @prev="prevSlide"
      @next="nextSlide"
      @goto="gotoSlide"
      @pages-loaded="onPagesLoaded"
    />

    <div v-else-if="mode === 'embed' && embedUrl" class="preview-office">
      <iframe :src="embedUrl" class="office-iframe" :title="title" />
      <div class="office-fallback">
        <p>{{ hint || '正在通过在线文档服务加载预览' }}</p>
        <span class="office-download-tip">如需原文件，请使用页面上的下载按钮</span>
      </div>
    </div>

    <PreviewFallback v-else :message="fallbackMessage" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import type { FilePreviewInfo } from '@/api/file'
import { buildOfficeEmbedUrl } from '@/utils/officePreview'
import PreviewPptImageSlides from './PreviewPptImageSlides.vue'
import PreviewPptPdfSlides from './PreviewPptPdfSlides.vue'
import PreviewPptHtmlSlides from './PreviewPptHtmlSlides.vue'
import PreviewFallback from './PreviewFallback.vue'

const props = defineProps<{
  title: string
  previewInfo: FilePreviewInfo | null
  loading?: boolean
  processing?: boolean
  error?: string
  originalUrl?: string
  isFullPreview: boolean
  currentSlide: number
}>()

const emit = defineEmits<{
  'prev-slide': []
  'next-slide': []
  'goto-slide': [index: number]
  'slide-count': [count: number]
}>()

const internalSlideCount = ref(0)

const mode = computed(() => {
  const m = props.previewInfo?.previewMode
  if (m === 'slides' && slideUrls.value.length) return 'slides'
  if (m === 'pdf' && pdfUrl.value) return 'pdf'
  if (m === 'html' && htmlUrl.value) return 'html'
  if (m === 'embed') return 'embed'
  return 'none'
})

const slideUrls = computed(() => props.previewInfo?.slideUrls?.filter(Boolean) ?? [])
const pdfUrl = computed(() =>
  props.previewInfo?.previewMode === 'pdf' ? props.previewInfo?.previewUrl || '' : '',
)
const htmlUrl = computed(() =>
  props.previewInfo?.previewMode === 'html' ? props.previewInfo?.previewUrl || '' : '',
)
const embedUrl = computed(() => {
  const url = props.previewInfo?.originalUrl || props.originalUrl || ''
  return buildOfficeEmbedUrl(url, 'ppt')
})
const hint = computed(() => props.previewInfo?.message || '')
const isProcessing = computed(
  () => props.processing || props.previewInfo?.previewMode === 'processing',
)
const processingMessage = computed(() => {
  if (props.previewInfo?.previewMode === 'processing' && props.previewInfo.message) {
    return props.previewInfo.message
  }
  return isProcessing.value
    ? '正在转码幻灯片，请稍候…（首次预览约需 1–2 分钟）'
    : '正在加载 PPT 预览…'
})
const fallbackMessage = computed(
  () => props.error || (props.previewInfo?.previewMode === 'html' ? '当前仅生成了 HTML 摘要，无法代表 PPT 原始版式。请安装/启用 LibreOffice 转码后查看真实幻灯片。' : props.previewInfo?.message) || '暂无法预览该演示文稿，请下载原文件查看',
)

watch(
  () => props.previewInfo?.slideCount,
  (count) => {
    if (count && count > 0) {
      internalSlideCount.value = count
      emit('slide-count', count)
    }
  },
  { immediate: true },
)

watch(
  () => slideUrls.value.length,
  (len) => {
    if (len > 0) {
      internalSlideCount.value = len
      emit('slide-count', len)
    }
  },
  { immediate: true },
)

function onPagesLoaded(count: number) {
  internalSlideCount.value = count
  emit('slide-count', count)
}

function prevSlide() {
  emit('prev-slide')
}

function nextSlide() {
  emit('next-slide')
}

function gotoSlide(index: number) {
  emit('goto-slide', index)
}


</script>

<style scoped>
.viewer-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 64px;
  color: #909399;
}

.viewer-hint {
  margin-bottom: 12px;
}

.preview-office .office-iframe {
  width: 100%;
  height: 560px;
  min-height: 480px;
  border: none;
  display: block;
}

.office-fallback {
  padding: 12px 16px;
  font-size: 13px;
  color: #606266;
  border-top: 1px solid #ebeef5;
}

.office-download-tip {
  display: inline-block;
  margin-top: 6px;
  color: #909399;
}
</style>




