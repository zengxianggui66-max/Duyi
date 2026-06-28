package com.k12.resource.search;

import com.k12.resource.entity.SysSearchDocument;
import com.k12.resource.mapper.SearchClickLogMapper;
import com.k12.resource.mapper.SysSearchDocumentMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * P2 多路召回：精确标题 / 分词 AND / 分词 OR / 维度 / 同义词 / 热门点击 / 频道 / 资讯
 */
@Service
public class SearchMultiRecallService {

    private static final int RECALL_LIMIT = 200;

    private final SysSearchDocumentMapper documentMapper;
    private final SearchClickLogMapper clickLogMapper;
    private final SearchLexiconService lexiconService;
    public SearchMultiRecallService(SysSearchDocumentMapper documentMapper, SearchClickLogMapper clickLogMapper, SearchLexiconService lexiconService) {
        this.documentMapper = documentMapper;
        this.clickLogMapper = clickLogMapper;
        this.lexiconService = lexiconService;
    }


    public List<SearchRecallCandidate> recall(ParsedSearchQuery parsed, boolean hideVip) {
        LinkedHashMap<String, SearchRecallCandidate> merged = new LinkedHashMap<>();
        SearchDocumentSqlParam base = buildBaseParam(parsed, hideVip);

        mergeRows(merged, documentMapper.searchExactTitle(base), SearchRecallPath.EXACT_TITLE);
        mergeRows(merged, documentMapper.searchPage(copy(base, RECALL_LIMIT, 0)), SearchRecallPath.TOKEN_AND);

        SearchDocumentSqlParam broad = copy(base, RECALL_LIMIT, 0);
        broad.setBroadMatch(true);
        mergeRows(merged, documentMapper.searchBroadPage(broad), SearchRecallPath.TOKEN_OR);

        if (hasIntentFilters(parsed)) {
            SearchDocumentSqlParam intentOnly = copy(base, RECALL_LIMIT, 0);
            intentOnly.setMustTokens(List.of());
            intentOnly.setTokenClauses(List.of());
            intentOnly.setRawKeyword(null);
            mergeRows(merged, documentMapper.searchPage(intentOnly), SearchRecallPath.DIMENSION);
        }

        List<String> expandedTerms = lexiconService.buildSearchTerms(
                StringUtils.hasText(parsed.getNormalizedQuery()) ? parsed.getNormalizedQuery() : parsed.getRawQuery());
        if (expandedTerms.size() > 1) {
            SearchDocumentSqlParam syn = copy(base, RECALL_LIMIT, 0);
            syn.setBroadMatch(true);
            syn.setMustTokens(expandedTerms);
            syn.setTokenClauses(lexiconService.buildTokenClauses(expandedTerms));
            mergeRows(merged, documentMapper.searchBroadPage(syn), SearchRecallPath.SYNONYM);
        }

        Map<String, Integer> clickCounts = loadClickCounts(parsed.getRawQuery());
        if (!clickCounts.isEmpty()) {
            List<SysSearchDocument> clickDocs = documentMapper.findByDocIds(
                    new ArrayList<>(clickCounts.keySet()), hideVip);
            for (SysSearchDocument doc : clickDocs) {
                SearchRecallCandidate c = merged.computeIfAbsent(
                        doc.getDocId(), k -> SearchRecallCandidate.of(doc, SearchRecallPath.HOT_CLICK));
                c.addPath(SearchRecallPath.HOT_CLICK);
                c.setClickCount(clickCounts.getOrDefault(doc.getDocId(), 0));
                c.setDocument(doc);
            }
        }

        SearchDocumentSqlParam channelParam = copy(base, 30, 0);
        channelParam.setBroadMatch(true);
        channelParam.setContentDomain("feature");
        mergeRows(merged, documentMapper.searchBroadPage(channelParam), SearchRecallPath.CHANNEL);

        SearchDocumentSqlParam newsParam = copy(base, 30, 0);
        newsParam.setBroadMatch(true);
        newsParam.setContentDomain("news");
        mergeRows(merged, documentMapper.searchBroadPage(newsParam), SearchRecallPath.NEWS);

        return new ArrayList<>(merged.values());
    }

    public long countApprox(ParsedSearchQuery parsed, boolean hideVip) {
        SearchDocumentSqlParam param = buildBaseParam(parsed, hideVip);
        long strict = documentMapper.countSearch(param);
        if (strict > 0) {
            return strict;
        }
        param.setBroadMatch(true);
        return documentMapper.countBroadSearch(param);
    }

    private Map<String, Integer> loadClickCounts(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return Map.of();
        }
        List<Map<String, Object>> rows = clickLogMapper.topDocIdsByKeyword(keyword.trim(), 15);
        LinkedHashMap<String, Integer> out = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            Object docId = row.get("docId");
            Object cnt = row.get("clickCount");
            if (docId != null) {
                out.put(String.valueOf(docId), cnt == null ? 1 : ((Number) cnt).intValue());
            }
        }
        return out;
    }

    private void mergeRows(
            LinkedHashMap<String, SearchRecallCandidate> merged,
            List<SysSearchDocument> rows,
            SearchRecallPath path) {
        if (rows == null) {
            return;
        }
        for (SysSearchDocument row : rows) {
            if (row == null || !StringUtils.hasText(row.getDocId())) {
                continue;
            }
            SearchRecallCandidate existing = merged.get(row.getDocId());
            if (existing == null) {
                merged.put(row.getDocId(), SearchRecallCandidate.of(row, path));
            } else {
                existing.addPath(path);
                existing.setDocument(row);
            }
        }
    }

    private boolean hasIntentFilters(ParsedSearchQuery parsed) {
        return StringUtils.hasText(parsed.getStageKey())
                || StringUtils.hasText(parsed.getChannelKey())
                || StringUtils.hasText(parsed.getSubject())
                || StringUtils.hasText(parsed.getGradeName())
                || StringUtils.hasText(parsed.getTeachingType())
                || StringUtils.hasText(parsed.getModuleName());
    }

    private SearchDocumentSqlParam buildBaseParam(ParsedSearchQuery parsed, boolean hideVip) {
        SearchDocumentSqlParam param = new SearchDocumentSqlParam();
        List<String> mustTokens = parsed.getMustTokens() == null ? List.of() : parsed.getMustTokens();
        param.setMustTokens(mustTokens);
        param.setTokenClauses(lexiconService.buildTokenClauses(mustTokens));
        param.setRawKeyword(StringUtils.hasText(parsed.getKeywordText()) ? parsed.getKeywordText() : parsed.getRawQuery());
        param.setStageKey(parsed.getStageKey());
        param.setChannelKey(parsed.getChannelKey());
        param.setTeachingType(parsed.getTeachingType());
        param.setSubject(parsed.getSubject());
        param.setSubjectKey(parsed.getSubjectKey());
        param.setEditionName(parsed.getEditionName());
        param.setGradeName(parsed.getGradeName());
        param.setSoftGradeFilter(StringUtils.hasText(parsed.getStageKey()) && StringUtils.hasText(parsed.getGradeName()));
        param.setModuleName(parsed.getModuleName());
        param.setModuleKey(parsed.getModuleKey());
        param.setResourceTypeKey(parsed.getResourceTypeKey());
        param.setTeachingTypeNames(resolveTeachingTypeNames(parsed));
        param.setContentDomain(parsed.getContentDomain());
        if (StringUtils.hasText(parsed.getContentDomain())) {
            param.setDomainChannelKeys(SearchSiteCatalog.domainChannelKeys(parsed.getContentDomain()));
        }
        param.setHideVip(hideVip);
        param.setSort("score");
        param.setOffset(0);
        param.setLimit(RECALL_LIMIT);
        return param;
    }

    private SearchDocumentSqlParam copy(SearchDocumentSqlParam source, int limit, int offset) {
        SearchDocumentSqlParam copy = new SearchDocumentSqlParam();
        copy.setMustTokens(source.getMustTokens());
        copy.setTokenClauses(source.getTokenClauses());
        copy.setRawKeyword(source.getRawKeyword());
        copy.setStageKey(source.getStageKey());
        copy.setChannelKey(source.getChannelKey());
        copy.setTeachingType(source.getTeachingType());
        copy.setSubject(source.getSubject());
        copy.setSubjectKey(source.getSubjectKey());
        copy.setEditionName(source.getEditionName());
        copy.setGradeName(source.getGradeName());
        copy.setSoftGradeFilter(source.isSoftGradeFilter());
        copy.setModuleName(source.getModuleName());
        copy.setModuleKey(source.getModuleKey());
        copy.setResourceTypeKey(source.getResourceTypeKey());
        copy.setContentDomain(source.getContentDomain());
        copy.setDomainChannelKeys(source.getDomainChannelKeys());
        copy.setHideVip(source.isHideVip());
        copy.setBroadMatch(source.isBroadMatch());
        copy.setSort(source.getSort());
        copy.setOffset(offset);
        copy.setLimit(limit);
        return copy;
    }

    private List<String> resolveTeachingTypeNames(ParsedSearchQuery parsed) {
        if (!StringUtils.hasText(parsed.getTeachingType())) {
            return List.of();
        }
        return lexiconService.expandTypeSynonyms(parsed.getTeachingType());
    }
}
