/**
 * Phase 3K-β 验收：COMPAT 源 resource_main 实时 upsert
 *
 * 用法（在 k12-edu-microservice 目录）:
 *   node scripts/phase3k-beta-acceptance.mjs
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createReporter } from './phase9-test-utils.mjs'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const RESOURCE_SRC = path.resolve(__dirname, '../k12-resource/src/main/java/com/k12/resource')

function readResource(rel) {
  return fs.readFileSync(path.join(RESOURCE_SRC, rel), 'utf8')
}

function main() {
  const r = createReporter()
  const upsert = readResource('service/ResourceMainUpsertService.java')
  const hook = readResource('search/SearchIndexSyncHook.java')
  const test = fs.readFileSync(
    path.resolve(__dirname, '../k12-resource/src/test/java/com/k12/resource/service/ResourceMainUpsertServiceTest.java'),
    'utf8',
  )

  r.add(
    'Kβ1-topic-upsert',
    upsert.includes('upsertFromTopicResource') && upsert.includes('TopicSourceAdapter.SOURCE_TYPE'),
    'ResourceMainUpsertService.java',
  )
  r.add(
    'Kβ2-culture-upsert',
    upsert.includes('upsertFromCultureResource') && upsert.includes('CultureSourceAdapter.SOURCE_TYPE'),
    'ResourceMainUpsertService.java',
  )
  r.add(
    'Kβ3-competition-upsert',
    upsert.includes('upsertFromCompetitionResource') && upsert.includes('CompetitionSourceAdapter.SOURCE_TYPE'),
    'ResourceMainUpsertService.java',
  )
  r.add(
    'Kβ4-hook-topic-culture-competition',
    hook.includes('upsertFromTopicResource') &&
      hook.includes('upsertFromCultureResource') &&
      hook.includes('upsertFromCompetitionResource'),
    'SearchIndexSyncHook.java',
  )
  r.add(
    'Kβ5-compat-status-mapper',
    upsert.includes('mapCompatLegacyStatus') && test.includes('mapCompatLegacyStatus_published'),
    'ResourceMainUpsertServiceTest.java',
  )

  process.exit(r.summary('Phase3-K-β Acceptance'))
}

main()
