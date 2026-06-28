package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.k12.common.dto.HomeHotWordVO;
import com.k12.common.dto.SearchAllResultVO;
import com.k12.common.dto.SearchClickRequest;
import com.k12.common.dto.SearchFacetBucketVO;
import com.k12.common.dto.SearchFacetsVO;
import com.k12.common.dto.SearchRedirectVO;
import com.k12.common.dto.SearchResultItemVO;
import com.k12.common.dto.SearchStatsVO;
import com.k12.common.dto.SearchSuggestItemVO;
import com.k12.common.entity.CompetitionResource;
import com.k12.common.entity.CultureResource;
import com.k12.common.entity.PrimaryChineseResource;
import com.k12.common.entity.TopicResource;
import com.k12.resource.config.SearchProperties;
import com.k12.resource.search.ParsedSearchQuery;
import com.k12.resource.search.SearchAnalyticsService;
import com.k12.resource.search.SearchP3ReadinessService;
import com.k12.resource.search.SearchCacheService;
import com.k12.resource.search.SearchClickLogService;
import com.k12.resource.search.SearchGuardService;
import com.k12.resource.search.SearchIntentRecognizer;
import com.k12.resource.search.SearchIndexSyncService;
import com.k12.resource.search.SearchIndexQueryService;
import com.k12.resource.search.SearchLexiconService;
import com.k12.resource.search.SearchLogService;
import com.k12.resource.search.SearchQueryParser;
import com.k12.resource.search.SearchSiteCatalog;
import com.k12.resource.search.SearchTextHighlighter;
import com.k12.resource.search.SearchDocumentQueryService;
import com.k12.resource.search.SearchDocumentSyncService;
import com.k12.resource.search.engine.SearchEngineHealthService;
import com.k12.resource.search.engine.SearchEngineShadowService;
import com.k12.resource.search.engine.SearchEngineSyncService;
import com.k12.resource.search.SearchRedirectResolver;
import com.k12.resource.entity.ResourceSearchIndex;
import com.k12.resource.entity.SysSearchDocument;
import com.k12.resource.entity.SearchHotKeyword;
import com.k12.resource.entity.SearchHistory;
import com.k12.resource.mapper.CompetitionResourceMapper;
import com.k12.resource.mapper.CultureResourceMapper;
import com.k12.resource.mapper.PrimaryChineseResourceMapper;
import com.k12.resource.mapper.SearchHotKeywordMapper;
import com.k12.resource.mapper.SearchHistoryMapper;
import com.k12.resource.mapper.TopicResourceMapper;
import com.k12.resource.service.HomeOpsService;
import com.k12.resource.service.SearchService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 搜索服务实现
 */
@Service
@SuppressWarnings("null")
public class SearchServiceImpl implements SearchService {
    private static final int SEARCH_FETCH_LIMIT = 120;
    private static final int SUGGEST_TOTAL_LIMIT = 12;
    private static final int SUGGEST_TITLE_LIMIT = 5;
    private static final int SUGGEST_CHANNEL_LIMIT = 3;
    private static final int SUGGEST_HISTORY_LIMIT = 5;
    private static final int SUGGEST_HOT_LIMIT = 5;
    private static final List<ChannelSuggestDef> CHANNEL_SUGGESTIONS = List.of(
            new ChannelSuggestDef("主题班会", List.of("班会", "德育", "成长"), "/theme-class-meeting"),
            new ChannelSuggestDef("生涯规划", List.of("生涯", "志愿", "选课", "升学"), "/feature/shengya"),
            new ChannelSuggestDef("传统文化", List.of("传统文化", "国学", "诗词", "书法", "研学"), "/culture"),
            new ChannelSuggestDef("竞赛专区", List.of("竞赛", "奥赛", "奥数", "信息学", "作文"), "/competition"),
            new ChannelSuggestDef("专题资源", List.of("专题", "备考", "寒暑假", "期中", "期末", "冲刺"), "/topic")
    );

    private final SearchHotKeywordMapper hotKeywordMapper;
    private final SearchHistoryMapper searchHistoryMapper;
    private final PrimaryChineseResourceMapper primaryChineseResourceMapper;
    private final TopicResourceMapper topicResourceMapper;
    private final CompetitionResourceMapper competitionResourceMapper;
    private final CultureResourceMapper cultureResourceMapper;
    private final SearchQueryParser queryParser;
    private final SearchIntentRecognizer intentRecognizer;
    private final SearchGuardService guardService;
    private final SearchIndexQueryService indexQueryService;
    private final SearchDocumentQueryService documentQueryService;
    private final SearchIndexSyncService indexSyncService;
    private final SearchDocumentSyncService documentSyncService;
    private final SearchRedirectResolver redirectResolver;
    private final SearchProperties searchProperties;
    private final SearchCacheService searchCacheService;
    private final SearchLogService searchLogService;
    private final SearchLexiconService lexiconService;
    private final SearchClickLogService searchClickLogService;
    private final SearchAnalyticsService searchAnalyticsService;
    private final SearchP3ReadinessService searchP3ReadinessService;
    private final SearchEngineShadowService searchEngineShadowService;
    private final SearchEngineSyncService searchEngineSyncService;
    private final SearchEngineHealthService searchEngineHealthService;
    private final HomeOpsService homeOpsService;
    public SearchServiceImpl(SearchHotKeywordMapper hotKeywordMapper, SearchHistoryMapper searchHistoryMapper, PrimaryChineseResourceMapper primaryChineseResourceMapper, TopicResourceMapper topicResourceMapper, CompetitionResourceMapper competitionResourceMapper, CultureResourceMapper cultureResourceMapper, SearchQueryParser queryParser, SearchIntentRecognizer intentRecognizer, SearchGuardService guardService, SearchIndexQueryService indexQueryService, SearchDocumentQueryService documentQueryService, SearchIndexSyncService indexSyncService, SearchDocumentSyncService documentSyncService, SearchRedirectResolver redirectResolver, SearchProperties searchProperties, SearchCacheService searchCacheService, SearchLogService searchLogService, SearchLexiconService lexiconService, SearchClickLogService searchClickLogService, SearchAnalyticsService searchAnalyticsService, SearchP3ReadinessService searchP3ReadinessService, SearchEngineShadowService searchEngineShadowService, SearchEngineSyncService searchEngineSyncService, SearchEngineHealthService searchEngineHealthService, HomeOpsService homeOpsService) {
        this.hotKeywordMapper = hotKeywordMapper;
        this.searchHistoryMapper = searchHistoryMapper;
        this.primaryChineseResourceMapper = primaryChineseResourceMapper;
        this.topicResourceMapper = topicResourceMapper;
        this.competitionResourceMapper = competitionResourceMapper;
        this.cultureResourceMapper = cultureResourceMapper;
        this.queryParser = queryParser;
        this.intentRecognizer = intentRecognizer;
        this.guardService = guardService;
        this.indexQueryService = indexQueryService;
        this.documentQueryService = documentQueryService;
        this.indexSyncService = indexSyncService;
        this.documentSyncService = documentSyncService;
        this.redirectResolver = redirectResolver;
        this.searchProperties = searchProperties;
        this.searchCacheService = searchCacheService;
        this.searchLogService = searchLogService;
        this.lexiconService = lexiconService;
        this.searchClickLogService = searchClickLogService;
        this.searchAnalyticsService = searchAnalyticsService;
        this.searchP3ReadinessService = searchP3ReadinessService;
        this.searchEngineShadowService = searchEngineShadowService;
        this.searchEngineSyncService = searchEngineSyncService;
        this.searchEngineHealthService = searchEngineHealthService;
        this.homeOpsService = homeOpsService;
    }


    @Override
    public List<Map<String, Object>> getHotKeywords(Integer limit) {
        int safeLimit = limit == null || limit < 1 ? 10 : Math.min(limit, 50);
        String cacheKey = "hot:" + safeLimit;
        return searchCacheService.getOrLoad(
                cacheKey,
                searchProperties.getCache().getHotTtlSeconds(),
                () -> loadHotKeywords(safeLimit));
    }

    private List<Map<String, Object>> loadHotKeywords(int limit) {
        LambdaQueryWrapper<SearchHotKeyword> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHotKeyword::getStatus, 1)
               .eq(SearchHotKeyword::getDeleted, 0);

        List<SearchHotKeyword> keywords = hotKeywordMapper.selectList(wrapper);
        keywords.sort(Comparator
                .comparingInt(this::hotKeywordEffectiveScore).reversed()
                .thenComparing(SearchHotKeyword::getSearchCount, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(SearchHotKeyword::getId));

        List<SearchHotKeyword> top = keywords.stream().limit(limit).toList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < top.size(); i++) {
            SearchHotKeyword k = top.get(i);
            Map<String, Object> map = new HashMap<>();
            map.put("keyword", k.getKeyword());
            map.put("searchCount", k.getSearchCount());
            map.put("boostScore", k.getBoostScore() == null ? 0 : k.getBoostScore());
            map.put("rank", i + 1);
            result.add(map);
        }
        return result;
    }

    private int hotKeywordEffectiveScore(SearchHotKeyword row) {
        int count = row.getSearchCount() == null ? 0 : row.getSearchCount();
        int boost = row.getBoostScore() == null ? 0 : row.getBoostScore();
        return count + boost;
    }

    @Override
    public void recordSearchKeyword(String keyword, Long userId) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        String trimmedKeyword = keyword.trim();

        // 先查询是否存在
        SearchHotKeyword existing = hotKeywordMapper.selectOne(
            new LambdaQueryWrapper<SearchHotKeyword>()
                .eq(SearchHotKeyword::getKeyword, trimmedKeyword)
                .eq(SearchHotKeyword::getDeleted, 0)
        );

        if (existing != null) {
            // 存在则更新热搜次数（使用原生SQL自增）
            hotKeywordMapper.incrementSearchCount(existing.getId());
        } else {
            // 不存在则新增
            SearchHotKeyword newKeyword = new SearchHotKeyword();
            newKeyword.setKeyword(trimmedKeyword);
            newKeyword.setSearchCount(1);
            newKeyword.setStatus(1);
            newKeyword.setDeleted(0);
            hotKeywordMapper.insert(newKeyword);
        }
        searchCacheService.invalidatePrefix("hot:");

        // 记录用户搜索历史
        if (userId != null && userId > 0) {
            SearchHistory history = new SearchHistory();
            history.setUserId(userId);
            history.setKeyword(trimmedKeyword);
            history.setSearchType("resource");
            history.setStatus(1);
            history.setDeleted(0);
            searchHistoryMapper.insert(history);
        }
    }

    @Override
    public List<String> getSearchHistory(Long userId, Integer limit) {
        if (userId == null || userId <= 0) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<SearchHistory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHistory::getUserId, userId)
               .eq(SearchHistory::getStatus, 1)
               .eq(SearchHistory::getDeleted, 0)
               .orderByDesc(SearchHistory::getCreateTime)
               .last("LIMIT " + limit);

        List<SearchHistory> histories = searchHistoryMapper.selectList(wrapper);

        // 去重并返回关键词列表
        return histories.stream()
                .map(SearchHistory::getKeyword)
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public void clearSearchHistory(Long userId) {
        if (userId == null || userId <= 0) {
            return;
        }

        LambdaUpdateWrapper<SearchHistory> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SearchHistory::getUserId, userId)
               .set(SearchHistory::getDeleted, 1);
        searchHistoryMapper.update(null, wrapper);
    }

    @Override
    public SearchAllResultVO searchAll(
            String q,
            Integer page,
            Integer size,
            String stage,
            String channel,
            String type,
            String domain,
            String sort,
            Long userId,
            String clientKey,
            String searchEngine) {
        long startMs = System.currentTimeMillis();
        String keyword = normalizeKeyword(q);
        int safePage = page == null || page < 1 ? 1 : page;
        int safeSize = size == null || size < 1 ? 20 : Math.min(size, 50);
        String safeSort = StringUtils.hasText(sort) ? sort : "score";
        if (!StringUtils.hasText(keyword)
                && !StringUtils.hasText(stage)
                && !StringUtils.hasText(channel)
                && !StringUtils.hasText(type)
                && !StringUtils.hasText(domain)) {
            return emptySearchResult(safePage, safeSize);
        }

        String rateKey = userId != null && userId > 0 ? "u:" + userId : StringUtils.hasText(clientKey) ? clientKey : "anonymous";
        SearchGuardService.GuardResult guard = guardService.checkAllowed(rateKey, keyword);
        if (!guard.allowed()) {
            SearchAllResultVO blocked = emptySearchResult(safePage, safeSize);
            blocked.setQueryHint(guard.message());
            logSearchQuery(userId, clientKey, keyword, blocked, safePage, stage, channel, type, domain, safeSort, guard.code(), startMs, null, "all");
            return blocked;
        }

        ParsedSearchQuery parsed = queryParser.mergeFilters(
                intentRecognizer.recognize(StringUtils.hasText(keyword) ? keyword : ""),
                stage, channel, type, domain);
        if (StringUtils.hasText(keyword) && parsed.getMustTokens().isEmpty() && !hasParsedFilters(parsed)) {
            SearchAllResultVO empty = emptySearchResult(safePage, safeSize);
            attachZeroRecommendations(empty);
            attachSearchIntents(empty, keyword);
            attachCatalogResults(empty, keyword);
            logSearchQuery(userId, clientKey, keyword, empty, safePage, stage, channel, type, domain, safeSort, null, startMs, parsed, "all");
            return empty;
        }
        List<String> highlightTerms = buildHighlightTerms(keyword, parsed);

        String cacheKey = buildSearchCacheKey(keyword, safePage, safeSize, stage, channel, type, domain, safeSort, userId, searchEngine);
        SearchAllResultVO data;
        if (safePage == 1) {
            data = searchCacheService.getOrLoad(
                    cacheKey,
                    searchProperties.getCache().getSearchTtlSeconds(),
                    () -> executeSearch(keyword, safePage, safeSize, parsed, safeSort, highlightTerms, userId, clientKey, searchEngine));
        } else {
            data = executeSearch(keyword, safePage, safeSize, parsed, safeSort, highlightTerms, userId, clientKey, searchEngine);
        }

        if (safePage == 1 && StringUtils.hasText(keyword)) {
            recordSearchKeyword(keyword, userId);
        }
        attachZeroRecommendations(data);
        attachSearchIntents(data, keyword);
        attachCatalogResults(data, keyword);
        int mysqlCostMs = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - startMs);
        logSearchQuery(userId, clientKey, keyword, data, safePage, stage, channel, type, domain, safeSort, null, startMs, parsed, "all");
        if (searchEngineShadowService.shouldShadow() && !searchEngineShadowService.shouldServeEngine(clientKey)) {
            searchEngineShadowService.compareAllAsync(keyword, parsed, data, mysqlCostMs);
        }
        return data;
    }

    private void attachCatalogResults(SearchAllResultVO data, String keyword) {
        if (data == null || !StringUtils.hasText(keyword)) return;
        if (documentQueryService.isIndexReady()) return;
        if (data.getTotal() != null && data.getTotal() > 0) return;
        List<SearchResultItemVO> catalogRows = SearchSiteCatalog.buildCatalogResults(keyword, 6);
        if (catalogRows.isEmpty()) return;
        data.setRecords(catalogRows);
        data.setTotal(catalogRows.size());
        data.setPages(data.getTotal() == 0 ? 0 : 1);
        data.setFacets(buildFacets(catalogRows));
    }

    private SearchAllResultVO executeSearch(
            String keyword,
            int safePage,
            int safeSize,
            ParsedSearchQuery parsed,
            String sort,
            List<String> highlightTerms,
            Long userId,
            String clientKey,
            String searchEngine) {
        boolean hideVip = guardService.shouldHideVip(userId);
        boolean useEngine = searchEngineShadowService.preferEngineForAuto(searchEngine)
                || searchEngineShadowService.shouldServeEngine(clientKey);
        if (useEngine) {
            try {
                SearchAllResultVO engineResult = searchEngineShadowService.buildResultFromEngine(
                        parsed, safePage, safeSize, hideVip, highlightTerms);
                if (engineResult != null) {
                    return engineResult;
                }
            } catch (Exception ignored) {
                if (!searchProperties.getEngine().isFallbackToMysql()) {
                    throw ignored;
                }
            }
        }
        if (documentQueryService.isIndexReady()) {
            return documentQueryService.search(parsed, safePage, safeSize, sort, hideVip, highlightTerms);
        }
        if (indexQueryService.isIndexReady()) {
            return indexQueryService.search(
                    parsed, safePage, safeSize, sort, hideVip, highlightTerms);
        }
        return searchAllLegacy(keyword, safePage, safeSize, parsed, sort, highlightTerms, hideVip);
    }

    private String buildSearchCacheKey(
            String keyword,
            int page,
            int size,
            String stage,
            String channel,
            String type,
            String domain,
            String sort,
            Long userId,
            String searchEngine) {
        return "search:"
                + Objects.toString(keyword, "")
                + "|p=" + page
                + "|s=" + size
                + "|st=" + Objects.toString(stage, "")
                + "|ch=" + Objects.toString(channel, "")
                + "|ty=" + Objects.toString(type, "")
                + "|dm=" + Objects.toString(domain, "")
                + "|so=" + Objects.toString(sort, "")
                + "|u=" + (userId == null ? 0 : userId)
                + "|eng=" + Objects.toString(searchEngine, "");
    }

    private void attachZeroRecommendations(SearchAllResultVO data) {
        if (data == null || data.getTotal() == null || data.getTotal() > 0) {
            return;
        }
        data.setRecommendations(buildZeroRecommendations());
    }

    private List<SearchSuggestItemVO> buildZeroRecommendations() {
        List<SearchSuggestItemVO> out = new ArrayList<>();
        Set<String> dedup = new LinkedHashSet<>();
        appendHotSuggestions(out, dedup, 6, null);
        appendChannelRecommendations(out, dedup, 4);
        for (SearchSuggestItemVO item : SearchSiteCatalog.buildZeroRecommendations()) {
            addUniqueSuggest(out, dedup, item);
        }
        return out;
    }

    private void attachSearchIntents(SearchAllResultVO data, String keyword) {
        if (data == null || !StringUtils.hasText(keyword)) return;
        List<SearchSuggestItemVO> intents = SearchSiteCatalog.buildCatalogIntents(keyword);
        if (!intents.isEmpty()) {
            data.setIntents(intents.stream().limit(6).toList());
        }
    }

    private void appendChannelRecommendations(List<SearchSuggestItemVO> out, Set<String> dedup, int maxCount) {
        int added = 0;
        for (ChannelSuggestDef item : CHANNEL_SUGGESTIONS) {
            if (added >= maxCount) break;
            SearchSuggestItemVO s = new SearchSuggestItemVO();
            s.setKind("channel");
            s.setText(item.name());
            s.setDetailRoute(item.detailRoute());
            if (addUniqueSuggest(out, dedup, s)) {
                added++;
            }
        }
    }

    private void logSearchQuery(
            Long userId,
            String clientKey,
            String keyword,
            SearchAllResultVO data,
            int page,
            String stage,
            String channel,
            String type,
            String domain,
            String sort,
            String blockedCode,
            long startMs,
            ParsedSearchQuery parsed,
            String apiPath) {
        if (!StringUtils.hasText(keyword)) {
            return;
        }
        int hitCount = data == null || data.getTotal() == null ? 0 : data.getTotal();
        int costMs = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - startMs);
        String normalized = parsed != null ? parsed.getNormalizedQuery() : null;
        String intentJson = parsed != null ? toIntentJson(intentRecognizer.toIntentVo(parsed)) : null;
        String contentDomain = resolveLogContentDomain(domain, parsed, data);
        searchLogService.logQuery(
                userId, clientKey, keyword, normalized, intentJson, contentDomain,
                hitCount, page, stage, channel, type, sort, blockedCode, costMs, apiPath);
    }

    private String resolveLogContentDomain(String domain, ParsedSearchQuery parsed, SearchAllResultVO data) {
        if (StringUtils.hasText(domain)) {
            return domain.trim();
        }
        if (parsed != null && StringUtils.hasText(parsed.getContentDomain())) {
            return parsed.getContentDomain();
        }
        if (data != null && data.getParsedIntent() != null && StringUtils.hasText(data.getParsedIntent().getContentDomain())) {
            return data.getParsedIntent().getContentDomain();
        }
        return null;
    }

    private String toIntentJson(com.k12.common.dto.SearchParsedIntentVO intent) {
        if (intent == null) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        addJsonPart(parts, "stage", intent.getStage());
        addJsonPart(parts, "subject", intent.getSubject());
        addJsonPart(parts, "grade", intent.getGrade());
        addJsonPart(parts, "resourceType", intent.getResourceType());
        addJsonPart(parts, "module", intent.getModule());
        addJsonPart(parts, "channel", intent.getChannel());
        if (parts.isEmpty()) {
            return null;
        }
        return "{" + String.join(",", parts) + "}";
    }

    private void addJsonPart(List<String> parts, String key, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        parts.add("\"" + key + "\":\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"");
    }

    private SearchAllResultVO searchAllLegacy(
            String keyword,
            int safePage,
            int safeSize,
            ParsedSearchQuery parsed,
            String sort,
            List<String> highlightTerms,
            boolean hideVip) {
        if (!StringUtils.hasText(keyword) && !hasParsedFilters(parsed)) {
            return emptySearchResult(safePage, safeSize);
        }

        List<String> searchTerms = StringUtils.hasText(keyword)
                ? buildSearchTerms(keyword)
                : List.of();
        Map<String, SearchResultItemVO> merged = new LinkedHashMap<>();
        if (searchTerms.isEmpty()) {
            mergeDocs(merged, searchPrimaryChinese("", highlightTerms));
            mergeDocs(merged, searchTopic("", highlightTerms));
            mergeDocs(merged, searchCompetition("", highlightTerms));
            mergeDocs(merged, searchCulture("", highlightTerms));
        } else {
            for (String term : searchTerms) {
                mergeDocs(merged, searchPrimaryChinese(term, highlightTerms));
                mergeDocs(merged, searchTopic(term, highlightTerms));
                mergeDocs(merged, searchCompetition(term, highlightTerms));
                mergeDocs(merged, searchCulture(term, highlightTerms));
            }
        }
        List<SearchResultItemVO> all = new ArrayList<>(merged.values());

        List<SearchResultItemVO> filtered = all.stream()
                .filter(item -> matchParsedFilter(item, parsed))
                .filter(item -> !hideVip || !isVipResult(item))
                .collect(Collectors.toList());

        sortResults(filtered, sort);

        int total = filtered.size();
        int from = Math.max((safePage - 1) * safeSize, 0);
        int to = Math.min(from + safeSize, total);
        List<SearchResultItemVO> records = from >= total ? List.of() : filtered.subList(from, to);

        SearchAllResultVO data = new SearchAllResultVO();
        data.setRecords(records);
        data.setTotal(total);
        data.setPage(safePage);
        data.setSize(safeSize);
        data.setPages(total == 0 ? 0 : (int) Math.ceil(total * 1.0 / safeSize));
        data.setFacets(buildFacets(filtered));
        data.setCostMs(0);
        return data;
    }

    private boolean hasParsedFilters(ParsedSearchQuery parsed) {
        return StringUtils.hasText(parsed.getStageKey())
                || StringUtils.hasText(parsed.getChannelKey())
                || StringUtils.hasText(parsed.getTeachingType())
                || StringUtils.hasText(parsed.getSubject())
                || StringUtils.hasText(parsed.getEditionName())
                || StringUtils.hasText(parsed.getGradeName())
                || StringUtils.hasText(parsed.getModuleName())
                || StringUtils.hasText(parsed.getContentDomain());
    }

    private boolean matchParsedFilter(SearchResultItemVO item, ParsedSearchQuery parsed) {
        if (StringUtils.hasText(parsed.getStageKey())
                && !Objects.equals(asString(item.getStageKey()), parsed.getStageKey())) {
            return false;
        }
        if (StringUtils.hasText(parsed.getChannelKey())
                && !Objects.equals(asString(item.getChannelKey()), parsed.getChannelKey())) {
            return false;
        }
        if (StringUtils.hasText(parsed.getTeachingType())
                && !Objects.equals(asString(item.getTeachingType()), parsed.getTeachingType())) {
            return false;
        }
        if (StringUtils.hasText(parsed.getSubject())
                && !Objects.equals(asString(item.getSubject()), parsed.getSubject())) {
            return false;
        }
        if (StringUtils.hasText(parsed.getContentDomain())
                && !Objects.equals(asString(item.getContentDomain()), parsed.getContentDomain())) {
            return false;
        }
        return true;
    }

    private boolean isVipResult(SearchResultItemVO item) {
        return item.getBadges() != null && item.getBadges().contains("VIP");
    }

    private List<String> buildHighlightTerms(String keyword, ParsedSearchQuery parsed) {
        return lexiconService.buildHighlightTerms(keyword, parsed.getMustTokens());
    }

    private List<String> buildSearchTerms(String keyword) {
        return lexiconService.buildSearchTerms(keyword);
    }

    @Override
    public int rebuildSearchIndex() {
        int docCount = documentSyncService.rebuildAll();
        indexSyncService.rebuildAll();
        return docCount;
    }

    @Override
    public Map<String, Object> searchEngineHealth() {
        return searchEngineHealthService.health();
    }

    @Override
    public int syncSearchEngine() {
        return searchEngineSyncService.syncFull();
    }

    @Override
    public List<SearchSuggestItemVO> suggest(String q, Integer limit, Long userId) {
        long startMs = System.currentTimeMillis();
        String keyword = normalizeKeyword(q);
        int safeLimit = limit == null || limit < 1 ? 10 : Math.min(limit, SUGGEST_TOTAL_LIMIT);
        SearchGuardService.GuardResult guard = guardService.checkSuggestAllowed(keyword);
        if (!guard.allowed()) {
            return List.of();
        }

        String cacheKey = "suggest:"
                + Objects.toString(keyword, "")
                + "|l=" + safeLimit
                + "|u=" + (userId == null ? 0 : userId);
        List<SearchSuggestItemVO> result = searchCacheService.getOrLoad(
                cacheKey,
                searchProperties.getCache().getSuggestTtlSeconds(),
                () -> buildSuggest(keyword, safeLimit, userId));
        if (StringUtils.hasText(keyword)) {
            int costMs = (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - startMs);
            searchLogService.logQuery(
                    userId, null, keyword, keyword, null, null,
                    result.size(), 1, null, null, null, null, null, costMs, "suggest");
        }
        return result;
    }

    private List<SearchSuggestItemVO> buildSuggest(String keyword, int safeLimit, Long userId) {
        List<SearchSuggestItemVO> result = new ArrayList<>();
        Set<String> dedup = new LinkedHashSet<>();
        if (!StringUtils.hasText(keyword)) {
            appendHistorySuggestions(result, dedup, userId, Math.min(safeLimit, SUGGEST_HISTORY_LIMIT));
            appendHotSuggestions(result, dedup, safeLimit, null);
            return result;
        }

        appendSubjectSuggestions(result, dedup, keyword, 3);
        appendCatalogSuggestions(result, dedup, keyword, safeLimit);
        if (documentQueryService.isIndexReady()) {
            appendDocumentSuggestions(result, dedup, keyword, safeLimit, userId);
        } else {
            appendTitleSuggestions(result, dedup, keyword, Math.min(safeLimit, SUGGEST_TITLE_LIMIT));
            appendChannelSuggestions(result, dedup, keyword, Math.min(SUGGEST_CHANNEL_LIMIT, safeLimit - result.size()));
        }
        appendHotSuggestions(result, dedup, safeLimit, keyword);
        return result;
    }

    private void appendDocumentSuggestions(
            List<SearchSuggestItemVO> out,
            Set<String> dedup,
            String keyword,
            int safeLimit,
            Long userId) {
        if (!StringUtils.hasText(keyword) || out.size() >= safeLimit) return;
        ParsedSearchQuery parsed = intentRecognizer.recognize(keyword);
        List<String> terms = buildHighlightTerms(keyword, parsed);
        boolean hideVip = guardService.shouldHideVip(userId);

        appendDocRows(out, dedup, documentQueryService.suggestSubjects(parsed, hideVip, 3), terms, "subject", safeLimit);
        appendDocRows(out, dedup, documentQueryService.suggestChannels(parsed, hideVip, 2), terms, "channel", safeLimit);
        appendDocRows(out, dedup, documentQueryService.suggestPrep(parsed, hideVip, 2), terms, "prep", safeLimit);
        appendDocRows(out, dedup, documentQueryService.suggestNews(parsed, hideVip, 2), terms, "news", safeLimit);
        appendDocRows(out, dedup, documentQueryService.suggestTitles(parsed, hideVip, SUGGEST_TITLE_LIMIT), terms, "title", safeLimit);
    }

    private void appendDocRows(
            List<SearchSuggestItemVO> out,
            Set<String> dedup,
            List<SysSearchDocument> rows,
            List<String> terms,
            String kind,
            int safeLimit) {
        if (rows == null) return;
        for (SysSearchDocument row : rows) {
            if (out.size() >= SUGGEST_TOTAL_LIMIT || out.size() >= safeLimit) return;
            SearchSuggestItemVO s = new SearchSuggestItemVO();
            s.setKind(kind);
            s.setText(row.getTitle());
            s.setSubtitle(row.getSubtitle());
            s.setDetailRoute(row.getRoutePath());
            if ("resource".equals(row.getDocType()) && StringUtils.hasText(row.getBizId())) {
                try {
                    s.setResourceId(Long.parseLong(row.getBizId()));
                } catch (NumberFormatException ignored) {
                    // bizId 非数字时跳过
                }
                if (row.getDocId() != null && row.getDocId().contains(":")) {
                    s.setResourceType(row.getDocId().substring(0, row.getDocId().indexOf(':')));
                }
            }
            s.setContentDomain(SearchSiteCatalog.resolveContentDomain(row.getChannelKey()));
            if ("news".equals(row.getDocType())) {
                s.setContentDomain("news");
            }
            s.setHighlight(SearchTextHighlighter.highlight(row.getTitle(), terms));
            addUniqueSuggest(out, dedup, s);
        }
    }

    private void appendSubjectSuggestions(List<SearchSuggestItemVO> out, Set<String> dedup, String keyword, int maxCount) {
        for (SearchSuggestItemVO item : SearchSiteCatalog.matchSubjectSuggestions(keyword, maxCount)) {
            if (out.size() >= SUGGEST_TOTAL_LIMIT) break;
            addUniqueSuggest(out, dedup, item);
        }
    }

    private void appendCatalogSuggestions(List<SearchSuggestItemVO> out, Set<String> dedup, String keyword, int safeLimit) {
        appendSuggestList(out, dedup, SearchSiteCatalog.matchFeatureSuggestions(keyword, 2), safeLimit);
        appendSuggestList(out, dedup, SearchSiteCatalog.matchPrepSuggestions(keyword, 2), safeLimit);
        appendSuggestList(out, dedup, SearchSiteCatalog.matchNewsSuggestions(keyword, 2), safeLimit);
        appendSuggestList(out, dedup, SearchSiteCatalog.matchResourceTypeSuggestions(keyword, 2), safeLimit);
    }

    private void appendSuggestList(
            List<SearchSuggestItemVO> out,
            Set<String> dedup,
            List<SearchSuggestItemVO> items,
            int safeLimit) {
        for (SearchSuggestItemVO item : items) {
            if (out.size() >= safeLimit || out.size() >= SUGGEST_TOTAL_LIMIT) return;
            addUniqueSuggest(out, dedup, item);
        }
    }

    private void appendTitleSuggestions(List<SearchSuggestItemVO> out, Set<String> dedup, String keyword, int maxCount) {
        if (maxCount <= 0) return;
        ParsedSearchQuery parsed = intentRecognizer.recognize(keyword);
        List<String> terms = buildHighlightTerms(keyword, parsed);

        if (indexQueryService.isIndexReady()) {
            List<ResourceSearchIndex> rows = indexQueryService.suggestTitles(parsed, false, maxCount);
            for (ResourceSearchIndex item : rows) {
                SearchSuggestItemVO s = new SearchSuggestItemVO();
                s.setText(item.getTitle());
                s.setKind("title");
                s.setResourceType(item.getResourceType());
                s.setResourceId(item.getResourceId());
                s.setDetailRoute(item.getDetailRoute());
                s.setHighlight(SearchTextHighlighter.highlight(item.getTitle(), terms));
                addUniqueSuggest(out, dedup, s);
            }
            return;
        }

        Map<String, SearchResultItemVO> merged = new LinkedHashMap<>();
        for (String term : terms) {
            mergeDocs(merged, searchPrimaryChinese(term, terms));
            mergeDocs(merged, searchTopic(term, terms));
            mergeDocs(merged, searchCompetition(term, terms));
            mergeDocs(merged, searchCulture(term, terms));
        }
        String kw = keyword.toLowerCase(Locale.ROOT);
        merged.values().stream()
                .filter(item -> StringUtils.hasText(item.getTitle()) &&
                        item.getTitle().toLowerCase(Locale.ROOT).contains(kw))
                .sorted(Comparator
                        .comparingDouble((SearchResultItemVO item) -> titleSuggestScore(item, keyword)).reversed()
                        .thenComparing(SearchResultItemVO::getTitle))
                .limit(maxCount)
                .forEach(item -> {
                    SearchSuggestItemVO s = new SearchSuggestItemVO();
                    s.setText(item.getTitle());
                    s.setKind("title");
                    s.setResourceType(item.getResourceType());
                    s.setResourceId(item.getResourceId());
                    s.setDetailRoute(item.getDetailRoute());
                    s.setHighlight(SearchTextHighlighter.highlight(item.getTitle(), terms));
                    addUniqueSuggest(out, dedup, s);
                });
    }

    private double titleSuggestScore(SearchResultItemVO item, String keyword) {
        String title = asString(item.getTitle()).toLowerCase(Locale.ROOT);
        String kw = keyword.toLowerCase(Locale.ROOT);
        double score = 0;
        if (title.startsWith(kw)) {
            score += 200;
        } else if (title.contains(kw)) {
            score += 120;
        }
        score += (item.getScore() == null ? 0 : item.getScore()) * 8;
        score += Math.log1p(nvl(item.getDownloadCount())) + Math.log1p(nvl(item.getViewCount())) * 0.5;
        return score;
    }

    private void appendChannelSuggestions(List<SearchSuggestItemVO> out, Set<String> dedup, String keyword, int maxCount) {
        if (maxCount <= 0 || !StringUtils.hasText(keyword)) return;
        List<String> terms = buildSearchTerms(keyword);
        for (ChannelSuggestDef item : CHANNEL_SUGGESTIONS) {
            if (out.size() >= SUGGEST_TOTAL_LIMIT || maxCount <= 0) break;
            if (!channelMatched(item, terms)) continue;
            SearchSuggestItemVO s = new SearchSuggestItemVO();
            s.setKind("channel");
            s.setText(item.name());
            s.setDetailRoute(item.detailRoute());
            s.setHighlight(SearchTextHighlighter.highlight(item.name(), terms));
            if (addUniqueSuggest(out, dedup, s)) {
                maxCount--;
            }
        }
    }

    private boolean channelMatched(ChannelSuggestDef item, List<String> terms) {
        String nameLower = item.name().toLowerCase(Locale.ROOT);
        for (String term : terms) {
            if (!StringUtils.hasText(term)) continue;
            String t = term.toLowerCase(Locale.ROOT);
            if (nameLower.contains(t)) return true;
            for (String alias : item.aliases()) {
                if (asString(alias).toLowerCase(Locale.ROOT).contains(t)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void appendHotSuggestions(List<SearchSuggestItemVO> out, Set<String> dedup, int safeLimit, String keyword) {
        if (out.size() >= safeLimit) return;
        appendOpsHotSuggestions(out, dedup, safeLimit, keyword);
        appendStatsHotSuggestions(out, dedup, safeLimit);
    }

    /** Phase 7-B: ops-configured hot words take priority over search stats */
    private void appendOpsHotSuggestions(
            List<SearchSuggestItemVO> out,
            Set<String> dedup,
            int safeLimit,
            String keyword) {
        List<HomeHotWordVO> words = homeOpsService.listHotWords(null, true);
        String kw = keyword != null ? keyword.trim().toLowerCase(Locale.ROOT) : "";
        for (HomeHotWordVO word : words) {
            if (out.size() >= safeLimit) break;
            if (!StringUtils.hasText(word.getLabel())) continue;
            if (StringUtils.hasText(kw) && !word.getLabel().toLowerCase(Locale.ROOT).contains(kw)) {
                continue;
            }
            SearchSuggestItemVO s = new SearchSuggestItemVO();
            s.setText(word.getLabel());
            s.setKind("hot");
            s.setSubtitle(StringUtils.hasText(word.getBadge()) ? word.getBadge() : word.getActionType());
            s.setNavTarget(word.getNavTarget());
            addUniqueSuggest(out, dedup, s);
        }
    }

    private void appendStatsHotSuggestions(List<SearchSuggestItemVO> out, Set<String> dedup, int safeLimit) {
        if (out.size() >= safeLimit) return;
        List<Map<String, Object>> hotKeywords = getHotKeywords(SUGGEST_HOT_LIMIT);
        for (Map<String, Object> item : hotKeywords) {
            if (out.size() >= safeLimit) break;
            String text = String.valueOf(item.getOrDefault("keyword", ""));
            SearchSuggestItemVO s = new SearchSuggestItemVO();
            s.setText(text);
            s.setKind("hot");
            s.setHitCount(asInt(item.get("searchCount")));
            addUniqueSuggest(out, dedup, s);
        }
    }

    private void appendHistorySuggestions(List<SearchSuggestItemVO> out, Set<String> dedup, Long userId, int maxCount) {
        if (maxCount <= 0 || userId == null || userId <= 0) return;
        List<String> history = getSearchHistory(userId, maxCount);
        for (String h : history) {
            if (out.size() >= SUGGEST_TOTAL_LIMIT) break;
            SearchSuggestItemVO s = new SearchSuggestItemVO();
            s.setText(h);
            s.setKind("history");
            addUniqueSuggest(out, dedup, s);
        }
    }

    private boolean addUniqueSuggest(List<SearchSuggestItemVO> out, Set<String> dedup, SearchSuggestItemVO item) {
        if (item == null || !StringUtils.hasText(item.getText())) return false;
        String key = item.getText().trim().toLowerCase(Locale.ROOT);
        if (!dedup.add(key)) return false;
        out.add(item);
        return true;
    }

    private record ChannelSuggestDef(String name, List<String> aliases, String detailRoute) {}

    @Override
    public SearchRedirectVO redirect(String q) {
        SearchProperties.Redirect cfg = searchProperties.getRedirect();
        SearchRedirectVO out = new SearchRedirectVO();
        out.setDirectHit(false);
        out.setConfidence(0.0);
        out.setTarget(null);
        out.setReason("no_match");

        if (cfg == null || !cfg.isEnabled()) {
            out.setReason("disabled");
            return out;
        }

        String keyword = normalizeKeyword(q);
        if (!StringUtils.hasText(keyword)) {
            return out;
        }

        SearchGuardService.GuardResult guard = guardService.checkSuggestAllowed(keyword);
        if (!guard.allowed()) {
            out.setReason("blocked");
            return out;
        }

        SearchRedirectVO resolved = redirectResolver.resolve(
                keyword, documentQueryService, guardService.shouldHideVip(null));
        if (Boolean.TRUE.equals(resolved.getDirectHit())) {
            recordSearchKeyword(keyword, null);
            return resolved;
        }
        if ("vague_subject_use_suggest".equals(resolved.getReason())) {
            return resolved;
        }

        if (!documentQueryService.isIndexReady()) {
            return redirectLegacy(keyword, cfg);
        }
        return resolved;
    }

    private SearchRedirectVO redirectLegacy(String keyword, SearchProperties.Redirect cfg) {
        SearchRedirectVO out = new SearchRedirectVO();
        out.setDirectHit(false);
        out.setConfidence(0.0);
        out.setTarget(null);
        out.setReason("no_match");

        SearchSuggestItemVO catalogHit = SearchSiteCatalog.findExactCatalogEntry(keyword);
        if (catalogHit != null
                && (StringUtils.hasText(catalogHit.getDetailRoute()) || catalogHit.getRouteQuery() != null)) {
            out.setDirectHit(true);
            out.setConfidence(0.96);
            out.setTarget(catalogToResult(catalogHit));
            out.setReason("catalog_exact_match");
            recordSearchKeyword(keyword, null);
            return out;
        }

        ParsedSearchQuery parsed = intentRecognizer.recognize(keyword);
        List<String> highlightTerms = buildHighlightTerms(keyword, parsed);
        SearchAllResultVO data = executeSearch(keyword, 1, 5, parsed, "score", highlightTerms, null, null, "auto");
        List<SearchResultItemVO> records = data.getRecords() == null ? List.of() : data.getRecords();
        if (records.isEmpty()) {
            return out;
        }

        SearchResultItemVO top = records.get(0);
        double topScore = top.getScore() == null ? 0 : top.getScore();
        String topTitle = asString(top.getTitle());
        boolean exact = topTitle.equalsIgnoreCase(keyword);
        double secondScore = records.size() > 1 && records.get(1).getScore() != null
                ? records.get(1).getScore()
                : 0;
        boolean uniqueTop = records.size() == 1 || topScore - secondScore > cfg.getMinScoreGap();
        boolean titleOk = !cfg.isRequireExactTitle() || exact;
        if (titleOk && uniqueTop && topScore >= cfg.getMinTopScore() && "resource".equals(top.getDocType())) {
            out.setDirectHit(true);
            out.setConfidence(Math.min(0.99, 0.8 + topScore / 10.0));
            out.setTarget(top);
            out.setReason(exact ? "title_exact_match" : "title_high_confidence");
            recordSearchKeyword(keyword, null);
        }
        return out;
    }

    @Override
    public void recordClick(SearchClickRequest request, Long userId) {
        if (request == null || !StringUtils.hasText(request.getKeyword())) {
            return;
        }
        ParsedSearchQuery parsed = intentRecognizer.recognize(request.getKeyword());
        String normalized = parsed.getNormalizedQuery();
        String contentDomain = StringUtils.hasText(request.getContentDomain())
                ? request.getContentDomain()
                : parsed.getContentDomain();
        searchClickLogService.logClick(
                userId,
                request.getKeyword(),
                normalized,
                contentDomain,
                request.getDocId(),
                request.getResourceId(),
                request.getResourceType(),
                request.getClickType(),
                request.getPosition(),
                request.getDetailRoute());
    }

    @Override
    public SearchStatsVO searchStats(Integer days) {
        return searchAnalyticsService.stats(days == null || days < 1 ? 7 : days);
    }

    @Override
    public com.k12.common.dto.SearchP3ReadinessVO searchP3Readiness(Integer days) {
        return searchP3ReadinessService.evaluate(days == null || days < 1 ? 7 : days);
    }

    private List<SearchResultItemVO> searchPrimaryChinese(String term, List<String> highlightTerms) {
        LambdaQueryWrapper<PrimaryChineseResource> w = new LambdaQueryWrapper<>();
        w.eq(PrimaryChineseResource::getIsDeleted, 0)
                .eq(PrimaryChineseResource::getStatus, 1);
        if (StringUtils.hasText(term)) {
            w.and(q -> q.like(PrimaryChineseResource::getTitle, term)
                    .or().like(PrimaryChineseResource::getLessonName, term)
                    .or().like(PrimaryChineseResource::getRemark, term)
                    .or().like(PrimaryChineseResource::getType, term)
                    .or().like(PrimaryChineseResource::getUnitName, term)
                    .or().like(PrimaryChineseResource::getModule, term)
                    .or().like(PrimaryChineseResource::getEdition, term)
                    .or().like(PrimaryChineseResource::getCatalogPath, term));
        }
        w.last("LIMIT " + SEARCH_FETCH_LIMIT);
        List<PrimaryChineseResource> list = primaryChineseResourceMapper.selectList(w);
        List<SearchResultItemVO> docs = new ArrayList<>();
        for (PrimaryChineseResource r : list) {
            docs.add(buildDoc(
                    "primary_chinese",
                    r.getId(),
                    r.getTitle(),
                    defaultSummary(r.getRemark(), r.getLessonName()),
                    "primary",
                    "小学",
                    "stage_resource",
                    "学段资源",
                    r.getSubject(),
                    r.getGradeName(),
                    r.getType(),
                    r.getFileExt(),
                    r.getDownloadCount(),
                    r.getViewCount(),
                    r.getUploadTime() == null ? null : r.getUploadTime().toString(),
                    "/resource/" + r.getId() + "?from=subject",
                    term,
                    highlightTerms));
        }
        return docs;
    }

    private List<SearchResultItemVO> searchTopic(String term, List<String> highlightTerms) {
        LambdaQueryWrapper<TopicResource> w = new LambdaQueryWrapper<>();
        w.eq(TopicResource::getDeleted, 0)
                .eq(TopicResource::getStatus, 1)
                .and(q -> q.like(TopicResource::getTitle, term)
                        .or().like(TopicResource::getSummary, term)
                        .or().like(TopicResource::getTags, term)
                        .or().like(TopicResource::getTopicLabel, term))
                .last("LIMIT " + SEARCH_FETCH_LIMIT);
        List<TopicResource> list = topicResourceMapper.selectList(w);
        List<SearchResultItemVO> docs = new ArrayList<>();
        for (TopicResource r : list) {
            docs.add(buildDoc(
                    "topic",
                    r.getId(),
                    r.getTitle(),
                    defaultSummary(r.getSummary(), r.getTopicLabel()),
                    null,
                    null,
                    "topic",
                    "专题资源",
                    r.getSubject(),
                    null,
                    r.getResourceForm(),
                    r.getFileFormat(),
                    r.getDownloadCount(),
                    r.getViewCount(),
                    r.getCreateTime() == null ? null : r.getCreateTime().toString(),
                    "/topic-zone/resource/" + r.getId(),
                    term,
                    highlightTerms));
        }
        return docs;
    }

    private List<SearchResultItemVO> searchCompetition(String term, List<String> highlightTerms) {
        LambdaQueryWrapper<CompetitionResource> w = new LambdaQueryWrapper<>();
        w.eq(CompetitionResource::getDeleted, 0)
                .eq(CompetitionResource::getStatus, 1)
                .and(q -> q.like(CompetitionResource::getTitle, term)
                        .or().like(CompetitionResource::getSummary, term)
                        .or().like(CompetitionResource::getTags, term)
                        .or().like(CompetitionResource::getCompetitionName, term))
                .last("LIMIT " + SEARCH_FETCH_LIMIT);
        List<CompetitionResource> list = competitionResourceMapper.selectList(w);
        List<SearchResultItemVO> docs = new ArrayList<>();
        for (CompetitionResource r : list) {
            docs.add(buildDoc(
                    "competition",
                    r.getId(),
                    r.getTitle(),
                    defaultSummary(r.getSummary(), r.getCompetitionName()),
                    r.getGradeStage(),
                    resolveStageName(r.getGradeStage()),
                    "competition",
                    "竞赛专区",
                    r.getSubject(),
                    null,
                    r.getResourceForm(),
                    r.getFileFormat(),
                    r.getDownloadCount(),
                    r.getViewCount(),
                    r.getCreateTime() == null ? null : r.getCreateTime().toString(),
                    "/competition-zone/resource/" + r.getId(),
                    term,
                    highlightTerms));
        }
        return docs;
    }

    private List<SearchResultItemVO> searchCulture(String term, List<String> highlightTerms) {
        LambdaQueryWrapper<CultureResource> w = new LambdaQueryWrapper<>();
        w.eq(CultureResource::getDeleted, 0)
                .eq(CultureResource::getStatus, 1)
                .and(q -> q.like(CultureResource::getTitle, term)
                        .or().like(CultureResource::getSummary, term)
                        .or().like(CultureResource::getTags, term)
                        .or().like(CultureResource::getLocation, term))
                .last("LIMIT " + SEARCH_FETCH_LIMIT);
        List<CultureResource> list = cultureResourceMapper.selectList(w);
        List<SearchResultItemVO> docs = new ArrayList<>();
        for (CultureResource r : list) {
            docs.add(buildDoc(
                    "culture",
                    r.getId(),
                    r.getTitle(),
                    defaultSummary(r.getSummary(), r.getLocation()),
                    null,
                    null,
                    "culture",
                    "传统文化",
                    null,
                    null,
                    r.getCategory(),
                    r.getFileFormat(),
                    r.getDownloadCount(),
                    r.getViewCount(),
                    r.getCreateTime() == null ? null : r.getCreateTime().toString(),
                    "/culture",
                    term,
                    highlightTerms));
        }
        return docs;
    }

    private SearchResultItemVO buildDoc(
            String resourceType,
            Long resourceId,
            String title,
            String summary,
            String stageKey,
            String stageName,
            String channelKey,
            String channelName,
            String subject,
            String gradeName,
            String teachingType,
            String fileExt,
            Integer downloadCount,
            Integer viewCount,
            String uploadTime,
            String detailRoute,
            String keyword,
            List<String> highlightTerms) {
        SearchResultItemVO item = new SearchResultItemVO();
        item.setDocId(resourceType + ":" + resourceId);
        item.setResourceId(resourceId);
        item.setResourceType(resourceType);
        item.setTitle(title);
        item.setSummary(summary);
        item.setTitleHighlight(SearchTextHighlighter.highlight(title, highlightTerms));
        item.setSummaryHighlight(SearchTextHighlighter.snippet(
                defaultSummary(summary, title),
                highlightTerms,
                120));
        item.setStageKey(stageKey);
        item.setStageName(stageName);
        item.setChannelKey(channelKey);
        item.setChannelName(channelName);
        item.setSubject(subject);
        item.setGradeName(gradeName);
        item.setTeachingType(teachingType);
        item.setFileExt(fileExt);
        item.setDownloadCount(nvl(downloadCount));
        item.setViewCount(nvl(viewCount));
        item.setUploadTime(uploadTime);
        item.setDetailRoute(detailRoute);
        item.setScore(SearchTextHighlighter.relevanceScore(
                title,
                summary,
                teachingType,
                keyword,
                highlightTerms,
                nvl(downloadCount),
                nvl(viewCount)));
        enrichResultItem(item);
        return item;
    }

    private SearchResultItemVO catalogToResult(SearchSuggestItemVO catalogHit) {
        return SearchSiteCatalog.catalogToResultItem(catalogHit);
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

    private void sortResults(List<SearchResultItemVO> list, String sort) {
        String s = StringUtils.hasText(sort) ? sort.trim().toLowerCase(Locale.ROOT) : "score";
        if ("download".equals(s)) {
            list.sort(Comparator.comparingInt((SearchResultItemVO m) -> nvl(m.getDownloadCount())).reversed());
            return;
        }
        if ("newest".equals(s)) {
            list.sort(Comparator.comparing((SearchResultItemVO m) -> asString(m.getUploadTime()),
                    Comparator.nullsLast(Comparator.reverseOrder())));
            return;
        }
        list.sort(Comparator.comparingDouble((SearchResultItemVO m) -> m.getScore() == null ? 0 : m.getScore()).reversed());
    }

    private SearchFacetsVO buildFacets(List<SearchResultItemVO> records) {
        Map<String, Long> stageCnt = records.stream()
                .map(SearchResultItemVO::getStageKey)
                .filter(StringUtils::hasText)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        Map<String, Long> channelCnt = records.stream()
                .map(SearchResultItemVO::getChannelKey)
                .filter(StringUtils::hasText)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        Map<String, Long> typeCnt = records.stream()
                .map(SearchResultItemVO::getTeachingType)
                .filter(StringUtils::hasText)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

        SearchFacetsVO facets = new SearchFacetsVO();
        facets.setStages(toFacet(stageCnt, stageNameMap()));
        facets.setChannels(toFacet(channelCnt, channelNameMap()));
        facets.setTypes(toFacet(typeCnt, null));
        facets.setDomains(buildDomainFacets(records));
        return facets;
    }

    private List<SearchFacetBucketVO> buildDomainFacets(List<SearchResultItemVO> records) {
        Map<String, Long> domainCnt = records.stream()
                .map(SearchResultItemVO::getContentDomain)
                .filter(StringUtils::hasText)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        return domainCnt.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> {
                    SearchFacetBucketVO item = new SearchFacetBucketVO();
                    item.setKey(e.getKey());
                    item.setName(SearchSiteCatalog.domainName(e.getKey()));
                    item.setCount(e.getValue());
                    return item;
                }).toList();
    }

    private List<SearchFacetBucketVO> toFacet(Map<String, Long> source, Map<String, String> nameMap) {
        return source.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> {
                    SearchFacetBucketVO item = new SearchFacetBucketVO();
                    item.setKey(e.getKey());
                    item.setName(nameMap == null ? e.getKey() : nameMap.getOrDefault(e.getKey(), e.getKey()));
                    item.setCount(e.getValue());
                    return item;
                }).toList();
    }

    private Map<String, String> stageNameMap() {
        Map<String, String> map = new HashMap<>();
        map.put("preschool", "幼儿");
        map.put("primary", "小学");
        map.put("junior", "初中");
        map.put("senior", "高中");
        map.put("art", "美术");
        map.put("dance", "舞蹈");
        return map;
    }

    private Map<String, String> channelNameMap() {
        Map<String, String> map = new HashMap<>();
        map.put("stage_resource", "学段资源");
        map.put("class_meeting", "主题班会");
        map.put("career", "生涯规划");
        map.put("culture", "传统文化");
        map.put("competition", "竞赛专区");
        map.put("topic", "专题资源");
        return map;
    }

    private SearchAllResultVO emptySearchResult(int page, int size) {
        SearchAllResultVO data = new SearchAllResultVO();
        data.setRecords(List.of());
        data.setTotal(0);
        data.setPage(page);
        data.setSize(size);
        data.setPages(0);
        SearchFacetsVO facets = new SearchFacetsVO();
        facets.setStages(List.of());
        facets.setChannels(List.of());
        facets.setTypes(List.of());
        facets.setDomains(List.of());
        data.setFacets(facets);
        data.setCostMs(0);
        return data;
    }

    private String resolveStageName(String stageKey) {
        if (!StringUtils.hasText(stageKey)) return null;
        return stageNameMap().getOrDefault(stageKey, stageKey);
    }

    private String defaultSummary(String summary, String fallback) {
        if (StringUtils.hasText(summary)) return summary;
        if (StringUtils.hasText(fallback)) return fallback;
        return "暂无摘要";
    }

    private String normalizeKeyword(String q) {
        if (!StringUtils.hasText(q)) return "";
        return q.trim().replaceAll("\\s+", " ");
    }

    private void mergeDocs(Map<String, SearchResultItemVO> merged, List<SearchResultItemVO> docs) {
        for (SearchResultItemVO doc : docs) {
            SearchResultItemVO old = merged.get(doc.getDocId());
            if (old == null) {
                merged.put(doc.getDocId(), doc);
                continue;
            }
            double oldScore = old.getScore() == null ? 0 : old.getScore();
            double newScore = doc.getScore() == null ? 0 : doc.getScore();
            if (newScore > oldScore) {
                merged.put(doc.getDocId(), doc);
            }
        }
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private String asString(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private int asInt(Object v) {
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return 0;
        }
    }

}
