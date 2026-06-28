/**

 * Phase 9-D acceptance: analytics dashboard

 * Usage: node scripts/phase9d-acceptance-test.mjs

 */

import {

  createReporter,

  api,

  login,

  isForbidden,

  hasTrend,

  permCodes,

} from './phase9-test-utils.mjs';



const { add, summary } = createReporter();



async function main() {

  console.log('=== Phase 9-D Acceptance Test ===\n');

  const adminH = await login('admin');

  let contentH = null;

  try {

    contentH = await login('content_admin');

  } catch {

    /* optional */

  }



  const users = await api('GET', '/api/admin/analytics/users?days=30', { headers: adminH });

  const u = users.json?.data;

  add(

    'D1-user-analytics',

    users.json?.code === 200 && u && typeof u.totalUsers === 'number' && u.totalUsers >= 0,

    `totalUsers=${u?.totalUsers} trendLen=${u?.registrationTrend?.length ?? 0}`,

  );



  const dash = await api('GET', '/api/admin/analytics/dashboard?days=7', { headers: adminH });

  const d = dash.json?.data;

  const s = d?.summary;

  add(

    'D2-dashboard-summary',

    dash.json?.code === 200 && s && typeof s.totalResources === 'number',

    `resources=${s?.totalResources} downloads=${s?.totalDownloads}`,

  );



  add(

    'D3-upload-trend-7d',

    hasTrend(d?.resourceUploadTrend, 7),

    `uploadTrend=${d?.resourceUploadTrend?.length ?? 0}`,

  );

  add(

    'D3-action-trend-7d',

    Array.isArray(d?.actionTrend) &&

      d.actionTrend.length >= 7 &&

      d.actionTrend.every((r) => r.date && typeof r.views === 'number'),

    `actionTrend=${d?.actionTrend?.length ?? 0}`,

  );



  const audit = d?.audit;

  add(

    'D4-audit-stats',

    audit && typeof audit.approved === 'number' && typeof audit.rejected === 'number',

    `approved=${audit?.approved} rejected=${audit?.rejected} passRate=${audit?.passRate}`,

  );



  add(

    'D5-distributions',

    Array.isArray(d?.stageDistribution) && Array.isArray(d?.subjectDistribution),

    `stages=${d?.stageDistribution?.length ?? 0} subjects=${d?.subjectDistribution?.length ?? 0}`,

  );

  add(

    'D5-top-resources',

    Array.isArray(d?.topByDownload) &&

      Array.isArray(d?.topByView) &&

      Array.isArray(d?.topByCollect),

    `dl=${d?.topByDownload?.length ?? 0} view=${d?.topByView?.length ?? 0} collect=${d?.topByCollect?.length ?? 0}`,

  );



  if (contentH) {

    const scoped = await api('GET', '/api/admin/analytics/dashboard?days=7', { headers: contentH });

    const sd = scoped.json?.data;

    add(

      'D6-content-admin-scoped',

      scoped.json?.code === 200 && sd?.scoped === true,

      `scoped=${sd?.scoped} hint=${sd?.scopeHint ?? 'none'}`,

    );

    const adminTotal = d?.summary?.totalResources ?? 0;

    const scopedTotal = sd?.summary?.totalResources ?? 0;

    add(

      'D6-scoped-not-wider-than-all',

      scopedTotal <= adminTotal,

      `adminTotal=${adminTotal} scopedTotal=${scopedTotal}`,

    );

  } else {

    add('D6-content-admin-scoped', false, 'content_admin login unavailable — 请执行 sql/52 或 sql/78');

    add('D6-scoped-not-wider-than-all', false, 'skipped');

  }



  const auditorH = await login('auditor');

  const auditorPerms = await api('GET', '/api/admin/permissions', { headers: auditorH });

  const auditorCodes = permCodes(auditorPerms.json);

  add(

    'D7-auditor-no-analytics-perm',

    !auditorCodes.includes('admin:analytics:view') && !auditorCodes.includes('admin:dashboard:view'),

    `dashboard=${auditorCodes.includes('admin:dashboard:view')} analytics=${auditorCodes.includes('admin:analytics:view')}`,

  );



  const forbiddenDash = await api('GET', '/api/admin/analytics/dashboard?days=7', { headers: auditorH });

  add(

    'D7-auditor-dashboard-403',

    isForbidden(forbiddenDash),

    `http=${forbiddenDash.http} code=${forbiddenDash.json?.code}`,

  );



  const forbiddenUsers = await api('GET', '/api/admin/analytics/users?days=30', { headers: auditorH });

  add(

    'D7-auditor-users-403',

    isForbidden(forbiddenUsers),

    `http=${forbiddenUsers.http} code=${forbiddenUsers.json?.code}`,

  );



  process.exit(summary('Phase 9-D'));

}



main().catch((e) => {

  console.error(e);

  process.exit(1);

});


