package com.k12.resource.search;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * P2 中文查询归一化：同义词替换 + 简单分词
 */
@Service
public class SearchChineseNormalizer {

    private final SearchLexiconService lexiconService;
    public SearchChineseNormalizer(SearchLexiconService lexiconService) {
        this.lexiconService = lexiconService;
    }


    /** 归一化整句（别名 → 标准词，去空白） */
    public String normalize(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        String text = raw.trim().replaceAll("\\s+", "");
        for (String alias : lexiconService.allAliasesByLengthDesc()) {
            if (text.contains(alias)) {
                String canonical = lexiconService.canonicalOf(alias);
                if (StringUtils.hasText(canonical) && !canonical.equals(alias)) {
                    text = text.replace(alias, canonical);
                }
            }
        }
        return text;
    }

    /** 分词：归一化后按标点和空白切分，并补充最长别名切词 */
    public List<String> tokenize(String raw) {
        LinkedHashSet<String> tokens = new LinkedHashSet<>();
        String normalized = normalize(raw);
        if (!StringUtils.hasText(normalized)) {
            return List.of();
        }
        extractAliasTokens(normalized, tokens);
        for (String part : normalized.split("[\\s,，;；、]+")) {
            String t = part.trim();
            if (StringUtils.hasText(t) && !lexiconService.isStopWord(t)) {
                tokens.add(t);
            }
        }
        if (tokens.isEmpty()) {
            tokens.add(normalized);
        }
        return new ArrayList<>(tokens);
    }

    private void extractAliasTokens(String text, LinkedHashSet<String> out) {
        for (String alias : lexiconService.allAliasesByLengthDesc()) {
            if (text.contains(alias) && !lexiconService.isStopWord(alias)) {
                out.add(lexiconService.canonicalOf(alias));
            }
        }
    }
}
