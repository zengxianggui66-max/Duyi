/**
 * Phase 3G 旧接口下线前验收：
 * 1) 旧接口调用量统计可写入
 * 2) 管理端可读取旧接口调用量
 * 3) Phase 3I-B unified suites/module-stats 可达
 */
import { api, createReporter, login } from './phase9-test-utils.mjs'

const BROWSE_CMP_PARAMS = {
  stage: process.env.P3I_CMP_STAGE || '小学',
  subject: process.env.P3I_CMP_SUBJECT || '语文',
  edition: process.env.P3I_CMP_EDITION || '统编版',
}

async function readUnifiedPrimaryChineseEnabled() {
  const flags = await api('GET', '/api/public/feature-flags')
  const data = flags.json?.data || {}
  return !!(data.resourceUnifiedReadEnabled && data.primaryChineseUnifiedReadEnabled)
}

async function main() {
  const r = createReporter()
  const adminH = await login('admin', process.env.PHASE9_ADMIN_PASSWORD || 'admin123')
  const unifiedPrimaryOn = await readUnifiedPrimaryChineseEnabled()
  r.add('G0-admin-login', !!adminH?.Authorization, 'admin token ready')

  // 打旧接口请求，触发拦截器计数
  const old1 = await api('GET', '/api/topic/resources/page', { params: { current: 1, size: 1 } })
  const old2 = await api('GET', '/api/competition/resources/page', { params: { current: 1, size: 1 } })
  const oldBrowse = await api('GET', '/api/resources/browse', { params: { current: 1, size: 1 } })
  const oldBrowseSuites = await api('GET', '/api/resources/browse/suites', { params: BROWSE_CMP_PARAMS })
  const oldBrowseModuleStats = await api('GET', '/api/resources/browse/module-stats', { params: BROWSE_CMP_PARAMS })
  r.add('G1-legacy-api-reachable-topic', old1.json?.code === 200, `code=${old1.json?.code ?? old1.http}`)
  r.add('G2-legacy-api-reachable-competition', old2.json?.code === 200, `code=${old2.json?.code ?? old2.http}`)
  r.add('G2b-legacy-api-reachable-browse', oldBrowse.json?.code === 200, `code=${oldBrowse.json?.code ?? oldBrowse.http}`)
  r.add(
    'G2c-legacy-api-reachable-browse-suites',
    oldBrowseSuites.json?.code === 200,
    `code=${oldBrowseSuites.json?.code ?? oldBrowseSuites.http}`,
  )
  r.add(
    'G2d-legacy-api-reachable-browse-module-stats',
    oldBrowseModuleStats.json?.code === 200,
    `code=${oldBrowseModuleStats.json?.code ?? oldBrowseModuleStats.http}`,
  )

  const unifiedSuites = await api('GET', '/api/resources/suites', {
    params: { ...BROWSE_CMP_PARAMS, sourceType: 'primary_chinese' },
  })
  const unifiedModuleStats = await api('GET', '/api/resources/module-stats', {
    params: { ...BROWSE_CMP_PARAMS, sourceType: 'primary_chinese' },
  })
  r.add(
    'G7-unified-suites',
    unifiedPrimaryOn ? unifiedSuites.json?.code === 200 : unifiedSuites.json?.code === 503,
    unifiedPrimaryOn
      ? `code=${unifiedSuites.json?.code ?? unifiedSuites.http}`
      : `unifiedOff code=${unifiedSuites.json?.code ?? unifiedSuites.http}`,
  )
  r.add(
    'G8-unified-module-stats',
    unifiedPrimaryOn ? unifiedModuleStats.json?.code === 200 : unifiedModuleStats.json?.code === 503,
    unifiedPrimaryOn
      ? `code=${unifiedModuleStats.json?.code ?? unifiedModuleStats.http}`
      : `unifiedOff code=${unifiedModuleStats.json?.code ?? unifiedModuleStats.http}`,
  )

  const usage = await api('GET', '/api/admin/resource-main/legacy-api-usage', {
    headers: adminH,
    params: { days: 7 },
  })
  const rows = usage.json?.data || []
  const hasTopic = Array.isArray(rows) && rows.some((x) => x.apiPath === '/api/topic/resources/page' && (x.hitCount || 0) > 0)
  const hasCompetition =
    Array.isArray(rows) && rows.some((x) => x.apiPath === '/api/competition/resources/page' && (x.hitCount || 0) > 0)
  const hasBrowse =
    Array.isArray(rows) && rows.some((x) => x.apiPath === '/api/resources/browse' && (x.hitCount || 0) > 0)
  const hasBrowseSuites =
    Array.isArray(rows) && rows.some((x) => x.apiPath === '/api/resources/browse/suites' && (x.hitCount || 0) > 0)
  const hasBrowseModuleStats =
    Array.isArray(rows) &&
    rows.some((x) => x.apiPath === '/api/resources/browse/module-stats' && (x.hitCount || 0) > 0)
  r.add('G3-legacy-usage-endpoint', usage.json?.code === 200, `code=${usage.json?.code ?? usage.http}`)
  r.add('G4-legacy-usage-topic-counted', hasTopic, `rows=${rows.length}`)
  r.add('G5-legacy-usage-competition-counted', hasCompetition, `rows=${rows.length}`)
  r.add('G6-legacy-usage-browse-counted', hasBrowse, `rows=${rows.length}`)
  r.add('G6b-legacy-usage-browse-suites-counted', hasBrowseSuites, `rows=${rows.length}`)
  r.add('G6c-legacy-usage-browse-module-stats-counted', hasBrowseModuleStats, `rows=${rows.length}`)

  process.exit(r.summary('Phase3-G Acceptance'))
}

main().catch((e) => {
  console.error(e)
  process.exit(1)
})
