package com.k12.resource.search;

import lombok.Data;

import java.util.List;

@Data
public class SearchIndexSqlParam {
    private List<String> mustTokens;
    private List<SearchTokenClause> tokenClauses;
    private String rawKeyword;
    private String stageKey;
    private String channelKey;
    private String teachingType;
    private String subject;
    private String editionName;
    private String gradeName;
    private String moduleName;
    private boolean hideVip;
    private String sort;
    private int offset;
    private int limit;
    /** 内容域筛选 */
    private String contentDomain;
    /** 内容域对应的 channel_key 列表（SQL IN） */
    private List<String> domainChannelKeys;
}
