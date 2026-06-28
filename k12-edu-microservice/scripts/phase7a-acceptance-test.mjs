/**
 * Phase 7-A home ops API acceptance
 * Usage: node scripts/phase7a-acceptance-test.mjs
 * Prerequisite: sql/69_phase7a_home_ops.sql executed; gateway 9001 up
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
  console.log('=== Phase 7-A Acceptance Test ===');
  let adminH;
  try {
    adminH = await login('content_admin');
  } catch {
    adminH = await login('admin');
  }

  const banners = await api('GET', '/api/home/banners?slotCode=home_hero&stage=primary');
  const bannerList = banners.json?.data ?? [];
  add(
    'A1-banners-seed',
    banners.json?.code === 200 && bannerList.length >= 3,
    `count=${bannerList.length} first=${bannerList[0]?.title ?? 'N/A'}`,
  );

  const entries = await api('GET', '/api/home/quick-entries?stage=primary');
  const entryList = entries.json?.data ?? [];
  add(
    'A1-quick-entries-seed',
    entries.json?.code === 200 && entryList.length >= 5,
    `count=${entryList.length}`,
  );

  const hotWords = await api('GET', '/api/home/hot-words?stage=primary');
  const wordList = hotWords.json?.data ?? [];
  add(
    'A1-hot-words-seed',
    hotWords.json?.code === 200 && wordList.length >= 4,
    `count=${wordList.length} labels=${wordList.map((w) => w.label).join(',')}`,
  );

  const hero = await api('GET', '/api/home/hero?stage=primary');
  const h = hero.json?.data;
  add(
    'A1-hero-aggregate',
    hero.json?.code === 200 && h?.banners?.length >= 1 && h?.quickEntries?.length >= 1,
    `banners=${h?.banners?.length ?? 0} entries=${h?.quickEntries?.length ?? 0} words=${h?.hotWords?.length ?? 0}`,
  );

  const browseWord = wordList.find((w) => w.label === '一年级语文');
  add(
    'A4-browse-hot-word',
    browseWord?.actionType === 'browse' && browseWord?.navTarget?.type === 'browse',
    `stageKey=${browseWord?.navTarget?.stageKey ?? 'N/A'}`,
  );

  const searchWord = wordList.find((w) => w.label === '教案模板');
  add(
    'A5-search-hot-word-mysql',
    searchWord?.actionType === 'search' && searchWord?.navTarget?.searchEngine === 'mysql',
    `keyword=${searchWord?.navTarget?.keyword ?? 'N/A'}`,
  );

  const invalidNav = await api('POST', '/api/admin/home/banners', {
    headers: adminH,
    body: {
      title: '无效测试',
      navTarget: { type: 'browse' },
    },
  });
  add(
    'A6-invalid-nav-target-400',
    invalidNav.http === 400 || invalidNav.json?.code === 400,
    `http=${invalidNav.http} code=${invalidNav.json?.code ?? 'N/A'}`,
  );

  const adminList = await api('GET', '/api/admin/home/banners?includeDisabled=true', {
    headers: adminH,
  });
  add(
    'A2-admin-list-banners',
    adminList.json?.code === 200 && (adminList.json?.data?.length ?? 0) >= 1,
    `admin banners=${adminList.json?.data?.length ?? 0}`,
  );

  const firstEntry = entryList[0];
  if (firstEntry?.entryKey) {
    const disable = await api(
      'PUT',
      `/api/admin/home/quick-entries/${firstEntry.id}/status?status=0`,
      { headers: adminH },
    );
    const afterDisable = await api('GET', '/api/home/quick-entries?stage=primary');
    const stillVisible = (afterDisable.json?.data ?? []).some((e) => e.id === firstEntry.id);
    add(
      'A3-disable-quick-entry',
      disable.json?.code === 200 && !stillVisible,
      `entry=${firstEntry.entryKey} visibleAfterDisable=${stillVisible}`,
    );
    await api('PUT', `/api/admin/home/quick-entries/${firstEntry.id}/status?status=1`, {
      headers: adminH,
    });
  } else {
    add('A3-disable-quick-entry', false, 'no quick entry to test');
  }

  summarize();
  const failed = results.some((r) => !r.pass);
  process.exit(failed ? 1 : 0);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
