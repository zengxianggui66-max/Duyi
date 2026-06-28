package com.k12.resource.search;

import lombok.Data;

import java.util.List;

@Data
public class SearchDocumentSqlParam {
    private List<String> mustTokens;
    private List<SearchTokenClause> tokenClauses;
    private String rawKeyword;
    private String stageKey;
    private String channelKey;
    private String teachingType;
    private String subject;
    private String subjectKey;
    private String editionName;
    private String gradeName;
    /** P2：年级仅参与排序，不做 SQL 硬过滤 */
    private boolean softGradeFilter;
    private String moduleName;
    private String moduleKey;
    private String resourceTypeKey;
    /** P2：资源类型同义词展开（教案/教学设计等） */
    private List<String> teachingTypeNames;
    /** P2：OR 宽召回（默认 AND） */
    private boolean broadMatch;
    private String contentDomain;
    private List<String> domainChannelKeys;
    private boolean hideVip;
    private String sort;
    private int offset;
    private int limit;
}
