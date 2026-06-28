/**
 * Phase 4 验收脚本：审核与发布解耦
 * 用法：
 *   node scripts/phase4-acceptance-test.mjs
 * 可选环境变量：
 *   PHASE4_GATEWAY=http://localhost:9001
 *   PHASE4_ADMIN_USER=admin
 *   PHASE4_ADMIN_PASS=admin123
 *   PHASE4_NO_PUBLISH_USER=auditor
 *   PHASE4_NO_PUBLISH_PASS=admin123
 */

import { execSync } from 'node:child_process'

const BASE = process.env.PHASE4_GATEWAY || 'http://localhost:9001'
const ADMIN_USER = process.env.PHASE4_ADMIN_USER || 'admin'
const ADMIN_PASS = process.env.PHASE4_ADMIN_PASS || 'admin123'
const NO_PUBLISH_USER = process.env.PHASE4_NO_PUBLISH_USER || 'auditor'
const NO_PUBLISH_PASS = process.env.PHASE4_NO_PUBLISH_PASS || 'admin123'
const TEST_RESOURCE_ID = process.env.PHASE4_TEST_RESOURCE_ID || '1'
const MYSQL_BIN =
  process.env.MYSQL_BIN || 'C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe'

const checks = []

function add(id, pass, detail) {
  checks.push({ id, pass: !!pass, detail: String(detail) })
  console.log(`[${pass ? 'PASS' : 'FAIL'}] ${id} - ${detail}`)
}

async function api(method, path, { headers = {}, body } = {}) {
  const url = `${BASE}${path}`
  const opts = { method, headers: { ...headers } }
  if (body !== undefined) {
    opts.headers['Content-Type'] = 'application/json; charset=utf-8'
    opts.body = JSON.stringify(body)
  }
  try {
    const res = await fetch(url, opts)
    const raw = await res.text()
    let json = null
    try {
      json = JSON.parse(raw)
    } catch {
      // ignore
    }
    return { ok: res.ok, http: res.status, json, raw }
  } catch (error) {
    return { ok: false, http: 0, json: null, raw: '', error: String(error?.message || error) }
  }
}

async function login(username, password) {
  const r = await api('POST', '/api/auth/login', {
    body: { username, password },
  })
  const token = r.json?.data?.token
  if (r.json?.code !== 200 || !token) {
    throw new Error(`登录失败(${username}): ${r.raw}`)
  }
  return { Authorization: `Bearer ${token}` }
}

function isPublishedLabel(label) {
  return typeof label === 'string' && label.includes('已上架')
}

async function seedPendingResource(authHeaders) {
  const taxVol = await api('GET', '/api/taxonomy/volumes?stage=primary', { headers: authHeaders })
  const gradeName = taxVol.json?.data?.[0]?.name || '一年级下册'
  const taxEd = await api('GET', '/api/taxonomy/editions?stage=primary&subject=chinese', {
    headers: authHeaders,
  })
  const edition = taxEd.json?.data?.[0]?.name || '统编版'
  const title = `P4验收-${Date.now()}`

  const draftRes = await api('POST', '/api/primary-chinese/draft/save', {
    headers: authHeaders,
    body: {
      title,
      stage: '小学',
      subject: 'chinese',
      module: '同步备课',
      type: '课件',
      gradeName,
      edition,
      remark: 'phase4 seed',
    },
  })
  const draftId = draftRes.json?.data?.id
  if (!draftId) {
    return null
  }

  await api('POST', '/api/primary-chinese/draft/save', {
    headers: authHeaders,
    body: {
      id: draftId,
      title,
      stage: '小学',
      subject: 'chinese',
      module: '同步备课',
      type: '课件',
      gradeName,
      edition,
      ossUrl: 'http://localhost:8082/uploads/phase4-test.pdf',
      fileExt: 'pdf',
      fileSizeKb: 100,
      allowPreview: 1,
    },
  })

  const submitRes = await api('POST', `/api/primary-chinese/draft/${draftId}/submit`, {
    headers: authHeaders,
  })
  if (submitRes.json?.code !== 200) {
    return null
  }
  return draftId
}

async function findPendingResource(adminHeaders, allowReset = true) {
  const pending = await api('GET', '/api/admin/resources/pending?current=1&size=20', {
    headers: adminHeaders,
  })
  const legacy = pending.json?.data?.records || []
  if (legacy.length) {
    return { id: legacy[0].id, source: 'legacy-pending' }
  }
  const mainPending = await api('GET', '/api/admin/resource-main/pending?current=1&size=20', {
    headers: adminHeaders,
  })
  const main = mainPending.json?.data?.records || []
  if (main.length) {
    const row = main[0]
    return { id: row.sourceId ?? row.id, source: 'resource-main-pending' }
  }
  if (allowReset && resetResourceToPending()) {
    return findPendingResource(adminHeaders, false)
  }
  return null
}

function resetResourceToPending(id = TEST_RESOURCE_ID) {
  try {
    execSync(
      `"${MYSQL_BIN}" -uroot -pzxg123456 -e "UPDATE xinketang.oss_primary_chinese_resource SET audit_status=0, publish_status=0, status=0 WHERE id=${id};"`,
      { stdio: 'pipe' },
    )
    return true
  } catch {
    return false
  }
}

async function main() {
  console.log('=== Phase 4 Acceptance Test ===')
  console.log('Gateway:', BASE)

  const adminHeaders = await login(ADMIN_USER, ADMIN_PASS)
  const noPublishHeaders = await login(NO_PUBLISH_USER, NO_PUBLISH_PASS)

  const target = await findPendingResource(adminHeaders)
  let id = target?.id
  if (!id) {
    const teacherLogin = await api('POST', '/api/auth/login', {
      body: { username: 'teacher_demo', password: 'teacher123' },
    })
    let seedHeaders = adminHeaders
    if (teacherLogin.json?.code === 200 && teacherLogin.json?.data?.token) {
      seedHeaders = { Authorization: `Bearer ${teacherLogin.json.data.token}` }
    }
    id = await seedPendingResource(seedHeaders)
    add('P4-A1', !!id, id ? `seed pending id=${id}` : 'pending count=0, seed failed')
  } else {
    add('P4-A1', true, `pending source=${target.source} id=${id}`)
  }
  if (!id) {
    add('P4-A2', false, '待审队列为空且种子数据创建失败')
    return finish()
  }

  add('P4-A2', !!id, `target resource id=${id}`)

  const approve = await api('POST', `/api/admin/resources/${id}/audit?status=1`, {
    headers: adminHeaders,
  })
  add('P4-B1', approve.json?.code === 200, `approve code=${approve.json?.code ?? approve.http}`)

  const detailAfterApprove = await api('GET', `/api/admin/resources/${id}`, {
    headers: adminHeaders,
  })
  const approvedAudit = detailAfterApprove.json?.data?.auditStatus === 1
  const notPublished = !isPublishedLabel(detailAfterApprove.json?.data?.shelfStatusLabel)
  add(
    'P4-B2',
    detailAfterApprove.json?.code === 200 && approvedAudit && notPublished,
    `auditStatus=${detailAfterApprove.json?.data?.auditStatus} shelf=${detailAfterApprove.json?.data?.shelfStatusLabel}`,
  )

  const publicBeforePublish = await api('GET', `/api/primary-chinese/${id}`)
  add(
    'P4-B3',
    publicBeforePublish.json?.code !== 200,
    `public before publish code=${publicBeforePublish.json?.code ?? publicBeforePublish.http}`,
  )

  const noPermPublish = await api('POST', `/api/admin/resources/${id}/publish`, {
    headers: noPublishHeaders,
  })
  add(
    'P4-C1',
    noPermPublish.http === 403 || noPermPublish.json?.code === 403,
    `no-perm publish code=${noPermPublish.json?.code ?? noPermPublish.http}`,
  )

  const publish = await api('POST', `/api/admin/resources/${id}/publish`, {
    headers: adminHeaders,
  })
  add('P4-D1', publish.json?.code === 200, `publish code=${publish.json?.code ?? publish.http}`)

  const detailAfterPublish = await api('GET', `/api/admin/resources/${id}`, {
    headers: adminHeaders,
  })
  add(
    'P4-D2',
    isPublishedLabel(detailAfterPublish.json?.data?.shelfStatusLabel),
    `shelf=${detailAfterPublish.json?.data?.shelfStatusLabel}`,
  )

  const publicAfterPublish = await api('GET', `/api/primary-chinese/${id}`)
  add(
    'P4-D3',
    publicAfterPublish.json?.code === 200 && !!publicAfterPublish.json?.data?.id,
    `public after publish code=${publicAfterPublish.json?.code ?? publicAfterPublish.http}`,
  )

  return finish()
}

async function finish() {
  const passed = checks.filter((c) => c.pass).length
  const failed = checks.length - passed
  console.log('\n=== SUMMARY ===')
  console.log(`PASS: ${passed} / ${checks.length}  FAIL: ${failed}`)

  const fs = await import('fs')
  const path = await import('path')
  const { fileURLToPath } = await import('url')
  const dir = path.dirname(fileURLToPath(import.meta.url))
  const outFile = path.join(dir, 'phase4-acceptance-result.json')
  fs.writeFileSync(
    outFile,
    JSON.stringify({ base: BASE, at: new Date().toISOString(), checks }, null, 2),
    'utf8',
  )
  console.log('Results saved:', outFile)
  process.exit(failed > 0 ? 1 : 0)
}

main().catch((error) => {
  console.error(error)
  process.exit(2)
})
