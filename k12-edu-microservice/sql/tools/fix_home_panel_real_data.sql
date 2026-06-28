-- ============================================================
-- 首页专区真实数据修复模板（安全版）
-- 说明：
-- 1) 先运行 check_home_panel_real_data.sql，确认异常 id
-- 2) 本脚本默认仅给出“可回滚、可审计”的修复模板
-- 3) 执行前请先备份目标行
--
-- 执行：
-- mysql -u root -p xinketang < sql/tools/fix_home_panel_real_data.sql
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

-- ============================================================
-- 0) 你需要先填这两个参数（来自 C1-DETAIL 查询结果）
--    例：SET @fix_id_1 = 12345; SET @fix_id_2 = 12346;
-- ============================================================
SET @fix_id_1 = 20002;
SET @fix_id_2 = 20001;

-- ============================================================
-- 1) 修复前快照（审计）
-- ============================================================
SELECT 'BEFORE' AS phase, r.*
FROM edu_resource r
WHERE r.id IN (@fix_id_1, @fix_id_2);

-- ============================================================
-- 2) 仅将误标资源恢复为可用（最小改动）
--    - status = 1
--    - is_deleted = 0
-- ============================================================
UPDATE edu_resource
SET
  status = 1,
  is_deleted = 0
WHERE id IN (@fix_id_1, @fix_id_2)
  AND (@fix_id_1 IS NOT NULL OR @fix_id_2 IS NOT NULL);

-- ============================================================
-- 3) 修复后快照
-- ============================================================
SELECT 'AFTER' AS phase, r.*
FROM edu_resource r
WHERE r.id IN (@fix_id_1, @fix_id_2);

-- ============================================================
-- 4) 回归验证（关键指标）
-- ============================================================
SELECT
  (SELECT COUNT(*) FROM edu_resource WHERE status <> 1 OR is_deleted <> 0) AS abnormal_count_after_fix,
  (SELECT COUNT(*)
   FROM edu_resource r
   LEFT JOIN edu_resource_dimension rd ON rd.resource_id = r.id
   WHERE r.status = 1 AND r.is_deleted = 0 AND rd.resource_id IS NULL) AS missing_dimension_rows_after_fix;

SELECT 'fix_home_panel_real_data.sql 执行完成（请确认你已填写 @fix_id_1/@fix_id_2）' AS message;
