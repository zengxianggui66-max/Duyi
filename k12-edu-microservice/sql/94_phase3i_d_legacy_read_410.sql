-- Phase 3I-D：旧读 API 410 下线开关（默认关，7 天 legacy 归零 + 灰度全开后由运维开启）
USE `xinketang`;
SET NAMES utf8mb4;

INSERT INTO `sys_config`
(`config_key`, `config_value`, `value_type`, `group_code`, `description`, `requires_restart`)
VALUES
('feature.legacyReadApi410.enabled', 'false', 'bool', 'feature',
 '旧读 API 410 下线总开关（开启后 GET legacy 读路径返回 410 + Location）', 0)
ON DUPLICATE KEY UPDATE
  `description` = VALUES(`description`),
  `group_code` = VALUES(`group_code`);

UPDATE `resource_migration_ledger`
SET `legacy_api_offline_status` = 1,
    `notes` = CONCAT(IFNULL(`notes`, ''), ' | 3I-D D1: 410 拦截器已就绪，待 7 天归零后开 feature.legacyReadApi410.enabled')
WHERE `source_type` IN ('primary_chinese', 'topic_resource', 'culture_resource', 'competition_resource')
  AND `legacy_api_offline_status` = 0;
