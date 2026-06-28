-- ============================================================
-- 85：Phase 8 — 质量治理和运营统计
-- 创建敏感词库 / 预览失败队列 / 资源日统计 / 审核员工作量 / 低访问视图
-- 依赖：05 (oss_primary_chinese_resource) / 83 (resource_main) 已执行
-- 执行：mysql -u root -p xinketang < sql/85_phase8_quality_governance.sql
-- MySQL 账号：root  密码：zxg123456
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------
-- 1. sys_sensitive_word 敏感词库表
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `sys_sensitive_word`;

CREATE TABLE `sys_sensitive_word` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `word`        VARCHAR(100) NOT NULL COMMENT '敏感词',
  `category`    TINYINT      DEFAULT 0 COMMENT '分类: 0-通用 1-政治 2-色情 3-广告 4-暴力 5-其他',
  `level`       TINYINT      DEFAULT 1 COMMENT '严重级别: 1-警告 2-阻断',
  `status`      TINYINT      DEFAULT 1 COMMENT '1-启用 0-禁用',
  `remark`      VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`),
  KEY `idx_category` (`category`, `status`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词库';

-- ----------------------------------------------------------
-- 2. preview_fail_queue 预览失败队列表
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `preview_fail_queue`;

CREATE TABLE `preview_fail_queue` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `source_type`    VARCHAR(40)  NOT NULL COMMENT '资源来源类型: primary_chinese 等',
  `source_id`      BIGINT UNSIGNED NOT NULL COMMENT '源资源ID',
  `global_id`      BIGINT UNSIGNED DEFAULT NULL COMMENT 'resource_main.id',
  `title`          VARCHAR(200) DEFAULT NULL COMMENT '资源标题',
  `fail_reason`    VARCHAR(500) DEFAULT NULL COMMENT '失败原因摘要',
  `fail_count`     INT          DEFAULT 1 COMMENT '连续失败次数',
  `last_fail_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '最近失败时间',
  `status`         TINYINT      DEFAULT 0 COMMENT '0-待处理 1-已处理 2-已忽略',
  `handler_id`     BIGINT       DEFAULT NULL COMMENT '处理人ID',
  `handler_name`   VARCHAR(100) DEFAULT NULL COMMENT '处理人姓名',
  `handler_note`   VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
  `handler_time`   DATETIME     DEFAULT NULL COMMENT '处理时间',
  `create_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`, `last_fail_time`),
  KEY `idx_source` (`source_type`, `source_id`),
  KEY `idx_global_id` (`global_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预览失败队列';

-- ----------------------------------------------------------
-- 3. resource_daily_stats 资源日统计快照表
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `resource_daily_stats`;

CREATE TABLE `resource_daily_stats` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `stat_date`       DATE    NOT NULL COMMENT '统计日期',
  `source_type`     VARCHAR(40) NOT NULL DEFAULT 'ALL' COMMENT '资源来源: ALL=全部 / primary_chinese 等',
  `total_count`     INT     DEFAULT 0 COMMENT '资源总数',
  `new_count`       INT     DEFAULT 0 COMMENT '当日新增数',
  `pending_count`   INT     DEFAULT 0 COMMENT '待审数(截至当日)',
  `approved_count`  INT     DEFAULT 0 COMMENT '当日审核通过数',
  `rejected_count`  INT     DEFAULT 0 COMMENT '当日驳回数',
  `published_count` INT     DEFAULT 0 COMMENT '已上架数(截至当日)',
  `offline_count`   INT     DEFAULT 0 COMMENT '当日下架数',
  `deleted_count`   INT     DEFAULT 0 COMMENT '当日删除数',
  `download_count`  INT     DEFAULT 0 COMMENT '当日下载次数',
  `view_count`      INT     DEFAULT 0 COMMENT '当日浏览次数',
  `create_time`     DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_source` (`stat_date`, `source_type`),
  KEY `idx_stat_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源日统计快照';

-- ----------------------------------------------------------
-- 4. auditor_workload 审核员工作量日汇总表
-- ----------------------------------------------------------
DROP TABLE IF EXISTS `auditor_workload`;

CREATE TABLE `auditor_workload` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `stat_date`        DATE    NOT NULL COMMENT '统计日期',
  `auditor_id`       BIGINT  NOT NULL COMMENT '审核员用户ID',
  `auditor_name`     VARCHAR(100) DEFAULT NULL COMMENT '审核员姓名',
  `approve_count`    INT     DEFAULT 0 COMMENT '审核通过数',
  `reject_count`     INT     DEFAULT 0 COMMENT '驳回数',
  `total_count`      INT     DEFAULT 0 COMMENT '审核总数',
  `avg_duration_sec` INT     DEFAULT 0 COMMENT '平均审核时长(秒)',
  `max_duration_sec` INT     DEFAULT 0 COMMENT '最长审核时长(秒)',
  `overtime_count`   INT     DEFAULT 0 COMMENT '超时审核数(>24h)',
  `create_time`      DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_date_auditor` (`stat_date`, `auditor_id`),
  KEY `idx_stat_date` (`stat_date`),
  KEY `idx_auditor` (`auditor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核员工作量日汇总';

-- ----------------------------------------------------------
-- 5. v_low_access_resource 低访问资源视图
--    条件：已上架超过30天 且 (下载<3 或 浏览<10)
-- ----------------------------------------------------------
CREATE OR REPLACE VIEW `v_low_access_resource` AS
SELECT
  rm.id              AS global_id,
  rm.source_type,
  rm.source_id,
  rm.title,
  rm.stage_code      AS stage,
  rm.subject_code    AS subject,
  rm.uploader_id,
  rm.upload_time,
  r.download_count,
  r.view_count,
  r.publish_status,
  r.audit_status,
  DATEDIFF(NOW(), rm.upload_time)          AS days_since_upload,
  CASE
    WHEN IFNULL(r.download_count,0) = 0 AND IFNULL(r.view_count,0) = 0 THEN '零访问'
    WHEN IFNULL(r.view_count,0)  < 10 THEN '低浏览'
    WHEN IFNULL(r.download_count,0) < 3  THEN '低下载'
    ELSE '正常'
  END AS access_level
FROM `resource_main` rm
INNER JOIN `oss_primary_chinese_resource` r ON r.id = rm.source_id
WHERE rm.is_deleted = 0
  AND rm.publish_status = 1
  AND rm.upload_time < DATE_SUB(NOW(), INTERVAL 30 DAY)
  AND (IFNULL(r.download_count,0) < 3 OR IFNULL(r.view_count,0) < 10);

-- ----------------------------------------------------------
-- 6. 索引增强：审核流水 + 资源表
-- ----------------------------------------------------------

-- 6.1 resource_audit_log 增加审核耗时相关索引
SET @idx_exists = (
    SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'resource_audit_log'
      AND index_name = 'idx_auditor_action'
);
SET @ddl = IF(@idx_exists = 0,
    'ALTER TABLE `resource_audit_log` ADD INDEX `idx_auditor_action` (`auditor_id`, `action`, `created_at`)',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6.2 resource_audit_log 时间范围索引
SET @idx_exists = (
    SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'resource_audit_log'
      AND index_name = 'idx_created_at'
);
SET @ddl = IF(@idx_exists = 0,
    'ALTER TABLE `resource_audit_log` ADD INDEX `idx_created_at` (`created_at`)',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6.3 oss_primary_chinese_resource 增加 audit_time + audit_status 联合索引
SET @idx_exists = (
    SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'oss_primary_chinese_resource'
      AND index_name = 'idx_audit_time_status'
);
SET @ddl = IF(@idx_exists = 0,
    'ALTER TABLE `oss_primary_chinese_resource` ADD INDEX `idx_audit_time_status` (`audit_time`, `audit_status`)',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 6.4 oss_primary_chinese_resource upload_time + audit_status 联合索引
SET @idx_exists = (
    SELECT COUNT(1) FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'oss_primary_chinese_resource'
      AND index_name = 'idx_upload_audit'
);
SET @ddl = IF(@idx_exists = 0,
    'ALTER TABLE `oss_primary_chinese_resource` ADD INDEX `idx_upload_audit` (`upload_time`, `audit_status`)',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------------------------------------
-- 7. sys_menu 质量治理菜单项
-- ----------------------------------------------------------
SET @parent_id = (SELECT id FROM `sys_menu` WHERE `name` = 'AdminAnalytics' AND `parent_id` = 0 LIMIT 1);

-- 如果 analytics 不存在，用 dashboard 作为替代
SET @parent_id = IFNULL(@parent_id, (SELECT id FROM `sys_menu` WHERE `name` = 'AdminDashboard' AND `parent_id` = 0 LIMIT 1));

INSERT INTO `sys_menu` (`parent_id`, `title`, `path`, `name`, `icon`, `component`, `permission_code`, `sort`, `hidden`, `status`)
VALUES
  (0, '质量治理', 'quality', 'AdminQualityShell', 'shield', 'admin/views/quality/QualityShell.vue', 'admin:quality:sensitive_view', 85, 0, 1),
  ((SELECT id FROM `sys_menu` WHERE `name` = 'AdminQualityShell' LIMIT 1),
   '质量大盘', 'dashboard', 'AdminQualityDashboard', NULL,
   'admin/views/quality/QualityDashboard.vue', 'admin:quality:sensitive_view', 10, 0, 1),
  ((SELECT id FROM `sys_menu` WHERE `name` = 'AdminQualityShell' LIMIT 1),
   '敏感词库', 'sensitive-words', 'AdminSensitiveWords', NULL,
   'admin/views/quality/SensitiveWords.vue', 'admin:quality:sensitive_view', 20, 0, 1),
  ((SELECT id FROM `sys_menu` WHERE `name` = 'AdminQualityShell' LIMIT 1),
   '预览失败', 'preview-fails', 'AdminPreviewFails', NULL,
   'admin/views/quality/PreviewFails.vue', 'admin:quality:preview_fail', 30, 0, 1);

-- ----------------------------------------------------------
-- 8. sys_permission 质量治理权限初始化
-- ----------------------------------------------------------
INSERT INTO `sys_permission` (`code`, `name`, `type`, `module`, `parent_id`, `sort`, `status`) VALUES
('admin:quality:menu',       '质量治理菜单',    'menu',   'quality', 7, 80, 1),
('admin:quality:sensitive_view', '查看敏感词',   'button', 'quality', 7, 81, 1),
('admin:quality:sensitive_edit', '管理敏感词',   'button', 'quality', 7, 82, 1),
('admin:quality:preview_fail',   '预览失败队列',  'button', 'quality', 7, 83, 1),
('admin:quality:low_access',     '低访问资源',    'button', 'quality', 7, 84, 1),
('admin:quality:workload',       '审核员工作量',  'button', 'quality', 7, 85, 1),
('admin:quality:sla',            '审核 SLA',      'button', 'quality', 7, 86, 1)
ON DUPLICATE KEY UPDATE
  `name`   = VALUES(`name`),
  `type`   = VALUES(`type`),
  `module` = VALUES(`module`),
  `sort`   = VALUES(`sort`),
  `status` = 1;

-- ----------------------------------------------------------
-- 9. 授权：super_admin (role_id=1) + system_admin (role_id=7)
-- ----------------------------------------------------------
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.id FROM `sys_permission` p WHERE p.code LIKE 'admin:quality:%';

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 7, p.id FROM `sys_permission` p WHERE p.code LIKE 'admin:quality:%';

-- ----------------------------------------------------------
-- 10. 初始敏感词种子数据
-- ----------------------------------------------------------
INSERT IGNORE INTO `sys_sensitive_word` (`word`, `category`, `level`, `status`, `remark`) VALUES
('赌博',   4, 2, 1, '违法信息-赌博'),
('暴力',   4, 1, 1, '不当内容-暴力'),
('毒品',   4, 2, 1, '违法信息-毒品'),
('代写作业', 3, 1, 1, '违规广告-代写'),
('代写论文', 3, 1, 1, '违规广告-代写'),
('作弊',   3, 1, 1, '违规内容-作弊'),
('考试答案', 3, 1, 1, '违规内容-答案买卖'),
('色情',   2, 2, 1, '违规信息-色情'),
('裸聊',   2, 2, 1, '违规信息-色情'),
('政治敏感', 1, 2, 1, '政治敏感-通用'),
('假证',   3, 1, 1, '违规广告-假证'),
('办证',   3, 1, 1, '违规广告-办证');

-- ----------------------------------------------------------
-- 11. 数据回填：将现有预览失败的资源灌入 preview_fail_queue
-- ----------------------------------------------------------
INSERT IGNORE INTO `preview_fail_queue`
  (`source_type`, `source_id`, `global_id`, `title`, `fail_reason`, `last_fail_time`, `status`)
SELECT
  'primary_chinese',
  r.id,
  rm.id,
  r.title,
  CONCAT('预览生成失败(preview_status=', r.preview_status, ')'),
  IFNULL(r.audit_time, r.upload_time),
  0
FROM `oss_primary_chinese_resource` r
LEFT JOIN `resource_main` rm ON rm.source_type = 'primary_chinese' AND rm.source_id = r.id
WHERE r.preview_status = 3
  AND r.is_deleted = 0;

-- ----------------------------------------------------------
-- 12. 数据回填：生成最近30天的 resource_daily_stats
-- ----------------------------------------------------------
INSERT IGNORE INTO `resource_daily_stats`
  (`stat_date`, `source_type`, `total_count`, `new_count`, `pending_count`,
   `approved_count`, `rejected_count`, `published_count`, `offline_count`,
   `deleted_count`, `download_count`, `view_count`)
SELECT
  d.stat_date,
  'ALL' AS source_type,
  COUNT(*) AS total_count,
  SUM(CASE WHEN DATE(r.upload_time) = d.stat_date THEN 1 ELSE 0 END) AS new_count,
  SUM(CASE WHEN r.audit_status = 0 THEN 1 ELSE 0 END) AS pending_count,
  SUM(CASE WHEN r.audit_status = 1 AND DATE(r.audit_time) = d.stat_date THEN 1 ELSE 0 END) AS approved_count,
  SUM(CASE WHEN r.audit_status = 2 AND DATE(r.audit_time) = d.stat_date THEN 1 ELSE 0 END) AS rejected_count,
  SUM(CASE WHEN r.publish_status = 1 THEN 1 ELSE 0 END) AS published_count,
  SUM(CASE WHEN r.publish_status = 2 AND DATE(r.update_time) = d.stat_date THEN 1 ELSE 0 END) AS offline_count,
  SUM(CASE WHEN r.is_deleted = 1 AND DATE(r.update_time) = d.stat_date THEN 1 ELSE 0 END) AS deleted_count,
  SUM(CASE WHEN DATE(r.update_time) = d.stat_date THEN IFNULL(r.download_count,0) ELSE 0 END) AS download_count,
  SUM(CASE WHEN DATE(r.update_time) = d.stat_date THEN IFNULL(r.view_count,0) ELSE 0 END) AS view_count
FROM (
  SELECT DATE_SUB(CURDATE(), INTERVAL n DAY) AS stat_date
  FROM (
    SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
    UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14
    UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19
    UNION ALL SELECT 20 UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24
    UNION ALL SELECT 25 UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29
  ) nums
) d
CROSS JOIN `oss_primary_chinese_resource` r
GROUP BY d.stat_date;

-- ----------------------------------------------------------
-- 13. 数据回填：生成最近30天的 auditor_workload
-- ----------------------------------------------------------
INSERT IGNORE INTO `auditor_workload`
  (`stat_date`, `auditor_id`, `auditor_name`, `approve_count`, `reject_count`,
   `total_count`, `avg_duration_sec`, `max_duration_sec`, `overtime_count`)
SELECT
  DATE(log.created_at) AS stat_date,
  log.auditor_id,
  MAX(log.auditor_name) AS auditor_name,
  SUM(CASE WHEN log.action IN ('approve', 'approve_publish', 'approve_audit') THEN 1 ELSE 0 END) AS approve_count,
  SUM(CASE WHEN log.action = 'reject' THEN 1 ELSE 0 END) AS reject_count,
  COUNT(*) AS total_count,
  -- 从 resource_audit_log 无法直接算出审核耗时(需要前后状态时间差)，暂设为0，后续通过定时任务精确计算
  0 AS avg_duration_sec,
  0 AS max_duration_sec,
  0 AS overtime_count
FROM `resource_audit_log` log
WHERE log.created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY DATE(log.created_at), log.auditor_id;

-- ----------------------------------------------------------
-- 14. 验证
-- ----------------------------------------------------------
SELECT '>>> 敏感词数量' AS info;
SELECT category, COUNT(*) AS cnt
FROM `sys_sensitive_word`
WHERE status = 1
GROUP BY category;

SELECT '>>> 预览失败队列' AS info;
SELECT status, COUNT(*) AS cnt
FROM `preview_fail_queue`
GROUP BY status;

SELECT '>>> 最近7天资源日统计' AS info;
SELECT stat_date, source_type, new_count, approved_count, rejected_count, published_count
FROM `resource_daily_stats`
WHERE stat_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
ORDER BY stat_date DESC
LIMIT 10;

SELECT '>>> 低访问资源数' AS info;
SELECT COUNT(*) AS low_access_cnt
FROM `v_low_access_resource`;

SET FOREIGN_KEY_CHECKS = 1;
-- ============================================================
-- 回滚：
-- DROP VIEW IF EXISTS `v_low_access_resource`;
-- DROP TABLE IF EXISTS `auditor_workload`;
-- DROP TABLE IF EXISTS `resource_daily_stats`;
-- DROP TABLE IF EXISTS `preview_fail_queue`;
-- DROP TABLE IF EXISTS `sys_sensitive_word`;
-- DELETE FROM `sys_role_permission` WHERE permission_id IN (SELECT id FROM `sys_permission` WHERE code LIKE 'admin:quality:%');
-- DELETE FROM `sys_menu` WHERE `name` IN ('AdminQualityShell','AdminQualityDashboard','AdminSensitiveWords','AdminPreviewFails');
-- DELETE FROM `sys_permission` WHERE code LIKE 'admin:quality:%';
-- ============================================================
