#!/usr/bin/env node
/**
 * Phase 3G + 3H-2 + 3K CI 门禁
 * - 3G: 禁止新增前端 legacy API 直连
 * - 3H-2: UTF-8 / mojibake 全库扫描（无需 DB）
 * - 3K: 禁止新增 Controller 直连 COMPAT Entity
 */
import { spawnSync } from 'node:child_process'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const root = path.resolve(__dirname, '..')

const gates = [
  ['phase3g-no-new-legacy-calls-gate.mjs', 'Phase3G frontend legacy imports'],
  ['phase3h2-encoding-scan.mjs', 'Phase3H-2 UTF-8 encoding scan'],
  ['phase3k-compat-controller-gate.mjs', 'Phase3K compat Controller entity'],
]

let failed = false
for (const [script, label] of gates) {
  const result = spawnSync(process.execPath, [path.join(__dirname, script)], {
    stdio: 'inherit',
    cwd: root,
  })
  if ((result.status ?? 1) !== 0) {
    console.error(`[FAIL] ${label}`)
    failed = true
  }
}

process.exit(failed ? 1 : 0)
