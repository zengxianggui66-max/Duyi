package com.k12.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 搜索出口验收：索引分布 / 核心词命中 / 零结果
 */
@SpringBootTest
@AutoConfigureMockMvc
class SearchExportAcceptanceTest {

    private static final List<String> CORE_KEYWORDS = List.of(
            "语文", "主题班会", "备课", "教育局通知", "课件", "教案", "初一语文", "教学设计", "教育局政策"
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void exportAcceptanceSnapshot() throws Exception {
        Map<String, Object> report = new LinkedHashMap<>();

        report.put("docTypeDistribution", jdbcTemplate.queryForList(
                "SELECT doc_type, COUNT(*) c FROM sys_search_document WHERE status=1 AND is_deleted=0 GROUP BY doc_type ORDER BY c DESC"));
        report.put("channelDistribution", jdbcTemplate.queryForList(
                "SELECT channel_key, COUNT(*) c FROM sys_search_document WHERE status=1 AND is_deleted=0 GROUP BY channel_key ORDER BY c DESC"));
        report.put("noRouteCount", jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_search_document WHERE status=1 AND is_deleted=0 AND (route_path IS NULL OR route_path='')", Long.class));
        report.put("domainCoverage", Map.of(
                "stage_resource", countByDomain("stage_resource"),
                "feature", countByDomain("feature"),
                "prep", countByDomain("prep"),
                "news", countByDomain("news")
        ));
        report.put("prepEntries", jdbcTemplate.queryForList(
                "SELECT doc_id, title, route_path FROM sys_search_document WHERE doc_id IN ('prep:papers','prep:basket','prep:lesson','prep:smart','prep:assemble')"));
        report.put("topQueryKeywords", jdbcTemplate.queryForList(
                "SELECT keyword, COUNT(*) query_count FROM search_query_log GROUP BY keyword ORDER BY query_count DESC LIMIT 50"));
        report.put("zeroHitKeywords", jdbcTemplate.queryForList(
                "SELECT keyword, COUNT(*) cnt FROM search_query_log WHERE hit_count=0 GROUP BY keyword ORDER BY cnt DESC LIMIT 20"));

        List<Map<String, Object>> coreResults = new ArrayList<>();
        for (String kw : CORE_KEYWORDS) {
            coreResults.add(searchSummary(kw));
        }
        report.put("coreKeywordResults", coreResults);

        System.out.println("===== SEARCH EXPORT ACCEPTANCE =====");
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(report));

        assertTrue(((Number) report.get("noRouteCount")).longValue() == 0, "存在 route_path 为空的索引");
        assertTrue(((Number) ((Map<?, ?>) report.get("domainCoverage")).get("news")).longValue() > 0);
        assertTrue(((Number) ((Map<?, ?>) report.get("domainCoverage")).get("prep")).longValue() > 0);

        for (Map<String, Object> row : coreResults) {
            String kw = (String) row.get("keyword");
            if ("xyz_no_result_p2_acceptance_999".equals(kw)) {
                continue;
            }
            assertTrue(((Number) row.get("total")).intValue() > 0, "核心词无命中: " + kw);
        }
    }

    private long countByDomain(String domain) {
        return switch (domain) {
            case "news" -> jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sys_search_document WHERE status=1 AND is_deleted=0 AND doc_type='news'", Long.class);
            case "prep" -> jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sys_search_document WHERE status=1 AND is_deleted=0 AND doc_type IN ('prep','lesson')", Long.class);
            case "feature" -> jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sys_search_document WHERE status=1 AND is_deleted=0 AND doc_type IN ('feature','channel') AND channel_key IN ('topic','culture','competition','class_meeting','career')", Long.class);
            default -> jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM sys_search_document WHERE status=1 AND is_deleted=0 AND doc_type IN ('resource','subject','page')", Long.class);
        };
    }

    private Map<String, Object> searchSummary(String keyword) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/search/all")
                        .param("q", keyword).param("page", "1").param("size", "3"))
                .andExpect(status().isOk()).andReturn();
        JsonNode data = mapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8)).path("data");
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("keyword", keyword);
        row.put("total", data.path("total").asInt());
        row.put("parsedIntent", mapper.readValue(data.path("parsedIntent").toString(), Map.class));
        List<String> tops = new ArrayList<>();
        for (JsonNode r : data.path("records")) {
            tops.add(r.path("title").asText() + "|" + r.path("docType").asText() + "|" + r.path("contentDomain").asText());
        }
        row.put("top3", tops);
        row.put("recommendations", data.path("recommendations").size());
        return row;
    }
}
