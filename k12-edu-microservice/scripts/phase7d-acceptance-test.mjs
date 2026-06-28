/**
 * Phase 7-D home latest columns acceptance
 * Usage: node scripts/phase7d-acceptance-test.mjs
 */
const BASE = process.env.PHASE7_GATEWAY || 'http://localhost:9001';

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
  console.log('=== Phase 7-D Acceptance Test ===');
  let adminH;
  try {
    adminH = await login('content_admin');
  } catch {
    adminH = await login('admin');
  }

  const list = await api('GET', '/api/admin/home/latest-columns', { headers: adminH });
  const cols = list.json?.data ?? [];
  add(
    'D1-D2-admin-columns',
    list.json?.code === 200 && cols.length === 3,
    `count=${cols.length} keys=${cols.map((c) => c.columnKey).join(',')}`,
  );

  const material = cols.find((c) => c.columnKey === 'material');
  if (material?.id) {
    const newTitle = material.title === '最新资料' ? '最新试卷资料' : '最新资料';
    const upd = await api('PUT', `/api/admin/home/latest-columns/${material.id}`, {
      headers: adminH,
      body: {
        title: newTitle,
        morePath: material.morePath,
        dataSource: material.dataSource,
        rule: material.rule,
        sort: material.sort,
        status: material.status,
      },
    });
    add('D3-update-title', upd.json?.code === 200, `title=${newTitle}`);
    await api('PUT', `/api/admin/home/latest-columns/${material.id}`, {
      headers: adminH,
      body: {
        title: '最新资料',
        morePath: material.morePath,
        dataSource: material.dataSource,
        rule: material.rule,
        sort: material.sort,
        status: material.status,
      },
    });

    const preview = await api(
      'GET',
      `/api/admin/home/latest-columns/${material.id}/preview?stageKey=primary`,
      { headers: adminH },
    );
    add(
      'D4-material-preview',
      preview.json?.code === 200 && Array.isArray(preview.json?.data),
      `items=${preview.json?.data?.length ?? 0}`,
    );
  } else {
    add('D3-update-title', false, 'material column not found');
    add('D4-material-preview', false, 'material column not found');
  }

  const cList = await api('GET', '/api/home/latest-columns?stageKey=primary');
  const cCols = cList.json?.data ?? [];
  add(
    'D5-c-latest-columns',
    cList.json?.code === 200 && cCols.length >= 2,
    `count=${cCols.length}`,
  );

  const topic = cols.find((c) => c.columnKey === 'topic');
  if (topic?.id) {
    await api('PUT', `/api/admin/home/latest-columns/${topic.id}/status?status=0`, {
      headers: adminH,
    });
    const cAfter = await api('GET', '/api/home/latest-columns?stageKey=primary');
    const afterCols = cAfter.json?.data ?? [];
    const disabledOk = !afterCols.some((c) => c.key === 'topic');
    add('D6-disable-column', disabledOk, `cCount=${afterCols.length}`);
    await api('PUT', `/api/admin/home/latest-columns/${topic.id}/status?status=1`, {
      headers: adminH,
    });
  } else {
    add('D6-disable-column', false, 'topic column not found');
  }

  summarize();
  const failed = results.some((r) => !r.pass);
  process.exit(failed ? 1 : 0);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
