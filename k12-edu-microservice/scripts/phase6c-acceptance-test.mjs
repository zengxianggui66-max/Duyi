/**
 * Phase 6-C 行为流水 API 自动化验收
 * 用法: node scripts/phase6c-acceptance-test.mjs
 */
const BASE = process.env.PHASE6_GATEWAY || 'http://localhost:9001';

const results = [];
const state = { teacherId: null, resourceId: null, downloadActionId: null };

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

async function login(username, password = 'admin123') {
  const r = await api('POST', '/api/auth/login', { body: { username, password } });
  if (!r.json || r.json.code !== 200 || !r.json.data?.token) {
    throw new Error(`Login failed for ${username}: ${r.raw}`);
  }
  return { Authorization: `Bearer ${r.json.data.token}` };
}

async function ensureTeacherLogin(adminH) {
  for (const pwd of ['123456', 'admin123', 'teacher123']) {
    const r = await api('POST', '/api/auth/login', { body: { username: 'teacher_demo', password: pwd } });
    if (r.json?.code === 200 && r.json.data?.token) {
      return { Authorization: `Bearer ${r.json.data.token}` };
    }
  }
  const reset = await api('POST', '/api/admin/users/11/reset-password', { headers: adminH });
  const newPwd = reset.json?.data;
  if (!newPwd) throw new Error('无法重置 teacher_demo 密码');
  console.log(`[INFO] teacher_demo 密码已重置为 ${newPwd}`);
  return login('teacher_demo', newPwd);
}

function sleep(ms) {
  return new Promise((r) => setTimeout(r, ms));
}

async function main() {
  console.log('=== Phase 6-C Acceptance Test ===');
  console.log('Gateway:', BASE);

  const adminH = await login('admin');
  const teacherH = await ensureTeacherLogin(adminH);
  const auditorH = await login('auditor');

  // 找 teacher_demo 用户 ID
  const users = await api('GET', '/api/admin/users?keyword=teacher_demo&size=5', { headers: adminH });
  const teacher = (users.json?.data?.records || []).find((u) => u.username === 'teacher_demo');
  state.teacherId = teacher?.id;
  add('SETUP', !!state.teacherId, `teacher_demo id=${state.teacherId ?? 'NOT FOUND'}`);
  if (!state.teacherId) {
    printSummary();
    process.exit(1);
  }

  // 权限：admin 有 view_behavior，auditor 无
  const adminPerms = await api('GET', '/api/admin/permissions', { headers: adminH });
  const auditorPerms = await api('GET', '/api/admin/permissions', { headers: auditorH });
  const adminHas = (adminPerms.json?.data || []).includes('admin:user:view_behavior');
  const auditorHas = (auditorPerms.json?.data || []).includes('admin:user:view_behavior');
  add('1-perm-admin', adminHas, `admin view_behavior=${adminHas}`);
  add('1-perm-auditor', !auditorHas, `auditor view_behavior=${auditorHas} (expect false)`);

  // 403：auditor 调 actions
  const forbidden = await api(
    'GET',
    `/api/admin/users/${state.teacherId}/actions?actionType=download&current=1&size=10`,
    { headers: auditorH },
  );
  add('7-forbidden', forbidden.json?.code === 403, `auditor actions code=${forbidden.json?.code}`);

  // 找可用资源 ID
  const searchRes = await api('GET', '/api/search/all?q=语文&page=1&size=1', { headers: teacherH });
  const firstHit = searchRes.json?.data?.records?.[0] || searchRes.json?.data?.list?.[0];
  state.resourceId = firstHit?.id || firstHit?.resourceId || 1;
  add('SETUP-resource', !!state.resourceId, `resourceId=${state.resourceId}`);

  const ts = Date.now();
  const testTitle = `Phase6C验收-${ts}`;

  // 产生行为：浏览、下载、搜索（teacher 登录态）
  const viewR = await api('POST', '/api/resource/view/record', {
    headers: teacherH,
    body: { resourceId: state.resourceId, resourceType: 'primary_chinese', title: testTitle },
  });
  add('4-view-api', viewR.json?.code === 200, `view record code=${viewR.json?.code}`);

  const dlR = await api('POST', '/api/resource/download/record', {
    headers: teacherH,
    body: {
      resourceId: state.resourceId,
      resourceType: 'primary_chinese',
      resourceTitle: testTitle,
    },
  });
  add('3-download-api', dlR.json?.code === 200, `download record code=${dlR.json?.code}`);

  const searchKeyword = `phase6c-${ts}`;
  const searchR = await api('GET', `/api/search/all?q=${encodeURIComponent(searchKeyword)}&page=1&size=5`, {
    headers: teacherH,
  });
  add('5-search-api', searchR.json?.code === 200, `search code=${searchR.json?.code}`);

  // 再次登录产生登录流水
  await ensureTeacherLogin(adminH);
  add('6-login', true, 'teacher_demo re-login ok');

  // 30s 内可见：稍等后查管理端
  await sleep(1500);

  const t0 = Date.now();
  let downloadFound = false;
  let latestDownload = null;
  for (let i = 0; i < 6; i++) {
    const dlList = await api(
      'GET',
      `/api/admin/users/${state.teacherId}/actions?actionType=download&current=1&size=10`,
      { headers: adminH },
    );
    const records = dlList.json?.data?.records || [];
    latestDownload = records.find((r) => r.title?.includes('Phase6C验收') || r.title === testTitle);
    if (latestDownload) {
      downloadFound = true;
      state.downloadActionId = latestDownload.id;
      break;
    }
    await sleep(2000);
  }
  const elapsed = Date.now() - t0;
  add(
    '3-download-visible',
    downloadFound && elapsed < 30000,
    downloadFound
      ? `found id=${latestDownload?.id} title="${latestDownload?.title}" in ${elapsed}ms`
      : `not found within ${elapsed}ms`,
  );

  // stats 含行为计数
  const stats = await api('GET', `/api/admin/users/${state.teacherId}/stats`, { headers: adminH });
  const s = stats.json?.data || {};
  add(
    '2-stats',
    stats.json?.code === 200 &&
      typeof s.uploadCount === 'number' &&
      typeof s.downloadCount === 'number' &&
      typeof s.viewCount === 'number' &&
      typeof s.searchCount === 'number' &&
      typeof s.loginCount === 'number',
    `upload=${s.uploadCount} collect=${s.collectionCount} view=${s.viewCount} dl=${s.downloadCount} search=${s.searchCount} login=${s.loginCount}`,
  );

  // 浏览 Tab
  const viewList = await api(
    'GET',
    `/api/admin/users/${state.teacherId}/actions?actionType=view&current=1&size=10`,
    { headers: adminH },
  );
  const viewHit = (viewList.json?.data?.records || []).some((r) => r.title?.includes('Phase6C验收'));
  add('4-view-visible', viewHit, `view records=${viewList.json?.data?.records?.length ?? 0}`);

  // 搜索 Tab
  const searchList = await api(
    'GET',
    `/api/admin/users/${state.teacherId}/actions?actionType=search&current=1&size=10`,
    { headers: adminH },
  );
  const searchHit = (searchList.json?.data?.records || []).some((r) => r.keyword?.includes('phase6c-'));
  add('5-search-visible', searchHit, `search records=${searchList.json?.data?.records?.length ?? 0}`);

  // 登录 Tab
  const loginList = await api(
    'GET',
    `/api/admin/users/${state.teacherId}/login-logs?current=1&size=5`,
    { headers: adminH },
  );
  const loginOk =
    loginList.json?.code === 200 && (loginList.json?.data?.records?.length ?? 0) > 0;
  add('6-login-visible', loginOk, `login logs=${loginList.json?.data?.records?.length ?? 0}`);

  printSummary();
  process.exit(results.every((r) => r.pass) ? 0 : 1);
}

function printSummary() {
  const passed = results.filter((r) => r.pass).length;
  console.log('\n=== Summary ===');
  console.log(`${passed}/${results.length} passed`);
  if (passed === results.length) {
    console.log('Overall: Go');
  } else {
    console.log('Overall: No-Go');
    results.filter((r) => !r.pass).forEach((r) => console.log(`  FAIL ${r.id}: ${r.detail}`));
  }
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
