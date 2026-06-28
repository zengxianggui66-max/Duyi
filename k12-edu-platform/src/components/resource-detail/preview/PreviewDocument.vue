<template>
  <div class="preview-document" :class="{ 'is-fullscreen': fullscreen }">
    <PreviewPdfIframe
      v-if="resolvedUrl && fullscreen"
      :pdf-url="resolvedUrl"
      :fullscreen="fullscreen"
    />
    <PreviewPdfViewer
      v-else-if="resolvedUrl"
      :pdf-url="resolvedUrl"
      :fullscreen="fullscreen"
    />
    <PreviewFallback
      v-else
      :message="fallbackMessage"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { resolvePreviewAssetUrl } from '@/utils/filePreview'
import PreviewPdfIframe from './PreviewPdfIframe.vue'
import PreviewPdfViewer from './PreviewPdfViewer.vue'
import PreviewFallback from './PreviewFallback.vue'

const props = defineProps<{
  pdfUrl: string
  previewMode?: string
  fallbackMessage?: string
  fullscreen?: boolean
}>()

const resolvedUrl = computed(() => {
  if (!props.pdfUrl) return ''
  if (props.previewMode && props.previewMode !== 'pdf') return ''
  return resolvePreviewAssetUrl(props.pdfUrl)
})

const fallbackMessage = computed(
  () =>
    props.fallbackMessage ||
    '文档正在转码或暂无法预览，请稍后刷新或下载原文件查看',
)
</script>

<style scoped>
.preview-document {
  display: flex;
  flex-direction: column;
  min-height: 600px;
}

.preview-document.is-fullscreen {
  flex: 1;
  min-height: 0;
  height: 100%;
}
</style>
