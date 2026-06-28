package com.k12.resource.search;

import com.k12.resource.config.SearchProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 启动时若索引表为空则自动全量重建（可配置关闭）
 */
@Slf4j
@Component
public class SearchIndexBootstrapRunner implements ApplicationRunner {

    private final SearchProperties searchProperties;
    private final SearchIndexSyncService syncService;
    private final SearchDocumentSyncService documentSyncService;
    public SearchIndexBootstrapRunner(SearchProperties searchProperties, SearchIndexSyncService syncService, SearchDocumentSyncService documentSyncService) {
        this.searchProperties = searchProperties;
        this.syncService = syncService;
        this.documentSyncService = documentSyncService;
    }


    @Override
    public void run(ApplicationArguments args) {
        if (!searchProperties.isIndexAutoRebuildOnEmpty()) {
            return;
        }
        if (documentSyncService.countDocuments() > 0) {
            return;
        }
        if (syncService.countIndexRows() > 0) {
            log.info("sys_search_document 为空但 resource_search_index 有数据，启动 P1 文档索引重建");
            documentSyncService.rebuildAllAsync();
            return;
        }
        log.info("搜索索引为空，启动异步全量重建（P1 sys_search_document + P0+ resource_search_index）");
        documentSyncService.rebuildAllAsync();
        syncService.rebuildAllAsync();
    }
}
