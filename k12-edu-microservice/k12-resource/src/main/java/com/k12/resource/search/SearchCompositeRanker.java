package com.k12.resource.search;

import com.k12.resource.entity.SysSearchDocument;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

/**
 * P2 综合排序：文本 45% + 意图 25% + 质量 12% + 热度 8% + 新鲜度 6% + 点击 4%
 */
public final class SearchCompositeRanker {

    private static final double W_TEXT = 0.45;
    private static final double W_INTENT = 0.25;
    private static final double W_QUALITY = 0.12;
    private static final double W_HOT = 0.08;
    private static final double W_FRESH = 0.06;
    private static final double W_CLICK = 0.04;

    private SearchCompositeRanker() {
    }

    public static double score(
            SysSearchDocument doc,
            ParsedSearchQuery parsed,
            List<String> highlightTerms,
            SearchRecallCandidate candidate) {
        double text = textScore(doc, parsed, highlightTerms, candidate);
        double intent = intentScore(doc, parsed);
        double quality = qualityScore(doc);
        double hot = hotScore(doc);
        double fresh = freshnessScore(doc);
        double click = clickScore(candidate);
        return text * W_TEXT + intent * W_INTENT + quality * W_QUALITY
                + hot * W_HOT + fresh * W_FRESH + click * W_CLICK;
    }

    private static double textScore(
            SysSearchDocument doc,
            ParsedSearchQuery parsed,
            List<String> highlightTerms,
            SearchRecallCandidate candidate) {
        double raw = SearchTextHighlighter.relevanceScore(
                doc.getTitle(),
                doc.getSummary(),
                doc.getResourceTypeName(),
                parsed.getRawQuery(),
                highlightTerms,
                doc.getDownloadCount() == null ? 0 : doc.getDownloadCount(),
                doc.getViewCount() == null ? 0 : doc.getViewCount());
        if (candidate != null && candidate.getPaths().contains(SearchRecallPath.EXACT_TITLE)) {
            raw += 8;
        }
        if (candidate != null && candidate.getPaths().contains(SearchRecallPath.TOKEN_AND)) {
            raw += 2;
        }
        if (parsed != null && isSubjectOnlyQuery(parsed)) {
            if ("subject".equals(safe(doc.getDocType()))) {
                raw += 10;
            } else if ("page".equals(safe(doc.getDocType()))) {
                raw += 6;
            }
        }
        if (parsed != null && "news".equals(parsed.getContentDomain()) && "news".equals(safe(doc.getDocType()))) {
            raw += 5;
        }
        return Math.min(100, raw * 12);
    }

    private static boolean isSubjectOnlyQuery(ParsedSearchQuery parsed) {
        return parsed != null
                && StringUtils.hasText(parsed.getSubject())
                && !StringUtils.hasText(parsed.getGradeName())
                && !StringUtils.hasText(parsed.getTeachingType())
                && !StringUtils.hasText(parsed.getStageKey())
                && !StringUtils.hasText(parsed.getChannelKey());
    }

    private static String safe(String v) {
        return v == null ? "" : v;
    }

    private static double intentScore(SysSearchDocument doc, ParsedSearchQuery parsed) {
        if (parsed == null) {
            return 0;
        }
        double score = 0;
        int checks = 0;
        if (StringUtils.hasText(parsed.getStageKey())) {
            checks++;
            if (parsed.getStageKey().equalsIgnoreCase(safe(doc.getStageKey()))) {
                score += 25;
            }
        }
        if (StringUtils.hasText(parsed.getSubjectKey()) || StringUtils.hasText(parsed.getSubject())) {
            checks++;
            if (matchesSubject(doc, parsed)) {
                score += 25;
            }
        }
        if (StringUtils.hasText(parsed.getGradeName())) {
            checks++;
            if (containsSafe(doc.getGradeName(), parsed.getGradeName())) {
                score += 20;
            }
        }
        if (StringUtils.hasText(parsed.getTeachingType()) || StringUtils.hasText(parsed.getResourceTypeKey())) {
            checks++;
            if (matchesType(doc, parsed)) {
                score += 20;
            }
        }
        if (StringUtils.hasText(parsed.getModuleKey()) || StringUtils.hasText(parsed.getModuleName())) {
            checks++;
            if (matchesModule(doc, parsed)) {
                score += 10;
            }
        }
        if (StringUtils.hasText(parsed.getChannelKey())) {
            checks++;
            if (parsed.getChannelKey().equalsIgnoreCase(safe(doc.getChannelKey()))) {
                score += 15;
            }
        }
        if (checks == 0) {
            return 40;
        }
        if (isSubjectOnlyQuery(parsed)) {
            if ("subject".equals(safe(doc.getDocType()))) {
                score = Math.max(score, 90);
            } else if ("page".equals(safe(doc.getDocType()))) {
                score = Math.max(score, 75);
            }
        }
        if ("news".equals(parsed.getContentDomain()) && "news".equals(safe(doc.getDocType()))) {
            score = Math.max(score, 80);
        }
        return Math.min(100, score);
    }

    private static double qualityScore(SysSearchDocument doc) {
        if (doc.getQualityScore() != null) {
            return Math.min(100, doc.getQualityScore().doubleValue() * 20);
        }
        return 50;
    }

    private static double hotScore(SysSearchDocument doc) {
        double hot = doc.getHotScore() == null ? 0 : doc.getHotScore();
        int dl = doc.getDownloadCount() == null ? 0 : doc.getDownloadCount();
        int view = doc.getViewCount() == null ? 0 : doc.getViewCount();
        double raw = hot + Math.log1p(dl) * 3 + Math.log1p(view) * 1.5;
        return Math.min(100, raw * 5);
    }

    private static double freshnessScore(SysSearchDocument doc) {
        LocalDateTime pt = doc.getPublishTime();
        if (pt == null) {
            return 40;
        }
        long days = ChronoUnit.DAYS.between(pt.toLocalDate(), LocalDateTime.now().toLocalDate());
        if (days <= 7) {
            return 100;
        }
        if (days <= 30) {
            return 85;
        }
        if (days <= 90) {
            return 65;
        }
        if (days <= 365) {
            return 45;
        }
        return 25;
    }

    private static double clickScore(SearchRecallCandidate candidate) {
        if (candidate == null || candidate.getClickCount() <= 0) {
            return candidate != null && candidate.getPaths().contains(SearchRecallPath.HOT_CLICK) ? 60 : 0;
        }
        return Math.min(100, 30 + candidate.getClickCount() * 10);
    }

    private static boolean matchesSubject(SysSearchDocument doc, ParsedSearchQuery parsed) {
        if (StringUtils.hasText(parsed.getSubjectKey())
                && parsed.getSubjectKey().equalsIgnoreCase(safe(doc.getSubjectKey()))) {
            return true;
        }
        if (StringUtils.hasText(parsed.getSubject())
                && containsSafe(doc.getSubjectName(), parsed.getSubject())) {
            return true;
        }
        return false;
    }

    private static boolean matchesType(SysSearchDocument doc, ParsedSearchQuery parsed) {
        if (StringUtils.hasText(parsed.getResourceTypeKey())
                && parsed.getResourceTypeKey().equalsIgnoreCase(safe(doc.getResourceTypeKey()))) {
            return true;
        }
        if (StringUtils.hasText(parsed.getTeachingType())
                && containsSafe(doc.getResourceTypeName(), parsed.getTeachingType())) {
            return true;
        }
        return false;
    }

    private static boolean matchesModule(SysSearchDocument doc, ParsedSearchQuery parsed) {
        if (StringUtils.hasText(parsed.getModuleKey())
                && parsed.getModuleKey().equalsIgnoreCase(safe(doc.getModuleKey()))) {
            return true;
        }
        return StringUtils.hasText(parsed.getModuleName())
                && containsSafe(doc.getModuleName(), parsed.getModuleName());
    }

    private static boolean containsSafe(String haystack, String needle) {
        if (!StringUtils.hasText(haystack) || !StringUtils.hasText(needle)) {
            return false;
        }
        return haystack.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }
}
