package com.k12.resource.search;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.k12.resource.config.SearchProperties;

import com.k12.resource.entity.SysSearchSynonym;

import com.k12.resource.mapper.SysSearchSynonymMapper;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;



import java.util.ArrayList;

import java.util.Comparator;

import java.util.LinkedHashMap;

import java.util.LinkedHashSet;

import java.util.List;

import java.util.Locale;

import java.util.Map;

import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;



/**

 * 搜索词典：停用词 + 同义词（P2：DB 词典优先，YAML 兜底）

 */

@Service
public class SearchLexiconService {



    private static final Set<String> DEFAULT_STOP_WORDS = Set.of(

            "的", "了", "和", "及", "或", "与", "在", "是", "请", "帮我", "一下", "一个", "资源", "搜索"

    );



    private static final Map<String, List<String>> DEFAULT_SYNONYMS = Map.of(

            "教案", List.of("教学设计", "导学案"),

            "课件", List.of("ppt", "PPT", "幻灯片"),

            "试卷", List.of("真题", "模拟卷"),

            "班会", List.of("主题班会", "德育"),

            "竞赛", List.of("奥赛", "奥数", "信息学")

    );



    private final SearchProperties searchProperties;

    private final SysSearchSynonymMapper synonymMapper;
    public SearchLexiconService(SearchProperties searchProperties, SysSearchSynonymMapper synonymMapper) {
        this.searchProperties = searchProperties;
        this.synonymMapper = synonymMapper;
    }




    private volatile Map<String, List<String>> synonymMap = Map.of();

    private volatile Map<String, String> aliasToCanonical = Map.of();

    private volatile List<String> aliasesByLengthDesc = List.of();



    @PostConstruct

    public void init() {

        refreshFromDb();

    }



    public void refreshFromDb() {

        List<SysSearchSynonym> rows = synonymMapper.selectList(

                new LambdaQueryWrapper<SysSearchSynonym>().eq(SysSearchSynonym::getStatus, 1));

        if (rows == null || rows.isEmpty()) {

            synonymMap = mergeWithDefaults(Map.of());

            rebuildAliasIndex(synonymMap);

            return;

        }

        Map<String, List<String>> dbMap = new LinkedHashMap<>();

        Map<String, String> canonicalMap = new ConcurrentHashMap<>();

        for (SysSearchSynonym row : rows) {

            String word = row.getWord();

            if (!StringUtils.hasText(word)) {

                continue;

            }

            LinkedHashSet<String> group = new LinkedHashSet<>();

            group.add(word.trim());

            String canonical = StringUtils.hasText(row.getCanonical()) ? row.getCanonical().trim() : word.trim();

            canonicalMap.put(word.trim().toLowerCase(Locale.ROOT), canonical);

            if (StringUtils.hasText(row.getSynonyms())) {

                for (String part : row.getSynonyms().split("[,，;；]")) {

                    String alias = part.trim();

                    if (StringUtils.hasText(alias)) {

                        group.add(alias);

                        canonicalMap.put(alias.toLowerCase(Locale.ROOT), canonical);

                    }

                }

            }

            dbMap.put(word.trim(), new ArrayList<>(group));

        }

        synonymMap = mergeWithDefaults(dbMap);

        aliasToCanonical = canonicalMap;

        rebuildAliasIndex(synonymMap);

    }



    public boolean isStopWord(String token) {

        if (!StringUtils.hasText(token)) {

            return true;

        }

        return stopWords().contains(token.trim());

    }



    public Set<String> stopWords() {

        List<String> configured = searchProperties.getLexicon().getStopWords();

        if (configured == null || configured.isEmpty()) {

            return DEFAULT_STOP_WORDS;

        }

        return Set.copyOf(configured);

    }



    public Map<String, List<String>> synonyms() {

        if (synonymMap.isEmpty()) {

            return DEFAULT_SYNONYMS;

        }

        return synonymMap;

    }



    public String canonicalOf(String alias) {

        if (!StringUtils.hasText(alias)) {

            return alias;

        }

        String key = alias.trim().toLowerCase(Locale.ROOT);

        return aliasToCanonical.getOrDefault(key, alias.trim());

    }



    public List<String> allAliasesByLengthDesc() {

        return aliasesByLengthDesc;

    }



    public List<String> buildSearchTerms(String keyword) {

        LinkedHashMap<String, Boolean> terms = new LinkedHashMap<>();

        String normalized = normalize(keyword);

        if (!StringUtils.hasText(normalized)) {

            return List.of();

        }

        terms.put(normalized, Boolean.TRUE);

        for (String token : normalized.split("[\\s,，;；]+")) {

            addTokenWithSynonyms(terms, token);

        }

        if (terms.isEmpty()) {

            terms.put(normalized, Boolean.TRUE);

        }

        return new ArrayList<>(terms.keySet());

    }



    public List<String> buildHighlightTerms(String keyword, List<String> mustTokens) {

        LinkedHashSet<String> terms = new LinkedHashSet<>();

        if (mustTokens != null) {

            for (String token : mustTokens) {

                addTokenWithSynonyms(terms, token);

            }

        }

        if (StringUtils.hasText(keyword)) {

            for (String token : normalize(keyword).split("[\\s,，;；]+")) {

                addTokenWithSynonyms(terms, token);

            }

        }

        return new ArrayList<>(terms);

    }



    public List<SearchTokenClause> buildTokenClauses(List<String> mustTokens) {

        List<SearchTokenClause> clauses = new ArrayList<>();

        if (mustTokens == null) {

            return clauses;

        }

        for (String token : mustTokens) {

            if (!StringUtils.hasText(token) || isStopWord(token)) {

                continue;

            }

            LinkedHashSet<String> aliases = new LinkedHashSet<>();

            aliases.add(token.trim());

            aliases.addAll(lookupSynonyms(token.trim()));

            clauses.add(SearchTokenClause.of(token.trim(), new ArrayList<>(aliases)));

        }

        return clauses;

    }



    /** 资源类型同义词展开（教案 ↔ 教学设计） */

    public List<String> expandTypeSynonyms(String typeName) {

        LinkedHashSet<String> out = new LinkedHashSet<>();

        if (!StringUtils.hasText(typeName)) {

            return List.of();

        }

        out.add(typeName.trim());

        out.addAll(lookupSynonyms(typeName.trim()));

        return new ArrayList<>(out);

    }



    private Map<String, List<String>> mergeWithDefaults(Map<String, List<String>> dbMap) {

        Map<String, List<String>> merged = new LinkedHashMap<>(DEFAULT_SYNONYMS);

        merged.putAll(dbMap);

        Map<String, List<String>> configured = searchProperties.getLexicon().getSynonyms();

        if (configured != null && !configured.isEmpty()) {

            merged.putAll(configured);

        }

        return merged;

    }



    private void rebuildAliasIndex(Map<String, List<String>> map) {

        LinkedHashSet<String> aliases = new LinkedHashSet<>();

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {

            aliases.add(entry.getKey());

            aliases.addAll(entry.getValue());

        }

        aliasesByLengthDesc = aliases.stream()

                .filter(StringUtils::hasText)

                .sorted(Comparator.comparingInt(String::length).reversed())

                .distinct()

                .toList();

    }



    private void addTokenWithSynonyms(Set<String> out, String raw) {

        if (!StringUtils.hasText(raw)) {

            return;

        }

        String t = raw.trim();

        if (isStopWord(t)) {

            return;

        }

        out.add(t);

        out.addAll(lookupSynonyms(t));

    }



    private void addTokenWithSynonyms(Map<String, Boolean> out, String raw) {

        if (!StringUtils.hasText(raw)) {

            return;

        }

        String t = raw.trim();

        if (isStopWord(t)) {

            return;

        }

        out.put(t, Boolean.TRUE);

        for (String alias : lookupSynonyms(t)) {

            out.put(alias, Boolean.TRUE);

        }

    }



    private List<String> lookupSynonyms(String token) {

        List<String> result = new ArrayList<>();

        Map<String, List<String>> map = synonyms();

        List<String> direct = map.get(token);

        if (direct != null) {

            result.addAll(direct);

        }

        String lower = token.toLowerCase(Locale.ROOT);

        for (Map.Entry<String, List<String>> entry : map.entrySet()) {

            if (entry.getKey().equalsIgnoreCase(token) || entry.getKey().equalsIgnoreCase(lower)) {

                result.addAll(entry.getValue());

            }

            for (String alias : entry.getValue()) {

                if (alias.equalsIgnoreCase(token)) {

                    result.add(entry.getKey());

                    result.addAll(entry.getValue());

                }

            }

        }

        return result.stream().filter(StringUtils::hasText).distinct().toList();

    }



    private String normalize(String q) {

        if (!StringUtils.hasText(q)) {

            return "";

        }

        return q.trim().replaceAll("\\s+", " ");

    }

}


