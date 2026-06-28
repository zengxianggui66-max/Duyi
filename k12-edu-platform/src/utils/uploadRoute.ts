/**
 * 学科浏览页 → 上传页 路由 query 编解码（与 useResourceBrowseContext / useApiResources 对齐）
 */
import type { RouteLocationRaw } from 'vue-router'
import type { StageKey } from '@/config/subjectConfig'
import { stageNames } from '@/config/subjectConfig'
import { normalizeVersionKey } from '@/composables/useResourceBrowseContext'

/** 上传页 query（同步备课为主） */
export interface UploadRouteQuery {
  from?: 'browse'
  brand?: string
  node?: string
  stage?: string
  subject?: string
  version?: string
  module?: string
  type?: string
  volume?: string
  gradeName?: string
  edition?: string
  unit?: string
  lesson?: string
  mode?: 'single' | 'suite'
}

export interface UploadBrowseSnapshot {
  fromBrowse: boolean
  brandCode: string
  catalogNodeId: number
  stageKey: StageKey | ''
  subjectKey: string
  versionKey: string
  module: string
  teachingType: string
  volumeId: string
  gradeName: string
  editionName: string
  unitName: string
  lessonName: string
  resourceMode: 'single' | 'suite'
  /** 列表 API 用的父级单元名（课文叶子时） */
  unitNameForApi: string
  /** 课文叶子时的 keyword */
  keywordForApi?: string
}

export function buildUploadRouteQuery(input: {
  brandCode?: string
  catalogNodeId?: number
  stage: string
  subjectKey: string
  versionKey: string
  module?: string
  teachingType?: string
  volumeId?: string
  gradeName?: string
  editionName?: string
  unit?: string
  lesson?: string
  resourceMode?: 'single' | 'suite'
}): UploadRouteQuery {
  const query: UploadRouteQuery = {
    from: 'browse',
    stage: input.stage,
    subject: input.subjectKey,
    version: normalizeVersionKey(input.versionKey),
  }
  if (input.brandCode) query.brand = input.brandCode
  if (input.catalogNodeId && input.catalogNodeId > 0) {
    query.node = String(input.catalogNodeId)
  }
  if (input.module) query.module = input.module
  if (input.teachingType && input.teachingType !== '全部') query.type = input.teachingType
  if (input.volumeId) query.volume = input.volumeId
  if (input.gradeName) query.gradeName = input.gradeName
  if (input.editionName) query.edition = input.editionName
  if (input.unit) query.unit = input.unit
  if (input.lesson) query.lesson = input.lesson
  if (input.resourceMode) query.mode = input.resourceMode
  return query
}

export function buildUploadLocation(input: Parameters<typeof buildUploadRouteQuery>[0]): RouteLocationRaw {
  return {
    path: '/upload',
    query: buildUploadRouteQuery(input) as Record<string, string>,
  }
}

export function buildSubjectReturnLocation(snapshot: UploadBrowseSnapshot): RouteLocationRaw | null {
  if (!snapshot.fromBrowse || !snapshot.stageKey || !snapshot.subjectKey || !snapshot.versionKey) {
    return null
  }
  const query: Record<string, string> = {}
  if (snapshot.brandCode) query.brand = snapshot.brandCode
  if (snapshot.catalogNodeId > 0) query.node = String(snapshot.catalogNodeId)
  if (snapshot.module) query.module = snapshot.module
  if (snapshot.teachingType && snapshot.teachingType !== '全部') query.type = snapshot.teachingType
  if (snapshot.volumeId) query.volume = snapshot.volumeId
  if (snapshot.resourceMode) query.mode = snapshot.resourceMode
  if (snapshot.lessonName) {
    query.unit = snapshot.unitName
    query.lesson = snapshot.lessonName
  } else if (snapshot.unitName) {
    query.unit = snapshot.unitName
  }
  return {
    name: 'SubjectDetail',
    params: {
      stage: snapshot.stageKey,
      subject: snapshot.subjectKey,
      version: normalizeVersionKey(snapshot.versionKey),
    },
    query,
  }
}

export function parseUploadRouteQuery(
  raw: Record<string, string | string[] | undefined>,
): UploadBrowseSnapshot {
  const str = (k: string) => {
    const v = raw[k]
    return typeof v === 'string' ? v : ''
  }

  const brandCode = str('brand')
  const nodeRaw = str('node')
  const catalogNodeId = nodeRaw ? Number(nodeRaw) : 0
  const stageKey = (str('stage') || '') as StageKey | ''
  const subjectKey = str('subject')
  const versionKey = normalizeVersionKey(str('version'))
  const module = str('module') || '同步备课'
  const teachingType = str('type') || '课件'
  const volumeId = str('volume')
  const gradeName = str('gradeName')
  const editionName = str('edition')
  const unitName = str('unit')
  const lessonName = str('lesson')
  const resourceMode = str('mode') === 'suite' ? 'suite' : 'single'
  /** 仅学科页显式 from=browse 时走两步向导；个人中心/草稿续编带 stage 仍用四步直达向导 */
  const fromBrowse = str('from') === 'browse'

  const isLesson = !!lessonName
  const unitNameForApi = isLesson ? unitName || lessonName : unitName || lessonName
  const keywordForApi = isLesson ? lessonName : undefined

  return {
    fromBrowse,
    brandCode,
    catalogNodeId: Number.isNaN(catalogNodeId) ? 0 : catalogNodeId,
    stageKey,
    subjectKey,
    versionKey,
    module,
    teachingType,
    volumeId,
    gradeName,
    editionName,
    unitName: isLesson ? unitName : unitName || lessonName,
    lessonName,
    resourceMode,
    unitNameForApi,
    keywordForApi,
  }
}

export function formatUploadContextSummary(snapshot: UploadBrowseSnapshot, subjectName: string): string {
  const stageLabel = snapshot.stageKey ? stageNames[snapshot.stageKey as StageKey] || '' : ''
  const parts = [
    stageLabel,
    subjectName,
    snapshot.gradeName,
    snapshot.editionName,
    snapshot.module,
    snapshot.teachingType !== '全部' ? snapshot.teachingType : '',
    snapshot.lessonName
      ? snapshot.unitName
        ? `${snapshot.unitName} · ${snapshot.lessonName}`
        : snapshot.lessonName
      : snapshot.unitName,
  ]
  return parts.filter(Boolean).join(' · ')
}

export function formatResourceModeLabel(mode: 'single' | 'suite'): string {
  return mode === 'suite' ? '找成套' : '找单份'
}

/** 直达上传：根据表单字段生成与列表筛选一致的快照 */
export function buildPlacementSnapshot(input: {
  stageKey: StageKey | ''
  subjectKey: string
  versionKey?: string
  module: string
  teachingType: string
  gradeName: string
  editionName: string
  unitName: string
  lessonName: string
  resourceMode?: 'single' | 'suite'
  catalogNodeId?: number
}): UploadBrowseSnapshot {
  const lessonName = input.lessonName || ''
  const unitName = input.unitName || ''
  const catalogNodeId = input.catalogNodeId && input.catalogNodeId > 0 ? input.catalogNodeId : 0
  return {
    fromBrowse: false,
    brandCode: '',
    catalogNodeId,
    stageKey: input.stageKey,
    subjectKey: input.subjectKey,
    versionKey: input.versionKey || 'tongbian2024',
    module: input.module || '同步备课',
    teachingType: input.teachingType || '课件',
    volumeId: '',
    gradeName: input.gradeName,
    editionName: input.editionName,
    unitName,
    lessonName,
    resourceMode: input.resourceMode || 'single',
    unitNameForApi: lessonName ? unitName : unitName,
    keywordForApi: undefined,
  }
}

/** 成套上传单条标题：套件名 + 文件名（去扩展名） */
export function suggestSuiteItemTitle(
  baseTitle: string,
  fileName: string,
  index: number,
): string {
  const stem = fileName.replace(/\.[^.]+$/, '').trim()
  const prefix = baseTitle.trim() || '成套资源'
  if (index <= 1 && !stem) return prefix
  return stem ? `${prefix}-${stem}` : `${prefix}-${index}`
}

/** 同步备课默认标题：课文名-类型 */
export function suggestSyncUploadTitle(snapshot: UploadBrowseSnapshot, subjectName: string): string {
  const type = snapshot.teachingType && snapshot.teachingType !== '全部' ? snapshot.teachingType : '课件'
  if (snapshot.lessonName) {
    return `${snapshot.lessonName}-${type}`
  }
  const parts = [snapshot.gradeName, snapshot.editionName, subjectName, snapshot.unitName, type].filter(Boolean)
  return parts.join('') || ''
}
