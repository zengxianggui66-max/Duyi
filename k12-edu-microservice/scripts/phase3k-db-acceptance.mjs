/**
 * Phase 3K 数据库验收（K1–K3）
 *
 * K1-doc-exists          → 冻结清单含 11 表名 + PRIMARY/COMPAT/SEARCH
 * K1-sql-comments        → COMMENT 含 [TIER:
 * K2-deprecated-no-entity→ tag/region/search_flat 无 @TableName；resource_category @Deprecated
 * K3-unmapped-all-zero   → 五源孤儿 SUM=0（含 primary_chinese）
 * K3-view-coverage       → v_admin_resource_main 五 source_type 均存在且 cnt>0
 * K3-migration-quality   → v_resource_migration_quality 可读（≥6 指标）
 *
 * 用法：node scripts/phase3k-db-acceptance.mjs
 * 环境：PHASE3K_SKIP_SQL=1 跳过 97 重放
 */
import fs from 'node:fs'
import path from 'node:path'
import { spawnSync } from 'node:child_process'
import { createReporter } from './phase9-test-utils.mjs'

const MYSQL_USER = process.env.MYSQL_USER || 'root'
const MYSQL_PASSWORD = process.env.MYSQL_PASSWORD || 'zxg123456'
const MYSQL_DB = process.env.MYSQL_DB || 'xinketang'
const DOC_PATH = 'docs/Phase3K-表分层冻结清单.md'
const COMMENT_SQL = 'sql/97_phase3k_table_tier_comments.sql'
const VERIFY_SQL = 'sql/tools/verify_phase3_resource_main_chain.sql'

const SCOPED_TABLES = [
  'resource_main',
  'edu_resource',
  'edu_resource_file',
  'edu_resource_dimension',
  'edu_resource_placement',
  'oss_primary_chinese_resource',
  'topic_resource',
  'culture_resource',
  'competition_resource',
  'article',
  'sys_search_document',
]

const NO_ENTITY_DEPRECATED = ['edu_resource_tag', 'edu_resource_region', 'edu_resource_search_flat']

const REQUIRED_SOURCES = [
  'primary_chinese',
  'topic_resource',
  'culture_resource',
  'competition_resource',
  'edu_resource',
]

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
    cwd: process.cwd(),
    env: process.env,
    encoding: 'utf8',
  })
}

function runQuery(mysqlBin, sql) {
  const ret = runMysql(mysqlBin, [
    `-u${MYSQL_USER}`,
    `-p${MYSQL_PASSWORD}`,
    MYSQL_DB,
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

function getOneInt(rows, fallback = 0) {
  if (!rows.length || !rows[0].length) return fallback
  const n = Number(rows[0][0])
  return Number.isFinite(n) ? n : fallback
}

function readFile(rel) {
  const abs = path.resolve(process.cwd(), rel)
  return fs.existsSync(abs) ? fs.readFileSync(abs, 'utf8') : ''
}

function hasTableNameEntity(table) {
  const roots = ['k12-common/src/main/java', 'k12-resource/src/main/java', 'k12-article/src/main/java']
  for (const root of roots) {
    const dir = path.join(process.cwd(), root)
    if (!fs.existsSync(dir)) continue
    const stack = [dir]
    while (stack.length) {
      const d = stack.pop()
      for (const ent of fs.readdirSync(d, { withFileTypes: true })) {
        const full = path.join(d, ent.name)
        if (ent.isDirectory()) stack.push(full)
        else if (ent.name.endsWith('.java')) {
          const content = fs.readFileSync(full, 'utf8')
          if (content.includes('@TableName') && content.includes(`"${table}"`)) return true
        }
      }
    }
  }
  return false
}

async function main() {
  const r = createReporter()
  const mysqlBin = detectMysqlBin()

  if (String(process.env.PHASE3K_SKIP_SQL || '0') !== '1' && fs.existsSync(COMMENT_SQL)) {
    runMysql(mysqlBin, [`-u${MYSQL_USER}`, `-p${MYSQL_PASSWORD}`, '-e', `source ${COMMENT_SQL}`])
  }

  // --- K1 ---
  const doc = readFile(DOC_PATH)
  r.add(
    'K1-doc-exists',
    doc.includes('PRIMARY') &&
      doc.includes('COMPAT') &&
      doc.includes('SEARCH_INDEX') &&
      SCOPED_TABLES.every((t) => doc.includes(`\`${t}\``) || doc.includes(t)),
    DOC_PATH,
  )

  const commentRows = runQuery(
    mysqlBin,
    `SELECT COUNT(*) FROM information_schema.TABLES
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME IN ('${SCOPED_TABLES.concat('resource_category', ...NO_ENTITY_DEPRECATED).join("','")}')
       AND TABLE_COMMENT LIKE '%[TIER:%'`,
  )
  r.add('K1-sql-comments', getOneInt(commentRows) >= 15, `marked=${getOneInt(commentRows)}/15`)

  const verifySrc = readFile(VERIFY_SQL)
  r.add(
    'K1-verify-sql-t3b',
    verifySrc.includes('T3b primary_chinese unmapped') && verifySrc.includes('primary_chinese'),
    VERIFY_SQL,
  )

  // --- K2 ---
  const noEntityOk = NO_ENTITY_DEPRECATED.every((t) => !hasTableNameEntity(t))
  const deprecatedRowsOk = NO_ENTITY_DEPRECATED.every(
    (t) => getOneInt(runQuery(mysqlBin, `SELECT COUNT(*) FROM \`${t}\``)) === 0,
  )
  const categorySrc = readFile('k12-common/src/main/java/com/k12/common/entity/ResourceCategory.java')
  r.add(
    'K2-deprecated-no-entity',
    noEntityOk &&
      deprecatedRowsOk &&
      categorySrc.includes('@Deprecated') &&
      getOneInt(runQuery(mysqlBin, 'SELECT COUNT(*) FROM resource_category')) === 0,
    `noEntity=${noEntityOk} rows0=${deprecatedRowsOk} categoryDeprecated=${categorySrc.includes('@Deprecated')}`,
  )

  // --- K3 ---
  const unmappedSql = `
    SELECT SUM(c) FROM (
      SELECT COUNT(*) c FROM topic_resource t
      LEFT JOIN resource_main rm ON rm.source_type='topic_resource' AND rm.source_id=t.id
      WHERE t.is_deleted=0 AND rm.id IS NULL
      UNION ALL SELECT COUNT(*) FROM culture_resource c
      LEFT JOIN resource_main rm ON rm.source_type='culture_resource' AND rm.source_id=c.id
      WHERE c.is_deleted=0 AND rm.id IS NULL
      UNION ALL SELECT COUNT(*) FROM competition_resource c
      LEFT JOIN resource_main rm ON rm.source_type='competition_resource' AND rm.source_id=c.id
      WHERE c.is_deleted=0 AND rm.id IS NULL
      UNION ALL SELECT COUNT(*) FROM edu_resource e
      LEFT JOIN resource_main rm ON rm.source_type='edu_resource' AND rm.source_id=e.id
      WHERE e.is_deleted=0 AND rm.id IS NULL
      UNION ALL SELECT COUNT(*) FROM oss_primary_chinese_resource p
      LEFT JOIN resource_main rm ON rm.source_type='primary_chinese' AND rm.source_id=p.id
      WHERE p.is_deleted=0 AND rm.id IS NULL
    ) x`
  const unmappedTotal = getOneInt(runQuery(mysqlBin, unmappedSql))
  r.add('K3-unmapped-all-zero', unmappedTotal === 0, `total=${unmappedTotal}`)

  const viewRows = runQuery(
    mysqlBin,
    'SELECT source_type, COUNT(*) AS cnt FROM v_admin_resource_main GROUP BY source_type',
  )
  const viewMap = new Map(viewRows.map((row) => [row[0], Number(row[1]) || 0]))
  const viewOk = REQUIRED_SOURCES.every((s) => viewMap.has(s) && (viewMap.get(s) ?? 0) > 0)
  r.add(
    'K3-view-coverage',
    viewOk,
    REQUIRED_SOURCES.map((s) => `${s}=${viewMap.get(s) ?? 0}`).join(' '),
  )

  const mqRows = runQuery(mysqlBin, 'SELECT COUNT(*) FROM v_resource_migration_quality')
  r.add('K3-migration-quality', getOneInt(mqRows) >= 6, `metrics=${getOneInt(mqRows)}`)

  const rmSources = runQuery(
    mysqlBin,
    'SELECT source_type FROM resource_main WHERE is_deleted=0 GROUP BY source_type',
  )
  const rmSet = new Set(rmSources.map((x) => x[0]))
  r.add(
    'K3-resource-main-sources',
    REQUIRED_SOURCES.every((s) => rmSet.has(s)),
    `found=${REQUIRED_SOURCES.filter((s) => rmSet.has(s)).length}/5`,
  )

  process.exit(r.summary('Phase 3K DB Acceptance (K1-K3)'))
}

main().catch((e) => {
  console.error(e)
  process.exit(1)
})
