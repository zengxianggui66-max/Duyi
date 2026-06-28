/**
 * Phase 3C 验收：
 * 1) 执行 88 SQL（可跳过）
 * 2) 校验别名表/质量视图
 * 3) 校验新 API（质量看板 + 别名接口）
 * 4) 继承 P0 门禁
 */
import fs from 'node:fs';
import { spawnSync } from 'node:child_process';
import { api, createReporter, login } from './phase9-test-utils.mjs';

const MYSQL_USER = process.env.MYSQL_USER || 'root';
const MYSQL_PASSWORD = process.env.MYSQL_PASSWORD || 'zxg123456';
const MYSQL_DB = process.env.MYSQL_DB || 'xinketang';
const SQL_FILE = 'sql/88_phase3c_alias_quality.sql';

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
  return spawnSync(mysqlBin, args, {
    cwd: process.cwd(),
    env: process.env,
    encoding: 'utf8',
  });
}

function query(mysqlBin, sql) {
  const ret = runMysql(mysqlBin, [
    `-u${MYSQL_USER}`,
    `-p${MYSQL_PASSWORD}`,
    MYSQL_DB,
    '--batch',
    '--raw',
    '--skip-column-names',
    '-e',
    sql,
  ]);
  if ((ret.status ?? 1) !== 0) {
    throw new Error((ret.stderr || ret.stdout || '').trim() || 'mysql query failed');
  }
  const text = (ret.stdout || '').trim();
  return text ? text.split(/\r?\n/).map((line) => line.split('\t')) : [];
}

async function main() {
  const r = createReporter();
  const mysqlBin = detectMysqlBin();
  r.add('C0-mysql-bin', !!mysqlBin, mysqlBin);
  r.add('C0-sql-file', fs.existsSync(SQL_FILE), SQL_FILE);

  if (String(process.env.PHASE3C_SKIP_SQL || '0') !== '1') {
    const execRet = runMysql(mysqlBin, [
      `-u${MYSQL_USER}`,
      `-p${MYSQL_PASSWORD}`,
      '-e',
      `source ${SQL_FILE}`,
    ]);
    r.add(
      'C1-exec-88-sql',
      (execRet.status ?? 1) === 0,
      (execRet.stderr || execRet.stdout || '').trim().slice(0, 200) || 'ok',
    );
  } else {
    r.add('C1-exec-88-sql', true, 'skipped by PHASE3C_SKIP_SQL=1');
  }

  const tbl = query(
    mysqlBin,
    "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'resource_catalog_alias'",
  );
  r.add('C2-alias-table-exists', Number(tbl?.[0]?.[0] || 0) === 1, `count=${tbl?.[0]?.[0] || 0}`);

  const viewMetrics = query(mysqlBin, 'SELECT COUNT(*) FROM v_resource_migration_quality');
  r.add(
    'C3-quality-view-metrics',
    Number(viewMetrics?.[0]?.[0] || 0) >= 6,
    `rows=${viewMetrics?.[0]?.[0] || 0}`,
  );

  const adminH = await login('admin', process.env.PHASE9_ADMIN_PASSWORD || 'admin123');

  // upsert alias
  const upsert = await api('POST', '/api/admin/resource-main/catalog-aliases', {
    headers: adminH,
    body: {
      sourceType: 'primary_chinese',
      legacyTitle: '我爱学语文',
      aliasTitle: '我爱我们的祖国',
      catalogNodeId: 1,
      confidence: 80,
      status: 1,
      notes: 'phase3c acceptance seed',
    },
  });
  r.add(
    'C4-alias-upsert-api',
    upsert.json?.code === 200 && upsert.json?.data?.id != null,
    `code=${upsert.json?.code ?? upsert.http}`,
  );

  const list = await api('GET', '/api/admin/resource-main/catalog-aliases', { headers: adminH });
  const total = list.json?.data?.total ?? 0;
  r.add('C5-alias-list-api', list.json?.code === 200 && total >= 1, `total=${total}`);

  const dash = await api('GET', '/api/admin/quality/analytics/migration-dashboard', { headers: adminH });
  const d = dash.json?.data || {};
  const keys = [
    'unplacedResources',
    'emptyFileResources',
    'approvedNotPublished',
    'publishedButInvisible',
    'catalogNodesWithoutResources',
    'orphanResourcesWithoutCatalog',
    'aliasTotal',
    'aliasEnabled',
  ];
  const dashboardOk = dash.json?.code === 200 && keys.every((k) => d[k] != null);
  r.add('C6-migration-dashboard-api', dashboardOk, `code=${dash.json?.code ?? dash.http}`);

  const gate = spawnSync(process.execPath, ['scripts/phase3p0-release-gate.mjs'], {
    cwd: process.cwd(),
    env: process.env,
    stdio: 'inherit',
  });
  r.add('C7-p0-gate-pass', (gate.status ?? 1) === 0, `exit=${gate.status ?? 1}`);

  process.exit(r.summary('Phase3-C Acceptance'));
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
