/**

 * Phase 9-E acceptance: search + analytics RBAC & menus

 * Usage: node scripts/phase9e-acceptance-test.mjs

 */

import {

  createReporter,

  api,

  login,

  isForbidden,

  permCodes,

  hasMenuTree,

  menuTitles,

} from './phase9-test-utils.mjs';



const { add, summary } = createReporter();



async function main() {

  console.log('=== Phase 9-E Acceptance Test ===\n');

  const adminH = await login('admin');

  const operatorH = await login('operator');

  const contentH = await login('content_admin');

  const auditorH = await login('auditor');



  const adminPerms = await api('GET', '/api/admin/permissions', { headers: adminH });

  const codes = permCodes(adminPerms.json);

  add(

    'E1-admin-perms',

    codes.includes('admin:analytics:view') &&

      codes.includes('admin:search:view') &&

      codes.includes('admin:search:edit') &&

      codes.includes('admin:search:reindex'),

    `analytics=${codes.includes('admin:analytics:view')} search=${codes.includes('admin:search:view')}`,

  );



  const adminMenus = await api('GET', '/api/admin/menus', { headers: adminH });

  add(

    'E2-search-submenu',

    hasMenuTree(adminMenus.json, '搜索运营', '无结果词') && hasMenuTree(adminMenus.json, '搜索运营', '搜索热词'),

    `titles=${menuTitles(adminMenus.json).filter((t) => t.includes('搜索') || t.includes('数据')).join('|')}`,

  );

  add(

    'E2-analytics-submenu',

    hasMenuTree(adminMenus.json, '数据分析', '运营概览') && hasMenuTree(adminMenus.json, '数据分析', '资源分析'),

    `hasUsers=${hasMenuTree(adminMenus.json, '数据分析', '用户与行为')}`,

  );



  const opPerms = await api('GET', '/api/admin/permissions', { headers: operatorH });

  const opCodes = permCodes(opPerms.json);

  add(

    'E3-operator-readonly',

    opCodes.includes('admin:search:view') &&

      opCodes.includes('admin:analytics:view') &&

      !opCodes.includes('admin:search:edit') &&

      !opCodes.includes('admin:search:reindex'),

    `edit=${opCodes.includes('admin:search:edit')} reindex=${opCodes.includes('admin:search:reindex')}`,

  );



  const ctPerms = await api('GET', '/api/admin/permissions', { headers: contentH });

  const ctCodes = permCodes(ctPerms.json);

  add(

    'E4-content-admin-edit',

    ctCodes.includes('admin:search:edit') &&

      ctCodes.includes('admin:search:reindex') &&

      ctCodes.includes('admin:analytics:view'),

    `edit=${ctCodes.includes('admin:search:edit')} reindex=${ctCodes.includes('admin:search:reindex')}`,

  );



  const opDash = await api('GET', '/api/admin/analytics/dashboard?days=7', { headers: operatorH });

  add('E5-operator-analytics-api', opDash.json?.code === 200, `http=${opDash.http}`);



  const opReindex = await api('POST', '/api/admin/search/reindex', { headers: operatorH });

  add('E6-operator-reindex-403', isForbidden(opReindex), `http=${opReindex.http}`);



  const ctUsers = await api('GET', '/api/admin/analytics/users?days=30', { headers: contentH });

  add('E7-content-analytics-users', ctUsers.json?.code === 200, `http=${ctUsers.http}`);



  const auPerms = await api('GET', '/api/admin/permissions', { headers: auditorH });

  const auCodes = permCodes(auPerms.json);

  add(

    'E8-auditor-no-dashboard-analytics',

    !auCodes.includes('admin:dashboard:view') && !auCodes.includes('admin:analytics:view'),

    `dashboard=${auCodes.includes('admin:dashboard:view')} analytics=${auCodes.includes('admin:analytics:view')}`,

  );



  const auDash = await api('GET', '/api/admin/analytics/dashboard?days=7', { headers: auditorH });

  add('E9-auditor-analytics-403', isForbidden(auDash), `http=${auDash.http} code=${auDash.json?.code}`);



  process.exit(summary('Phase 9-E'));

}



main().catch((e) => {

  console.error(e);

  process.exit(1);

});


