/**
 * Phase 3H / 3H-2 验收：
 * H1  关键 UI 文件 0 mojibake
 * H2  CI 编码扫描（phase3h2-encoding-scan）
 * H3  SubjectDetailPage 筛选项「全部/更多/收起」回归（静态）
 * H4–H6 taxonomy 册别 API（原 H2/H3，保留兼容）
 */
import fs from 'node:fs'
import path from 'node:path'
import { spawnSync } from 'node:child_process'
import { fileURLToPath } from 'node:url'
import { api, createReporter, login } from './phase9-test-utils.mjs'
import { scanEncoding, UI_CRITICAL_REL } from './phase3h2-encoding-scan.mjs'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const ROOT = path.resolve(__dirname, '..')
const PLATFORM_SRC = path.resolve(ROOT, '../k12-edu-platform/src')

function readPlatform(relPath) {
  return fs.readFileSync(path.join(PLATFORM_SRC, relPath), 'utf8')
}

async function main() {
  const r = createReporter()
  const adminH = await login('admin', process.env.PHASE9_ADMIN_PASSWORD || 'admin123')
  r.add('H0-admin-login', !!adminH?.Authorization, 'admin token ready')

  const criticalScan = scanEncoding({ criticalOnly: true })
  r.add(
    'H1-ui-critical-no-mojibake',
    criticalScan.bad.length === 0,
    `files=${UI_CRITICAL_REL.length};bad=${criticalScan.bad.length}`,
  )

  const encScan = spawnSync(process.execPath, [path.join(__dirname, 'phase3h2-encoding-scan.mjs')], {
    cwd: ROOT,
    encoding: 'utf8',
  })
  r.add(
    'H2-encoding-scan-full',
    encScan.status === 0,
    encScan.status === 0 ? `scanned ok` : (encScan.stdout || encScan.stderr || '').trim().slice(-200),
  )

  const filterRow = readPlatform('components/shared/FilterOptionRow.vue')
  const subjectPage = readPlatform('views/resource/SubjectDetailPage.vue')
  const resourceTypeBar = readPlatform('components/subject/ResourceTypeBar.vue')
  const h3Ok =
    filterRow.includes("moreLabel: '更多'") &&
    filterRow.includes("collapseLabel: '收起'") &&
    subjectPage.includes("activeResourceType.value === '全部'") &&
    subjectPage.includes(':scope-key="typeStatsScopeKey"') &&
    resourceTypeBar.includes('scopeKey')
  r.add('H3-filter-labels-subject-scope', h3Ok, 'FilterOptionRow + SubjectDetailPage scope-key')

  const newsZone = readPlatform('constants/newsZone.ts')
  const themeNav = readPlatform('constants/themeClassMeetingNav.ts')
  const filterCss = fs.readFileSync(
    path.resolve(ROOT, '../k12-edu-platform/src/assets/styles/filter-option.css'),
    'utf8',
  )
  const p3Ok =
    newsZone.includes('教育政策') &&
    themeNav.includes('THEME_CLASS_MEETING_MODULE') &&
    themeNav.includes('主题班会') &&
    filterCss.includes('.filter-option-btn')
  r.add('H3b-key-constants-utf8', p3Ok, 'newsZone + themeClassMeetingNav + filter-option.css')

  const gitattributesPath = path.resolve(ROOT, '../.gitattributes')
  const gitattrOk =
    fs.existsSync(gitattributesPath) &&
    fs.readFileSync(gitattributesPath, 'utf8').includes('encoding=utf-8')
  r.add('H3c-gitattributes', gitattrOk, '.gitattributes')

  const ciGate = fs.readFileSync(path.join(__dirname, 'ci-phase3g-gate.mjs'), 'utf8')
  r.add(
    'H3d-ci-encoding-gate',
    ciGate.includes('phase3h2-encoding-scan.mjs'),
    'ci-phase3g-gate.mjs',
  )

  const stagesRes = await api('GET', '/api/taxonomy/stages', { headers: adminH })
  const stages = stagesRes.json?.data || []
  const stageCodes = Array.isArray(stages) ? stages.map((x) => x.code) : []
  const hasCoreStages = ['primary', 'junior', 'senior'].every((s) => stageCodes.includes(s))
  r.add('H4-taxonomy-stages', hasCoreStages, `codes=${stageCodes.join(',')}`)

  const targets = ['primary', 'junior', 'senior']
  for (const stage of targets) {
    const vols = await api('GET', '/api/taxonomy/volumes', { headers: adminH, params: { stage } })
    const ok = vols.json?.code === 200 && Array.isArray(vols.json?.data)
    r.add(`H5-volumes-${stage}`, ok, `code=${vols.json?.code ?? vols.http};count=${vols.json?.data?.length ?? 0}`)
  }

  process.exit(r.summary('Phase3-H / 3H-2 Acceptance'))
}

main().catch((e) => {
  console.error(e)
  process.exit(1)
})
