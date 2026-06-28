/**
 * Phase 3G 门禁：
 * 禁止新增前端对 legacy API 模块的直连调用（只允许基线文件列表）
 */
import fs from 'node:fs'
import path from 'node:path'

const ROOT = process.cwd()
const FRONTEND_SRC = path.resolve(ROOT, '../k12-edu-platform/src')

const LEGACY_IMPORT_RE = /from ['"]@\/api\/(primaryChinese|browse|topic|cultureStudy|competition)['"]/g

const ALLOWED_FILES = new Set([
  'api/resourceGateway.ts',
  'components/competition/CompetitionUploadDialog.vue',
  'views/feature/CompetitionZone.vue',
  'composables/useCompetitionZone.ts',
  'views/feature/TopicZone.vue',
  'views/feature/TraditionalCulture.vue',
  'composables/useCultureStudy.ts',
  'composables/useTopicZone.ts',
  'views/lesson/components/PrepSchoolList.vue',
  'views/lesson/components/PrepAlbumList.vue',
  'components/topic/TopicUploadDialog.vue',
  'components/culture/CultureUploadDialog.vue',
  'composables/useMyResources.ts',
  'composables/useMyDownloads.ts',
  'admin/components/AdminAuditPreviewDrawer.vue',
  'composables/useResourceDetail.ts',
  'composables/useBrowseTypeStats.ts',
  'composables/useResourceUploadForm.ts',
  'utils/uploadPersist.ts',
  'composables/useUnitDirectory.ts',
  'composables/useSuiteResourceActions.ts',
  'composables/useHomeSubjectVersions.ts',
  'views/user/Profile.vue',
  'components/user/ProfileHomePanel.vue',
  // Phase 3J — 专题 browse（迁移期允许，走 topicApi）
  'composables/useFeatureChannel.ts',
  'composables/useThemeClassMeeting.ts',
  'views/feature/ClassMeetingCategory.vue',
  'views/feature/FeaturePage.vue',
])

function walk(dir, out = []) {
  for (const name of fs.readdirSync(dir)) {
    const p = path.join(dir, name)
    const stat = fs.statSync(p)
    if (stat.isDirectory()) {
      walk(p, out)
    } else if (/\.(ts|tsx|js|jsx|vue)$/.test(name)) {
      out.push(p)
    }
  }
  return out
}

function toRel(absPath) {
  return absPath.replaceAll('\\', '/').replace(FRONTEND_SRC.replaceAll('\\', '/') + '/', '')
}

function main() {
  const files = walk(FRONTEND_SRC)
  const offenders = []
  for (const f of files) {
    const content = fs.readFileSync(f, 'utf8')
    LEGACY_IMPORT_RE.lastIndex = 0
    if (!LEGACY_IMPORT_RE.test(content)) {
      continue
    }
    const rel = toRel(f)
    if (!ALLOWED_FILES.has(rel)) {
      offenders.push(rel)
    }
  }

  if (offenders.length > 0) {
    console.error('[FAIL] Phase3G gate: found new legacy API direct imports:')
    for (const f of offenders) {
      console.error(`  - ${f}`)
    }
    process.exit(1)
  }

  console.log(`[PASS] Phase3G gate: no new legacy API direct imports (baseline=${ALLOWED_FILES.size})`)
}

main()
