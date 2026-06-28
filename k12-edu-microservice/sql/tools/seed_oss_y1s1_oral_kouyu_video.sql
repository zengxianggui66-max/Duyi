-- ============================================================
-- OSS 入库：一年级上册语文 · 口语交际：我说你做 · 微课视频
-- 桶：xinketangdu（深圳）
-- 路径：1.第一单元/口语交际：我说你做/教学视频/微课视频/...
-- 执行：mysql -u root -p xinketang < sql/tools/seed_oss_y1s1_oral_kouyu_video.sql
-- 依赖：34_seed_catalog_primary_2023_full.sql（目录节点 y1s1_语文_u02_l06）
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

SET @oss_bucket = 'xinketangdu';
SET @oss_object_key = '1.第一单元/口语交际：我说你做/教学视频/微课视频/【微课视频】口语交际：我说你做.mp4';
SET @oss_url = 'https://xinketangdu.oss-cn-shenzhen.aliyuncs.com/1.%E7%AC%AC%E4%B8%80%E5%8D%95%E5%85%83/%E5%8F%A3%E8%AF%AD%E4%BA%A4%E9%99%85%EF%BC%9A%E6%88%91%E8%AF%B4%E4%BD%A0%E5%81%9A/%E6%95%99%E5%AD%A6%E8%A7%86%E9%A2%91/%E5%BE%AE%E8%AF%BE%E8%A7%86%E9%A1%91/%E3%80%90%E5%BE%AE%E8%AF%BE%E8%A7%86%E9%A1%91%E3%80%91%E5%8F%A3%E8%AF%AD%E4%BA%A4%E9%99%85%EF%BC%9A%E6%88%91%E8%AF%B4%E4%BD%A0%E5%81%9A.mp4';

-- 目录节点：第一单元：识字 / 口语交际：我说你做
SET @catalog_node_id = (
  SELECT `id`
  FROM `edu_catalog_node`
  WHERE `code` = 'y1s1_语文_u02_l06'
    AND `name` = '口语交际：我说你做'
    AND `status` = 1
  LIMIT 1
);

-- 幂等：同 object_key 先删挂载再删宽表
DELETE p
FROM `edu_resource_placement` p
INNER JOIN `oss_primary_chinese_resource` r ON r.`id` = p.`resource_id`
WHERE r.`oss_object_key` = @oss_object_key;

DELETE FROM `oss_primary_chinese_resource`
WHERE `oss_object_key` = @oss_object_key;

INSERT INTO `oss_primary_chinese_resource` (
  `stage`, `subject`, `module`, `type`, `sub_type`,
  `grade_name`, `edition`, `brand_code`,
  `catalog_node_id`, `catalog_path`,
  `unit_name`, `lesson_name`,
  `title`, `original_filename`, `file_ext`,
  `oss_bucket`, `oss_object_key`, `oss_url`,
  `file_size_kb`, `download_count`, `view_count`,
  `status`, `allow_preview`, `sort`, `remark`,
  `upload_time`, `is_deleted`
) VALUES (
  '小学', '语文', '同步备课', '视频', '微课视频',
  '一年级上册', '统编版(2024)', 'qicai',
  @catalog_node_id,
  '1.第一单元/口语交际：我说你做',
  '第一单元：识字', '口语交际：我说你做',
  '【微课视频】口语交际：我说你做',
  '【微课视频】口语交际：我说你做.mp4',
  'mp4',
  @oss_bucket, @oss_object_key, @oss_url,
  8661, 0, 0,
  1, 1, 10,
  '一年级上册语文 · 第一单元：识字 · 口语交际：我说你做 · 同步备课微课视频',
  '2026-05-24 15:20:29',
  0
);

SET @resource_id = LAST_INSERT_ID();

INSERT INTO `edu_resource_placement` (`resource_id`, `catalog_node_id`, `is_primary`, `sort`)
SELECT @resource_id, @catalog_node_id, 1, 10
WHERE @catalog_node_id IS NOT NULL;

-- 校验
SELECT
  r.`id`,
  r.`type`,
  r.`sub_type`,
  r.`grade_name`,
  r.`edition`,
  r.`unit_name`,
  r.`lesson_name`,
  r.`catalog_node_id`,
  r.`title`,
  r.`oss_url`,
  r.`file_size_kb`,
  (SELECT COUNT(*) FROM `edu_resource_placement` p WHERE p.`resource_id` = r.`id`) AS placement_cnt
FROM `oss_primary_chinese_resource` r
WHERE r.`oss_object_key` = @oss_object_key;
