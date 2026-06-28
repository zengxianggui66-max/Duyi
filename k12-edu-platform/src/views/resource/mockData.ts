/**
 * Mock 数据
 * 成套资源数据
 */
import { ref } from 'vue'

export interface Suite {
  key: string
  icon: string
  title: string
  sub: string
  tag?: string
  fileCount: number
  updateTime: string
}

export const suitePackages = ref<Suite[]>([
  { key: 'courseware', icon: '📚', title: '成套装课件', sub: '2026统编版语文一年级下册PPT课件(电子新教材)', tag: '配套资源', fileCount: 24, updateTime: '2026-05-10' },
  { key: 'lesson_plan', icon: '📖', title: '成套装教案', sub: '2026统编版一年级下册语文教案全册(新教材)', tag: '配套资源', fileCount: 18, updateTime: '2026-05-08' },
  { key: 'exercise', icon: '✏️', title: '成套装练习', sub: '2026统编版语文一年级下册同步练习可打印2026', tag: '配套资源', fileCount: 32, updateTime: '2026-05-05' },
  { key: 'guide', icon: '📘', title: '成套装学案', sub: '2026统编版语文一年级下册导学案及答案(新教材)', tag: '配套资源', fileCount: 15, updateTime: '2026-05-01' },
  { key: 'paper', icon: '📝', title: '成套装试卷', sub: '2026统编版语文一年级下册同步试卷2026', tag: '配套资源', fileCount: 12, updateTime: '2026-04-28' },
  { key: 'video', icon: '🎬', title: '成套装视频', sub: '2026统编版语文一年级下册教学视频全套', tag: '配套资源', fileCount: 20, updateTime: '2026-04-25' },
  { key: 'audio', icon: '🎧', title: '成套装音频', sub: '2026统编版语文一年级下册朗读音频MP3', tag: '配套资源', fileCount: 8, updateTime: '2026-04-22' },
  { key: 'test', icon: '📋', title: '成套装测试卷', sub: '2026统编版语文一年级下册单元测试卷含答案', tag: '配套资源', fileCount: 16, updateTime: '2026-04-20' },
  { key: 'summary', icon: '📑', title: '成套装教学总结', sub: '2026统编版语文一年级下册教学反思与总结', tag: '配套资源', fileCount: 10, updateTime: '2026-04-18' },
])

export const suiteCurrentPage = ref(1)
export const suitePageSize = ref(3)
