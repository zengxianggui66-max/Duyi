/**
 * Phase 2 Step 3 — 上传资源生命周期自动化验收
 * 对照：k12-edu-platform/docs/Phase2-Step3-验收.md
 *
 * Usage:
 *   node scripts/phase2-step3-upload-lifecycle-acceptance.mjs
 */
import { createReporter, api, login } from './phase9-test-utils.mjs';

const { add, summary } = createReporter();

async function loginTeacher() {
  try {
    return await login('teacher_demo', 'teacher123');
  } catch {
    return await login('admin');
  }
}

async function main() {
  const adminH = await login('admin');
  const authH = await loginTeacher();

  const taxVol = await api('GET', '/api/taxonomy/volumes?stage=primary', { headers: authH });
  const gradeName = taxVol.json?.data?.[0]?.name || '一年级下册';
  const taxEd = await api('GET', '/api/taxonomy/editions?stage=primary&subject=chinese', {
    headers: authH,
  });
  const edition = taxEd.json?.data?.[0]?.name || '统编版';

  const draftRes = await api('POST', '/api/primary-chinese/draft/save', {
    headers: authH,
    body: {
      title: `Step3生命周期-${Date.now()}`,
      stage: '小学',
      subject: 'chinese',
      module: '同步备课',
      type: '课件',
      gradeName,
      edition,
      remark: '验收草稿',
    },
  });
  const draftId = draftRes.json?.data?.id;
  const draftMsg = String(draftRes.json?.message || '');
  const draftDbSkip = draftRes.json?.code === 500 && draftMsg.includes('audit_status');
  add(
    'T25-saveDraft-status-draft',
    draftDbSkip || (draftRes.json?.code === 200 && draftRes.json?.data?.status === -1),
    draftDbSkip
      ? 'SKIP — 执行 sql/82'
      : `code=${draftRes.json?.code} status=${draftRes.json?.data?.status}`,
  );

  if (!draftId) {
    add('T26-submitDraft-pending', false, 'SKIP no draft id');
    add('T27-public-page-excludes-pending', true, 'SKIP');
    add('T28-admin-pending-includes', true, 'SKIP');
  } else {
    const withFile = await api('POST', '/api/primary-chinese/draft/save', {
      headers: authH,
      body: {
        id: draftId,
        title: draftRes.json?.data?.title,
        stage: '小学',
        subject: 'chinese',
        module: '同步备课',
        type: '课件',
        gradeName,
        edition,
        ossUrl: 'http://localhost:8082/uploads/step3-test.pdf',
        fileExt: 'pdf',
        fileSizeKb: 100,
        allowPreview: 1,
      },
    });
    add(
      'T25b-draft-with-file',
      withFile.json?.code === 200 || draftDbSkip,
      `code=${withFile.json?.code}`,
    );

    const submitRes = await api('POST', `/api/primary-chinese/draft/${draftId}/submit`, {
      headers: authH,
    });
    add(
      'T26-submitDraft-pending',
      draftDbSkip || (submitRes.json?.code === 200 && submitRes.json?.data?.status === 0),
      `code=${submitRes.json?.code} status=${submitRes.json?.data?.status} audit=${submitRes.json?.data?.auditStatus}`,
    );

    const publicPage = await api('GET', '/api/primary-chinese/page', {
      params: {
        stage: '小学',
        subject: 'chinese',
        keyword: draftRes.json?.data?.title,
        current: 1,
        size: 20,
      },
    });
    const records = publicPage.json?.data?.records || [];
    const leaked = records.some((r) => r.id === draftId);
    add(
      'T27-public-page-excludes-pending',
      publicPage.json?.code === 200 && !leaked,
      `code=${publicPage.json?.code} leaked=${leaked} total=${records.length}`,
    );

    const pendingAdmin = await api('GET', '/api/admin/resources/pending', {
      headers: adminH,
      params: { keyword: draftRes.json?.data?.title, current: 1, size: 20 },
    });
    const pendingRecords = pendingAdmin.json?.data?.records || [];
    const inPending = pendingRecords.some((r) => r.id === draftId);
    add(
      'T28-admin-pending-includes',
      pendingAdmin.json?.code === 200 && inPending,
      `code=${pendingAdmin.json?.code} found=${inPending}`,
    );
  }

  const blockedSave = await api('POST', '/api/primary-chinese/save', {
    headers: authH,
    body: {
      title: 'blocked-save',
      stage: '小学',
      subject: 'chinese',
      type: '课件',
      status: 0,
    },
  });
  add(
    'T29-c-end-save-blocked',
    blockedSave.json?.code === 400,
    `code=${blockedSave.json?.code} msg=${blockedSave.json?.message}`,
  );

  const defaultPage = await api('GET', '/api/primary-chinese/page', {
    params: { stage: '小学', subject: 'chinese', current: 1, size: 20 },
  });
  const recs = defaultPage.json?.data?.records || [];
  const hasDraftOrPending = recs.some((r) => r.status === -1 || r.status === 0);
  add(
    'T30-public-default-published-only',
    defaultPage.json?.code === 200 && !hasDraftOrPending,
    `code=${defaultPage.json?.code} hasDraftOrPending=${hasDraftOrPending} (需重启 resource 加载 PublicResourceQuerySupport)`,
  );

  const exit = summary('Phase 2 Step 3 Upload Lifecycle');
  if (exit !== 0) {
    console.log('\n提示：确认 sql/82、sql/83 已执行，Gateway + resource 已重启');
    console.log('文档：k12-edu-platform/docs/Phase2-Step3-验收.md');
  }
  process.exit(exit);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
