/**
 * 服务端预览信息（阶段 C）
 */
export type PreviewMode = 'native' | 'pdf' | 'html' | 'slides' | 'embed' | 'processing' | 'none'

export interface FilePreviewInfo {
  previewUrl: string
  previewType: string
  previewMode: PreviewMode
  originalUrl: string
  converted: boolean
  provider: string
  message?: string
  slideUrls?: string[]
  slideCount?: number
}

/**
 * 将后端返回的绝对地址（如 http://localhost:8082/uploads/...）转为同源代理路径 /uploads/...
 * 避免前端 fetch/pdf.js 跨域导致 Failed to fetch
 */
export function resolvePreviewAssetUrl(url: string | null | undefined): string {
  if (!url) return ''
  const trimmed = url.trim()
  if (!trimmed) return ''

  if (trimmed.startsWith('/')) return trimmed

  try {
    const parsed = new URL(trimmed)
    if (parsed.pathname.startsWith('/uploads/')) {
      return `${parsed.pathname}${parsed.search}`
    }
  } catch {
    return trimmed
  }

  return trimmed
}

export function normalizePreviewInfo<T extends FilePreviewInfo>(info: T): T {
  return {
    ...info,
    previewUrl: resolvePreviewAssetUrl(info.previewUrl),
    slideUrls: info.slideUrls?.map((u) => resolvePreviewAssetUrl(u)),
  }
}

export function isServerConvertedPreview(info: FilePreviewInfo | null | undefined): boolean {
  if (!info) return false
  return info.converted && (info.previewMode === 'pdf' || info.previewMode === 'html')
}

export function getPreviewProviderLabel(provider: string | undefined): string {
  switch (provider) {
    case 'libreoffice':
      return '服务端 PDF 转码'
    case 'poi':
      return '服务端 HTML 摘要'
    case 'native':
      return '原文件直链'
    default:
      return ''
  }
}
