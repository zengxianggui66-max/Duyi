package com.k12.resource.search;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 单个 must token 及其同义词扩展（SQL 内 OR 匹配）
 */
@Data
public class SearchTokenClause {
    private String primary;
    private List<String> aliases = new ArrayList<>();

    public static SearchTokenClause of(String primary, List<String> aliases) {
        SearchTokenClause clause = new SearchTokenClause();
        clause.setPrimary(primary);
        clause.setAliases(aliases == null || aliases.isEmpty() ? List.of(primary) : aliases);
        return clause;
    }
}
