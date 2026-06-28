/**
 * Phase 3A 全量验收：
 * 1) 契约冻结验收
 * 2) 继承 P0 对比与发布门禁
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
  console.log('=== Phase3-A Full Acceptance ===\n');

  const c1 = runNodeScript('scripts/phase3a-contract-acceptance.mjs');
  if (c1 !== 0) {
    process.exit(c1);
  }

  const c2 = runNodeScript('scripts/phase3p0-release-gate.mjs');
  if (c2 !== 0) {
    process.exit(c2);
  }

  console.log('\n=== Phase3-A Full Acceptance: PASS ===');
  process.exit(0);
}

main();
