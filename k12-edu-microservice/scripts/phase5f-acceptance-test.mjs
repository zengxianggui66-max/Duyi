/**
 * Phase 5-F subject-nav API acceptance
 * Usage: node scripts/phase5f-acceptance-test.mjs
 */
const BASE = process.env.PHASE5_GATEWAY || 'http://localhost:9001';

const results = [];

function add(id, pass, detail) {
  results.push({ id, pass: !!pass, detail: String(detail) });
  console.log(`[${pass ? 'PASS' : 'FAIL'}] ${id} - ${detail}`);
}

async function api(method, path, { headers = {}, body } = {}) {
  const url = `${BASE}${path}`;
  const opts = { method, headers: { ...headers } };
  if (body !== undefined) {
    opts.headers['Content-Type'] = 'application/json; charset=utf-8';
    opts.body = JSON.stringify(body);
  }
  try {
    const res = await fetch(url, opts);
    const text = await res.text();
    let json = null;
    try {
      json = JSON.parse(text);
    } catch {
      /* ignore */
    }
    return { ok: res.ok, http: res.status, json, raw: text };
  } catch (e) {
    return { ok: false, http: 0, json: null, raw: null, error: e.message };
  }
}

async function login(username) {
  const r = await api('POST', '/api/auth/login', { body: { username, password: 'admin123' } });
  if (!r.json || r.json.code !== 200 || !r.json.data?.token) {
    throw new Error(`Login failed for ${username}: ${r.raw}`);
  }
  return { Authorization: `Bearer ${r.json.data.token}` };
}

function summarize() {
  const passed = results.filter((r) => r.pass).length;
  console.log(`\n=== Summary: ${passed}/${results.length} passed ===`);
}

async function main() {
  console.log('=== Phase 5-F Acceptance Test ===');
  let adminH;
  try {
    adminH = await login('content_admin');
  } catch {
    adminH = await login('admin');
  }

  const navPrimaryChinese = await api(
    'GET',
    '/api/home/subject-nav?stage=primary&subject=chinese',
  );
  const d1 = navPrimaryChinese.json?.data;
  add(
    'F1-nav-primary-chinese',
    navPrimaryChinese.json?.code === 200 && d1?.subject?.code === 'chinese',
    `editions=${d1?.syncPrep?.editions?.length ?? 0} types=${d1?.syncPrep?.resourceTypes?.length ?? 0}`,
  );

  const reviewNames = (d1?.reviewPrep?.modules || []).map((m) => m.name);
  add(
    'F3-guoxue-primary-chinese',
    reviewNames.includes('国学阅读'),
    `review modules include guoxue=${reviewNames.includes('国学阅读')}`,
  );

  const navPreschool = await api(
    'GET',
    '/api/home/subject-nav?stage=preschool&subject=chinese',
  );
  const d2 = navPreschool.json?.data;
  const preschoolReview = (d2?.reviewPrep?.modules || []).map((m) => m.name);
  add(
    'F2-preschool-no-guoxue',
    !preschoolReview.includes('国学阅读'),
    `preschool review count=${preschoolReview.length}`,
  );

  const taxonomySubjects = await api('GET', '/api/taxonomy/subjects?stage=primary');
  add(
    'F6-taxonomy-subjects',
    (taxonomySubjects.json?.data?.length ?? 0) >= 1,
    `primary subjects=${taxonomySubjects.json?.data?.length ?? 0}`,
  );

  const adminSubjects = await api('GET', '/api/admin/taxonomy/subjects?stageId=1', {
    headers: adminH,
  });
  const chineseRow = (adminSubjects.json?.data || []).find((s) => s.code === 'chinese');
  add(
    'F4-admin-subject-bindings',
    Array.isArray(chineseRow?.moduleIds) && Array.isArray(chineseRow?.resourceTypeIds),
    `chinese moduleIds=${chineseRow?.moduleIds?.length ?? 'N/A'} typeIds=${chineseRow?.resourceTypeIds?.length ?? 'N/A'}`,
  );

  const invalidNav = await api('GET', '/api/home/subject-nav?stage=primary&subject=not_exist_xyz');
  add(
    'F7-invalid-subject-empty',
    invalidNav.json?.code === 200 && !invalidNav.json?.data?.subject?.code,
    `empty subject payload`,
  );

  summarize();
  const failed = results.some((r) => !r.pass);
  process.exit(failed ? 1 : 0);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
