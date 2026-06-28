-- Phase 8-B: sys_config + config_view permission + yml seed defaults
USE `xinketang`;
SET NAMES utf8mb4;

-- ---------- 1. 系统配置表 ----------
CREATE TABLE IF NOT EXISTS `sys_config` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  `config_key`       VARCHAR(128) NOT NULL COMMENT '配置键，全局唯一',
  `config_value`     TEXT                  COMMENT '配置值（JSON 或 scalar）',
  `value_type`       VARCHAR(16)  NOT NULL COMMENT 'string/int/bool/json/secret',
  `group_code`       VARCHAR(32)  NOT NULL COMMENT 'upload/preview/storage/sms/oauth/feature',
  `description`      VARCHAR(255)          DEFAULT NULL,
  `requires_restart` TINYINT      NOT NULL DEFAULT 0 COMMENT '1=需重启生效',
  `updated_by`       BIGINT                DEFAULT NULL,
  `updated_at`       DATETIME              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_group_code` (`group_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统运行时配置';

-- ---------- 2. 种子（幂等，对齐 application.yml 默认值） ----------
INSERT IGNORE INTO `sys_config`
  (`config_key`, `config_value`, `value_type`, `group_code`, `description`, `requires_restart`) VALUES
-- upload
('upload.maxSizeMb', '500', 'int', 'upload', '单文件上传上限（MB）', 1),
('upload.allowedFormats', '["pdf","doc","docx","ppt","pptx","xls","xlsx","mp4","jpg","jpeg","png","zip"]', 'json', 'upload', '允许上传的文件扩展名', 0),
-- preview
('preview.enabled', 'true', 'bool', 'preview', '文档预览总开关', 0),
('preview.libreofficeEnabled', 'true', 'bool', 'preview', 'LibreOffice 转换开关', 0),
('preview.libreofficePath', 'C:/Program Files/LibreOffice/program/soffice.exe', 'string', 'preview', 'LibreOffice 可执行路径', 0),
('preview.poiFallbackEnabled', 'true', 'bool', 'preview', 'POI 降级预览', 0),
('preview.asyncEnabled', 'true', 'bool', 'preview', '异步预览任务', 0),
-- storage
('storage.provider', 'minio', 'string', 'storage', '存储提供商 local/minio/oss', 1),
('storage.minio.endpoint', 'http://localhost:9000', 'string', 'storage', 'MinIO 服务地址', 1),
('storage.minio.bucketName', 'k12-resources', 'string', 'storage', 'MinIO Bucket', 1),
('storage.minio.accessKey', 'admin', 'secret', 'storage', 'MinIO AccessKey', 1),
('storage.minio.secretKey', 'admin123', 'secret', 'storage', 'MinIO SecretKey', 1),
-- sms
('sms.provider', 'mock', 'string', 'sms', '短信服务商 mock/aliyun', 0),
('sms.mockEnabled', 'true', 'bool', 'sms', '短信 Mock 模式', 0),
-- oauth
('oauth.mockEnabled', 'true', 'bool', 'oauth', 'OAuth Mock 模式', 0),
('oauth.weixin.appId', 'your-weixin-app-id', 'string', 'oauth', '微信 AppId', 0),
('oauth.weixin.appSecret', 'your-weixin-app-secret', 'secret', 'oauth', '微信 AppSecret', 0),
('oauth.qq.appId', 'your-qq-app-id', 'string', 'oauth', 'QQ AppId', 0),
('oauth.qq.appSecret', 'your-qq-app-secret', 'secret', 'oauth', 'QQ AppSecret', 0),
-- feature
('feature.homeOpsApi.enabled', 'true', 'bool', 'feature', '首页运营 API 开关', 0);

-- ---------- 3. 权限：查看系统配置 ----------
INSERT INTO `sys_permission` (`code`, `name`, `type`, `module`, `parent_id`, `sort`, `status`) VALUES
('admin:system:config_view', '查看系统配置', 'button', 'system', 7, 75, 1)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `type` = VALUES(`type`),
  `module` = VALUES(`module`),
  `parent_id` = VALUES(`parent_id`),
  `sort` = VALUES(`sort`),
  `status` = VALUES(`status`);

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, p.id FROM `sys_permission` p WHERE p.code = 'admin:system:config_view';

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 7, p.id FROM `sys_permission` p WHERE p.code = 'admin:system:config_view';

INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT rp.role_id, p2.id
FROM `sys_role_permission` rp
INNER JOIN `sys_permission` p ON p.id = rp.permission_id AND p.code = 'admin:system:config_edit'
INNER JOIN `sys_permission` p2 ON p2.code = 'admin:system:config_view';
