package com.k12.resource.search.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.k12.resource.config.SearchProperties;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.util.EntityUtils;
import org.opensearch.client.Request;
import org.opensearch.client.Response;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.DeleteRequest;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.bulk.BulkOperation;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * OpenSearch 低层客户端（懒加载，未配置 hosts 时不连接）
 */
@Slf4j
@Component
public class SearchEngineClient {

    private final SearchProperties searchProperties;
    private final ObjectMapper objectMapper;
    private final AtomicReference<OpenSearchClient> clientRef = new AtomicReference<>();
    private final AtomicReference<RestClient> restClientRef = new AtomicReference<>();

    public SearchEngineClient(SearchProperties searchProperties, ObjectMapper objectMapper) {
        this.searchProperties = searchProperties;
        this.objectMapper = objectMapper;
    }

    public boolean isConfigured() {
        return searchProperties.getEngine().isConfigured();
    }

    public boolean ping() {
        if (!isConfigured()) {
            return false;
        }
        try {
            return getClient().ping().value();
        } catch (Exception e) {
            log.debug("OpenSearch ping failed: {}", e.getMessage());
            return false;
        }
    }

    public void ensureIndex() throws IOException {
        if (!isConfigured()) {
            return;
        }
        String indexName = indexName();
        OpenSearchClient client = getClient();
        boolean exists = client.indices().exists(ExistsRequest.of(e -> e.index(indexName))).value();
        if (exists) {
            return;
        }
        try (InputStream in = new ClassPathResource("opensearch/k12_search_document_v1.json").getInputStream()) {
            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            RestClient rest = restClientRef.get();
            Request request = new Request("PUT", "/" + indexName);
            request.setJsonEntity(json);
            Response response = rest.performRequest(request);
            int code = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            if (code >= 200 && code < 300) {
                log.info("Created OpenSearch index {}", indexName);
            }
        }
    }

    public void indexOne(Map<String, Object> doc) throws IOException {
        if (!isConfigured() || doc == null) {
            return;
        }
        String docId = (String) doc.get("docId");
        if (!StringUtils.hasText(docId)) {
            return;
        }
        getClient().index(IndexRequest.of(i -> i
                .index(indexName())
                .id(docId)
                .document(doc)));
    }

    public int bulkIndex(List<Map<String, Object>> docs) throws IOException {
        if (!isConfigured() || docs == null || docs.isEmpty()) {
            return 0;
        }
        List<BulkOperation> ops = new ArrayList<>();
        for (Map<String, Object> doc : docs) {
            String docId = (String) doc.get("docId");
            if (!StringUtils.hasText(docId)) {
                continue;
            }
            ops.add(BulkOperation.of(b -> b.index(IndexOperation.of(i -> i
                    .index(indexName())
                    .id(docId)
                    .document(doc)))));
        }
        if (ops.isEmpty()) {
            return 0;
        }
        BulkResponse resp = getClient().bulk(BulkRequest.of(b -> b.operations(ops)));
        if (resp.errors()) {
            log.warn("OpenSearch bulk had errors, items={}", resp.items().size());
        }
        return ops.size();
    }

    public void deleteOne(String docId) throws IOException {
        if (!isConfigured() || !StringUtils.hasText(docId)) {
            return;
        }
        getClient().delete(DeleteRequest.of(d -> d.index(indexName()).id(docId)));
    }

    public SearchEngineQueryResult search(String keyword, int from, int size) {
        SearchEngineQueryResult result = new SearchEngineQueryResult();
        if (!isConfigured()) {
            result.setError("engine_not_configured");
            return result;
        }
        long start = System.currentTimeMillis();
        try {
            Query query = buildQuery(keyword);
            SearchRequest request = SearchRequest.of(s -> s
                    .index(indexName())
                    .from(from)
                    .size(size)
                    .query(query)
                    .source(src -> src.filter(f -> f.includes("docId"))));
            SearchResponse<?> response = getClient().search(request, Map.class);
            result.setTotal(response.hits().total() == null ? 0 : (int) response.hits().total().value());
            response.hits().hits().forEach(hit -> {
                Object rawSource = hit.source();
                if (rawSource instanceof Map<?, ?> source && source.get("docId") != null) {
                    result.getDocIds().add(String.valueOf(source.get("docId")));
                } else if (StringUtils.hasText(hit.id())) {
                    result.getDocIds().add(hit.id());
                }
            });
        } catch (Exception e) {
            log.warn("OpenSearch search failed: {}", e.getMessage());
            result.setError(e.getMessage());
        }
        result.setCostMs((int) (System.currentTimeMillis() - start));
        return result;
    }

    public Map<String, Object> clusterHealth() throws IOException {
        if (!isConfigured()) {
            return Map.of("status", "NOT_CONFIGURED");
        }
        var health = getClient().cluster().health();
        return Map.of(
                "status", health.status().jsonValue(),
                "clusterName", health.clusterName(),
                "numberOfNodes", health.numberOfNodes(),
                "activePrimaryShards", health.activePrimaryShards());
    }

    public long countIndexDocs() throws IOException {
        if (!isConfigured()) {
            return -1;
        }
        var resp = getClient().count(c -> c.index(indexName()));
        return resp.count();
    }

    private Query buildQuery(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Query.of(q -> q.matchAll(m -> m));
        }
        String q = keyword.trim();
        return Query.of(query -> query.bool(b -> b
                .must(m -> m.multiMatch(mm -> mm
                        .query(q)
                        .fields("title^3", "subtitle^2", "summary", "keywordText^2", "contentText")))
                .filter(f -> f.term(t -> t.field("status").value(FieldValue.of(1))))
                .filter(f -> f.term(t -> t.field("isDeleted").value(FieldValue.of(0))))));
    }

    private String indexName() {
        return searchProperties.getEngine().getIndexName();
    }

    private OpenSearchClient getClient() {
        OpenSearchClient existing = clientRef.get();
        if (existing != null) {
            return existing;
        }
        synchronized (clientRef) {
            if (clientRef.get() != null) {
                return clientRef.get();
            }
            RestClient restClient = buildRestClientOnly();
            RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper(objectMapper));
            OpenSearchClient client = new OpenSearchClient(transport);
            restClientRef.set(restClient);
            clientRef.set(client);
            return client;
        }
    }

    private RestClient buildRestClientOnly() {
        SearchProperties.Engine cfg = searchProperties.getEngine();
        String[] parts = cfg.getHosts().split(",");
        HttpHost[] hosts = new HttpHost[parts.length];
        for (int i = 0; i < parts.length; i++) {
            String hostPort = parts[i].trim();
            String host = hostPort;
            int port = 9200;
            if (hostPort.contains(":")) {
                String[] hp = hostPort.split(":", 2);
                host = hp[0];
                port = Integer.parseInt(hp[1]);
            }
            hosts[i] = new HttpHost(host, port, cfg.getScheme());
        }
        RestClientBuilder builder = RestClient.builder(hosts);
        if (StringUtils.hasText(cfg.getUsername())) {
            BasicCredentialsProvider creds = new BasicCredentialsProvider();
            creds.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(cfg.getUsername(), cfg.getPassword()));
            builder.setHttpClientConfigCallback(hcb -> hcb.setDefaultCredentialsProvider(creds));
        }
        return builder.build();
    }

    @PreDestroy
    public void close() {
        RestClient restClient = restClientRef.getAndSet(null);
        clientRef.set(null);
        if (restClient != null) {
            try {
                restClient.close();
            } catch (IOException e) {
                log.debug("Close OpenSearch client: {}", e.getMessage());
            }
        }
    }
}
