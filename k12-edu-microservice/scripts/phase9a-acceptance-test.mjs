/**

 * Phase 9-A acceptance: search ops dashboard + admin search APIs

 * Usage: node scripts/phase9a-acceptance-test.mjs

 */

import {

  createReporter,

  api,

  login,

  isForbidden,

  legacySearchAdminGone,

} from './phase9-test-utils.mjs';



const { add, summary } = createReporter();



async function main() {

  console.log('=== Phase 9-A Acceptance Test ===\n');



  const adminH = await login('admin');

  const auditorH = await login('auditor');



  const stats7 = await api('GET', '/api/admin/search/stats?days=7', { headers: adminH });

  add(

    'A1-stats-7d',

    stats7.json?.code === 200 && typeof stats7.json?.data?.totalQueries === 'number',

    `code=${stats7.json?.code} totalQueries=${stats7.json?.data?.totalQueries}`,

  );



  const stats30 = await api('GET', '/api/admin/search/stats?days=30', { headers: adminH });

  add('A2-stats-30d', stats30.json?.code === 200, `code=${stats30.json?.code}`);



  const readiness = await api('GET', '/api/admin/search/p3-readiness?days=7', { headers: adminH });

  add('A3-p3-readiness', readiness.json?.code === 200, `code=${readiness.json?.code}`);



  const health = await api('GET', '/api/admin/search/engine/health', { headers: adminH });

  add('A4-engine-health', health.json?.code === 200, `code=${health.json?.code}`);



  const auditorStats = await api('GET', '/api/admin/search/stats?days=7', { headers: auditorH });

  add(

    'A5-auditor-403',

    isForbidden(auditorStats),

    `http=${auditorStats.http} code=${auditorStats.json?.code}`,

  );



  // A6: 旧路径必须 404（网关）且不可再返回有效 stats

  const legacyAuth = await api('GET', '/api/search/admin/stats?days=7', { headers: adminH });

  const legacyAnon = await api('GET', '/api/search/admin/stats?days=7');

  add(

    'A6-legacy-path-gone',

    legacyAuth.http === 404 && legacySearchAdminGone(legacyAuth),

    `authHttp=${legacyAuth.http} bizCode=${legacyAuth.json?.code ?? 'n/a'}`,

  );

  add(

    'A6-legacy-anon-404',

    legacyAnon.http === 404,

    `anonHttp=${legacyAnon.http}`,

  );



  const before = await api('GET', '/api/admin/system/logs?module=search&action=reindex&size=1', {

    headers: adminH,

  });

  const beforeTopId = before.json?.data?.records?.[0]?.id ?? 0;

  const reindex = await api('POST', '/api/admin/search/reindex', { headers: adminH });

  const after = await api('GET', '/api/admin/system/logs?module=search&action=reindex&size=1', {

    headers: adminH,

  });

  const afterTopId = after.json?.data?.records?.[0]?.id ?? 0;

  add(

    'A7-reindex-oplog',

    reindex.json?.code === 200 && afterTopId > beforeTopId,

    `reindexCode=${reindex.json?.code} logId ${beforeTopId}->${afterTopId}`,

  );



  process.exit(summary('Phase 9-A'));

}



main().catch((e) => {

  console.error(e);

  process.exit(1);

});


