/**
 * Phase 2 Step 1 baseline acceptance (build gate + RBAC + logs + Phase9 regression hook)
 * Usage:
 *   node scripts/phase2-step1-baseline-acceptance.mjs
 *   PHASE2_INCLUDE_PHASE9=0 node scripts/phase2-step1-baseline-acceptance.mjs  # 仅基线
 *
 * Env:
 *   PHASE2_GATEWAY / PHASE9_GATEWAY  default http://localhost:9001
 *   PHASE2_SKIP_BUILD=1              跳过 npm/mvn 构建检查（默认跳过）
 *   PHASE2_CHECK_BUILD=1             强制跑 build + compile
 *   PHASE2_INCLUDE_PHASE9=1          基线通过后串联 phase9-acceptance-test.mjs
 */
import { spawn, spawnSync } from 'node:child_process';
import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import path from 'node:path';
import {
  createReporter,
  api,
  login,
  loginFirst,
  isForbidden,
  permCodes,
  hasMenuTree,
  menuTitles,
  legacySearchAdminGone,
  BASE,
} from './phase9-test-utils.mjs';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const ROOT = path.resolve(__dirname, '..');
const PLATFORM = path.resolve(ROOT, '..', 'k12-edu-platform');

function isUnauthorized(res) {
  return res.http === 401 || res.json?.code === 401;
}

function runCmd(cwd, cmd, args) {
  return new Promise((resolve) => {
    const child = spawn(cmd, args, { cwd, stdio: 'inherit', shell: process.platform === 'win32' });
    child.on('close', (code) => resolve(code ?? 1));
  });
}

async function checkBuild(add) {
  const shouldCheck = process.env.PHASE2_CHECK_BUILD === '1';
  const skip = process.env.PHASE2_SKIP_BUILD === '1' || !shouldCheck;
  if (skip) {
    add('B0-build-skipped', true, 'set PHASE2_CHECK_BUILD=1 to run npm build + mvn compile');
    return;
  }

  const feCode = await runCmd(PLATFORM, 'npm', ['run', 'build']);
  add('B1-frontend-build', feCode === 0, `exit=${feCode}`);

  const mvn = process.platform === 'win32' ? 'mvnw.cmd' : './mvnw';
  const beCode = await runCmd(ROOT, mvn, [
    '-pl',
    'k12-common,k12-auth,k12-resource,k12-gateway',
    '-am',
    '-DskipTests',
    'compile',
  ]);
  add('B2-backend-compile', beCode === 0, `exit=${beCode}`);
}

function seedStep1Users() {
  if (process.env.PHASE2_SKIP_SQL_SEED === '1') {
    return false;
  }
  try {
    const sql = readFileSync(path.join(ROOT, 'sql', '81_phase2_step1_baseline.sql'), 'utf8');
    const user = process.env.MYSQL_USER || 'root';
    const db = process.env.MYSQL_DB || 'xinketang';
    const args = ['-u', user, db, '--default-character-set=utf8mb4'];
    if (process.env.MYSQL_PWD) {
      args.splice(1, 0, `-p${process.env.MYSQL_PWD}`);
    }
    const r = spawnSync('mysql', args, { input: sql, encoding: 'utf8' });
    return r.status === 0;
  } catch {
    return false;
  }
}

async function ensureStep1Users(add) {
  try {
    await login('normal_user');
    return;
  } catch {
    /* seed below */
  }
  const seeded = seedStep1Users();
  add(
    'S0-seed-sql81',
    true,
    seeded ? 'auto-seeded sql/81' : 'SKIP — run scripts/run-phase2-step1-sql.cmd or set MYSQL_PWD',
  );
}

async function runBaselineApiTests() {
  const { add, summary } = createReporter();
  console.log(`=== Phase 2 Step 1 Baseline Acceptance ===\nGateway: ${BASE}\n`);

  await checkBuild(add);
  await ensureStep1Users(add);

  // ---------- 身份与网关 ----------
  const anonMenus = await api('GET', '/api/admin/menus');
  add('S1-anon-menus-401', isUnauthorized(anonMenus), `http=${anonMenus.http} code=${anonMenus.json?.code}`);

  const adminH = await login('admin');
  const me = await api('GET', '/api/admin/auth/me', { headers: adminH });
  add(
    'S2-admin-me',
    me.json?.code === 200 && Array.isArray(me.json?.data?.roles) && me.json.data.roles.length > 0,
    `roles=${me.json?.data?.roles?.join(',')}`,
  );

  let normalH;
  let normalUserLabel = 'normal_user';
  try {
    const normal = await loginFirst([
      { username: 'normal_user', password: 'admin123' },
      { username: 'teacher_demo', password: 'teacher123' },
    ]);
    normalH = normal.headers;
    normalUserLabel = normal.username;
  } catch (e) {
    add(
      'S3-normal-user-login',
      false,
      `no C端测试账号 — 请用 cmd 执行: mysql ... < sql/81_phase2_step1_baseline.sql (${e.message})`,
    );
    normalH = null;
  }
  if (normalH) {
    const normalMe = await api('GET', '/api/admin/auth/me', { headers: normalH });
    add(
      'S3-normal-user-me-403',
      isForbidden(normalMe),
      `user=${normalUserLabel} http=${normalMe.http} code=${normalMe.json?.code}`,
    );
  }

  let staffNoRoleH;
  try {
    staffNoRoleH = await login('staff_no_role');
  } catch (e) {
    add(
      'S4-staff-no-role-login',
      false,
      `请用 cmd 执行 sql/81（PowerShell 勿直接粘贴含 $ 的 SQL）: ${e.message}`,
    );
    staffNoRoleH = null;
  }
  if (staffNoRoleH) {
    const snrMe = await api('GET', '/api/admin/auth/me', { headers: staffNoRoleH });
    add(
      'S4-staff-no-role-403',
      isForbidden(snrMe),
      `http=${snrMe.http} code=${snrMe.json?.code} msg=${snrMe.json?.message}`,
    );
  }

  // ---------- 角色 API 矩阵 ----------
  const operatorH = await login('operator');
  const contentH = await login('content_admin');
  const auditorH = await login('auditor');

  const adminMenus = await api('GET', '/api/admin/menus', { headers: adminH });
  add(
    'S5-admin-menu-tree',
    hasMenuTree(adminMenus.json, '数据分析', '运营概览') &&
      hasMenuTree(adminMenus.json, '搜索运营', '搜索概览'),
    `analytics+search titles=${menuTitles(adminMenus.json).filter((t) => t.includes('数据') || t.includes('搜索')).join('|')}`,
  );

  const opStats = await api('GET', '/api/admin/search/stats?days=7', { headers: operatorH });
  add('S6-operator-search-stats', opStats.json?.code === 200, `http=${opStats.http}`);

  const opReindex = await api('POST', '/api/admin/search/reindex', { headers: operatorH });
  add('S7-operator-reindex-403', isForbidden(opReindex), `http=${opReindex.http}`);

  const ctUsers = await api('GET', '/api/admin/analytics/users?days=30', { headers: contentH });
  add('S8-content-analytics-users', ctUsers.json?.code === 200, `http=${ctUsers.http}`);

  const auDash = await api('GET', '/api/admin/analytics/dashboard?days=7', { headers: auditorH });
  add('S9-auditor-analytics-403', isForbidden(auDash), `http=${auDash.http}`);

  const auSearch = await api('GET', '/api/admin/search/stats?days=7', { headers: auditorH });
  add('S10-auditor-search-403', isForbidden(auSearch), `http=${auSearch.http}`);

  // ---------- 权限码统一：stats 走 admin:analytics:view ----------
  const opResStats = await api('GET', '/api/admin/stats/resources', { headers: operatorH });
  add('S11-operator-stats-resources', opResStats.json?.code === 200, `http=${opResStats.http}`);

  const auResStats = await api('GET', '/api/admin/stats/resources', { headers: auditorH });
  add('S12-auditor-stats-resources-403', isForbidden(auResStats), `http=${auResStats.http}`);

  const opPrimaryStats = await api('GET', '/api/admin/resources/stats', { headers: operatorH });
  add('S13-operator-resources-stats', opPrimaryStats.json?.code === 200, `http=${opPrimaryStats.http}`);

  const auPrimaryStats = await api('GET', '/api/admin/resources/stats', { headers: auditorH });
  add('S14-auditor-resources-stats-403', isForbidden(auPrimaryStats), `http=${auPrimaryStats.http}`);

  const auConfig = await api('PUT', '/api/admin/system/config?group=upload', {
    headers: auditorH,
    body: { maxSizeMb: 50 },
  });
  add('S15-auditor-config-edit-403', isForbidden(auConfig), `http=${auConfig.http}`);

  const legacy = await api('GET', '/api/search/admin/stats?days=7', { headers: adminH });
  add('S16-legacy-search-admin-gone', legacySearchAdminGone(legacy), `http=${legacy.http}`);

  const auPerms = await api('GET', '/api/admin/permissions', { headers: auditorH });
  const auCodes = permCodes(auPerms.json);
  add(
    'S17-auditor-no-analytics-perm',
    !auCodes.includes('admin:analytics:view') && !auCodes.includes('admin:dashboard:view'),
    `dashboard=${auCodes.includes('admin:dashboard:view')} analytics=${auCodes.includes('admin:analytics:view')}`,
  );

  // ---------- 操作日志（至少一个核心模块有记录 + API 可用） ----------
  const auditLogs = await api('GET', '/api/admin/system/logs?module=audit&size=1', { headers: adminH });
  const resourceLogs = await api('GET', '/api/admin/system/logs?module=resource&size=1', { headers: adminH });
  const searchLogs = await api('GET', '/api/admin/system/logs?module=search&size=1', { headers: adminH });
  const logTotal = (res) => res.json?.data?.total ?? res.json?.data?.records?.length ?? 0;
  const coreLogTotal = logTotal(auditLogs) + logTotal(resourceLogs) + logTotal(searchLogs);
  add(
    'S18-core-operation-log',
    auditLogs.json?.code === 200 && coreLogTotal > 0,
    `audit=${logTotal(auditLogs)} resource=${logTotal(resourceLogs)} search=${logTotal(searchLogs)}`,
  );

  add('S19-search-log-api', searchLogs.json?.code === 200, `searchTotal=${logTotal(searchLogs)}`);

  const auLogs = await api('GET', '/api/admin/system/logs?current=1&size=1', { headers: auditorH });
  add('S20-auditor-logs-403', isForbidden(auLogs), `http=${auLogs.http}`);

  return summary('Phase 2 Step 1 Baseline');
}

function runScript(name) {
  return new Promise((resolve) => {
    const file = path.join(__dirname, name);
    const child = spawn(process.execPath, [file], { stdio: 'inherit', env: process.env });
    child.on('close', (code) => resolve(code ?? 1));
  });
}

async function main() {
  const baselineCode = await runBaselineApiTests();
  if (baselineCode !== 0) {
    console.log('\n提示：若 S3/S4 失败，请执行 sql/81_phase2_step1_baseline.sql');
    console.log('提示：若 S16 失败，请重启 k12-gateway 与 k12-resource');
    console.log('提示：若 S17 失败，请执行 sql/79_phase9_post_acceptance_fix.sql');
    process.exit(1);
  }

  const includePhase9 = process.env.PHASE2_INCLUDE_PHASE9 !== '0';
  if (!includePhase9) {
    console.log('\n(PHASE2_INCLUDE_PHASE9=0，跳过 Phase 9 回归)');
    process.exit(0);
  }

  console.log('\n>>> Phase 9 regression (optional baseline extension)\n');
  const phase9Code = await runScript('phase9-acceptance-test.mjs');
  process.exit(phase9Code);
}

main().catch((e) => {
  console.error('\nPhase 2 Step 1 acceptance failed:', e.message);
  process.exit(1);
});
