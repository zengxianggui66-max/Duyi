/**
 * Phase 3D + 3E 基础验收
 * - 3D: /api/resources 统一读写行为
 * - 3E: /api/admin/resource-main 写接口可用性
 */
import { api, createReporter, login } from './phase9-test-utils.mjs';

async function main() {
  const r = createReporter();
  const adminH = await login('admin', process.env.PHASE9_ADMIN_PASSWORD || 'admin123');
  r.add('D0-admin-login', !!adminH?.Authorization, 'admin token ready');

  const page = await api('GET', '/api/resources/page', {
    headers: adminH,
    params: { current: 1, size: 5 },
  });
  const pageOk = page.json?.code === 200 && Array.isArray(page.json?.data?.records);
  r.add('D1-public-page', pageOk, `code=${page.json?.code ?? page.http}`);

  const firstId = page.json?.data?.records?.[0]?.globalId;
  if (firstId) {
    const detail = await api('GET', `/api/resources/detail/${firstId}`, { headers: adminH });
    r.add('D2-public-detail', detail.json?.code === 200, `code=${detail.json?.code ?? detail.http}`);

    const stats = await api('GET', '/api/resources/stats', { headers: adminH });
    r.add('D3-public-stats', stats.json?.code === 200, `code=${stats.json?.code ?? stats.http}`);

    const types = await api('GET', '/api/resources/types', { headers: adminH });
    r.add('D4-public-types', types.json?.code === 200, `code=${types.json?.code ?? types.http}`);

    const preview = await api('GET', `/api/resources/${firstId}/preview`, { headers: adminH });
    r.add('D5-public-preview', preview.json?.code === 200, `code=${preview.json?.code ?? preview.http}`);

    const view = await api('POST', `/api/resources/${firstId}/view`, { headers: adminH });
    r.add('D6-public-view', view.json?.code === 200, `code=${view.json?.code ?? view.http}`);

    const download = await api('POST', `/api/resources/${firstId}/download`, { headers: adminH });
    r.add('D7-public-download', download.json?.code === 200, `code=${download.json?.code ?? download.http}`);

    const collect = await api('POST', `/api/resources/${firstId}/collect`, { headers: adminH });
    r.add('D8-public-collect', collect.json?.code === 200, `code=${collect.json?.code ?? collect.http}`);
  } else {
    r.add('D2-public-detail', false, 'no public resource id');
    r.add('D3-public-stats', false, 'no public resource id');
    r.add('D4-public-types', false, 'no public resource id');
    r.add('D5-public-preview', false, 'no public resource id');
    r.add('D6-public-view', false, 'no public resource id');
    r.add('D7-public-download', false, 'no public resource id');
    r.add('D8-public-collect', false, 'no public resource id');
  }

  const adminList = await api('GET', '/api/admin/resource-main', {
    headers: adminH,
    params: { current: 1, size: 5, sourceType: 'primary_chinese' },
  });
  const adminOk = adminList.json?.code === 200 && Array.isArray(adminList.json?.data?.records);
  r.add('E1-admin-resource-main-list', adminOk, `code=${adminList.json?.code ?? adminList.http}`);

  const globalId = adminList.json?.data?.records?.[0]?.globalId;
  if (globalId) {
    const update = await api('PUT', `/api/admin/resource-main/${globalId}`, {
      headers: adminH,
      body: { remark: 'phase3d3e acceptance touch' },
    });
    r.add('E2-admin-update', update.json?.code === 200, `code=${update.json?.code ?? update.http}`);

    const placement = await api('POST', `/api/admin/resource-main/${globalId}/placement`, {
      headers: adminH,
      body: {},
    });
    r.add('E3-admin-placement', placement.json?.code === 200, `code=${placement.json?.code ?? placement.http}`);
  } else {
    r.add('E2-admin-update', false, 'no admin globalId');
    r.add('E3-admin-placement', false, 'no admin globalId');
  }

  const nonPrimaryTypes = ['topic_resource', 'culture_resource', 'competition_resource'];
  let nonPrimaryChecked = false;
  for (const sourceType of nonPrimaryTypes) {
    const list = await api('GET', '/api/admin/resource-main', {
      headers: adminH,
      params: { current: 1, size: 1, sourceType },
    });
    const id = list.json?.data?.records?.[0]?.globalId;
    if (!id) {
      continue;
    }
    nonPrimaryChecked = true;
    const update = await api('PUT', `/api/admin/resource-main/${id}`, {
      headers: adminH,
      body: {},
    });
    r.add(`E4-${sourceType}-update`, update.json?.code === 200, `code=${update.json?.code ?? update.http}`);

    const recommend = await api('POST', `/api/admin/resource-main/${id}/recommend`, {
      headers: adminH,
      params: { enabled: true },
    });
    r.add(`E5-${sourceType}-recommend`, recommend.json?.code === 200, `code=${recommend.json?.code ?? recommend.http}`);

    const insights = await api('GET', `/api/admin/resource-main/${id}/audit-insights`, { headers: adminH });
    r.add(`E6-${sourceType}-audit-insights`, insights.json?.code === 200, `code=${insights.json?.code ?? insights.http}`);
    break;
  }
  if (!nonPrimaryChecked) {
    r.add('E4-non-primary-update', false, 'no non-primary data');
    r.add('E5-non-primary-recommend', false, 'no non-primary data');
    r.add('E6-non-primary-audit-insights', false, 'no non-primary data');
  }

  process.exit(r.summary('Phase3-D3E Acceptance'));
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
