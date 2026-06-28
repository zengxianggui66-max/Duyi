package com.k12.resource.search;



import com.k12.common.dto.SearchAllResultVO;

import com.k12.common.dto.SearchFacetBucketVO;

import com.k12.common.dto.SearchFacetsVO;

import com.k12.common.dto.SearchResultItemVO;

import com.k12.resource.entity.ResourceSearchIndex;

import com.k12.resource.mapper.ResourceSearchIndexMapper;

import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;



import java.util.ArrayList;

import java.util.List;



/**

 * 基于 resource_search_index 的单表检索 + SQL facets（P0+）

 */

@Service
public class SearchIndexQueryService {



    private final ResourceSearchIndexMapper indexMapper;

    private final SearchLexiconService lexiconService;
    public SearchIndexQueryService(ResourceSearchIndexMapper indexMapper, SearchLexiconService lexiconService) {
        this.indexMapper = indexMapper;
        this.lexiconService = lexiconService;
    }




    public boolean isIndexReady() {

        return indexMapper.selectCount(null) > 0;

    }



    public SearchAllResultVO search(

            ParsedSearchQuery parsed,

            int page,

            int size,

            String sort,

            boolean hideVip,

            List<String> highlightTerms) {

        long start = System.currentTimeMillis();

        SearchIndexSqlParam param = toSqlParam(parsed, hideVip, sort, page, size);



        long total = indexMapper.countSearch(param);

        List<ResourceSearchIndex> rows = total == 0 ? List.of() : indexMapper.searchPage(param);



        SearchAllResultVO data = new SearchAllResultVO();

        data.setRecords(rows.stream().map(r -> toResultItem(r, parsed, highlightTerms)).toList());

        data.setTotal((int) total);

        data.setPage(page);

        data.setSize(size);

        data.setPages(total == 0 ? 0 : (int) Math.ceil(total * 1.0 / size));

        data.setFacets(buildFacets(param));

        data.setCostMs((int) (System.currentTimeMillis() - start));

        return data;

    }



    public List<ResourceSearchIndex> suggestTitles(ParsedSearchQuery parsed, boolean hideVip, int limit) {

        SearchIndexSqlParam param = toSqlParam(parsed, hideVip, "score", 1, limit);

        return indexMapper.searchPage(param);

    }



    private SearchIndexSqlParam toSqlParam(ParsedSearchQuery parsed, boolean hideVip, String sort, int page, int size) {

        SearchIndexSqlParam param = new SearchIndexSqlParam();

        List<String> mustTokens = parsed.getMustTokens() == null ? List.of() : parsed.getMustTokens();

        param.setMustTokens(mustTokens);

        param.setTokenClauses(lexiconService.buildTokenClauses(mustTokens));

        param.setRawKeyword(StringUtils.hasText(parsed.getKeywordText()) ? parsed.getKeywordText() : parsed.getRawQuery());

        param.setStageKey(parsed.getStageKey());

        param.setChannelKey(parsed.getChannelKey());

        param.setTeachingType(parsed.getTeachingType());

        param.setSubject(parsed.getSubject());

        param.setEditionName(parsed.getEditionName());

        param.setGradeName(parsed.getGradeName());

        param.setModuleName(parsed.getModuleName());

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



    private SearchFacetsVO buildFacets(SearchIndexSqlParam param) {

        SearchFacetsVO facets = new SearchFacetsVO();

        facets.setStages(toFacetBuckets(indexMapper.facetStages(param)));

        facets.setChannels(toFacetBuckets(indexMapper.facetChannels(param)));

        facets.setTypes(toFacetBuckets(indexMapper.facetTypes(param)));

        facets.setDomains(buildDomainFacets(param));

        return facets;

    }



    private List<SearchFacetBucketVO> buildDomainFacets(SearchIndexSqlParam param) {

        List<SearchFacetBucketVO> list = new ArrayList<>();

        for (String domainKey : List.of("stage_resource", "feature", "prep", "news")) {

            SearchIndexSqlParam domainParam = copyParamWithDomain(param, domainKey);

            long count = indexMapper.countSearch(domainParam);

            if (count <= 0) continue;

            SearchFacetBucketVO item = new SearchFacetBucketVO();

            item.setKey(domainKey);

            item.setName(SearchSiteCatalog.domainName(domainKey));

            item.setCount(Math.min(Integer.MAX_VALUE, count));

            list.add(item);

        }

        return list;

    }



    private SearchIndexSqlParam copyParamWithDomain(SearchIndexSqlParam source, String domain) {

        SearchIndexSqlParam copy = new SearchIndexSqlParam();

        copy.setMustTokens(source.getMustTokens());

        copy.setTokenClauses(source.getTokenClauses());

        copy.setRawKeyword(source.getRawKeyword());

        copy.setStageKey(source.getStageKey());

        copy.setChannelKey(source.getChannelKey());

        copy.setTeachingType(source.getTeachingType());

        copy.setSubject(source.getSubject());

        copy.setEditionName(source.getEditionName());

        copy.setGradeName(source.getGradeName());

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



    private SearchResultItemVO toResultItem(ResourceSearchIndex r, ParsedSearchQuery parsed, List<String> highlightTerms) {

        SearchResultItemVO item = new SearchResultItemVO();

        item.setDocId(r.getDocId());

        item.setResourceId(r.getResourceId());

        item.setResourceType(r.getResourceType());

        item.setTitle(r.getTitle());

        item.setSummary(r.getSummary());

        item.setTitleHighlight(SearchTextHighlighter.highlight(r.getTitle(), highlightTerms));

        item.setSummaryHighlight(SearchTextHighlighter.snippet(

                StringUtils.hasText(r.getSummary()) ? r.getSummary() : r.getTitle(),

                highlightTerms,

                120));

        item.setStageKey(r.getStageKey());

        item.setStageName(r.getStageName());

        item.setChannelKey(r.getChannelKey());

        item.setChannelName(r.getChannelName());

        item.setSubject(r.getSubject());

        item.setGradeName(r.getGradeName());

        item.setTeachingType(r.getTeachingType());

        item.setFileExt(r.getFileExt());

        item.setDownloadCount(r.getDownloadCount());

        item.setViewCount(r.getViewCount());

        item.setUploadTime(r.getPublishTime() == null ? null : r.getPublishTime().toString());

        item.setDetailRoute(r.getDetailRoute());

        enrichResultItem(item);

        item.setScore(SearchTextHighlighter.relevanceScore(

                r.getTitle(),

                r.getSummary(),

                r.getTeachingType(),

                parsed.getRawQuery(),

                highlightTerms,

                r.getDownloadCount() == null ? 0 : r.getDownloadCount(),

                r.getViewCount() == null ? 0 : r.getViewCount()));

        return item;

    }



    private void enrichResultItem(SearchResultItemVO item) {

        String channelKey = item.getChannelKey();

        item.setContentDomain(SearchSiteCatalog.resolveContentDomain(channelKey));

        item.setDocType(SearchSiteCatalog.resolveDocType(channelKey, item.getResourceType()));

        item.setBizId(item.getResourceId() == null ? null : String.valueOf(item.getResourceId()));

        if (!StringUtils.hasText(item.getSubtitle())) {

            StringBuilder subtitle = new StringBuilder();

            if (StringUtils.hasText(item.getStageName())) subtitle.append(item.getStageName());

            if (StringUtils.hasText(item.getSubject())) {

                if (!subtitle.isEmpty()) subtitle.append(" / ");

                subtitle.append(item.getSubject());

            }

            if (StringUtils.hasText(item.getTeachingType())) {

                if (!subtitle.isEmpty()) subtitle.append(" / ");

                subtitle.append(item.getTeachingType());

            }

            if (!subtitle.isEmpty()) item.setSubtitle(subtitle.toString());

        }

    }

}

