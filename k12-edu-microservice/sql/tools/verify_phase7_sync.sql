-- T7: 触发器同步验证 — 修改源表后 resource_main 自动同步
UPDATE oss_primary_chinese_resource SET audit_status = 0, publish_status = 0, status = 0 WHERE id = 1;

SELECT '=== 触发器同步后 resource_main ===' AS info;
SELECT rm.id AS global_id, rm.audit_status, rm.publish_status,
  r.audit_status AS src_audit, r.publish_status AS src_pub,
  CASE WHEN rm.audit_status = r.audit_status AND rm.publish_status = r.publish_status THEN 'SYNC_OK' ELSE 'SYNC_FAIL' END AS sync_check
FROM resource_main rm
INNER JOIN oss_primary_chinese_resource r ON r.id = rm.source_id AND rm.source_type='primary_chinese'
WHERE rm.source_id = 1;

-- 恢复
UPDATE oss_primary_chinese_resource SET audit_status = 0, publish_status = 0, status = 0 WHERE id = 1;
