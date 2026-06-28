package com.k12.resource.search.engine;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchEngineQueryResult {
    private int total;
    private List<String> docIds = new ArrayList<>();
    private int costMs;
    private String error;
}
