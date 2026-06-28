export type OfficeEmbedKind = 'ppt' | 'doc' | 'auto'

function detectOfficeKind(fileUrl: string): OfficeEmbedKind {
  if (/\.pptx?(\?|$)/i.test(fileUrl)) return 'ppt'
  if (/\.docx?(\?|$)/i.test(fileUrl)) return 'doc'
  return 'auto'
}

/**
 * PPT 优先走 Microsoft Office Online（避免 Google gview 中文页码工具栏错位）
 */
export function buildPptEmbedUrl(fileUrl: string): string | null {
  if (!fileUrl || !/^https?:\/\//i.test(fileUrl)) return null
  return `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(fileUrl)}`
}

/**
 * Office 类文件在线嵌入预览（依赖公网可访问的文件 URL）
 */
export function buildOfficeEmbedUrl(
  fileUrl: string,
  kind: OfficeEmbedKind = 'auto',
): string | null {
  if (!fileUrl || !/^https?:\/\//i.test(fileUrl)) return null
  const resolved = kind === 'auto' ? detectOfficeKind(fileUrl) : kind
  if (resolved === 'ppt') {
    return buildPptEmbedUrl(fileUrl)
  }
  return `https://docs.google.com/gview?url=${encodeURIComponent(fileUrl)}&embedded=true`
}
