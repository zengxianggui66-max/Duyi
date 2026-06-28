/**
 * 年级配置
 * 包含小学、初中、高中的年级配置
 */

// ==================== 类型定义 ====================

export type PrimaryGradeKey = 'grade1' | 'grade2' | 'grade3' | 'grade4' | 'grade5' | 'grade6'
export type JuniorGradeKey = 'grade7' | 'grade8' | 'grade9'
export type SeniorGradeKey = 'grade10' | 'grade11' | 'grade12'
export type KindergartenGradeKey = 'k1' | 'k2' | 'k3'

// ==================== 小学年级配置 ====================

export const primaryGradeKeys: PrimaryGradeKey[] = ['grade1', 'grade2', 'grade3', 'grade4', 'grade5', 'grade6']

export const primaryGradeNames: Record<string, string> = {
  grade1: '一年级', grade2: '二年级', grade3: '三年级',
  grade4: '四年级', grade5: '五年级', grade6: '六年级',
}

export const primaryGradeEmojis: Record<string, string> = {
  grade1: '🌱', grade2: '🌿', grade3: '🌸',
  grade4: '🌻', grade5: '🌟', grade6: '🚀',
}

export const primaryGradeColors: Record<string, string> = {
  grade1: 'linear-gradient(135deg, #FF6B6B, #ee5a24)',
  grade2: 'linear-gradient(135deg, #FF9F43, #e67e22)',
  grade3: 'linear-gradient(135deg, #26de81, #20bf6b)',
  grade4: 'linear-gradient(135deg, #45B7D1, #0984e3)',
  grade5: 'linear-gradient(135deg, #6C5CE7, #a29bfe)',
  grade6: 'linear-gradient(135deg, #fd79a8, #e84393)',
}

export const primaryGradeDescs: Record<string, string> = {
  grade1: '一年级语文、数学、英语等全科教学资源',
  grade2: '二年级语文、数学、英语等全科教学资源',
  grade3: '三年级语文、数学、英语等全科教学资源',
  grade4: '四年级语文、数学、英语、道法、科学等全科教学资源',
  grade5: '五年级语文、数学、英语、道法、科学等全科教学资源',
  grade6: '六年级语文、数学、英语、道法、科学、小升初备考资源',
}

// ==================== 初中年级配置 ====================

export const juniorGradeKeys: JuniorGradeKey[] = ['grade7', 'grade8', 'grade9']

export const juniorGradeNames: Record<string, string> = {
  grade7: '初一', grade8: '初二', grade9: '初三',
}

export const juniorGradeEmojis: Record<string, string> = {
  grade7: '📗', grade8: '📘', grade9: '📙',
}

export const juniorGradeColors: Record<string, string> = {
  grade7: 'linear-gradient(135deg, #4ECDC4, #44bd9e)',
  grade8: 'linear-gradient(135deg, #45B7D1, #0984e3)',
  grade9: 'linear-gradient(135deg, #A29BFE, #6C5CE7)',
}

export const juniorGradeDescs: Record<string, string> = {
  grade7: '初一年级语文、数学、英语等全科教学资源',
  grade8: '初二年级语文、数学、英语、物理等全科教学资源',
  grade9: '初三年级中考冲刺、全科复习备考资源',
}

// ==================== 高中年级配置 ====================

export const seniorGradeKeys: SeniorGradeKey[] = ['grade10', 'grade11', 'grade12']

export const seniorGradeNames: Record<string, string> = {
  grade10: '高一', grade11: '高二', grade12: '高三',
}

export const seniorGradeEmojis: Record<string, string> = {
  grade10: '🎓', grade11: '🎒', grade12: '🏆',
}

export const seniorGradeColors: Record<string, string> = {
  grade10: 'linear-gradient(135deg, #5B7CF9, #4a6cf7)',
  grade11: 'linear-gradient(135deg, #A78BFA, #8B5CF6)',
  grade12: 'linear-gradient(135deg, #F472B6, #EC4899)',
}

export const seniorGradeDescs: Record<string, string> = {
  grade10: '高一年级语数英物化生等全科教学资源',
  grade11: '高二年级语数英物化生等全科教学资源',
  grade12: '高三年级高考冲刺、全科复习备考资源',
}

// ==================== 幼儿年级配置 ====================

export const kindergartenGradeKeys: KindergartenGradeKey[] = ['k1', 'k2', 'k3']

export const kindergartenGradeNames: Record<string, string> = {
  k1: '小班', k2: '中班', k3: '大班',
}

export const kindergartenGradeEmojis: Record<string, string> = {
  k1: '🧒', k2: '👧', k3: '👦',
}

export const kindergartenGradeColors: Record<string, string> = {
  k1: 'linear-gradient(135deg, #FFB6C1, #FF69B4)',
  k2: 'linear-gradient(135deg, #DDA0DD, #BA55D3)',
  k3: 'linear-gradient(135deg, #E6E6FA, #9370DB)',
}

// ==================== 辅助函数 ====================

/** 判断是否为小学年级 */
export function isPrimaryGrade(key: string): boolean {
  return primaryGradeKeys.includes(key as PrimaryGradeKey)
}

/** 判断是否为低年级小学（1-3年级） */
export function isLowPrimaryGrade(key: string): boolean {
  return ['grade1', 'grade2', 'grade3'].includes(key)
}

/** 判断是否为低年级（isLowPrimaryGrade 的别名） */
export const isLowGrade = isLowPrimaryGrade

/** 判断是否为初中年级 */
export function isJuniorGrade(key: string): boolean {
  return juniorGradeKeys.includes(key as JuniorGradeKey)
}

/** 判断是否为初一年级 */
export function isJuniorLowGrade(key: string): boolean {
  return key === 'grade7'
}

/** 判断是否为高中年级 */
export function isSeniorGrade(key: string): boolean {
  return seniorGradeKeys.includes(key as SeniorGradeKey)
}

/** 判断是否为高三年级 */
export function isSeniorHighGrade(key: string): boolean {
  return key === 'grade12'
}

// ==================== 兼容性别名 ====================

/** gradeLevelNames 别名（兼容旧代码） */
export const gradeLevelNames = {
  ...primaryGradeNames,
  ...juniorGradeNames,
  ...seniorGradeNames,
}

/** gradeLevels 别名（兼容旧代码） */
export const gradeLevels = ['primary', 'junior', 'senior']

/** 根据年级 key 获取年级名称 */
export function getGradeName(key: string): string {
  return primaryGradeNames[key] ||
         juniorGradeNames[key] ||
         seniorGradeNames[key] ||
         kindergartenGradeNames[key] ||
         key
}

/** 根据年级 key 获取年级 emoji */
export function getGradeEmoji(key: string): string {
  return primaryGradeEmojis[key] ||
         juniorGradeEmojis[key] ||
         seniorGradeEmojis[key] ||
         kindergartenGradeEmojis[key] ||
         '📚'
}

/** 根据年级 key 获取年级渐变色 */
export function getGradeColor(key: string): string {
  return primaryGradeColors[key] ||
         juniorGradeColors[key] ||
         seniorGradeColors[key] ||
         kindergartenGradeColors[key] ||
         '#5B7CF9'
}
