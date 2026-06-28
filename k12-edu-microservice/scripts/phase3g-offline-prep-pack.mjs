/**
 * Phase 3G 下线准备包总入口
 * 1) 旧接口 7/14 天趋势
 * 2) 禁新增 legacy 直连门禁
 */
import { spawnSync } from 'node:child_process'

function run(label, args) {
  const r = spawnSync('node', args, { stdio: 'inherit', shell: process.platform === 'win32' })
  if (r.status !== 0) {
    console.error(`[FAIL] ${label}`)
    process.exit(r.status || 1)
  }
  console.log(`[PASS] ${label}`)
}

function main() {
  run('G-PACK-trend', ['scripts/phase3g-legacy-usage-trend.mjs'])
  run('G-PACK-no-new-legacy-calls-gate', ['scripts/phase3g-no-new-legacy-calls-gate.mjs'])
  console.log('\n=== Phase3-G Offline Prep Pack: PASS ===')
}

main()
