-- ============================================================
-- 57 Phase 4-B 审核中心：审核流水 + 驳回原因模板 + 菜单/权限
-- 前置：sql/49_admin_rbac_baseline.sql, sql/51_admin_rbac_phase2.sql
-- mysql -u root -p xinketang --default-character-set=utf8mb4
-- source sql/57_admin_audit_phase4b.sql
-- ============================================================
USE xinketang;
SET NAMES utf8mb4;

-- ---------- 1. 审核流水表（业务级，区别于旧 resource_audit） ----------
CREATE TABLE IF NOT EXISTS `resource_audit_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `resource_id` BIGINT NOT NULL COMMENT '资源ID（oss_primary_chinese_resource.id）',
  `auditor_id` BIGINT NOT NULL COMMENT '审核人用户ID',
  `auditor_name` VARCHAR(64) DEFAULT NULL COMMENT '审核人昵称/用户名',
  `action` VARCHAR(32) NOT NULL COMMENT 'approve|reject',
  `before_status` TINYINT NOT NULL COMMENT '审核前资源状态',
  `after_status` TINYINT NOT NULL COMMENT '审核后资源状态',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '驳回原因/备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_auditor_id` (`auditor_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源审核流水';

-- ---------- 2. 驳回原因模板 ----------
CREATE TABLE IF NOT EXISTS `audit_reject_reason` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `content` VARCHAR(500) NOT NULL COMMENT '驳回原因文案',
  `sort` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核驳回原因模板';

INSERT INTO `audit_reject_reason` (`content`, `sort`, `status`)
SELECT * FROM (
  SELECT '资源标题不规范，请修改后重新提交' AS content, 10 AS sort, 1 AS status
  UNION ALL SELECT '文件格式不符合要求或无法打开', 20, 1
  UNION ALL SELECT '内容与所选学段/学科/册别不匹配', 30, 1
  UNION ALL SELECT '存在版权风险或疑似重复资源', 40, 1
  UNION ALL SELECT '资源描述不完整，请补充说明', 50, 1
) AS seed
WHERE NOT EXISTS (SELECT 1 FROM audit_reject_reason LIMIT 1);

-- ---------- 3. 权限点（按 code 插入，勿占用 id 28-30：已被 sql/54 资源 Phase3 使用） ----------
INSERT INTO `sys_permission` (`code`, `name`, `type`, `module`, `parent_id`, `sort`, `status`) VALUES
('admin:audit:records',      '审核记录',     'menu',   'audit', 3, 33, 1),
('admin:audit:reasons',      '驳回模板管理', 'menu',   'audit', 3, 34, 1),
('admin:audit:reasons:edit', '编辑驳回模板', 'button', 'audit', 3, 35, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `type` = VALUES(`type`),
  `module` = VALUES(`module`),
  `parent_id` = VALUES(`parent_id`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);

-- super_admin
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:audit:records', 'admin:audit:reasons', 'admin:audit:reasons:edit');

-- auditor：审核记录 + 驳回模板（只读菜单）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 3, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:audit:records', 'admin:audit:reasons');

-- content_admin：可编辑驳回模板
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 2, p.id FROM `sys_permission` p
WHERE p.code IN ('admin:audit:records', 'admin:audit:reasons', 'admin:audit:reasons:edit');
-- ---------- 4. 菜单：审核中心改为父级 + 三个子页 ----------
UPDATE `sys_menu` SET
  `title` = '审核中心',
  `path` = '/admin/audit',
  `name` = 'AdminAudit',
  `component` = NULL,
  `permission_code` = 'admin:audit:view',
  `sort` = 3
WHERE `id` = 3;

INSERT INTO `sys_menu` (`id`, `parent_id`, `title`, `path`, `name`, `icon`, `component`, `permission_code`, `sort`, `hidden`, `status`) VALUES
(31, 3, '待审队列', '/admin/audit/resources', 'AdminAuditResources', 'Document', 'admin/views/audit/ResourceAudit', 'admin:audit:view', 1, 0, 1),
(32, 3, '审核记录', '/admin/audit/records', 'AdminAuditRecords', 'List', 'admin/views/audit/AuditRecords', 'admin:audit:records', 2, 0, 1),
(33, 3, '驳回模板', '/admin/audit/reject-reasons', 'AdminAuditRejectReasons', 'EditPen', 'admin/views/audit/RejectReasons', 'admin:audit:reasons', 3, 0, 1)
ON DUPLICATE KEY UPDATE
  `parent_id` = VALUES(`parent_id`),
  `title` = VALUES(`title`),
  `path` = VALUES(`path`),
  `name` = VALUES(`name`),
  `permission_code` = VALUES(`permission_code`),
  `sort` = VALUES(`sort`);

-- 校验（IDE / DataGrip 可直接执行；勿用 mysql 客户端专用的 \G）
SELECT '=== resource_audit_log ===' AS section;
SHOW CREATE TABLE resource_audit_log;

SELECT '=== audit_reject_reason count ===' AS section;
SELECT COUNT(*) AS cnt FROM audit_reject_reason WHERE status = 1;

SELECT '=== audit menus ===' AS section;
SELECT id, parent_id, title, path, permission_code FROM sys_menu WHERE id IN (3, 31, 32, 33) ORDER BY id;
