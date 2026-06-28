package com.k12.resource.search;

import lombok.Data;

@Data
public class SearchFacetRow {
    private String facetKey;
    private String facetName;
    private Long facetCount;
}
