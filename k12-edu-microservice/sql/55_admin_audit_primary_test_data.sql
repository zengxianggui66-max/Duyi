-- ============================================================
-- 55 管理端审核联调测试数据（主表 oss_primary_chinese_resource）
-- 前置：sql/54
-- mysql -u root -p xinketang --default-character-set=utf8mb4
-- source sql/55_admin_audit_primary_test_data.sql
-- ============================================================
USE xinketang;
SET NAMES utf8mb4;

INSERT INTO `oss_primary_chinese_resource` (
  `stage`, `subject`, `module`, `type`, `grade_name`, `edition`,
  `unit_name`, `lesson_name`, `title`, `original_filename`, `file_ext`,
  `oss_bucket`, `oss_object_key`, `oss_url`, `file_size_kb`,
  `status`, `uploader_id`, `is_deleted`, `remark`
)
SELECT * FROM (
  SELECT '小学' AS stage, '语文' AS subject, '同步备课' AS module, '课件' AS type,
         '一年级上册' AS grade_name, '人教版' AS edition,
         '第一单元' AS unit_name, '春夏秋冬' AS lesson_name,
         '【待审】小学语文第一单元课件' AS title, 'pending-demo-1.pptx' AS original_filename, 'pptx' AS file_ext,
         'qier-duuyi' AS oss_bucket, 'demo/pending-1.pptx' AS oss_object_key,
         'https://example.com/demo/pending-1.pptx' AS oss_url, 512 AS file_size_kb,
         0 AS status, 2 AS uploader_id, 0 AS is_deleted, '审核联调-主表待审数据' AS remark
  UNION ALL
  SELECT '小学', '数学', '同步备课', '试卷', '三年级上册', '人教版',
         '期中复习', NULL, '【待审】小学数学期中复习卷', 'pending-demo-2.pdf', 'pdf',
         'qier-duuyi', 'demo/pending-2.pdf', 'https://example.com/demo/pending-2.pdf', 256,
         0, 2, 0, '审核联调-主表待审数据'
  UNION ALL
  SELECT '初中', '英语', '同步备课', '音频', '七年级上册', '外研版',
         'Starter Unit', NULL, '【待审】初中英语听力材料', 'pending-demo-3.mp3', 'mp3',
         'qier-duuyi', 'demo/pending-3.mp3', 'https://example.com/demo/pending-3.mp3', 128,
         0, 2, 0, '审核联调-主表待审数据'
) AS seed
WHERE NOT EXISTS (
  SELECT 1 FROM `oss_primary_chinese_resource`
  WHERE `title` LIKE '【待审】%' AND `status` = 0 AND `is_deleted` = 0
  LIMIT 1
);

SELECT '=== 主表待审资源 ===' AS section;
SELECT id, title, subject, type, stage, status, uploader_id, upload_time
FROM `oss_primary_chinese_resource`
WHERE `status` = 0 AND `is_deleted` = 0
ORDER BY id DESC
LIMIT 10;
