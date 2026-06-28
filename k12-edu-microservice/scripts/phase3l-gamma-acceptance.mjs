/**
 * Phase 3L-γ 验收：班会 Admin CRUD + 资讯审核 + 3K-β resource_main upsert
 *
 * 用法（在 k12-edu-microservice 目录）:
 *   node scripts/phase3l-gamma-acceptance.mjs
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createReporter } from './phase9-test-utils.mjs'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const ROOT = path.resolve(__dirname, '..')
const PLATFORM_SRC = path.resolve(ROOT, '../k12-edu-platform/src')
const RESOURCE_SRC = path.join(ROOT, 'k12-resource/src/main/java/com/k12/resource')
const ARTICLE_SRC = path.join(ROOT, 'k12-article/src/main/java/com/k12/article')

function readPlatform(rel) {
  return fs.readFileSync(path.join(PLATFORM_SRC, rel), 'utf8')
}

function readResource(rel) {
  return fs.readFileSync(path.join(RESOURCE_SRC, rel), 'utf8')
}

function readArticle(rel) {
  return fs.readFileSync(path.join(ARTICLE_SRC, rel), 'utf8')
}

function main() {
  const r = createReporter()

  const themeOps = readPlatform('admin/views/content/ThemeClassMeetingOps.vue')
  r.add(
    'Lγ1-theme-ops-tab-crud',
    themeOps.includes('adminOpsChannelApi.updateTab') &&
      themeOps.includes('adminOpsChannelApi.createAlbum') &&
      themeOps.includes('adminOpsChannelApi.deleteAlbum') &&
      themeOps.includes("admin:home:edit"),
    'ThemeClassMeetingOps.vue',
  )

  const newsOps = readPlatform('admin/views/content/NewsOps.vue')
  r.add(
    'Lγ2-news-audit-ui',
    newsOps.includes('待审核') &&
      newsOps.includes(':value="2"') &&
      newsOps.includes('toggleStatus(row, 3)') &&
      newsOps.includes('isFeatured'),
    'NewsOps.vue',
  )

  const articleSvc = readArticle('service/impl/ArticleServiceImpl.java')
  r.add(
    'Lγ3-article-default-pending',
    articleSvc.includes('dto.getStatus() != null ? dto.getStatus() : 2') &&
      articleSvc.includes('status < 0 || status > 3'),
    'ArticleServiceImpl.java',
  )

  const upsertSvc = readResource('service/ResourceMainUpsertService.java')
  r.add(
    'Lγ4-resource-main-upsert-service',
    upsertSvc.includes('upsertFromEduResource') &&
      upsertSvc.includes('EduResourceSourceAdapter.SOURCE_TYPE'),
    'ResourceMainUpsertService.java',
  )

  const syncHook = readResource('search/SearchIndexSyncHook.java')
  r.add(
    'Lγ5-hook-calls-upsert',
    syncHook.includes('resourceMainUpsertService.upsertFromEduResource'),
    'SearchIndexSyncHook.java',
  )

  const writeSvc = readResource('service/ResourceWriteService.java')
  r.add(
    'Lγ6-write-service-hook',
    writeSvc.includes('searchIndexSyncHook.afterEduResourceChanged'),
    'ResourceWriteService.java',
  )

  const domainDoc = path.join(ROOT, 'docs/Phase3L-domain-decision.md')
  r.add(
    'Lγ7-domain-doc-gamma',
    fs.existsSync(domainDoc) && fs.readFileSync(domainDoc, 'utf8').includes('ResourceMainUpsertService'),
    'Phase3L-domain-decision.md',
  )

  process.exit(r.summary('Phase3-L-γ Acceptance'))
}

main()
