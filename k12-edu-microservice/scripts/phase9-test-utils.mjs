/**
 * Phase 9 验收脚本公共工具
 */
export const BASE = process.env.PHASE9_GATEWAY || process.env.PHASE8_GATEWAY || 'http://localhost:9001';

export function createReporter() {
  const results = [];
  return {
    results,
    add(id, pass, detail) {
      results.push({ id, pass: !!pass, detail: String(detail) });
      console.log(`[${pass ? 'PASS' : 'FAIL'}] ${id} - ${detail}`);
    },
    summary(label) {
      const passed = results.filter((r) => r.pass).length;
      const total = results.length;
      console.log(`\n=== ${label}: ${passed}/${total} ===`);
      return passed === total ? 0 : 1;
    },
  };
}

export async function api(method, path, { headers = {}, body, params } = {}) {
  let url = `${BASE}${path}`;
  if (params && typeof params === 'object') {
    const qs = new URLSearchParams();
    for (const [key, value] of Object.entries(params)) {
      if (value !== undefined && value !== null && value !== '') {
        qs.append(key, String(value));
      }
    }
    const q = qs.toString();
    if (q) {
      url += `${path.includes('?') ? '&' : '?'}${q}`;
    }
  }
  const opts = { method, headers: { ...headers } };
  if (body !== undefined) {
    opts.headers['Content-Type'] = 'application/json; charset=utf-8';
    opts.body = JSON.stringify(body);
  }
  const res = await fetch(url, opts);
  const text = await res.text();
  let json = null;
  try {
    json = JSON.parse(text);
  } catch {
    /* ignore */
  }
  return {
    ok: res.ok,
    http: res.status,
    json,
    raw: text,
    location: res.headers.get('location') || '',
  };
}

export async function loginSession(username, password = 'admin123') {
  const r = await api('POST', '/api/auth/login', { body: { username, password } });
  if (r.json?.code === 200 && r.json.data?.token) {
    return {
      headers: { Authorization: `Bearer ${r.json.data.token}` },
      userId: r.json.data?.user?.id ?? r.json.data?.id,
    };
  }
  throw new Error(`Login failed for ${username}: ${r.raw}`);
}

export async function login(username, password = 'admin123') {
  return (await loginSession(username, password)).headers;
}

/** 按顺序尝试多个账号，用于 optional 种子数据 */
export async function loginFirst(candidates) {
  let lastErr;
  for (const { username, password = 'admin123' } of candidates) {
    try {
      const session = await loginSession(username, password);
      return { headers: session.headers, userId: session.userId, username };
    } catch (e) {
      lastErr = e;
    }
  }
  throw lastErr;
}

/** HTTP 403 或业务 code 403 */
export function isForbidden(res) {
  return res.http === 403 || res.json?.code === 403;
}

/** 旧 /api/search/admin/** 应不可再提供有效统计（网关 404 为首选） */
export function legacySearchAdminGone(res) {
  if (res.http === 404) {
    return true;
  }
  // 仍返回 200 但 500/404 业务码或空 data 视为未下线
  if (res.http === 200 && res.json?.code === 200 && res.json?.data?.totalQueries != null) {
    return false;
  }
  // 401/403/500 等非成功统计均视为 legacy 已失效
  return res.http !== 200 || res.json?.code !== 200;
}

export function permCodes(jsonResponse) {
  return jsonResponse?.data || [];
}

function findMenuByTitle(nodes, title) {
  for (const node of nodes || []) {
    if (node.title === title) {
      return node;
    }
    const nested = findMenuByTitle(node.children, title);
    if (nested) {
      return nested;
    }
  }
  return null;
}

export function hasMenuTree(jsonResponse, parentTitle, childTitle) {
  const roots = jsonResponse?.data || [];
  const parent = findMenuByTitle(roots, parentTitle);
  if (!parent?.children?.length) {
    return false;
  }
  return parent.children.some((c) => c.title === childTitle);
}

export function menuTitles(jsonResponse) {
  const walk = (nodes, out = []) => {
    for (const n of nodes || []) {
      out.push(n.title);
      if (n.children?.length) {
        walk(n.children, out);
      }
    }
    return out;
  };
  return walk(jsonResponse?.data || []);
}

export function hasTrend(arr, minLen = 7) {
  return Array.isArray(arr) && arr.length >= minLen && arr.every((p) => p.date && typeof p.count === 'number');
}
