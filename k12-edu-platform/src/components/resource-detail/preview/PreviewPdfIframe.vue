<template>
  <div class="preview-pdf-iframe" :class="{ 'is-fullscreen': fullscreen }">
    <iframe
      :src="iframeSrc"
      class="pdf-iframe"
      :title="title || 'PDF 文档预览'"
      referrerpolicy="no-referrer"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { resolvePreviewAssetUrl } from '@/utils/filePreview'

const props = defineProps<{
  pdfUrl: string
  fullscreen?: boolean
  title?: string
}>()

const iframeSrc = computed(() => resolvePreviewAssetUrl(props.pdfUrl))
</script>

<style scoped>
.preview-pdf-iframe {
  display: flex;
  flex-direction: column;
  min-height: 600px;
  background: #525659;
  border-radius: 8px;
  overflow: hidden;
}

.preview-pdf-iframe.is-fullscreen {
  flex: 1;
  min-height: 0;
  height: 100%;
  border-radius: 0;
}

.pdf-iframe {
  flex: 1;
  width: 100%;
  min-height: 560px;
  border: none;
  background: #525659;
}

.preview-pdf-iframe.is-fullscreen .pdf-iframe {
  min-height: 0;
  height: 100%;
}
</style>
