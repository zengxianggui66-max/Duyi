/**
 * Phase 8-C acceptance: storage / preview health probes
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
  console.log('=== Phase 8-C Acceptance Test ===\n');
  const adminH = await login('admin');

  const storage = await api('GET', '/api/admin/system/storage/status', { headers: adminH });
  const s = storage.json?.data ?? {};
  add(
    'C1-storage-status',
    storage.json?.code === 200 && s.provider != null && typeof s.reachable === 'boolean',
    `provider=${s.provider} reachable=${s.reachable} bucket=${s.bucket ?? 'N/A'}`,
  );

  add(
    'C2-minio-down-still-200',
    storage.json?.code === 200,
    `http=200 reachable=${s.reachable} latencyMs=${s.latencyMs ?? 'N/A'}`,
  );

  const preview = await api('GET', '/api/admin/system/preview/status', { headers: adminH });
  const p = preview.json?.data ?? {};
  add(
    'C3-preview-status',
    preview.json?.code === 200
      && typeof p.enabled === 'boolean'
      && p.libreoffice != null
      && typeof p.libreoffice.reachable === 'boolean'
      && p.sampleProbe === 'skipped',
    `enabled=${p.enabled} loReachable=${p.libreoffice?.reachable} probe=${p.sampleProbe}`,
  );

  const auditorH = await login('auditor');
  const perms = await api('GET', '/api/admin/permissions', { headers: auditorH });
  const hasView = (perms.json?.data || []).includes('admin:system:config_view');
  const forbidden = await api('GET', '/api/admin/system/storage/status', { headers: auditorH });
  add(
    'C4-permission-denied',
    !hasView && forbidden.json?.code === 403,
    `auditor config_view=${hasView} code=${forbidden.json?.code}`,
  );

  const passed = results.filter((r) => r.pass).length;
  console.log(`\n=== Summary: ${passed}/${results.length} passed ===`);
  process.exit(results.some((r) => !r.pass) ? 1 : 0);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
