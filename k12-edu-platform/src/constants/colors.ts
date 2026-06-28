/**
 * 主题颜色配置
 */

/** 标签颜色列表（随机分配） */
export const tagColors = [
  '#409EFF', '#67C23A', '#E6A23C', '#F56C6C',
  '#909399', '#00b5ad', '#a233c6', '#ff7675',
]

/** 随机获取一个标签颜色 */
export function getRandomTagColor(): string {
  return tagColors[Math.floor(Math.random() * tagColors.length)]
}

/** 学段主题色 */
export const stageColors: Record<string, string> = {
  preschool: '#52C41A',
  primary: '#FF6B6B',
  junior: '#4ECDC4',
  senior: '#5B7CF9',
  art: '#E91E63',
  dance: '#9C27B0',
}

/** 获取学段主题色 */
export function getStageColor(stage: string): string {
  return stageColors[stage] || '#5B7CF9'
}

/** 学科主题色 */
export const subjectColors: Record<string, string> = {
  chinese: '#E74C3C',
  math: '#3498DB',
  english: '#2ECC71',
  physics: '#9B59B6',
  chemistry: '#F39C12',
  biology: '#1ABC9C',
}

/** 获取学科主题色 */
export function getSubjectColor(subject: string): string {
  return subjectColors[subject] || '#5B7CF9'
}
