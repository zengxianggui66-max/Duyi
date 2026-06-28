/**
 * 学段配置
 */
export interface StageInfo {
  key: string
  name: string
  icon: string
  color: string
  description: string
}

export const stages: StageInfo[] = [
  {
    key: 'preschool',
    name: '幼儿',
    icon: '🌱',
    color: '#52C41A',
    description: '拼音识字、数学启蒙、习惯养成、绘本阅读与幼小衔接资源'
  },
  {
    key: 'primary',
    name: '小学',
    icon: '🌈',
    color: '#FF6B6B',
    description: '语文、数学、英语、美术、书法、编程等全科教学资源'
  },
  {
    key: 'junior',
    name: '初中',
    icon: '📘',
    color: '#4ECDC4',
    description: '语文、数学、英语、物理、化学、生物·中考备考专区'
  },
  {
    key: 'senior',
    name: '高中',
    icon: '🎓',
    color: '#5B7CF9',
    description: '全科覆盖·理综·高考一轮二轮三轮复习专区'
  },
  {
    key: 'art',
    name: '美术',
    icon: '🎨',
    color: '#E91E63',
    description: '素描、色彩、速写、创意设计·美术考级与艺考专区'
  },
  {
    key: 'dance',
    name: '舞蹈',
    icon: '💃',
    color: '#9C27B0',
    description: '中国舞、芭蕾舞、街舞、拉丁舞·舞蹈考级与艺考专区'
  },
]

export const stageNames: Record<string, string> = {
  preschool: '幼儿',
  primary: '小学',
  junior: '初中',
  senior: '高中',
  art: '美术',
  dance: '舞蹈',
}

export function getStageInfo(key: string): StageInfo | undefined {
  return stages.find(s => s.key === key)
}
