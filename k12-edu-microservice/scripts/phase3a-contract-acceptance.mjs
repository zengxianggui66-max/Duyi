/**
 * Phase 3A 契约冻结验收
 * 目标：验证文档/关键术语/关键入口是否齐全
 */
import fs from 'node:fs';
import path from 'node:path';
import { createReporter } from './phase9-test-utils.mjs';

const ROOT = process.cwd();

const DOCS = {
  index: 'docs/Phase3A-资源主链路统一-文档目录.md',
  contract: 'docs/Phase3A-资源主链路统一-接口契约.md',
  dict: 'docs/Phase3A-资源主链路统一-字段字典.md',
  stateMachine: 'docs/Phase3A-资源主链路统一-枚举与状态机.md',
  mapping: 'docs/Phase3A-资源主链路统一-兼容映射与执行步骤.md',
  execution: 'docs/Phase3A-执行与验收清单.md',
};

const REQUIRED_BACKEND_FILES = [
  'k12-resource/src/main/java/com/k12/resource/controller/EduMasterResourceController.java',
  'k12-resource/src/main/java/com/k12/resource/controller/AdminResourcesController.java',
  'k12-resource/src/main/java/com/k12/resource/controller/AdminResourceMainController.java',
  'k12-resource/src/main/java/com/k12/resource/controller/PrimaryChineseResourceController.java',
  'k12-resource/src/main/java/com/k12/resource/controller/TopicController.java',
  'k12-resource/src/main/java/com/k12/resource/controller/CultureStudyController.java',
  'k12-resource/src/main/java/com/k12/resource/controller/CompetitionController.java',
];

const REQUIRED_FRONTEND_API_FILES = [
  '../k12-edu-platform/src/api/resources.ts',
  '../k12-edu-platform/src/api/primaryChinese.ts',
  '../k12-edu-platform/src/api/browse.ts',
  '../k12-edu-platform/src/api/topic.ts',
  '../k12-edu-platform/src/api/cultureStudy.ts',
  '../k12-edu-platform/src/api/competition.ts',
];

function read(relPath) {
  const abs = path.resolve(ROOT, relPath);
  return fs.existsSync(abs) ? fs.readFileSync(abs, 'utf8') : '';
}

function exists(relPath) {
  return fs.existsSync(path.resolve(ROOT, relPath));
}

function includesAll(text, arr) {
  return arr.every((k) => text.includes(k));
}

async function main() {
  const r = createReporter();

  // A1: 文档存在性
  for (const [k, p] of Object.entries(DOCS)) {
    r.add(`A1-doc-${k}`, exists(p), p);
  }

  // A2: 契约关键术语冻结
  const contract = read(DOCS.contract);
  const dict = read(DOCS.dict);
  const sm = read(DOCS.stateMachine);
  const mapping = read(DOCS.mapping);

  r.add(
    'A2-contract-api',
    includesAll(contract, ['/api/resources/page', '/api/admin/resources']),
    'contract contains unified paths',
  );
  r.add(
    'A2-dict-core-fields',
    includesAll(dict, ['canonicalResourceId', 'contentDomain', 'sourceType', 'placementType']),
    'dictionary contains 4 core fields',
  );
  r.add(
    'A2-state-machine',
    includesAll(sm, ['auditStatus', 'publishStatus', '双状态']),
    'state machine contains dual-status semantics',
  );
  r.add(
    'A2-mapping-table',
    includesAll(mapping, ['/api/primary-chinese/page', '/api/resources/page', '旧接口', '新接口']),
    'compat mapping table exists',
  );

  // A3: 代码基线入口存在性（契约落点检查）
  const backendOk = REQUIRED_BACKEND_FILES.every(exists);
  r.add(
    'A3-backend-entry-files',
    backendOk,
    `found=${REQUIRED_BACKEND_FILES.filter(exists).length}/${REQUIRED_BACKEND_FILES.length}`,
  );
  const feOk = REQUIRED_FRONTEND_API_FILES.every(exists);
  r.add(
    'A3-frontend-api-files',
    feOk,
    `found=${REQUIRED_FRONTEND_API_FILES.filter(exists).length}/${REQUIRED_FRONTEND_API_FILES.length}`,
  );

  process.exit(r.summary('Phase3-A Contract Acceptance'));
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
