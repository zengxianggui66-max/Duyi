/**
 * Phase 3K 门禁：禁止新增 Controller 直连 COMPAT 源表 Entity
 * （应走 ResourceMainController / AdminResourceMain + Adapter）
 *
 * 基线内为 Phase 3G/3I 遗留 Controller；新增文件若 import 下列 Entity 则 FAIL。
 */
import fs from 'node:fs'
import path from 'node:path'

const MICRO_ROOT = path.resolve(process.cwd())
const CONTROLLER_ROOT = path.join(MICRO_ROOT, 'k12-resource/src/main/java/com/k12/resource/controller')

/** COMPAT 层 Entity — 禁止在新 Controller 中直接引用 */
const COMPAT_ENTITY_IMPORTS = [
  'com.k12.common.entity.TopicResource',
  'com.k12.common.entity.CultureResource',
  'com.k12.common.entity.CompetitionResource',
  'com.k12.common.entity.PrimaryChineseResource',
]

const COMPAT_TABLE_NAMES = [
  'topic_resource',
  'culture_resource',
  'competition_resource',
  'oss_primary_chinese_resource',
]

/** 已存在的 legacy Controller 白名单（相对 controller/） */
const ALLOWED_CONTROLLERS = new Set([
  'TopicController.java',
  'CultureStudyController.java',
  'CompetitionController.java',
  'PrimaryChineseResourceController.java',
  'ResourceBrowseController.java',
  'ResourceController.java',
  'EduMasterResourceController.java',
])

function walkControllers(dir, out = []) {
  if (!fs.existsSync(dir)) return out
  for (const ent of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, ent.name)
    if (ent.isDirectory()) walkControllers(full, out)
    else if (ent.name.endsWith('Controller.java')) out.push(full)
  }
  return out
}

function main() {
  const files = walkControllers(CONTROLLER_ROOT)
  const offenders = []

  for (const file of files) {
    const base = path.basename(file)
    if (ALLOWED_CONTROLLERS.has(base)) continue

    const content = fs.readFileSync(file, 'utf8')
    const rel = path.relative(CONTROLLER_ROOT, file).replace(/\\/g, '/')

    for (const imp of COMPAT_ENTITY_IMPORTS) {
      if (content.includes(`import ${imp}`)) {
        offenders.push(`${rel}: import ${imp.split('.').pop()}`)
      }
    }
    for (const table of COMPAT_TABLE_NAMES) {
      if (content.includes(`@TableName("${table}")`) || content.includes(`@TableName("xinketang.${table}")`)) {
        offenders.push(`${rel}: @TableName(${table})`)
      }
    }
  }

  if (offenders.length > 0) {
    console.error('[FAIL] Phase3K compat-controller gate: new direct COMPAT entity usage in Controller:')
    for (const o of offenders) console.error(`  - ${o}`)
    process.exit(1)
  }

  console.log(
    `[PASS] Phase3K compat-controller gate: no new COMPAT entity in Controller (baseline=${ALLOWED_CONTROLLERS.size}, scanned=${files.length})`,
  )
}

main()
