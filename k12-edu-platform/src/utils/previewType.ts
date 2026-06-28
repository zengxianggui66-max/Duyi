/**
 * 文件预览类型（与后端 FileServiceImpl.FORMAT_CONFIGS 对齐，并区分本地上传页可预览能力）
 */
export type LocalPreviewType = 'image' | 'document' | 'video' | 'audio' | 'docx' | 'none'

export interface FormatPreviewMeta {
  extension: string
  name: string
  isPreviewable: boolean
  /** 后端 previewType：document | video | audio | image */
  serverPreviewType: string | null
  maxSizeMb: number
  /** 上传页是否可本地即时预览 */
  localPreviewType: LocalPreviewType
}

const DOCX_EXT = ['docx'] as const
const DOC_EXT = ['doc'] as const
const PDF_EXT = ['pdf'] as const
const PPT_EXT = ['ppt', 'pptx'] as const
const XLS_EXT = ['xls', 'xlsx'] as const
const VIDEO_EXT = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv', 'webm'] as const
const AUDIO_EXT = ['mp3', 'wav', 'flac', 'aac', 'ogg', 'wma'] as const
const IMAGE_EXT = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'] as const
const ZIP_EXT = ['zip', 'rar', '7z'] as const

function meta(
  extension: string,
  name: string,
  opts: {
    isPreviewable: boolean
    serverPreviewType: string | null
    maxSizeMb: number
    localPreviewType: LocalPreviewType
  },
): FormatPreviewMeta {
  return { extension, name, ...opts }
}

/** 与 Java FORMAT_CONFIGS 一致的静态表 */
const FORMAT_META: Record<string, FormatPreviewMeta> = {}

function register(list: readonly string[], item: Omit<FormatPreviewMeta, 'extension'>) {
  for (const ext of list) {
    FORMAT_META[ext] = { ...item, extension: ext }
  }
}

register(DOCX_EXT, meta('docx', 'Word 文档', {
  isPreviewable: true,
  serverPreviewType: 'document',
  maxSizeMb: 50,
  localPreviewType: 'docx',
}))

register(DOC_EXT, meta('doc', 'Word 文档(旧版)', {
  isPreviewable: true,
  serverPreviewType: 'document',
  maxSizeMb: 50,
  localPreviewType: 'none',
}))

register(PDF_EXT, meta('pdf', '文档', {
  isPreviewable: true,
  serverPreviewType: 'document',
  maxSizeMb: 50,
  localPreviewType: 'document',
}))

register(PPT_EXT, meta('ppt', '演示文稿', {
  isPreviewable: true,
  serverPreviewType: 'document',
  maxSizeMb: 100,
  localPreviewType: 'none',
}))

register(XLS_EXT, meta('xlsx', '表格', {
  isPreviewable: true,
  serverPreviewType: 'document',
  maxSizeMb: 50,
  localPreviewType: 'none',
}))

register(VIDEO_EXT, meta('mp4', '视频', {
  isPreviewable: true,
  serverPreviewType: 'video',
  maxSizeMb: 500,
  localPreviewType: 'video',
}))

register(AUDIO_EXT, meta('mp3', '音频', {
  isPreviewable: true,
  serverPreviewType: 'audio',
  maxSizeMb: 100,
  localPreviewType: 'audio',
}))

register(IMAGE_EXT, meta('jpg', '图片', {
  isPreviewable: true,
  serverPreviewType: 'image',
  maxSizeMb: 20,
  localPreviewType: 'image',
}))

register(ZIP_EXT, meta('zip', '压缩包', {
  isPreviewable: false,
  serverPreviewType: null,
  maxSizeMb: 500,
  localPreviewType: 'none',
}))

export function getFileExtension(filename: string): string {
  const parts = filename.split('.')
  if (parts.length < 2) return ''
  return (parts.pop() || '').toLowerCase()
}

export function getFormatPreviewMeta(filenameOrExt: string): FormatPreviewMeta | null {
  const ext = filenameOrExt.includes('.')
    ? getFileExtension(filenameOrExt)
    : filenameOrExt.toLowerCase().replace(/^\./, '')
  return FORMAT_META[ext] || null
}

export function canLocalPreview(meta: FormatPreviewMeta | null): boolean {
  return !!meta && meta.localPreviewType !== 'none'
}

export function needsServerPreview(meta: FormatPreviewMeta | null): boolean {
  return !!meta && meta.isPreviewable && !canLocalPreview(meta)
}

/** 是否适合用 Office 在线嵌入（Google gview）尝试预览 */
export function isOfficeEmbedFormat(ext: string): boolean {
  return ['doc', 'docx', 'ppt', 'pptx', 'xls', 'xlsx'].includes(ext)
}

export type ServerPreviewKind =
  | LocalPreviewType
  | 'office-embed'
  | 'server-html'
  | 'ppt-viewer'
  | 'none'

/** 根据服务端 previewMode 映射上传页预览组件类型 */
export function mapServerPreviewKind(
  serverType: string | null | undefined,
  ext: string,
  previewMode?: string | null,
): ServerPreviewKind {
  const mode = (previewMode || '').toLowerCase()
  if (mode === 'slides') return 'ppt-viewer'
  if (mode === 'pdf' && ['ppt', 'pptx'].includes(ext)) return 'ppt-viewer'
  if (mode === 'pdf') return 'document'
  if (mode === 'html' && ['ppt', 'pptx'].includes(ext)) return 'ppt-viewer'
  if (mode === 'html') return 'server-html'
  if (mode === 'embed') {
    return isOfficeEmbedFormat(ext) ? 'office-embed' : 'document'
  }
  return mapServerPreviewType(serverType, ext)
}

export function mapServerPreviewType(
  serverType: string | null | undefined,
  ext: string,
): ServerPreviewKind {
  const t = (serverType || '').toLowerCase()
  if (t === 'image') return 'image'
  if (t === 'video') return 'video'
  if (t === 'audio') return 'audio'
  if (t === 'document') {
    if (ext === 'pdf') return 'document'
    if (isOfficeEmbedFormat(ext)) return 'office-embed'
    return 'document'
  }
  return 'none'
}

export function getPreviewStatusLabel(
  meta: FormatPreviewMeta | null,
  opts?: {
    hasLocal?: boolean
    hasServer?: boolean
    serverProvider?: string
    previewMode?: string
  },
): string {
  if (!meta) return '未知格式'
  const local = opts?.hasLocal ?? canLocalPreview(meta)
  const server = opts?.hasServer ?? false
  const provider = opts?.serverProvider || ''
  if (local && server) {
    if (provider === 'libreoffice') return '本地+服务端 PDF 预览'
    if (provider === 'poi') return '本地+服务端 HTML 预览'
    return '本地+云端预览就绪'
  }
  if (local) return '支持本地预览'
  if (server) {
    if (opts?.previewMode === 'slides') return '真实幻灯片预览就绪'
    if (provider === 'libreoffice') return '服务端 PDF 转码预览'
    if (provider === 'poi') return '服务端 HTML 摘要预览'
    return '云端预览就绪'
  }
  if (meta.isPreviewable) return '正在上传云端…'
  return '暂不支持预览'
}

/** @deprecated 使用 getPreviewStatusLabel */
export function getLocalPreviewLabel(meta: FormatPreviewMeta | null): string {
  return getPreviewStatusLabel(meta, { hasLocal: canLocalPreview(meta) })
}
