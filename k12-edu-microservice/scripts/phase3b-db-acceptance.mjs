/**
 * Phase 3B 数据库验收：
 * 1) 执行 86 回填脚本（可跳过）
 * 2) 校验结构、覆盖、未映射、统一视图
 */
import fs from 'node:fs';
import path from 'node:path';
import { spawnSync } from 'node:child_process';
import { createReporter } from './phase9-test-utils.mjs';

const MYSQL_USER = process.env.MYSQL_USER || 'root';
const MYSQL_PASSWORD = process.env.MYSQL_PASSWORD || 'zxg123456';
const MYSQL_DB = process.env.MYSQL_DB || 'xinketang';
const SCRIPT_PATH = 'sql/86_phase3b_resource_main_chain.sql';

function detectMysqlBin() {
  const candidates = [
    process.env.MYSQL_BIN,
    'C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe',
    'mysql',
  ].filter(Boolean);
  for (const c of candidates) {
    if (c === 'mysql') return c;
    if (fs.existsSync(c)) return c;
  }
  return 'mysql';
}

function runMysql(mysqlBin, args) {
  const ret = spawnSync(mysqlBin, args, {
    cwd: process.cwd(),
    env: process.env,
    encoding: 'utf8',
  });
  return ret;
}

function runQuery(mysqlBin, sql) {
  const args = [
    `-u${MYSQL_USER}`,
    `-p${MYSQL_PASSWORD}`,
    MYSQL_DB,
    '--batch',
    '--raw',
    '--skip-column-names',
    '-e',
    sql,
  ];
  const ret = runMysql(mysqlBin, args);
  if ((ret.status ?? 1) !== 0) {
    throw new Error((ret.stderr || ret.stdout || '').trim() || 'mysql query failed');
  }
  const text = (ret.stdout || '').trim();
  if (!text) return [];
  return text.split(/\r?\n/).map((line) => line.split('\t'));
}

function getOneInt(rows, fallback = 0) {
  if (!rows.length || !rows[0].length) return fallback;
  const n = Number(rows[0][0]);
  return Number.isFinite(n) ? n : fallback;
}

async function main() {
  const r = createReporter();
  const mysqlBin = detectMysqlBin();
  const scriptAbs = path.resolve(process.cwd(), SCRIPT_PATH);

  r.add('B0-mysql-bin', !!mysqlBin, mysqlBin);
  r.add('B0-sql-script-exists', fs.existsSync(scriptAbs), SCRIPT_PATH);
  if (!fs.existsSync(scriptAbs)) {
    process.exit(r.summary('Phase3-B DB Acceptance'));
  }

  if (String(process.env.PHASE3B_SKIP_SQL || '0') !== '1') {
    const execRet = runMysql(mysqlBin, [
      `-u${MYSQL_USER}`,
      `-p${MYSQL_PASSWORD}`,
      '-e',
      `source ${SCRIPT_PATH}`,
    ]);
    r.add(
      'B1-exec-86-sql',
      (execRet.status ?? 1) === 0,
      (execRet.stderr || execRet.stdout || '').trim().slice(0, 200) || 'ok',
    );
  } else {
    r.add('B1-exec-86-sql', true, 'skipped by PHASE3B_SKIP_SQL=1');
  }

  // B2: 结构字段
  const cols = runQuery(
    mysqlBin,
    "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'resource_main' AND COLUMN_NAME IN ('content_domain','canonical_resource_id','legacy_source_table','legacy_source_id')",
  );
  const colCount = getOneInt(cols);
  r.add('B2-resource-main-columns', colCount === 4, `count=${colCount}/4`);

  // B3: source_type 覆盖
  const sourceRows = runQuery(
    mysqlBin,
    'SELECT source_type, COUNT(*) FROM resource_main WHERE is_deleted = 0 GROUP BY source_type',
  );
  const sourceSet = new Set(sourceRows.map((x) => x[0]));
  const requiredSources = [
    'primary_chinese',
    'topic_resource',
    'culture_resource',
    'competition_resource',
    'edu_resource',
  ];
  r.add(
    'B3-resource-main-source-coverage',
    requiredSources.every((s) => sourceSet.has(s)),
    `found=${requiredSources.filter((s) => sourceSet.has(s)).length}/${requiredSources.length}`,
  );

  // B4: 未映射检查应为 0
  const unmappedRows = runQuery(
    mysqlBin,
    `
    SELECT 'topic_unmapped', COUNT(*) FROM topic_resource t
    LEFT JOIN resource_main rm ON rm.source_type='topic_resource' AND rm.source_id=t.id
    WHERE t.is_deleted=0 AND rm.id IS NULL
    UNION ALL
    SELECT 'culture_unmapped', COUNT(*) FROM culture_resource c
    LEFT JOIN resource_main rm ON rm.source_type='culture_resource' AND rm.source_id=c.id
    WHERE c.is_deleted=0 AND rm.id IS NULL
    UNION ALL
    SELECT 'competition_unmapped', COUNT(*) FROM competition_resource c
    LEFT JOIN resource_main rm ON rm.source_type='competition_resource' AND rm.source_id=c.id
    WHERE c.is_deleted=0 AND rm.id IS NULL
    UNION ALL
    SELECT 'edu_unmapped', COUNT(*) FROM edu_resource e
    LEFT JOIN resource_main rm ON rm.source_type='edu_resource' AND rm.source_id=e.id
    WHERE e.is_deleted=0 AND rm.id IS NULL
    `,
  );
  const unmappedTotal = unmappedRows.reduce((acc, row) => acc + Number(row[1] || 0), 0);
  r.add('B4-unmapped-total', unmappedTotal === 0, `total=${unmappedTotal}`);

  // B5: 统一视图覆盖
  const viewRows = runQuery(
    mysqlBin,
    'SELECT source_type, COUNT(*) FROM v_admin_resource_main GROUP BY source_type',
  );
  const viewSet = new Set(viewRows.map((x) => x[0]));
  r.add(
    'B5-view-source-coverage',
    requiredSources.every((s) => viewSet.has(s)),
    `found=${requiredSources.filter((s) => viewSet.has(s)).length}/${requiredSources.length}`,
  );

  // B6: canonical_resource_id 不应为空
  const canonicalNullRows = runQuery(
    mysqlBin,
    'SELECT COUNT(*) FROM resource_main WHERE canonical_resource_id IS NULL',
  );
  const canonicalNull = getOneInt(canonicalNullRows);
  r.add('B6-canonical-not-null', canonicalNull === 0, `null_count=${canonicalNull}`);

  process.exit(r.summary('Phase3-B DB Acceptance'));
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
