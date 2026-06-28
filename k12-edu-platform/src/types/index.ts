/** 学段类型 */
export type GradeLevel = 'primary' | 'junior' | 'senior'

/** 学科类型 */
export type Subject =
  | 'chinese' | 'math' | 'english' | 'physics' | 'chemistry'
  | 'biology' | 'politics' | 'history' | 'geography' | 'science_composite'
  | 'art' | 'calligraphy' | 'programming' | 'parent_meeting' | 'home_visit'

/** 资源类型 */
export type ResourceType =
  | 'courseware' | 'lesson_plan' | 'study_guide' | 'exam_paper'
  | 'material' | 'audio_video' | 'comprehensive' | 'holiday_homework'

/** 教材版本 */
export type TextbookVersion = 'bubei' | 'renjiao' | 'bshida' | 'waiyan' | 'other'

/** 备考类型 */
export type ExamType =
  | 'unit_test' | 'monthly' | 'midterm' | 'final' | 'mock' | 'zhongkao' | 'gaokao'
  | 'competition' | 'independent' | 'spring_entrance'

/** 资源项 */
export interface Resource {
  id: number
  title: string
  gradeLevel: GradeLevel
  subject: Subject
  resourceType: ResourceType
  textbookVersion: TextbookVersion
  examType?: ExamType
  description: string
  coverUrl: string
  downloadCount: number
  viewCount: number
  fileSize: string
  fileType: string
  author: string
  createdAt: string
  tags: string[]
  isFree: boolean
  rating: number
}

/** 学科信息 */
export interface SubjectInfo {
  key: Subject
  name: string
  icon: string
  color: string
  gradeLevels: GradeLevel[]
}

/** 教材版本信息 */
export interface VersionInfo {
  key: TextbookVersion
  name: string
  shortName: string
}

/** 资源分类信息 */
export interface CategoryInfo {
  key: string
  name: string
  icon: string
  children?: CategoryInfo[]
}

/** 搜索参数 */
export interface SearchParams {
  keyword?: string
  gradeLevel?: GradeLevel
  subject?: Subject
  resourceType?: ResourceType
  textbookVersion?: TextbookVersion
  examType?: ExamType
  page?: number
  pageSize?: number
  sort?: 'latest' | 'popular' | 'rating'
}

/** 分页响应 */
export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
  totalPages: number
}

/** 用户信息 */
export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string
  email: string
  phone: string
  role: 'student' | 'teacher' | 'parent' | 'pending' | 'admin'
  memberLevel: 'free' | 'basic' | 'premium'
  memberExpireAt?: string
  createdAt: string
}

/** 新闻/资讯 */
export interface NewsItem {
  id: number
  title: string
  summary: string
  coverUrl: string
  category: 'policy' | 'reform' | 'research' | 'teacher' | 'course' | 'premium'
  viewCount: number
  createdAt: string
  tags: string[]
}

/** 试题项 */
export interface Question {
  id: number
  content: string
  type: 'choice' | 'fill' | 'short_answer' | 'essay'
  subject: Subject
  gradeLevel: GradeLevel
  difficulty: 1 | 2 | 3 | 4 | 5
  knowledgePoint: string
  options?: string[]
  answer: string
  analysis: string
  tags: string[]
}

/** 试卷 */
export interface ExamPaper {
  id: number
  title: string
  gradeLevel: GradeLevel
  subject: Subject
  examType: ExamType
  questionCount: number
  totalScore: number
  duration: number
  difficulty: number
  createdAt: string
  downloadCount: number
  tags: string[]
}

/** API 统一响应 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
  timestamp: number
}
