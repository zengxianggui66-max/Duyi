/**
 * Phase 3-P0: 发布门禁
 * 未达到比对阈值时，阻止切流量。
 */
import { runComparison } from './phase3p0-compare-resource-apis.mjs';

async function main() {
  const threshold = Number(process.env.P0_COMPARE_PASS_THRESHOLD ?? 95);
  const allowEmptyAnomaly = String(process.env.P0_ALLOW_EMPTY_ANOMALY ?? 'false') === 'true';
  const report = await runComparison();

  const passRateOk = report.overallPassRate >= threshold;
  const emptyOk = allowEmptyAnomaly || !report.hasEmptyAnomaly;
  const gatePassed = passRateOk && emptyOk;

  console.log(
    `RELEASE_GATE threshold=${threshold} passRate=${report.overallPassRate} ` +
      `emptyAnomaly=${report.hasEmptyAnomaly} allowEmptyAnomaly=${allowEmptyAnomaly} result=${gatePassed ? 'PASS' : 'BLOCK'}`,
  );

  if (!gatePassed) {
    process.exit(1);
  }
  process.exit(0);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
