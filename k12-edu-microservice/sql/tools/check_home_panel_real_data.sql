-- ============================================================
-- 首页专区真实数据体检脚本（资源可跳转专项）
-- 目标：
-- 1) 校验首页三大专区是否命中真实资源（非伪数据）
-- 2) 校验资源主表与维度表是否完整（影响前端筛选与跳转）
-- 3) 给出可直接用于 /resource/{id} 的候选数据
--
-- 执行：
-- mysql -u root -p xinketang < sql/tools/check_home_panel_real_data.sql
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

-- ============================================================
-- A. 首页配置完整性
-- ============================================================

-- A1: 首页 tab 配置总览（应有 sync_prep/paper_zone/promotion）
SELECT
  panel_code,
  tab_key,
  COALESCE(filter_key, '(null)') AS filter_key,
  query_mode,
  status,
  sort
FROM home_panel_tab_config
ORDER BY panel_code, tab_key, filter_key, sort;

-- A2: 配置中出现 suite 模式的 tab（用于确认是否仍需要成套）
SELECT
  panel_code,
  tab_key,
  COALESCE(filter_key, '(null)') AS filter_key,
  query_mode
FROM home_panel_tab_config
WHERE status = 1
  AND query_mode = 'suite'
ORDER BY panel_code, tab_key, filter_key;

-- A3: 置顶位数据源分布（建议均为可追溯 source + 真实 id）
SELECT
  resource_source,
  COUNT(*) AS cnt
FROM home_panel_featured
WHERE status = 1
GROUP BY resource_source
ORDER BY cnt DESC;

-- ============================================================
-- B. 置顶位可达性检查（前端点击依赖 id/detailPath）
-- ============================================================

-- B1: 置顶位关联不到底表记录（会导致点击失败）
SELECT
  f.id AS featured_id,
  f.panel_code,
  f.tab_key,
  f.filter_key,
  f.stage_key,
  f.subject_name,
  f.resource_source,
  f.resource_id
FROM home_panel_featured f
LEFT JOIN edu_resource er
  ON f.resource_source = 'edu_resource'
 AND f.resource_id = er.id
 AND er.is_deleted = 0
 AND er.status = 1
LEFT JOIN oss_primary_chinese_resource oss
  ON f.resource_source = 'oss_primary_chinese'
 AND f.resource_id = oss.id
 AND oss.is_deleted = 0
 AND oss.status = 1
LEFT JOIN edu_resource_suite s
  ON f.resource_source = 'edu_resource_suite'
 AND f.resource_id = s.id
 AND s.status = 1
WHERE f.status = 1
  AND (
    (f.resource_source = 'edu_resource' AND er.id IS NULL)
    OR (f.resource_source = 'oss_primary_chinese' AND oss.id IS NULL)
    OR (f.resource_source = 'edu_resource_suite' AND s.id IS NULL)
  )
ORDER BY f.panel_code, f.tab_key, f.id;

-- ============================================================
-- C. edu_resource 主链路数据质量（影响首页 + 详情页）
-- ============================================================

-- C1: 主表状态异常（理论上首页只应命中 status=1 且 is_deleted=0）
SELECT
  COUNT(*) AS abnormal_count
FROM edu_resource
WHERE status <> 1 OR is_deleted <> 0;

-- C1-DETAIL: 状态异常明细（用于精准修复）
SELECT
  id,
  title,
  status,
  is_deleted,
  DATE_FORMAT(upload_time, '%Y-%m-%d %H:%i:%s') AS upload_time,
  DATE_FORMAT(publish_time, '%Y-%m-%d %H:%i:%s') AS publish_time
FROM edu_resource
WHERE status <> 1 OR is_deleted <> 0
ORDER BY upload_time DESC, id DESC
LIMIT 200;

-- C2: 缺失维度行（resource 有记录但 dimension 无关联）
SELECT
  r.id,
  r.title,
  r.upload_time
FROM edu_resource r
LEFT JOIN edu_resource_dimension rd ON rd.resource_id = r.id
WHERE r.status = 1
  AND r.is_deleted = 0
  AND rd.resource_id IS NULL
ORDER BY r.upload_time DESC, r.id DESC
LIMIT 200;

-- C3: 维度关键字段缺失（学段/学科/栏目/资源类型）
SELECT
  rd.resource_id,
  r.title,
  rd.stage_id,
  rd.subject_id,
  rd.module_id,
  rd.resource_type_id,
  rd.grade_id,
  rd.volume_id
FROM edu_resource_dimension rd
JOIN edu_resource r ON r.id = rd.resource_id
WHERE r.status = 1
  AND r.is_deleted = 0
  AND (
    rd.stage_id IS NULL
    OR rd.subject_id IS NULL
    OR rd.module_id IS NULL
    OR rd.resource_type_id IS NULL
  )
ORDER BY rd.resource_id DESC
LIMIT 200;

-- C4: 维度外键无效（挂了不存在的维度 id）
SELECT
  rd.resource_id,
  r.title,
  rd.stage_id,
  rd.subject_id,
  rd.module_id,
  rd.resource_type_id
FROM edu_resource_dimension rd
JOIN edu_resource r ON r.id = rd.resource_id
LEFT JOIN edu_stage s ON s.id = rd.stage_id
LEFT JOIN edu_subject sub ON sub.id = rd.subject_id
LEFT JOIN edu_module m ON m.id = rd.module_id
LEFT JOIN edu_resource_type rt ON rt.id = rd.resource_type_id
WHERE r.status = 1
  AND r.is_deleted = 0
  AND (
    s.id IS NULL
    OR sub.id IS NULL
    OR m.id IS NULL
    OR rt.id IS NULL
  )
ORDER BY rd.resource_id DESC
LIMIT 200;

-- ============================================================
-- D. 首页“真实可跳转”候选抽样（直接拼 /resource/{id}）
-- ============================================================

-- D1: 同步备课候选（最近 30 条）
SELECT
  r.id,
  r.title,
  DATE_FORMAT(r.upload_time, '%Y-%m-%d') AS upload_date,
  s.name AS stage_name,
  sub.name AS subject_name,
  m.name AS module_name,
  rt.name AS resource_type,
  CONCAT('/resource/', r.id) AS detail_path
FROM edu_resource r
JOIN edu_resource_dimension rd ON rd.resource_id = r.id
LEFT JOIN edu_stage s ON s.id = rd.stage_id
LEFT JOIN edu_subject sub ON sub.id = rd.subject_id
LEFT JOIN edu_module m ON m.id = rd.module_id
LEFT JOIN edu_resource_type rt ON rt.id = rd.resource_type_id
WHERE r.status = 1
  AND r.is_deleted = 0
  AND m.name = '同步备课'
ORDER BY r.upload_time DESC, r.id DESC
LIMIT 30;

-- D2: 试卷专区候选（最近 30 条）
SELECT
  r.id,
  r.title,
  DATE_FORMAT(r.upload_time, '%Y-%m-%d') AS upload_date,
  s.name AS stage_name,
  sub.name AS subject_name,
  m.name AS module_name,
  rt.name AS resource_type,
  CONCAT('/resource/', r.id) AS detail_path
FROM edu_resource r
JOIN edu_resource_dimension rd ON rd.resource_id = r.id
LEFT JOIN edu_stage s ON s.id = rd.stage_id
LEFT JOIN edu_subject sub ON sub.id = rd.subject_id
LEFT JOIN edu_module m ON m.id = rd.module_id
LEFT JOIN edu_resource_type rt ON rt.id = rd.resource_type_id
WHERE r.status = 1
  AND r.is_deleted = 0
  AND m.name IN ('期中', '期末', '月考', '开学专区', '寒假', '暑假', '小升初真题', '小升初模拟', '中考真题', '中考模拟', '高考真题', '高考模拟')
ORDER BY r.upload_time DESC, r.id DESC
LIMIT 30;

-- D3: 升学专区候选（exam_scene 维度）
SELECT
  r.id,
  r.title,
  DATE_FORMAT(r.upload_time, '%Y-%m-%d') AS upload_date,
  s.name AS stage_name,
  sub.name AS subject_name,
  ex.exam_level,
  m.name AS module_name,
  CONCAT('/resource/', r.id) AS detail_path
FROM edu_resource r
JOIN edu_resource_dimension rd ON rd.resource_id = r.id
LEFT JOIN edu_stage s ON s.id = rd.stage_id
LEFT JOIN edu_subject sub ON sub.id = rd.subject_id
LEFT JOIN edu_module m ON m.id = rd.module_id
LEFT JOIN edu_exam_scene ex ON ex.id = rd.exam_scene_id
WHERE r.status = 1
  AND r.is_deleted = 0
  AND ex.exam_level IN ('xsc', 'zk', 'gk', 'mock', 'real_paper')
ORDER BY r.upload_time DESC, r.id DESC
LIMIT 30;

-- ============================================================
-- E. 快速结论指标（给验收/排障看）
-- ============================================================
SELECT
  (SELECT COUNT(*) FROM home_panel_tab_config WHERE status = 1) AS active_tab_configs,
  (SELECT COUNT(*) FROM home_panel_featured WHERE status = 1) AS active_featured_rows,
  (SELECT COUNT(*) FROM edu_resource WHERE status = 1 AND is_deleted = 0) AS active_edu_resources,
  (SELECT COUNT(*) FROM edu_resource_dimension) AS total_dimension_rows,
  (SELECT COUNT(*)
   FROM edu_resource r
   LEFT JOIN edu_resource_dimension rd ON rd.resource_id = r.id
   WHERE r.status = 1 AND r.is_deleted = 0 AND rd.resource_id IS NULL) AS missing_dimension_rows;

SELECT 'check_home_panel_real_data.sql 执行完成' AS message;
