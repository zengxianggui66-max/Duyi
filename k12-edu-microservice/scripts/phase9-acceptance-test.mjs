/**
 * Phase 9 unified acceptance (9-A ~ 9-E)
 * Usage: node scripts/phase9-acceptance-test.mjs
 */
import { spawn } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const scripts = [
  'phase9a-acceptance-test.mjs',
  'phase9b-acceptance-test.mjs',
  'phase9c-acceptance-test.mjs',
  'phase9d-acceptance-test.mjs',
  'phase9e-acceptance-test.mjs',
];

function run(script) {
  return new Promise((resolve) => {
    const file = path.join(__dirname, script);
    const child = spawn(process.execPath, [file], {
      stdio: 'inherit',
      env: process.env,
    });
    child.on('close', (code) => resolve({ script, code: code ?? 1 }));
  });
}

async function main() {
  console.log('========== Phase 9 Full Acceptance (A→E) ==========\n');
  const summary = [];
  for (const script of scripts) {
    console.log(`\n>>> Running ${script}\n`);
    const result = await run(script);
    summary.push(result);
  }

  console.log('\n========== Phase 9 Summary ==========');
  let failed = 0;
  for (const { script, code } of summary) {
    const ok = code === 0;
    if (!ok) failed++;
    console.log(`${ok ? 'PASS' : 'FAIL'}  ${script}`);
  }
  console.log(`\nTotal: ${summary.length - failed}/${summary.length} scripts passed`);
  if (failed > 0) {
    const failedScripts = summary.filter((s) => s.code !== 0).map((s) => s.script);
    if (failedScripts.some((s) => s.includes('phase9a'))) {
      console.log('\n提示 A6：请重启 k12-gateway(9001) 与 k12-resource(8082) 使 /api/search/admin/** 返回 404');
    }
    if (failedScripts.some((s) => s.includes('phase9d') || s.includes('phase9e'))) {
      console.log('提示 D7/E8：请执行 sql/79_phase9_post_acceptance_fix.sql 移除 auditor 的 admin:dashboard:view');
    }
  }
  process.exit(failed === 0 ? 0 : 1);
}

main().catch((e) => {
  console.error('\nPhase 9 unified test failed:', e.message);
  process.exit(1);
});
