-- ============================================================
-- 97：Phase 3K — 表分层冻结（COMMENT 标记，不 DROP）
-- 文档：docs/Phase3K-表分层冻结清单.md
-- 执行：
--   mysql -u root -pzxg123456 xinketang < sql/97_phase3k_table_tier_comments.sql
-- 说明：MySQL ALTER TABLE COMMENT；冻结 1 个版本周期后再评估 DROP
-- ============================================================
USE `xinketang`;
SET NAMES utf8mb4;

-- ---------- PRIMARY 主链路（5） ----------
ALTER TABLE `resource_main` COMMENT='[TIER:PRIMARY] 统一资源索引；新功能经 ResourceWriteService/回填维护';
ALTER TABLE `edu_resource` COMMENT='[TIER:PRIMARY] 资源主事实表；写入 ResourceWriteService';
ALTER TABLE `edu_resource_file` COMMENT='[TIER:PRIMARY] 资源文件域；写入 ResourceWriteService';
ALTER TABLE `edu_resource_dimension` COMMENT='[TIER:PRIMARY] 资源维度域；写入 ResourceWriteService';
ALTER TABLE `edu_resource_placement` COMMENT='[TIER:PRIMARY] 挂载关系域；写入 ResourcePlacementService';

-- ---------- COMPAT 兼容源表（5） ----------
ALTER TABLE `oss_primary_chinese_resource` COMMENT='[TIER:COMPAT] 小学语文宽表；PrimaryChineseResourceService/ResourceSyncService 写；禁止新功能直连读';
ALTER TABLE `topic_resource` COMMENT='[TIER:COMPAT] 专题兼容源表；仅 TopicZoneService/TopicSourceAdapter 写';
ALTER TABLE `culture_resource` COMMENT='[TIER:COMPAT] 传统文化兼容源表；仅 CultureSourceAdapter 写';
ALTER TABLE `competition_resource` COMMENT='[TIER:COMPAT] 竞赛兼容源表；仅 CompetitionSourceAdapter 写';
ALTER TABLE `article` COMMENT='[TIER:COMPAT] 资讯正文域（k12-article）；不进 edu_resource；搜索经 SearchDocumentSyncService';

-- ---------- SEARCH_INDEX 搜索索引（1） ----------
ALTER TABLE `sys_search_document` COMMENT='[TIER:SEARCH_INDEX] 全站搜索统一索引；仅 SearchDocumentSyncService 写';

-- ---------- DEPRECATED 待废弃（4） ----------
ALTER TABLE `resource_category` COMMENT='[TIER:DEPRECATED@3K] 旧资源分类；Entity @Deprecated；row_count=0；见 Phase3K-delete-candidates.md';
ALTER TABLE `edu_resource_tag` COMMENT='[TIER:DEPRECATED@3K] 无 Entity；row_count=0 可删';
ALTER TABLE `edu_resource_region` COMMENT='[TIER:DEPRECATED@3K] 无 Entity；row_count=0 可删；已由 edu_resource_dimension 替代';
ALTER TABLE `edu_resource_search_flat` COMMENT='[TIER:DEPRECATED@3K] 无 Entity；row_count=0 可删；已由 sys_search_document 替代';

SELECT 'Phase 3K table tier comments applied' AS info;
