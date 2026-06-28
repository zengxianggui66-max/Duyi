package com.k12.common.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SearchResultItemVO {
    private String docId;
    private Long resourceId;
    private String resourceType;
    private String title;
    private String summary;
    private String titleHighlight;
    private String summaryHighlight;
    private String stageKey;
    private String stageName;
    private String channelKey;
    private String channelName;
    private String subject;
    private String gradeName;
    private String teachingType;
    private String fileExt;
    private Integer downloadCount;
    private Integer viewCount;
    private String uploadTime;
    private Double score;
    private String detailRoute;
    private String coverUrl;
    private List<String> badges;
    private String docType;
    private String bizId;
    private String subtitle;
    private List<String> matchedFields;
    private Map<String, String> routeQuery;
    private String contentDomain;
}

