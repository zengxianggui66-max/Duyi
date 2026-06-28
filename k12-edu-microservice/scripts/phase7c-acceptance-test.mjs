/**
 * Phase 7-C home panel admin acceptance
 * Usage: node scripts/phase7c-acceptance-test.mjs
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
  console.log('=== Phase 7-C Acceptance Test ===');
  let adminH;
  try {
    adminH = await login('content_admin');
  } catch {
    adminH = await login('admin');
  }

  const tabs = await api('GET', '/api/admin/home/panels/tabs?panelCode=sync_prep', { headers: adminH });
  const tabList = tabs.json?.data ?? [];
  const coursewareTab = tabList.find((t) => t.tabKey === 'courseware' && !t.filterKey);
  add(
    'C1-admin-tabs',
    tabs.json?.code === 200 && tabList.length >= 5,
    `sync_prep tabs=${tabList.length}`,
  );

  if (coursewareTab?.id) {
    const newLabel = coursewareTab.tabLabel === '课件' ? '课件资源' : '课件';
    const upd = await api('PUT', `/api/admin/home/panels/tabs/${coursewareTab.id}`, {
      headers: adminH,
      body: {
        tabLabel: newLabel,
        queryMode: coursewareTab.queryMode || 'resource',
        moduleNames: coursewareTab.moduleNames,
        resourceTypeNames: coursewareTab.resourceTypeNames,
        sort: coursewareTab.sort,
      },
    });
    add('C2-update-tab-label', upd.json?.code === 200, `label=${newLabel}`);
    await api('PUT', `/api/admin/home/panels/tabs/${coursewareTab.id}`, {
      headers: adminH,
      body: {
        tabLabel: '课件',
        queryMode: coursewareTab.queryMode || 'resource',
        moduleNames: coursewareTab.moduleNames,
        resourceTypeNames: coursewareTab.resourceTypeNames,
        sort: coursewareTab.sort,
      },
    });
  } else {
    add('C2-update-tab-label', false, 'courseware tab not found');
  }

  const featured = await api('GET', '/api/admin/home/panels/featured?panelCode=sync_prep', {
    headers: adminH,
  });
  const featList = featured.json?.data ?? [];
  add(
    'C3-featured-list',
    featured.json?.code === 200 && featList.length >= 1,
    `count=${featList.length} title=${featList[0]?.resourceTitle ?? 'N/A'}`,
  );

  const invalid = await api('POST', '/api/admin/home/panels/featured', {
    headers: adminH,
    body: {
      panelCode: 'sync_prep',
      tabKey: 'courseware',
      resourceId: 999999999,
      resourceSource: 'oss_primary_chinese',
      sort: 1,
      status: 1,
    },
  });
  add(
    'C4-invalid-resource-400',
    invalid.http === 400 || invalid.json?.code === 400,
    `http=${invalid.http} code=${invalid.json?.code ?? 'N/A'}`,
  );

  const preview = await api(
    'GET',
    '/api/admin/home/panels/preview?panelCode=sync_prep&tabKey=courseware&stageKey=primary&subjectName=语文&limit=5',
    { headers: adminH },
  );
  add(
    'C5-preview',
    preview.json?.code === 200 && Array.isArray(preview.json?.data?.items),
    `items=${preview.json?.data?.items?.length ?? 0}`,
  );

  const cSync = await api(
    'GET',
    '/api/home/panels/sync-prep?stageKey=primary&subjectName=语文&tabKey=courseware&limit=5',
  );
  add(
    'C6-c-sync-prep',
    cSync.json?.code === 200 && (cSync.json?.data?.items?.length ?? 0) >= 1,
    `items=${cSync.json?.data?.items?.length ?? 0}`,
  );

  summarize();
  const failed = results.some((r) => !r.pass);
  process.exit(failed ? 1 : 0);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
