-- ============================================================
-- 第五步：管理端审核中心强化 — SQL 变更
-- 1. 驳回模板增加问题分类字段
-- 2. 审核流水动作字段兼容 approve_publish / approve_audit
-- 3. 初始化默认分类模板数据
-- ============================================================

-- 给 audit_reject_reason 表新增 category 字段
ALTER TABLE audit_reject_reason
    ADD COLUMN category TINYINT NOT NULL DEFAULT 0 COMMENT '问题分类: 0-通用, 1-内容质量, 2-格式规范, 3-版权合规, 4-分类挂载, 5-其他'
    AFTER content;

-- 将现有模板标记为"通用"分类（存量兼容）
-- UPDATE audit_reject_reason SET category = 0 WHERE category IS NULL OR category = 0;

-- 插入默认分类驳回模板（若尚不存在时可执行）
-- 内容质量类
INSERT IGNORE INTO audit_reject_reason (content, category, sort, status, created_at, updated_at) VALUES
('内容存在知识点错误或表述不准确', 1, 10, 1, NOW(), NOW());

INSERT IGNORE INTO audit_reject_reason (content, category, sort, status, created_at, updated_at) VALUES
('资源内容与标题/描述不符', 1, 20, 1, NOW(), NOW());

INSERT IGNORE INTO audit_reject_reason (content, category, sort, status, created_at, updated_at) VALUES
('存在错别字或语句不通顺', 1, 30, 1, NOW(), NOW());

-- 格式规范类
INSERT IGNORE INTO audit_reject_reason (content, category, sort, status, created_at, updated_at) VALUES
('文件格式不支持，请上传 PDF/PPT/Word/图片格式', 2, 10, 1, NOW(), NOW());

INSERT IGNORE INTO audit_reject_reason (content, category, sort, status, created_at, updated_at) VALUES
('文件排版混乱或内容不完整', 2, 20, 1, NOW(), NOW());

INSERT IGNORE INTO audit_reject_reason (content, category, sort, status, created_at, updated_at) VALUES
('文件清晰度不足，文字或图片无法识别', 2, 30, 1, NOW(), NOW());

-- 版权合规类
INSERT IGNORE INTO audit_reject_reason (content, category, sort, status, created_at, updated_at) VALUES
('疑似侵权内容，请提供版权证明', 3, 10, 1, NOW(), NOW());

INSERT IGNORE INTO audit_reject_reason (content, category, sort, status, created_at, updated_at) VALUES
('含有未授权第三方商业素材', 3, 20, 1, NOW(), NOW());

INSERT IGNORE INTO audit_reject_reason (content, category, sort, status, created_at, updated_at) VALUES
('水印遮挡内容主体，请重新上传', 3, 30, 1, NOW(), NOW());

-- 分类挂载类
INSERT IGNORE INTO audit_reject_reason (content, category, sort, status, created_at, updated_at) VALUES
('学段/年级/学科选择错误', 4, 10, 1, NOW(), NOW());

INSERT IGNORE INTO audit_reject_reason (content, category, sort, status, created_at, updated_at) VALUES
('栏目/分类挂载不正确', 4, 20, 1, NOW(), NOW());

INSERT IGNORE INTO audit_reject_reason (content, category, sort, status, created_at, updated_at) VALUES
('目录路径或单元/课文挂载错误', 4, 30, 1, NOW(), NOW());

-- 其他
INSERT IGNORE INTO audit_reject_reason (content, category, sort, status, created_at, updated_at) VALUES
('资源不符合平台收录标准', 5, 10, 1, NOW(), NOW());

INSERT IGNORE INTO audit_reject_reason (content, category, sort, status, created_at, updated_at) VALUES
('重复上传（已有相同或相似资源）', 5, 20, 1, NOW(), NOW());

-- 资源审核流水表兼容性：无需 DDL，action 字段为 VARCHAR 已支持新值
-- approve → 旧版兼容（等同于 approve_publish）
-- approve_publish → 审核通过并自动上架
-- approve_audit → 审核通过但未上架（等待手动发布）
-- reject → 驳回
