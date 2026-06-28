/**
 * 上传表单标签映射常量
 * 从 useResourceUploadForm 中提取，避免重复定义
 */

export const GRADE_LEVEL_LABELS: Record<string, string> = {
  preschool: '幼儿',
  primary: '小学',
  junior: '初中',
  senior: '高中',
  art: '美术',
  dance: '舞蹈',
}

export const RESOURCE_TYPE_LABELS: Record<string, string> = {
  paper: '纸质类',
  video: '音视频类',
  document: '文档类',
  tool: '数字化工具',
  expand: '拓展资源',
}

export const DIFFICULTY_LABELS: Record<string, string> = {
  basic: '基础',
  improved: '提高',
  excellent: '培优',
  competition: '竞赛',
  art_exam: '艺考',
}

export const SCENARIO_LABELS: Record<string, string> = {
  classroom: '课堂教学',
  homework: '课后作业',
  review: '复习备考',
  competition: '竞赛辅导',
  exam_level: '考级集训',
  parent: '家长辅导',
}

export const EXAM_TYPE_LABELS: Record<string, string> = {
  kindergarten_bridge: '幼小衔接',
  school_sync: '校内同步',
  primary_exam: '小升初择校',
  ket_pet: 'KET/PET',
  olympiad: '奥数',
  coding_exam: '编程考级',
  middle_exam: '中考冲刺',
  competition: '学科竞赛',
  english_contest: '英语能力竞赛',
  college_entrance: '高考冲刺',
  art_culture: '艺体考文化课',
  ielts_toefl: '留学英语备考',
  art_level_exam: '考级',
  art_exam_prep: '艺考备考',
  art_talent: '艺术特长',
  art_competition: '比赛备赛',
  dance_level_exam: '考级',
  dance_exam_prep: '艺考备考',
  dance_talent: '艺术特长',
  dance_competition: '比赛备赛',
}
