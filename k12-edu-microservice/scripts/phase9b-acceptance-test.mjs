/**
 * Phase 9-B acceptance: search lexicon + redirect
 * Usage: node scripts/phase9b-acceptance-test.mjs
 */
import { createReporter, api, login, isForbidden } from './phase9-test-utils.mjs';

const { add, summary } = createReporter();
const ts = Date.now();

async function main() {
  console.log('=== Phase 9-B Acceptance Test ===\n');
  const adminH = await login('admin');
  const auditorH = await login('auditor');

  const testKeyword = `phase9b-${ts}`;
  const testRedirectKw = `phase9b-redirect-${ts}`;
  let synonymId;
  let redirectId;

  // B1: list synonyms
  const synList = await api('GET', '/api/admin/search/synonyms', { headers: adminH });
  add('B1-list-synonyms', synList.json?.code === 200, `code=${synList.json?.code}`);

  // B2: create synonym + reload implied
  const synCreate = await api('POST', '/api/admin/search/synonyms', {
    headers: adminH,
    body: {
      word: testKeyword,
      synonyms: `${testKeyword},别名${ts}`,
      domain: 'global',
      canonical: testKeyword,
      status: 1,
    },
  });
  synonymId = synCreate.json?.data?.id;
  add(
    'B2-create-synonym',
    synCreate.json?.code === 200 && synonymId,
    `id=${synonymId} code=${synCreate.json?.code}`,
  );

  // B3: synonym draft from zero-result keyword
  const draftKw = `draft-${ts}`;
  const draft = await api('POST', `/api/admin/search/synonyms/draft?keyword=${encodeURIComponent(draftKw)}`, {
    headers: adminH,
  });
  add(
    'B3-synonym-draft',
    draft.json?.code === 200 && draft.json?.data?.word === draftKw,
    `word=${draft.json?.data?.word} status=${draft.json?.data?.status}`,
  );

  // B4: vague subject redirect rejected
  const vague = await api('POST', '/api/admin/search/redirects', {
    headers: adminH,
    body: { keyword: '语文', routePath: '/subject/primary/chinese', status: 1 },
  });
  add(
    'B4-vague-subject-blocked',
    vague.json?.code === 400 || vague.http === 400,
    `code=${vague.json?.code} msg=${vague.json?.message}`,
  );

  // B5: create redirect + public redirect API
  const redCreate = await api('POST', '/api/admin/search/redirects', {
    headers: adminH,
    body: {
      keyword: testRedirectKw,
      title: testRedirectKw,
      routePath: '/news',
      priority: 200,
      status: 1,
    },
  });
  redirectId = redCreate.json?.data?.id;
  const pubRedirect = await api('GET', `/api/search/redirect?q=${encodeURIComponent(testRedirectKw)}`);
  add(
    'B5-db-redirect-hit',
    redCreate.json?.code === 200 &&
      pubRedirect.json?.code === 200 &&
      pubRedirect.json?.data?.directHit === true &&
      pubRedirect.json?.data?.reason === 'db_redirect_rule',
    `redirectId=${redirectId} directHit=${pubRedirect.json?.data?.directHit} reason=${pubRedirect.json?.data?.reason}`,
  );

  // B6: seed redirect priority (传统文化 from SQL seed)
  const seedRedirect = await api('GET', '/api/search/redirect?q=' + encodeURIComponent('传统文化'));
  add(
    'B6-seed-redirect',
    seedRedirect.json?.code === 200 && seedRedirect.json?.data?.directHit === true,
    `reason=${seedRedirect.json?.data?.reason}`,
  );

  // B7: intent rules read-only
  const intents = await api('GET', '/api/admin/search/intent-rules', { headers: adminH });
  add(
    'B7-intent-rules',
    intents.json?.code === 200 && Array.isArray(intents.json?.data),
    `count=${intents.json?.data?.length ?? 0}`,
  );

  // B8: auditor cannot edit
  const auditorEdit = await api('POST', '/api/admin/search/synonyms', {
    headers: auditorH,
    body: { word: 'auditor-block', synonyms: 'x', domain: 'global', status: 1 },
  });
  add(
    'B8-auditor-403-edit',
    isForbidden(auditorEdit),
    `http=${auditorEdit.http} code=${auditorEdit.json?.code}`,
  );

  // B9: create synonym writes operation log
  const before = await api('GET', '/api/admin/system/logs?module=search&action=create_synonym&size=1', {
    headers: adminH,
  });
  const beforeTopId = before.json?.data?.records?.[0]?.id ?? 0;
  await api('POST', '/api/admin/search/synonyms', {
    headers: adminH,
    body: {
      word: `log-${ts}`,
      synonyms: 'a,b',
      domain: 'global',
      status: 1,
    },
  });
  const after = await api('GET', '/api/admin/system/logs?module=search&action=create_synonym&size=1', {
    headers: adminH,
  });
  const afterTopId = after.json?.data?.records?.[0]?.id ?? 0;
  add('B9-synonym-oplog', afterTopId > beforeTopId, `logId ${beforeTopId}->${afterTopId}`);

  // cleanup
  if (synonymId) await api('DELETE', `/api/admin/search/synonyms/${synonymId}`, { headers: adminH });
  if (redirectId) await api('DELETE', `/api/admin/search/redirects/${redirectId}`, { headers: adminH });
  if (draft.json?.data?.id) {
    await api('DELETE', `/api/admin/search/synonyms/${draft.json.data.id}`, { headers: adminH });
  }

  process.exit(summary('Phase 9-B'));
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
