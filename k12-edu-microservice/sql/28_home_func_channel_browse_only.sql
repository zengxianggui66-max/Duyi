-- ============================================================
-- 【单独执行】首页顶栏 P4：资源浏览跳转字段
-- 依赖：27_home_func_channel_only.sql
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

ALTER TABLE `home_func_channel`
  ADD COLUMN `browse_module` VARCHAR(64) DEFAULT NULL COMMENT '资源浏览栏目 module' AFTER `exam_tab_label`,
  ADD COLUMN `browse_stage_key` VARCHAR(20) DEFAULT NULL COMMENT '资源浏览学段' AFTER `browse_module`,
  ADD COLUMN `browse_default_volume` VARCHAR(30) DEFAULT NULL COMMENT '资源浏览默认册别名' AFTER `browse_stage_key`;

UPDATE `home_func_channel` SET
  `browse_module` = '拼音识字',
  `browse_stage_key` = 'preschool',
  `browse_default_volume` = '大班下学期'
WHERE `func_key` = 'youxiao';

UPDATE `home_func_channel` SET
  `browse_module` = '小升初真题',
  `browse_stage_key` = 'primary',
  `browse_default_volume` = '六年级下册'
WHERE `func_key` = 'xiaoshengchu';

UPDATE `home_func_channel` SET
  `browse_module` = '中考真题',
  `browse_stage_key` = 'junior',
  `browse_default_volume` = '九年级下册'
WHERE `func_key` = 'zhongkao';

UPDATE `home_func_channel` SET
  `browse_module` = '高考真题',
  `browse_stage_key` = 'senior',
  `browse_default_volume` = '选择性必修二'
WHERE `func_key` = 'gaokao';

UPDATE `home_func_channel` SET
  `browse_module` = '学业水平',
  `browse_stage_key` = 'junior',
  `browse_default_volume` = '九年级下册'
WHERE `func_key` = 'duikou';

SELECT func_key, browse_module, browse_stage_key, browse_default_volume
FROM home_func_channel
ORDER BY sort;
