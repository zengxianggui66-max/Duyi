package com.k12.resource.search.engine;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.resource.config.SearchProperties;
import com.k12.resource.entity.SysSearchDocument;
import com.k12.resource.mapper.SysSearchDocumentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * sys_search_document -> OpenSearch 全量/增量同步
 */
@Slf4j
@Service
public class SearchEngineSyncService {

    private static final int BULK_BATCH = 200;

    private final SearchEngineClient engineClient;
    private final SearchProperties searchProperties;
    private final SysSearchDocumentMapper documentMapper;
    public SearchEngineSyncService(SearchEngineClient engineClient, SearchProperties searchProperties, SysSearchDocumentMapper documentMapper) {
        this.engineClient = engineClient;
        this.searchProperties = searchProperties;
        this.documentMapper = documentMapper;
    }


    public boolean isActive() {
        return searchProperties.getEngine().isConfigured();
    }

    public int syncFull() {
        if (!isActive()) {
            log.info("Search engine sync skipped: hosts not configured");
            return 0;
        }
        try {
            engineClient.ensureIndex();
            List<SysSearchDocument> all = documentMapper.selectList(
                    new LambdaQueryWrapper<SysSearchDocument>()
                            .eq(SysSearchDocument::getStatus, 1)
                            .eq(SysSearchDocument::getIsDeleted, 0));
            int total = 0;
            List<Map<String, Object>> batch = new ArrayList<>(BULK_BATCH);
            for (SysSearchDocument doc : all) {
                batch.add(SearchEngineDocumentMapper.toEngineDoc(doc));
                if (batch.size() >= BULK_BATCH) {
                    total += engineClient.bulkIndex(batch);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) {
                total += engineClient.bulkIndex(batch);
            }
            log.info("OpenSearch full sync done, docs={}", total);
            return total;
        } catch (Exception e) {
            log.error("OpenSearch full sync failed", e);
            return 0;
        }
    }

    @Async
    public void syncFullAsync() {
        syncFull();
    }

    public void syncDocument(SysSearchDocument doc) {
        if (!isActive() || doc == null || !StringUtils.hasText(doc.getDocId())) {
            return;
        }
        try {
            engineClient.ensureIndex();
            if (Integer.valueOf(1).equals(doc.getStatus()) && Integer.valueOf(0).equals(doc.getIsDeleted())) {
                engineClient.indexOne(SearchEngineDocumentMapper.toEngineDoc(doc));
            } else {
                engineClient.deleteOne(doc.getDocId());
            }
        } catch (Exception e) {
            log.warn("OpenSearch sync doc {} failed: {}", doc.getDocId(), e.getMessage());
        }
    }

    public void syncByDocId(String docId) {
        if (!isActive() || !StringUtils.hasText(docId)) {
            return;
        }
        SysSearchDocument doc = documentMapper.selectOne(
                new LambdaQueryWrapper<SysSearchDocument>().eq(SysSearchDocument::getDocId, docId));
        if (doc == null) {
            deleteByDocId(docId);
        } else {
            syncDocument(doc);
        }
    }

    public void deleteByDocId(String docId) {
        if (!isActive() || !StringUtils.hasText(docId)) {
            return;
        }
        try {
            engineClient.deleteOne(docId);
        } catch (Exception e) {
            log.warn("OpenSearch delete {} failed: {}", docId, e.getMessage());
        }
    }
}
