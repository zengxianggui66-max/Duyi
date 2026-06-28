/** 资源文件格式展示工具 */

const EXT_ICON: Record<string, { icon: string; label: string; className: string }> = {
  ppt: { icon: '📊', label: 'PPT', className: 'format-ppt' },
  pptx: { icon: '📊', label: 'PPT', className: 'format-ppt' },
  pdf: { icon: '📕', label: 'PDF', className: 'format-pdf' },
  doc: { icon: '📄', label: 'Word', className: 'format-word' },
  docx: { icon: '📄', label: 'Word', className: 'format-word' },
  xls: { icon: '📗', label: 'Excel', className: 'format-excel' },
  xlsx: { icon: '📗', label: 'Excel', className: 'format-excel' },
  zip: { icon: '📦', label: 'ZIP', className: 'format-zip' },
  rar: { icon: '📦', label: 'RAR', className: 'format-zip' },
  mp4: { icon: '🎬', label: '视频', className: 'format-video' },
  mp3: { icon: '🎧', label: '音频', className: 'format-audio' },
}

const DEFAULT_FORMAT = { icon: '📎', label: '文件', className: 'format-default' }

export function getFileFormatInfo(ext?: string) {
  if (!ext) return DEFAULT_FORMAT
  return EXT_ICON[ext.toLowerCase().replace(/^\./, '')] || DEFAULT_FORMAT
}

export function inferFormatFromType(type?: string, ext?: string): string {
  if (ext) return ext
  if (!type) return ''
  if (type.includes('课件')) return 'ppt'
  if (type.includes('试卷')) return 'pdf'
  return 'doc'
}
