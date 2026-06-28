-- Phase 3-P0: migration guardrails (ledger + unified read feature flags)
USE `xinketang`;
SET NAMES utf8mb4;

-- 1) 资源来源治理台账
CREATE TABLE IF NOT EXISTS `resource_migration_ledger` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `source_type` VARCHAR(64) NOT NULL COMMENT '资源来源类型',
  `legacy_source_table` VARCHAR(128) DEFAULT NULL COMMENT '旧来源表',
  `target_main_chain` VARCHAR(255) DEFAULT NULL COMMENT '目标主链路',
  `backfill_status` TINYINT NOT NULL DEFAULT 0 COMMENT '回填状态:0未开始 1进行中 2完成',
  `frontend_switch_status` TINYINT NOT NULL DEFAULT 0 COMMENT '前台切换:0未切 1灰度 2全量',
  `legacy_api_offline_status` TINYINT NOT NULL DEFAULT 0 COMMENT '旧接口:0保留 1兼容期 2下线',
  `compare_status` TINYINT NOT NULL DEFAULT 0 COMMENT '比对状态:0未知 1通过 2失败',
  `compare_pass_rate` TINYINT NOT NULL DEFAULT 0 COMMENT '最近比对通过率 0-100',
  `last_compared_at` DATETIME DEFAULT NULL COMMENT '最近比对时间',
  `notes` VARCHAR(1000) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_source_type` (`source_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源来源治理台账';

INSERT INTO `resource_migration_ledger`
(`source_type`, `legacy_source_table`, `target_main_chain`, `backfill_status`, `frontend_switch_status`, `legacy_api_offline_status`, `compare_status`, `compare_pass_rate`, `notes`)
VALUES
('primary_chinese', 'oss_primary_chinese_resource', 'resource_main + edu_resource + edu_resource_placement', 0, 0, 0, 0, 0, '学科资源主迁移链路'),
('topic_resource', 'topic_resource', 'resource_main + edu_resource', 0, 0, 0, 0, 0, '专题资源迁移链路'),
('culture_resource', 'culture_resource', 'resource_main + edu_resource', 0, 0, 0, 0, 0, '传统文化资源迁移链路'),
('competition_resource', 'competition_resource', 'resource_main + edu_resource', 0, 0, 0, 0, 0, '竞赛资源迁移链路'),
('article_attachment', 'article', 'article + edu_resource(file)', 0, 0, 0, 0, 0, '资讯附件迁移链路')
ON DUPLICATE KEY UPDATE
  `legacy_source_table` = VALUES(`legacy_source_table`),
  `target_main_chain` = VALUES(`target_main_chain`);

-- 2) Phase 3-P0 灰度开关（复用 sys_config feature 分组）
INSERT INTO `sys_config`
(`config_key`, `config_value`, `value_type`, `group_code`, `description`, `requires_restart`)
VALUES
('feature.resourceUnifiedRead.enabled', 'false', 'bool', 'feature', '统一资源读取总开关', 0),
('feature.topicUnifiedRead.enabled', 'false', 'bool', 'feature', '专题频道统一读取开关', 0),
('feature.cultureUnifiedRead.enabled', 'false', 'bool', 'feature', '传统文化频道统一读取开关', 0),
('feature.competitionUnifiedRead.enabled', 'false', 'bool', 'feature', '竞赛频道统一读取开关', 0),
('feature.primaryChineseUnifiedRead.enabled', 'false', 'bool', 'feature', '学科频道统一读取开关', 0)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `group_code` = VALUES(`group_code`);
