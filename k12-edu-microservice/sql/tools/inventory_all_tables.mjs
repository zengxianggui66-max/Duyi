/**
 * Phase 3K Step 1 — 全库表盘点
 *
 * 用法（在 k12-edu-microservice 目录）:
 *   node sql/tools/inventory_all_tables.mjs
 *   node sql/tools/inventory_all_tables.mjs --format=json --out=sql/tools/inventory_all_tables.json
 *   node sql/tools/inventory_all_tables.mjs --format=csv --out=sql/tools/inventory_all_tables.csv
 *
 * 环境变量:
 *   MYSQL_USER / MYSQL_PASSWORD / MYSQL_DB / MYSQL_BIN
 *   INVENTORY_EXACT_COUNT=1  — 对每张表执行 COUNT(*)（较慢，行数精确）
 */
import fs from 'node:fs'
import path from 'node:path'
import { spawnSync } from 'node:child_process'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const MICRO_ROOT = path.resolve(__dirname, '../..')
const SQL_DIR = path.join(MICRO_ROOT, 'sql')
const FRONTEND_SRC = path.resolve(MICRO_ROOT, '../k12-edu-platform/src')

const MYSQL_USER = process.env.MYSQL_USER || 'root'
const MYSQL_PASSWORD = process.env.MYSQL_PASSWORD || 'zxg123456'
const MYSQL_DB = process.env.MYSQL_DB || 'xinketang'
const EXACT_COUNT = String(process.env.INVENTORY_EXACT_COUNT || '0') === '1'

/** Phase 3K 分层（11 资源表 + 4 待废弃） */
const TIER_MAP = {
  resource_main: 'PRIMARY',
  edu_resource: 'PRIMARY',
  edu_resource_file: 'PRIMARY',
  edu_resource_dimension: 'PRIMARY',
  edu_resource_placement: 'PRIMARY',
  oss_primary_chinese_resource: 'COMPAT',
  topic_resource: 'COMPAT',
  culture_resource: 'COMPAT',
  competition_resource: 'COMPAT',
  article: 'COMPAT',
  sys_search_document: 'SEARCH_INDEX',
  resource_category: 'DEPRECATED',
  edu_resource_tag: 'DEPRECATED',
  edu_resource_region: 'DEPRECATED',
  edu_resource_search_flat: 'DEPRECATED',
}

const CSV_COLUMNS = [
  'table_name',
  'row_count',
  'tier',
  'entity_class',
  'java_refs',
  'frontend_refs',
  'last_sql_migration',
  'verdict',
]

function parseArgs(argv) {
  let format = 'csv'
  let out = ''
  for (let i = 2; i < argv.length; i++) {
    const arg = argv[i]
    if (arg.startsWith('--format=')) {
      format = arg.slice('--format='.length)
    } else if (arg === '--format' && argv[i + 1]) {
      format = argv[++i]
    } else if (arg.startsWith('--out=')) {
      out = arg.slice('--out='.length)
    } else if (arg === '--out' && argv[i + 1]) {
      out = argv[++i]
    }
  }
  return { format, out }
}

function detectMysqlBin() {
  const candidates = [
    process.env.MYSQL_BIN,
    'C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe',
    'mysql',
  ].filter(Boolean)
  for (const c of candidates) {
    if (c === 'mysql') return c
    if (fs.existsSync(c)) return c
  }
  return 'mysql'
}

function runMysql(mysqlBin, args) {
  return spawnSync(mysqlBin, args, {
    cwd: MICRO_ROOT,
    env: process.env,
    encoding: 'utf8',
  })
}

function runQuery(mysqlBin, sql) {
  const ret = runMysql(mysqlBin, [
    `-u${MYSQL_USER}`,
    `-p${MYSQL_PASSWORD}`,
    MYSQL_DB,
    '--default-character-set=utf8mb4',
    '--batch',
    '--raw',
    '--skip-column-names',
    '-e',
    sql,
  ])
  if ((ret.status ?? 1) !== 0) {
    throw new Error((ret.stderr || ret.stdout || '').trim() || 'mysql query failed')
  }
  const text = (ret.stdout || '').trim()
  if (!text) return []
  return text.split(/\r?\n/).map((line) => line.split('\t'))
}

function listDbTables(mysqlBin) {
  const rows = runQuery(
    mysqlBin,
    `SELECT TABLE_NAME, TABLE_TYPE, IFNULL(TABLE_ROWS, 0), IFNULL(TABLE_COMMENT, '')
     FROM information_schema.TABLES
     WHERE TABLE_SCHEMA = '${MYSQL_DB}'
       AND TABLE_TYPE IN ('BASE TABLE', 'VIEW')
     ORDER BY TABLE_NAME`,
  )
  return rows.map(([name, type, rowsApprox, comment]) => ({
    table_name: name,
    table_type: type,
    row_count_approx: Number(rowsApprox) || 0,
    table_comment: comment,
  }))
}

function exactRowCount(mysqlBin, tableName) {
  const safe = tableName.replace(/`/g, '')
  try {
    const rows = runQuery(mysqlBin, `SELECT COUNT(*) FROM \`${safe}\``)
    return Number(rows[0]?.[0]) || 0
  } catch {
    return -1
  }
}

function listSqlFiles() {
  if (!fs.existsSync(SQL_DIR)) return []
  return fs
    .readdirSync(SQL_DIR)
    .filter((f) => f.endsWith('.sql'))
    .sort()
}

function findLastSqlMigration(tableName, sqlFiles) {
  let last = ''
  const needle = tableName.toLowerCase()
  for (const file of sqlFiles) {
    const content = fs.readFileSync(path.join(SQL_DIR, file), 'utf8').toLowerCase()
    if (
      content.includes(`\`${needle}\``) ||
      content.includes(`create table ${needle}`) ||
      content.includes(`create table \`${needle}\``) ||
      content.includes(`'${needle}'`)
    ) {
      last = file
    }
  }
  return last
}

function walkFiles(dir, ext, out = []) {
  if (!fs.existsSync(dir)) return out
  for (const ent of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, ent.name)
    if (ent.isDirectory()) {
      if (ent.name === 'node_modules' || ent.name === 'target' || ent.name === 'dist') continue
      walkFiles(full, ext, out)
    } else if (ent.name.endsWith(ext)) {
      out.push(full)
    }
  }
  return out
}

function normalizeTableFromTableName(value) {
  if (!value) return ''
  const v = value.trim().replace(/^["']|["']$/g, '')
  const dot = v.lastIndexOf('.')
  return (dot >= 0 ? v.slice(dot + 1) : v).toLowerCase()
}

function scanJava(microRoot) {
  const entityByTable = new Map()

  const javaFiles = [
    ...walkFiles(path.join(microRoot, 'k12-common'), '.java'),
    ...walkFiles(path.join(microRoot, 'k12-resource'), '.java'),
    ...walkFiles(path.join(microRoot, 'k12-article'), '.java'),
    ...walkFiles(path.join(microRoot, 'k12-auth'), '.java'),
    ...walkFiles(path.join(microRoot, 'k12-gateway'), '.java'),
    ...walkFiles(path.join(microRoot, 'k12-prep'), '.java'),
    ...walkFiles(path.join(microRoot, 'k12-lesson'), '.java'),
    ...walkFiles(path.join(microRoot, 'k12-exam'), '.java'),
    ...walkFiles(path.join(microRoot, 'k12-member'), '.java'),
  ]

  const tableNameRe = /@TableName\s*\(\s*"([^"]+)"\s*\)/g

  for (const file of javaFiles) {
    const content = fs.readFileSync(file, 'utf8')
    const base = path.basename(file)

    let m
    tableNameRe.lastIndex = 0
    while ((m = tableNameRe.exec(content)) !== null) {
      const table = normalizeTableFromTableName(m[1])
      if (!table) continue
      const classMatch = content.match(/(?:public\s+)?(?:class|interface|enum)\s+(\w+)/)
      const className = classMatch?.[1] || base.replace(/\.java$/, '')
      entityByTable.set(table, className)
    }
  }

  return { entityByTable, javaFileCount: javaFiles.length }
}

function contentIncludesTable(content, tableName) {
  const lower = content.toLowerCase()
  const t = tableName.toLowerCase()
  return (
    lower.includes(`\`${t}\``) ||
    lower.includes(`"${t}"`) ||
    lower.includes(`'${t}'`) ||
    lower.includes(`from ${t}`) ||
    lower.includes(`join ${t}`) ||
    lower.includes(`into ${t}`) ||
    lower.includes(`update ${t}`) ||
    lower.includes(`@tablename("${t}")`) ||
    lower.includes(`@tablename("xinketang.${t}")`)
  )
}

function scanAllJavaRefs(microRoot, tableNames) {
  const refsByTable = new Map()
  for (const t of tableNames) refsByTable.set(t, new Set())

  const roots = [
    'k12-common',
    'k12-resource',
    'k12-article',
    'k12-auth',
    'k12-prep',
    'k12-lesson',
    'k12-exam',
    'k12-member',
  ]
  const javaFiles = []
  for (const r of roots) {
    walkFiles(path.join(microRoot, r), '.java', javaFiles)
  }

  for (const file of javaFiles) {
    const content = fs.readFileSync(file, 'utf8')
    const rel = path.relative(microRoot, file).replace(/\\/g, '/')
    for (const table of tableNames) {
      if (contentIncludesTable(content, table)) {
        refsByTable.get(table).add(rel)
      }
    }
  }
  return refsByTable
}

function scanFrontend(tableNames) {
  const refsByTable = new Map()
  for (const t of tableNames) refsByTable.set(t, new Set())

  const files = walkFiles(FRONTEND_SRC, '.ts').concat(walkFiles(FRONTEND_SRC, '.vue'))
  for (const file of files) {
    const content = fs.readFileSync(file, 'utf8').toLowerCase()
    const rel = path.relative(FRONTEND_SRC, file).replace(/\\/g, '/')
    for (const table of tableNames) {
      if (content.includes(table.toLowerCase())) {
        refsByTable.get(table).add(rel)
      }
    }
  }
  return refsByTable
}

function inferVerdict(tier, rowCount, javaRefCount, frontendRefCount) {
  if (tier === 'PRIMARY' || tier === 'COMPAT' || tier === 'SEARCH_INDEX') {
    return 'KEEP'
  }
  if (tier === 'DEPRECATED') {
    if (rowCount === 0 && javaRefCount === 0) return 'DELETE_CANDIDATE'
    return 'DEPRECATE'
  }
  if (rowCount === 0 && javaRefCount === 0 && frontendRefCount === 0) {
    return 'REVIEW'
  }
  return 'UNCLASSIFIED'
}

function escapeCsv(value) {
  const s = String(value ?? '')
  if (/[",\n\r]/.test(s)) return `"${s.replace(/"/g, '""')}"`
  return s
}

function toCsv(rows) {
  const lines = [CSV_COLUMNS.join(',')]
  for (const row of rows) {
    lines.push(CSV_COLUMNS.map((c) => escapeCsv(row[c])).join(','))
  }
  return lines.join('\n') + '\n'
}

function summarize(rows) {
  const byTier = {}
  for (const row of rows) {
    byTier[row.tier] = (byTier[row.tier] || 0) + 1
  }
  const lines = [
    `=== Phase 3K table inventory: ${rows.length} tables in ${MYSQL_DB} ===`,
    `Tier breakdown: ${Object.entries(byTier)
      .sort(([a], [b]) => a.localeCompare(b))
      .map(([k, v]) => `${k}=${v}`)
      .join(', ')}`,
    `Phase3K scoped: PRIMARY=5 COMPAT=5 SEARCH_INDEX=1 DEPRECATED=4`,
    `Exact row count: ${EXACT_COUNT ? 'yes' : 'no (information_schema.TABLE_ROWS approximate)'}`,
  ]
  return lines.join('\n')
}

async function main() {
  const { format, out } = parseArgs(process.argv)
  const mysqlBin = detectMysqlBin()

  console.error(`[inventory] mysql=${mysqlBin} db=${MYSQL_DB}`)

  const dbTables = listDbTables(mysqlBin)
  const sqlFiles = listSqlFiles()
  const tableNames = dbTables.map((t) => t.table_name)

  const { entityByTable } = scanJava(MICRO_ROOT)
  const javaRefs = scanAllJavaRefs(MICRO_ROOT, tableNames)
  const frontendRefs = scanFrontend(tableNames)

  const rows = []
  for (const t of dbTables) {
    const name = t.table_name
    const tier = TIER_MAP[name] || 'OTHER'

    let rowCount = t.row_count_approx
    if (EXACT_COUNT || tier === 'DEPRECATED') {
      const exact = exactRowCount(mysqlBin, name)
      if (exact >= 0) rowCount = exact
    }

    const javaSet = javaRefs.get(name) || new Set()
    const feSet = frontendRefs.get(name) || new Set()
    const javaRefList = [...javaSet].sort()
    const feRefList = [...feSet].sort()

    rows.push({
      table_name: name,
      row_count: rowCount,
      tier,
      entity_class: entityByTable.get(name.toLowerCase()) || '',
      java_refs: javaRefList.join(';'),
      frontend_refs: feRefList.join(';'),
      last_sql_migration: findLastSqlMigration(name, sqlFiles),
      verdict: inferVerdict(tier, rowCount, javaRefList.length, feRefList.length),
      table_type: t.table_type,
      table_comment: t.table_comment,
    })
  }

  const payload = {
    generated_at: new Date().toISOString(),
    database: MYSQL_DB,
    table_count: rows.length,
    exact_count: EXACT_COUNT,
    rows,
  }

  console.error(summarize(rows))

  let text
  if (format === 'json') {
    text = JSON.stringify(payload, null, 2) + '\n'
  } else {
    text = toCsv(rows)
  }

  if (out) {
    const outPath = path.isAbsolute(out) ? out : path.join(MICRO_ROOT, out)
    fs.mkdirSync(path.dirname(outPath), { recursive: true })
    fs.writeFileSync(outPath, text, 'utf8')
    console.error(`[inventory] wrote ${outPath}`)
  } else {
    process.stdout.write(text)
  }
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
