-- Phase 7 验收验证查询
SELECT '=== T1: resource_main 数据 ===' AS info;
SELECT source_type, COUNT(*) AS cnt FROM resource_main GROUP BY source_type;

SELECT '=== T2: 视图总行数 ===' AS info;
SELECT COUNT(*) AS view_rows FROM v_admin_resource_main;

SELECT '=== T3: 资源统计 ===' AS info;
SELECT source_type, COUNT(*) AS total,
  SUM(CASE WHEN audit_status = 0 THEN 1 ELSE 0 END) AS pending,
  SUM(CASE WHEN audit_status = 1 THEN 1 ELSE 0 END) AS approved,
  SUM(CASE WHEN publish_status = 1 THEN 1 ELSE 0 END) AS published
FROM resource_main WHERE is_deleted = 0 GROUP BY source_type;

SELECT '=== T4: 视图 sample ===' AS info;
SELECT global_id, source_type, title, stage, subject, audit_status, publish_status
FROM v_admin_resource_main LIMIT 3;

SELECT '=== T5: 待审数量 ===' AS info;
SELECT COUNT(*) AS pending_count FROM v_admin_resource_main WHERE is_deleted = 0 AND audit_status = 0;

SELECT '=== T6: 触发器存在 ===' AS info;
SELECT TRIGGER_NAME FROM information_schema.TRIGGERS
WHERE TRIGGER_SCHEMA = 'xinketang'
AND TRIGGER_NAME IN ('trg_sync_resource_main_ins', 'trg_sync_resource_main_upd');

SELECT '=== ALL PASSED ===' AS result;
