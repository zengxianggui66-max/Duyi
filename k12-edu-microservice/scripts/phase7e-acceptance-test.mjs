/**
 * Phase 7-E feature channel admin acceptance
 * Usage: node scripts/phase7e-acceptance-test.mjs
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
    try { json = JSON.parse(text); } catch { /* ignore */ }
    return { ok: res.ok, http: res.status, json, raw: text };
  } catch (e) {
    return { ok: false, http: 0, json: null, raw: null, error: e.message };
  }
}

async function login(username) {
  const r = await api('POST', '/api/auth/login', { body: { username, password: 'admin123' } });
  if (!r.json || r.json.code !== 200 || !r.json.data?.token) {
    throw new Error(`Login failed: ${r.raw}`);
  }
  return { Authorization: `Bearer ${r.json.data.token}` };
}

function summarize() {
  const passed = results.filter((r) => r.pass).length;
  console.log(`\n=== Summary: ${passed}/${results.length} passed ===`);
}

async function main() {
  console.log('=== Phase 7-E Acceptance Test ===');
  let adminH;
  try { adminH = await login('content_admin'); } catch { adminH = await login('admin'); }

  const funcList = await api('GET', '/api/admin/home/func-channels', { headers: adminH });
  const funcs = funcList.json?.data ?? [];
  add('E1-E2-func-channels', funcList.json?.code === 200 && funcs.length >= 5, `count=${funcs.length}`);

  const youxiao = funcs.find((f) => f.funcKey === 'youxiao');
  if (youxiao?.id) {
    const newName = youxiao.name === '幼小衔接' ? '幼小衔接专区' : '幼小衔接';
    const upd = await api('PUT', `/api/admin/home/func-channels/${youxiao.id}`, {
      headers: adminH,
      body: {
        name: newName,
        examType: youxiao.examType,
        defaultTopic: youxiao.defaultTopic,
        stageKey: youxiao.stageKey,
        paperTab: youxiao.paperTab,
        paperDefaultGrade: youxiao.paperDefaultGrade,
        scrollTarget: youxiao.scrollTarget || 'exam-module',
        examTabLabel: youxiao.examTabLabel,
        browseModule: youxiao.browseModule,
        browseStageKey: youxiao.browseStageKey,
        browseDefaultVolume: youxiao.browseDefaultVolume,
        sort: youxiao.sort,
        status: youxiao.status,
      },
    });
    add('E3-update-func-name', upd.json?.code === 200, `name=${newName}`);
    await api('PUT', `/api/admin/home/func-channels/${youxiao.id}`, {
      headers: adminH,
      body: {
        name: '幼小衔接',
        examType: youxiao.examType,
        defaultTopic: youxiao.defaultTopic,
        stageKey: youxiao.stageKey,
        paperTab: youxiao.paperTab,
        paperDefaultGrade: youxiao.paperDefaultGrade,
        scrollTarget: youxiao.scrollTarget || 'exam-module',
        examTabLabel: youxiao.examTabLabel,
        browseModule: youxiao.browseModule,
        browseStageKey: youxiao.browseStageKey,
        browseDefaultVolume: youxiao.browseDefaultVolume,
        sort: youxiao.sort,
        status: youxiao.status,
      },
    });
  } else {
    add('E3-update-func-name', false, 'youxiao not found');
  }

  const opsList = await api('GET', '/api/admin/operation/channels', { headers: adminH });
  const ops = opsList.json?.data ?? [];
  add('E4-admin-ops-channels', opsList.json?.code === 200 && ops.length >= 5, `count=${ops.length}`);

  const bootstrap = await api('GET', '/api/channel/banhui');
  const tabs = bootstrap.json?.data?.mainTabs ?? [];
  add('E5-c-channel-bootstrap', bootstrap.json?.code === 200 && tabs.length >= 10, `tabs=${tabs.length}`);

  const chuantongTab = await api('GET', '/api/admin/operation/channels/chuantong/tabs', { headers: adminH });
  const tabRows = chuantongTab.json?.data ?? [];
  const guoxue = tabRows.find((t) => t.tabKey === 'guoxue');
  if (guoxue?.id) {
    const newTabName = guoxue.tabName === '国学经典' ? '国学精选' : '国学经典';
    const tabUpd = await api('PUT', `/api/admin/operation/channels/tabs/${guoxue.id}`, {
      headers: adminH,
      body: {
        tabKey: guoxue.tabKey,
        tabName: newTabName,
        icon: guoxue.icon,
        searchKeyword: guoxue.searchKeyword,
        sort: guoxue.sort,
        status: guoxue.status,
      },
    });
    add('E6-update-tab-name', tabUpd.json?.code === 200, `tab=${newTabName}`);
    await api('PUT', `/api/admin/operation/channels/tabs/${guoxue.id}`, {
      headers: adminH,
      body: {
        tabKey: guoxue.tabKey,
        tabName: '国学经典',
        icon: guoxue.icon,
        searchKeyword: guoxue.searchKeyword,
        sort: guoxue.sort,
        status: guoxue.status,
      },
    });
  } else {
    add('E6-update-tab-name', false, 'guoxue tab not found');
  }

  const cFunc = await api('GET', '/api/home/func-channels');
  add('E7-c-func-channels', cFunc.json?.code === 200 && (cFunc.json?.data?.channels?.length ?? 0) >= 5,
    `channels=${cFunc.json?.data?.channels?.length ?? 0}`);

  summarize();
  process.exit(results.some((r) => !r.pass) ? 1 : 0);
}

main().catch((e) => { console.error(e); process.exit(1); });
