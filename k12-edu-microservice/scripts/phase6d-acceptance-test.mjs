/**
 * Phase 6-D 运营增强 API 验收
 * 用法: node scripts/phase6d-acceptance-test.mjs
 */
const BASE = process.env.PHASE6_GATEWAY || 'http://localhost:9001';

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
      /* csv or empty */
    }
    return { ok: res.ok, http: res.status, json, raw: text, headers: res.headers };
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

async function main() {
  console.log('=== Phase 6-D Acceptance Test ===');
  const adminH = await login('admin');
  const auditorH = await login('auditor');

  const users = await api('GET', '/api/admin/users?keyword=teacher_demo&size=5&staffOnly=false', {
    headers: adminH,
  });
  const teacher = (users.json?.data?.records || []).find((u) => u.username === 'teacher_demo');
  add('SETUP', !!teacher?.id, `teacher_demo id=${teacher?.id ?? 'N/A'}`);
  if (!teacher?.id) {
    summarize();
    process.exit(1);
  }

  const remark = await api('POST', `/api/admin/users/${teacher.id}/remarks`, {
    headers: adminH,
    body: { content: `Phase6D验收-${Date.now()}` },
  });
  add('1-remark', remark.json?.code === 200, `add remark code=${remark.json?.code}`);

  const remarkList = await api('GET', `/api/admin/users/${teacher.id}/remarks?current=1&size=5`, {
    headers: adminH,
  });
  add(
    '1-remark-list',
    (remarkList.json?.data?.records?.length ?? 0) > 0,
    `remarks=${remarkList.json?.data?.records?.length ?? 0}`,
  );

  const exportR = await api('GET', '/api/admin/users/export?staffOnly=false', { headers: adminH });
  add(
    '2-export',
    exportR.raw?.includes('username') && exportR.raw?.includes('teacher_demo'),
    `csv bytes=${exportR.raw?.length ?? 0}`,
  );

  const opLogs = await api('GET', `/api/admin/users/${teacher.id}/operation-logs?current=1&size=10`, {
    headers: adminH,
  });
  add('5-oplogs', opLogs.json?.code === 200, `oplogs=${opLogs.json?.data?.records?.length ?? 0}`);

  const auditorList = await api('GET', '/api/admin/users?staffOnly=false&size=50', { headers: auditorH });
  const auditorPerms = await api('GET', '/api/admin/permissions', { headers: auditorH });
  const hasUserView = (auditorPerms.json?.data || []).includes('admin:user:view');
  add('4-auditor-perm', hasUserView, `auditor admin:user:view=${hasUserView}`);
  add(
    '4-auditor-list',
    auditorList.json?.code === 200 || auditorList.json?.code === 403,
    `auditor list code=${auditorList.json?.code} count=${auditorList.json?.data?.records?.length ?? 0}`,
  );

  if (hasUserView) {
    const forbidden = await api('GET', `/api/admin/users/1`, { headers: auditorH });
    add(
      '4-auditor-scope',
      forbidden.json?.code === 403 || forbidden.json?.code === 404,
      `auditor detail id=1 code=${forbidden.json?.code}`,
    );
  }

  summarize();
  process.exit(results.every((r) => r.pass) ? 0 : 1);
}

function summarize() {
  const passed = results.filter((r) => r.pass).length;
  console.log(`\n=== Summary ===\n${passed}/${results.length} passed`);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
