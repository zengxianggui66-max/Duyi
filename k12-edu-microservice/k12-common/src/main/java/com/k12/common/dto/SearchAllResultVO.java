package com.k12.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchAllResultVO {
    private List<SearchResultItemVO> records;
    private Integer total;
    private Integer page;
    private Integer size;
    private Integer pages;
    private SearchFacetsVO facets;
    private Integer costMs;
    /** 限流/敏感词等提示（非空时前端可 toast 展示） */
    private String queryHint;
    /** 零结果时推荐的热词/频道（P1） */
    private List<SearchSuggestItemVO> recommendations;
    /** 顶部「你可能要找」意图建议（P0） */
    private List<SearchSuggestItemVO> intents;
    /** P2 查询意图解析结果 */
    private SearchParsedIntentVO parsedIntent;
}

