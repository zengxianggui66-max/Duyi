/**
 * Phase 7 full acceptance (7-A through 7-F)
 * Usage: node scripts/phase7-acceptance-test.mjs
 */
import { spawnSync } from 'child_process';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const dir = dirname(fileURLToPath(import.meta.url));
const scripts = [
  'phase7a-acceptance-test.mjs',
  'phase7b-acceptance-test.mjs',
  'phase7c-acceptance-test.mjs',
  'phase7d-acceptance-test.mjs',
  'phase7e-acceptance-test.mjs',
  'phase7f-acceptance-test.mjs',
];

let failed = false;
console.log('========== Phase 7 Full Acceptance ==========\n');
for (const script of scripts) {
  console.log(`--- ${script} ---`);
  const r = spawnSync(process.execPath, [join(dir, script)], {
    stdio: 'inherit',
    env: process.env,
  });
  if (r.status !== 0) failed = true;
  console.log('');
}
console.log(failed ? '=== Phase 7 FULL: FAILED ===' : '=== Phase 7 FULL: ALL PASSED ===');
process.exit(failed ? 1 : 0);
