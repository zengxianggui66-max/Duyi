/**
 * API 模块类型定义统一导出
 * 避免循环依赖，各模块的类型定义放在这里统一导出
 */

// ==================== Auth 类型 ====================

export interface LoginData {
  username: string
  password: string
  role?: string
}

export interface SmsLoginData {
  phone: string
  code: string
  role?: string
}

export interface RegisterData {
  username: string
  password: string
  email?: string
  nickname?: string
  role: string
  agreedToTerms: string
}

export interface SmsRegisterData {
  phone: string
  code: string
  role: string
  nickname?: string
  agreedToTerms: string
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  role: string
  memberLevel: string
  avatar?: string
  phone?: string
  email?: string
  createTime?: string
}

export interface LoginResult {
  token: string
  userId: number
  username: string
  role: string
  userInfo: UserInfo
}

// ==================== Resource 类型 ====================

export interface ResourceItem {
  id: number
  title: string
  description?: string
  fileUrl?: string
  fileSize?: number
  fileFormat?: string
  downloadCount?: number
  viewCount?: number
  rating?: number
  collectCount?: number
  uploaderId?: number
  uploaderName?: string
  uploadTime?: string
  gradeLevel?: string
  gradeNo?: string
  subject?: string
  resourceType?: string
  coverUrl?: string
  tags?: string[]
}

export interface ResourceListParams {
  gradeLevel?: string
  gradeNo?: string
  subject?: string
  resourceType?: string
  textbookVersion?: string
  examType?: string
  keyword?: string
  channelType?: string
  mediaType?: string
  isFree?: number
  current?: number
  size?: number
  sortField?: 'createTime' | 'downloadCount' | 'viewCount' | 'rating' | 'collectCount'
  sortOrder?: 'desc' | 'asc'
}

export interface ResourceStats {
  totalResources: number
  todayResources: number
  totalUsers: number
  totalDownloads: number
}

// ==================== EduResource 类型 ====================

export interface DimensionItem {
  id: number
  name: string
  icon?: string
}

export interface FilterOptions {
  stages: DimensionItem[]
  subjects: DimensionItem[]
  editions: DimensionItem[]
  grades: DimensionItem[]
  semesters: DimensionItem[]
  volumes: DimensionItem[]
  modules: DimensionItem[]
  resourceTypes: DimensionItem[]
  units: DimensionItem[]
}

export interface EduResourceItem {
  id: number
  title: string
  description?: string
  originalFilename?: string
  fileExt?: string
  ossBucket?: string
  ossObjectKey?: string
  ossUrl?: string
  fileSizeKb?: number
  downloadCount?: number
  viewCount?: number
  collectCount?: number
  status?: number
  uploaderId?: number
  uploadTime?: string
  updateTime?: string
  sort?: number
  remark?: string
  stageName?: string
  subjectName?: string
  editionName?: string
  gradeName?: string
  semesterName?: string
  volumeName?: string
  moduleName?: string
  resourceTypeName?: string
  unitName?: string
}

export interface EduResourceParams {
  stageName?: string
  subjectName?: string
  editionName?: string
  gradeName?: string
  semesterName?: string
  volumeName?: string
  moduleName?: string
  resourceTypeName?: string
  unitName?: string
  keyword?: string
  fileExt?: string
  status?: number
  current?: number
  size?: number
  sortField?: string
  sortOrder?: 'desc' | 'asc'
}

// ==================== News 类型 ====================

export interface NewsItem {
  id: number
  title: string
  summary?: string
  content?: string
  coverUrl?: string
  category?: string
  categoryName?: string
  author?: string
  publishTime?: string
  createTime?: string
  viewCount?: number
  commentCount?: number
  tags?: string
  subCategory?: string
  gradeLevels?: string
  regions?: string
  sourceName?: string
  sourceUrl?: string
  isTop?: number
  policyPoints?: string
  relatedTopicKeywords?: string
  consultEnabled?: number
  contentType?: string
}

export interface NewsListParams {
  category?: string
  gradeLevel?: string
  region?: string
  keyword?: string
  sort?: 'publishTime' | 'hot'
  current?: number
  size?: number
}

export interface RelatedLinkVO {
  title: string
  path: string
  type: string
  icon?: string
}

export interface NewsDetailVO {
  article: NewsItem
  relatedArticles: NewsItem[]
  relatedLinks: RelatedLinkVO[]
  collected: boolean
}

export interface NewsHomeVO {
  headlines: NewsItem[]
  channels: Record<string, NewsItem[]>
  hotKeywords: string[]
}

export interface ConsultLeadPayload {
  articleId?: number
  name: string
  phone: string
  grade?: string
  intentType?: 'trial' | 'material' | 'promotion' | 'general'
  remark?: string
  sourcePage?: string
}

export interface ArticleCreatePayload {
  title: string
  summary?: string
  content?: string
  coverUrl?: string
  category: string
  author?: string
  tags?: string
  gradeLevels?: string
  regions?: string
  policyPoints?: string
  relatedTopicKeywords?: string
  consultEnabled?: number
  contentType?: 'article' | 'video'
  publishTime?: string
  status?: 0 | 1
}

// ==================== Collect 类型 ====================

export interface CollectItem {
  id: number
  resourceId: number
  title: string
  fileUrl?: string
  coverUrl?: string
  collectTime: string
}

// ==================== Share 类型 ====================

export interface ShareRecord {
  id: number
  resourceId: number
  title: string
  shareUrl: string
  shareType: string
  sharePlatform?: string
  shareTime: string
}

// ==================== File 类型 ====================

export interface UploadResult {
  fileName: string
  fileUrl: string
  filePath: string
  fileSize: number
  fileFormat: string
  contentType: string
  isPreviewable: boolean
  previewType: string
  uploadRecordId: number
  message?: string
}

export interface UploadResourceResult extends UploadResult {
  resourceId: number
}

export interface FileFormat {
  extension: string
  name: string
  isSupported: boolean
  maxSizeMb: number
}

// ==================== Member 类型 ====================

export interface MemberInfo {
  id: number
  userId: number
  level: string
  expireTime?: string
  privileges: string[]
}

export type MemberLevel = 0 | 1 | 2 | 3

// ==================== Lesson 类型 ====================

export interface LessonGenerateParams {
  gradeLevel: string
  subject: string
  topic: string
  type: 'courseware' | 'lesson_plan' | 'study_guide'
}

export interface LessonHistoryItem {
  id: number
  gradeLevel: string
  subject: string
  topic: string
  type: string
  content?: string
  createdAt: string
}

// ==================== Exam 类型 ====================

export interface ExamGenerateParams {
  gradeLevel: string
  subject: string
  difficulty: number
  questionTypes: string[]
  totalScore?: number
  questionCount?: number
}

export interface ExamHistoryItem {
  id: number
  title: string
  gradeLevel: string
  subject: string
  difficulty: number
  totalScore: number
  questionCount: number
  createdAt: string
}

export interface ExamDetail {
  id: number
  title: string
  content: string
  answer: string
  analysis: string
  gradeLevel: string
  subject: string
  difficulty: number
  totalScore: number
}

// ==================== PrimaryChinese 类型 ====================

export interface PrimaryChineseItem {
  id: number
  title: string
  description?: string
  remark?: string
  originalFilename?: string
  fileExt?: string
  ossUrl?: string
  ossBucket?: string
  ossObjectKey?: string
  fileSizeKb?: number
  downloadCount?: number
  viewCount?: number
  lessonName?: string
  allowPreview?: number
  previewStatus?: number
  fileSafetyStatus?: number
  isFree?: number
  uploaderId?: number
  status?: number
  auditStatus?: number
  publishStatus?: number
  sort?: number
  stage?: string
  stageName?: string
  subject?: string
  subjectName?: string
  module?: string
  type?: string
  gradeName?: string
  edition?: string
  brandCode?: string
  subType?: string
  catalogNodeId?: number
  includeSubtree?: boolean
  /** 展示类型 Tab（可选；服务端会解析为 type/subType） */
  displayType?: string
  schemeId?: number
  unitName?: string
  uploadTime?: string
  lessonPlanJson?: string
}

export interface UnitTreeNode {
  name: string
  subUnits: string[]
}

export interface ResourceSuite {
  key: string
  icon: string
  title: string
  sub: string
  tag: string
  fileCount: number
  updateTime: string
  items: PrimaryChineseItem[]
}

export interface PrimaryChineseFilterOptions {
  gradeNames: string[]
  editions: string[]
  modules: string[]
  types: string[]
}

/** 上传位置联动筛选项（按学段/学科） */
export interface UploadFilterOptions {
  gradeNames: string[]
  editions: string[]
  modules: string[]
  types: string[]
}

export interface MyUploadStats {
  total: number
  published: number
  pending: number
  draft: number
  rejected: number
  offline: number
  totalViews: number
  totalDownloads: number
}

/** 上传者查看的资源驳回信息 */
export interface ResourceRejectInfo {
  resourceId: number
  reason?: string
  auditorName?: string
  rejectedAt?: string
}

export interface PrimaryChineseParams {
  stage?: string
  subject?: string
  module?: string
  type?: string
  gradeName?: string
  edition?: string
  brandCode?: string
  subType?: string
  catalogNodeId?: number
  includeSubtree?: boolean
  /** 展示类型 Tab（可选；服务端会解析为 type/subType） */
  displayType?: string
  schemeId?: number
  unitName?: string
  lessonName?: string
  keyword?: string
  fileExt?: string
  status?: number
  auditStatus?: number
  publishStatus?: number
  uploaderId?: number
  current?: number
  size?: number
  sortField?: string
  sortOrder?: 'desc' | 'asc'
}

// ==================== Sinology 类型 ====================

/** 国学阅读素材 */
export interface SinologyReadingItem {
  id: number
  resourceId?: number
  title: string
  dynasty?: string
  author?: string
  sourceBook?: string
  genre?: string
  content: string
  translation?: string
  appreciation?: string
  compositionHint?: string
  difficulty?: number
  keyPhrases?: string
  wordCount?: number
  audioUrl?: string
  videoUrl?: string
  status?: number
  sort?: number
  // 维度关联字段
  unitName?: string
  unitId?: number
  gradeName?: string
  editionName?: string
  volumeName?: string
  semesterName?: string
}

/** 作文训练素材 */
export interface CompositionTrainingItem {
  readingId: number
  title: string
  dynasty?: string
  author?: string
  genre?: string
  content: string
  translation?: string
  appreciation?: string
  compositionHint?: string
  difficulty?: number
  keyPhrases?: string
  wordCount?: number
  audioUrl?: string
  videoUrl?: string
}

/** 单元打包响应 */
export interface UnitBundleVO {
  unit: {
    unitId: number
    unitName: string
    gradeName?: string
    editionName?: string
    volumeName?: string
    semesterName?: string
    subjectName?: string
  }
  sinologyReadings: CompositionTrainingItem[]
  compositionTrainings: CompositionTrainingItem[]
  relatedSchools: SinologySchoolItem[]
}

/** 学校信息 */
export interface SinologySchoolItem {
  id: number
  name: string
  shortName?: string
  regionId?: number
  regionPath?: string
  regionName?: string
  schoolType?: string
  schoolLevel?: string
  tags?: string
}

/** 筛选枚举 */
export interface SinologyFilterEnums {
  editions: Array<{ value: string; label: string; cnt?: number }>
  units: Array<{ value: string; label: string; cnt?: number }>
  semesters: Array<{ value: string; label: string; cnt?: number }>
}

