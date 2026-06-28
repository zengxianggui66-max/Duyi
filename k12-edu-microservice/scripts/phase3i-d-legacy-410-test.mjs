/**
 * Phase 3I-D：旧读 API 410 验收
 *
 * 默认仅验证「开关关 → legacy 仍 200」。
 * 若 staging 已开 feature.legacyReadApi410.enabled，则额外验证 410 + Location。
 *
 * 环境变量：
 *   PHASE3I_EXPECT_LEGACY_410=true  强制断言 410 行为（开关必须已开）
 */
import { api, createReporter } from './phase9-test-utils.mjs';

const LEGACY_READ_CASES = [
  {
    id: 'D1-primary-page',
    path: '/api/primary-chinese/page',
    expectLocation: '/api/resources/page?sourceType=primary_chinese',
  },
  {
    id: 'D1-browse',
    path: '/api/resources/browse',
    params: { current: 1, size: 1 },
    expectLocation: '/api/resources/page?sourceType=primary_chinese',
  },
  {
    id: 'D1-topic-page',
    path: '/api/topic/resources/page',
    params: { current: 1, size: 1 },
    expectLocation: '/api/resources/page?sourceType=topic_resource',
  },
];

async function readLegacy410Enabled() {
  const flags = await api('GET', '/api/public/feature-flags');
  return !!flags.json?.data?.legacyReadApi410Enabled;
}

async function main() {
  const r = createReporter();
  const force410 = String(process.env.PHASE3I_EXPECT_LEGACY_410 ?? 'false') === 'true';
  const flagOn = force410 || (await readLegacy410Enabled());
  r.add('D0-feature-flag-readable', true, `legacyReadApi410Enabled=${flagOn}`);

  for (const item of LEGACY_READ_CASES) {
    const res = await api('GET', item.path, { params: item.params });
    const http = res.http;
    const location = res.location || '';

    if (flagOn) {
      const ok = http === 410 && String(location).includes(item.expectLocation);
      r.add(item.id + '-410', ok, `http=${http} location=${location || '-'}`);
    } else {
      const ok = http === 200 && res.json?.code === 200;
      r.add(item.id + '-legacy-open', ok, `http=${http} code=${res.json?.code ?? '-'}`);
    }
  }

  // 写链路 / 元数据不应 410
  const meta = await api('GET', '/api/topic/filter-options');
  r.add(
    'D1-meta-not-410',
    meta.http === 200,
    `filter-options http=${meta.http}`,
  );

  process.exit(r.summary('Phase3I-D Legacy 410'));
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
