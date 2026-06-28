-- ============================================================
-- OSS 入库：一年级上册语文 · 我上学了 · 2 我是小学生 · 课件+教案
-- 桶：xinketangdu（深圳）
-- OSS 路径：0.我上学了/2 我是小学生/课件+教案/
-- 依赖：34_seed_catalog_primary_2023_full.sql（目录节点 y1s1_语文_u01_l02）
-- 执行：mysql -u root -p xinketang < sql/tools/seed_oss_y1s1_woshixiaoxuesheng.sql
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

-- ============================================================
-- 1. 查找目录节点：y1s1_语文_u01_l02 → 我是小学生
-- ============================================================
SET @catalog_node_id = (
  SELECT `id`
  FROM `edu_catalog_node`
  WHERE `code` = 'y1s1_语文_u01_l02'
    AND `name` = '我是小学生'
    AND `status` = 1
  LIMIT 1
);

-- OSS 公共参数
SET @oss_bucket = 'xinketangdu';
SET @oss_base_url = 'https://xinketangdu.oss-cn-shenzhen.aliyuncs.com';
SET @catalog_path = '0.我上学了/2 我是小学生';
SET @brand_code = 'qicai';

-- ============================================================
-- 2. 幂等清理（按 oss_object_key 精确匹配）
-- ============================================================
DELETE p
FROM `edu_resource_placement` p
INNER JOIN `oss_primary_chinese_resource` r ON r.`id` = p.`resource_id`
WHERE r.`oss_object_key` IN (
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生 优质教案.doc',
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生【优质版】.pptx',
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生【新课标版】.pptx',
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生【精华版】.pptx'
);

DELETE FROM `oss_primary_chinese_resource`
WHERE `oss_object_key` IN (
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生 优质教案.doc',
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生【优质版】.pptx',
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生【新课标版】.pptx',
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生【精华版】.pptx'
);

-- ============================================================
-- 3. 插入 4 条资源记录
-- ============================================================
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
) VALUES
-- ------------------------------------------------------------------
-- 资源 1：优质教案 .doc
-- ------------------------------------------------------------------
(
  '小学', '语文', '同步备课', '教案', NULL,
  '一年级上册', '统编版(2024)', @brand_code,
  @catalog_node_id, @catalog_path,
  '我上学了', '我是小学生',
  '2 我是小学生 优质教案',
  '2 我是小学生 优质教案.doc',
  'doc',
  @oss_bucket,
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生 优质教案.doc',
  CONCAT(@oss_base_url, '/0.%E6%88%91%E4%B8%8A%E5%AD%A6%E4%BA%86/2%20%E6%88%91%E6%98%AF%E5%B0%8F%E5%AD%A6%E7%94%9F/%E8%AF%BE%E4%BB%B6%2B%E6%95%99%E6%A1%88/2%20%E6%88%91%E6%98%AF%E5%B0%8F%E5%AD%A6%E7%94%9F%20%E4%BC%98%E8%B4%A8%E6%95%99%E6%A1%88.doc'),
  0, 0, 0,
  1, 1, 10,
  '一年级上册语文 · 我上学了 · 我是小学生 · 同步备课优质教案',
  '2026-05-27 18:00:00',
  0
),
-- ------------------------------------------------------------------
-- 资源 2：课件【优质版】.pptx
-- ------------------------------------------------------------------
(
  '小学', '语文', '同步备课', '课件', '优质版',
  '一年级上册', '统编版(2024)', @brand_code,
  @catalog_node_id, @catalog_path,
  '我上学了', '我是小学生',
  '2 我是小学生【优质版】',
  '2 我是小学生【优质版】.pptx',
  'pptx',
  @oss_bucket,
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生【优质版】.pptx',
  CONCAT(@oss_base_url, '/0.%E6%88%91%E4%B8%8A%E5%AD%A6%E4%BA%86/2%20%E6%88%91%E6%98%AF%E5%B0%8F%E5%AD%A6%E7%94%9F/%E8%AF%BE%E4%BB%B6%2B%E6%95%99%E6%A1%88/2%20%E6%88%91%E6%98%AF%E5%B0%8F%E5%AD%A6%E7%94%9F%E3%80%90%E4%BC%98%E8%B4%A8%E7%89%88%E3%80%91.pptx'),
  0, 0, 0,
  1, 1, 20,
  '一年级上册语文 · 我上学了 · 我是小学生 · 同步备课课件（优质版）',
  '2026-05-27 18:00:00',
  0
),
-- ------------------------------------------------------------------
-- 资源 3：课件【新课标版】.pptx
-- ------------------------------------------------------------------
(
  '小学', '语文', '同步备课', '课件', '新课标版',
  '一年级上册', '统编版(2024)', @brand_code,
  @catalog_node_id, @catalog_path,
  '我上学了', '我是小学生',
  '2 我是小学生【新课标版】',
  '2 我是小学生【新课标版】.pptx',
  'pptx',
  @oss_bucket,
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生【新课标版】.pptx',
  CONCAT(@oss_base_url, '/0.%E6%88%91%E4%B8%8A%E5%AD%A6%E4%BA%86/2%20%E6%88%91%E6%98%AF%E5%B0%8F%E5%AD%A6%E7%94%9F/%E8%AF%BE%E4%BB%B6%2B%E6%95%99%E6%A1%88/2%20%E6%88%91%E6%98%AF%E5%B0%8F%E5%AD%A6%E7%94%9F%E3%80%90%E6%96%B0%E8%AF%BE%E6%A0%87%E7%89%88%E3%80%91.pptx'),
  0, 0, 0,
  1, 1, 30,
  '一年级上册语文 · 我上学了 · 我是小学生 · 同步备课课件（新课标版）',
  '2026-05-27 18:00:00',
  0
),
-- ------------------------------------------------------------------
-- 资源 4：课件【精华版】.pptx
-- ------------------------------------------------------------------
(
  '小学', '语文', '同步备课', '课件', '精华版',
  '一年级上册', '统编版(2024)', @brand_code,
  @catalog_node_id, @catalog_path,
  '我上学了', '我是小学生',
  '2 我是小学生【精华版】',
  '2 我是小学生【精华版】.pptx',
  'pptx',
  @oss_bucket,
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生【精华版】.pptx',
  CONCAT(@oss_base_url, '/0.%E6%88%91%E4%B8%8A%E5%AD%A6%E4%BA%86/2%20%E6%88%91%E6%98%AF%E5%B0%8F%E5%AD%A6%E7%94%9F/%E8%AF%BE%E4%BB%B6%2B%E6%95%99%E6%A1%88/2%20%E6%88%91%E6%98%AF%E5%B0%8F%E5%AD%A6%E7%94%9F%E3%80%90%E7%B2%BE%E5%8D%8E%E7%89%88%E3%80%91.pptx'),
  0, 0, 0,
  1, 1, 40,
  '一年级上册语文 · 我上学了 · 我是小学生 · 同步备课课件（精华版）',
  '2026-05-27 18:00:00',
  0
);

-- ============================================================
-- 4. 为每条资源建立目录挂载（edu_resource_placement）
-- ============================================================
INSERT INTO `edu_resource_placement` (`resource_id`, `catalog_node_id`, `is_primary`, `sort`)
SELECT r.`id`, @catalog_node_id, 1, r.`sort`
FROM `oss_primary_chinese_resource` r
WHERE r.`oss_object_key` IN (
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生 优质教案.doc',
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生【优质版】.pptx',
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生【新课标版】.pptx',
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生【精华版】.pptx'
) AND @catalog_node_id IS NOT NULL;

-- ============================================================
-- 5. 校验结果
-- ============================================================
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
  r.`original_filename`,
  r.`file_ext`,
  r.`oss_url`,
  r.`file_size_kb`,
  (SELECT COUNT(*) FROM `edu_resource_placement` p WHERE p.`resource_id` = r.`id`) AS placement_cnt
FROM `oss_primary_chinese_resource` r
WHERE r.`oss_object_key` IN (
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生 优质教案.doc',
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生【优质版】.pptx',
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生【新课标版】.pptx',
  '0.我上学了/2 我是小学生/课件+教案/2 我是小学生【精华版】.pptx'
)
ORDER BY r.`sort`;
