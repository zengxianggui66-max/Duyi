package com.k12.resource.search;

import com.k12.common.dto.SearchRedirectVO;
import com.k12.common.dto.SearchResultItemVO;
import com.k12.common.dto.SearchSuggestItemVO;
import com.k12.resource.entity.SysSearchRedirect;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

/**
 * P1 搜索直达规则：DB 运营规则 &gt; 硬编码频道 &gt; 目录 &gt; 标题精确匹配。
 * 泛学科词不直跳。
 */
@Component
public class SearchRedirectResolver {

    private static final Map<String, String> EXACT_ROUTES = Map.ofEntries(
            Map.entry("主题班会", "/theme-class-meeting"),
            Map.entry("传统文化", "/culture"),
            Map.entry("竞赛专区", "/competition"),
            Map.entry("备课", "/lesson"),
            Map.entry("智能备课", "/lesson/smart"),
            Map.entry("教育资讯", "/news"),
            Map.entry("备课中心", "/lesson"),
            Map.entry("组卷", "/lesson/assemble"),
            Map.entry("教育局通知", "/news/list?keyword=通知"),
            Map.entry("政策资讯", "/news/list?keyword=政策"),
            Map.entry("帮助中心", "/help"),
            Map.entry("专题资源", "/topic"),
            Map.entry("生涯规划", "/topic")
    );

    private final SearchRedirectRuleService redirectRuleService;
    public SearchRedirectResolver(SearchRedirectRuleService redirectRuleService) {
        this.redirectRuleService = redirectRuleService;
    }


    public SearchRedirectVO resolve(String keyword, SearchDocumentQueryService queryService, boolean hideVip) {
        SearchRedirectVO out = new SearchRedirectVO();
        out.setDirectHit(false);
        out.setConfidence(0.0);
        out.setTarget(null);
        out.setReason("no_match");

        String q = keyword == null ? "" : keyword.trim();
        if (!StringUtils.hasText(q)) {
            return out;
        }

        if (SearchRedirectConstants.isVagueSubject(q)) {
            out.setReason("vague_subject_use_suggest");
            return out;
        }

        Optional<SysSearchRedirect> dbRule = redirectRuleService.findRule(q);
        if (dbRule.isPresent() && StringUtils.hasText(dbRule.get().getRoutePath())) {
            String title = StringUtils.hasText(dbRule.get().getTitle()) ? dbRule.get().getTitle() : q;
            return buildHit(out, title, dbRule.get().getRoutePath(), "db_redirect_rule", 0.98);
        }

        String exactRoute = EXACT_ROUTES.get(q);
        if (StringUtils.hasText(exactRoute)) {
            return buildHit(out, q, exactRoute, "channel_exact_match", 0.97);
        }

        SearchSuggestItemVO catalogHit = SearchSiteCatalog.findExactCatalogEntry(q);
        if (catalogHit != null && StringUtils.hasText(catalogHit.getDetailRoute())) {
            out.setDirectHit(true);
            out.setConfidence(0.96);
            out.setTarget(SearchSiteCatalog.catalogToResultItem(catalogHit));
            out.setReason("catalog_exact_match");
            return out;
        }

        if (queryService != null && queryService.isIndexReady()) {
            ParsedSearchQuery parsed = new ParsedSearchQuery();
            parsed.setRawQuery(q);
            SearchResultItemVO exact = queryService.findExactTitle(parsed, q, hideVip);
            if (exact != null && "resource".equals(exact.getDocType())) {
                out.setDirectHit(true);
                out.setConfidence(0.92);
                out.setTarget(exact);
                out.setReason("title_exact_match");
                return out;
            }
        }

        return out;
    }

    private SearchRedirectVO buildHit(SearchRedirectVO out, String title, String route, String reason, double confidence) {
        SearchResultItemVO target = new SearchResultItemVO();
        target.setDocId("redirect:" + title);
        target.setTitle(title);
        target.setDetailRoute(route);
        target.setDocType("channel");
        target.setScore(confidence);
        out.setDirectHit(true);
        out.setConfidence(confidence);
        out.setTarget(target);
        out.setReason(reason);
        return out;
    }

    public boolean isVagueSubject(String keyword) {
        return SearchRedirectConstants.isVagueSubject(keyword);
    }
}
