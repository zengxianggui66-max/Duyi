-- Phase 5-E：前台切源 + 搜索联动（无 DDL，仅说明）
-- 后端：TaxonomySearchSyncHook 已在 taxonomy/catalog 管理端变更时增量刷新 stage-subject 搜索文档
-- 前端：默认 USE_TAXONOMY_API / USE_DICTIONARY_API 开启；回退设 VITE_USE_TAXONOMY_API=false
USE `xinketang`;
SELECT 'phase_5e_no_schema_change' AS note;
