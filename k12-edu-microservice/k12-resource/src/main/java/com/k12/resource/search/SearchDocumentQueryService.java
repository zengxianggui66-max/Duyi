package com.k12.resource.search;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.dto.SearchAllResultVO;
import com.k12.common.dto.SearchFacetBucketVO;
import com.k12.common.dto.SearchFacetsVO;
import com.k12.common.dto.SearchResultItemVO;
import com.k12.resource.entity.SysSearchDocument;
import com.k12.resource.mapper.SysSearchDocumentMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * P1 基于 sys_search_document 的单表检索 + 分面 + 联想
 */
@Service
public class SearchDocumentQueryService {

    private final SysSearchDocumentMapper documentMapper;
    private final SearchLexiconService lexiconService;
    private final SearchMultiRecallService multiRecallService;
    private final SearchIntentRecognizer intentRecognizer;
    public SearchDocumentQueryService(SysSearchDocumentMapper documentMapper, SearchLexiconService lexiconService, SearchMultiRecallService multiRecallService, SearchIntentRecognizer intentRecognizer) {
        this.documentMapper = documentMapper;
        this.lexiconService = lexiconService;
        this.multiRecallService = multiRecallService;
        this.intentRecognizer = intentRecognizer;
    }


    public boolean isIndexReady() {
        return documentMapper.selectCount(null) > 0;
    }

    /** P3：按引擎返回的 docId 顺序加载文档 */
    public List<SysSearchDocument> loadByDocIds(List<String> docIds) {
        if (docIds == null || docIds.isEmpty()) {
            return List.of();
        }
        List<SysSearchDocument> rows = documentMapper.selectList(
                new LambdaQueryWrapper<SysSearchDocument>().in(SysSearchDocument::getDocId, docIds));
        Map<String, SysSearchDocument> byId = new LinkedHashMap<>();
        for (SysSearchDocument row : rows) {
            byId.put(row.getDocId(), row);
        }
        List<SysSearchDocument> ordered = new ArrayList<>();
        for (String docId : docIds) {
            SysSearchDocument doc = byId.get(docId);
            if (doc != null) {
                ordered.add(doc);
            }
        }
        return ordered;
    }

    public SearchResultItemVO toPublicResultItem(
            SysSearchDocument doc,
            ParsedSearchQuery parsed,
            List<String> highlightTerms) {
        return toResultItem(doc, parsed, highlightTerms, null);
    }

    public SearchAllResultVO search(
            ParsedSearchQuery parsed,
            int page,
            int size,
            String sort,
            boolean hideVip,
            List<String> highlightTerms) {
        long start = System.currentTimeMillis();
        if (parsed != null && !StringUtils.hasText(parsed.getNormalizedQuery())) {
            ParsedSearchQuery enriched = intentRecognizer.recognize(parsed.getRawQuery());
            mergeParsed(parsed, enriched);
        }

        SearchAllResultVO data = new SearchAllResultVO();
        data.setParsedIntent(intentRecognizer.toIntentVo(parsed));

        if (useP2Ranking(parsed, sort)) {
            return searchWithMultiRecall(parsed, page, size, sort, hideVip, highlightTerms, data, start);
        }

        SearchDocumentSqlParam param = toSqlParam(parsed, hideVip, sort, page, size);
        long total = documentMapper.countSearch(param);
        List<SysSearchDocument> rows = total == 0 ? List.of() : documentMapper.searchPage(param);

        data.setRecords(rows.stream().map(r -> toResultItem(r, parsed, highlightTerms, null)).toList());
        data.setTotal((int) total);
        data.setPage(page);
        data.setSize(size);
        data.setPages(total == 0 ? 0 : (int) Math.ceil(total * 1.0 / size));
        data.setFacets(buildFacets(param));
        data.setCostMs((int) (System.currentTimeMillis() - start));
        return data;
    }

    private SearchAllResultVO searchWithMultiRecall(
            ParsedSearchQuery parsed,
            int page,
            int size,
            String sort,
            boolean hideVip,
            List<String> highlightTerms,
            SearchAllResultVO data,
            long start) {
        List<SearchRecallCandidate> candidates = multiRecallService.recall(parsed, hideVip);
        List<ScoredDoc> scored = new ArrayList<>();
        for (SearchRecallCandidate candidate : candidates) {
            SysSearchDocument doc = candidate.getDocument();
            if (doc == null) {
                continue;
            }
            double composite = SearchCompositeRanker.score(doc, parsed, highlightTerms, candidate);
            scored.add(new ScoredDoc(doc, candidate, composite));
        }
        if ("newest".equals(sort)) {
            scored.sort((a, b) -> {
                LocalDateTime ta = a.doc.getPublishTime();
                LocalDateTime tb = b.doc.getPublishTime();
                if (ta == null && tb == null) {
                    return 0;
                }
                if (ta == null) {
                    return 1;
                }
                if (tb == null) {
                    return -1;
                }
                return tb.compareTo(ta);
            });
        } else if ("download".equals(sort)) {
            scored.sort((a, b) -> Integer.compare(
                    b.doc.getDownloadCount() == null ? 0 : b.doc.getDownloadCount(),
                    a.doc.getDownloadCount() == null ? 0 : a.doc.getDownloadCount()));
        } else {
            scored.sort((a, b) -> Double.compare(b.score, a.score));
        }

        long total = multiRecallService.countApprox(parsed, hideVip);
        if (total < scored.size()) {
            total = scored.size();
        }
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(from + size, scored.size());
        List<SearchResultItemVO> records = from >= scored.size()
                ? List.of()
                : scored.subList(from, to).stream()
                .map(s -> toResultItem(s.doc, parsed, highlightTerms, s.score))
                .toList();

        SearchDocumentSqlParam facetParam = toSqlParam(parsed, hideVip, sort, 1, size);
        data.setRecords(records);
        data.setTotal((int) Math.min(Integer.MAX_VALUE, total));
        data.setPage(page);
        data.setSize(size);
        data.setPages(total == 0 ? 0 : (int) Math.ceil(total * 1.0 / size));
        data.setFacets(buildFacets(facetParam));
        data.setCostMs((int) (System.currentTimeMillis() - start));
        return data;
    }

    private boolean useP2Ranking(ParsedSearchQuery parsed, String sort) {
        return parsed != null && StringUtils.hasText(parsed.getRawQuery());
    }

    private void mergeParsed(ParsedSearchQuery target, ParsedSearchQuery source) {
        if (source == null) {
            return;
        }
        if (StringUtils.hasText(source.getNormalizedQuery())) {
            target.setNormalizedQuery(source.getNormalizedQuery());
        }
        if (StringUtils.hasText(source.getStageKey())) {
            target.setStageKey(source.getStageKey());
        }
        if (StringUtils.hasText(source.getChannelKey())) {
            target.setChannelKey(source.getChannelKey());
        }
        if (StringUtils.hasText(source.getTeachingType())) {
            target.setTeachingType(source.getTeachingType());
        }
        if (StringUtils.hasText(source.getSubject())) {
            target.setSubject(source.getSubject());
        }
        if (StringUtils.hasText(source.getSubjectKey())) {
            target.setSubjectKey(source.getSubjectKey());
        }
        if (StringUtils.hasText(source.getGradeName())) {
            target.setGradeName(source.getGradeName());
        }
        if (StringUtils.hasText(source.getModuleName())) {
            target.setModuleName(source.getModuleName());
        }
        if (StringUtils.hasText(source.getModuleKey())) {
            target.setModuleKey(source.getModuleKey());
        }
        if (StringUtils.hasText(source.getResourceTypeKey())) {
            target.setResourceTypeKey(source.getResourceTypeKey());
        }
        if (source.getMustTokens() != null && !source.getMustTokens().isEmpty()) {
            target.setMustTokens(source.getMustTokens());
        }
        if (StringUtils.hasText(source.getKeywordText())) {
            target.setKeywordText(source.getKeywordText());
        }
    }

    private record ScoredDoc(SysSearchDocument doc, SearchRecallCandidate candidate, double score) {
    }

    public List<SysSearchDocument> suggestTitles(ParsedSearchQuery parsed, boolean hideVip, int limit) {
        SearchDocumentSqlParam param = toSqlParam(parsed, hideVip, "score", 1, limit);
        return documentMapper.suggestByKind(param, List.of("resource"));
    }

    public List<SysSearchDocument> suggestSubjects(ParsedSearchQuery parsed, boolean hideVip, int limit) {
        SearchDocumentSqlParam param = toSqlParam(parsed, hideVip, "score", 1, limit);
        return documentMapper.suggestByKind(param, List.of("subject"));
    }

    public List<SysSearchDocument> suggestChannels(ParsedSearchQuery parsed, boolean hideVip, int limit) {
        SearchDocumentSqlParam param = toSqlParam(parsed, hideVip, "score", 1, limit);
        return documentMapper.suggestByKind(param, List.of("channel", "feature", "page"));
    }

    public List<SysSearchDocument> suggestNews(ParsedSearchQuery parsed, boolean hideVip, int limit) {
        SearchDocumentSqlParam param = toSqlParam(parsed, hideVip, "score", 1, limit);
        return documentMapper.suggestByKind(param, List.of("news", "page"));
    }

    public List<SysSearchDocument> suggestPrep(ParsedSearchQuery parsed, boolean hideVip, int limit) {
        SearchDocumentSqlParam param = toSqlParam(parsed, hideVip, "score", 1, limit);
        return documentMapper.suggestByKind(param, List.of("prep", "lesson"));
    }

    public SearchResultItemVO findExactTitle(ParsedSearchQuery parsed, String keyword, boolean hideVip) {
        if (!StringUtils.hasText(keyword)) return null;
        parsed.setRawQuery(keyword);
        parsed.setKeywordText(keyword);
        parsed.setMustTokens(List.of(keyword));
        SearchDocumentSqlParam param = toSqlParam(parsed, hideVip, "score", 1, 5);
        List<SysSearchDocument> rows = documentMapper.searchPage(param);
        for (SysSearchDocument row : rows) {
            if (keyword.equalsIgnoreCase(row.getTitle())) {
                return toResultItem(row, parsed, List.of(keyword), null);
            }
        }
        return null;
    }

    private SearchDocumentSqlParam toSqlParam(ParsedSearchQuery parsed, boolean hideVip, String sort, int page, int size) {
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
        if (StringUtils.hasText(parsed.getTeachingType())) {
            param.setTeachingTypeNames(lexiconService.expandTypeSynonyms(parsed.getTeachingType()));
        }
        param.setContentDomain(parsed.getContentDomain());
        if (StringUtils.hasText(parsed.getContentDomain())) {
            param.setDomainChannelKeys(SearchSiteCatalog.domainChannelKeys(parsed.getContentDomain()));
        }
        param.setHideVip(hideVip);
        param.setSort(StringUtils.hasText(sort) ? sort : "score");
        param.setOffset(Math.max(page - 1, 0) * size);
        param.setLimit(size);
        return param;
    }

    private SearchFacetsVO buildFacets(SearchDocumentSqlParam param) {
        SearchFacetsVO facets = new SearchFacetsVO();
        facets.setStages(toFacetBuckets(documentMapper.facetStages(param)));
        facets.setChannels(toFacetBuckets(documentMapper.facetChannels(param)));
        facets.setTypes(toFacetBuckets(documentMapper.facetTypes(param)));
        facets.setDomains(buildDomainFacets(param));
        return facets;
    }

    private List<SearchFacetBucketVO> buildDomainFacets(SearchDocumentSqlParam param) {
        List<SearchFacetBucketVO> list = new ArrayList<>();
        for (String domainKey : List.of("stage_resource", "feature", "prep", "news")) {
            SearchDocumentSqlParam domainParam = copyParamWithDomain(param, domainKey);
            long count = documentMapper.countSearch(domainParam);
            if (count <= 0) continue;
            SearchFacetBucketVO item = new SearchFacetBucketVO();
            item.setKey(domainKey);
            item.setName(SearchSiteCatalog.domainName(domainKey));
            item.setCount(Math.min(Integer.MAX_VALUE, count));
            list.add(item);
        }
        return list;
    }

    private SearchDocumentSqlParam copyParamWithDomain(SearchDocumentSqlParam source, String domain) {
        SearchDocumentSqlParam copy = new SearchDocumentSqlParam();
        copy.setMustTokens(source.getMustTokens());
        copy.setTokenClauses(source.getTokenClauses());
        copy.setRawKeyword(source.getRawKeyword());
        copy.setStageKey(source.getStageKey());
        copy.setChannelKey(source.getChannelKey());
        copy.setTeachingType(source.getTeachingType());
        copy.setSubject(source.getSubject());
        copy.setEditionName(source.getEditionName());
        copy.setGradeName(source.getGradeName());
        copy.setSoftGradeFilter(source.isSoftGradeFilter());
        copy.setModuleName(source.getModuleName());
        copy.setHideVip(source.isHideVip());
        copy.setSort(source.getSort());
        copy.setOffset(0);
        copy.setLimit(1);
        copy.setContentDomain(domain);
        copy.setDomainChannelKeys(SearchSiteCatalog.domainChannelKeys(domain));
        return copy;
    }

    private List<SearchFacetBucketVO> toFacetBuckets(List<SearchFacetRow> rows) {
        List<SearchFacetBucketVO> list = new ArrayList<>();
        if (rows == null) return list;
        for (SearchFacetRow row : rows) {
            SearchFacetBucketVO item = new SearchFacetBucketVO();
            item.setKey(row.getFacetKey());
            item.setName(StringUtils.hasText(row.getFacetName()) ? row.getFacetName() : row.getFacetKey());
            item.setCount(row.getFacetCount() == null ? 0 : row.getFacetCount());
            list.add(item);
        }
        return list;
    }

    private SearchResultItemVO toResultItem(
            SysSearchDocument r,
            ParsedSearchQuery parsed,
            List<String> highlightTerms,
            Double compositeScore) {
        SearchResultItemVO item = new SearchResultItemVO();
        item.setDocId(r.getDocId());
        item.setBizId(r.getBizId());
        item.setDocType(r.getDocType());
        item.setTitle(r.getTitle());
        item.setSubtitle(r.getSubtitle());
        item.setSummary(r.getSummary());
        item.setTitleHighlight(SearchTextHighlighter.highlight(r.getTitle(), highlightTerms));
        item.setSummaryHighlight(SearchTextHighlighter.snippet(
                StringUtils.hasText(r.getSummary()) ? r.getSummary() : r.getTitle(),
                highlightTerms, 120));
        item.setStageKey(r.getStageKey());
        item.setStageName(r.getStageName());
        item.setChannelKey(r.getChannelKey());
        item.setChannelName(r.getChannelName());
        item.setSubject(r.getSubjectName());
        item.setGradeName(r.getGradeName());
        item.setTeachingType(r.getResourceTypeName());
        item.setDownloadCount(r.getDownloadCount());
        item.setViewCount(r.getViewCount());
        item.setUploadTime(r.getPublishTime() == null ? null : r.getPublishTime().toString());
        item.setDetailRoute(r.getRoutePath());
        item.setCoverUrl(r.getCoverUrl());
        if ("resource".equals(r.getDocType()) && StringUtils.hasText(r.getBizId())) {
            try {
                item.setResourceId(Long.parseLong(r.getBizId()));
            } catch (NumberFormatException ignored) {
                // bizId 非数字时跳过
            }
            item.setResourceType(r.getDocType());
        }
        item.setContentDomain(SearchSiteCatalog.resolveContentDomain(r.getChannelKey()));
        if ("news".equals(r.getDocType())) {
            item.setContentDomain("news");
        } else if ("prep".equals(r.getDocType()) || "lesson".equals(r.getDocType())) {
            item.setContentDomain("prep");
        }
        if (compositeScore != null) {
            item.setScore(compositeScore);
        } else {
            item.setScore(SearchTextHighlighter.relevanceScore(
                    r.getTitle(), r.getSummary(), r.getResourceTypeName(),
                    parsed.getRawQuery(), highlightTerms,
                    r.getDownloadCount() == null ? 0 : r.getDownloadCount(),
                    r.getViewCount() == null ? 0 : r.getViewCount()));
        }
        return item;
    }
}
