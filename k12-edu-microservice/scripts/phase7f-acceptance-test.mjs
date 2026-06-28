/**
 * Phase 7-F acceptance: cache / schedule / preview / searchEngine auto
 */
const BASE = process.env.PHASE7_GATEWAY || 'http://localhost:9001';
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
  try { json = JSON.parse(text); } catch { /* ignore */ }
  return { ok: res.ok, http: res.status, json, raw: text };
}
async function login() {
  for (const u of ['content_admin', 'admin']) {
    const r = await api('POST', '/api/auth/login', { body: { username: u, password: 'admin123' } });
    if (r.json?.code === 200 && r.json.data?.token) {
      return { Authorization: `Bearer ${r.json.data.token}` };
    }
  }
  throw new Error('Login failed');
}
async function main() {
  console.log('=== Phase 7-F Acceptance Test ===');
  const adminH = await login();

  const stats = await api('GET', '/api/admin/home/cache/stats', { headers: adminH });
  add('F1-cache-stats', stats.json?.code === 200 && stats.json?.data?.enabled === true,
    `enabled=${stats.json?.data?.enabled}`);

  const preview = await api('GET', '/api/admin/home/preview?stageKey=primary', { headers: adminH });
  add('F2-admin-preview', preview.json?.code === 200 && preview.json?.data?.hero,
    `columns=${preview.json?.data?.latestColumns?.length ?? 0}`);

  const bootstrap = await api('GET', '/api/home/bootstrap?stage=primary');
  add('F3-c-bootstrap', bootstrap.json?.code === 200 && bootstrap.json?.data?.hero,
    `banners=${bootstrap.json?.data?.hero?.banners?.length ?? 0}`);

  const inv = await api('POST', '/api/admin/home/cache/invalidate', { headers: adminH });
  add('F4-cache-invalidate', inv.json?.code === 200, `code=${inv.json?.code}`);

  const sched = await api('POST', '/api/admin/home/schedule/run', { headers: adminH });
  add('F5-schedule-run', sched.json?.code === 200, `changed=${sched.json?.data?.changed ?? 'N/A'}`);

  const searchAuto = await api('GET', '/api/search/all?q=数学&page=1&size=5&searchEngine=auto');
  add('F6-search-auto', searchAuto.json?.code === 200, `total=${searchAuto.json?.data?.total ?? 0}`);

  const health = await api('GET', '/api/search/admin/engine/health', { headers: adminH });
  add('F7-engine-health', health.json?.code === 200, `configured=${health.json?.data?.configured ?? 'N/A'}`);

  const passed = results.filter((r) => r.pass).length;
  console.log(`\n=== Summary: ${passed}/${results.length} passed ===`);
  process.exit(results.some((r) => !r.pass) ? 1 : 0);
}
main().catch((e) => { console.error(e); process.exit(1); });
