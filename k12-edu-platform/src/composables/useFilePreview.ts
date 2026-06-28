import { ref, computed } from 'vue'
import { fileApi, type FilePreviewInfo } from '@/api/file'
import { unwrapData } from '@/api/request'
import { normalizePreviewInfo } from '@/utils/filePreview'

const POLL_INTERVAL_MS = 3000
/** 与后端 libreoffice-timeout-seconds(120) 对齐，留余量 */
const MAX_POLL_ATTEMPTS = 50

function sleep(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

function isTerminalPreviewMode(mode: string | undefined): boolean {
  return !!mode && mode !== 'processing'
}

/**
 * 统一文件预览信息加载（阶段 C/D + 异步 PPT 轮询）
 */
export function useFilePreview() {
  const loading = ref(false)
  const processing = ref(false)
  const error = ref('')
  const previewInfo = ref<FilePreviewInfo | null>(null)

  const slideCount = computed(() => {
    const info = previewInfo.value
    if (!info) return 0
    if (info.slideCount && info.slideCount > 0) return info.slideCount
    if (info.slideUrls?.length) return info.slideUrls.length
    return 0
  })

  const isPptSlides = computed(() => previewInfo.value?.previewMode === 'slides')
  const isPptPdf = computed(
    () =>
      previewInfo.value?.previewMode === 'pdf' &&
      !!previewInfo.value?.previewUrl,
  )
  const isPptHtml = computed(() => previewInfo.value?.previewMode === 'html')
  const isProcessing = computed(
    () => processing.value || previewInfo.value?.previewMode === 'processing',
  )

  async function fetchPreviewOnce(fileUrl: string) {
    const res = await fileApi.getPreviewInfo(fileUrl, {
      timeout: 20000,
      silentError: true,
    })
    const raw = unwrapData(res)
    return raw ? normalizePreviewInfo(raw) : null
  }

  async function loadPreview(fileUrl: string | null | undefined) {
    previewInfo.value = null
    error.value = ''
    processing.value = false
    if (!fileUrl) return

    loading.value = true
    try {
      let info = await fetchPreviewOnce(fileUrl)
      if (!info) {
        error.value = '预览加载失败'
        return
      }

      previewInfo.value = info

      if (info.previewMode === 'processing') {
        processing.value = true
        for (let attempt = 0; attempt < MAX_POLL_ATTEMPTS; attempt++) {
          await sleep(POLL_INTERVAL_MS)
          info = await fetchPreviewOnce(fileUrl)
          if (!info) continue
          previewInfo.value = info
          if (isTerminalPreviewMode(info.previewMode)) {
            processing.value = false
            if (info.previewMode === 'none') {
              error.value = info.message || '文档转码失败，请下载原文件查看'
            }
            return
          }
        }
        processing.value = false
        error.value = '文档转码超时，请稍后刷新或下载原文件查看'
      }
    } catch (e: unknown) {
      error.value = e instanceof Error ? e.message : '预览加载失败'
    } finally {
      loading.value = false
      processing.value = false
    }
  }

  function reset() {
    previewInfo.value = null
    error.value = ''
    loading.value = false
    processing.value = false
  }

  return {
    loading,
    processing,
    error,
    previewInfo,
    slideCount,
    isPptSlides,
    isPptPdf,
    isPptHtml,
    isProcessing,
    loadPreview,
    reset,
  }
}
