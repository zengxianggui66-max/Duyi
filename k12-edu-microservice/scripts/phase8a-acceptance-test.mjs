/**
 * Phase 8-A acceptance: audit gaps + system log APIs
 * Usage: node scripts/phase8a-acceptance-test.mjs
 */
const BASE = process.env.PHASE8_GATEWAY || 'http://localhost:9001';
const results = [];

function add(id, pass, detail) {
  results.push({ id, pass: !!pass, detail: String(detail) });
  console.log(`[${pass ? 'PASS' : 'FAIL'}] ${id} - ${detail}`);
}

async function api(method, path, { headers = {}, body } = {}) {
  const opts = { method, headers: { ...headers } };
  if (body !== undefined) {
    opts.headers['Content-Type'] = 'application/json; charset=utf-8';
    opts.body = JSON.stringify(body);
  }
  const res = await fetch(`${BASE}${path}`, opts);
  const text = await res.text();
  let json = null;
  try {
    json = JSON.parse(text);
  } catch {
    /* ignore */
  }
  return { ok: res.ok, http: res.status, json, raw: text };
}

async function login(username, password = 'admin123') {
  const r = await api('POST', '/api/auth/login', { body: { username, password } });
  if (r.json?.code === 200 && r.json.data?.token) {
    return { Authorization: `Bearer ${r.json.data.token}` };
  }
  throw new Error(`Login failed for ${username}: ${r.raw}`);
}

async function main() {
  console.log('=== Phase 8-A Acceptance Test ===\n');

  const adminH = await login('admin');

  // A1: admin login → user_login_log loginType=admin
  const loginLogsAfterAdmin = await api(
    'GET',
    '/api/admin/system/login-logs?loginType=admin&current=1&size=5',
    { headers: adminH },
  );
  const hasAdminLogin = (loginLogsAfterAdmin.json?.data?.records || []).some(
    (r) => r.loginType === 'admin' && r.success === 1 && r.username === 'admin',
  );
  add(
    'A1-admin-login-log',
    loginLogsAfterAdmin.json?.code === 200 && hasAdminLogin,
    `records=${loginLogsAfterAdmin.json?.data?.records?.length ?? 0} hasAdmin=${hasAdminLogin}`,
  );

  // A2: login-logs API
  add(
    'A2-login-logs-api',
    loginLogsAfterAdmin.json?.code === 200,
    `code=${loginLogsAfterAdmin.json?.code}`,
  );

  // A3: resource update → sys_operation_log module=resource action=update
  const resList = await api('GET', '/api/admin/resources?current=1&size=1', { headers: adminH });
  const resourceId = resList.json?.data?.records?.[0]?.id;
  let a3Pass = false;
  if (resourceId) {
    const before = await api('GET', '/api/admin/system/logs?module=resource&action=update&size=1', {
      headers: adminH,
    });
    const beforeTopId = before.json?.data?.records?.[0]?.id ?? 0;
    await api('PUT', `/api/admin/resources/${resourceId}`, {
      headers: adminH,
      body: { description: `phase8a-${Date.now()}` },
    });
    const after = await api('GET', '/api/admin/system/logs?module=resource&action=update&size=1', {
      headers: adminH,
    });
    const afterTopId = after.json?.data?.records?.[0]?.id ?? 0;
    a3Pass = afterTopId > beforeTopId;
  }
  add('A3-resource-update-log', a3Pass, `resourceId=${resourceId ?? 'N/A'}`);

  // A4: user export → module=user action=export
  const beforeExport = await api('GET', '/api/admin/system/logs?module=user&action=export&size=1', {
    headers: adminH,
  });
  const exportTopBefore = beforeExport.json?.data?.records?.[0]?.id ?? 0;
  await api('GET', '/api/admin/users/export?staffOnly=false', { headers: adminH });
  const afterExport = await api('GET', '/api/admin/system/logs?module=user&action=export&size=1', {
    headers: adminH,
  });
  const exportTopAfter = afterExport.json?.data?.records?.[0]?.id ?? 0;
  add(
    'A4-user-export-log',
    exportTopAfter > exportTopBefore,
    `topId before=${exportTopBefore} after=${exportTopAfter}`,
  );

  // A5: 403 also logged (content_admin assign role to admin)
  const contentH = await login('content_admin');
  const before403 = await api(
    'GET',
    '/api/admin/system/logs?module=user&action=assign_admin_roles&status=0&size=1',
    { headers: adminH },
  );
  const failTopBefore = before403.json?.data?.records?.[0]?.id ?? 0;
  await api('PUT', '/api/admin/users/1/roles', {
    headers: contentH,
    body: { roleIds: [2] },
  });
  const after403 = await api(
    'GET',
    '/api/admin/system/logs?module=user&action=assign_admin_roles&status=0&size=1',
    { headers: adminH },
  );
  const failTopAfter = after403.json?.data?.records?.[0]?.id ?? 0;
  add(
    'A5-forbidden-also-logged',
    failTopAfter > failTopBefore,
    `failTopId before=${failTopBefore} after=${failTopAfter}`,
  );

  // A6: old API compatible with system/logs
  const oldLogs = await api('GET', '/api/admin/operation-logs?current=1&size=5', { headers: adminH });
  const newLogs = await api('GET', '/api/admin/system/logs?current=1&size=5', { headers: adminH });
  const oldTotal = oldLogs.json?.data?.total;
  const newTotal = newLogs.json?.data?.total;
  add(
    'A6-old-api-compatible',
    oldLogs.json?.code === 200 && newLogs.json?.code === 200 && oldTotal === newTotal,
    `oldTotal=${oldTotal} newTotal=${newTotal}`,
  );

  // A7: auditor without log_view → 403
  const auditorH = await login('auditor');
  const auditorPerms = await api('GET', '/api/admin/permissions', { headers: auditorH });
  const hasLogView = (auditorPerms.json?.data || []).includes('admin:system:log_view');
  const forbidden = await api('GET', '/api/admin/system/logs?current=1&size=5', { headers: auditorH });
  add(
    'A7-permission-denied',
    !hasLogView && forbidden.json?.code === 403,
    `auditor log_view=${hasLogView} code=${forbidden.json?.code}`,
  );

  const passed = results.filter((r) => r.pass).length;
  console.log(`\n=== Summary: ${passed}/${results.length} passed ===`);
  process.exit(results.some((r) => !r.pass) ? 1 : 0);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
