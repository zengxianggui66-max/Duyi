package com.k12.resource.search.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k12.common.dto.SearchAllResultVO;
import com.k12.common.dto.SearchResultItemVO;
import com.k12.resource.config.SearchProperties;
import com.k12.resource.entity.SearchEngineShadowLog;
import com.k12.resource.entity.SysSearchDocument;
import com.k12.resource.mapper.SearchEngineShadowLogMapper;
import com.k12.resource.search.ParsedSearchQuery;
import com.k12.resource.search.SearchDocumentQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * P3 影子双查：MySQL 主路径返回，异步对比 OpenSearch 并落库
 */
@Slf4j
@Service
public class SearchEngineShadowService {

    private final SearchProperties searchProperties;
    private final SearchEngineQueryService engineQueryService;
    private final SearchEngineShadowLogMapper shadowLogMapper;
    private final SearchDocumentQueryService documentQueryService;
    private final ObjectMapper objectMapper;
    public SearchEngineShadowService(SearchProperties searchProperties, SearchEngineQueryService engineQueryService, SearchEngineShadowLogMapper shadowLogMapper, SearchDocumentQueryService documentQueryService, ObjectMapper objectMapper) {
        this.searchProperties = searchProperties;
        this.engineQueryService = engineQueryService;
        this.shadowLogMapper = shadowLogMapper;
        this.documentQueryService = documentQueryService;
        this.objectMapper = objectMapper;
    }


    public boolean shouldShadow() {
        SearchProperties.Engine cfg = searchProperties.getEngine();
        return cfg.isShadow() && cfg.isConfigured();
    }

    public boolean shouldServeEngine(String clientKey) {
        SearchProperties.Engine cfg = searchProperties.getEngine();
        if (!cfg.isEnabled() || !cfg.isConfigured()) {
            return false;
        }
        int pct = Math.min(100, Math.max(0, cfg.getTrafficPercent()));
        if (pct >= 100) {
            return true;
        }
        if (pct <= 0) {
            return false;
        }
        String key = StringUtils.hasText(clientKey) ? clientKey : "anonymous";
        int bucket = Math.abs(key.hashCode()) % 100;
        return bucket < pct;
    }

    /** Phase 7-F：NavTarget searchEngine=auto 时，hosts 已配置则优先走 OpenSearch（失败回退 MySQL） */
    public boolean preferEngineForAuto(String searchEngine) {
        return "auto".equalsIgnoreCase(searchEngine) && searchProperties.getEngine().isConfigured();
    }

    @Async
    public void compareAllAsync(
            String keyword,
            ParsedSearchQuery parsed,
            SearchAllResultVO mysqlResult,
            int mysqlCostMs) {
        if (!shouldShadow() || !StringUtils.hasText(keyword)) {
            return;
        }
        try {
            int page = mysqlResult != null && mysqlResult.getPage() != null ? mysqlResult.getPage() : 1;
            int size = mysqlResult != null && mysqlResult.getSize() != null ? mysqlResult.getSize() : 20;
            SearchEngineQueryResult engineResult = engineQueryService.search(parsed, page, size);

            List<String> mysqlTop = extractDocIds(mysqlResult, 10);
            List<String> engineTop = engineResult.getDocIds().stream().limit(10).collect(Collectors.toList());

            SearchEngineShadowLog row = new SearchEngineShadowLog();
            row.setKeyword(keyword.trim());
            row.setApiPath("all");
            row.setMysqlCostMs(mysqlCostMs);
            row.setEngineCostMs(engineResult.getCostMs());
            row.setMysqlTopDocIds(toJson(mysqlTop));
            row.setEngineTopDocIds(toJson(engineTop));
            row.setTop3OverlapRate(toRate(overlapCount(mysqlTop, engineTop, 3), 3));
            row.setTop10OverlapRate(toRate(overlapCount(mysqlTop, engineTop, 10), 10));
            row.setMysqlTotal(mysqlResult == null || mysqlResult.getTotal() == null ? 0 : mysqlResult.getTotal());
            row.setEngineTotal(engineResult.getTotal());
            row.setEngineError(engineResult.getError());
            shadowLogMapper.insert(row);
        } catch (Exception e) {
            log.debug("Shadow compare failed for keyword={}: {}", keyword, e.getMessage());
        }
    }

    /**
     * 灰度阶段：用引擎 docId 顺序从 MySQL 索引表组装结果（fallback 仍走 MySQL 主搜）
     */
    public SearchAllResultVO buildResultFromEngine(
            ParsedSearchQuery parsed,
            int page,
            int size,
            boolean hideVip,
            List<String> highlightTerms) {
        SearchEngineQueryResult engineResult = engineQueryService.search(parsed, page, size);
        if (StringUtils.hasText(engineResult.getError())) {
            return null;
        }
        List<SysSearchDocument> ordered = documentQueryService.loadByDocIds(engineResult.getDocIds());
        if (ordered.isEmpty() && engineResult.getTotal() > 0) {
            return null;
        }
        SearchAllResultVO data = new SearchAllResultVO();
        data.setRecords(ordered.stream()
                .map(doc -> documentQueryService.toPublicResultItem(doc, parsed, highlightTerms))
                .collect(Collectors.toList()));
        data.setTotal(engineResult.getTotal());
        data.setPage(page);
        data.setSize(size);
        data.setPages(engineResult.getTotal() == 0 ? 0 : (int) Math.ceil(engineResult.getTotal() * 1.0 / size));
        data.setCostMs(engineResult.getCostMs());
        return data;
    }

    private List<String> extractDocIds(SearchAllResultVO mysqlResult, int limit) {
        if (mysqlResult == null || mysqlResult.getRecords() == null) {
            return List.of();
        }
        List<String> ids = new ArrayList<>();
        for (SearchResultItemVO item : mysqlResult.getRecords()) {
            if (ids.size() >= limit) {
                break;
            }
            if (StringUtils.hasText(item.getDocId())) {
                ids.add(item.getDocId());
            }
        }
        return ids;
    }

    private int overlapCount(List<String> a, List<String> b, int topN) {
        if (a == null || b == null || a.isEmpty()) {
            return 0;
        }
        Set<String> setB = new HashSet<>(b.subList(0, Math.min(topN, b.size())));
        int count = 0;
        for (int i = 0; i < Math.min(topN, a.size()); i++) {
            if (setB.contains(a.get(i))) {
                count++;
            }
        }
        return count;
    }

    private BigDecimal toRate(int overlap, int topN) {
        return BigDecimal.valueOf(overlap)
                .divide(BigDecimal.valueOf(topN), 4, RoundingMode.HALF_UP);
    }

    private String toJson(List<String> ids) {
        try {
            return objectMapper.writeValueAsString(ids);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }
}
