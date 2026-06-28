package com.k12.resource.search;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 搜索高亮与摘要片段（P2：避免嵌套 em、长词优先、摘要窗口）
 */
public final class SearchTextHighlighter {

    private static final int DEFAULT_SNIPPET_LEN = 120;
    private static final int SNIPPET_PADDING = 36;

    private SearchTextHighlighter() {
    }

    public static String highlight(String text, List<String> terms) {
        if (!StringUtils.hasText(text) || terms == null || terms.isEmpty()) {
            return text;
        }
        List<String> sorted = terms.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();
        if (sorted.isEmpty()) {
            return text;
        }

        List<int[]> ranges = new ArrayList<>();
        for (String term : sorted) {
            Pattern pattern = Pattern.compile(Pattern.quote(term), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                addRange(ranges, matcher.start(), matcher.end());
            }
        }
        if (ranges.isEmpty()) {
            return text;
        }
        ranges.sort(Comparator.comparingInt(a -> a[0]));

        StringBuilder sb = new StringBuilder();
        int cursor = 0;
        for (int[] range : ranges) {
            if (range[0] > cursor) {
                sb.append(text, cursor, range[0]);
            }
            sb.append("<em>").append(text, range[0], range[1]).append("</em>");
            cursor = range[1];
        }
        if (cursor < text.length()) {
            sb.append(text.substring(cursor));
        }
        return sb.toString();
    }

    public static String snippet(String text, List<String> terms, int maxLen) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        int safeLen = maxLen <= 0 ? DEFAULT_SNIPPET_LEN : maxLen;
        if (text.length() <= safeLen) {
            return highlight(text, terms);
        }
        int hit = findFirstHit(text, terms);
        if (hit < 0) {
            return text.substring(0, Math.min(text.length(), safeLen)) + (text.length() > safeLen ? "…" : "");
        }
        int start = Math.max(0, hit - SNIPPET_PADDING);
        int end = Math.min(text.length(), start + safeLen);
        if (end - start < safeLen && end == text.length()) {
            start = Math.max(0, end - safeLen);
        }
        String part = text.substring(start, end);
        String prefix = start > 0 ? "…" : "";
        String suffix = end < text.length() ? "…" : "";
        return prefix + highlight(part, terms) + suffix;
    }

    public static double relevanceScore(
            String title,
            String summary,
            String teachingType,
            String keyword,
            List<String> terms,
            int downloadCount,
            int viewCount) {
        String k = keyword == null ? "" : keyword.toLowerCase(Locale.ROOT);
        double score = 0;
        String t = safeLower(title);
        String s = safeLower(summary);
        String tp = safeLower(teachingType);

        if (StringUtils.hasText(k)) {
            if (t.equals(k)) {
                score += 6;
            } else if (t.startsWith(k)) {
                score += 5;
            } else if (t.contains(k)) {
                score += 4;
            }
        }
        for (String term : terms == null ? List.<String>of() : terms) {
            if (!StringUtils.hasText(term)) {
                continue;
            }
            String token = term.toLowerCase(Locale.ROOT);
            if (t.startsWith(token)) {
                score += 2.5;
            } else if (t.contains(token)) {
                score += 1.8;
            }
            if (tp.contains(token)) {
                score += 2.0;
            }
            if (s.contains(token)) {
                score += 1.0;
            }
        }
        score += Math.log1p(downloadCount) * 0.2 + Math.log1p(viewCount) * 0.1;
        return score;
    }

    private static int findFirstHit(String text, List<String> terms) {
        if (terms == null) {
            return -1;
        }
        int best = -1;
        String lower = text.toLowerCase(Locale.ROOT);
        for (String term : terms) {
            if (!StringUtils.hasText(term)) {
                continue;
            }
            int idx = lower.indexOf(term.toLowerCase(Locale.ROOT));
            if (idx >= 0 && (best < 0 || idx < best)) {
                best = idx;
            }
        }
        return best;
    }

    private static void addRange(List<int[]> ranges, int start, int end) {
        for (int[] existing : ranges) {
            if (start <= existing[1] && end >= existing[0]) {
                existing[0] = Math.min(existing[0], start);
                existing[1] = Math.max(existing[1], end);
                return;
            }
        }
        ranges.add(new int[] {start, end});
    }

    private static String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
