package com.k12.resource.search;

import com.k12.resource.entity.SysSearchDocument;
import lombok.Data;

import java.util.EnumSet;
import java.util.Set;

/**
 * 多路召回候选文档
 */
@Data
public class SearchRecallCandidate {
    private SysSearchDocument document;
    private Set<SearchRecallPath> paths = EnumSet.noneOf(SearchRecallPath.class);
    private int clickCount;

    public static SearchRecallCandidate of(SysSearchDocument doc, SearchRecallPath path) {
        SearchRecallCandidate c = new SearchRecallCandidate();
        c.setDocument(doc);
        c.getPaths().add(path);
        return c;
    }

    public void addPath(SearchRecallPath path) {
        paths.add(path);
    }
}
