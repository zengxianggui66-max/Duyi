/**
 * Phase 3-P0 验收：台账 + 开关 + 比对脚本可执行
 */
import { api, createReporter, login } from './phase9-test-utils.mjs';

async function main() {
  const r = createReporter();
  const adminH = await login('admin', process.env.PHASE9_ADMIN_PASSWORD || 'admin123');

  const flags = await api('GET', '/api/admin/system/feature-flags', { headers: adminH });
  const keys = new Set((flags.json?.data?.flags || []).map((f) => f.key));
  const requiredFlags = [
    'resourceUnifiedReadEnabled',
    'topicUnifiedReadEnabled',
    'cultureUnifiedReadEnabled',
    'competitionUnifiedReadEnabled',
    'primaryChineseUnifiedReadEnabled',
  ];
  r.add(
    'P0-A1-flags-exist',
    requiredFlags.every((k) => keys.has(k)),
    `found=${requiredFlags.filter((k) => keys.has(k)).length}/${requiredFlags.length}`,
  );

  const ledgerList = await api('GET', '/api/admin/resource-main/migration-ledger', { headers: adminH });
  const rows = ledgerList.json?.data || [];
  r.add(
    'P0-A2-ledger-list',
    ledgerList.json?.code === 200 && Array.isArray(rows) && rows.length >= 4,
    `rows=${Array.isArray(rows) ? rows.length : 0}`,
  );

  const upsert = await api('PUT', '/api/admin/resource-main/migration-ledger/topic_resource', {
    headers: adminH,
    body: {
      compareStatus: 1,
      comparePassRate: 99,
      notes: 'phase3p0 acceptance touch',
    },
  });
  r.add(
    'P0-A3-ledger-upsert',
    upsert.json?.code === 200 && upsert.json?.data?.sourceType === 'topic_resource',
    `code=${upsert.json?.code ?? upsert.http}`,
  );

  process.exit(r.summary('Phase3-P0 Acceptance'));
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
