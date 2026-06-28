/**
 * 上传页智能推荐：按栏目 MODULE_RULES + 浏览上下文生成候选并填充表单
 */
import type { UploadFormData } from '@/composables/useResourceUploadForm'
import type { UploadBrowseSnapshot } from '@/utils/uploadRoute'
import { suggestSyncUploadTitle } from '@/utils/uploadRoute'
import type { StageKey } from '@/config/subjectConfig'

export interface UploadRecommendContext {
  fromBrowse: boolean
  module: string
  teachingType: string
  stageKey: string
  subjectKey: string
  gradeName: string
  editionName: string
  unitName: string
  lessonName: string
  resourceMode: 'single' | 'suite'
  gradeLevel: string
  subject: string
  channel: '' | 'competition' | 'topic'
}

export interface UploadRecommendation {
  id: string
  teachingType: string
  label: string
  description: string
  tags: string[]
  resourceType: string
  subType: string
  examTypes: string[]
  scenarios: string[]
  /** 与浏览页 type 一致，用于置顶与自动应用 */
  matchBrowse: boolean
  score: number
}

export interface ModuleRule {
  teachingTypes: string[]
  paperSubType?: string
  extraTags?: string[]
  examTypes?: string[]
}

/** 栏目 → 推荐的教学资源类型（与列表 teachingType 一致） */
export const MODULE_RULES: Record<string, ModuleRule> = {
  同步备课: {
    teachingTypes: [
      '课件',
      '教案',
      '学案',
      '练习',
      '试卷',
      '教学反思',
      '视频',
      '知识点',
      '精彩片段',
      '课文相关图片',
    ],
  },
  开学专区: {
    teachingTypes: ['试卷', '学案', '课件', '教案'],
    paperSubType: '入学测试',
    extraTags: ['sync'],
    examTypes: ['school_sync'],
  },
  月考: {
    teachingTypes: ['试卷'],
    paperSubType: 'unit_test',
    extraTags: ['sync'],
    examTypes: ['school_sync'],
  },
  期中: {
    teachingTypes: ['试卷'],
    paperSubType: 'final_exam',
    extraTags: ['quality', 'review'],
    examTypes: ['school_sync'],
  },
  期末: {
    teachingTypes: ['试卷'],
    paperSubType: 'final_exam',
    extraTags: ['quality', 'review'],
    examTypes: ['school_sync'],
  },
  小升初真题: {
    teachingTypes: ['试卷', '学案'],
    paperSubType: 'final_exam',
    examTypes: ['primary_exam'],
  },
  小升初模拟: {
    teachingTypes: ['试卷'],
    paperSubType: 'final_exam',
    examTypes: ['primary_exam'],
  },
  中考模拟: {
    teachingTypes: ['试卷', '学案'],
    paperSubType: 'final_exam',
    examTypes: ['middle_exam'],
  },
  中考真题: {
    teachingTypes: ['试卷'],
    paperSubType: 'final_exam',
    examTypes: ['middle_exam'],
  },
  高考模拟: {
    teachingTypes: ['试卷', '学案'],
    paperSubType: 'final_exam',
    examTypes: ['college_entrance'],
  },
  高考真题: {
    teachingTypes: ['试卷'],
    paperSubType: 'final_exam',
    examTypes: ['college_entrance'],
  },
  专题复习: {
    teachingTypes: ['学案', '试卷', '课件', '教案'],
    paperSubType: 'lecture_notes',
    extraTags: ['review'],
  },
  一轮复习: {
    teachingTypes: ['学案', '试卷', '课件'],
    extraTags: ['review'],
  },
  二轮专题: {
    teachingTypes: ['学案', '试卷', '课件'],
    extraTags: ['review'],
  },
  三轮冲刺: {
    teachingTypes: ['试卷', '学案'],
    extraTags: ['review'],
    examTypes: ['middle_exam', 'college_entrance'],
  },
  真题汇编: {
    teachingTypes: ['试卷', '学案'],
    extraTags: ['review', 'quality'],
  },
  暑假: {
    teachingTypes: ['学案', '练习', '试卷', '课件'],
    paperSubType: 'workbook',
  },
  寒假: {
    teachingTypes: ['学案', '练习', '试卷', '课件'],
    paperSubType: 'workbook',
  },
  作文: {
    teachingTypes: ['学案', '课件', '教案', '练习'],
  },
  阅读: {
    teachingTypes: ['学案', '课件', '练习', '教案'],
  },
  竞赛: {
    teachingTypes: ['试卷', '学案', '课件'],
    extraTags: ['competition'],
    examTypes: ['competition', 'olympiad'],
  },
  纯素材: {
    teachingTypes: ['课件', '视频', '音频/朗读', '知识点', '其他'],
  },
  学业水平: {
    teachingTypes: ['试卷', '学案'],
    paperSubType: 'final_exam',
    examTypes: ['school_sync'],
  },
}

const ART_DANCE_MODULE_RULES: Record<string, ModuleRule> = {
  同步备课: { teachingTypes: ['课件', '教案', '练习', '视频', '知识点'] },
  开学专区: { teachingTypes: ['试卷', '课件', '教案'], paperSubType: '入学测试' },
  月考: { teachingTypes: ['试卷'], paperSubType: 'unit_test' },
  期中: { teachingTypes: ['试卷'], paperSubType: 'final_exam' },
  期末: { teachingTypes: ['试卷'], paperSubType: 'final_exam' },
  专题复习: { teachingTypes: ['课件', '学案', '视频'] },
  暑假: { teachingTypes: ['课件', '练习', '视频'] },
  寒假: { teachingTypes: ['课件', '练习', '视频'] },
  竞赛: { teachingTypes: ['试卷', '课件', '视频'], extraTags: ['competition', 'exam_level'] },
}

const LESSON_SYNC_TYPES = ['课件', '教案', '学案', '练习', '试卷', '教学反思', '音频/朗读', '视频', '知识点']

const TEACHING_META: Record<string, { resourceType: string; subType: string }> = {
  课件: { resourceType: 'document', subType: 'courseware' },
  教案: { resourceType: 'document', subType: 'lesson_plan' },
  学案: { resourceType: 'paper', subType: '学案' },
  练习: { resourceType: 'paper', subType: 'workbook' },
  试卷: { resourceType: 'paper', subType: 'unit_test' },
  教学反思: { resourceType: 'document', subType: 'lesson_plan' },
  '音频/朗读': { resourceType: 'video', subType: 'listening' },
  视频: { resourceType: 'video', subType: '微课' },
  精彩片段: { resourceType: 'video', subType: '精彩片段' },
  课文相关图片: { resourceType: 'document', subType: '课文图片' },
  知识点: { resourceType: 'document', subType: '考点手册' },
  电子课本: { resourceType: 'tool', subType: 'ebook' },
  其他: { resourceType: 'expand', subType: '教具' },
}

const DEFAULT_MODULE_RULE: ModuleRule = MODULE_RULES['同步备课']

/** 以试卷/测评为主的栏目：浏览 type 与栏目不一致时，优先栏目规则而非 Tab */
const EXAM_FIRST_MODULES = new Set([
  '月考',
  '期中',
  '期末',
  '开学专区',
  '小升初真题',
  '小升初模拟',
  '中考模拟',
  '中考真题',
  '高考模拟',
  '高考真题',
  '学业水平',
  '真题汇编',
])

export function buildUploadRecommendContext(
  snapshot: UploadBrowseSnapshot,
  formData: Pick<
    UploadFormData,
    | 'module'
    | 'teachingType'
    | 'gradeLevel'
    | 'subject'
    | 'gradeName'
    | 'editionName'
    | 'unitName'
    | 'lessonName'
  >,
  channel: '' | 'competition' | 'topic' = '',
): UploadRecommendContext {
  return {
    fromBrowse: snapshot.fromBrowse,
    module: formData.module || snapshot.module || '同步备课',
    teachingType: formData.teachingType || snapshot.teachingType || '课件',
    stageKey: snapshot.stageKey || formData.gradeLevel || '',
    subjectKey: snapshot.subjectKey || formData.subject || '',
    gradeName: formData.gradeName || snapshot.gradeName || '',
    editionName: formData.editionName || snapshot.editionName || '',
    unitName: formData.unitName || snapshot.unitName || '',
    lessonName: formData.lessonName || snapshot.lessonName || '',
    resourceMode: snapshot.resourceMode || 'single',
    gradeLevel: formData.gradeLevel || snapshot.stageKey || '',
    subject: formData.subject || snapshot.subjectKey || '',
    channel,
  }
}

function resolveModuleRule(ctx: UploadRecommendContext): ModuleRule {
  const stage = ctx.stageKey || ctx.gradeLevel
  const table =
    stage === 'art' || stage === 'dance' ? ART_DANCE_MODULE_RULES : MODULE_RULES
  return table[ctx.module] ?? DEFAULT_MODULE_RULE
}

function filterTypesForCatalog(types: string[], ctx: UploadRecommendContext): string[] {
  if (ctx.module !== '同步备课') return types
  if (ctx.lessonName) {
    return types.filter((t) => LESSON_SYNC_TYPES.includes(t))
  }
  if (ctx.unitName && !ctx.lessonName) {
    return types.filter((t) =>
      ['课件', '教案', '学案', '练习', '试卷', '知识点'].includes(t),
    )
  }
  return types
}

function buildDescription(ctx: UploadRecommendContext, teachingType: string, rule: ModuleRule): string {
  const locParts = [ctx.gradeName, ctx.editionName, ctx.unitName, ctx.lessonName].filter(Boolean)
  const loc = locParts.length ? locParts.join(' · ') : '对应教材目录'
  const module = ctx.module

  if (teachingType === '试卷') {
    if (module === '月考') {
      return `本资源为${loc}月考试卷，难度适中，覆盖近期所学重难点，含参考答案，适合阶段性测评。`
    }
    if (module === '期中' || module === '期末') {
      return `本资源为${loc}${module}试卷，题目梯度合理，覆盖${module}考查范围，含答案与解析。`
    }
    if (module === '开学专区') {
      return `本资源为${loc}开学/摸底试卷，用于衔接新旧学期、诊断学情。`
    }
    if (['小升初真题', '小升初模拟', '中考模拟', '中考真题', '高考模拟', '高考真题'].includes(module)) {
      return `本资源为${module}相关试卷，贴近真题命题思路，适合备考与模拟训练。`
    }
    return `本资源为${loc}同步试卷，与${module}场景匹配，可用于课堂检测或课后巩固。`
  }

  const typeDesc: Record<string, string> = {
    课件: '课件设计清晰，含教学目标、重难点与课堂活动，便于投屏授课。',
    教案: '教案结构完整，含教学目标、教学过程与板书设计，可直接用于备课。',
    学案: '学案层次分明，含导学、练习与反思，适合学生自主学习或课堂发放。',
    练习: '练习覆盖本课知识点，难度分层，适合课后巩固与讲评。',
    视频: '视频时长适中，可用于课堂导入、知识点讲解或拓展。',
    知识点: '知识点梳理条理清晰，便于复习与考前回顾。',
  }

  const extra = typeDesc[teachingType] || `内容紧扣${module}教学要求。`
  return `本资源为「${module}」场景下的${teachingType}，适用于${loc}。${extra}`
}

function snapshotFromContext(ctx: UploadRecommendContext): UploadBrowseSnapshot {
  return {
    fromBrowse: ctx.fromBrowse,
    brandCode: '',
    catalogNodeId: 0,
    stageKey: (ctx.stageKey || ctx.gradeLevel) as StageKey | '',
    subjectKey: ctx.subjectKey || ctx.subject,
    versionKey: '',
    module: ctx.module,
    teachingType: ctx.teachingType,
    volumeId: '',
    gradeName: ctx.gradeName,
    editionName: ctx.editionName,
    unitName: ctx.unitName,
    lessonName: ctx.lessonName,
    resourceMode: ctx.resourceMode,
    unitNameForApi: ctx.unitName,
  }
}

function buildRecommendation(
  ctx: UploadRecommendContext,
  teachingType: string,
  rule: ModuleRule,
  index: number,
  subjectName: string,
): UploadRecommendation {
  const meta = TEACHING_META[teachingType] ?? TEACHING_META['其他']
  const isPaper = teachingType === '试卷'
  const subType = isPaper && rule.paperSubType ? rule.paperSubType : meta.subType
  const browseType = ctx.fromBrowse ? ctx.teachingType : ''
  const modulePrimary = rule.teachingTypes[0]
  const browseAlignsModule =
    !EXAM_FIRST_MODULES.has(ctx.module) ||
    browseType === modulePrimary ||
    rule.teachingTypes.includes(browseType)
  const matchBrowse =
    !!browseType &&
    browseType !== '全部' &&
    teachingType === browseType &&
    browseAlignsModule
  const baseTags = new Set<string>(['sync'])
  if (rule.extraTags) rule.extraTags.forEach((t) => baseTags.add(t))
  if (ctx.channel === 'competition') {
    baseTags.delete('sync')
    baseTags.add('competition')
  }
  if (ctx.channel === 'topic') {
    baseTags.delete('sync')
    baseTags.add('topic')
  }
  if (teachingType === '试卷' && (ctx.module === '期中' || ctx.module === '期末')) {
    baseTags.add('quality')
    baseTags.add('review')
  }

  let score = 100 - index
  if (matchBrowse) score += 200
  if (EXAM_FIRST_MODULES.has(ctx.module) && teachingType === modulePrimary) {
    score += 180
  }
  if (ctx.lessonName && ['课件', '教案', '学案', '练习'].includes(teachingType)) {
    score += 10
  }
  if (ctx.module !== '同步备课' && teachingType === '试卷') {
    score += 15
  }

  const id = `${ctx.module}_${teachingType}`.replace(/\s+/g, '_')

  return {
    id,
    teachingType,
    label: matchBrowse ? `与浏览一致：${teachingType}` : teachingType,
    description: buildDescription(ctx, teachingType, rule),
    tags: [...baseTags],
    resourceType: meta.resourceType,
    subType,
    examTypes: rule.examTypes ? [...rule.examTypes] : isPaper ? ['school_sync'] : [],
    scenarios: isPaper ? ['review', 'exam_level'] : ['classroom', 'homework'],
    matchBrowse,
    score,
  }
}

export function getUploadRecommendations(
  ctx: UploadRecommendContext,
  subjectName = '',
): UploadRecommendation[] {
  if (ctx.channel === 'competition' || ctx.channel === 'topic') {
    return []
  }

  const rule = resolveModuleRule(ctx)
  let types = filterTypesForCatalog([...rule.teachingTypes], ctx)

  const browseType =
    ctx.fromBrowse && ctx.teachingType && ctx.teachingType !== '全部' ? ctx.teachingType : ''
  if (browseType && !types.includes(browseType)) {
    types = [browseType, ...types]
  }

  const seen = new Set<string>()
  const list: UploadRecommendation[] = []
  types.forEach((t, i) => {
    if (seen.has(t)) return
    seen.add(t)
    list.push(buildRecommendation(ctx, t, rule, i, subjectName))
  })

  list.sort((a, b) => b.score - a.score)
  return list.slice(0, 8)
}

export function applyUploadRecommendation(
  formData: UploadFormData,
  rec: UploadRecommendation,
  options: {
    subjectName?: string
    context?: UploadRecommendContext
    updateTitle?: boolean
    preserveDescription?: boolean
    /** 浏览进入时保持列表 Tab 类型，仅更新简介/标签等 */
    preserveTeachingType?: boolean
    silent?: boolean
  } = {},
): void {
  if (!options.preserveTeachingType) {
    formData.teachingType = rec.teachingType
    formData.resourceType = rec.resourceType
    formData.subType = rec.subType
    formData.examTypes = [...rec.examTypes]
    formData.scenarios = [...rec.scenarios]
  }
  if (!options.preserveDescription) {
    formData.description = rec.description
  }

  const tagSet = new Set(formData.tags)
  rec.tags.forEach((t) => tagSet.add(t))
  formData.tags = [...tagSet]

  if (options.updateTitle !== false && options.context) {
    const snap = snapshotFromContext({
      ...options.context,
      teachingType: rec.teachingType,
    })
    const title = suggestSyncUploadTitle(snap, options.subjectName || '')
    if (title) formData.title = title
  }
}

/** 更多模板（非浏览主路径，艺术/竞赛等） */
export const LEGACY_UPLOAD_TEMPLATES: {
  id: string
  label: string
  group: string
  patch: Partial<UploadFormData>
}[] = [
  {
    id: 'primary_english_lesson',
    label: '小学英语教案',
    group: '通用',
    patch: {
      description: '本资源为小学英语同步教案，适配外研版教材，可用于课堂教学和课后巩固练习。',
      tags: ['sync', 'quality'],
      scenarios: ['classroom', 'homework'],
      resourceType: 'document',
      subType: 'lesson_plan',
      teachingType: '教案',
    },
  },
  {
    id: 'art_exam_portfolio',
    label: '美术考级作品集',
    group: '艺术',
    patch: {
      description: '本资源为美术考级作品集模板，含作品说明与点评要点，适用于考级备赛与展示。',
      tags: ['exam_level', 'art_exam'],
      scenarios: ['exam_level'],
      resourceType: 'expand',
      subType: '成长手册',
      teachingType: '其他',
      examTypes: ['art_level_exam'],
    },
  },
  {
    id: 'dance_teaching_plan',
    label: '舞蹈考级教学计划',
    group: '艺术',
    patch: {
      description: '本资源为舞蹈考级教学计划，含阶段目标、基本功训练与考级曲目安排。',
      tags: ['exam_level'],
      scenarios: ['classroom', 'exam_level'],
      resourceType: 'document',
      subType: 'lesson_plan',
      teachingType: '教案',
      examTypes: ['dance_level_exam'],
    },
  },
]

