-- ============================================================
-- 54 管理端资源治理 Phase 3（主表 oss_primary_chinese_resource 扩展）
-- mysql -u root -p xinketang --default-character-set=utf8mb4
-- source sql/54_admin_resource_phase3.sql
-- ============================================================
USE xinketang;
SET NAMES utf8mb4;

SET @db = DATABASE();

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'oss_primary_chinese_resource' AND COLUMN_NAME = 'is_recommend') = 0,
  'ALTER TABLE `oss_primary_chinese_resource` ADD COLUMN `is_recommend` TINYINT NOT NULL DEFAULT 0 COMMENT ''是否推荐''',
  'SELECT 1');
PREPARE _s FROM @sql; EXECUTE _s; DEALLOCATE PREPARE _s;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'oss_primary_chinese_resource' AND COLUMN_NAME = 'is_top') = 0,
  'ALTER TABLE `oss_primary_chinese_resource` ADD COLUMN `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT ''是否置顶''',
  'SELECT 1');
PREPARE _s FROM @sql; EXECUTE _s; DEALLOCATE PREPARE _s;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'oss_primary_chinese_resource' AND COLUMN_NAME = 'top_sort') = 0,
  'ALTER TABLE `oss_primary_chinese_resource` ADD COLUMN `top_sort` INT NOT NULL DEFAULT 0 COMMENT ''置顶排序''',
  'SELECT 1');
PREPARE _s FROM @sql; EXECUTE _s; DEALLOCATE PREPARE _s;

SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'oss_primary_chinese_resource' AND COLUMN_NAME = 'is_free') = 0,
  'ALTER TABLE `oss_primary_chinese_resource` ADD COLUMN `is_free` TINYINT NOT NULL DEFAULT 1 COMMENT ''1免费 0付费''',
  'SELECT 1');
PREPARE _s FROM @sql; EXECUTE _s; DEALLOCATE PREPARE _s;

-- 索引（可选，提升管理端筛选）
SET @sql = IF(
  (SELECT COUNT(*) FROM information_schema.STATISTICS
   WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'oss_primary_chinese_resource' AND INDEX_NAME = 'idx_pcr_admin_status') = 0,
  'ALTER TABLE `oss_primary_chinese_resource` ADD KEY `idx_pcr_admin_status` (`status`, `upload_time`)',
  'SELECT 1');
PREPARE _s FROM @sql; EXECUTE _s; DEALLOCATE PREPARE _s;

-- 按钮权限：推荐 / 置顶 / 批量
INSERT INTO `sys_permission` (`id`, `code`, `name`, `type`, `module`, `parent_id`, `sort`, `status`) VALUES
(28, 'admin:resource:recommend', '推荐资源',     'button', 'resource', 2, 27, 1),
(29, 'admin:resource:top',       '置顶资源',     'button', 'resource', 2, 28, 1),
(30, 'admin:resource:batch',     '批量操作',     'button', 'resource', 2, 29, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`), `type` = VALUES(`type`), `module` = VALUES(`module`), `sort` = VALUES(`sort`);

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `sys_permission` WHERE id IN (28, 29, 30);

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(2, 28), (2, 29), (2, 30);

SELECT '=== oss_primary_chinese_resource 新列 ===' AS section;
SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'oss_primary_chinese_resource'
  AND COLUMN_NAME IN ('is_recommend', 'is_top', 'top_sort', 'is_free');
