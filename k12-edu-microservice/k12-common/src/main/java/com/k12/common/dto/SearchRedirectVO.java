package com.k12.common.dto;

import lombok.Data;

@Data
public class SearchRedirectVO {
    private Boolean directHit;
    private Double confidence;
    private String reason;
    private SearchResultItemVO target;
}

