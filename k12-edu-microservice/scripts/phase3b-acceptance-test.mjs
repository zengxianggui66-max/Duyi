/**
 * Phase 3B 全量验收：
 * 1) 数据库结构与回填验收
 * 2) 继承 P0 发布门禁，防止 3B 影响主链稳定
 */
import { spawnSync } from 'node:child_process';

function runNodeScript(script) {
  const ret = spawnSync(process.execPath, [script], {
    stdio: 'inherit',
    cwd: process.cwd(),
    env: process.env,
  });
  return ret.status ?? 1;
}

function main() {
  console.log('=== Phase3-B Full Acceptance ===\n');

  const db = runNodeScript('scripts/phase3b-db-acceptance.mjs');
  if (db !== 0) {
    process.exit(db);
  }

  const gate = runNodeScript('scripts/phase3p0-release-gate.mjs');
  if (gate !== 0) {
    process.exit(gate);
  }

  console.log('\n=== Phase3-B Full Acceptance: PASS ===');
  process.exit(0);
}

main();
