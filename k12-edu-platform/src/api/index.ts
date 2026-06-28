/**
 * API 模块统一导出
 * 使用方式:
 * import { authApi, resourceApi } from '@/api'
 * 或者按需导入:
 * import { authApi } from '@/api/auth'
 */

// 导出 request 实例和基础类型
export { request } from './request'
export type { ApiResult, PageData } from './request'

// 导出各模块 API
export { authApi } from './auth'
export { resourceApi } from './resource'
export { eduResourceApi } from './eduResource'
export { searchApi } from './search'
export { newsApi } from './news'
export { collectApi } from './collect'
export { downloadApi } from './download'
export { viewApi } from './view'
export { shareApi } from './share'
export { fileApi } from './file'
export type { UploadResult, UploadResourceResult, FilePreviewInfo } from './file'
export { memberApi } from './member'
export { lessonApi } from './lesson'
export { examApi } from './exam'
export { prepApi } from './prep'
export { primaryChineseApi } from './primaryChinese'
export { brandApi } from './brand'
export { catalogApi } from './catalog'
export { taxonomyApi } from './taxonomy'
export { homeSubjectNavApi } from './homeSubjectNav'
export type { HomeSubjectNavPayload } from './homeSubjectNav'
export { browseApi } from './browse'
export { resourcesApi } from './resources'
export type { ResourceWritePayload, ResourceFilePayload } from './resources'
export { cultureStudyApi } from './cultureStudy'
export { competitionApi } from './competition'
export { resourceGateway } from './resourceGateway'
export { homePanelApi, HOME_LIMIT } from './homePanel'
export type {
  HomePanelItem,
  HomePanelListResult,
  HomeFuncChannelDto,
  HomeFuncChannelsResult,
  PromotionExamTabDto,
} from './homePanel'
export { sinologyApi } from './sinology'

// 兼容旧代码：导出命名空间对象作为 default
// @deprecated 请逐步改用命名导入方式: import { authApi, resourceApi } from '@/api'
import * as apiNamespace from './request'
import * as authNamespace from './auth'
import * as resourceNamespace from './resource'
import * as eduResourceNamespace from './eduResource'
import * as searchNamespace from './search'
import * as newsNamespace from './news'
import * as collectNamespace from './collect'
import * as shareNamespace from './share'
import * as fileNamespace from './file'
import * as memberNamespace from './member'
import * as lessonNamespace from './lesson'
import * as examNamespace from './exam'
import * as primaryChineseNamespace from './primaryChinese'
import * as sinologyNamespace from './sinology'

const api = {
  ...apiNamespace,
  ...authNamespace,
  ...resourceNamespace,
  ...eduResourceNamespace,
  ...searchNamespace,
  ...newsNamespace,
  ...collectNamespace,
  ...shareNamespace,
  ...fileNamespace,
  ...memberNamespace,
  ...lessonNamespace,
  ...examNamespace,
  ...primaryChineseNamespace,
  ...sinologyNamespace,
}

export default api

// 重新导出各模块的类型定义，便于外部使用
export * from './types'

