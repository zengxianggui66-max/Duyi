package com.k12.resource.search.engine;

import com.k12.resource.config.SearchProperties;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OpenSearch 健康检查
 */
@Service
public class SearchEngineHealthService {

    private final SearchEngineClient engineClient;
    private final SearchProperties searchProperties;
    public SearchEngineHealthService(SearchEngineClient engineClient, SearchProperties searchProperties) {
        this.engineClient = engineClient;
        this.searchProperties = searchProperties;
    }


    public Map<String, Object> health() {
        Map<String, Object> out = new LinkedHashMap<>();
        SearchProperties.Engine cfg = searchProperties.getEngine();
        out.put("provider", cfg.getProvider());
        out.put("indexName", cfg.getIndexName());
        out.put("configured", cfg.isConfigured());
        out.put("enabled", cfg.isEnabled());
        out.put("shadow", cfg.isShadow());
        out.put("fallbackToMysql", cfg.isFallbackToMysql());
        out.put("trafficPercent", cfg.getTrafficPercent());
        if (!cfg.isConfigured()) {
            out.put("reachable", false);
            out.put("status", "NOT_CONFIGURED");
            return out;
        }
        boolean ping = engineClient.ping();
        out.put("reachable", ping);
        if (!ping) {
            out.put("status", "UNREACHABLE");
            return out;
        }
        try {
            out.putAll(engineClient.clusterHealth());
            out.put("indexDocCount", engineClient.countIndexDocs());
            out.put("status", "OK");
        } catch (Exception e) {
            out.put("status", "ERROR");
            out.put("error", e.getMessage());
        }
        return out;
    }
}
