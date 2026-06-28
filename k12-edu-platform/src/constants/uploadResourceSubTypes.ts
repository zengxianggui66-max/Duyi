/** 资源类型对应的子类型（分类配置步使用，非 taxonomy 兜底） */
export type ResourceSubTypeOption = { label: string; value: string }

export const subTypesByResourceType: Record<string, ResourceSubTypeOption[]> = {
  paper: [
    { label: '讲义', value: 'lecture_notes' },
    { label: '练习册', value: 'workbook' },
    { label: '同步教辅', value: '同步教辅' },
    { label: '单元测试', value: 'unit_test' },
    { label: '期末试卷', value: 'final_exam' },
    { label: '教材配套学案', value: '学案' },
    { label: '错题本', value: '错题集' },
  ],
  video: [
    { label: '微课', value: '微课' },
    { label: '精彩片段', value: '精彩片段' },
    { label: '短视频', value: '短视频' },
    { label: '动画教学', value: 'animation' },
    { label: '听力素材', value: 'listening' },
    { label: '课堂实录', value: '课堂实录' },
    { label: '实验教学视频', value: '实验视频' },
  ],
  document: [
    { label: '教案', value: 'lesson_plan' },
    { label: '课件', value: 'courseware' },
    { label: '教学大纲', value: '教学大纲' },
    { label: '学情分析报告', value: '学情分析' },
    { label: '家长沟通手册', value: '家长手册' },
    { label: '考点手册', value: '考点手册' },
  ],
  tool: [
    { label: '题库', value: '题库' },
    { label: 'AI组卷', value: 'ai_paper' },
    { label: '电子教材', value: 'ebook' },
    { label: '课件模板', value: '课件模板' },
    { label: '学习平台', value: 'learning_platform' },
  ],
  expand: [
    { label: '入学测', value: '入学测试' },
    { label: '阶段测', value: '阶段测试' },
    { label: '实验器材', value: '实验器材' },
    { label: '教具', value: '教具' },
    { label: '手账', value: '手账' },
    { label: '成长手册', value: '成长手册' },
  ],
}
