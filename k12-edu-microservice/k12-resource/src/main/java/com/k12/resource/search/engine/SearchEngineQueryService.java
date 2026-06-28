package com.k12.resource.search.engine;

import com.k12.resource.config.SearchProperties;
import com.k12.resource.search.ParsedSearchQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * OpenSearch 查询封装
 */
@Service
public class SearchEngineQueryService {

    private final SearchEngineClient engineClient;
    private final SearchProperties searchProperties;
    public SearchEngineQueryService(SearchEngineClient engineClient, SearchProperties searchProperties) {
        this.engineClient = engineClient;
        this.searchProperties = searchProperties;
    }


    public boolean isAvailable() {
        return searchProperties.getEngine().isConfigured() && engineClient.ping();
    }

    public SearchEngineQueryResult search(ParsedSearchQuery parsed, int page, int size) {
        String keyword = resolveKeyword(parsed);
        int from = Math.max(0, (page - 1) * size);
        return engineClient.search(keyword, from, size);
    }

    public SearchEngineQueryResult searchKeyword(String keyword, int page, int size) {
        int from = Math.max(0, (page - 1) * size);
        return engineClient.search(keyword, from, size);
    }

    private String resolveKeyword(ParsedSearchQuery parsed) {
        if (parsed == null) {
            return "";
        }
        if (StringUtils.hasText(parsed.getNormalizedQuery())) {
            return parsed.getNormalizedQuery();
        }
        return parsed.getRawQuery() == null ? "" : parsed.getRawQuery();
    }
}
