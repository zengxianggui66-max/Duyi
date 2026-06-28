/**
 * 「我的资源」页：学段 / 学科 / 资源类型（与学科浏览、上传向导对齐）
 */
import type { RouteLocationRaw } from 'vue-router'
import type { StageKey } from '@/config/subjectConfig'
import { stageNames, subjectDataMap } from '@/config/subjectConfig'
import { getSubjectsByGrade, getSubjectInfo, type SubjectInfo } from '@/constants/subjects'
import { getResourceTypesByGrade, getResourceTypeName } from '@/constants/resource-types'
import { MODULE_RULES } from '@/constants/uploadRecommend'

export type MyResourceSection = StageKey

export interface MyResourceSectionItem {
  key: MyResourceSection
  name: string
  icon: string
}

export interface MyResourceNavItem {
  key: string
  name: string
  icon: string
  color?: string
}

export interface MyResourceTypeTab {
  key: string
  name: string
  icon: string
}

export const MY_RESOURCE_SECTIONS: MyResourceSectionItem[] = [
  { key: 'preschool', name: '幼儿', icon: '🌱' },
  { key: 'primary', name: '小学', icon: '🌈' },
  { key: 'junior', name: '初中', icon: '📘' },
  { key: 'senior', name: '高中', icon: '🎓' },
  { key: 'art', name: '美术', icon: '🎨' },
  { key: 'dance', name: '舞蹈', icon: '💃' },
]

/** 幼儿 / 小学 / 初中 / 高中侧边学科（用户指定核心学科） */
const K12_SUBJECT_NAV: Record<'preschool' | 'primary' | 'junior' | 'senior', MyResourceNavItem[]> = {
  preschool: [
    { key: 'chinese', name: '拼音识字', icon: '🔤', color: '#52C41A' },
    { key: 'math', name: '数学启蒙', icon: '🔢', color: '#3498DB' },
    { key: 'habit', name: '习惯养成', icon: '🌟', color: '#F59E0B' },
    { key: 'activity', name: '综合活动', icon: '🧩', color: '#8B5CF6' },
  ],
  primary: [
    { key: 'chinese', name: '语文', icon: '📖', color: '#E74C3C' },
    { key: 'math', name: '数学', icon: '🔢', color: '#3498DB' },
    { key: 'english', name: '英语', icon: '🌍', color: '#2ECC71' },
  ],
  junior: [
    { key: 'chinese', name: '语文', icon: '📖', color: '#E74C3C' },
    { key: 'math', name: '数学', icon: '🔢', color: '#3498DB' },
    { key: 'english', name: '英语', icon: '🌍', color: '#2ECC71' },
    { key: 'physics', name: '物理', icon: '⚡', color: '#9B59B6' },
    { key: 'chemistry', name: '化学', icon: '🧪', color: '#F39C12' },
    { key: 'biology', name: '生物', icon: '🧬', color: '#1ABC9C' },
  ],
  senior: [
    { key: 'chinese', name: '语文', icon: '📖', color: '#E74C3C' },
    { key: 'math', name: '数学', icon: '🔢', color: '#3498DB' },
    { key: 'english', name: '英语', icon: '🌍', color: '#2ECC71' },
    { key: 'physics', name: '物理', icon: '⚡', color: '#9B59B6' },
    { key: 'chemistry', name: '化学', icon: '🧪', color: '#F39C12' },
    { key: 'biology', name: '生物', icon: '🧬', color: '#1ABC9C' },
  ],
}

const TEACHING_TYPE_ICONS: Record<string, string> = {
  课件: '📊',
  教案: '📖',
  练习: '✏️',
  试卷: '📋',
  学案: '📘',
  电子课本: '📕',
  教学反思: '💭',
  '音频/朗读': '🎧',
  视频: '🎬',
  知识点: '📚',
  其他: '📄',
}

export function isK12Section(section: MyResourceSection): boolean {
  return section === 'preschool' || section === 'primary' || section === 'junior' || section === 'senior'
}

export function isSpecialtySection(section: MyResourceSection): boolean {
  return section === 'art' || section === 'dance'
}

/** 侧边学科列表 */
export function getMyResourceSubjects(section: MyResourceSection): MyResourceNavItem[] {
  if (isK12Section(section)) {
    return K12_SUBJECT_NAV[section]
  }
  return getSubjectsByGrade(section).map((s: SubjectInfo) => ({
    key: s.key,
    name: s.name,
    icon: s.icon,
    color: s.color,
  }))
}

export function getMyResourceSubjectNavTitle(section: MyResourceSection): string {
  if (section === 'art') return '🎨 美术分类'
  if (section === 'dance') return '💃 舞蹈分类'
  if (section === 'preschool') return '🌱 幼儿启蒙'
  if (section === 'primary') return '📚 小学学科'
  if (section === 'junior') return '📘 初中学科'
  if (section === 'senior') return '🎓 高中学科'
  return '学科分类'
}

/** 右侧资源类型 Tab（K12 用同步备课 teachingType；美术/舞蹈用专区类型配置） */
export function getMyResourceTypeTabs(section: MyResourceSection): MyResourceTypeTab[] {
  const allTab: MyResourceTypeTab = { key: 'all', name: '全部', icon: '📁' }
  if (isSpecialtySection(section)) {
    return [
      allTab,
      ...getResourceTypesByGrade(section).map((t) => ({
        key: t.key,
        name: t.name,
        icon: t.icon,
      })),
    ]
  }
  const teachingTypes = MODULE_RULES['同步备课']?.teachingTypes ?? [
    '课件',
    '教案',
    '练习',
    '试卷',
    '学案',
    '视频',
    '知识点',
  ]
  return [
    allTab,
    ...teachingTypes.map((name) => ({
      key: name,
      name,
      icon: TEACHING_TYPE_ICONS[name] || '📄',
    })),
  ]
}

export function sectionToApiStage(section: MyResourceSection): string {
  return stageNames[section] || section
}

/** API 学段名 / stageKey → 我的资源侧边栏 section */
export function apiStageToSection(stage?: string, stageKey?: string): MyResourceSection | '' {
  if (stageKey && MY_RESOURCE_SECTIONS.some((s) => s.key === stageKey)) {
    return stageKey as MyResourceSection
  }
  const map: Record<string, MyResourceSection> = {
    小学: 'primary',
    幼儿: 'preschool',
    初中: 'junior',
    高中: 'senior',
    美术: 'art',
    舞蹈: 'dance',
  }
  if (stage && map[stage]) return map[stage]
  return ''
}

export function resolveK12SubjectName(section: MyResourceSection, subjectKey: string): string {
  const fromMap = subjectDataMap[section]?.find((s) => s.key === subjectKey)
  if (fromMap) return fromMap.name
  const fromNav = K12_SUBJECT_NAV[section as 'preschool' | 'primary' | 'junior' | 'senior']?.find(
    (s) => s.key === subjectKey,
  )
  return fromNav?.name || subjectKey
}

export function resolveSubjectApiName(
  section: MyResourceSection,
  subjectKey: string,
): string {
  if (!subjectKey) return ''
  if (isK12Section(section)) {
    return resolveK12SubjectName(section, subjectKey)
  }
  return getSubjectInfo(subjectKey)?.name || subjectKey
}

export function resolveTypeDisplayName(
  section: MyResourceSection,
  typeKey: string,
): string {
  if (!typeKey || typeKey === 'all') return '全部'
  if (isSpecialtySection(section)) {
    return getResourceTypeName(typeKey, section) || typeKey
  }
  return typeKey
}

/** 跳转统一上传页（与学科页 buildUploadLocation 参数一致） */
export function buildMyResourceUploadLocation(input: {
  section: MyResourceSection
  subjectKey?: string
  typeKey?: string
}): RouteLocationRaw {
  const { section, subjectKey, typeKey } = input
  const stage = section
  let subject = subjectKey || ''
  if (!subject) {
    if (isK12Section(section)) {
      subject = K12_SUBJECT_NAV[section][0]?.key || 'chinese'
    } else if (section === 'art') {
      subject = 'art'
    } else if (section === 'dance') {
      subject = 'dance'
    }
  }
  const query: Record<string, string> = {
    stage,
    subject,
    module: '同步备课',
  }
  if (typeKey && typeKey !== 'all') {
    query.type = typeKey
  }
  if (isSpecialtySection(section) && subjectKey) {
    const cat = getSubjectInfo(subjectKey)
    if (cat?.name) {
      query.unit = cat.name
    }
  }
  return { path: '/upload', query }
}
