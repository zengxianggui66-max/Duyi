package com.k12.resource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * P2 验收用例（需本地 MySQL + sys_search_document 索引）
 */
@SpringBootTest
@AutoConfigureMockMvc
class SearchP2AcceptanceTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    private JsonNode search(String q) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/search/all")
                        .param("q", q)
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode root = mapper.readTree(body);
        assertEquals(200, root.path("code").asInt(), body);
        return root.path("data");
    }

    @Test
    void lessonPlanSynonymShouldRecallInstructionalDesign() throws Exception {
        JsonNode data = search("教案");
        assertTrue(data.path("total").asInt() > 0, "搜「教案」应有结果");
        JsonNode designData = search("教学设计");
        assertTrue(designData.path("total").asInt() > 0, "搜「教学设计」应有结果（同义词）");
    }

    @Test
    void chuYiShouldNormalizeToGrade7Chinese() throws Exception {
        JsonNode data = search("初一语文");
        JsonNode intent = data.path("parsedIntent");
        assertTrue(
                intent.path("grade").asText("").contains("七年级")
                        || intent.path("normalizedQuery").asText("").contains("七年级"),
                "「初一语文」应归一化为七年级语文，intent=" + intent);
        assertEquals("chinese", intent.path("subject").asText());
        assertTrue(data.path("total").asInt() > 0, "「初一语文」识别后应召回初中学段语文资源");
    }

    @Test
    void chineseShouldPrioritizeStageSubjectEntries() throws Exception {
        JsonNode data = search("语文");
        Assumptions.assumeTrue(data.path("total").asInt() > 0, "索引无数据，跳过");
        List<JsonNode> records = toList(data.path("records"));
        int topN = Math.min(5, records.size());
        long subjectLike = records.stream().limit(topN)
                .filter(r -> isStageSubjectEntry(r))
                .count();
        assertTrue(subjectLike >= 2,
                "搜「语文」前5条应优先学段学科入口，实际前5: " + summarize(records, topN));
    }

    @Test
    void policyQueryShouldRecallNews() throws Exception {
        JsonNode data = search("教育局政策");
        Assumptions.assumeTrue(data.path("total").asInt() > 0, "索引无资讯数据，跳过");
        boolean hasNews = toList(data.path("records")).stream()
                .anyMatch(r -> "news".equals(r.path("contentDomain").asText())
                        || r.path("title").asText().contains("政策")
                        || r.path("title").asText().contains("通知")
                        || r.path("title").asText().contains("资讯"));
        assertTrue(hasNews, "「教育局政策」应召回教育资讯，records=" + summarize(toList(data.path("records")), 5));
    }

    @Test
    void zeroResultShouldProvideRecommendations() throws Exception {
        JsonNode data = search("xyz_no_result_p2_acceptance_999");
        assertEquals(0, data.path("total").asInt());
        JsonNode recs = data.path("recommendations");
        assertTrue(recs.isArray() && recs.size() > 0, "零结果应返回 recommendations");
    }

    private boolean isStageSubjectEntry(JsonNode row) {
        String title = row.path("title").asText("");
        String docType = row.path("docType").asText("");
        if ("subject".equals(docType)) return true;
        return title.contains("小学语文") || title.contains("初中语文") || title.contains("高中语文")
                || title.matches(".*(小学|初中|高中).*语文.*");
    }

    private List<JsonNode> toList(JsonNode arr) {
        List<JsonNode> list = new ArrayList<>();
        if (arr != null && arr.isArray()) {
            arr.forEach(list::add);
        }
        return list;
    }

    private String summarize(List<JsonNode> records, int n) {
        return records.stream().limit(n)
                .map(r -> r.path("title").asText("") + "|" + r.path("docType").asText("") + "|"
                        + r.path("contentDomain").asText(""))
                .collect(Collectors.joining("; "));
    }
}
