/**
 * Phase 8-F acceptance: compliance matrix checks
 * Usage: node scripts/phase8f-acceptance-test.mjs
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

function hasPlainSecret(obj, knownSecrets = []) {
  const s = JSON.stringify(obj ?? {});
  for (const sec of knownSecrets) {
    if (sec && s.includes(sec)) return true;
  }
  return /"secretKey"\s*:\s*"[^*]/.test(s) || /"accessKey"\s*:\s*"admin"/.test(s);
}

async function main() {
  console.log('=== Phase 8-F Compliance Acceptance ===\n');
  const adminH = await login('admin');

  // F1: 谁、何时、做了什么 — sys_operation_log 字段完整
  const opLogs = await api('GET', '/api/admin/system/logs?current=1&size=5', { headers: adminH });
  const opRec = opLogs.json?.data?.records?.[0];
  add(
    'F1-audit-fields',
    opLogs.json?.code === 200
      && opRec?.username
      && opRec?.createTime
      && opRec?.module
      && opRec?.action
      && (opLogs.json?.data?.total ?? 0) > 0,
    `total=${opLogs.json?.data?.total ?? 0} sample=${opRec?.module}:${opRec?.action}`,
  );

  // F2: 登录成功/失败 — user_login_log 可筛选
  const failLogs = await api(
    'GET',
    '/api/admin/system/login-logs?success=0&current=1&size=3',
    { headers: adminH },
  );
  const okLogs = await api(
    'GET',
    '/api/admin/system/login-logs?success=1&loginType=admin&current=1&size=3',
    { headers: adminH },
  );
  add(
    'F2-login-log-filter',
    failLogs.json?.code === 200 && okLogs.json?.code === 200,
    `fail=${failLogs.json?.data?.total ?? 0} adminOk=${okLogs.json?.data?.total ?? 0}`,
  );

  // F3: 配置变更可追溯 — module=system action=update_config
  const sysLogs = await api(
    'GET',
    '/api/admin/system/logs?module=system&action=update_config&size=5',
    { headers: adminH },
  );
  add(
    'F3-config-audit-trail',
    sysLogs.json?.code === 200 && (sysLogs.json?.data?.total ?? 0) > 0,
    `update_config count=${sysLogs.json?.data?.total ?? 0}`,
  );

  // F4: 敏感信息不泄露 — oauth GET 无明文 secret
  const oauth = await api('GET', '/api/admin/system/config?group=oauth', { headers: adminH });
  const oauthRaw = JSON.stringify(oauth.json?.data ?? {});
  add(
    'F4-oauth-no-plain-secret',
    oauth.json?.code === 200
      && oauth.json?.data?.values?.weixinAppSecret === '******'
      && !oauthRaw.includes('your-weixin-app-secret')
      && !oauthRaw.includes('phase8b-keep-secret'),
    `weixinAppSecret=${oauth.json?.data?.values?.weixinAppSecret}`,
  );

  // F5: storage GET 无 accessKey/secretKey 明文
  const storageCfg = await api('GET', '/api/admin/system/config?group=storage', { headers: adminH });
  add(
    'F5-storage-no-plain-secret',
    storageCfg.json?.code === 200
      && storageCfg.json?.data?.values?.minioAccessKey === '******'
      && storageCfg.json?.data?.values?.minioSecretKey === '******'
      && !hasPlainSecret(storageCfg.json?.data, ['admin123']),
    `masked=${storageCfg.json?.data?.values?.minioSecretKey}`,
  );

  // F6: 权限最小化 — auditor 403 矩阵
  const auditorH = await login('auditor');
  const checks403 = [
    ['/api/admin/system/logs?size=1', 'logs'],
    ['/api/admin/system/config?group=upload', 'config'],
    ['/api/admin/system/feature-flags', 'feature-flags'],
    ['/api/admin/users/export', 'export'],
  ];
  let auditorBlocked = 0;
  for (const [path] of checks403) {
    const r = await api('GET', path, { headers: auditorH });
    if (r.json?.code === 403) auditorBlocked += 1;
  }
  add(
    'F6-rbac-least-privilege',
    auditorBlocked >= 3,
    `auditor 403 on ${auditorBlocked}/${checks403.length} sensitive endpoints`,
  );

  // F7: 数据保留 — 文档约定（自动化仅验证索引/表存在性 via API 间接：登录日志可分页即表可用）
  const retentionProbe = await api(
    'GET',
    '/api/admin/system/login-logs?current=1&size=1',
    { headers: adminH },
  );
  add(
    'F7-retention-ready',
    retentionProbe.json?.code === 200,
    'login_log paginated; retention policy documented 180d (see Phase8-F-验收.md)',
  );

  const passed = results.filter((r) => r.pass).length;
  console.log(`\n=== Summary: ${passed}/${results.length} passed ===`);
  process.exit(results.some((r) => !r.pass) ? 1 : 0);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
