/**
 * Phase 3H-2 UTF-8 / mojibake 专项扫描
 *
 * 用法（在 k12-edu-microservice 目录）:
 *   node scripts/phase3h2-encoding-scan.mjs
 *   node scripts/phase3h2-encoding-scan.mjs --critical-only
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const MICRO_ROOT = path.resolve(__dirname, '..')
const PLATFORM_SRC = path.resolve(MICRO_ROOT, '../k12-edu-platform/src')

export const UI_CRITICAL_REL = [
  'views/feature/ClassMeetingCategory.vue',
  'views/resource/SubjectDetailPage.vue',
  'components/shared/FilterOptionRow.vue',
  'components/subject/ResourceTypeBar.vue',
]

export const UI_CRITICAL_FILES = UI_CRITICAL_REL.map((rel) => path.join(PLATFORM_SRC, rel))

export const MOJIBAKE_PATTERNS = [
  { id: 'U+FFFD', re: /\uFFFD/ },
  { id: 'GBK-garbled', re: /[绌鐗鍗鎬锟]{2,}/ },
  { id: 'latin1-mojibake', re: /(?:Ã|Â)[\u0080-\u00BF]/ },
  { id: 'replacement-seq', re: /ï¿½/ },
]

export const SCAN_DIRS = (() => {
  const dirs = [
    PLATFORM_SRC,
    path.join(MICRO_ROOT, 'sql'),
    path.join(MICRO_ROOT, 'docs'),
  ]
  if (fs.existsSync(MICRO_ROOT)) {
    for (const name of fs.readdirSync(MICRO_ROOT)) {
      if (!name.startsWith('k12-')) continue
      const javaDir = path.join(MICRO_ROOT, name, 'src/main/java')
      if (fs.existsSync(javaDir)) dirs.push(javaDir)
    }
  }
  return dirs
})()

const TEXT_EXT = new Set([
  '.java', '.sql', '.ts', '.tsx', '.js', '.jsx', '.mjs', '.vue',
  '.md', '.json', '.yml', '.yaml', '.xml', '.properties',
])

const SKIP_DIRS = new Set(['node_modules', 'dist', 'target', '.git'])

function hasTemplatePlaceholderCorruption(template) {
  const stripped = template
    .replace(/\{\{[\s\S]*?\}\}/g, '')
    .replace(/(?:v-|:|@)[\w-]+="[^"]*"/g, '')
  if (/>\s*\?{2,}(\s|<)/.test(stripped)) return true
  if (/['"]\?{2,}['"]/.test(stripped)) return true
  return false
}

function walkFiles(dir, out = []) {
  if (!fs.existsSync(dir)) return out
  for (const name of fs.readdirSync(dir)) {
    if (SKIP_DIRS.has(name)) continue
    const p = path.join(dir, name)
    const stat = fs.statSync(p)
    if (stat.isDirectory()) {
      walkFiles(p, out)
    } else if (TEXT_EXT.has(path.extname(p).toLowerCase())) {
      out.push(p)
    }
  }
  return out
}

function rel(p) {
  return path.relative(MICRO_ROOT, p).replace(/\\/g, '/')
}

function hasJavaPlaceholderCorruption(text) {
  const lines = text.split(/\r?\n/)
  for (const line of lines) {
    if (!line.includes('??')) continue
    if (/\?\?\s*[\w('"([]/.test(line)) continue
    if (/^\s*\/\//.test(line) || /"[^"]*\?\?[^"]*"/.test(line) || /'[^']*\?\?[^']*'/.test(line)) {
      return true
    }
  }
  return false
}

function checkFile(filePath) {
  const issues = []
  const buf = fs.readFileSync(filePath)

  let text
  try {
    text = buf.toString('utf8')
    try {
      new TextDecoder('utf-8', { fatal: true }).decode(buf)
    } catch {
      issues.push('invalid UTF-8 bytes')
    }
  } catch {
    issues.push('invalid UTF-8 decode')
    return issues
  }

  for (const { id, re } of MOJIBAKE_PATTERNS) {
    re.lastIndex = 0
    if (re.test(text)) {
      issues.push(id)
    }
  }

  if (filePath.endsWith('.java') && hasJavaPlaceholderCorruption(text)) {
    issues.push('java-comment-??-placeholder')
  }

  if (filePath.endsWith('.vue')) {
    const tplMatch = text.match(/<template>([\s\S]*?)<\/template>/)
    if (tplMatch && hasTemplatePlaceholderCorruption(tplMatch[1])) {
      issues.push('vue-template-??-placeholder')
    }
  }

  return [...new Set(issues)]
}

/**
 * @param {{ criticalOnly?: boolean, dirs?: string[] }} options
 * @returns {{ bad: { file: string, issues: string[] }[], scanned: number }}
 */
export function scanEncoding(options = {}) {
  const { criticalOnly = false, dirs = SCAN_DIRS } = options
  const files = criticalOnly
    ? UI_CRITICAL_FILES.filter((f) => fs.existsSync(f))
    : [...new Set(dirs.flatMap((d) => walkFiles(d)))]

  const bad = []
  for (const file of files) {
    const issues = checkFile(file)
    if (issues.length) {
      bad.push({ file: rel(file), issues })
    }
  }
  return { bad, scanned: files.length }
}

function main() {
  const criticalOnly = process.argv.includes('--critical-only')
  const { bad, scanned } = scanEncoding({ criticalOnly })

  console.log(`Phase 3H-2 encoding scan (${criticalOnly ? 'critical' : 'full'}): ${scanned} files`)
  if (bad.length === 0) {
    console.log('[PASS] no mojibake / invalid UTF-8 detected')
    process.exit(0)
  }

  for (const row of bad) {
    console.log(`[FAIL] ${row.file}: ${row.issues.join(', ')}`)
  }
  console.log(`\n=== FAILED: ${bad.length} file(s) ===`)
  process.exit(1)
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  main()
}
