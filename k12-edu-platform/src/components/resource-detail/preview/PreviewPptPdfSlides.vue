<template>
  <div class="preview-slides" :class="{ 'is-full': isFullPreview }">
    <div v-if="loading" class="slide-loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>正在加载幻灯片…</span>
    </div>

    <el-alert v-else-if="error" type="warning" :closable="false" show-icon :title="error" />

    <template v-else-if="pageCount > 0">
      <div class="slide-container" :class="{ 'full-preview': isFullPreview }">
        <canvas ref="canvasRef" class="slide-canvas" />
        <button class="slide-nav prev" :disabled="currentSlide <= 0" @click="$emit('prev')">
          <el-icon><ArrowLeft /></el-icon>
        </button>
        <button
          class="slide-nav next"
          :disabled="currentSlide >= pageCount - 1"
          @click="$emit('next')"
        >
          <el-icon><ArrowRight /></el-icon>
        </button>
      </div>
      <div class="slide-indicator">
        <span>第 {{ currentSlide + 1 }} / {{ pageCount }} 页</span>
        <div class="slide-dots">
          <span
            v-for="i in pageCount"
            :key="i"
            class="dot"
            :class="{ active: currentSlide === i - 1 }"
            @click="$emit('goto', i - 1)"
          />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { resolvePreviewAssetUrl } from '@/utils/filePreview'
import { ArrowLeft, ArrowRight, Loading } from '@element-plus/icons-vue'
import * as pdfjsLib from 'pdfjs-dist'
import type { PDFDocumentProxy } from 'pdfjs-dist'

pdfjsLib.GlobalWorkerOptions.workerSrc = new URL(
  'pdfjs-dist/build/pdf.worker.min.mjs',
  import.meta.url,
).toString()

const props = defineProps<{
  pdfUrl: string
  currentSlide: number
  isFullPreview: boolean
}>()

const emit = defineEmits<{
  prev: []
  next: []
  goto: [index: number]
  'pages-loaded': [count: number]
}>()

const canvasRef = ref<HTMLCanvasElement | null>(null)
const loading = ref(true)
const error = ref('')
const pageCount = ref(0)
let pdfDoc: PDFDocumentProxy | null = null
let renderTask: { cancel?: () => void } | null = null

async function loadPdf(url: string) {
  loading.value = true
  error.value = ''
  pageCount.value = 0
  if (pdfDoc) {
    await pdfDoc.destroy().catch(() => {})
    pdfDoc = null
  }

  try {
    const fetchUrl = resolvePreviewAssetUrl(url)
    const task = pdfjsLib.getDocument({ url: fetchUrl, withCredentials: false })
    pdfDoc = await task.promise
    pageCount.value = pdfDoc.numPages
    emit('pages-loaded', pageCount.value)
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'PDF 幻灯片加载失败'
  } finally {
    loading.value = false
  }

  if (pdfDoc && pageCount.value > 0) {
    await nextTick()
    await renderPage(props.currentSlide + 1)
  }
}

async function renderPage(pageNumber: number) {
  if (!pdfDoc || !canvasRef.value) return
  if (renderTask?.cancel) {
    renderTask.cancel()
  }

  const page = await pdfDoc.getPage(pageNumber)
  const canvas = canvasRef.value
  const context = canvas.getContext('2d')
  if (!context) return

  const container = canvas.parentElement
  const containerWidth = container?.clientWidth || 960
  const viewport = page.getViewport({ scale: 1 })
  const scale = Math.min(containerWidth / viewport.width, 1.8)
  const scaled = page.getViewport({ scale })

  canvas.width = scaled.width
  canvas.height = scaled.height

  const render = page.render({ canvasContext: context, viewport: scaled })
  renderTask = render
  await render.promise
}

watch(
  () => props.pdfUrl,
  (url) => {
    if (url) void loadPdf(url)
  },
  { immediate: true },
)

watch(
  () => props.currentSlide,
  (index) => {
    if (pdfDoc && pageCount.value > 0) {
      void renderPage(index + 1)
    }
  },
)

onUnmounted(() => {
  if (pdfDoc) {
    void pdfDoc.destroy()
    pdfDoc = null
  }
})
</script>

<style scoped>
.preview-slides.is-full {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: #0f0f1a;
  display: flex;
  flex-direction: column;
}

.slide-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 80px;
  color: #909399;
}

.slide-container {
  position: relative;
  background: #1a1a2e;
  border-radius: 8px;
  overflow: hidden;
  min-height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: 1;
}

.slide-container.full-preview {
  border-radius: 0;
  min-height: calc(100vh - 56px);
}

.slide-canvas {
  max-width: 100%;
  height: auto;
  display: block;
}

.slide-nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.92);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
}

.slide-nav.prev {
  left: 16px;
}

.slide-nav.next {
  right: 16px;
}

.slide-nav:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.slide-indicator {
  padding: 12px;
  color: #909399;
  font-size: 14px;
  text-align: center;
}

.slide-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 8px;
  flex-wrap: wrap;
  max-height: 48px;
  overflow: auto;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #dcdfe6;
  cursor: pointer;
}

.dot.active {
  background: var(--el-color-primary);
}

.is-full .slide-indicator {
  color: rgba(255, 255, 255, 0.75);
}
</style>
