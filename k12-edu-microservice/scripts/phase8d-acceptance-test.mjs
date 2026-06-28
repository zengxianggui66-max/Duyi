/**
 * Phase 8-D acceptance: feature flags public + admin
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

async function login(username = 'admin', password = 'admin123') {
  const r = await api('POST', '/api/auth/login', { body: { username, password } });
  if (r.json?.code === 200 && r.json.data?.token) {
    return { Authorization: `Bearer ${r.json.data.token}` };
  }
  throw new Error(`Login failed: ${r.raw}`);
}

async function main() {
  console.log('=== Phase 8-D Acceptance Test ===\n');

  const pub = await api('GET', '/api/public/feature-flags');
  add(
    'D1-public-flags',
    pub.json?.code === 200 && pub.json?.data?.homeOpsApiEnabled != null,
    `keys=${Object.keys(pub.json?.data ?? {}).join(',')}`,
  );

  const adminH = await login();
  const adminFlags = await api('GET', '/api/admin/system/feature-flags', { headers: adminH });
  add(
    'D2-admin-flags',
    adminFlags.json?.code === 200 && (adminFlags.json?.data?.flags?.length ?? 0) >= 4,
    `count=${adminFlags.json?.data?.flags?.length ?? 0}`,
  );

  const beforeLog = await api(
    'GET',
    '/api/admin/system/logs?module=system&action=update_config&size=1',
    { headers: adminH },
  );
  const logBefore = beforeLog.json?.data?.records?.[0]?.id ?? 0;
  const toggled = !(pub.json?.data?.homeOpsApiEnabled ?? true);
  await api('PUT', '/api/admin/system/config?group=feature', {
    headers: adminH,
    body: { homeOpsApiEnabled: toggled },
  });
  const afterLog = await api(
    'GET',
    '/api/admin/system/logs?module=system&action=update_config&size=1',
    { headers: adminH },
  );
  add(
    'D3-put-audit',
    (afterLog.json?.data?.records?.[0]?.id ?? 0) > logBefore,
    `logId before=${logBefore} after=${afterLog.json?.data?.records?.[0]?.id ?? 0}`,
  );

  const pubAfter = await api('GET', '/api/public/feature-flags');
  add(
    'D4-public-reflects',
    pubAfter.json?.data?.homeOpsApiEnabled === toggled,
    `homeOpsApiEnabled=${pubAfter.json?.data?.homeOpsApiEnabled}`,
  );

  const passed = results.filter((r) => r.pass).length;
  console.log(`\n=== Summary: ${passed}/${results.length} passed ===`);
  process.exit(results.some((r) => !r.pass) ? 1 : 0);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
