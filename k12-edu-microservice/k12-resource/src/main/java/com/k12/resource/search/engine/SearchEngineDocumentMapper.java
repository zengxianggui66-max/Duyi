package com.k12.resource.search.engine;

import com.k12.resource.entity.SysSearchDocument;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * sys_search_document -> OpenSearch 文档
 */
public final class SearchEngineDocumentMapper {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private SearchEngineDocumentMapper() {
    }

    public static Map<String, Object> toEngineDoc(SysSearchDocument doc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("docId", doc.getDocId());
        m.put("docType", doc.getDocType());
        m.put("bizId", doc.getBizId());
        m.put("title", nvl(doc.getTitle()));
        m.put("subtitle", nvl(doc.getSubtitle()));
        m.put("summary", nvl(doc.getSummary()));
        m.put("contentText", nvl(doc.getContentText()));
        m.put("keywordText", nvl(doc.getKeywordText()));
        m.put("stageKey", doc.getStageKey());
        m.put("stageName", doc.getStageName());
        m.put("subjectKey", doc.getSubjectKey());
        m.put("subjectName", doc.getSubjectName());
        m.put("gradeName", doc.getGradeName());
        m.put("channelKey", doc.getChannelKey());
        m.put("channelName", doc.getChannelName());
        m.put("moduleKey", doc.getModuleKey());
        m.put("moduleName", doc.getModuleName());
        m.put("resourceTypeKey", doc.getResourceTypeKey());
        m.put("resourceTypeName", doc.getResourceTypeName());
        m.put("routePath", doc.getRoutePath());
        if (doc.getPublishTime() != null) {
            m.put("publishTime", doc.getPublishTime().format(ISO));
        }
        m.put("hotScore", doc.getHotScore() == null ? 0D : doc.getHotScore());
        m.put("qualityScore", doc.getQualityScore() == null ? 0D : doc.getQualityScore().doubleValue());
        m.put("status", doc.getStatus() == null ? 1 : doc.getStatus());
        m.put("isDeleted", doc.getIsDeleted() == null ? 0 : doc.getIsDeleted());
        return m;
    }

    private static String nvl(String s) {
        return StringUtils.hasText(s) ? s : "";
    }
}
