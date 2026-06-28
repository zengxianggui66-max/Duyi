/**
 * Phase 3L-β 验收：banhui/zhuanti 与 canonical 同源
 *
 * 用法（在 k12-edu-microservice 目录）:
 *   node scripts/phase3l-beta-acceptance.mjs
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { api, createReporter } from './phase9-test-utils.mjs'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const FRONTEND_ROOT = path.resolve(__dirname, '../../k12-edu-platform')

function readFrontend(rel) {
  return fs.readFileSync(path.join(FRONTEND_ROOT, rel), 'utf8')
}

async function main() {
  const r = createReporter()

  const router = readFrontend('src/router/index.ts')
  r.add(
    'Lβ1-router-redirects',
    router.includes("path: '/feature/banhui'") &&
      router.includes("redirect: '/theme-class-meeting'") &&
      router.includes("path: '/feature/zhuanti'") &&
      router.includes("redirect: '/topic'"),
    'router/index.ts',
  )

  const channelList = readFrontend('src/composables/useChannelResourceList.ts')
  r.add(
    'Lβ2-channel-resource-list-module',
    channelList.includes('fetchBanhuiResourcePage') &&
      channelList.includes('fetchZhuantiResourcePage') &&
      channelList.includes('THEME_CLASS_MEETING_MODULE'),
    'useChannelResourceList.ts',
  )

  const featureChannel = readFrontend('src/composables/useFeatureChannel.ts')
  r.add(
    'Lβ3-feature-channel-delegate',
    featureChannel.includes('fetchBanhuiResourcePage') &&
      featureChannel.includes('fetchZhuantiResourcePage') &&
      featureChannel.includes('resolveChannelDetailPath'),
    'useFeatureChannel.ts',
  )

  const themeOps = readFrontend('src/admin/views/content/ThemeClassMeetingOps.vue')
  r.add(
    'Lβ4-theme-ops-copy',
    !themeOps.includes('3J-3') && themeOps.includes('同源 browse API'),
    'ThemeClassMeetingOps.vue',
  )

  const banhuiBrowse = await api('GET', '/api/resources/browse', {
    params: { current: 1, size: 1, module: '主题班会' },
  })
  const banhuiOk = banhuiBrowse.json?.code === 200
  const banhuiTotal = banhuiBrowse.json?.data?.total ?? -1
  r.add(
    'Lβ5-banhui-browse-api',
    banhuiOk,
    `code=${banhuiBrowse.json?.code ?? banhuiBrowse.http};total=${banhuiTotal}`,
  )

  const topicPage = await api('GET', '/api/topic/resources/page', {
    params: { current: 1, size: 1 },
  })
  const topicOk = topicPage.json?.code === 200
  const topicTotal = topicPage.json?.data?.total ?? -1
  r.add(
    'Lβ6-zhuanti-topic-api',
    topicOk,
    `code=${topicPage.json?.code ?? topicPage.http};total=${topicTotal}`,
  )

  const topicDedicated = await api('GET', '/api/topic/resources/page', {
    params: { current: 1, size: 5, sortField: 'downloadCount', sortOrder: 'desc' },
  })
  const unifiedTopic = await api('GET', '/api/resources/page', {
    params: { current: 1, size: 5, sourceType: 'topic_resource', sortField: 'downloadCount', sortOrder: 'desc' },
  })
  const topicRecords = topicDedicated.json?.data?.records?.length ?? 0
  const unifiedRecords = unifiedTopic.json?.data?.records?.length ?? 0
  r.add(
    'Lβ7-topic-unified-readable',
    topicDedicated.json?.code === 200 && unifiedTopic.json?.code === 200,
    `topicRecords=${topicRecords};unifiedRecords=${unifiedRecords}`,
  )

  process.exit(r.summary('Phase3-L-β Acceptance'))
}

main().catch((e) => {
  console.error(e)
  process.exit(1)
})
