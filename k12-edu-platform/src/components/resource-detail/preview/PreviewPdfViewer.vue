<template>
  <div class="preview-pdf-viewer" :class="{ 'is-fullscreen': fullscreen }">
    <div v-if="loading" class="pdf-loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>正在加载文档…</span>
    </div>

    <el-alert v-else-if="error" type="warning" :closable="false" show-icon :title="error" />

    <template v-else-if="pageCount > 0">
      <div class="pdf-toolbar">
        <button type="button" class="pdf-tool-btn" :disabled="currentPage <= 0" @click="goPrev">
          <el-icon><ArrowLeft /></el-icon>
        </button>
        <span class="pdf-page-label">第 {{ currentPage + 1 }} / {{ pageCount }} 页</span>
        <button
          type="button"
          class="pdf-tool-btn"
          :disabled="currentPage >= pageCount - 1"
          @click="goNext"
        >
          <el-icon><ArrowRight /></el-icon>
        </button>
        <span class="pdf-zoom-label">{{ zoomPercent }}%</span>
        <button type="button" class="pdf-tool-btn" :disabled="zoomPercent <= 50" @click="zoomOut">−</button>
        <button type="button" class="pdf-tool-btn" :disabled="zoomPercent >= 200" @click="zoomIn">+</button>
      </div>

      <div ref="containerRef" class="pdf-canvas-wrap">
        <canvas ref="canvasRef" class="pdf-canvas" />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onUnmounted } from 'vue'
import { ArrowLeft, ArrowRight, Loading } from '@element-plus/icons-vue'
import { resolvePreviewAssetUrl } from '@/utils/filePreview'
import * as pdfjsLib from 'pdfjs-dist'
import type { PDFDocumentProxy } from 'pdfjs-dist'

pdfjsLib.GlobalWorkerOptions.workerSrc = new URL(
  'pdfjs-dist/build/pdf.worker.min.mjs',
  import.meta.url,
).toString()

const props = defineProps<{
  pdfUrl: string
  fullscreen?: boolean
}>()

const containerRef = ref<HTMLElement | null>(null)
const canvasRef = ref<HTMLCanvasElement | null>(null)
const loading = ref(true)
const error = ref('')
const pageCount = ref(0)
const currentPage = ref(0)
const zoomScale = ref(1)

const zoomPercent = computed(() => Math.round(zoomScale.value * 100))

let pdfDoc: PDFDocumentProxy | null = null
let renderTask: { cancel?: () => void } | null = null
let resizeObserver: ResizeObserver | null = null

async function loadPdf(url: string) {
  loading.value = true
  error.value = ''
  pageCount.value = 0
  currentPage.value = 0
  zoomScale.value = 1

  if (pdfDoc) {
    await pdfDoc.destroy().catch(() => {})
    pdfDoc = null
  }

  try {
    const fetchUrl = resolvePreviewAssetUrl(url)
    pdfDoc = await pdfjsLib.getDocument({ url: fetchUrl, withCredentials: false }).promise
    pageCount.value = pdfDoc.numPages
  } catch (e: unknown) {
    error.value = e instanceof Error ? e.message : 'PDF 加载失败'
  } finally {
    loading.value = false
  }

  if (pdfDoc && pageCount.value > 0) {
    await nextTick()
    await renderCurrentPage()
  }
}

async function renderCurrentPage() {
  if (!pdfDoc || !canvasRef.value) return
  if (renderTask?.cancel) renderTask.cancel()

  const page = await pdfDoc.getPage(currentPage.value + 1)
  const canvas = canvasRef.value
  const context = canvas.getContext('2d')
  if (!context) return

  const container = canvas.parentElement
  const containerWidth = container?.clientWidth || containerRef.value?.clientWidth || 960
  const baseViewport = page.getViewport({ scale: 1 })
  const fitScale = Math.min(containerWidth / baseViewport.width, 1.8)
  const scaled = page.getViewport({ scale: fitScale * zoomScale.value })

  canvas.width = scaled.width
  canvas.height = scaled.height

  const render = page.render({ canvasContext: context, viewport: scaled })
  renderTask = render
  await render.promise
}

function goPrev() {
  if (currentPage.value > 0) {
    currentPage.value -= 1
    void renderCurrentPage()
  }
}

function goNext() {
  if (currentPage.value < pageCount.value - 1) {
    currentPage.value += 1
    void renderCurrentPage()
  }
}

function zoomIn() {
  zoomScale.value = Math.min(2, zoomScale.value + 0.1)
  void renderCurrentPage()
}

function zoomOut() {
  zoomScale.value = Math.max(0.5, zoomScale.value - 0.1)
  void renderCurrentPage()
}

watch(
  () => props.pdfUrl,
  (url) => {
    if (url) void loadPdf(url)
  },
  { immediate: true },
)

watch(
  containerRef,
  (el) => {
    resizeObserver?.disconnect()
    resizeObserver = null
    if (typeof ResizeObserver === 'undefined' || !el) return
    resizeObserver = new ResizeObserver(() => {
      if (pdfDoc && pageCount.value > 0 && !loading.value) void renderCurrentPage()
    })
    resizeObserver.observe(el)
  },
  { immediate: true },
)

onUnmounted(() => {
  resizeObserver?.disconnect()
  if (pdfDoc) {
    void pdfDoc.destroy()
    pdfDoc = null
  }
})
</script>

<style scoped>
.preview-pdf-viewer {
  display: flex;
  flex-direction: column;
  min-height: 600px;
  background: #525659;
  border-radius: 8px;
  overflow: hidden;
}

.preview-pdf-viewer.is-fullscreen {
  flex: 1;
  min-height: 0;
  height: 100%;
  border-radius: 0;
}

.pdf-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 80px;
  color: rgba(255, 255, 255, 0.75);
}

.pdf-toolbar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 8px 12px;
  background: #323639;
  color: rgba(255, 255, 255, 0.9);
  flex-shrink: 0;
}

.pdf-page-label,
.pdf-zoom-label {
  font-size: 13px;
  min-width: 88px;
  text-align: center;
}

.pdf-tool-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.pdf-tool-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.22);
}

.pdf-tool-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.pdf-canvas-wrap {
  flex: 1;
  min-height: 360px;
  overflow: auto;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 16px;
  background: #525659;
}

.preview-pdf-viewer.is-fullscreen .pdf-canvas-wrap {
  padding: 12px;
}

.pdf-canvas {
  display: block;
  max-width: 100%;
  height: auto;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.35);
  background: #fff;
}
</style>
