/**
 * Phase 3F 前台灰度迁移验收
 * - 新旧比对（沿用 P0 对比脚本）
 * - 统一资源主链路验收（沿用 3D/3E 脚本）
 */
import { spawnSync } from 'node:child_process'

function runStep(label, args) {
  const result = spawnSync('node', args, { stdio: 'inherit', shell: process.platform === 'win32' })
  if (result.status !== 0) {
    console.error(`[FAIL] ${label}`)
    process.exit(result.status || 1)
  }
  console.log(`[PASS] ${label}`)
}

function main() {
  runStep('F1-compare-old-vs-new', ['scripts/phase3p0-compare-resource-apis.mjs'])
  runStep('F2-unified-api-acceptance', ['scripts/phase3d-3e-acceptance-test.mjs'])
  console.log('\n=== Phase3-F Acceptance: PASS ===')
}

main()
