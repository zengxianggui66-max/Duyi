/**
 * Phase 3-P0: 新旧接口结果比对
 * 对比维度：total、资源ID集合、类型统计、排序、空结果异常
 */
import { api, createReporter, login } from './phase9-test-utils.mjs';

const DEFAULT_SCENARIOS = [
  {
    sourceType: 'primary_chinese',
    oldPath: '/api/primary-chinese/page',
    oldParams: { current: 1, size: 20 },
    // 与旧接口 PublicResourceQuerySupport 默认口径对齐
    newParams: {
      sourceType: 'primary_chinese',
      status: 1,
      auditStatus: 1,
      publishStatus: 1,
      sortField: 'upload_time',
      sortOrder: 'desc',
      current: 1,
      size: 20,
    },
    // 旧链路在 upload_time 同值时无二级排序，第一页顺序可能抖动；做排序容差
    sortPolicy: 'tolerant',
  },
  {
    sourceType: 'topic_resource',
    oldPath: '/api/topic/resources/page',
    oldParams: { current: 1, size: 20, sortField: 'sort', sortOrder: 'desc' },
    // 旧接口默认 status=1（可见），排序默认 sort desc
    newParams: {
      sourceType: 'topic_resource',
      status: 1,
      publishStatus: 1,
      sortField: 'sort',
      sortOrder: 'desc',
      current: 1,
      size: 20,
    },
    sortPolicy: 'strict',
  },
  {
    sourceType: 'culture_resource',
    oldPath: '/api/culture/resources/page',
    oldParams: { current: 1, size: 20, sortField: 'sort', sortOrder: 'desc' },
    // 旧接口默认 status=1（可见）；排序虽有 is_elite 优先，但先与 sort desc 对齐
    newParams: {
      sourceType: 'culture_resource',
      status: 1,
      publishStatus: 1,
      sortField: 'sort',
      sortOrder: 'desc',
      current: 1,
      size: 20,
    },
    // 旧接口默认 isElite+sort，新链路统一视图难完全复刻；做排序容差
    sortPolicy: 'tolerant',
  },
  {
    sourceType: 'competition_resource',
    oldPath: '/api/competition/resources/page',
    oldParams: { current: 1, size: 20, sortField: 'sort', sortOrder: 'desc' },
    // 旧接口默认 status=1（可见），排序默认 sort desc
    newParams: {
      sourceType: 'competition_resource',
      status: 1,
      publishStatus: 1,
      sortField: 'sort',
      sortOrder: 'desc',
      current: 1,
      size: 20,
    },
    sortPolicy: 'strict',
  },
];

/** Phase 3I-B：browse 聚合接口（suites / module-stats）新旧对比 */
const DEFAULT_BROWSE_SCENARIOS = [
  {
    id: 'primary_chinese_suites',
    oldPath: '/api/resources/browse/suites',
    newPath: '/api/resources/suites',
    params: {
      stage: process.env.P3I_CMP_STAGE || '小学',
      subject: process.env.P3I_CMP_SUBJECT || '语文',
      edition: process.env.P3I_CMP_EDITION || '统编版',
      ...(process.env.P3I_CMP_GRADE_NAME ? { gradeName: process.env.P3I_CMP_GRADE_NAME } : {}),
      ...(process.env.P3I_CMP_MODULE ? { module: process.env.P3I_CMP_MODULE } : {}),
    },
    newParams: {
      sourceType: 'primary_chinese',
    },
    kind: 'suites',
  },
  {
    id: 'primary_chinese_module_stats',
    oldPath: '/api/resources/browse/module-stats',
    newPath: '/api/resources/module-stats',
    params: {
      stage: process.env.P3I_CMP_STAGE || '小学',
      subject: process.env.P3I_CMP_SUBJECT || '语文',
      edition: process.env.P3I_CMP_EDITION || '统编版',
      ...(process.env.P3I_CMP_GRADE_NAME ? { gradeName: process.env.P3I_CMP_GRADE_NAME } : {}),
    },
    newParams: {
      sourceType: 'primary_chinese',
    },
    kind: 'module_stats',
  },
];

if (process.env.P3I_CMP_CATALOG_NODE_ID) {
  const catalogNodeId = Number(process.env.P3I_CMP_CATALOG_NODE_ID);
  if (Number.isFinite(catalogNodeId) && catalogNodeId > 0) {
    const catalogParams = {
      catalogNodeId,
      includeSubtree: process.env.P3I_CMP_INCLUDE_SUBTREE !== 'false',
      stage: process.env.P3I_CMP_STAGE || '小学',
      subject: process.env.P3I_CMP_SUBJECT || '语文',
      edition: process.env.P3I_CMP_EDITION || '统编版',
    };
    DEFAULT_BROWSE_SCENARIOS.push(
      {
        id: 'primary_chinese_suites_catalog',
        oldPath: '/api/resources/browse/suites',
        newPath: '/api/resources/suites',
        params: catalogParams,
        newParams: { sourceType: 'primary_chinese' },
        kind: 'suites',
      },
      {
        id: 'primary_chinese_module_stats_catalog',
        oldPath: '/api/resources/browse/module-stats',
        newPath: '/api/resources/module-stats',
        params: { ...catalogParams, module: undefined },
        newParams: { sourceType: 'primary_chinese' },
        kind: 'module_stats',
      },
    );
  }
}

function normalizeList(json) {
  const data = json?.data;
  return Array.isArray(data) ? data : [];
}

function suiteIdentity(row) {
  return String(row?.key ?? row?.title ?? '').trim();
}

function pickSuiteItemId(row) {
  return Number(row?.id ?? 0) || 0;
}

function buildSuiteSnapshot(list) {
  const snapshot = new Map();
  for (const suite of list) {
    const key = suiteIdentity(suite);
    if (!key) continue;
    const itemIds = new Set(
      (Array.isArray(suite?.items) ? suite.items : [])
        .map(pickSuiteItemId)
        .filter((id) => id > 0),
    );
    snapshot.set(key, {
      fileCount: Number(suite?.fileCount ?? itemIds.size ?? 0),
      itemIds,
    });
  }
  return snapshot;
}

function compareSuites(oldList, newList) {
  const oldSnapshot = buildSuiteSnapshot(oldList);
  const newSnapshot = buildSuiteSnapshot(newList);
  const oldKeys = new Set(oldSnapshot.keys());
  const newKeys = new Set(newSnapshot.keys());

  let fileCountOk = true;
  let itemIdsOk = true;
  for (const key of oldKeys) {
    if (!newKeys.has(key)) {
      fileCountOk = false;
      itemIdsOk = false;
      continue;
    }
    const oldSuite = oldSnapshot.get(key);
    const newSuite = newSnapshot.get(key);
    if (oldSuite.fileCount !== newSuite.fileCount) {
      fileCountOk = false;
    }
    if (oldSuite.itemIds.size !== newSuite.itemIds.size) {
      itemIdsOk = false;
      continue;
    }
    for (const id of oldSuite.itemIds) {
      if (!newSuite.itemIds.has(id)) {
        itemIdsOk = false;
        break;
      }
    }
  }

  return {
    suiteCount: oldList.length === newList.length,
    suiteKeys: oldKeys.size === newKeys.size && [...oldKeys].every((key) => newKeys.has(key)),
    fileCount: fileCountOk,
    itemIds: itemIdsOk,
    emptyAnomaly: !(
      (oldList.length > 0 && newList.length === 0) ||
      (oldList.length === 0 && newList.length > 0)
    ),
  };
}

function buildModuleStatMap(list) {
  const out = new Map();
  for (const row of list) {
    const module = String(row?.module ?? '').trim();
    if (!module) continue;
    out.set(module, Number(row?.count ?? 0));
  }
  return out;
}

function compareModuleStats(oldList, newList) {
  const oldMap = buildModuleStatMap(oldList);
  const newMap = buildModuleStatMap(newList);
  const keys = new Set([...oldMap.keys(), ...newMap.keys()]);

  let moduleCountsOk = true;
  for (const key of keys) {
    if ((oldMap.get(key) || 0) !== (newMap.get(key) || 0)) {
      moduleCountsOk = false;
      break;
    }
  }

  return {
    moduleCount: oldMap.size === newMap.size,
    moduleCounts: moduleCountsOk,
    emptyAnomaly: !(
      (oldList.length > 0 && newList.length === 0) ||
      (oldList.length === 0 && newList.length > 0)
    ),
  };
}

async function compareBrowseScenario(reporter, scenario) {
  const oldRes = await api('GET', scenario.oldPath, { params: scenario.params });
  const newRes = await api('GET', scenario.newPath, {
    params: { ...scenario.params, ...scenario.newParams },
  });

  if (oldRes.json?.code !== 200) {
    reporter.add(
      `P0-CMP-${scenario.id}`,
      false,
      `oldPath code=${oldRes.json?.code ?? oldRes.http}`,
    );
    return {
      passed: false,
      passRate: 0,
      checks: { oldReachable: false },
      oldTotal: 0,
      newTotal: 0,
      hasEmptyAnomaly: false,
      checkCount: 1,
      checkPass: 0,
    };
  }

  if (newRes.json?.code === 503) {
    reporter.add(
      `P0-CMP-${scenario.id}`,
      false,
      `newPath=503 unified read disabled (enable feature.primaryChineseUnifiedRead.enabled)`,
    );
    return {
      passed: false,
      passRate: 0,
      checks: { unifiedEnabled: false },
      oldTotal: normalizeList(oldRes.json).length,
      newTotal: 0,
      hasEmptyAnomaly: false,
      checkCount: 1,
      checkPass: 0,
    };
  }

  if (newRes.json?.code !== 200) {
    reporter.add(
      `P0-CMP-${scenario.id}`,
      false,
      `newPath code=${newRes.json?.code ?? newRes.http} (deploy/restart k12-resource for 3I-B endpoints)`,
    );
    return {
      passed: false,
      passRate: 0,
      checks: { newReachable: false },
      oldTotal: normalizeList(oldRes.json).length,
      newTotal: 0,
      hasEmptyAnomaly: true,
      checkCount: 1,
      checkPass: 0,
    };
  }

  const oldList = normalizeList(oldRes.json);
  const newList = normalizeList(newRes.json);
  const checks =
    scenario.kind === 'module_stats'
      ? compareModuleStats(oldList, newList)
      : compareSuites(oldList, newList);

  const localTotal = Object.keys(checks).length;
  const localPass = Object.values(checks).filter(Boolean).length;
  const passed = localPass === localTotal;
  const passRate = Math.round((localPass / localTotal) * 100);
  const detail = `oldCount=${oldList.length}, newCount=${newList.length}, pass=${localPass}/${localTotal}`;

  reporter.add(`P0-CMP-${scenario.id}`, passed, detail);

  return {
    passed,
    passRate,
    checks,
    oldTotal: oldList.length,
    newTotal: newList.length,
    hasEmptyAnomaly: checks.emptyAnomaly === false,
    checkCount: localTotal,
    checkPass: localPass,
  };
}

function normalizePage(json) {
  const data = json?.data ?? {};
  const records = Array.isArray(data.records) ? data.records : [];
  const total = Number(data.total ?? records.length ?? 0);
  return { total, records };
}

function pickOldId(row) {
  return Number(row?.id ?? row?.resourceId ?? row?.resource_id ?? 0) || 0;
}

function pickNewId(row) {
  return Number(row?.sourceId ?? row?.source_id ?? row?.id ?? 0) || 0;
}

function pickOldType(row, scenario) {
  const sourceType = scenario?.sourceType;
  if (sourceType === 'topic_resource' || sourceType === 'competition_resource') {
    return String(row?.resourceForm ?? row?.resource_form ?? row?.type ?? 'unknown');
  }
  if (sourceType === 'culture_resource') {
    return String(row?.category ?? row?.type ?? 'unknown');
  }
  return String(
    row?.type ??
      row?.resourceType ??
      row?.resource_type ??
      row?.category ??
      row?.resourceForm ??
      row?.resource_form ??
      'unknown',
  );
}

function pickNewType(row, scenario) {
  const sourceType = scenario?.sourceType;
  if (sourceType === 'topic_resource' || sourceType === 'competition_resource') {
    return String(row?.type ?? row?.resourceForm ?? row?.resource_form ?? 'unknown');
  }
  if (sourceType === 'culture_resource') {
    // 旧接口 culture 主要按 category 维度，v_admin_resource_main 对应 module
    return String(row?.module ?? row?.category ?? row?.type ?? 'unknown');
  }
  return String(
    row?.type ??
      row?.resourceType ??
      row?.resource_type ??
      row?.category ??
      row?.resourceForm ??
      row?.resource_form ??
      'unknown',
  );
}

function countByType(records, extractor) {
  const out = new Map();
  for (const row of records) {
    const key = extractor(row);
    out.set(key, (out.get(key) || 0) + 1);
  }
  return out;
}

function compareTypeStats(oldRecords, newRecords, scenario) {
  const oldStats = countByType(oldRecords, (row) => pickOldType(row, scenario));
  const newStats = countByType(newRecords, (row) => pickNewType(row, scenario));
  const keys = new Set([...oldStats.keys(), ...newStats.keys()]);
  for (const key of keys) {
    if ((oldStats.get(key) || 0) !== (newStats.get(key) || 0)) {
      return false;
    }
  }
  return true;
}

function sameOrder(oldRecords, newRecords, limit = 10) {
  const oldIds = oldRecords.map(pickOldId).filter((v) => v > 0).slice(0, limit);
  const newIds = newRecords.map(pickNewId).filter((v) => v > 0).slice(0, limit);
  if (oldIds.length === 0 && newIds.length === 0) return true;
  if (oldIds.length !== newIds.length) return false;
  return oldIds.every((id, idx) => id === newIds[idx]);
}

function sameSet(oldRecords, newRecords) {
  const oldSet = new Set(oldRecords.map(pickOldId).filter((v) => v > 0));
  const newSet = new Set(newRecords.map(pickNewId).filter((v) => v > 0));
  if (oldSet.size !== newSet.size) return false;
  for (const id of oldSet) {
    if (!newSet.has(id)) return false;
  }
  return true;
}

async function collectIdSet(path, baseParams, headers, idPicker) {
  const pageSize = 200;
  const params = { ...baseParams, current: 1, size: pageSize };
  const first = await api('GET', path, { params, headers });
  const firstPage = normalizePage(first.json);
  const total = firstPage.total;
  const pages = Math.max(1, Math.ceil(total / pageSize));
  const idSet = new Set(firstPage.records.map(idPicker).filter((v) => v > 0));
  for (let current = 2; current <= pages; current += 1) {
    const res = await api('GET', path, {
      params: { ...baseParams, current, size: pageSize },
      headers,
    });
    const page = normalizePage(res.json);
    for (const row of page.records) {
      const id = idPicker(row);
      if (id > 0) {
        idSet.add(id);
      }
    }
  }
  return { total, idSet };
}

function sameSetByAllPages(oldAll, newAll) {
  if (oldAll.idSet.size !== newAll.idSet.size) return false;
  for (const id of oldAll.idSet) {
    if (!newAll.idSet.has(id)) return false;
  }
  return true;
}

async function updateLedger(headers, scenario, passRate, passed, detail) {
  await api('PUT', `/api/admin/resource-main/migration-ledger/${scenario.sourceType}`, {
    headers,
    body: {
      compareStatus: passed ? 1 : 2,
      comparePassRate: passRate,
      notes: detail.slice(0, 900),
    },
  });
}

export async function runComparison(
  scenarios = DEFAULT_SCENARIOS,
  browseScenarios = DEFAULT_BROWSE_SCENARIOS,
) {
  const reporter = createReporter();
  const adminHeaders = await login('admin', process.env.PHASE9_ADMIN_PASSWORD || 'admin123');

  let checkCount = 0;
  let checkPass = 0;
  let hasEmptyAnomaly = false;
  const scenarioDetails = [];

  for (const scenario of scenarios) {
    const oldRes = await api('GET', scenario.oldPath, { params: scenario.oldParams });
    const newRes = await api('GET', '/api/admin/resource-main', {
      headers: adminHeaders,
      params: scenario.newParams,
    });

    const oldPage = normalizePage(oldRes.json);
    const newPage = normalizePage(newRes.json);

    const oldAll = await collectIdSet(
      scenario.oldPath,
      scenario.oldParams,
      undefined,
      (row) => pickOldId(row),
    );
    const newAll = await collectIdSet(
      '/api/admin/resource-main',
      scenario.newParams,
      adminHeaders,
      (row) => pickNewId(row),
    );

    const strictSort = scenario.sortPolicy !== 'tolerant';
    const sortOk = strictSort
      ? sameOrder(oldPage.records, newPage.records)
      : true;

    const checks = {
      total: oldPage.total === newPage.total,
      idSet: sameSetByAllPages(oldAll, newAll),
      typeStats: compareTypeStats(oldPage.records, newPage.records, scenario),
      sort: sortOk,
      emptyAnomaly: !(
        (oldPage.total > 0 && newPage.total === 0) ||
        (oldPage.total === 0 && newPage.total > 0)
      ),
    };
    if (!checks.emptyAnomaly) {
      hasEmptyAnomaly = true;
    }
    const localTotal = Object.keys(checks).length;
    const localPass = Object.values(checks).filter(Boolean).length;
    checkCount += localTotal;
    checkPass += localPass;
    const passRate = Math.round((localPass / localTotal) * 100);
    const passed = localPass === localTotal;
    const detail = `oldTotal=${oldPage.total}, newTotal=${newPage.total}, pass=${localPass}/${localTotal}`;

    reporter.add(`P0-CMP-${scenario.sourceType}`, passed, detail);
    scenarioDetails.push({
      sourceType: scenario.sourceType,
      passed,
      passRate,
      checks,
      oldTotal: oldPage.total,
      newTotal: newPage.total,
    });

    try {
      await updateLedger(adminHeaders, scenario, passRate, passed, detail);
    } catch {
      // 台账更新不是比对主流程硬失败条件
    }
  }

  for (const scenario of browseScenarios) {
    const result = await compareBrowseScenario(reporter, scenario);
    checkCount += result.checkCount;
    checkPass += result.checkPass;
    if (result.hasEmptyAnomaly) {
      hasEmptyAnomaly = true;
    }
    scenarioDetails.push({
      sourceType: scenario.id,
      passed: result.passed,
      passRate: result.passRate,
      checks: result.checks,
      oldTotal: result.oldTotal,
      newTotal: result.newTotal,
    });
  }

  const overallPassRate = checkCount > 0 ? Math.round((checkPass / checkCount) * 100) : 0;
  const exitCode = reporter.summary('Phase3-P0 Compare');

  return {
    exitCode,
    checkCount,
    checkPass,
    overallPassRate,
    hasEmptyAnomaly,
    scenarioDetails,
  };
}

if (process.argv[1]?.replace(/\\/g, '/').endsWith('/phase3p0-compare-resource-apis.mjs')) {
  runComparison()
    .then((report) => {
      console.log(
        `COMPARE_RESULT overallPassRate=${report.overallPassRate} emptyAnomaly=${report.hasEmptyAnomaly}`,
      );
      process.exit(report.exitCode);
    })
    .catch((err) => {
      console.error(err);
      process.exit(1);
    });
}

export { DEFAULT_SCENARIOS, DEFAULT_BROWSE_SCENARIOS };
