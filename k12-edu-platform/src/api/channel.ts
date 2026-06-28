import { request, unwrapData, type ApiResult } from '@/api/request'

export interface ChannelBootstrapPayload {
  code: string
  name: string
  icon?: string
  desc?: string
  bgColor?: string
  stats?: { total?: string; elite?: string; free?: string }
  showGradeFilter?: boolean
  showSubjectFilter?: boolean
  eliteTitle?: string
  eliteDesc?: string
  mainTabs?: { key: string; name: string; icon?: string }[]
  eliteAlbums?: {
    id?: number
    title: string
    icon?: string
    meta?: string
    resourceCount?: number
    downloadCount?: number
    coverColor?: string
    linkPath?: string
  }[]
  tabKeywords?: Record<string, string>
}

export async function fetchChannelBootstrap(code: string) {
  const res = await request.get<ApiResult<ChannelBootstrapPayload>>(`/api/channel/${code}`)
  return unwrapData(res)
}
