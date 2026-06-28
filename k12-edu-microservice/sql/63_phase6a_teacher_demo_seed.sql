-- Phase 6-A 验收种子：教师 demo 用户 + 3 上传 + 5 收藏
-- 前置：sql/62_admin_user_phase6a.sql
-- mysql -u root -p xinketang --default-character-set=utf8mb4 < sql/63_phase6a_teacher_demo_seed.sql
USE `xinketang`;
SET NAMES utf8mb4;

-- teacher123 与 admin123 相同 BCrypt
SET @pwd := '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi';

INSERT INTO `user` (`username`, `password`, `nickname`, `phone`, `role`, `member_level`, `status`, `deleted`)
SELECT 'teacher_demo', @pwd, '演示教师', '13800138001', 'teacher', 0, 1, 0
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'teacher_demo');

SET @teacher_id := (SELECT id FROM `user` WHERE `username` = 'teacher_demo' LIMIT 1);

-- 绑定微信 OAuth（只读展示）
INSERT INTO `user_oauth_bind` (`user_id`, `oauth_type`, `oauth_id`, `nickname`, `avatar`)
SELECT @teacher_id, 'wechat', 'wx_demo_openid_001', '演示教师', ''
FROM DUAL
WHERE @teacher_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `user_oauth_bind` WHERE `user_id` = @teacher_id AND `oauth_type` = 'wechat'
  );

-- 将 3 条已有小学语文资源归属该教师
UPDATE `oss_primary_chinese_resource` r
JOIN (
  SELECT id FROM `oss_primary_chinese_resource`
  WHERE is_deleted = 0
  ORDER BY id ASC
  LIMIT 3
) t ON r.id = t.id
SET r.uploader_id = @teacher_id
WHERE @teacher_id IS NOT NULL;

-- 收藏 5 条不同资源（primary_chinese）
INSERT IGNORE INTO `collection` (`user_id`, `resource_id`, `resource_type`)
SELECT @teacher_id, r.id, 'primary_chinese'
FROM `oss_primary_chinese_resource` r
WHERE r.is_deleted = 0
  AND @teacher_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `collection` c
    WHERE c.user_id = @teacher_id AND c.resource_id = r.id AND c.resource_type = 'primary_chinese'
  )
ORDER BY r.id ASC
LIMIT 5;

SELECT '=== Phase 6-A demo 教师 ===' AS section;
SELECT id, username, nickname, role, phone FROM `user` WHERE username = 'teacher_demo';

SELECT '=== 上传统计 ===' AS section;
SELECT COUNT(*) AS upload_count FROM `oss_primary_chinese_resource`
WHERE uploader_id = @teacher_id AND is_deleted = 0;

SELECT '=== 收藏统计 ===' AS section;
SELECT COUNT(*) AS collection_count FROM `collection` WHERE user_id = @teacher_id;
