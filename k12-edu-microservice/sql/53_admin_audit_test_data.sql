-- ============================================================
-- 53 管理端审核联调测试数据（待审资源）
-- 前置：sql/08 或已有 resource 表
-- mysql -u root -p xinketang --default-character-set=utf8mb4
-- source sql/53_admin_audit_test_data.sql
-- ============================================================
USE xinketang;
SET NAMES utf8mb4;

INSERT INTO `resource` (
  `title`, `description`, `grade_level`, `subject`, `grade`,
  `resource_type`, `author_id`, `author_name`, `status`, `is_free`, `deleted`
) VALUES
('【待审】小学语文第一单元课件', '审核联调测试数据-课件', 'primary', 'chinese', '一年级', 'courseware', 2, 'teacher_demo', 0, 1, 0),
('【待审】小学数学期中复习卷', '审核联调测试数据-试卷', 'primary', 'math', '三年级', 'paper', 2, 'teacher_demo', 0, 1, 0),
('【待审】初中英语听力材料', '审核联调测试数据-音频', 'junior', 'english', '七年级', 'audio', 2, 'teacher_demo', 0, 0, 0);

SELECT '=== 待审资源 ===' AS section;
SELECT id, title, subject, resource_type, author_name, status, create_time
FROM `resource`
WHERE `status` = 0 AND `deleted` = 0
ORDER BY id DESC
LIMIT 10;
