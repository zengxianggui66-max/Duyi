/**
 * Phase 3L-α 验收：双轨路由 redirect + domain 文档
 *
 * 用法（在 k12-edu-microservice 目录）:
 *   node scripts/phase3l-alpha-acceptance.mjs
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { createReporter } from './phase9-test-utils.mjs'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const ROOT = path.resolve(__dirname, '..')
const PLATFORM_SRC = path.resolve(ROOT, '../k12-edu-platform/src')

function readPlatform(rel) {
  return fs.readFileSync(path.join(PLATFORM_SRC, rel), 'utf8')
}

function main() {
  const r = createReporter()
  const router = readPlatform('router/index.ts')
  const latest = readPlatform('views/home/components/LatestContent.vue')
  const registry = readPlatform('constants/featureChannelRegistry.ts')
  const docPath = path.join(ROOT, 'docs/Phase3L-domain-decision.md')

  r.add(
    'Lα1-router-banhui-redirect',
    router.includes("path: '/feature/banhui'") && router.includes("redirect: '/theme-class-meeting'"),
    'router/index.ts',
  )
  r.add(
    'Lα2-router-zhuanti-redirect',
    router.includes("path: '/feature/zhuanti'") && router.includes("redirect: '/topic'"),
    'router/index.ts',
  )
  r.add(
    'Lα3-latest-content-more-path',
    latest.includes("morePath: '/topic'") && !latest.includes("morePath: '/feature/topic'"),
    'LatestContent.vue',
  )
  r.add(
    'Lα4-registry-theme-meeting-path',
    registry.includes("path: '/theme-class-meeting'"),
    'featureChannelRegistry.ts',
  )
  r.add(
    'Lα5-domain-doc',
    fs.existsSync(docPath) && fs.readFileSync(docPath, 'utf8').includes('edu_resource'),
    'docs/Phase3L-domain-decision.md',
  )

  process.exit(r.summary('Phase3-L-α Acceptance'))
}

main()
