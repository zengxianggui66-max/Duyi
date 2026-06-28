/**
 * 资源相关常量
 */

/** 默认资源类型 */
export const DEFAULT_RESOURCE_TYPE = '全部'

/** 默认教学类型 */
export const DEFAULT_TEACHING_TYPE = '课件'

/** 同步备课模块名 */
export const SYNC_PREP_NAME = '同步备课'

/** 期末复习模块名 */
export const FINAL_REVIEW_NAME = '期末复习'

/** 默认模块 */
export const DEFAULT_MODULE = '同步备课'

/** 教师用书相关类型 */
export const TEACHER_BOOK_TYPES = ['课堂实录', '讲义', '说课稿', '教学设计', '预习', '教学总结', '逐字稿', '公开课']

/** 学段默认学科映射 */
export const STAGE_TO_DEFAULT_SUBJECT: Record<string, string> = {
  preschool: 'chinese',
  primary: 'chinese',
  junior: 'chinese',
  senior: 'chinese',
  art: 'art',
  dance: 'dance',
}
