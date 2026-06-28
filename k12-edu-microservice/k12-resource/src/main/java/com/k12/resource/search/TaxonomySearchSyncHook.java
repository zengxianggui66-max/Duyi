package com.k12.resource.search;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Phase 5-E：分类维度 / 字典 / 目录变更后触发搜索索引增量同步
 */
@Slf4j
@Component
public class TaxonomySearchSyncHook {

    private final SearchDocumentSyncService documentSyncService;
    public TaxonomySearchSyncHook(SearchDocumentSyncService documentSyncService) {
        this.documentSyncService = documentSyncService;
    }


    /**
     * 学段 / 学科 / 版本 / 栏目 / 资源类型等 taxonomy 变更
     */
    public void afterTaxonomyChanged() {
        log.info("taxonomy changed, refreshing stage-subject search documents");
        documentSyncService.syncStageSubjectBrowseAsync();
    }

    /**
     * 业务字典 / 标签变更（考试场景、浏览标签等）
     */
    public void afterDictionaryChanged() {
        log.debug("dictionary changed, search facet refresh deferred to next full rebuild");
    }

    /**
     * 教材目录树节点变更
     */
    public void afterCatalogChanged() {
        log.info("catalog changed, refreshing stage-subject search documents");
        documentSyncService.syncStageSubjectBrowseAsync();
    }
}
