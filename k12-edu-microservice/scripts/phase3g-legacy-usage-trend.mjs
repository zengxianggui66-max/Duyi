/**
 * Phase 3G：旧接口调用趋势报告（7/14 天）
 */
import { api, login } from './phase9-test-utils.mjs'

function aggregate(rows, days) {
  const cutoff = new Date()
  cutoff.setDate(cutoff.getDate() - days + 1)
  const daily = new Map()
  const byApi = new Map()
  for (const row of rows) {
    const d = row?.statDate
    if (!d) continue
    const date = new Date(`${d}T00:00:00`)
    if (Number.isNaN(date.getTime()) || date < cutoff) continue
    const count = Number(row?.hitCount || 0)
    daily.set(d, (daily.get(d) || 0) + count)
    const apiPath = row?.apiPath || 'unknown'
    byApi.set(apiPath, (byApi.get(apiPath) || 0) + count)
  }
  const total = [...daily.values()].reduce((a, b) => a + b, 0)
  return { total, daily, byApi }
}

function printSummary(label, agg) {
  console.log(`\n=== ${label} ===`)
  console.log(`total=${agg.total}`)
  console.log('daily:')
  for (const [day, count] of [...agg.daily.entries()].sort((a, b) => a[0].localeCompare(b[0]))) {
    console.log(`  ${day}: ${count}`)
  }
  console.log('byApi:')
  for (const [apiPath, count] of [...agg.byApi.entries()].sort((a, b) => b[1] - a[1])) {
    console.log(`  ${apiPath}: ${count}`)
  }
}

async function main() {
  const adminH = await login('admin', process.env.PHASE9_ADMIN_PASSWORD || 'admin123')
  const res = await api('GET', '/api/admin/resource-main/legacy-api-usage', {
    headers: adminH,
    params: { days: 14 },
  })
  if (res.json?.code !== 200 || !Array.isArray(res.json?.data)) {
    console.error('加载旧接口调用数据失败:', res.raw)
    process.exit(1)
  }
  const rows = res.json.data
  const agg7 = aggregate(rows, 7)
  const agg14 = aggregate(rows, 14)
  printSummary('Phase3G Legacy Trend (7d)', agg7)
  printSummary('Phase3G Legacy Trend (14d)', agg14)
  process.exit(0)
}

main().catch((e) => {
  console.error(e)
  process.exit(1)
})
