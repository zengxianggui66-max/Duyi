/**
 * Phase 7-B hot word × search suggest acceptance
 * Usage: node scripts/phase7b-acceptance-test.mjs
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
  console.log('=== Phase 7-B Acceptance Test ===');
  let adminH;
  try {
    adminH = await login('content_admin');
  } catch {
    adminH = await login('admin');
  }

  const suggestEmpty = await api('GET', '/api/search/suggest?q=&limit=12');
  const suggestList = suggestEmpty.json?.data ?? [];
  const hotItems = suggestList.filter((i) => i.kind === 'hot');
  const opsHot = hotItems.find((i) => i.text === '一年级语文');
  add(
    'B1-suggest-ops-hot',
    suggestEmpty.json?.code === 200 && !!opsHot?.navTarget,
    `hotCount=${hotItems.length} navType=${opsHot?.navTarget?.type ?? 'N/A'}`,
  );

  const homeWords = await api('GET', '/api/home/hot-words');
  const homeLabels = (homeWords.json?.data ?? []).map((w) => w.label);
  const suggestHotLabels = hotItems.map((i) => i.text);
  const prefixMatch = homeLabels.slice(0, 3).every((l, idx) => suggestHotLabels[idx] === l);
  add(
    'B2-order-aligned',
    homeWords.json?.code === 200 && prefixMatch,
    `home=${homeLabels.slice(0, 3).join(',')} suggest=${suggestHotLabels.slice(0, 3).join(',')}`,
  );

  const statsHot = await api('GET', '/api/search/hot-keywords?limit=5');
  add(
    'B3-stats-independent',
    statsHot.json?.code === 200,
    `statsCount=${statsHot.json?.data?.length ?? 0}`,
  );

  const browseWord = hotItems.find((i) => i.text === '期中试卷');
  add(
    'B4-browse-nav-target',
    browseWord?.navTarget?.type === 'browse',
    `module=${browseWord?.navTarget?.query?.module ?? 'N/A'}`,
  );

  const words = homeWords.json?.data ?? [];
  if (words.length >= 2) {
    const reorderPayload = {
      items: words.map((w, idx) => ({ id: w.id, sort: (words.length - idx) * 10 })),
    };
    const reorder = await api('PUT', '/api/admin/home/hot-words/reorder', {
      headers: adminH,
      body: reorderPayload,
    });
    add('B5-admin-reorder', reorder.json?.code === 200, `items=${reorderPayload.items.length}`);
  } else {
    add('B5-admin-reorder', false, 'not enough hot words');
  }

  summarize();
  const failed = results.some((r) => !r.pass);
  process.exit(failed ? 1 : 0);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
