package com.k12.resource.search;

import com.k12.common.dto.SearchResultItemVO;
import com.k12.common.dto.SearchSuggestItemVO;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * P0 全站搜索静态目录：学段学科 / 特色频道 / 备课专区 / 教育资讯 / 资源类型
 * 在无强搜索引擎时提供规则化 suggest 与 redirect 兜底。
 */
public final class SearchSiteCatalog {

    private SearchSiteCatalog() {}

    private static final List<SubjectSuggestDef> SUBJECT_SUGGESTS = List.of(
            new SubjectSuggestDef("小学语文", "primary", "chinese", "小学 / 语文 / 同步资源"),
            new SubjectSuggestDef("初中语文", "junior", "chinese", "初中 / 语文 / 同步资源"),
            new SubjectSuggestDef("高中语文", "senior", "chinese", "高中 / 语文 / 同步资源"),
            new SubjectSuggestDef("小学数学", "primary", "math", "小学 / 数学 / 同步资源"),
            new SubjectSuggestDef("初中数学", "junior", "math", "初中 / 数学 / 同步资源"),
            new SubjectSuggestDef("高中数学", "senior", "math", "高中 / 数学 / 同步资源"),
            new SubjectSuggestDef("小学英语", "primary", "english", "小学 / 英语 / 同步资源"),
            new SubjectSuggestDef("初中英语", "junior", "english", "初中 / 英语 / 同步资源"),
            new SubjectSuggestDef("高中英语", "senior", "english", "高中 / 英语 / 同步资源")
    );

    private static final List<CatalogEntryDef> FEATURE_ENTRIES = List.of(
            new CatalogEntryDef("主题班会", "feature", "主题班会 / 德育", "/theme-class-meeting",
                    List.of("班会", "德育", "成长"), "channel"),
            new CatalogEntryDef("生涯规划", "feature", "生涯规划 / 升学指导", "/feature/shengya",
                    List.of("生涯", "志愿", "选课", "升学"), "feature"),
            new CatalogEntryDef("传统文化", "feature", "传统文化 / 国学", "/culture",
                    List.of("传统文化", "国学", "诗词", "书法"), "feature"),
            new CatalogEntryDef("竞赛专区", "feature", "竞赛专区 / 奥赛", "/competition",
                    List.of("竞赛", "奥赛", "奥数", "作文"), "feature")
    );

    private static final List<CatalogEntryDef> PREP_ENTRIES = List.of(
            new CatalogEntryDef("备课中心", "prep", "教案 / 课件 / 学案", "/lesson",
                    List.of("备课", "备课中心"), "prep"),
            new CatalogEntryDef("智能备课", "prep", "AI 辅助备课", "/lesson/smart",
                    List.of("智能备课", "智能"), "prep"),
            new CatalogEntryDef("组卷", "prep", "试卷 / 组卷", "/lesson/assemble",
                    List.of("组卷", "试卷", "出题"), "prep"),
            new CatalogEntryDef("试卷列表", "prep", "备课专区 / 试卷", "/lesson/papers",
                    List.of("试卷列表", "我的试卷"), "prep"),
            new CatalogEntryDef("资料篮", "prep", "备课专区 / 资源篮", "/lesson/basket",
                    List.of("资料篮", "资源篮"), "prep")
    );

    private static final List<CatalogEntryDef> NEWS_SUGGESTS = List.of(
            new CatalogEntryDef("教育局通知", "news", "教育资讯 / 通知", "/news/list?keyword=通知",
                    List.of("通知", "教育局", "教育局通知"), "news"),
            new CatalogEntryDef("政策资讯", "news", "教育资讯 / 政策", "/news/channel/policy",
                    List.of("政策", "资讯", "新闻"), "news"),
            new CatalogEntryDef("教育政策", "news", "教育资讯 / 政策", "/news/channel/policy",
                    List.of("教育政策", "政策文件"), "news"),
            new CatalogEntryDef("教研动态", "news", "教育资讯 / 教研", "/news/channel/research",
                    List.of("教研", "教研活动"), "news"),
            new CatalogEntryDef("升学备考", "news", "教育资讯 / 升学", "/news/channel/exam",
                    List.of("升学", "中高考"), "news"),
            new CatalogEntryDef("名师讲堂", "news", "教育资讯 / 名师", "/news/channel/teacher",
                    List.of("名师", "课例"), "news"),
            new CatalogEntryDef("教学改革", "news", "教育资讯 / 改革", "/news/channel/reform",
                    List.of("新课标", "改革"), "news")
    );

    private static final List<CatalogEntryDef> RESOURCE_TYPE_SUGGESTS = List.of(
            new CatalogEntryDef("课件", "stage_resource", "PPT / 课件资源", "/search/result?q=课件&type=课件",
                    List.of("课件", "ppt"), "resourceType"),
            new CatalogEntryDef("教案", "stage_resource", "教学设计 / 教案", "/search/result?q=教案&type=教案",
                    List.of("教案", "教学设计"), "resourceType"),
            new CatalogEntryDef("试卷", "stage_resource", "试卷 / 真题 / 模拟", "/search/result?q=试卷&type=试卷",
                    List.of("试卷", "真题", "模拟卷"), "resourceType")
    );

    private static final Map<String, String> DOMAIN_NAMES = Map.of(
            "stage_resource", "学段资源",
            "feature", "特色频道",
            "prep", "备课专区",
            "news", "教育资讯"
    );

    private static final Map<String, List<String>> DOMAIN_CHANNELS = Map.of(
            "stage_resource", List.of("stage_resource"),
            "feature", List.of("topic", "culture", "competition", "class_meeting", "career"),
            "prep", List.of("prep", "lesson"),
            "news", List.of("news")
    );

    public static List<String> domainChannelKeys(String domain) {
        if (!StringUtils.hasText(domain)) return List.of();
        return DOMAIN_CHANNELS.getOrDefault(domain.trim(), List.of());
    }

    public static String domainName(String domain) {
        if (!StringUtils.hasText(domain)) return domain;
        return DOMAIN_NAMES.getOrDefault(domain, domain);
    }

    public static String resolveContentDomain(String channelKey) {
        if (!StringUtils.hasText(channelKey)) return "stage_resource";
        return switch (channelKey) {
            case "stage_resource" -> "stage_resource";
            case "topic", "culture", "competition", "class_meeting", "career" -> "feature";
            case "prep", "lesson" -> "prep";
            case "news" -> "news";
            default -> "stage_resource";
        };
    }

    public static String resolveDocType(String channelKey, String resourceType) {
        if ("news".equals(channelKey)) return "news";
        if (Set.of("topic", "culture", "competition", "class_meeting", "career").contains(channelKey)) {
            return "channel";
        }
        if (Set.of("prep", "lesson").contains(channelKey)) return "prep";
        if (StringUtils.hasText(resourceType)) return "resource";
        return "resource";
    }

    public static List<SearchSuggestItemVO> matchSubjectSuggestions(String keyword, int maxCount) {
        List<SearchSuggestItemVO> out = new ArrayList<>();
        String kw = keyword.toLowerCase(Locale.ROOT);
        for (SubjectSuggestDef item : SUBJECT_SUGGESTS) {
            if (out.size() >= maxCount) break;
            if (!subjectMatched(item, kw)) continue;
            SearchSuggestItemVO s = new SearchSuggestItemVO();
            s.setKind("subject");
            s.setText(item.label());
            s.setSubtitle(item.subtitle());
            s.setContentDomain("stage_resource");
            s.setDetailRoute("/subject/" + item.stageKey() + "/" + item.subjectKey() + "/tongbian2024");
            out.add(s);
        }
        return out;
    }

    public static List<SearchSuggestItemVO> matchFeatureSuggestions(String keyword, int maxCount) {
        return matchCatalogSuggestions(FEATURE_ENTRIES, keyword, maxCount);
    }

    public static List<SearchSuggestItemVO> matchPrepSuggestions(String keyword, int maxCount) {
        return matchCatalogSuggestions(PREP_ENTRIES, keyword, maxCount);
    }

    public static List<SearchSuggestItemVO> matchNewsSuggestions(String keyword, int maxCount) {
        return matchCatalogSuggestions(NEWS_SUGGESTS, keyword, maxCount);
    }

    public static List<SearchSuggestItemVO> matchResourceTypeSuggestions(String keyword, int maxCount) {
        return matchCatalogSuggestions(RESOURCE_TYPE_SUGGESTS, keyword, maxCount);
    }

    public static List<SearchSuggestItemVO> matchCatalogSuggestions(
            List<CatalogEntryDef> catalog,
            String keyword,
            int maxCount) {
        List<SearchSuggestItemVO> out = new ArrayList<>();
        List<String> terms = tokenize(keyword);
        for (CatalogEntryDef item : catalog) {
            if (out.size() >= maxCount) break;
            if (!catalogMatched(item, keyword, terms)) continue;
            out.add(toSuggest(item));
        }
        return out;
    }

    public static SearchSuggestItemVO findExactCatalogEntry(String keyword) {
        String trimmed = keyword == null ? "" : keyword.trim();
        if (!StringUtils.hasText(trimmed)) return null;
        for (CatalogEntryDef item : allCatalogEntries()) {
            if (item.label().equalsIgnoreCase(trimmed)) {
                return toSuggest(item);
            }
        }
        return null;
    }

    public static List<SearchSuggestItemVO> buildCatalogIntents(String keyword) {
        List<SearchSuggestItemVO> out = new ArrayList<>();
        out.addAll(matchSubjectSuggestions(keyword, 3));
        out.addAll(matchFeatureSuggestions(keyword, 2));
        out.addAll(matchPrepSuggestions(keyword, 2));
        out.addAll(matchNewsSuggestions(keyword, 2));
        return out;
    }

    public static List<SearchResultItemVO> buildCatalogResults(String keyword, int maxCount) {
        List<SearchResultItemVO> out = new ArrayList<>();
        int limit = maxCount;
        appendCatalogResults(out, matchNewsSuggestions(keyword, limit), limit);
        appendCatalogResults(out, matchPrepSuggestions(keyword, limit - out.size()), limit);
        appendCatalogResults(out, matchFeatureSuggestions(keyword, limit - out.size()), limit);
        appendCatalogResults(out, matchSubjectSuggestions(keyword, limit - out.size()), limit);
        return out;
    }

    private static void appendCatalogResults(
            List<SearchResultItemVO> out,
            List<SearchSuggestItemVO> items,
            int maxCount) {
        for (SearchSuggestItemVO item : items) {
            if (out.size() >= maxCount) break;
            out.add(catalogToResultItem(item));
        }
    }

    public static SearchResultItemVO catalogToResultItem(SearchSuggestItemVO item) {
        SearchResultItemVO row = new SearchResultItemVO();
        row.setDocId("catalog:" + item.getText());
        row.setTitle(item.getText());
        row.setSummary(item.getSubtitle());
        row.setSubtitle(item.getSubtitle());
        row.setDetailRoute(item.getDetailRoute());
        row.setRouteQuery(item.getRouteQuery());
        row.setContentDomain(item.getContentDomain());
        row.setDocType(item.getKind());
        row.setResourceType(item.getKind());
        row.setBizId(item.getText());
        row.setScore(1.0);
        if ("news".equals(item.getContentDomain())) {
            row.setChannelName("教育资讯");
            row.setChannelKey("news");
        } else if ("prep".equals(item.getContentDomain())) {
            row.setChannelName("备课专区");
            row.setChannelKey("prep");
        } else if ("feature".equals(item.getContentDomain())) {
            row.setChannelName("特色频道");
        } else if ("subject".equals(item.getKind())) {
            row.setChannelName("学段资源");
            row.setChannelKey("stage_resource");
        }
        return row;
    }

    public static List<SearchSuggestItemVO> buildSubjectIntents(String keyword) {
        return matchSubjectSuggestions(keyword, 3);
    }

    public static List<SearchSuggestItemVO> buildZeroRecommendations() {
        List<SearchSuggestItemVO> out = new ArrayList<>();
        for (CatalogEntryDef item : FEATURE_ENTRIES) {
            out.add(toSuggest(item));
        }
        for (CatalogEntryDef item : PREP_ENTRIES) {
            out.add(toSuggest(item));
        }
        return out;
    }

    private static List<CatalogEntryDef> allCatalogEntries() {
        List<CatalogEntryDef> all = new ArrayList<>();
        all.addAll(FEATURE_ENTRIES);
        all.addAll(PREP_ENTRIES);
        all.addAll(NEWS_SUGGESTS);
        all.addAll(RESOURCE_TYPE_SUGGESTS);
        return all;
    }

    private static SearchSuggestItemVO toSuggest(CatalogEntryDef item) {
        SearchSuggestItemVO s = new SearchSuggestItemVO();
        s.setKind(item.kind());
        s.setText(item.label());
        s.setSubtitle(item.subtitle());
        s.setContentDomain(item.contentDomain());
        if (StringUtils.hasText(item.detailRoute())) {
            s.setDetailRoute(item.detailRoute());
        } else {
            Map<String, String> routeQuery = new LinkedHashMap<>();
            routeQuery.put("type", item.label());
            s.setRouteQuery(routeQuery);
        }
        return s;
    }

    private static boolean subjectMatched(SubjectSuggestDef item, String kwLower) {
        String labelLower = item.label().toLowerCase(Locale.ROOT);
        if (labelLower.contains(kwLower) || kwLower.contains(item.subjectKey())) return true;
        for (String subject : List.of("语文", "数学", "英语", "物理", "化学", "生物")) {
            if (kwLower.contains(subject.toLowerCase(Locale.ROOT)) && labelLower.contains(subject)) {
                return true;
            }
        }
        return false;
    }

    private static boolean catalogMatched(CatalogEntryDef item, String keyword, List<String> terms) {
        String labelLower = item.label().toLowerCase(Locale.ROOT);
        String kwLower = keyword.toLowerCase(Locale.ROOT);
        if (labelLower.contains(kwLower) || kwLower.contains(labelLower)) return true;
        for (String alias : item.aliases()) {
            String aliasLower = alias.toLowerCase(Locale.ROOT);
            if (kwLower.contains(aliasLower) || aliasLower.contains(kwLower)) return true;
        }
        for (String term : terms) {
            if (labelLower.contains(term) || item.aliases().stream().anyMatch(a -> a.toLowerCase(Locale.ROOT).contains(term))) {
                return true;
            }
        }
        return false;
    }

    private static List<String> tokenize(String keyword) {
        List<String> tokens = new ArrayList<>();
        if (!StringUtils.hasText(keyword)) return tokens;
        for (String part : keyword.trim().split("[\\s,，;；]+")) {
            if (StringUtils.hasText(part)) tokens.add(part.toLowerCase(Locale.ROOT));
        }
        return tokens;
    }

    private record SubjectSuggestDef(String label, String stageKey, String subjectKey, String subtitle) {}

    private record CatalogEntryDef(
            String label,
            String contentDomain,
            String subtitle,
            String detailRoute,
            List<String> aliases,
            String kind) {}
}
