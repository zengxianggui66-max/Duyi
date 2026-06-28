/**
 * 常量模块统一导出
 * 使用方式:
 * import { SUBJECTS, GRADES } from '@/constants'
 */

// 学科
export * from './subjects'

// 年级/学段
export * from './grades'
export * from './stages'

// 通用常量
export * from './resourceConstants'

// 资源类型
export * from './resource-types'

// 题型相关
export * from './exam-types'

// 资源浏览
export * from './resourceBrowseTags'

// 新闻/专题/竞赛区域
export * from './newsZone'
export * from './topicZone'
export * from './competitionZone'

// 我的资源
export * from './myResources'

// 上传相关
export * from './uploadDefaults'
export * from './uploadLabels'
export * from './uploadResourceSubTypes'
export * from './uploadRecommend'
// uploadOptions 已降级为 API 离线兜底，不在此 re-export；请使用 taxonomySource / dictionarySource

// 版本信息
export * from './versions'

// 工具函数
export * from './utils'

// 其他
export * from './colors'
export * from './zone-cards'
export * from './featureChannels'
export * from './featureChannelRegistry'
