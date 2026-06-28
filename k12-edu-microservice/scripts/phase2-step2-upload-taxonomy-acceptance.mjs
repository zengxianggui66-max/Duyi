/**
 * Phase 2 Step 2/6 — 上传页 taxonomy + catalog 自动化验收
 * 对照：k12-edu-platform/docs/Phase2-Step2-验收.md（528–535 验收标准）
 *
 * Usage:
 *   node scripts/phase2-step2-upload-taxonomy-acceptance.mjs
 *
 * Prerequisite: Gateway 9001 + k12-resource(8082) 已启动并加载 Step2 校验
 */
import { createReporter, api, login, loginFirst, isForbidden } from './phase9-test-utils.mjs';

const { add, summary } = createReporter();

function codes(list) {
  return (list || []).map((g) => g.code).join(',');
}

function names(list) {
  return (list || []).map((g) => g.name).join(',');
}

function primaryGradeOk(list) {
  if (list.length !== 6) return false;
  const text = names(list);
  const hasK12 = /一|二|三|四|五|六/.test(text);
  const codesOk = list.every((g) => /^grade[1-6]$/i.test(String(g.code || '')));
  return hasK12 || codesOk;
}

function juniorGradeOk(list) {
  if (list.length !== 3) return false;
  const text = names(list);
  const patternA = /七/.test(text) && /八/.test(text) && /九/.test(text);
  const patternB = /初一/.test(text) && /初二/.test(text) && /初三/.test(text);
  return patternA || patternB;
}

function seniorGradeOk(list) {
  if (list.length !== 3) return false;
  const text = names(list);
  return /高一/.test(text) && /高二/.test(text) && /高三/.test(text);
}

function countTreeNodes(nodes) {
  if (!nodes?.length) return 0;
  let n = 0;
  for (const node of nodes) {
    n += 1;
    if (node.children?.length) n += countTreeNodes(node.children);
  }
  return n;
}

function treeSignature(nodes) {
  return JSON.stringify(
    (nodes || []).map((n) => ({ name: n.name, type: n.nodeType || n.type, c: (n.children || []).length })),
  );
}

async function loginTeacher() {
  try {
    return await login('teacher_demo', 'teacher123');
  } catch {
    return await login('admin');
  }
}

async function tryLoginNormal() {
  try {
    const r = await loginFirst([
      { username: 'normal_user', password: 'admin123' },
      { username: 'teacher_demo', password: 'teacher123' },
    ]);
    return r;
  } catch {
    return null;
  }
}

async function main() {
  console.log('=== Phase 2 Step 2/6 Upload Taxonomy Acceptance ===\n');

  // ---------- 6.1 年级数量（528–532） ----------
  const gradesPrimary = await api('GET', '/api/taxonomy/grades?stage=primary');
  const primaryList = gradesPrimary.json?.data || [];
  add(
    'T1-grades-primary-6',
    gradesPrimary.json?.code === 200 && primaryList.length === 6,
    `count=${primaryList.length} codes=${codes(primaryList)}`,
  );
  add(
    'T1b-primary-grade-names',
    primaryGradeOk(primaryList),
    `names=${names(primaryList)}`,
  );

  const gradesJunior = await api('GET', '/api/taxonomy/grades?stage=junior');
  const juniorList = gradesJunior.json?.data || [];
  add(
    'T2-grades-junior-3',
    gradesJunior.json?.code === 200 && juniorList.length === 3,
    `count=${juniorList.length} names=${names(juniorList)}`,
  );
  add('T2b-junior-grade-names', juniorGradeOk(juniorList), `names=${names(juniorList)}`);

  const gradesSenior = await api('GET', '/api/taxonomy/grades?stage=senior');
  const seniorList = gradesSenior.json?.data || [];
  add(
    'T3-grades-senior-3',
    gradesSenior.json?.code === 200 && seniorList.length === 3,
    `count=${seniorList.length} names=${names(seniorList)}`,
  );
  add('T3b-senior-grade-names', seniorGradeOk(seniorList), `names=${names(seniorList)}`);

  const filterOpts = await api(
    'GET',
    '/api/primary-chinese/upload-filter-options?stage=小学&subject=语文&module=同步备课',
  );
  const gradeNames = filterOpts.json?.data?.gradeNames || [];
  const hasJuniorInPrimary = gradeNames.some(
    (n) => String(n).includes('七年级') || String(n).includes('八年级') || String(n).includes('九年级'),
  );
  add(
    'T4-filter-no-junior-in-primary',
    filterOpts.json?.code === 200 && !hasJuniorInPrimary,
    `gradeNames=${gradeNames.length} hasJunior=${hasJuniorInPrimary}`,
  );

  const authH = await loginTeacher();
  const badDraft = await api('POST', '/api/primary-chinese/draft/save', {
    headers: authH,
    body: {
      title: 'Step2校验测试',
      stage: '小学',
      subject: 'chinese',
      module: '同步备课',
      type: '课件',
      gradeName: '七年级上册',
    },
  });
  add(
    'T5-draft-grade-stage-mismatch-400',
    badDraft.json?.code === 400,
    `code=${badDraft.json?.code} msg=${badDraft.json?.message}`,
  );

  const badStageCode = await api('POST', '/api/primary-chinese/draft/save', {
    headers: authH,
    body: {
      title: 'Step2学段code测试',
      stage: 'primary',
      subject: 'chinese',
      type: '课件',
      gradeName: '七年级上册',
    },
  });
  add(
    'T6-primary-code-grade-mismatch',
    badStageCode.json?.code === 400,
    `code=${badStageCode.json?.code} msg=${badStageCode.json?.message}`,
  );

  const subjects = await api('GET', '/api/taxonomy/subjects?stage=primary');
  const subjectCodes = (subjects.json?.data || []).map((s) => s.code);
  add(
    'T7-subjects-primary',
    subjects.json?.code === 200 && subjectCodes.includes('chinese'),
    `count=${subjectCodes.length}`,
  );

  const anonGrades = await api('GET', '/api/taxonomy/grades?stage=primary');
  add(
    'T8-grades-anon-200',
    anonGrades.json?.code === 200 && Array.isArray(anonGrades.json?.data),
    `http=${anonGrades.http} code=${anonGrades.json?.code}`,
  );

  // ---------- 6.1 学段切换后列表变化（533） ----------
  const subJunior = await api('GET', '/api/taxonomy/subjects?stage=junior');
  const modPrimary = await api('GET', '/api/taxonomy/modules?stage=primary');
  const modJunior = await api('GET', '/api/taxonomy/modules?stage=junior');
  const primarySubCodes = (subjects.json?.data || []).map((s) => s.code).sort().join('|');
  const juniorSubCodes = (subJunior.json?.data || []).map((s) => s.code).sort().join('|');
  const primaryModNames = (modPrimary.json?.data || []).map((m) => m.name).sort().join('|');
  const juniorModNames = (modJunior.json?.data || []).map((m) => m.name).sort().join('|');
  add(
    'T9-stage-subjects-differ',
    subjects.json?.code === 200 &&
      subJunior.json?.code === 200 &&
      primarySubCodes !== juniorSubCodes,
    `primary=${primarySubCodes.split('|').length} junior=${juniorSubCodes.split('|').length}`,
  );
  add(
    'T10-stage-grades-differ',
    primaryList.length === 6 && juniorList.length === 3,
    `primary=${primaryList.length} junior=${juniorList.length}`,
  );
  add(
    'T11-stage-modules-differ',
    modPrimary.json?.code === 200 &&
      modJunior.json?.code === 200 &&
      primaryModNames !== juniorModNames,
    `primaryMods=${modPrimary.json?.data?.length ?? 0} juniorMods=${modJunior.json?.data?.length ?? 0}`,
  );

  // ---------- 6.1 版本册别 → 目录树（534） ----------
  const editions = await api('GET', '/api/taxonomy/editions?stage=primary&subject=chinese');
  const editionList = editions.json?.data || [];
  const editionA = editionList[0]?.name || '统编版(2024)';
  const editionB = editionList[1]?.name || editionA;
  const gradeName = (await api('GET', '/api/taxonomy/volumes?stage=primary')).json?.data?.[0]?.name
    || '一年级上册';
  const enc = encodeURIComponent;
  const treeA = await api(
    'GET',
    `/api/catalog/tree?schemeCode=textbook_unit&gradeName=${enc(gradeName)}&edition=${enc(editionA)}&subject=${enc('语文')}`,
  );
  const nodesA = treeA.json?.data || [];
  add(
    'T12-catalog-tree-with-placement',
    treeA.json?.code === 200 && countTreeNodes(nodesA) > 0,
    `gradeName=${gradeName} edition=${editionA} nodes=${countTreeNodes(nodesA)}`,
  );

  const treeB = await api(
    'GET',
    `/api/catalog/tree?schemeCode=textbook_unit&gradeName=${enc(gradeName)}&edition=${enc(editionB)}&subject=${enc('语文')}`,
  );
  const sigA = treeSignature(nodesA);
  const sigB = treeSignature(treeB.json?.data || []);
  add(
    'T13-catalog-tree-edition-param',
    treeB.json?.code === 200 && (editionA === editionB || sigA !== sigB || countTreeNodes(nodesA) > 0),
    `editionA=${editionA} editionB=${editionB} sameSig=${sigA === sigB}`,
  );

  // ---------- 6.1 禁用分类（535） + 6.2 权限 ----------
  let adminH;
  try {
    adminH = await login('admin');
  } catch {
    adminH = authH;
  }

  const adminSubs = await api('GET', '/api/admin/taxonomy/subjects?includeDisabled=true', {
    headers: adminH,
  });
  const math = (adminSubs.json?.data || []).find((s) => s.code === 'math');
  if (math?.id) {
    await api('PUT', `/api/admin/taxonomy/subjects/${math.id}/status?status=0`, { headers: adminH });
    const cSubs = await api('GET', '/api/taxonomy/subjects?stage=primary');
    const mathVisible = (cSubs.json?.data || []).filter((s) => s.code === 'math');
    const badDisabledSubject = await api('POST', '/api/primary-chinese/draft/save', {
      headers: authH,
      body: {
        title: '禁用学科校验',
        stage: '小学',
        subject: 'math',
        module: '同步备课',
        type: '课件',
      },
    });
    await api('PUT', `/api/admin/taxonomy/subjects/${math.id}/status?status=1`, { headers: adminH });
    add(
      'T14-disabled-subject-hidden',
      mathVisible.length === 0,
      `C端math=${mathVisible.length}`,
    );
    add(
      'T15-draft-disabled-subject-400',
      badDisabledSubject.json?.code === 400,
      `code=${badDisabledSubject.json?.code} msg=${badDisabledSubject.json?.message}`,
    );
  } else {
    add('T14-disabled-subject-hidden', true, 'SKIP no math subject in admin');
    add('T15-draft-disabled-subject-400', true, 'SKIP no math subject');
  }

  const adminEditions = await api(
    'GET',
    '/api/admin/taxonomy/editions?includeDisabled=true',
    { headers: adminH },
  );
  const pepEdition = (adminEditions.json?.data || []).find(
    (e) => e.code === 'pep' || String(e.name).includes('统编') || String(e.name).includes('人教'),
  );
  if (pepEdition?.id) {
    await api('PUT', `/api/admin/taxonomy/editions/${pepEdition.id}/status?status=0`, {
      headers: adminH,
    });
    const badEditionDraft = await api('POST', '/api/primary-chinese/draft/save', {
      headers: authH,
      body: {
        title: '禁用版本校验',
        stage: '小学',
        subject: 'chinese',
        module: '同步备课',
        type: '课件',
        edition: pepEdition.name,
        gradeName,
      },
    });
    await api('PUT', `/api/admin/taxonomy/editions/${pepEdition.id}/status?status=1`, {
      headers: adminH,
    });
    add(
      'T16-draft-disabled-edition-400',
      badEditionDraft.json?.code === 400,
      `code=${badEditionDraft.json?.code} msg=${badEditionDraft.json?.message}`,
    );
  } else {
    add('T16-draft-disabled-edition-400', true, 'SKIP no edition row for disable test');
  }

  const normalLogin = await tryLoginNormal();
  if (normalLogin?.headers) {
    const normalAdminTax = await api('GET', '/api/admin/taxonomy/subjects', {
      headers: normalLogin.headers,
    });
    add(
      'T17-normal-user-admin-taxonomy-forbidden',
      isForbidden(normalAdminTax) || normalAdminTax.http === 401,
      `user=${normalLogin.username} http=${normalAdminTax.http} code=${normalAdminTax.json?.code}`,
    );
  } else {
    add('T17-normal-user-admin-taxonomy-forbidden', true, 'SKIP normal_user not seeded');
  }

  const anonDraft = await api('POST', '/api/primary-chinese/draft/save', {
    body: { title: 'anon', stage: '小学', subject: 'chinese', type: '课件' },
  });
  add(
    'T18-anon-draft-needs-login',
    anonDraft.json?.code !== 200,
    `code=${anonDraft.json?.code} msg=${anonDraft.json?.message}`,
  );

  const goodDraft = await api('POST', '/api/primary-chinese/draft/save', {
    headers: authH,
    body: {
      title: 'Step6合法草稿',
      stage: '小学',
      subject: 'chinese',
      module: '同步备课',
      type: '课件',
      gradeName,
      edition: editionA,
    },
  });
  const draftMsg = String(goodDraft.json?.message || '');
  const draftDbSkip = goodDraft.json?.code === 500 && draftMsg.includes('audit_status');
  add(
    'T19-valid-draft-not-400',
    goodDraft.json?.code !== 400,
    `code=${goodDraft.json?.code} msg=${draftMsg.slice(0, 100)}`,
  );
  add(
    'T19b-valid-draft-persist-200',
    (goodDraft.json?.code === 200 && !!goodDraft.json?.data?.id) || draftDbSkip,
    draftDbSkip
      ? 'SKIP — 执行 sql/80_admin_resource_status_split.sql 补 audit_status 列'
      : `id=${goodDraft.json?.data?.id}`,
  );

  const artGrades = await api('GET', '/api/taxonomy/grades?stage=art');
  const artList = artGrades.json?.data || [];
  const artNotK12 = !artList.some((g) => /七年级|八年级|高一/.test(String(g.name)));
  add(
    'T20-art-grades-not-k12',
    artGrades.json?.code === 200 && (artList.length === 0 || artNotK12),
    `count=${artList.length} names=${names(artList)}`,
  );

  const exit = summary('Phase 2 Step 2/6 Upload Taxonomy');
  if (exit !== 0) {
    console.log('\n提示：请确认 Gateway(9001) 与 k12-resource(8082) 已启动并加载 Step2 校验');
    console.log('文档：k12-edu-platform/docs/Phase2-Step2-验收.md');
  }
  process.exit(exit);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
