/**
 * Phase 3J 内容中心统一验收
 *
 * J1  admin 可按 sourceType 筛五类资源
 * J2  内容中心 Admin API + 前台专题专辑可读
 * J3  班会 browse 可达 + 前台去 mock
 * J4  资讯 Admin 发布 → 前台 /news 可见（含 E2E）
 *
 * 用法（在 k12-edu-microservice 目录）:
 *   node scripts/phase3j-content-center-acceptance.mjs
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { api, createReporter, login } from './phase9-test-utils.mjs'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const FRONTEND_ROOT = path.resolve(__dirname, '../../k12-edu-platform')

const SOURCE_TYPES = [
  'primary_chinese',
  'topic_resource',
  'culture_resource',
  'competition_resource',
  'edu_resource',
]

function readFile(relPath) {
  return fs.readFileSync(path.join(FRONTEND_ROOT, relPath), 'utf8')
}

function fileIncludes(relPath, needles) {
  const content = readFile(relPath)
  return needles.every((n) => content.includes(n))
}

async function main() {
  const r = createReporter()
  const adminH = await login('admin', process.env.PHASE9_ADMIN_PASSWORD || 'admin123')
  r.add('J0-admin-login', !!adminH?.Authorization, 'admin token ready')

  for (const sourceType of SOURCE_TYPES) {
    const res = await api('GET', '/api/admin/resource-main', {
      headers: adminH,
      params: { current: 1, size: 1, sourceType },
    })
    const ok = res.json?.code === 200 && Array.isArray(res.json?.data?.records)
    r.add(`J1-list-${sourceType}`, ok, `code=${res.json?.code ?? res.http}`)
  }

  const constantsOk = fileIncludes('src/admin/constants/resourceSourceTypes.ts', SOURCE_TYPES)
  r.add('J1-ui-source-type-constants', constantsOk, 'resourceSourceTypes.ts')

  const listOk = fileIncludes('src/admin/views/resources/ResourceList.vue', [
    'RESOURCE_SOURCE_TYPE_OPTIONS',
    '@/admin/constants/resourceSourceTypes',
  ])
  r.add('J1-ui-resource-list', listOk, 'ResourceList.vue')

  const qualityShellOk = fileIncludes('src/admin/views/quality/QualityShell.vue', ['legacy-api-usage', '旧接口调用'])
  r.add('J1-quality-legacy-tab', qualityShellOk, 'QualityShell.vue')

  const channelOk = fileIncludes('src/admin/views/home-config/HomeConfig.vue', ['频道中心', '专题频道'])
  r.add('J1-channel-center-copy', channelOk, 'HomeConfig.vue')

  // J2: 内容中心 Admin + 前台
  const contentShellOk = fileIncludes('src/admin/views/content/ContentShell.vue', [
    '专题资料',
    'admin/content/topic',
  ])
  r.add('J2-ui-content-shell', contentShellOk, 'ContentShell.vue')

  const topicAdmin = await api('GET', '/api/admin/topic/albums', {
    headers: adminH,
    params: { current: 1, size: 1 },
  })
  r.add(
    'J2-topic-admin-list',
    topicAdmin.json?.code === 200 && Array.isArray(topicAdmin.json?.data?.records),
    `code=${topicAdmin.json?.code ?? topicAdmin.http}`,
  )

  const cultureAdmin = await api('GET', '/api/admin/culture/packages', {
    headers: adminH,
    params: { current: 1, size: 1 },
  })
  r.add(
    'J2-culture-admin-list',
    cultureAdmin.json?.code === 200 && Array.isArray(cultureAdmin.json?.data?.records),
    `code=${cultureAdmin.json?.code ?? cultureAdmin.http}`,
  )

  const competitionAdmin = await api('GET', '/api/admin/competition/packages', {
    headers: adminH,
    params: { current: 1, size: 1 },
  })
  r.add(
    'J2-competition-admin-list',
    competitionAdmin.json?.code === 200 && Array.isArray(competitionAdmin.json?.data?.records),
    `code=${competitionAdmin.json?.code ?? competitionAdmin.http}`,
  )

  const topicPublic = await api('GET', '/api/topic/albums/page', { params: { current: 1, size: 1 } })
  r.add('J2-topic-public-list', topicPublic.json?.code === 200, `code=${topicPublic.json?.code ?? topicPublic.http}`)

  // J3: 班会 browse + 前台 API 接线
  const classMeeting = await api('GET', '/api/resources/browse', {
    params: { current: 1, size: 1, module: '主题班会' },
  })
  r.add('J3-class-meeting-browse', classMeeting.json?.code === 200, `code=${classMeeting.json?.code ?? classMeeting.http}`)

  const themeOpsOk = fileIncludes('src/admin/views/content/ThemeClassMeetingOps.vue', ['banhui', 'edu_resource'])
  r.add('J3-ui-theme-class-meeting-ops', themeOpsOk, 'ThemeClassMeetingOps.vue')

  const themeComposableOk = fileIncludes('src/composables/useThemeClassMeeting.ts', [
    'browseApi',
    'fetchChannelBootstrap',
    'THEME_CLASS_MEETING_MODULE',
  ])
  r.add('J3-ui-theme-composable', themeComposableOk, 'useThemeClassMeeting.ts')

  const themePageOk = fileIncludes('src/views/feature/ThemeClassMeeting.vue', [
    'useThemeClassMeeting',
  ]) && !readFile('src/views/feature/ThemeClassMeeting.vue').includes('downloadData = {')
  r.add('J3-ui-theme-page-api', themePageOk, 'ThemeClassMeeting.vue')

  const categoryApiOk = fileIncludes('src/views/feature/ClassMeetingCategory.vue', [
    'browseApi',
    'THEME_CLASS_MEETING_MODULE',
  ]) && !readFile('src/views/feature/ClassMeetingCategory.vue').includes('generateCourseData')
  r.add('J3-ui-category-browse', categoryApiOk, 'ClassMeetingCategory.vue')

  const featureTopicsOk = fileIncludes('src/views/feature/FeaturePage.vue', [
    'topicApi.listAlbums',
    'loadTopics',
  ])
  r.add('J3-ui-feature-topics-api', featureTopicsOk, 'FeaturePage.vue')

  const channelSideOk = fileIncludes('src/composables/useFeatureChannel.ts', [
    'loadHotDownloads',
    'loadLatestUploads',
    'fetchBanhuiResourcePage',
    'fetchZhuantiResourcePage',
  ]) && !readFile('src/composables/useFeatureChannel.ts').includes('const weekData = [')
  r.add('J3-ui-feature-channel-side', channelSideOk, 'useFeatureChannel.ts')

  const categoryRouteOk = readFile('src/router/index.ts').includes("name: 'ClassMeetingCategory'")
  r.add('J3-ui-category-route', categoryRouteOk, 'router/index.ts')

  // J4: 资讯
  const newsAdmin = await api('GET', '/api/admin/articles', {
    headers: adminH,
    params: { current: 1, size: 1 },
  })
  r.add(
    'J4-news-admin-list',
    newsAdmin.json?.code === 200 && Array.isArray(newsAdmin.json?.data?.records),
    `code=${newsAdmin.json?.code ?? newsAdmin.http}`,
  )

  const newsPublic = await api('GET', '/api/news/list', { params: { current: 1, size: 1 } })
  r.add('J4-news-public-list', newsPublic.json?.code === 200, `code=${newsPublic.json?.code ?? newsPublic.http}`)

  const newsOpsOk = fileIncludes('src/admin/views/content/NewsOps.vue', ['adminContentApi', '发布资讯'])
  r.add('J4-ui-news-ops', newsOpsOk, 'NewsOps.vue')

  const newsEditOk = fileIncludes('src/admin/views/content/NewsOps.vue', ['getArticle']) &&
    fileIncludes('src/admin/api/content.ts', ['getArticle'])
  r.add('J4-ui-news-edit-fetch', newsEditOk, 'NewsOps + content.ts')

  const articleSvcPath = path.join(__dirname, '../k12-article/src/main/java/com/k12/article/service/impl/ArticleServiceImpl.java')
  const articleSvcSrc = fs.existsSync(articleSvcPath)
    ? fs.readFileSync(articleSvcPath, 'utf8')
    : ''

  const newsBetaOk = fileIncludes('src/admin/views/content/NewsOps.vue', [
    'NEWS_CATEGORY_OPTIONS',
    'openPreview',
    'openPreview(',
  ]) && fileIncludes('src/views/news/NewsList.vue', [
    'NEWS_LIST_CATEGORIES',
    'coverGradient',
    'coverUrl',
  ])
  r.add('J4-beta-channel-preview-cover', newsBetaOk, 'NewsOps + NewsList + newsZone')

  const newsGammaUiOk = fileIncludes('src/admin/views/content/NewsOps.vue', [
    'fileApi.upload',
    'isTop',
    'isFeatured',
  ])
  r.add('J4-gamma-ui-upload-top-featured', newsGammaUiOk, 'NewsOps cover + top/featured')

  const syncSvcPath = path.join(__dirname, '../k12-resource/src/main/java/com/k12/resource/search/SearchDocumentSyncService.java')
  const syncSvcSrc = fs.existsSync(syncSvcPath) ? fs.readFileSync(syncSvcPath, 'utf8') : ''
  const articleDtoPath = path.join(__dirname, '../k12-common/src/main/java/com/k12/common/dto/ArticleCreateDTO.java')
  const articleDtoSrc = fs.existsSync(articleDtoPath) ? fs.readFileSync(articleDtoPath, 'utf8') : ''
  const newsGammaBeOk = syncSvcSrc.includes('syncArticleById') &&
    articleDtoSrc.includes('isTop') &&
    articleSvcSrc.includes('articleSearchIndexClient')
  r.add('J4-gamma-be-search-top', newsGammaBeOk, 'SearchDocumentSync + ArticleCreateDTO + ArticleServiceImpl')
  const detailPublishedOnly = articleSvcSrc.includes('getStatus() != 1')
  r.add('J4-be-detail-published-only', detailPublishedOnly, 'ArticleServiceImpl.getDetail')

  // J4 E2E: Admin 发布 → 前台 list 可见 → 下线不可见 → 详情拒绝
  const e2eTitle = `J4-E2E-${Date.now()}`
  const e2eBody = {
    title: e2eTitle,
    summary: 'Phase 3J J4 acceptance article',
    content: '<p>J4 E2E body</p>',
    category: 'policy',
    author: 'acceptance-bot',
    status: 1,
  }
  const created = await api('POST', '/api/admin/articles', { headers: adminH, body: e2eBody })
  const articleId = created.json?.data
  r.add(
    'J4-e2e-create',
    created.json?.code === 200 && typeof articleId === 'number' && articleId > 0,
    `code=${created.json?.code ?? created.http} id=${articleId ?? 'none'}`,
  )

  let e2eId = articleId
  if (e2eId) {
    const pubList = await api('GET', '/api/news/list', {
      params: { keyword: e2eTitle, current: 1, size: 20 },
    })
    const inList = (pubList.json?.data?.records || []).some(
      (row) => row.id === e2eId || row.title === e2eTitle,
    )
    r.add('J4-e2e-public-list', inList, `list code=${pubList.json?.code ?? pubList.http}`)

    const pubDetail = await api('GET', `/api/news/detail/${e2eId}`)
    r.add(
      'J4-e2e-public-detail',
      pubDetail.json?.code === 200 && pubDetail.json?.data?.article?.title === e2eTitle,
      `detail code=${pubDetail.json?.code ?? pubDetail.http}`,
    )

    const offline = await api('PUT', `/api/admin/articles/${e2eId}/status`, {
      headers: adminH,
      params: { status: 0 },
    })
    r.add('J4-e2e-offline', offline.json?.code === 200, `code=${offline.json?.code ?? offline.http}`)

    const afterList = await api('GET', '/api/news/list', {
      params: { keyword: e2eTitle, current: 1, size: 20 },
    })
    const goneFromList = !(afterList.json?.data?.records || []).some((row) => row.id === e2eId)
    r.add('J4-e2e-not-in-list', goneFromList, `list after offline code=${afterList.json?.code ?? afterList.http}`)

    const draftDetail = await api('GET', `/api/news/detail/${e2eId}`)
    r.add(
      'J4-e2e-detail-blocked',
      draftDetail.json?.code !== 200,
      `detail after offline code=${draftDetail.json?.code ?? draftDetail.http}`,
    )

    const removed = await api('DELETE', `/api/admin/articles/${e2eId}`, { headers: adminH })
    r.add('J4-e2e-cleanup', removed.json?.code === 200, `delete code=${removed.json?.code ?? removed.http}`)
    e2eId = null
  } else {
    r.add('J4-e2e-public-list', false, 'skipped: create failed')
    r.add('J4-e2e-public-detail', false, 'skipped: create failed')
    r.add('J4-e2e-offline', false, 'skipped: create failed')
    r.add('J4-e2e-not-in-list', false, 'skipped: create failed')
    r.add('J4-e2e-detail-blocked', false, 'skipped: create failed')
    r.add('J4-e2e-cleanup', false, 'skipped: create failed')
  }

  const exitCode = r.summary('Phase 3J content center acceptance')
  process.exit(exitCode)
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
