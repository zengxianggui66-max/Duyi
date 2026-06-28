package com.k12.common.dto;

import lombok.Data;

@Data
public class SearchFacetBucketVO {
    private String key;
    private String name;
    private Long count;
}

