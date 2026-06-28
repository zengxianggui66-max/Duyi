/**
 * Phase 8 full acceptance (8-A through 8-F)
 * Usage: node scripts/phase8-acceptance-test.mjs
 */
import { spawnSync } from 'child_process';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const dir = dirname(fileURLToPath(import.meta.url));
const scripts = [
  'phase8a-acceptance-test.mjs',
  'phase8b-acceptance-test.mjs',
  'phase8c-acceptance-test.mjs',
  'phase8d-acceptance-test.mjs',
  'phase8e-acceptance-test.mjs',
  'phase8f-acceptance-test.mjs',
];

const expectedCounts = { a: 7, b: 6, c: 4, d: 4, e: 6, f: 7 };
const totalExpected = Object.values(expectedCounts).reduce((a, b) => a + b, 0);

let failed = false;
console.log('========== Phase 8 Full Acceptance (8-A ~ 8-F) ==========');
console.log(`Expected automated cases: ${totalExpected}+\n`);

for (const script of scripts) {
  console.log(`--- ${script} ---`);
  const r = spawnSync(process.execPath, [join(dir, script)], {
    stdio: 'inherit',
    env: process.env,
  });
  if (r.status !== 0) failed = true;
  console.log('');
}

console.log(failed ? '=== Phase 8 FULL: FAILED ===' : `=== Phase 8 FULL: ALL PASSED (${totalExpected}/${totalExpected}) ===`);
console.log('Manual sign-off: docs/Phase8-F-验收.md');
process.exit(failed ? 1 : 0);
