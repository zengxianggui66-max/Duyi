/**
 * Phase 3H-2 L3 — 检测非 UTF-8 文件并可选 GB18030→UTF-8 转换
 *
 * 用法（在 k12-edu-microservice 目录）:
 *   node scripts/phase3h2-convert-encoding.mjs              # 列出需关注文件
 *   node scripts/phase3h2-convert-encoding.mjs --fix      # 尝试 GB18030 转 UTF-8
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { scanEncoding, SCAN_DIRS } from './phase3h2-encoding-scan.mjs'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const MICRO_ROOT = path.resolve(__dirname, '..')

const TEXT_EXT = new Set([
  '.java', '.sql', '.ts', '.tsx', '.js', '.jsx', '.mjs', '.vue',
  '.md', '.properties', '.xml', '.yml', '.yaml',
])

const SKIP_DIRS = new Set(['node_modules', 'dist', 'target', '.git'])

function walkFiles(dir, out = []) {
  if (!fs.existsSync(dir)) return out
  for (const name of fs.readdirSync(dir)) {
    if (SKIP_DIRS.has(name)) continue
    const p = path.join(dir, name)
    const stat = fs.statSync(p)
    if (stat.isDirectory()) walkFiles(p, out)
    else if (TEXT_EXT.has(path.extname(p).toLowerCase())) out.push(p)
  }
  return out
}

function isValidUtf8(buf) {
  try {
    new TextDecoder('utf-8', { fatal: true }).decode(buf)
    return true
  } catch {
    return false
  }
}

function tryGb18030ToUtf8(buf) {
  try {
    return new TextDecoder('gb18030').decode(buf)
  } catch {
    return null
  }
}

function rel(p) {
  return path.relative(MICRO_ROOT, p).replace(/\\/g, '/')
}

function main() {
  const fix = process.argv.includes('--fix')
  const scanBad = new Set(scanEncoding().bad.map((b) => b.file))
  const files = [...new Set(SCAN_DIRS.flatMap((d) => walkFiles(d)))]

  const needsAttention = []
  for (const file of files) {
    const buf = fs.readFileSync(file)
    const r = rel(file)
    const invalidUtf8 = !isValidUtf8(buf)
    const scanHit = scanBad.has(r)
    if (invalidUtf8 || scanHit) {
      needsAttention.push({ file, rel: r, invalidUtf8, scanHit })
    }
  }

  if (needsAttention.length === 0) {
    console.log('[PASS] no files need encoding conversion')
    process.exit(0)
  }

  console.log(`Found ${needsAttention.length} file(s) to review:`)
  let fixed = 0
  for (const row of needsAttention) {
    console.log(`  - ${row.rel}${row.invalidUtf8 ? ' (invalid UTF-8)' : ''}${row.scanHit ? ' (scan)' : ''}`)
    if (!fix || !row.invalidUtf8) continue
    const buf = fs.readFileSync(row.file)
    const converted = tryGb18030ToUtf8(buf)
    if (!converted) {
      console.log(`    [SKIP] cannot decode as GB18030`)
      continue
    }
    fs.writeFileSync(row.file, converted, 'utf8')
    console.log(`    [FIXED] wrote UTF-8`)
    fixed++
  }

  if (fix) {
    const after = scanEncoding()
    if (after.bad.length === 0) {
      console.log(`\n[PASS] ${fixed} converted; scan clean`)
      process.exit(0)
    }
    console.log(`\n[WARN] ${fixed} converted; ${after.bad.length} scan issue(s) remain (may need manual fix)`)
    process.exit(after.bad.length > 0 ? 1 : 0)
  }

  process.exit(1)
}

main()
