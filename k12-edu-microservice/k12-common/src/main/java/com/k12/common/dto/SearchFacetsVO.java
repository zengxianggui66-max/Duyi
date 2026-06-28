package com.k12.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchFacetsVO {
    private List<SearchFacetBucketVO> stages;
    private List<SearchFacetBucketVO> channels;
    private List<SearchFacetBucketVO> types;
    private List<SearchFacetBucketVO> domains;
}

