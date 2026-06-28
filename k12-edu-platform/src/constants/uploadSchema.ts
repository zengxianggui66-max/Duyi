/**
 * 上传向导：按栏目控制可见字段与默认类型（与 uploadRecommend.MODULE_RULES 对齐）
 */
import { MODULE_RULES } from '@/constants/uploadRecommend'
import { LESSON_TYPE_ORDER } from '@/composables/useLessonHub'

export interface UploadModuleSchema {
  module: string
  /** 同步备课式：册别/版本/目录 */
  showSyncCatalog: boolean
  /** 展示资源类型选择 */
  showTeachingType: boolean
  /** 展示单份/成套 */
  showResourceMode: boolean
  /** 直达向导是否跳过「分类配置」步 */
  skipClassificationStep: boolean
  /** 是否隐藏备考类型块（分类步内也由 isSyncUpload 控制） */
  hideExamPrep: boolean
  defaultTeachingType: string
  teachingTypeOptions: string[]
}

const EXAM_MODULES = new Set([
  '月考',
  '期中',
  '期末',
  '小升初真题',
  '中考真题',
  '高考真题',
  '模拟',
  '真题',
  '一轮复习',
  '二轮复习',
])

function teachingTypesForModule(module: string): string[] {
  const rule = MODULE_RULES[module]
  if (rule?.teachingTypes?.length) return [...rule.teachingTypes]
  return LESSON_TYPE_ORDER.filter((t) => !['全部', '其他'].includes(String(t)))
}

export function getUploadModuleSchema(module: string): UploadModuleSchema {
  const isSync = module === '同步备课'
  const isExam = EXAM_MODULES.has(module)
  const types = teachingTypesForModule(module)

  return {
    module,
    showSyncCatalog: isSync,
    showTeachingType: true,
    showResourceMode: isSync,
    skipClassificationStep: isSync || isExam,
    hideExamPrep: isSync || isExam,
    defaultTeachingType: types[0] || '课件',
    teachingTypeOptions: types,
  }
}

export function shouldSkipClassificationStep(module: string): boolean {
  return getUploadModuleSchema(module).skipClassificationStep
}


