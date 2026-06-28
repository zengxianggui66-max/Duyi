package com.k12.resource.search;
import com.k12.resource.service.ResourceMainUpsertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 搜索索引增量同步钩子：资源上传/审核/更新后触发
 */
@Slf4j
@Component
public class SearchIndexSyncHook {
    private final SearchIndexSyncService syncService;
    private final SearchDocumentSyncService documentSyncService;
    private final ResourceMainUpsertService resourceMainUpsertService;

    public SearchIndexSyncHook(SearchIndexSyncService syncService,
                               SearchDocumentSyncService documentSyncService,
                               ResourceMainUpsertService resourceMainUpsertService) {
        this.syncService = syncService;
        this.documentSyncService = documentSyncService;
        this.resourceMainUpsertService = resourceMainUpsertService;
    }


    public void afterPrimaryChanged(Long resourceId) {
        if (resourceId == null) {
            return;
        }
        log.debug("search index sync hook: primary_chinese {}", resourceId);
        syncService.syncPrimaryChineseByIdAsync(resourceId);
        documentSyncService.syncPrimaryByIdAsync(resourceId);
    }

    public void afterTopicChanged(Long resourceId) {
        if (resourceId == null) {
            return;
        }
        log.debug("search index sync hook: topic {}", resourceId);
        resourceMainUpsertService.upsertFromTopicResource(resourceId);
        syncService.syncTopicByIdAsync(resourceId);
        documentSyncService.syncTopicByIdAsync(resourceId);
    }

    public void afterCompetitionChanged(Long resourceId) {
        if (resourceId == null) {
            return;
        }
        log.debug("search index sync hook: competition {}", resourceId);
        resourceMainUpsertService.upsertFromCompetitionResource(resourceId);
        syncService.syncCompetitionByIdAsync(resourceId);
        documentSyncService.syncCompetitionByIdAsync(resourceId);
    }

    public void afterCultureChanged(Long resourceId) {
        if (resourceId == null) {
            return;
        }
        log.debug("search index sync hook: culture {}", resourceId);
        resourceMainUpsertService.upsertFromCultureResource(resourceId);
        syncService.syncCultureByIdAsync(resourceId);
        documentSyncService.syncCultureByIdAsync(resourceId);
    }

    public void afterEduResourceChanged(Long resourceId) {
        if (resourceId == null) {
            return;
        }
        log.debug("search index sync hook: edu_resource {}", resourceId);
        resourceMainUpsertService.upsertFromEduResource(resourceId);
        documentSyncService.syncEduResourceByIdAsync(resourceId);
    }
}
