/**
 * Phase 2 Step 4~8 自动化验收（审核解耦 / resource-main / 已通过Tab / 公开双状态）
 * 用法：node scripts/phase2-step4-8-acceptance.mjs
 */
import { createReporter, api, login, loginSession, loginFirst } from './phase9-test-utils.mjs';
import { execSync } from 'node:child_process';

const { add, summary } = createReporter();
const TEST_RESOURCE_ID = process.env.PHASE2_TEST_RESOURCE_ID || '1';
const MYSQL_BIN =
  process.env.MYSQL_BIN || 'C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysql.exe';

function isPublishedLabel(label) {
  return typeof label === 'string' && label.includes('已上架');
}

function resetResourceToPending(id = TEST_RESOURCE_ID) {
  try {
    execSync(
      `"${MYSQL_BIN}" -uroot -pzxg123456 -e "UPDATE xinketang.oss_primary_chinese_resource SET audit_status=0, publish_status=0, status=0 WHERE id=${id};"`,
      { stdio: 'pipe' },
    );
    return true;
  } catch {
    return false;
  }
}

async function loadPendingRecords(adminH) {
  let legacy = await api('GET', '/api/admin/resources/pending?current=1&size=5', {
    headers: adminH,
  });
  let main = await api('GET', '/api/admin/resource-main/pending?current=1&size=5', {
    headers: adminH,
  });
  let records = main.json?.data?.records?.length
    ? main.json.data.records
    : legacy.json?.data?.records || [];
  if (!records.length && resetResourceToPending()) {
    legacy = await api('GET', '/api/admin/resources/pending?current=1&size=5', {
      headers: adminH,
    });
    main = await api('GET', '/api/admin/resource-main/pending?current=1&size=5', {
      headers: adminH,
    });
    records = main.json?.data?.records?.length
      ? main.json.data.records
      : legacy.json?.data?.records || [];
  }
  return { legacy, main, records };
}

async function main() {
  console.log('=== Phase 2 Step 4~8 Acceptance ===');

  const adminH = await login('admin');
  let noPublishH;
  try {
    noPublishH = await login('auditor');
  } catch {
    noPublishH = adminH;
    add('T40-auditor-login', false, 'auditor 账号不可用，权限用例跳过');
  }

  // --- Step 4: 审核与发布解耦 ---
  const { legacy: pendingLegacy, main: pendingMain, records: pendingRecords } =
    await loadPendingRecords(adminH);
  add(
    'T41-pending-apis',
    pendingLegacy.json?.code === 200 && pendingMain.json?.code === 200,
    `legacy=${pendingLegacy.json?.data?.records?.length ?? 0} main=${pendingMain.json?.data?.records?.length ?? 0}`,
  );

  let flowResourceId = pendingRecords[0]?.sourceId ?? pendingRecords[0]?.id;
  if (pendingRecords.length) {
    const id = flowResourceId;
    const approve = await api('POST', `/api/admin/resources/${id}/audit?status=1`, {
      headers: adminH,
    });
    add('T42-approve', approve.json?.code === 200, `code=${approve.json?.code}`);

    const detail = await api('GET', `/api/admin/resources/${id}`, { headers: adminH });
    const approved = detail.json?.data?.auditStatus === 1;
    const notPublished = !isPublishedLabel(detail.json?.data?.shelfStatusLabel);
    add(
      'T43-approve-not-auto-publish',
      approved && notPublished,
      `audit=${detail.json?.data?.auditStatus} shelf=${detail.json?.data?.shelfStatusLabel}`,
    );

    const publicBefore = await api('GET', `/api/primary-chinese/${id}`);
    add(
      'T44-public-hidden-before-publish',
      publicBefore.json?.code !== 200,
      `code=${publicBefore.json?.code ?? publicBefore.http}`,
    );

    if (noPublishH !== adminH) {
      const noPerm = await api('POST', `/api/admin/resources/${id}/publish`, {
        headers: noPublishH,
      });
      add(
        'T45-no-perm-publish',
        noPerm.http === 403 || noPerm.json?.code === 403,
        `code=${noPerm.json?.code ?? noPerm.http}`,
      );
    }

    const publish = await api('POST', `/api/admin/resources/${id}/publish`, { headers: adminH });
    add('T46-publish', publish.json?.code === 200, `code=${publish.json?.code}`);

    const publicAfter = await api('GET', `/api/primary-chinese/${id}`);
    add(
      'T47-public-visible-after-publish',
      publicAfter.json?.code === 200,
      `code=${publicAfter.json?.code ?? publicAfter.http}`,
    );
  } else {
    add('T42-approve', false, 'SKIP — 待审队列为空且无法重置测试资源');
  }

  // --- Step 5: resource-main + sourceType ---
  const allMain = await api('GET', '/api/admin/resource-main?current=1&size=1', {
    headers: adminH,
  });
  const primaryOnly = await api('GET', '/api/admin/resource-main?current=1&size=1&sourceType=primary_chinese', {
    headers: adminH,
  });
  const totalAll = allMain.json?.data?.total ?? -1;
  const totalPrimary = primaryOnly.json?.data?.total ?? -1;
  add(
    'T51-resource-main-list',
    allMain.json?.code === 200 && totalAll >= 0,
    `total=${totalAll}`,
  );
  add(
    'T52-sourceType-filter',
    primaryOnly.json?.code === 200 && totalPrimary >= 0 && totalPrimary <= totalAll,
    `all=${totalAll} primary=${totalPrimary}`,
  );

  // --- Step 6: 我的资源「已通过」Tab ---
  let teacherAuth;
  let teacherId;
  try {
    const logged = await loginFirst([
      { username: 'teacher_demo', password: 'teacher123' },
      { username: 'admin' },
    ]);
    teacherAuth = logged.headers;
    teacherId = logged.userId;
  } catch (e) {
    add('T61-teacher-login', false, String(e.message));
    const session = await loginSession('admin');
    teacherAuth = session.headers;
    teacherId = session.userId;
  }

  const approvedMine = await api('GET', '/api/primary-chinese/page', {
    headers: teacherAuth,
    params: {
      current: 1,
      size: 10,
      uploaderId: teacherId,
      auditStatus: 1,
      publishStatus: 0,
    },
  });
  add(
    'T62-my-approved-query',
    approvedMine.json?.code === 200,
    `code=${approvedMine.json?.code} total=${approvedMine.json?.data?.total ?? '?'}`,
  );

  // --- Step 7: 前台公开查询双状态 ---
  const publicPage = await api('GET', '/api/primary-chinese/page?current=1&size=20');
  const publicRecords = publicPage.json?.data?.records || [];
  const allPublicOk = publicRecords.every(
    (r) =>
      (r.auditStatus == null || r.auditStatus === 1) &&
      (r.publishStatus == null || r.publishStatus === 1) &&
      (r.status == null || r.status === 1),
  );
  add(
    'T71-public-page-dual-status',
    publicPage.json?.code === 200 && allPublicOk,
    `records=${publicRecords.length} allPublished=${allPublicOk}`,
  );

  const browseStats = await api('GET', '/api/resources/browse/stats', {
    params: { catalogNodeId: 1, stage: '小学', subject: 'chinese' },
  });
  add(
    'T72-browse-stats',
    browseStats.json?.code === 200,
    `code=${browseStats.json?.code} total=${browseStats.json?.data?.total ?? '?'}`,
  );

  // --- Step 8: 管理端详情状态文案 ---
  const sampleId = flowResourceId ?? TEST_RESOURCE_ID;
  const adminDetail = await api('GET', `/api/admin/resources/${sampleId}`, { headers: adminH });
  add(
    'T81-admin-detail-labels',
    adminDetail.json?.code === 200 &&
      typeof adminDetail.json?.data?.auditStatusLabel === 'string' &&
      typeof adminDetail.json?.data?.shelfStatusLabel === 'string',
    `auditLabel=${adminDetail.json?.data?.auditStatusLabel} shelf=${adminDetail.json?.data?.shelfStatusLabel}`,
  );

  const code = summary('Phase2 Step4~8');
  process.exit(code);
}

main().catch((e) => {
  console.error(e);
  process.exit(2);
});
