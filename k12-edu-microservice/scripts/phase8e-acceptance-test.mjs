/**
 * Phase 8-E acceptance: system admin pages API wiring
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
  return { json };
}

async function login() {
  const r = await api('POST', '/api/auth/login', {
    body: { username: 'admin', password: 'admin123' },
  });
  return { Authorization: `Bearer ${r.json.data.token}` };
}

async function main() {
  console.log('=== Phase 8-E Acceptance Test ===\n');
  const H = await login();

  const checks = [
    ['E1-logs', '/api/admin/system/logs?current=1&size=1'],
    ['E2-login-logs', '/api/admin/system/login-logs?current=1&size=1'],
    ['E3-upload-config', '/api/admin/system/config?group=upload'],
    ['E4-preview-config', '/api/admin/system/config?group=preview'],
    ['E5-storage', '/api/admin/system/storage/status'],
    ['E6-feature-flags', '/api/admin/system/feature-flags'],
  ];

  for (const [id, path] of checks) {
    const r = await api('GET', path, { headers: H });
    add(id, r.json?.code === 200, `code=${r.json?.code}`);
  }

  const passed = results.filter((r) => r.pass).length;
  console.log(`\n=== Summary: ${passed}/${results.length} passed ===`);
  process.exit(results.some((r) => !r.pass) ? 1 : 0);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
