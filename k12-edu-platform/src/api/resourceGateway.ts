import { request } from './request'
import type { ApiResult, PageData, SilentRequestConfig } from './request'
import { topicApi, type TopicPageResult, type TopicQueryParams, type TopicResourceItem } from './topic'
import { cultureStudyApi, type CulturePageResult, type CultureQueryParams, type CultureResourceItem } from './cultureStudy'
import { primaryChineseApi } from './primaryChinese'
import {
  competitionApi,
  type CompetitionPageResult,
  type CompetitionQueryParams,
  type CompetitionResourceItem,
} from './competition'
import { browseApi, type BrowseStatsResult, type BrowseModuleStat } from './browse'
import type { PrimaryChineseItem, PrimaryChineseParams, ResourceSuite } from './types'

type SourceType = 'primary_chinese' | 'topic_resource' | 'culture_resource' | 'competition_resource'
type GatewaySource = 'legacy' | 'unified'

interface RuntimeFeatureFlags {
  resourceUnifiedReadEnabled?: boolean
  topicUnifiedReadEnabled?: boolean
  cultureUnifiedReadEnabled?: boolean
  competitionUnifiedReadEnabled?: boolean
  primaryChineseUnifiedReadEnabled?: boolean
}

interface UnifiedResourceItem {
  globalId: number
  sourceType: SourceType
  sourceId: number
  title?: string
  stage?: string
  subject?: string
  module?: string
  type?: string
  fileExt?: string
  ossUrl?: string
  downloadCount?: number
  viewCount?: number
  uploadTime?: string
  isFree?: number
  catalogNodeId?: number
  catalogPath?: string
  subType?: string
  gradeName?: string
  edition?: string
  brandCode?: string
  unitName?: string
  lessonName?: string
  fileSizeKb?: number
  allowPreview?: number
  previewStatus?: number
  fileSafetyStatus?: number
  auditStatus?: number
  publishStatus?: number
  legacyStatus?: number
}

interface UnifiedPageResult {
  records: UnifiedResourceItem[]
  total: number
  current: number
  size: number
  pages?: number
}

export interface UnifiedResourceDetail {
  globalId: number
  sourceType: SourceType
  sourceId: number
  title?: string
  stage?: string
  subject?: string
  module?: string
  type?: string
  fileExt?: string
  ossUrl?: string
  remark?: string
  downloadCount?: number
  viewCount?: number
  isFree?: number
  catalogNodeId?: number
  catalogPath?: string
  subType?: string
  gradeName?: string
  edition?: string
  brandCode?: string
  unitName?: string
  lessonName?: string
  fileSizeKb?: number
  allowPreview?: number
  previewStatus?: number
  fileSafetyStatus?: number
  auditStatus?: number
  publishStatus?: number
  legacyStatus?: number
}

interface UnifiedQuery {
  sourceType: SourceType
  stage?: string
  subject?: string
  module?: string
  type?: string
  keyword?: string
  subType?: string
  gradeName?: string
  edition?: string
  brandCode?: string
  unitName?: string
  lessonName?: string
  fileExt?: string
  catalogNodeId?: number
  includeSubtree?: boolean
  current?: number
  size?: number
  sortField?: string
  sortOrder?: string
}

function cleanAll(value?: string): string | undefined {
  const text = value?.trim()
  if (!text || text === '全部' || text === 'all') {
    return undefined
  }
  return text
}

const silentRead: SilentRequestConfig = { silentError: true }
const featureFlagCache = {
  value: null as RuntimeFeatureFlags | null,
  expireAt: 0,
}
const inFlightControllers = new Map<string, AbortController>()

function beginRequest(channel: string): AbortController {
  const previous = inFlightControllers.get(channel)
  if (previous) {
    previous.abort()
  }
  const controller = new AbortController()
  inFlightControllers.set(channel, controller)
  return controller
}

function finishRequest(channel: string, controller: AbortController) {
  if (inFlightControllers.get(channel) === controller) {
    inFlightControllers.delete(channel)
  }
}

async function getRuntimeFeatureFlags(): Promise<RuntimeFeatureFlags> {
  const now = Date.now()
  if (featureFlagCache.value && featureFlagCache.expireAt > now) {
    return featureFlagCache.value
  }
  const res = await request.get<ApiResult<RuntimeFeatureFlags>>('/public/feature-flags', silentRead)
  const flags = res.data.data || {}
  featureFlagCache.value = flags
  featureFlagCache.expireAt = now + 10_000
  return flags
}

function canUseUnified(flags: RuntimeFeatureFlags, sourceType: SourceType): boolean {
  if (!flags.resourceUnifiedReadEnabled) {
    return false
  }
  switch (sourceType) {
    case 'topic_resource':
      return !!flags.topicUnifiedReadEnabled
    case 'culture_resource':
      return !!flags.cultureUnifiedReadEnabled
    case 'competition_resource':
      return !!flags.competitionUnifiedReadEnabled
    case 'primary_chinese':
      return !!flags.primaryChineseUnifiedReadEnabled
    default:
      return false
  }
}

async function listUnified(query: UnifiedQuery, signal: AbortSignal): Promise<UnifiedPageResult> {
  const res = await request.get<ApiResult<UnifiedPageResult>>('/resources/page', {
    params: query,
    signal,
    ...silentRead,
  })
  return res.data.data || { records: [], total: 0, current: query.current || 1, size: query.size || 12, pages: 0 }
}

async function resolveGlobalId(sourceType: SourceType, sourceId: number): Promise<number> {
  const res = await request.get<ApiResult<number>>('/resources/resolve-global-id', {
    params: { sourceType, sourceId },
    ...silentRead,
  })
  return res.data.data
}

async function getUnifiedDetail(globalId: number): Promise<UnifiedResourceDetail> {
  const res = await request.get<ApiResult<UnifiedResourceDetail>>(`/resources/detail/${globalId}`, silentRead)
  return res.data.data
}

async function recordUnifiedView(globalId: number): Promise<void> {
  await request.post<ApiResult<void>>(`/resources/${globalId}/view`, null, silentRead)
}

async function recordUnifiedDownload(globalId: number): Promise<{ downloadUrl?: string }> {
  const res = await request.post<ApiResult<{ downloadUrl?: string }>>(`/resources/${globalId}/download`, null, silentRead)
  return res.data.data || {}
}

function mapDetailToCultureItem(detail: UnifiedResourceDetail): CultureResourceItem {
  const kind = detail.type === 'external' ? 'external' : 'platform'
  return {
    id: detail.sourceId,
    title: detail.title || '',
    summary: detail.remark || '',
    category: detail.module || 'all',
    region: 'all',
    durationType: 'all',
    resourceKind: kind,
    fileFormat: detail.fileExt || '',
    fileUrl: kind === 'platform' ? (detail.ossUrl || '') : undefined,
    externalUrl: kind === 'external' ? (detail.ossUrl || '') : undefined,
    downloadCount: detail.downloadCount || 0,
    viewCount: detail.viewCount || 0,
    isFree: detail.isFree ?? 1,
    isElite: 0,
  }
}

function mapToTopicItem(record: UnifiedResourceItem): TopicResourceItem {
  return {
    id: record.sourceId,
    title: record.title || '',
    summary: '',
    category: record.module || 'all',
    region: 'all',
    gradeStage: record.stage || 'all',
    subject: record.subject || '',
    resourceForm: record.type || 'material',
    fileFormat: record.fileExt || '',
    fileUrl: record.ossUrl || '',
    downloadCount: record.downloadCount || 0,
    viewCount: record.viewCount || 0,
    isFree: record.isFree ?? 1,
    isElite: 0,
  }
}

function mapToCompetitionItem(record: UnifiedResourceItem): CompetitionResourceItem {
  return {
    id: record.sourceId,
    title: record.title || '',
    summary: '',
    category: record.module || 'all',
    gradeStage: record.stage || 'all',
    subject: record.subject || '',
    resourceForm: record.type || 'document',
    fileFormat: record.fileExt || '',
    fileUrl: record.ossUrl || '',
    downloadCount: record.downloadCount || 0,
    viewCount: record.viewCount || 0,
    isFree: record.isFree ?? 1,
    isElite: 0,
  }
}

function mapToCultureItem(record: UnifiedResourceItem): CultureResourceItem {
  const kind = record.type === 'external' ? 'external' : 'platform'
  return {
    id: record.sourceId,
    title: record.title || '',
    summary: '',
    category: record.module || 'all',
    region: 'all',
    durationType: 'all',
    resourceKind: kind,
    fileFormat: record.fileExt || '',
    fileUrl: kind === 'platform' ? (record.ossUrl || '') : undefined,
    externalUrl: kind === 'external' ? (record.ossUrl || '') : undefined,
    downloadCount: record.downloadCount || 0,
    viewCount: record.viewCount || 0,
    isFree: record.isFree ?? 1,
    isElite: 0,
  }
}

function mapToPrimaryChineseItem(record: UnifiedResourceItem): PrimaryChineseItem {
  return {
    id: record.sourceId,
    title: record.title || '',
    stage: record.stage,
    subject: record.subject,
    module: record.module,
    type: record.type,
    subType: record.subType,
    gradeName: record.gradeName,
    edition: record.edition,
    brandCode: record.brandCode,
    unitName: record.unitName,
    lessonName: record.lessonName,
    catalogNodeId: record.catalogNodeId,
    fileExt: record.fileExt,
    ossUrl: record.ossUrl,
    fileSizeKb: record.fileSizeKb,
    downloadCount: record.downloadCount || 0,
    viewCount: record.viewCount || 0,
    uploadTime: record.uploadTime,
    isFree: record.isFree ?? 1,
    allowPreview: record.allowPreview,
    previewStatus: record.previewStatus,
    fileSafetyStatus: record.fileSafetyStatus,
    auditStatus: record.auditStatus,
    publishStatus: record.publishStatus,
    status: record.legacyStatus,
  }
}

function mapDetailToPrimaryChineseItem(detail: UnifiedResourceDetail): PrimaryChineseItem {
  return {
    ...mapToPrimaryChineseItem(detail),
    remark: detail.remark,
  }
}

function mapDetailToTopicItem(detail: UnifiedResourceDetail): TopicResourceItem {
  return {
    id: detail.sourceId,
    title: detail.title || '',
    summary: detail.remark || '',
    category: detail.module || 'all',
    region: 'all',
    gradeStage: detail.stage || 'all',
    subject: detail.subject || '',
    resourceForm: detail.type || 'material',
    fileFormat: detail.fileExt || '',
    fileUrl: detail.ossUrl || '',
    downloadCount: detail.downloadCount || 0,
    viewCount: detail.viewCount || 0,
    isFree: detail.isFree ?? 1,
    isElite: 0,
  }
}

function mapDetailToCompetitionItem(detail: UnifiedResourceDetail): CompetitionResourceItem {
  return {
    id: detail.sourceId,
    title: detail.title || '',
    summary: detail.remark || '',
    category: detail.module || 'all',
    gradeStage: detail.stage || 'all',
    subject: detail.subject || '',
    resourceForm: detail.type || 'document',
    fileFormat: detail.fileExt || '',
    fileUrl: detail.ossUrl || '',
    downloadCount: detail.downloadCount || 0,
    viewCount: detail.viewCount || 0,
    isFree: detail.isFree ?? 1,
    isElite: 0,
  }
}

function toPrimaryChineseBrowseParams(params: PrimaryChineseParams = {}): Record<string, unknown> {
  return {
    sourceType: 'primary_chinese',
    stage: cleanAll(params.stage),
    subject: cleanAll(params.subject),
    module: cleanAll(params.module),
    type: cleanAll(params.type),
    subType: cleanAll(params.subType),
    gradeName: cleanAll(params.gradeName),
    edition: cleanAll(params.edition),
    brandCode: cleanAll(params.brandCode),
    unitName: cleanAll(params.unitName),
    lessonName: cleanAll(params.lessonName),
    fileExt: cleanAll(params.fileExt),
    catalogNodeId: params.catalogNodeId,
    includeSubtree: params.includeSubtree,
    keyword: params.keyword,
    displayType: params.displayType,
    schemeId: params.schemeId,
    sortField: params.sortField,
    sortOrder: params.sortOrder,
  }
}

function shouldUseBrowseApi(params: PrimaryChineseParams): boolean {
  return typeof params.catalogNodeId === 'number' && params.catalogNodeId > 0
}

function toPrimaryUnifiedQuery(params: PrimaryChineseParams = {}): UnifiedQuery {
  return {
    sourceType: 'primary_chinese',
    stage: cleanAll(params.stage),
    subject: cleanAll(params.subject),
    module: cleanAll(params.module),
    type: cleanAll(params.type),
    subType: cleanAll(params.subType),
    gradeName: cleanAll(params.gradeName),
    edition: cleanAll(params.edition),
    brandCode: cleanAll(params.brandCode),
    unitName: cleanAll(params.unitName),
    lessonName: cleanAll(params.lessonName),
    fileExt: cleanAll(params.fileExt),
    catalogNodeId: params.catalogNodeId,
    includeSubtree: params.includeSubtree,
    keyword: params.keyword,
    current: params.current,
    size: params.size,
    sortField: params.sortField,
    sortOrder: params.sortOrder,
  }
}

export const resourceGateway = {
  async listPrimaryChineseResources(
    params: PrimaryChineseParams = {},
    config?: SilentRequestConfig
  ): Promise<{ source: GatewaySource; page: PageData<PrimaryChineseItem> }> {
    const controller = beginRequest('primary_chinese')
    try {
      const flags = await getRuntimeFeatureFlags()
      if (canUseUnified(flags, 'primary_chinese')) {
        const data = await listUnified(toPrimaryUnifiedQuery(params), controller.signal)
        return {
          source: 'unified',
          page: {
            records: (data.records || []).map(mapToPrimaryChineseItem),
            total: data.total || 0,
            current: data.current || params.current || 1,
            size: data.size || params.size || 10,
            pages: data.pages || 0,
          },
        }
      }
      const legacy = shouldUseBrowseApi(params)
        ? await browseApi.getPage(params, config)
        : await primaryChineseApi.getPage(params, config)
      return { source: 'legacy', page: legacy.data.data }
    } finally {
      finishRequest('primary_chinese', controller)
    }
  },

  async listPrimaryChineseAll(
    params: PrimaryChineseParams = {},
    config?: SilentRequestConfig
  ): Promise<{ source: GatewaySource; records: PrimaryChineseItem[] }> {
    const pageParams = { ...params, current: params.current || 1, size: params.size || 500 }
    const result = await this.listPrimaryChineseResources(pageParams, config)
    return { source: result.source, records: result.page.records || [] }
  },

  async getPrimaryChineseStats(
    params: Omit<PrimaryChineseParams, 'current' | 'size'> = {},
    config?: SilentRequestConfig
  ): Promise<{ source: GatewaySource; stats: BrowseStatsResult }> {
    const controller = beginRequest('primary_chinese_stats')
    try {
      const flags = await getRuntimeFeatureFlags()
      if (canUseUnified(flags, 'primary_chinese')) {
        const query = toPrimaryUnifiedQuery(params)
        delete query.type
        const [statsRes, typesRes] = await Promise.all([
          request.get<ApiResult<{ total?: number }>>('/resources/stats', {
            params: query,
            signal: controller.signal,
            ...silentRead,
          }),
          request.get<ApiResult<Array<{ type?: string; count?: number }>>>('/resources/types', {
            params: query,
            signal: controller.signal,
            ...silentRead,
          }),
        ])
        const types = (typesRes.data.data || []).map((item) => ({
          type: item.type || '其他',
          displayType: item.type || '其他',
          count: item.count || 0,
        }))
        const total = statsRes.data.data?.total || 0
        return {
          source: 'unified',
          stats: {
            types,
            total,
            typeSum: types.reduce((sum, item) => sum + item.count, 0),
          },
        }
      }
      const legacy = await browseApi.getStats(params, config)
      return { source: 'legacy', stats: legacy.data.data }
    } finally {
      finishRequest('primary_chinese_stats', controller)
    }
  },

  async getPrimaryChineseDetail(sourceId: number): Promise<PrimaryChineseItem> {
    const flags = await getRuntimeFeatureFlags()
    if (canUseUnified(flags, 'primary_chinese')) {
      const globalId = await resolveGlobalId('primary_chinese', sourceId)
      const detail = await getUnifiedDetail(globalId)
      return mapDetailToPrimaryChineseItem(detail)
    }
    const legacy = await primaryChineseApi.getDetail(sourceId)
    return legacy.data.data
  },

  async getPrimaryChineseSuites(
    params: Omit<PrimaryChineseParams, 'current' | 'size'> = {},
    config?: SilentRequestConfig
  ): Promise<{ source: GatewaySource; suites: ResourceSuite[] }> {
    const flags = await getRuntimeFeatureFlags()
    if (canUseUnified(flags, 'primary_chinese')) {
      const res = await request.get<ApiResult<ResourceSuite[]>>('/resources/suites', {
        params: toPrimaryChineseBrowseParams(params),
        ...silentRead,
        ...config,
      })
      return { source: 'unified', suites: res.data.data || [] }
    }
    const legacy = shouldUseBrowseApi(params)
      ? await browseApi.getSuites(params, config)
      : await primaryChineseApi.getSuites(params, config)
    return { source: 'legacy', suites: legacy.data.data || [] }
  },

  async getPrimaryChineseModuleStats(
    params: Omit<PrimaryChineseParams, 'current' | 'size'> = {},
    config?: SilentRequestConfig
  ): Promise<{ source: GatewaySource; modules: BrowseModuleStat[] }> {
    const flags = await getRuntimeFeatureFlags()
    if (canUseUnified(flags, 'primary_chinese')) {
      const res = await request.get<ApiResult<BrowseModuleStat[]>>('/resources/module-stats', {
        params: toPrimaryChineseBrowseParams(params),
        ...silentRead,
        ...config,
      })
      return { source: 'unified', modules: res.data.data || [] }
    }
    const legacy = await browseApi.getModuleStats(params, config)
    return { source: 'legacy', modules: legacy.data.data || [] }
  },

  async getTopicResourceDetail(
    sourceId: number
  ): Promise<{ item: TopicResourceItem; globalId: number | null }> {
    const flags = await getRuntimeFeatureFlags()
    if (canUseUnified(flags, 'topic_resource')) {
      const globalId = await resolveGlobalId('topic_resource', sourceId)
      const detail = await getUnifiedDetail(globalId)
      return { item: mapDetailToTopicItem(detail), globalId }
    }
    const res = await topicApi.getResource(sourceId)
    const item = res.data.data
    if (!item) {
      throw new Error('资源不存在或已下架')
    }
    return { item, globalId: null }
  },

  async getCompetitionResourceDetail(
    sourceId: number
  ): Promise<{ item: CompetitionResourceItem; globalId: number | null }> {
    const flags = await getRuntimeFeatureFlags()
    if (canUseUnified(flags, 'competition_resource')) {
      const globalId = await resolveGlobalId('competition_resource', sourceId)
      const detail = await getUnifiedDetail(globalId)
      return { item: mapDetailToCompetitionItem(detail), globalId }
    }
    const res = await competitionApi.getResource(sourceId)
    const item = res.data.data
    if (!item) {
      throw new Error('资源不存在或已下架')
    }
    return { item, globalId: null }
  },

  async recordViewBySource(sourceType: SourceType, sourceId: number): Promise<void> {
    const flags = await getRuntimeFeatureFlags()
    if (canUseUnified(flags, sourceType)) {
      const globalId = await resolveGlobalId(sourceType, sourceId)
      await recordUnifiedView(globalId)
      return
    }
    switch (sourceType) {
      case 'topic_resource':
        await topicApi.recordView(sourceId)
        break
      case 'competition_resource':
        await competitionApi.recordView(sourceId)
        break
      case 'primary_chinese':
        await primaryChineseApi.getDetail(sourceId)
        break
      default:
        break
    }
  },

  async recordDownloadBySource(
    sourceType: 'topic_resource' | 'competition_resource' | 'primary_chinese',
    sourceId: number
  ): Promise<{ downloadUrl?: string }> {
    const flags = await getRuntimeFeatureFlags()
    if (canUseUnified(flags, sourceType)) {
      const globalId = await resolveGlobalId(sourceType, sourceId)
      return recordUnifiedDownload(globalId)
    }
    if (sourceType === 'topic_resource') {
      await topicApi.recordDownload(sourceId)
    } else if (sourceType === 'competition_resource') {
      await competitionApi.recordDownload(sourceId)
    }
    return {}
  },

  async listTopicResources(
    params: TopicQueryParams
  ): Promise<{ source: GatewaySource; page: TopicPageResult<TopicResourceItem> }> {
    const controller = beginRequest('topic')
    try {
      const flags = await getRuntimeFeatureFlags()
      const hasRegionFilter = !!params.region && params.region !== 'all'
      if (canUseUnified(flags, 'topic_resource') && !hasRegionFilter) {
        const data = await listUnified(
          {
            sourceType: 'topic_resource',
            stage: params.gradeStage,
            subject: params.subject,
            module: params.category,
            type: params.resourceForm,
            keyword: params.keyword,
            current: params.current,
            size: params.size,
            sortField: params.sortField,
            sortOrder: params.sortOrder,
          },
          controller.signal
        )
        return {
          source: 'unified',
          page: {
            records: (data.records || []).map(mapToTopicItem),
            total: data.total || 0,
            current: data.current || params.current || 1,
            size: data.size || params.size || 12,
            pages: data.pages || 0,
          },
        }
      }
      const legacy = await topicApi.listResources(params)
      return { source: 'legacy', page: legacy.data.data }
    } finally {
      finishRequest('topic', controller)
    }
  },

  async listCompetitionResources(
    params: CompetitionQueryParams
  ): Promise<{ source: GatewaySource; page: CompetitionPageResult<CompetitionResourceItem> }> {
    const controller = beginRequest('competition')
    try {
      const flags = await getRuntimeFeatureFlags()
      if (canUseUnified(flags, 'competition_resource')) {
        const data = await listUnified(
          {
            sourceType: 'competition_resource',
            stage: params.gradeStage,
            subject: params.subject,
            module: params.category,
            type: params.resourceForm,
            keyword: params.keyword,
            current: params.current,
            size: params.size,
            sortField: params.sortField,
            sortOrder: params.sortOrder,
          },
          controller.signal
        )
        return {
          source: 'unified',
          page: {
            records: (data.records || []).map(mapToCompetitionItem),
            total: data.total || 0,
            current: data.current || params.current || 1,
            size: data.size || params.size || 12,
            pages: data.pages || 0,
          },
        }
      }
      const legacy = await competitionApi.listResources(params)
      return { source: 'legacy', page: legacy.data.data }
    } finally {
      finishRequest('competition', controller)
    }
  },

  async listCultureResources(
    params: CultureQueryParams
  ): Promise<{ source: GatewaySource; page: CulturePageResult<CultureResourceItem> }> {
    const controller = beginRequest('culture')
    try {
      const flags = await getRuntimeFeatureFlags()
      const hasRegionFilter = !!params.region && params.region !== 'all'
      const hasDurationFilter = !!params.durationType && params.durationType !== 'all'
      if (canUseUnified(flags, 'culture_resource') && !hasRegionFilter && !hasDurationFilter) {
        const data = await listUnified(
          {
            sourceType: 'culture_resource',
            module: params.category,
            type: params.resourceKind,
            keyword: params.keyword,
            current: params.current,
            size: params.size,
            sortField: params.sortField,
            sortOrder: params.sortOrder,
          },
          controller.signal
        )
        return {
          source: 'unified',
          page: {
            records: (data.records || []).map(mapToCultureItem),
            total: data.total || 0,
            current: data.current || params.current || 1,
            size: data.size || params.size || 12,
            pages: data.pages || 0,
          },
        }
      }
      const legacy = await cultureStudyApi.listResources(params)
      return { source: 'legacy', page: legacy.data.data }
    } finally {
      finishRequest('culture', controller)
    }
  },

  resolveGlobalId,
  getUnifiedDetail,
  recordUnifiedView,
  recordUnifiedDownload,

  async getCultureResourceDetail(sourceId: number): Promise<CultureResourceItem> {
    const globalId = await resolveGlobalId('culture_resource', sourceId)
    const detail = await getUnifiedDetail(globalId)
    return mapDetailToCultureItem(detail)
  },

  async recordBySource(
    sourceType: SourceType,
    sourceId: number,
    action: 'view' | 'download'
  ): Promise<{ downloadUrl?: string }> {
    const globalId = await resolveGlobalId(sourceType, sourceId)
    if (action === 'view') {
      await recordUnifiedView(globalId)
      return {}
    }
    return recordUnifiedDownload(globalId)
  },
}

export type { TopicResourceItem, TopicQueryParams } from './topic'
export type { CompetitionResourceItem, CompetitionQueryParams } from './competition'
