package com.k12.common.dto;

import lombok.Data;

@Data
public class SearchClickRequest {
    private String keyword;
    private String normalizedKeyword;
    private String contentDomain;
    private String docId;
    private Long resourceId;
    private String resourceType;
    /** result / redirect / recommend */
    private String clickType;
    private Integer position;
    private String detailRoute;
}
