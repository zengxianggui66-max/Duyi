/**
 * Phase 8-B acceptance: sys_config GET/PUT + audit
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

async function main() {
  console.log('=== Phase 8-B Acceptance Test ===\n');
  const adminH = await login('admin');

  // B1: GET upload config
  const upload = await api('GET', '/api/admin/system/config?group=upload', { headers: adminH });
  const vals = upload.json?.data?.values ?? {};
  add(
    'B1-get-upload',
    upload.json?.code === 200 && vals.maxSizeMb != null && Array.isArray(vals.allowedFormats),
    `maxSizeMb=${vals.maxSizeMb} formats=${vals.allowedFormats?.length ?? 0}`,
  );

  // B2: PUT maxSize + audit log
  const beforeLog = await api(
    'GET',
    '/api/admin/system/logs?module=system&action=update_config&size=1',
    { headers: adminH },
  );
  const logTopBefore = beforeLog.json?.data?.records?.[0]?.id ?? 0;
  const putSize = await api('PUT', '/api/admin/system/config?group=upload', {
    headers: adminH,
    body: { maxSizeMb: 512 },
  });
  const afterLog = await api(
    'GET',
    '/api/admin/system/logs?module=system&action=update_config&size=1',
    { headers: adminH },
  );
  const logTopAfter = afterLog.json?.data?.records?.[0]?.id ?? 0;
  add(
    'B2-put-audit',
    putSize.json?.code === 200 && logTopAfter > logTopBefore,
    `putCode=${putSize.json?.code} logId before=${logTopBefore} after=${logTopAfter}`,
  );

  // B3: GET oauth secret masked
  const oauth = await api('GET', '/api/admin/system/config?group=oauth', { headers: adminH });
  const oauthVals = oauth.json?.data?.values ?? {};
  const secretPlain = JSON.stringify(oauthVals).includes('your-weixin-app-secret')
    || JSON.stringify(oauthVals).includes('admin123');
  add(
    'B3-oauth-secret-masked',
    oauth.json?.code === 200
      && oauthVals.weixinAppSecret === '******'
      && !secretPlain,
    `weixinAppSecret=${oauthVals.weixinAppSecret}`,
  );

  // B4: PUT oauth keep secret
  await api('PUT', '/api/admin/system/config?group=oauth', {
    headers: adminH,
    body: { weixinAppSecret: 'phase8b-keep-secret' },
  });
  const newAppId = `phase8b-app-${Date.now()}`;
  await api('PUT', '/api/admin/system/config?group=oauth', {
    headers: adminH,
    body: { weixinAppId: newAppId, weixinAppSecret: '******' },
  });
  const oauthAfter = await api('GET', '/api/admin/system/config?group=oauth', { headers: adminH });
  const afterVals = oauthAfter.json?.data?.values ?? {};
  const weixinField = (oauthAfter.json?.data?.fields || []).find((f) => f.key === 'weixinAppSecret');
  add(
    'B4-oauth-keep-secret',
    oauthAfter.json?.code === 200
      && afterVals.weixinAppId === newAppId
      && afterVals.weixinAppSecret === '******'
      && weixinField?.configured === true,
    `appId=${afterVals.weixinAppId} configured=${weixinField?.configured}`,
  );

  // B5: no config_edit → 403
  const auditorH = await login('auditor');
  const forbidden = await api('PUT', '/api/admin/system/config?group=upload', {
    headers: auditorH,
    body: { maxSizeMb: 100 },
  });
  add('B5-no-config-edit', forbidden.json?.code === 403, `code=${forbidden.json?.code}`);

  // B6: invalid maxSize → 400
  const invalid = await api('PUT', '/api/admin/system/config?group=upload', {
    headers: adminH,
    body: { maxSizeMb: 0 },
  });
  add('B6-invalid-max-size', invalid.json?.code === 400, `code=${invalid.json?.code}`);

  const passed = results.filter((r) => r.pass).length;
  console.log(`\n=== Summary: ${passed}/${results.length} passed ===`);
  process.exit(results.some((r) => !r.pass) ? 1 : 0);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
