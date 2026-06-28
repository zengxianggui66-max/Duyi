/**
 * Phase 9-C acceptance: search hot keyword admin
 * Usage: node scripts/phase9c-acceptance-test.mjs
 */
import { createReporter, api, login, isForbidden } from './phase9-test-utils.mjs';

const { add, summary } = createReporter();

async function main() {
  console.log('=== Phase 9-C Acceptance Test ===\n');
  const adminH = await login('admin');
  const auditorH = await login('auditor');

  const list = await api('GET', '/api/admin/search/hot-keywords?includeDisabled=true', { headers: adminH });
  const rows = list.json?.data || [];
  add('C1-list-hot-keywords', list.json?.code === 200 && Array.isArray(rows), `count=${rows.length}`);

  const target = rows.find((r) => r.status === 1 && r.id);
  if (!target) {
    add('C2-boost-score', false, 'no enabled hot keyword — 请先保留至少一条启用热词');
    add('C3-disable-hides-public', false, 'skipped');
    add('C6-boost-oplog', false, 'skipped');
  } else {
    const boostVal = 8888;
    const boost = await api('PUT', `/api/admin/search/hot-keywords/${target.id}/boost?boostScore=${boostVal}`, {
      headers: adminH,
    });
    add(
      'C2-boost-score',
      boost.json?.code === 200 && boost.json?.data?.boostScore === boostVal,
      `boost=${boost.json?.data?.boostScore}`,
    );

    await api('PUT', `/api/admin/search/hot-keywords/${target.id}/boost?boostScore=${target.boostScore ?? 0}`, {
      headers: adminH,
    });

    const kw = target.keyword;
    await api('PUT', `/api/admin/search/hot-keywords/${target.id}/status?status=0`, { headers: adminH });
    const pubHot = await api('GET', '/api/search/hot-keywords?limit=50');
    const pubKeywords = (pubHot.json?.data || []).map((x) => x.keyword);
    add(
      'C3-disable-hides-public',
      pubHot.json?.code === 200 && !pubKeywords.includes(kw),
      `keyword=${kw} inPublic=${pubKeywords.includes(kw)}`,
    );
    await api('PUT', `/api/admin/search/hot-keywords/${target.id}/status?status=1`, { headers: adminH });

    const before = await api('GET', '/api/admin/system/logs?module=search&action=boost_hot_keyword&size=1', {
      headers: adminH,
    });
    const beforeTopId = before.json?.data?.records?.[0]?.id ?? 0;
    await api('PUT', `/api/admin/search/hot-keywords/${target.id}/boost?boostScore=100`, { headers: adminH });
    const after = await api('GET', '/api/admin/system/logs?module=search&action=boost_hot_keyword&size=1', {
      headers: adminH,
    });
    const afterTopId = after.json?.data?.records?.[0]?.id ?? 0;
    add('C6-boost-oplog', afterTopId > beforeTopId, `logId ${beforeTopId}->${afterTopId}`);
    await api('PUT', `/api/admin/search/hot-keywords/${target.id}/boost?boostScore=${target.boostScore ?? 0}`, {
      headers: adminH,
    });
  }

  const pub = await api('GET', '/api/search/hot-keywords?limit=5');
  add('C4-public-hot-keywords', pub.json?.code === 200, `count=${pub.json?.data?.length ?? 0}`);

  const firstId = rows[0]?.id;
  if (!firstId) {
    add('C5-auditor-403', false, 'no hot keyword rows');
  } else {
    const auditorToggle = await api('PUT', `/api/admin/search/hot-keywords/${firstId}/status?status=1`, {
      headers: auditorH,
    });
    add('C5-auditor-403', isForbidden(auditorToggle), `firstId=${firstId} http=${auditorToggle.http}`);
  }

  const list2 = await api('GET', '/api/admin/search/hot-keywords', { headers: adminH });
  const hasRank = (list2.json?.data || []).every((r, i) => r.rank === i + 1);
  add('C7-rank-computed', list2.json?.code === 200 && hasRank, `hasRank=${hasRank}`);

  process.exit(summary('Phase 9-C'));
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
