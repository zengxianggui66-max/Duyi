package com.k12.resource.search;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * QueryParser 输出：从用户 query 中拆出结构化 filter 与 must 检索词（AND 语义）
 */
@Data
public class ParsedSearchQuery {
    private String rawQuery = "";
    /** 去掉实体后的剩余关键词（空格拼接） */
    private String keywordText = "";
    /** 参与 search_text LIKE 的 token，AND 关系 */
    private List<String> mustTokens = new ArrayList<>();
    private String stageKey;
    private String channelKey;
    private String teachingType;
    private String subject;
    private String editionName;
    private String gradeName;
    private String moduleName;
    /** 内容域：stage_resource / feature / prep / news */
    private String contentDomain;
    /** P2 归一化后的 query */
    private String normalizedQuery;
    /** P2 结构化 key：chinese / math / courseware / sync_prep */
    private String subjectKey;
    private String resourceTypeKey;
    private String moduleKey;
}
