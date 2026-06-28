package com.k12.common.dto;

import lombok.Data;

import java.util.Map;

@Data
public class SearchSuggestItemVO {
    private String text;
    private String kind;
    private String resourceType;
    private Long resourceId;
    private String detailRoute;
    private Integer hitCount;
    private String highlight;
    private String subtitle;
    private Map<String, String> routeQuery;
    private String contentDomain;
    /** Phase 7-B: ops hot word navigation (browse/search) */
    private NavTargetDTO navTarget;
}

